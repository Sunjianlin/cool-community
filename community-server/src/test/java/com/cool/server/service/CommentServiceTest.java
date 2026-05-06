package com.cool.server.service;

import com.cool.pojo.dto.CommentCreateDTO;
import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.vo.CommentVO;
import com.cool.pojo.vo.PageVO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommentServiceTest {

    @Autowired
    private CommentService commentService;
    
    @Autowired
    private PostService postService;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_POST_ID = 1L;

    private CommentCreateDTO createTestCommentDTO(Long postId, Long parentId) {
        CommentCreateDTO dto = new CommentCreateDTO();
        dto.setPostId(postId != null ? postId : TEST_POST_ID);
        dto.setContent("这是测试评论内容_" + System.currentTimeMillis());
        dto.setParentId(parentId);
        return dto;
    }

    @Test
    @Order(1)
    @DisplayName("创建评论 - 正常流程")
    void testCreateComment_Success() {
        CommentCreateDTO dto = createTestCommentDTO(TEST_POST_ID, null);

        Long commentId = commentService.createComment(dto);

        assertNotNull(commentId);
        assertTrue(commentId > 0);
    }

    @Test
    @Order(2)
    @DisplayName("创建评论 - 内容为空")
    void testCreateComment_EmptyContent() {
        CommentCreateDTO dto = createTestCommentDTO(TEST_POST_ID, null);
        dto.setContent("");

        assertThrows(Exception.class, () -> commentService.createComment(dto));
    }

    @Test
    @Order(3)
    @DisplayName("创建评论 - 内容超长")
    void testCreateComment_ContentTooLong() {
        CommentCreateDTO dto = createTestCommentDTO(TEST_POST_ID, null);
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longContent.append("测");
        }
        dto.setContent(longContent.toString());

        assertThrows(Exception.class, () -> commentService.createComment(dto));
    }

    @Test
    @Order(4)
    @DisplayName("创建回复评论 - 成功")
    void testCreateReply_Success() {
        CommentCreateDTO parentDto = createTestCommentDTO(TEST_POST_ID, null);
        Long parentId = commentService.createComment(parentDto);

        CommentCreateDTO replyDto = createTestCommentDTO(TEST_POST_ID, parentId);
        Long replyId = commentService.createComment(replyDto);

        assertNotNull(replyId);
    }

    @Test
    @Order(5)
    @DisplayName("获取评论列表 - 分页查询")
    void testGetCommentList_Pagination() {
        for (int i = 0; i < 5; i++) {
            CommentCreateDTO dto = createTestCommentDTO(TEST_POST_ID, null);
            commentService.createComment(dto);
        }

        PageQueryDTO dto = new PageQueryDTO();
        dto.setPage(1);
        dto.setPageSize(10);

        PageVO<CommentVO> result = commentService.getCommentList(TEST_POST_ID, dto);

        assertNotNull(result);
        assertNotNull(result.getRecords());
    }

    @Test
    @Order(6)
    @DisplayName("获取评论列表 - 空列表")
    void testGetCommentList_EmptyList() {
        PageQueryDTO dto = new PageQueryDTO();
        dto.setPage(1);
        dto.setPageSize(10);

        PageVO<CommentVO> result = commentService.getCommentList(999999L, dto);

        assertNotNull(result);
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    @Order(7)
    @DisplayName("删除评论 - 成功")
    void testDeleteComment_Success() {
        CommentCreateDTO dto = createTestCommentDTO(TEST_POST_ID, null);
        Long commentId = commentService.createComment(dto);

        assertDoesNotThrow(() -> commentService.deleteComment(commentId));
    }

    @Test
    @Order(8)
    @DisplayName("删除评论 - 评论不存在")
    void testDeleteComment_NotFound() {
        assertThrows(Exception.class, () -> commentService.deleteComment(999999L));
    }

    @Test
    @Order(9)
    @DisplayName("点赞评论 - 成功")
    void testLikeComment_Success() {
        CommentCreateDTO dto = createTestCommentDTO(TEST_POST_ID, null);
        Long commentId = commentService.createComment(dto);

        assertDoesNotThrow(() -> commentService.likeComment(commentId));
    }

    @Test
    @Order(10)
    @DisplayName("取消点赞评论 - 成功")
    void testUnlikeComment_Success() {
        CommentCreateDTO dto = createTestCommentDTO(TEST_POST_ID, null);
        Long commentId = commentService.createComment(dto);
        
        commentService.likeComment(commentId);

        assertDoesNotThrow(() -> commentService.unlikeComment(commentId));
    }

    @Test
    @Order(11)
    @DisplayName("创建评论 - XSS攻击测试")
    void testCreateComment_XssAttack() {
        CommentCreateDTO dto = createTestCommentDTO(TEST_POST_ID, null);
        dto.setContent("<script>alert('xss')</script>测试评论");

        Long commentId = commentService.createComment(dto);
        assertNotNull(commentId);
    }

    @Test
    @Order(12)
    @DisplayName("创建评论 - SQL注入测试")
    void testCreateComment_SqlInjection() {
        CommentCreateDTO dto = createTestCommentDTO(TEST_POST_ID, null);
        dto.setContent("测试评论'; DROP TABLE comment; --");

        Long commentId = commentService.createComment(dto);
        assertNotNull(commentId);
    }
}
