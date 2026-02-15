package com.cool.server.service.impl;

import com.cool.common.constant.MessageConstant;
import com.cool.common.constant.RedisConstant;
import com.cool.common.constant.RoleConstant;
import com.cool.common.properties.AliOssProperties;
import com.cool.common.properties.JwtProperties;
import com.cool.common.utils.AliOssUtil;
import com.cool.common.utils.JwtUtil;
import com.cool.pojo.dto.UserLoginDTO;
import com.cool.pojo.dto.UserRegisterDTO;
import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.entity.User;
import com.cool.pojo.vo.UserLoginVO;
import com.cool.pojo.vo.UserVO;
import com.cool.pojo.vo.PageVO;
import com.cool.server.context.BaseContext;
import com.cool.server.mapper.UserMapper;
import com.cool.server.service.UserService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.DigestUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    private final JwtProperties jwtProperties;
    private final AliOssProperties aliOssProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public UserServiceImpl(UserMapper userMapper, JwtProperties jwtProperties,
                          AliOssProperties aliOssProperties, StringRedisTemplate stringRedisTemplate) {
        this.userMapper = userMapper;
        this.jwtProperties = jwtProperties;
        this.aliOssProperties = aliOssProperties;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void register(UserRegisterDTO dto) {
        User existUser = userMapper.getByUsername(dto.getUsername());
        if (existUser != null) {
            throw new RuntimeException(MessageConstant.USERNAME_EXISTS);
        }
        
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(DigestUtil.md5Hex(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(1);
        user.setRole(RoleConstant.USER);
        
        userMapper.insert(user);
    }

    @Override
    public UserLoginVO login(UserLoginDTO dto) {
        User user = userMapper.getByUsername(dto.getUsername());
        if (user == null) {
            throw new RuntimeException(MessageConstant.USERNAME_OR_PASSWORD_ERROR);
        }
        
        if (!DigestUtil.md5Hex(dto.getPassword()).equals(user.getPassword())) {
            throw new RuntimeException(MessageConstant.USERNAME_OR_PASSWORD_ERROR);
        }
        
        if (user.getStatus() == 0) {
            throw new RuntimeException(MessageConstant.ACCOUNT_DISABLED);
        }
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());
        String token = JwtUtil.createToken(jwtProperties.getSecretKey(), jwtProperties.getExpireTime(), claims);
        
        stringRedisTemplate.opsForValue().set(
                RedisConstant.USER_TOKEN_KEY + user.getId(),
                token,
                RedisConstant.TOKEN_EXPIRE_TIME,
                TimeUnit.SECONDS
        );
        
        return new UserLoginVO(
                user.getId(), 
                user.getUsername(), 
                user.getNickname(), 
                user.getAvatar(),
                user.getRole(),
                token
        );
    }

    @Override
    public UserVO getUserInfo() {
        Long userId = BaseContext.getCurrentId();
        return getUserInfoById(userId);
    }

    @Override
    public UserVO getUserInfoById(Long id) {
        User user = userMapper.getById(id);
        if (user == null) {
            throw new RuntimeException(MessageConstant.DATA_NOT_FOUND);
        }
        
        UserVO vo = new UserVO();
        BeanUtil.copyProperties(user, vo);
        vo.setRoleName(RoleConstant.getRoleName(user.getRole()));
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
        try {
            Long userId = BaseContext.getCurrentId();
            String url = AliOssUtil.uploadAvatar(
                    aliOssProperties.getEndpoint(),
                    aliOssProperties.getAccessKeyId(),
                    aliOssProperties.getAccessKeySecret(),
                    aliOssProperties.getBucketName(),
                    file.getOriginalFilename(),
                    file.getBytes()
            );
            
            User user = new User();
            user.setId(userId);
            user.setAvatar(url);
            userMapper.update(user);
            
            return url;
        } catch (IOException e) {
            throw new RuntimeException(MessageConstant.UPLOAD_FAILED);
        }
    }

    @Override
    public void followUser(Long id) {
        Long userId = BaseContext.getCurrentId();
        if (userId.equals(id)) {
            throw new RuntimeException("不能关注自己");
        }
        userMapper.insertFollow(userId, id, 1);
    }

    @Override
    public void unfollowUser(Long id) {
        Long userId = BaseContext.getCurrentId();
        userMapper.deleteFollow(userId, id, 1);
    }

    @Override
    public PageVO<UserVO> getUserList(PageQueryDTO dto) {
        Long total = userMapper.count(dto);
        List<UserVO> users = userMapper.list(dto);
        users.forEach(u -> u.setRoleName(RoleConstant.getRoleName(u.getRole())));
        return PageVO.of(users, total, dto.getPage(), dto.getPageSize());
    }

    @Override
    public void banUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setStatus(0);
        userMapper.update(user);
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
    }
}
