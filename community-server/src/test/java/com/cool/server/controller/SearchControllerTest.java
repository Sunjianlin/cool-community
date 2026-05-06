package com.cool.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    @DisplayName("综合搜索接口 - 成功")
    void testSearchAll_Success() throws Exception {
        mockMvc.perform(get("/api/search/all")
                .param("keyword", "测试")
                .param("page", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(2)
    @DisplayName("综合搜索接口 - 空关键词")
    void testSearchAll_EmptyKeyword() throws Exception {
        mockMvc.perform(get("/api/search/all")
                .param("keyword", "")
                .param("page", "1")
                .param("pageSize", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    @DisplayName("综合搜索接口 - 关键词过长")
    void testSearchAll_KeywordTooLong() throws Exception {
        StringBuilder longKeyword = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            longKeyword.append("测");
        }

        mockMvc.perform(get("/api/search/all")
                .param("keyword", longKeyword.toString())
                .param("page", "1")
                .param("pageSize", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("帖子搜索接口 - 成功")
    void testSearchPosts_Success() throws Exception {
        mockMvc.perform(get("/api/search/posts")
                .param("keyword", "测试")
                .param("page", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(5)
    @DisplayName("用户搜索接口 - 成功")
    void testSearchUsers_Success() throws Exception {
        mockMvc.perform(get("/api/search/users")
                .param("keyword", "用户")
                .param("page", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(6)
    @DisplayName("话题搜索接口 - 成功")
    void testSearchTopics_Success() throws Exception {
        mockMvc.perform(get("/api/search/topics")
                .param("keyword", "话题")
                .param("page", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(7)
    @DisplayName("搜索建议接口 - 成功")
    void testGetSuggestions_Success() throws Exception {
        mockMvc.perform(get("/api/search/suggest")
                .param("prefix", "测试"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(8)
    @DisplayName("搜索建议接口 - 空前缀")
    void testGetSuggestions_EmptyPrefix() throws Exception {
        mockMvc.perform(get("/api/search/suggest")
                .param("prefix", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(9)
    @DisplayName("热门关键词接口 - 成功")
    void testGetHotKeywords_Success() throws Exception {
        mockMvc.perform(get("/api/search/hot-keywords"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(10)
    @DisplayName("搜索接口 - 特殊字符")
    void testSearch_SpecialCharacters() throws Exception {
        mockMvc.perform(get("/api/search/all")
                .param("keyword", "<script>alert('xss')</script>")
                .param("page", "1")
                .param("pageSize", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(11)
    @DisplayName("搜索接口 - 无效分页参数")
    void testSearch_InvalidPagination() throws Exception {
        mockMvc.perform(get("/api/search/posts")
                .param("keyword", "测试")
                .param("page", "-1")
                .param("pageSize", "100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(12)
    @DisplayName("搜索接口 - 分页大小超限")
    void testSearch_PageSizeExceeded() throws Exception {
        mockMvc.perform(get("/api/search/posts")
                .param("keyword", "测试")
                .param("page", "1")
                .param("pageSize", "100"))
                .andExpect(status().isBadRequest());
    }
}
