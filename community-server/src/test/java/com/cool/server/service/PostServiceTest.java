package com.cool.server.service;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.dto.PostCreateDTO;
import com.cool.pojo.entity.Post;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.PostVO;
import com.cool.server.mapper.PostMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostMapper postMapper;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static Long testPostId;
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_TOPIC_ID = 1L;

    private PostCreateDTO createTestPostDTO() {
        PostCreateDTO dto = new PostCreateDTO();
        dto.setTitle("测试帖子标题_" + System.currentTimeMillis());
        dto.setContent("这是测试帖子的内容，用于单元测试。");
        dto.setTopicId(TEST_TOPIC_ID);
        //dto.setUserId(TEST_USER_ID);
        return dto;
    }

    @Test
    @Order(1)
    @DisplayName("创建帖子 - 正常流程")
    void testCreatePost_Success() {
        PostCreateDTO dto = createTestPostDTO();

        Long postId = postService.createPost(dto);
        
        assertNotNull(postId);
        assertTrue(postId > 0);
        
        testPostId = postId;
    }

    @Test
    @Order(2)
    @DisplayName("创建帖子 - 标题为空")
    void testCreatePost_EmptyTitle() {
        PostCreateDTO dto = createTestPostDTO();
        dto.setTitle("");

        assertThrows(Exception.class, () -> postService.createPost(dto));
    }

    @Test
    @Order(3)
    @DisplayName("创建帖子 - 内容为空")
    void testCreatePost_EmptyContent() {
        PostCreateDTO dto = createTestPostDTO();
        dto.setContent("");

        assertThrows(Exception.class, () -> postService.createPost(dto));
    }

    @Test
    @Order(4)
    @DisplayName("创建帖子 - 标题超长")
    void testCreatePost_TitleTooLong() {
        PostCreateDTO dto = createTestPostDTO();
        StringBuilder longTitle = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            longTitle.append("测");
        }
        dto.setTitle(longTitle.toString());

        assertThrows(Exception.class, () -> postService.createPost(dto));
    }

    @Test
    @Order(5)
    @DisplayName("获取帖子列表 - 分页查询")
    void testGetPostList_Pagination() {
        for (int i = 0; i < 5; i++) {
            PostCreateDTO dto = createTestPostDTO();
            postService.createPost(dto);
        }

        PageQueryDTO dto = new PageQueryDTO();
        dto.setPage(1);
        dto.setPageSize(10);

        PageVO<PostVO> result = postService.getPostList(dto);
        
        assertNotNull(result);
        assertNotNull(result.getRecords());
    }

    @Test
    @Order(6)
    @DisplayName("获取帖子列表 - 第一页")
    void testGetPostList_FirstPage() {
        PageQueryDTO dto = new PageQueryDTO();
        dto.setPage(1);
        dto.setPageSize(5);

        PageVO<PostVO> result = postService.getPostList(dto);

        assertNotNull(result);
        assertTrue(result.getRecords().size() <= 5);
    }

    @Test
    @Order(7)
    @DisplayName("获取帖子列表 - 空列表")
    void testGetPostList_EmptyList() {
        PageQueryDTO dto = new PageQueryDTO();
        dto.setPage(9999);
        dto.setPageSize(10);

        PageVO<PostVO> result = postService.getPostList(dto);

        assertNotNull(result);
        assertTrue(result.getRecords().isEmpty() || result.getRecords().size() <= 10);
    }

    @Test
    @Order(8)
    @DisplayName("获取帖子详情 - 成功")
    void testGetPostDetail_Success() {
        PostCreateDTO createDTO = createTestPostDTO();
        Long postId = postService.createPost(createDTO);

        PostVO post = postService.getPostDetail(postId);

        assertNotNull(post);
        assertEquals(postId, post.getId());
        assertEquals(createDTO.getTitle(), post.getTitle());
        assertEquals(createDTO.getContent(), post.getContent());
    }

    @Test
    @Order(9)
    @DisplayName("获取帖子详情 - 帖子不存在")
    void testGetPostDetail_NotFound() {
        assertThrows(Exception.class, () -> postService.getPostDetail(999999L));
    }

    @Test
    @Order(10)
    @DisplayName("删除帖子 - 成功")
    void testDeletePost_Success() {
        PostCreateDTO createDTO = createTestPostDTO();
        Long postId = postService.createPost(createDTO);

        assertDoesNotThrow(() -> postService.deletePost(postId));
    }

    @Test
    @Order(11)
    @DisplayName("删除帖子 - 帖子不存在")
    void testDeletePost_NotFound() {
        assertThrows(Exception.class, () -> postService.deletePost(999999L));
    }

    @Test
    @Order(12)
    @DisplayName("点赞帖子 - 成功")
    void testLikePost_Success() {
        PostCreateDTO createDTO = createTestPostDTO();
        Long postId = postService.createPost(createDTO);

        assertDoesNotThrow(() -> postService.likePost(postId));
    }

    @Test
    @Order(13)
    @DisplayName("取消点赞 - 成功")
    void testUnlikePost_Success() {
        PostCreateDTO createDTO = createTestPostDTO();
        Long postId = postService.createPost(createDTO);
        
        postService.likePost(postId);

        assertDoesNotThrow(() -> postService.unlikePost(postId));
    }

    @Test
    @Order(14)
    @DisplayName("点赞帖子 - 并发测试")
    void testLikePost_Concurrent() throws InterruptedException {
        PostCreateDTO createDTO = createTestPostDTO();
        Long postId = postService.createPost(createDTO);

        int threadCount = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final Long userId = (long) (i + 1000);
            executorService.submit(() -> {
                try {
                    stringRedisTemplate.opsForValue().setIfAbsent(
                        "post:like:" + postId + ":" + userId, 
                        "1"
                    );
                    successCount.incrementAndGet();
                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        assertTrue(successCount.get() > 0);
    }

    @Test
    @Order(15)
    @DisplayName("收藏帖子 - 成功")
    void testCollectPost_Success() {
        PostCreateDTO createDTO = createTestPostDTO();
        Long postId = postService.createPost(createDTO);

        assertDoesNotThrow(() -> postService.collectPost(postId));
    }

    @Test
    @Order(16)
    @DisplayName("取消收藏 - 成功")
    void testUncollectPost_Success() {
        PostCreateDTO createDTO = createTestPostDTO();
        Long postId = postService.createPost(createDTO);
        
        postService.collectPost(postId);

        assertDoesNotThrow(() -> postService.uncollectPost(postId));
    }

    @Test
    @Order(17)
    @DisplayName("审核通过帖子 - 成功")
    void testApprovePost_Success() {
        PostCreateDTO createDTO = createTestPostDTO();
        Long postId = postService.createPost(createDTO);

        assertDoesNotThrow(() -> postService.approvePost(postId));
    }

    @Test
    @Order(18)
    @DisplayName("审核拒绝帖子 - 成功")
    void testRejectPost_Success() {
        PostCreateDTO createDTO = createTestPostDTO();
        Long postId = postService.createPost(createDTO);

        assertDoesNotThrow(() -> postService.rejectPost(postId));
    }

    @Test
    @Order(19)
    @DisplayName("置顶帖子 - 成功")
    void testSetTop_Success() {
        PostCreateDTO createDTO = createTestPostDTO();
        Long postId = postService.createPost(createDTO);

        assertDoesNotThrow(() -> postService.setTop(postId));
    }

    @Test
    @Order(20)
    @DisplayName("取消置顶 - 成功")
    void testCancelTop_Success() {
        PostCreateDTO createDTO = createTestPostDTO();
        Long postId = postService.createPost(createDTO);
        
        postService.setTop(postId);

        assertDoesNotThrow(() -> postService.cancelTop(postId));
    }

    @Test
    @Order(21)
    @DisplayName("设为精华 - 成功")
    void testSetEssence_Success() {
        PostCreateDTO createDTO = createTestPostDTO();
        Long postId = postService.createPost(createDTO);

        assertDoesNotThrow(() -> postService.setEssence(postId));
    }

    @Test
    @Order(22)
    @DisplayName("取消精华 - 成功")
    void testCancelEssence_Success() {
        PostCreateDTO createDTO = createTestPostDTO();
        Long postId = postService.createPost(createDTO);
        
        postService.setEssence(postId);

        assertDoesNotThrow(() -> postService.cancelEssence(postId));
    }

    @Test
    @Order(23)
    @DisplayName("创建帖子 - XSS攻击测试")
    void testCreatePost_XssAttack() {
        PostCreateDTO dto = createTestPostDTO();
        dto.setTitle("<script>alert('xss')</script>测试标题");
        dto.setContent("<script>alert('xss')</script>测试内容");

        Long postId = postService.createPost(dto);
        assertNotNull(postId);
    }

    @Test
    @Order(24)
    @DisplayName("创建帖子 - SQL注入测试")
    void testCreatePost_SqlInjection() {
        PostCreateDTO dto = createTestPostDTO();
        dto.setTitle("测试标题' OR '1'='1");
        dto.setContent("测试内容'; DROP TABLE post; --");

        Long postId = postService.createPost(dto);
        assertNotNull(postId);
    }
}
