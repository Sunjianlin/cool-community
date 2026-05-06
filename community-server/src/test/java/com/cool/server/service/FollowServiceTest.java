package com.cool.server.service;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.dto.UserRegisterDTO;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.UserVO;
import com.cool.server.mapper.UserMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FollowServiceTest {

    @Autowired
    private FollowService followService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserMapper userMapper;

    private static Long testUserId1;
    private static Long testUserId2;
    private static Long testTopicId = 1L;

    @BeforeEach
    void setUp() {
        if (testUserId1 == null) {
            testUserId1 = createTestUser("follower_");
        }
        if (testUserId2 == null) {
            testUserId2 = createTestUser("following_");
        }
    }

    private Long createTestUser(String prefix) {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername(prefix + System.currentTimeMillis());
        dto.setPassword("Test@123456");
        dto.setNickname(prefix + "用户");
        userService.register(dto);
        return userMapper.getByUsername(dto.getUsername()).getId();
    }

    @Test
    @Order(1)
    @DisplayName("关注用户 - 成功")
    void testFollowUser_Success() {
        assertDoesNotThrow(() -> 
            followService.follow(testUserId2, FollowService.TYPE_USER)
        );
    }

    @Test
    @Order(2)
    @DisplayName("取消关注用户 - 成功")
    void testUnfollowUser_Success() {
        followService.follow(testUserId2, FollowService.TYPE_USER);

        assertDoesNotThrow(() -> 
            followService.unfollow(testUserId2, FollowService.TYPE_USER)
        );
    }

    @Test
    @Order(3)
    @DisplayName("检查是否关注 - 已关注")
    void testIsFollowing_True() {
        followService.follow(testUserId2, FollowService.TYPE_USER);

        boolean isFollowing = followService.isFollowing(testUserId1, testUserId2, FollowService.TYPE_USER);

        assertTrue(isFollowing);
    }

    @Test
    @Order(4)
    @DisplayName("检查是否关注 - 未关注")
    void testIsFollowing_False() {
        boolean isFollowing = followService.isFollowing(testUserId1, 999999L, FollowService.TYPE_USER);

        assertFalse(isFollowing);
    }

    @Test
    @Order(5)
    @DisplayName("获取关注列表 - 成功")
    void testGetFollowingList_Success() {
        followService.follow(testUserId2, FollowService.TYPE_USER);

        PageQueryDTO dto = new PageQueryDTO();
        dto.setPage(1);
        dto.setPageSize(10);

        PageVO<?> result = followService.getFollowingList(testUserId1, FollowService.TYPE_USER, dto);

        assertNotNull(result);
        assertNotNull(result.getRecords());
    }

    @Test
    @Order(6)
    @DisplayName("获取粉丝列表 - 成功")
    void testGetFollowerList_Success() {
        followService.follow(testUserId2, FollowService.TYPE_USER);

        PageQueryDTO dto = new PageQueryDTO();
        dto.setPage(1);
        dto.setPageSize(10);

        PageVO<?> result = followService.getFollowerList(testUserId2, FollowService.TYPE_USER, dto);

        assertNotNull(result);
        assertNotNull(result.getRecords());
    }

    @Test
    @Order(7)
    @DisplayName("获取关注数量 - 成功")
    void testGetFollowCount_Success() {
        followService.follow(testUserId2, FollowService.TYPE_USER);

        Long count = followService.getFollowCount(testUserId1, FollowService.TYPE_USER);

        assertNotNull(count);
        assertTrue(count >= 0);
    }

    @Test
    @Order(8)
    @DisplayName("获取粉丝数量 - 成功")
    void testGetFollowerCount_Success() {
        followService.follow(testUserId2, FollowService.TYPE_USER);

        Long count = followService.getFollowerCount(testUserId2, FollowService.TYPE_USER);

        assertNotNull(count);
        assertTrue(count >= 0);
    }

    @Test
    @Order(9)
    @DisplayName("关注话题 - 成功")
    void testFollowTopic_Success() {
        assertDoesNotThrow(() -> 
            followService.follow(testTopicId, FollowService.TYPE_TOPIC)
        );
    }

    @Test
    @Order(10)
    @DisplayName("取消关注话题 - 成功")
    void testUnfollowTopic_Success() {
        followService.follow(testTopicId, FollowService.TYPE_TOPIC);

        assertDoesNotThrow(() -> 
            followService.unfollow(testTopicId, FollowService.TYPE_TOPIC)
        );
    }

    @Test
    @Order(11)
    @DisplayName("重复关注用户 - 幂等性测试")
    void testFollowUser_Idempotent() {
        followService.follow(testUserId2, FollowService.TYPE_USER);
        
        assertDoesNotThrow(() -> 
            followService.follow(testUserId2, FollowService.TYPE_USER)
        );
    }

    @Test
    @Order(12)
    @DisplayName("关注不存在的用户 - 异常")
    void testFollowNonExistentUser() {
        assertThrows(Exception.class, () -> 
            followService.follow(999999L, FollowService.TYPE_USER)
        );
    }

    @Test
    @Order(13)
    @DisplayName("获取关注列表 - 空列表")
    void testGetFollowingList_Empty() {
        PageQueryDTO dto = new PageQueryDTO();
        dto.setPage(1);
        dto.setPageSize(10);

        PageVO<?> result = followService.getFollowingList(999999L, FollowService.TYPE_USER, dto);

        assertNotNull(result);
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    @Order(14)
    @DisplayName("获取粉丝列表 - 空列表")
    void testGetFollowerList_Empty() {
        PageQueryDTO dto = new PageQueryDTO();
        dto.setPage(1);
        dto.setPageSize(10);

        PageVO<?> result = followService.getFollowerList(999999L, FollowService.TYPE_USER, dto);

        assertNotNull(result);
        assertTrue(result.getRecords().isEmpty());
    }
}
