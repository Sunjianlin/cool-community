package com.cool.server.controller;

import com.cool.pojo.dto.UserLoginDTO;
import com.cool.pojo.dto.UserRegisterDTO;
import com.cool.pojo.vo.UserLoginVO;
import com.cool.server.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    private static String accessToken;
    private static String testUsername;

    @Test
    @Order(1)
    @DisplayName("用户注册接口 - 成功")
    void testRegister_Success() throws Exception {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("testuser_" + System.currentTimeMillis());
        dto.setPassword("Test@123456");
        dto.setNickname("测试用户");
        testUsername = dto.getUsername();

        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(2)
    @DisplayName("用户注册接口 - 参数缺失")
    void testRegister_MissingParams() throws Exception {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("testuser_" + System.currentTimeMillis());

        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    @DisplayName("用户登录接口 - 成功")
    void testLogin_Success() throws Exception {
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setUsername("loginuser_" + System.currentTimeMillis());
        registerDTO.setPassword("Test@123456");
        registerDTO.setNickname("登录测试用户");
        userService.register(registerDTO);

        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername(registerDTO.getUsername());
        loginDTO.setPassword(registerDTO.getPassword());

        MvcResult result = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
    }

    @Test
    @Order(4)
    @DisplayName("用户登录接口 - 密码错误")
    void testLogin_WrongPassword() throws Exception {
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setUsername("wrongpwduser_" + System.currentTimeMillis());
        registerDTO.setPassword("Test@123456");
        registerDTO.setNickname("密码错误测试用户");
        userService.register(registerDTO);

        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername(registerDTO.getUsername());
        loginDTO.setPassword("WrongPassword");

        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    @DisplayName("用户登录接口 - 用户不存在")
    void testLogin_UserNotFound() throws Exception {
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername("nonexistent_user_" + System.currentTimeMillis());
        loginDTO.setPassword("Test@123456");

        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    @DisplayName("获取用户信息接口 - 未授权")
    void testGetUserInfo_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/user/info"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(7)
    @DisplayName("用户登出接口 - 成功")
    void testLogout_Success() throws Exception {
        mockMvc.perform(post("/api/user/logout"))
                .andExpect(status().isOk());
    }
}
