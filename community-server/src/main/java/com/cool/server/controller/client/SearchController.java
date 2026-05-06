package com.cool.server.controller.client;

import com.cool.pojo.Result;
import com.cool.pojo.dto.SearchDTO;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.PostVO;
import com.cool.pojo.vo.SearchResultVO;
import com.cool.pojo.vo.TopicVO;
import com.cool.pojo.vo.UserVO;
import com.cool.server.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.regex.Pattern;

@Tag(name = "搜索接口", description = "全局搜索功能")
@RestController
@RequestMapping("/search")
public class SearchController {
    
    @Autowired
    private SearchService searchService;
    
    private static final int MAX_KEYWORD_LENGTH = 100;
    private static final Pattern DANGEROUS_PATTERN = Pattern.compile("[<>\"'\\\\]");
    
    @GetMapping("/all")
    @Operation(summary = "综合搜索", description = "搜索帖子、用户、话题")
    public Result<SearchResultVO> searchAll(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        validateKeyword(keyword);
        validatePagination(page, pageSize);
        
        SearchDTO searchDTO = new SearchDTO();
        searchDTO.setKeyword(sanitizeKeyword(keyword));
        searchDTO.setPage(page);
        searchDTO.setPageSize(pageSize);
        return Result.success(searchService.searchAll(searchDTO));
    }
    
    @GetMapping("/posts")
    @Operation(summary = "帖子搜索")
    public Result<PageVO<PostVO>> searchPosts(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        validateKeyword(keyword);
        validatePagination(page, pageSize);
        
        SearchDTO searchDTO = new SearchDTO();
        searchDTO.setKeyword(sanitizeKeyword(keyword));
        searchDTO.setPage(page);
        searchDTO.setPageSize(pageSize);
        return Result.success(searchService.searchPosts(searchDTO));
    }
    
    @GetMapping("/users")
    @Operation(summary = "用户搜索")
    public Result<PageVO<UserVO>> searchUsers(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        validateKeyword(keyword);
        validatePagination(page, pageSize);
        
        SearchDTO searchDTO = new SearchDTO();
        searchDTO.setKeyword(sanitizeKeyword(keyword));
        searchDTO.setPage(page);
        searchDTO.setPageSize(pageSize);
        return Result.success(searchService.searchUsers(searchDTO));
    }
    
    @GetMapping("/topics")
    @Operation(summary = "话题搜索")
    public Result<PageVO<TopicVO>> searchTopics(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        validateKeyword(keyword);
        validatePagination(page, pageSize);
        
        SearchDTO searchDTO = new SearchDTO();
        searchDTO.setKeyword(sanitizeKeyword(keyword));
        searchDTO.setPage(page);
        searchDTO.setPageSize(pageSize);
        return Result.success(searchService.searchTopics(searchDTO));
    }
    
    @GetMapping("/suggest")
    @Operation(summary = "搜索建议")
    public Result<List<String>> getSuggestions(
            @Parameter(description = "前缀") @RequestParam String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return Result.success(List.of());
        }
        return Result.success(searchService.getSuggestions(sanitizeKeyword(prefix)));
    }
    
    @GetMapping("/hot-keywords")
    @Operation(summary = "热门搜索词")
    public Result<List<String>> getHotKeywords() {
        return Result.success(searchService.getHotKeywords());
    }
    
    @PostMapping("/sync")
    @Operation(summary = "同步数据到ES", description = "管理员接口")
    public Result<String> syncData() {
        searchService.syncAllData();
        return Result.success("数据同步完成");
    }
    
    private void validateKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }
        if (keyword.length() > MAX_KEYWORD_LENGTH) {
            throw new IllegalArgumentException("搜索关键词长度不能超过" + MAX_KEYWORD_LENGTH + "个字符");
        }
        if (DANGEROUS_PATTERN.matcher(keyword).find()) {
            throw new IllegalArgumentException("搜索关键词包含非法字符");
        }
    }
    
    private void validatePagination(Integer page, Integer pageSize) {
        if (page != null && page < 1) {
            throw new IllegalArgumentException("页码必须大于0");
        }
        if (pageSize != null && (pageSize < 1 || pageSize > 50)) {
            throw new IllegalArgumentException("每页数量必须在1-50之间");
        }
    }
    
    private String sanitizeKeyword(String keyword) {
        return keyword.trim().replaceAll("\\s+", " ");
    }
}
