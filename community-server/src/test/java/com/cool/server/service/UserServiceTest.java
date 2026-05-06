package com.cool.server.service;

import com.cool.common.exception.BusinessException;
import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.dto.UserLoginDTO;
import com.cool.pojo.dto.UserRegisterDTO;
import com.cool.pojo.entity.User;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.UserLoginVO;
import com.cool.pojo.vo.UserVO;
import com.cool.server.context.BaseContext;
import com.cool.server.mapper.UserMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static Long testUserId;
    private static String testUsername;

    private UserRegisterDTO createTestUserDTO() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("testuser_" + System.currentTimeMillis());
        dto.setPassword("Test@123456");
        dto.setNickname("测试用户");
        return dto;
    }

    @Test
    @Order(1)
    @DisplayName("用户注册 - 正常流程")
    void testRegister_Success() {
        UserRegisterDTO dto = createTestUserDTO();
        
        userService.register(dto);
        
        User savedUser = userMapper.getByUsername(dto.getUsername());
        assertNotNull(savedUser);
        assertEquals(dto.getUsername(), savedUser.getUsername());
        assertEquals(dto.getNickname(), savedUser.getNickname());
        assertEquals(1, savedUser.getStatus());
        
        testUserId = savedUser.getId();
        testUsername = dto.getUsername();
    }

    @Test
    @Order(2)
    @DisplayName("用户注册 - 用户名已存在")
    void testRegister_DuplicateUsername() {
        UserRegisterDTO dto1 = createTestUserDTO();
        userService.register(dto1);

        UserRegisterDTO dto2 = new UserRegisterDTO();
        dto2.setUsername(dto1.getUsername());
        dto2.setPassword("Test@654321");
        dto2.setNickname("另一个用户");

        assertThrows(Exception.class, () -> userService.register(dto2));
    }

    @Test
    @Order(3)
    @DisplayName("用户注册 - 参数校验：用户名为空")
    void testRegister_EmptyUsername() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("");
        dto.setPassword("Test@123456");
        dto.setNickname("测试用户");

        assertThrows(Exception.class, () -> userService.register(dto));
    }

    @Test
    @Order(4)
    @DisplayName("用户注册 - 参数校验：密码为空")
    void testRegister_EmptyPassword() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("testuser_" + System.currentTimeMillis());
        dto.setPassword("");
        dto.setNickname("测试用户");

        assertThrows(Exception.class, () -> userService.register(dto));
    }

    @Test
    @Order(5)
    @DisplayName("用户登录 - 成功")
    void testLogin_Success() {
        UserRegisterDTO registerDTO = createTestUserDTO();
        userService.register(registerDTO);

        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername(registerDTO.getUsername());
        loginDTO.setPassword(registerDTO.getPassword());

        UserLoginVO loginVO = userService.login(loginDTO);

        assertNotNull(loginVO);
        //assertNotNull(loginVO.getAccessToken());
        assertNotNull(loginVO.getRefreshToken());
        assertEquals(registerDTO.getUsername(), loginVO.getUsername());
    }

    @Test
    @Order(6)
    @DisplayName("用户登录 - 密码错误")
    void testLogin_WrongPassword() {
        UserRegisterDTO registerDTO = createTestUserDTO();
        userService.register(registerDTO);

        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername(registerDTO.getUsername());
        loginDTO.setPassword("WrongPassword123");

        assertThrows(Exception.class, () -> userService.login(loginDTO));
    }

    @Test
    @Order(7)
    @DisplayName("用户登录 - 用户不存在")
    void testLogin_UserNotFound() {
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername("nonexistent_user_" + System.currentTimeMillis());
        loginDTO.setPassword("Test@123456");

        assertThrows(Exception.class, () -> userService.login(loginDTO));
    }

    @Test
    @Order(8)
    @DisplayName("用户登录 - 并发登录测试")
    void testLogin_Concurrent() throws InterruptedException {
        UserRegisterDTO registerDTO = createTestUserDTO();
        userService.register(registerDTO);

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        CountDownLatch successLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    UserLoginDTO loginDTO = new UserLoginDTO();
                    loginDTO.setUsername(registerDTO.getUsername());
                    loginDTO.setPassword(registerDTO.getPassword());
                    
                    UserLoginVO result = userService.login(loginDTO);
                    if (result != null && result.getAccessToken() != null) {
                        successLatch.countDown();
                    }
                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        assertEquals(0, successLatch.getCount());
    }

    @Test
    @Order(9)
    @DisplayName("获取用户信息 - 成功")
    void testGetUserInfo_Success() {
        UserRegisterDTO registerDTO = createTestUserDTO();
        userService.register(registerDTO);
        
        User user = userMapper.getByUsername(registerDTO.getUsername());
        assertNotNull(user);

        UserVO userVO = userService.getUserInfoById(user.getId());

        assertNotNull(userVO);
        assertEquals(user.getId(), userVO.getId());
        assertEquals(registerDTO.getUsername(), userVO.getUsername());
    }

    @Test
    @Order(10)
    @DisplayName("获取用户信息 - 用户不存在")
    void testGetUserInfo_NotFound() {
        assertThrows(Exception.class, () -> userService.getUserInfoById(999999L));
    }

    @Test
    @Order(11)
    @DisplayName("更新用户信息 - 成功")
    void testUpdateUserInfo_Success() {
        UserRegisterDTO registerDTO = createTestUserDTO();
        userService.register(registerDTO);
        
        User user = userMapper.getByUsername(registerDTO.getUsername());
        assertNotNull(user);

        UserVO updateVO = new UserVO();
        updateVO.setId(user.getId());
        updateVO.setNickname("新昵称");
        updateVO.setBio("这是新的个人简介");

        userService.updateUserInfo(updateVO);

        User updatedUser = userMapper.getById(user.getId());
        assertEquals("新昵称", updatedUser.getNickname());
        assertEquals("这是新的个人简介", updatedUser.getBio());
    }

    @Test
    @Order(12)
    @DisplayName("获取用户列表 - 分页")
    void testGetUserList_Pagination() {
        for (int i = 0; i < 5; i++) {
            UserRegisterDTO dto = createTestUserDTO();
            userService.register(dto);
        }

        PageQueryDTO dto = new PageQueryDTO();
        dto.setPage(1);
        dto.setPageSize(10);

        PageVO<UserVO> result = userService.getUserList(dto);

        assertNotNull(result);
        assertNotNull(result.getRecords());
        assertTrue(result.getRecords().size() <= 10);
    }

    @Test
    @Order(13)
    @DisplayName("获取用户列表 - 边界值测试")
    void testGetUserList_Boundary() {
        PageQueryDTO dto = new PageQueryDTO();
        dto.setPage(1);
        dto.setPageSize(1);

        PageVO<UserVO> result = userService.getUserList(dto);

        assertNotNull(result);
        assertTrue(result.getRecords().size() <= 1);
    }

    @Test
    @Order(14)
    @DisplayName("封禁用户 - 成功")
    void testBanUser_Success() {
        UserRegisterDTO registerDTO = createTestUserDTO();
        userService.register(registerDTO);
        
        User user = userMapper.getByUsername(registerDTO.getUsername());
        assertNotNull(user);

        userService.banUser(user.getId());

        User bannedUser = userMapper.getById(user.getId());
        assertEquals(0, bannedUser.getStatus());
    }

    @Test
    @Order(15)
    @DisplayName("解封用户 - 成功")
    void testUnbanUser_Success() {
        UserRegisterDTO registerDTO = createTestUserDTO();
        userService.register(registerDTO);
        
        User user = userMapper.getByUsername(registerDTO.getUsername());
        userService.banUser(user.getId());
        
        userService.unbanUser(user.getId());

        User unbannedUser = userMapper.getById(user.getId());
        assertEquals(1, unbannedUser.getStatus());
    }

    @Test
    @Order(16)
    @DisplayName("删除用户 - 成功")
    void testDeleteUser_Success() {
        UserRegisterDTO registerDTO = createTestUserDTO();
        userService.register(registerDTO);
        
        User user = userMapper.getByUsername(registerDTO.getUsername());
        assertNotNull(user);

        userService.deleteUser(user.getId());

        User deletedUser = userMapper.getById(user.getId());
        assertNull(deletedUser);
    }

    @Test
    @Order(17)
    @DisplayName("Token刷新 - 成功")
    void testRefreshToken_Success() {
        UserRegisterDTO registerDTO = createTestUserDTO();
        userService.register(registerDTO);

        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername(registerDTO.getUsername());
        loginDTO.setPassword(registerDTO.getPassword());

        UserLoginVO loginVO = userService.login(loginDTO);
        assertNotNull(loginVO.getRefreshToken());

        UserLoginVO refreshVO = userService.refreshToken(loginVO.getRefreshToken(), "test-device");
        
        assertNotNull(refreshVO);
        assertNotNull(refreshVO.getAccessToken());
        assertNotEquals(loginVO.getAccessToken(), refreshVO.getAccessToken());
    }

    @Test
    @Order(18)
    @DisplayName("Token刷新 - 无效Token")
    void testRefreshToken_InvalidToken() {
        assertThrows(Exception.class, () -> 
            userService.refreshToken("invalid_refresh_token", "test-device")
        );
    }

    @Test
    @Order(19)
    @DisplayName("获取在线用户统计")
    void testGetOnlineUserStats() {
        var stats = userService.getOnlineUserStats();
        
        assertNotNull(stats);
        assertTrue(stats.containsKey("onlineCount") || stats.containsKey("total"));
    }

    @Test
    @Order(20)
    @DisplayName("用户登出 - 成功")
    void testLogout_Success() {
        UserRegisterDTO registerDTO = createTestUserDTO();
        userService.register(registerDTO);

        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername(registerDTO.getUsername());
        loginDTO.setPassword(registerDTO.getPassword());

        UserLoginVO loginVO = userService.login(loginDTO);
        assertNotNull(loginVO);

        assertDoesNotThrow(() -> userService.logout());
    }
}
