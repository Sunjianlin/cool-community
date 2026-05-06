package com.cool.server.controller;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.dto.PostCreateDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long TEST_TOPIC_ID = 1L;

    private PostCreateDTO createTestPostDTO() {
        PostCreateDTO dto = new PostCreateDTO();
        dto.setTitle("测试帖子标题_" + System.currentTimeMillis());
        dto.setContent("这是测试帖子的内容，用于接口测试。");
        dto.setTopicId(TEST_TOPIC_ID);
        return dto;
    }

    @Test
    @Order(1)
    @DisplayName("获取帖子列表接口 - 成功")
    void testGetPostList_Success() throws Exception {
        mockMvc.perform(get("/api/post/list")
                .param("page", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(2)
    @DisplayName("获取帖子列表接口 - 默认分页")
    void testGetPostList_DefaultPagination() throws Exception {
        mockMvc.perform(get("/api/post/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(3)
    @DisplayName("获取帖子详情接口 - 成功")
    void testGetPostDetail_Success() throws Exception {
        mockMvc.perform(get("/api/post/1"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    @DisplayName("获取帖子详情接口 - 帖子不存在")
    void testGetPostDetail_NotFound() throws Exception {
        mockMvc.perform(get("/api/post/999999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    @DisplayName("创建帖子接口 - 未授权")
    void testCreatePost_Unauthorized() throws Exception {
        PostCreateDTO dto = createTestPostDTO();

        mockMvc.perform(post("/api/post")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(6)
    @DisplayName("点赞帖子接口 - 未授权")
    void testLikePost_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/post/1/like"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(7)
    @DisplayName("收藏帖子接口 - 未授权")
    void testCollectPost_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/post/1/collect"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(8)
    @DisplayName("删除帖子接口 - 未授权")
    void testDeletePost_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/post/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(9)
    @DisplayName("获取帖子列表接口 - 无效分页参数")
    void testGetPostList_InvalidPagination() throws Exception {
        mockMvc.perform(get("/api/post/list")
                .param("page", "-1")
                .param("pageSize", "0"))
                .andExpect(status().isBadRequest());
    }
}
