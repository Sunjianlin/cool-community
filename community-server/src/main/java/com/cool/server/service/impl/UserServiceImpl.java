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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    
    private final UserMapper userMapper;
    private final JwtProperties jwtProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final OnlineStatusService onlineStatusService;
    private final FollowService followService;
    private final String aliyunOssEndpoint;
    private final String aliyunOssAccessKeyId;
    private final String aliyunOssAccessKeySecret;
    private final String aliyunOssBucketName;

    public UserServiceImpl(UserMapper userMapper, JwtProperties jwtProperties, StringRedisTemplate stringRedisTemplate, OnlineStatusService onlineStatusService, FollowService followService,
                          @Value("${aliyun.oss.endpoint}") String aliyunOssEndpoint,
                          @Value("${aliyun.oss.access-key-id}") String aliyunOssAccessKeyId,
                          @Value("${aliyun.oss.access-key-secret}") String aliyunOssAccessKeySecret,
                          @Value("${aliyun.oss.bucket-name}") String aliyunOssBucketName) {
        this.userMapper = userMapper;
        this.jwtProperties = jwtProperties;
        this.stringRedisTemplate = stringRedisTemplate;
        this.onlineStatusService = onlineStatusService;
        this.followService = followService;
        this.aliyunOssEndpoint = aliyunOssEndpoint;
        this.aliyunOssAccessKeyId = aliyunOssAccessKeyId;
        this.aliyunOssAccessKeySecret = aliyunOssAccessKeySecret;
        this.aliyunOssBucketName = aliyunOssBucketName;
    }

    @Override
    public UserLoginVO login(UserLoginDTO dto) {
        User user = userMapper.getByUsername(dto.getUsername());
        if (user == null) {
            throw new RuntimeException(MessageConstant.USER_NOT_FOUND);
        }
        
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        
        String encryptedPassword = DigestUtil.md5Hex(dto.getPassword());
        if (!user.getPassword().equals(encryptedPassword)) {
            throw new RuntimeException(MessageConstant.PASSWORD_ERROR);
        }
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());
        String token = JwtUtil.createToken(jwtProperties.getSecretKey(), jwtProperties.getExpireTime(), claims);
        
        String tokenKey = RedisConstant.USER_TOKEN_KEY + user.getId();
        stringRedisTemplate.opsForValue().set(tokenKey, token,TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);
        log.info("用户登录成功，生成新token并覆盖旧token: userId={}", user.getId());
        
        UserLoginVO vo = new UserLoginVO();
        BeanUtil.copyProperties(user, vo);
        vo.setToken(token);
        
        return vo;
    }

    @Override
    public void register(UserRegisterDTO dto) {
        User existingUser = userMapper.getByUsername(dto.getUsername());
        if (existingUser != null) {
            throw new RuntimeException(MessageConstant.USERNAME_EXISTS);
        }
        
        User user = new User();
        BeanUtil.copyProperties(dto, user);
        user.setPassword(DigestUtil.md5Hex(dto.getPassword()));
        user.setStatus(1);
        user.setRole(0);
        
        userMapper.insert(user);
    }

    @Override
    public void logout() {
        Long userId = BaseContext.getCurrentId();
        if (userId != null) {
            String tokenKey = RedisConstant.USER_TOKEN_KEY + userId;
            stringRedisTemplate.delete(tokenKey);
            log.info("用户登出，删除token: userId={}", userId);
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
        
        stringRedisTemplate.delete(RedisConstant.USER_TOKEN_KEY + id);
        log.info("用户被禁用，删除token: userId={}", id);
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
        stringRedisTemplate.delete(RedisConstant.USER_TOKEN_KEY + id);
        log.info("用户被删除，删除token: userId={}", id);
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
        stringRedisTemplate.delete(RedisConstant.USER_TOKEN_KEY + id);
        log.info("用户被踢下线: userId={}", id);
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
}
