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
import com.cool.server.mapper.UserMapper;
import com.cool.server.service.UserService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    private final JwtProperties jwtProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public UserServiceImpl(UserMapper userMapper, JwtProperties jwtProperties, StringRedisTemplate stringRedisTemplate) {
        this.userMapper = userMapper;
        this.jwtProperties = jwtProperties;
        this.stringRedisTemplate = stringRedisTemplate;
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
        stringRedisTemplate.opsForValue().set(tokenKey, token);
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
        return null;
    }

    @Override
    public void followUser(Long id) {
        Long userId = BaseContext.getCurrentId();
        
        if (userId.equals(id)) {
            throw new RuntimeException("不能关注自己");
        }
        
        User targetUser = userMapper.getById(id);
        if (targetUser == null) {
            throw new RuntimeException(MessageConstant.USER_NOT_FOUND);
        }
        
        if (isFollowing(userId, id)) {
            throw new RuntimeException("已经关注了该用户");
        }
        
        userMapper.insertFollow(userId, id, 0);
        userMapper.incrementFollowingCount(userId);
        userMapper.incrementFollowerCount(id);
    }

    @Override
    public void unfollowUser(Long id) {
        Long userId = BaseContext.getCurrentId();
        
        if (!isFollowing(userId, id)) {
            throw new RuntimeException("未关注该用户");
        }
        
        userMapper.deleteFollow(userId, id, 0);
        userMapper.decrementFollowingCount(userId);
        userMapper.decrementFollowerCount(id);
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
        Integer count = userMapper.checkFollow(userId, targetId, 0);
        return count != null && count > 0;
    }

    @Override
    public void kickUser(Long id) {
        stringRedisTemplate.delete(RedisConstant.USER_TOKEN_KEY + id);
        log.info("用户被踢下线: userId={}", id);
    }
}
