package com.cool.server.service;

import com.cool.pojo.dto.CommentCreateDTO;
import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.vo.CommentVO;
import com.cool.pojo.vo.PageVO;

public interface CommentService {
    
    Long createComment(CommentCreateDTO dto);

    PageVO<CommentVO> getCommentList(Long postId, PageQueryDTO dto);

    void deleteComment(Long id);

    void likeComment(Long id);

    void unlikeComment(Long id);
}
