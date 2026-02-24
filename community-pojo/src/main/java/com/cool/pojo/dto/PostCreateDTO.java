package com.cool.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PostCreateDTO {
    
    @NotNull(message = "话题ID不能为空")
    private Long topicId;

    private Long productId;

    @NotNull(message = "帖子类型不能为空")
    private Integer type;

    private Integer rating;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String images;
}
