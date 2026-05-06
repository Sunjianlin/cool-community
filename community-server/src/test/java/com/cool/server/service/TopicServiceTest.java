package com.cool.server.service;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.dto.TopicCreateDTO;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.TopicVO;
import com.cool.server.mapper.TopicMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TopicServiceTest {

    @Autowired
    private TopicService topicService;

    @Autowired
    private TopicMapper topicMapper;

    private static Long testTopicId;

    private TopicCreateDTO createTestTopicDTO() {
        TopicCreateDTO dto = new TopicCreateDTO();
        dto.setName("测试话题_" + System.currentTimeMillis());
        dto.setDescription("这是测试话题的描述");
        dto.setIcon("https://example.com/icon.png");
        dto.setCategory("数码");
        return dto;
    }

    @Test
    @Order(1)
    @DisplayName("创建话题 - 正常流程")
    void testCreateTopic_Success() {
        TopicCreateDTO dto = createTestTopicDTO();

        Long topicId = topicService.createTopic(dto);

        assertNotNull(topicId);
        assertTrue(topicId > 0);
        
        testTopicId = topicId;
    }

    @Test
    @Order(2)
    @DisplayName("创建话题 - 名称为空")
    void testCreateTopic_EmptyName() {
        TopicCreateDTO dto = createTestTopicDTO();
        dto.setName("");

        assertThrows(Exception.class, () -> topicService.createTopic(dto));
    }

    @Test
    @Order(3)
    @DisplayName("创建话题 - 名称重复")
    void testCreateTopic_DuplicateName() {
        TopicCreateDTO dto1 = createTestTopicDTO();
        Long topicId = topicService.createTopic(dto1);

        TopicCreateDTO dto2 = createTestTopicDTO();
        dto2.setName(dto1.getName());

        assertThrows(Exception.class, () -> topicService.createTopic(dto2));
    }

    @Test
    @Order(4)
    @DisplayName("更新话题 - 成功")
    void testUpdateTopic_Success() {
        TopicCreateDTO createDTO = createTestTopicDTO();
        Long topicId = topicService.createTopic(createDTO);

        TopicCreateDTO updateDTO = new TopicCreateDTO();
        //updateDTO.setId(topicId);
        updateDTO.setName("更新后的话题名");
        updateDTO.setDescription("更新后的描述");
        updateDTO.setCategory("科技");

        assertDoesNotThrow(() -> topicService.updateTopic(updateDTO));
    }

    @Test
    @Order(5)
    @DisplayName("删除话题 - 成功")
    void testDeleteTopic_Success() {
        TopicCreateDTO dto = createTestTopicDTO();
        Long topicId = topicService.createTopic(dto);

        assertDoesNotThrow(() -> topicService.deleteTopic(topicId));
    }

    @Test
    @Order(6)
    @DisplayName("删除话题 - 话题不存在")
    void testDeleteTopic_NotFound() {
        assertThrows(Exception.class, () -> topicService.deleteTopic(999999L));
    }

    @Test
    @Order(7)
    @DisplayName("获取话题列表 - 分页查询")
    void testGetTopicList_Pagination() {
        for (int i = 0; i < 5; i++) {
            TopicCreateDTO dto = createTestTopicDTO();
            topicService.createTopic(dto);
        }

        PageQueryDTO dto = new PageQueryDTO();
        dto.setPage(1);
        dto.setPageSize(10);

        PageVO<TopicVO> result = topicService.getTopicList(dto);

        assertNotNull(result);
        assertNotNull(result.getRecords());
    }

    @Test
    @Order(8)
    @DisplayName("获取热门话题 - 成功")
    void testGetHotTopics_Success() {
        PageQueryDTO dto = new PageQueryDTO();
        dto.setPage(1);
        dto.setPageSize(10);

        PageVO<TopicVO> result = topicService.getHotTopics(dto);

        assertNotNull(result);
        assertNotNull(result.getRecords());
    }

    @Test
    @Order(9)
    @DisplayName("获取话题详情 - 成功")
    void testGetTopicDetail_Success() {
        TopicCreateDTO dto = createTestTopicDTO();
        Long topicId = topicService.createTopic(dto);

        TopicVO topic = topicService.getTopicDetail(topicId);

        assertNotNull(topic);
        assertEquals(topicId, topic.getId());
        assertEquals(dto.getName(), topic.getName());
    }

    @Test
    @Order(10)
    @DisplayName("获取话题详情 - 话题不存在")
    void testGetTopicDetail_NotFound() {
        assertThrows(Exception.class, () -> topicService.getTopicDetail(999999L));
    }

    @Test
    @Order(11)
    @DisplayName("关注话题 - 成功")
    void testFollowTopic_Success() {
        TopicCreateDTO dto = createTestTopicDTO();
        Long topicId = topicService.createTopic(dto);

        assertDoesNotThrow(() -> topicService.followTopic(topicId));
    }

    @Test
    @Order(12)
    @DisplayName("取消关注话题 - 成功")
    void testUnfollowTopic_Success() {
        TopicCreateDTO dto = createTestTopicDTO();
        Long topicId = topicService.createTopic(dto);
        
        topicService.followTopic(topicId);

        assertDoesNotThrow(() -> topicService.unfollowTopic(topicId));
    }

    @Test
    @Order(13)
    @DisplayName("设置热门话题 - 成功")
    void testSetHot_Success() {
        TopicCreateDTO dto = createTestTopicDTO();
        Long topicId = topicService.createTopic(dto);

        assertDoesNotThrow(() -> topicService.setHot(topicId, true));
    }

    @Test
    @Order(14)
    @DisplayName("取消热门话题 - 成功")
    void testCancelHot_Success() {
        TopicCreateDTO dto = createTestTopicDTO();
        Long topicId = topicService.createTopic(dto);
        
        topicService.setHot(topicId, true);

        assertDoesNotThrow(() -> topicService.setHot(topicId, false));
    }

    @Test
    @Order(15)
    @DisplayName("创建话题 - XSS攻击测试")
    void testCreateTopic_XssAttack() {
        TopicCreateDTO dto = createTestTopicDTO();
        dto.setName("<script>alert('xss')</script>测试话题");
        dto.setDescription("<script>alert('xss')</script>测试描述");

        assertThrows(Exception.class, () -> topicService.createTopic(dto));
    }
}
