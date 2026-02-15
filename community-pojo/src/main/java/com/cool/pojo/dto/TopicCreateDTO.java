package com.cool.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TopicCreateDTO {
    
    @NotBlank(message = "话题名称不能为空")
    private String name;

    private String description;
    private String icon;
    private String cover;
    private String category;
}
