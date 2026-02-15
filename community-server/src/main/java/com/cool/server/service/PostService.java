package com.cool.server.service;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.dto.PostCreateDTO;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.PostVO;

public interface PostService {
    
    Long createPost(PostCreateDTO dto);

    PageVO<PostVO> getPostList(PageQueryDTO dto);

    PostVO getPostDetail(Long id);

    void deletePost(Long id);

    void likePost(Long id);

    void unlikePost(Long id);

    void collectPost(Long id);

    void uncollectPost(Long id);

    void approvePost(Long id);

    void rejectPost(Long id);

    void setTop(Long id);

    void cancelTop(Long id);

    void setEssence(Long id);

    void cancelEssence(Long id);
}
