package com.cool.server.service;

import com.cool.pojo.dto.SearchDTO;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.PostVO;
import com.cool.pojo.vo.SearchResultVO;
import com.cool.pojo.vo.TopicVO;
import com.cool.pojo.vo.UserVO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SearchServiceTest {

    @Autowired
    private SearchService searchService;

    private SearchDTO createSearchDTO(String keyword) {
        SearchDTO dto = new SearchDTO();
        dto.setKeyword(keyword);
        dto.setPage(1);
        dto.setPageSize(10);
        return dto;
    }

    @Test
    @Order(1)
    @DisplayName("综合搜索 - 正常流程")
    void testSearchAll_Success() {
        SearchDTO dto = createSearchDTO("测试");

        SearchResultVO result = searchService.searchAll(dto);

        assertNotNull(result);
    }

    @Test
    @Order(2)
    @DisplayName("综合搜索 - 空关键词")
    void testSearchAll_EmptyKeyword() {
        SearchDTO dto = createSearchDTO("");

        assertThrows(Exception.class, () -> searchService.searchAll(dto));
    }

    @Test
    @Order(3)
    @DisplayName("综合搜索 - 关键词过长")
    void testSearchAll_KeywordTooLong() {
        StringBuilder longKeyword = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            longKeyword.append("测");
        }
        SearchDTO dto = createSearchDTO(longKeyword.toString());

        assertThrows(Exception.class, () -> searchService.searchAll(dto));
    }

    @Test
    @Order(4)
    @DisplayName("帖子搜索 - 正常流程")
    void testSearchPosts_Success() {
        SearchDTO dto = createSearchDTO("测试");

        PageVO<PostVO> result = searchService.searchPosts(dto);

        assertNotNull(result);
        assertNotNull(result.getRecords());
    }

    @Test
    @Order(5)
    @DisplayName("帖子搜索 - 分页测试")
    void testSearchPosts_Pagination() {
        SearchDTO dto = createSearchDTO("测试");
        dto.setPage(1);
        dto.setPageSize(5);

        PageVO<PostVO> result = searchService.searchPosts(dto);

        assertNotNull(result);
        assertTrue(result.getRecords().size() <= 5);
    }

    @Test
    @Order(6)
    @DisplayName("帖子搜索 - 无结果")
    void testSearchPosts_NoResult() {
        SearchDTO dto = createSearchDTO("这是一个非常特殊的关键词_不存在_" + System.currentTimeMillis());

        PageVO<PostVO> result = searchService.searchPosts(dto);

        assertNotNull(result);
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    @Order(7)
    @DisplayName("用户搜索 - 正常流程")
    void testSearchUsers_Success() {
        SearchDTO dto = createSearchDTO("用户");

        PageVO<UserVO> result = searchService.searchUsers(dto);

        assertNotNull(result);
        assertNotNull(result.getRecords());
    }

    @Test
    @Order(8)
    @DisplayName("用户搜索 - 无结果")
    void testSearchUsers_NoResult() {
        SearchDTO dto = createSearchDTO("不存在的用户名_" + System.currentTimeMillis());

        PageVO<UserVO> result = searchService.searchUsers(dto);

        assertNotNull(result);
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    @Order(9)
    @DisplayName("话题搜索 - 正常流程")
    void testSearchTopics_Success() {
        SearchDTO dto = createSearchDTO("话题");

        PageVO<TopicVO> result = searchService.searchTopics(dto);

        assertNotNull(result);
        assertNotNull(result.getRecords());
    }

    @Test
    @Order(10)
    @DisplayName("话题搜索 - 无结果")
    void testSearchTopics_NoResult() {
        SearchDTO dto = createSearchDTO("不存在的话题_" + System.currentTimeMillis());

        PageVO<TopicVO> result = searchService.searchTopics(dto);

        assertNotNull(result);
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    @Order(11)
    @DisplayName("搜索建议 - 正常流程")
    void testGetSuggestions_Success() {
        String prefix = "测";

        List<String> suggestions = searchService.getSuggestions(prefix);

        assertNotNull(suggestions);
    }

    @Test
    @Order(12)
    @DisplayName("搜索建议 - 空前缀")
    void testGetSuggestions_EmptyPrefix() {
        String prefix = "";

        List<String> suggestions = searchService.getSuggestions(prefix);

        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty());
    }

    @Test
    @Order(13)
    @DisplayName("搜索建议 - 单字符")
    void testGetSuggestions_SingleChar() {
        String prefix = "a";

        List<String> suggestions = searchService.getSuggestions(prefix);

        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty());
    }

    @Test
    @Order(14)
    @DisplayName("热门关键词 - 获取")
    void testGetHotKeywords() {
        List<String> keywords = searchService.getHotKeywords();

        assertNotNull(keywords);
    }

    @Test
    @Order(15)
    @DisplayName("同步帖子到ES - 成功")
    void testSyncPostToEs_Success() {
        assertDoesNotThrow(() -> searchService.syncPostToEs(1L));
    }

    @Test
    @Order(16)
    @DisplayName("同步用户到ES - 成功")
    void testSyncUserToEs_Success() {
        assertDoesNotThrow(() -> searchService.syncUserToEs(1L));
    }

    @Test
    @Order(17)
    @DisplayName("同步话题到ES - 成功")
    void testSyncTopicToEs_Success() {
        assertDoesNotThrow(() -> searchService.syncTopicToEs(1L));
    }

    @Test
    @Order(18)
    @DisplayName("搜索 - 特殊字符")
    void testSearch_SpecialCharacters() {
        SearchDTO dto = createSearchDTO("测试<script>");

        assertThrows(Exception.class, () -> searchService.searchAll(dto));
    }

    @Test
    @Order(19)
    @DisplayName("搜索 - SQL注入测试")
    void testSearch_SqlInjection() {
        SearchDTO dto = createSearchDTO("测试' OR '1'='1");

        assertThrows(Exception.class, () -> searchService.searchAll(dto));
    }

    @Test
    @Order(20)
    @DisplayName("分页深度限制测试")
    void testSearch_DeepPagination() {
        SearchDTO dto = createSearchDTO("测试");
        dto.setPage(10000);
        dto.setPageSize(10);

        assertThrows(Exception.class, () -> searchService.searchPosts(dto));
    }
}
