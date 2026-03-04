package com.cool.server.service.impl;

import com.cool.common.constant.MessageConstant;
import com.cool.common.constant.RedisConstant;
import com.cool.common.properties.JwtProperties;
import com.cool.common.utils.JwtUtil;
import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.dto.UserLoginDTO;
import com.cool.pojo.dto.UserRegisterDTO;
import com.cool.pojo.entity.User;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.UserLoginVO;
import com.cool.pojo.vo.UserVO;
import com.cool.server.context.BaseContext;
import com.cool.common.enumeration.OnlineStatus;
import com.cool.server.mapper.UserMapper;
import com.cool.server.service.FollowService;
import com.cool.server.service.OnlineStatusService;
import com.cool.server.service.UserService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.cool.common.constant.RedisConstant.TOKEN_EXPIRE_TIME;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final JwtProperties jwtProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final OnlineStatusService onlineStatusService;
    private final FollowService followService;
    private final String aliyunOssEndpoint;
    private final String aliyunOssAccessKeyId;
    private final String aliyunOssAccessKeySecret;
    private final String aliyunOssBucketName;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, JwtProperties jwtProperties, StringRedisTemplate stringRedisTemplate, OnlineStatusService onlineStatusService, FollowService followService,
                          @Value("${aliyun.oss.endpoint}") String aliyunOssEndpoint,
                          @Value("${aliyun.oss.access-key-id}") String aliyunOssAccessKeyId,
                          @Value("${aliyun.oss.access-key-secret}") String aliyunOssAccessKeySecret,
                          @Value("${aliyun.oss.bucket-name}") String aliyunOssBucketName,
                           JwtUtil jwtUtil,
                           PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.jwtProperties = jwtProperties;
        this.stringRedisTemplate = stringRedisTemplate;
        this.onlineStatusService = onlineStatusService;
        this.followService = followService;
        this.aliyunOssEndpoint = aliyunOssEndpoint;
        this.aliyunOssAccessKeyId = aliyunOssAccessKeyId;
        this.aliyunOssAccessKeySecret = aliyunOssAccessKeySecret;
        this.aliyunOssBucketName = aliyunOssBucketName;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserLoginVO login(UserLoginDTO dto) {

        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new BusinessException(MessageConstant.USERNAME_NOT_NULL);
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new BusinessException(MessageConstant.PASSWORD_NOT_NULL);
        }

        User user = userMapper.getByUsername(dto.getUsername());
        if (user == null) {
            throw new RuntimeException(MessageConstant.USER_NOT_FOUND);
        }
        
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("登录失败：密码错误，username={}", dto.getUsername());
            throw new BusinessException(MessageConstant.PASSWORD_ERROR);
        }
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());

        String accessToken = jwtUtil.generateAccessToken(claims);
        String refreshToken = jwtUtil.generateRefreshToken(dto.getUsername(),user.getId(),dto.getDeviceId());
        

        String refreshTokenKey = RedisConstant.USER_TOKEN_KEY + "refresh:" + user.getId();
        stringRedisTemplate.opsForValue().set(refreshTokenKey, refreshToken, RedisConstant.REFRESH_TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);
        
        log.info("用户登录成功，生成双token: userId={}", user.getId());
        
        UserLoginVO vo = new UserLoginVO();
        BeanUtil.copyProperties(user, vo);
        vo.setToken(accessToken);
        vo.setRefreshToken(refreshToken);
        
        return vo;
    }

    @Override
    public void register(UserRegisterDTO dto) {
        // 1. 核心参数校验（避免空指针/无效数据）
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new BusinessException(MessageConstant.USERNAME_NOT_NULL);
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new BusinessException("密码不能为空且长度不能少于6位");
        }
//        if (dto.getPhone() == null || dto.getPhone().isBlank()) { // 若有手机号字段
//            throw new BusinessException(MessageConstant.PHONE_NOT_NULL);
//        }

        // 2. 统一用户名格式（可选：转小写，避免大小写重复）
        String username = dto.getUsername().trim().toLowerCase();

        // 3. 校验用户名是否已存在
        User existingUser = userMapper.getByUsername(username);
        if (existingUser != null) {
            log.warn("注册失败：用户名已存在，username={}", username);
            throw new BusinessException(MessageConstant.USERNAME_EXISTS);
        }

        // 4. 封装用户对象（核心：改用BCrypt加密密码）
        User user = new User();
        BeanUtil.copyProperties(dto, user);
        user.setUsername(username); // 存入统一格式的用户名
        // 关键修复：用PasswordEncoder加密密码（BCrypt），替代MD5
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(1); // 1=启用
        user.setRole(0);   // 0=普通用户

        // 5. 插入数据库
        userMapper.insert(user);
        log.info("用户注册成功，userId={}, username={}", user.getId(), username);
    }

    @Override
    public void logout() {
        Long userId = BaseContext.getCurrentId();
        if (userId != null) {
            String refreshTokenKey = RedisConstant.USER_TOKEN_KEY + "refresh:" + userId;
            stringRedisTemplate.delete(refreshTokenKey);
            log.info("用户登出，删除刷新令牌: userId={}", userId);
        }
    }

    @Override
    public UserVO getUserInfo() {
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new RuntimeException(MessageConstant.USER_NOT_FOUND);
        }
        
        UserVO vo = new UserVO();
        BeanUtil.copyProperties(user, vo);
        vo.setFollowingCount(userMapper.getFollowingCount(userId));
        vo.setFollowerCount(userMapper.getFollowerCount(userId));
        
        // 获取在线状态
        OnlineStatus status = onlineStatusService.getStatus(userId);
        vo.setOnlineStatus(status.getCode());
        
        return vo;
    }

    @Override
    public UserVO getUserInfoById(Long id) {
        User user = userMapper.getById(id);
        if (user == null) {
            throw new RuntimeException(MessageConstant.USER_NOT_FOUND);
        }
        
        UserVO vo = new UserVO();
        BeanUtil.copyProperties(user, vo);
        vo.setFollowingCount(userMapper.getFollowingCount(id));
        vo.setFollowerCount(userMapper.getFollowerCount(id));
        
        // 获取在线状态
        OnlineStatus status = onlineStatusService.getStatus(id);
        vo.setOnlineStatus(status.getCode());
        
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId != null) {
            vo.setIsFollowing(isFollowing(currentUserId, id));
        }
        
        return vo;
    }

    @Override
    public void updateUserInfo(UserVO vo) {
        Long userId = BaseContext.getCurrentId();
        User user = new User();
        BeanUtil.copyProperties(vo, user);
        user.setId(userId);
        userMapper.update(user);
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        OSS ossClient = null;
        try {
            // 初始化OSS客户端
            ossClient = new OSSClientBuilder().build(aliyunOssEndpoint, aliyunOssAccessKeyId, aliyunOssAccessKeySecret);
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = "avatars/" + System.currentTimeMillis() + suffix;
            
            // 上传文件到OSS
            ossClient.putObject(aliyunOssBucketName, filename, file.getInputStream());
            
            // 构建文件URL
            String fileUrl = "https://" + aliyunOssBucketName + "." + aliyunOssEndpoint + "/" + filename;
            
            // 返回文件URL
            return fileUrl;
        } catch (Exception e) {
            log.error("上传头像失败", e);
            throw new RuntimeException("上传头像失败");
        } finally {
            // 关闭OSS客户端
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Override
    public void followUser(Long id) {
        followService.follow(id, FollowService.TYPE_USER);
    }

    @Override
    public void unfollowUser(Long id) {
        followService.unfollow(id, FollowService.TYPE_USER);
    }

    @Override
    public PageVO<UserVO> getUserList(PageQueryDTO dto) {
        Long total = userMapper.count(dto);
        int offset = (dto.getPage() - 1) * dto.getPageSize();
        List<UserVO> users = userMapper.list(dto);
        return PageVO.of(users, total, dto.getPage(), dto.getPageSize());
    }

    @Override
    public void banUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setStatus(0);
        userMapper.update(user);
        
        stringRedisTemplate.delete(RedisConstant.USER_TOKEN_KEY + "refresh:" + id);
        log.info("用户被禁用，删除刷新令牌: userId={}", id);
    }

    @Override
    public void unbanUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setStatus(1);
        userMapper.update(user);
    }

    @Override
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
        stringRedisTemplate.delete(RedisConstant.USER_TOKEN_KEY + "refresh:" + id);
        log.info("用户被删除，删除刷新令牌: userId={}", id);
    }

    @Override
    public PageVO<UserVO> getFollowingList(Long userId, PageQueryDTO dto) {
        int offset = (dto.getPage() - 1) * dto.getPageSize();
        List<UserVO> users = userMapper.getFollowingList(userId, offset, dto.getPageSize());
        Long total = userMapper.countFollows(userId, 0);
        
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId != null) {
            users.forEach(user -> {
                user.setIsFollowing(isFollowing(currentUserId, user.getId()));
            });
        }
        
        return PageVO.of(users, total, dto.getPage(), dto.getPageSize());
    }

    @Override
    public PageVO<UserVO> getFollowerList(Long userId, PageQueryDTO dto) {
        int offset = (dto.getPage() - 1) * dto.getPageSize();
        List<UserVO> users = userMapper.getFollowerList(userId, offset, dto.getPageSize());
        Long total = userMapper.countFollows(userId, 0);
        
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId != null) {
            users.forEach(user -> {
                user.setIsFollowing(isFollowing(currentUserId, user.getId()));
            });
        }
        
        return PageVO.of(users, total, dto.getPage(), dto.getPageSize());
    }

    @Override
    public boolean isFollowing(Long userId, Long targetId) {
        return followService.isFollowing(userId, targetId, FollowService.TYPE_USER);
    }

    @Override
    public void kickUser(Long id) {
        stringRedisTemplate.delete(RedisConstant.USER_TOKEN_KEY + "refresh:" + id);
        log.info("用户被踢下线，删除刷新令牌: userId={}", id);
    }

    @Override
    public Map<String, Object> getOnlineUserStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取在线用户总数
        long totalOnline = onlineStatusService.getOnlineUserCount();
        stats.put("totalOnline", totalOnline);
        
        // 获取不同状态的在线用户数
        Map<OnlineStatus, Long> statusCounts = onlineStatusService.getOnlineUserCountByStatus();
        stats.put("statusCounts", statusCounts);
        
        // 获取最近登录的用户
        List<UserVO> recentUsers = onlineStatusService.getRecentOnlineUsers(10);
        stats.put("recentUsers", recentUsers);
        
        return stats;
    }

    public UserLoginVO refreshToken(String refreshToken, String deviceId) {
        // ========== 1. 基础校验：刷新令牌非空 ==========
        if (refreshToken == null || refreshToken.isBlank()) {
            log.error("刷新令牌为空");
            throw new BusinessException(MessageConstant.REFRESH_TOKEN_EMPTY);
        }

        // ========== 2. 校验刷新令牌有效性（JWT层面） ==========
        // 解析令牌（JwtUtil已做异常处理，返回null则无效）
        Claims refreshTokenClaims = jwtUtil.parseToken(refreshToken);
        if (refreshTokenClaims == null) {
            log.error("刷新令牌解析失败，token={}", maskToken(refreshToken));
            throw new BusinessException(MessageConstant.REFRESH_TOKEN_INVALID);
        }

        // 校验令牌类型是否为refresh
        String tokenType = jwtUtil.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            log.error("令牌类型错误，非刷新令牌，token={}", maskToken(refreshToken));
            throw new BusinessException(MessageConstant.REFRESH_TOKEN_INVALID);
        }

        // 校验令牌是否过期
        if (jwtUtil.isExpired(refreshToken)) {
            log.error("刷新令牌已过期，token={}", maskToken(refreshToken));
            throw new BusinessException(MessageConstant.REFRESH_TOKEN_EXPIRED);
        }

        // ========== 3. 提取刷新令牌中的用户信息 ==========
        Long userId = jwtUtil.getUserId(refreshToken);
        String username = jwtUtil.getUsername(refreshToken);
        String jti = jwtUtil.getJti(refreshToken); // 获取令牌唯一标识
        if (userId == null || username == null || jti == null) {
            log.error("刷新令牌缺少核心信息，userId={}, username={}", userId, username);
            throw new BusinessException(MessageConstant.REFRESH_TOKEN_INVALID);
        }

        // ========== 4. 校验设备绑定（可选，增强安全性） ==========
        if (deviceId != null && !deviceId.isBlank()) {
            String tokenDeviceId = refreshTokenClaims.get("deviceId", String.class);
            if (!deviceId.equals(tokenDeviceId)) {
                log.error("刷新令牌设备不匹配，userId={}, reqDevice={}, tokenDevice={}", userId, deviceId, tokenDeviceId);
                throw new BusinessException(MessageConstant.DEVICE_NOT_MATCH);
            }
        }

        // ========== 5. 校验Redis中的刷新令牌（按jti存储，支持精准注销） ==========
        // Redis Key规范：user:token:refresh:{userId}:{jti}
        String refreshTokenKey = RedisConstant.USER_TOKEN_KEY + "refresh:" + userId + ":" + jti;
        String storedRefreshToken = stringRedisTemplate.opsForValue().get(refreshTokenKey);

        // 两种无效场景：1. Redis中无此令牌 2. 令牌内容不匹配
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            log.error("刷新令牌不存在或已失效，userId={}, jti={}", userId, jti);
            throw new BusinessException(MessageConstant.REFRESH_TOKEN_EXPIRED);
        }

        // ========== 6. 校验用户是否存在 ==========
        User user = userMapper.getById(userId);
        if (user == null) {
            log.error("用户不存在，userId={}", userId);
            throw new BusinessException(MessageConstant.USER_NOT_FOUND);
        }

        // ========== 7. 生成新的Access Token和Refresh Token ==========
        // 7.1 构建Access Token的Claims
        Map<String, Object> accessClaims = new HashMap<>();
        accessClaims.put("userId", user.getId());
        accessClaims.put("username", user.getUsername());
        accessClaims.put("role", user.getRole().toString()); // 统一为String，适配JwtUtil

        // 7.2 生成新令牌
        String newAccessToken = jwtUtil.generateAccessToken(accessClaims);
        // 生成新的Refresh Token（携带userId、deviceId）
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getId(), deviceId);
        String newRefreshTokenJti = jwtUtil.getJti(newRefreshToken); // 新令牌的jti

        // ========== 8. 更新Redis中的刷新令牌 ==========
        // 8.1 删除旧的刷新令牌（注销旧令牌）
        stringRedisTemplate.delete(refreshTokenKey);

        // 8.2 存储新的刷新令牌（Key包含新jti，过期时间与令牌一致）
        String newRefreshTokenKey = RedisConstant.USER_TOKEN_KEY + "refresh:" + userId + ":" + newRefreshTokenJti;
        stringRedisTemplate.opsForValue().set(
                newRefreshTokenKey,
                newRefreshToken,
                jwtProperties.getRefreshExpireTime(), // 与令牌过期时间一致
                TimeUnit.SECONDS
        );

        // ========== 9. 构建返回VO ==========
        UserLoginVO vo = new UserLoginVO();
        BeanUtil.copyProperties(user, vo);
        vo.setToken(newAccessToken);
        vo.setRefreshToken(newRefreshToken);

        log.info("用户刷新令牌成功，userId={}, 旧jti={}, 新jti={}", userId, jti, newRefreshTokenJti);
        return vo;
    }

    /**
     * 令牌脱敏（日志中隐藏部分字符，保护安全）
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return token;
        }
        return token.substring(0, 8) + "****" + token.substring(token.length() - 8);
    }

    // ========== 补充必要的常量/异常定义（如果项目中未定义） ==========
    /**
     * 业务异常类（替代通用RuntimeException）
     */
    public static class BusinessException extends RuntimeException {
        private final String code;

        public BusinessException(String message) {
            super(message);
            this.code = "500";
        }

        public BusinessException(String code, String message) {
            super(message);
            this.code = code;
        }

        // Getter
        public String getCode() {
            return code;
        }
    }
}
