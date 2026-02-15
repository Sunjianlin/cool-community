package com.cool.pojo.vo;

import lombok.Data;
import java.util.List;

@Data
public class PageVO<T> {
    
    private List<T> records;
    private Long total;
    private Integer page;
    private Integer pageSize;
    private Integer totalPages;

    public static <T> PageVO<T> of(List<T> records, Long total, Integer page, Integer pageSize) {
        PageVO<T> pageVO = new PageVO<>();
        pageVO.records = records;
        pageVO.total = total;
        pageVO.page = page;
        pageVO.pageSize = pageSize;
        pageVO.totalPages = pageSize > 0 ? (int) Math.ceil((double) total / pageSize) : 0;
        return pageVO;
    }
}
