package com.cool.server.service.impl;

import com.cool.common.exception.BusinessException;
import com.cool.pojo.document.PostDocument;
import com.cool.pojo.document.TopicDocument;
import com.cool.pojo.document.UserDocument;
import com.cool.pojo.dto.SearchDTO;
import com.cool.pojo.entity.Post;
import com.cool.pojo.entity.Topic;
import com.cool.pojo.entity.User;
import com.cool.pojo.vo.*;
import com.cool.server.mapper.PostMapper;
import com.cool.server.mapper.TopicMapper;
import com.cool.server.mapper.UserMapper;
import com.cool.server.service.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SearchServiceImpl implements SearchService {
    
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    
    @Autowired
    private PostMapper postMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private TopicMapper topicMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final String HOT_KEYWORDS_KEY = "search:hot:keywords";
    
    private static final String POST_INDEX = "post";
    private static final String USER_INDEX = "user";
    private static final String TOPIC_INDEX = "topic";
    
    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_RESULT_WINDOW = 10000;
    private static final int SEARCH_TIMEOUT_SECONDS = 5;
    private static final int SUGGESTION_MIN_LENGTH = 2;
    private static final int SUGGESTION_MAX_SIZE = 10;
    
    private final Executor searchExecutor = Executors.newFixedThreadPool(4);
    
    @Override
    public SearchResultVO searchAll(SearchDTO searchDTO) {
        validateSearchParams(searchDTO);
        
        SearchResultVO result = new SearchResultVO();
        
        CompletableFuture<PageVO<PostVO>> postsFuture = CompletableFuture.supplyAsync(
            () -> searchPosts(searchDTO), searchExecutor
        );
        CompletableFuture<PageVO<UserVO>> usersFuture = CompletableFuture.supplyAsync(
            () -> searchUsers(searchDTO), searchExecutor
        );
        CompletableFuture<PageVO<TopicVO>> topicsFuture = CompletableFuture.supplyAsync(
            () -> searchTopics(searchDTO), searchExecutor
        );
        
        CompletableFuture.allOf(postsFuture, usersFuture, topicsFuture).join();
        
        PageVO<PostVO> posts = postsFuture.join();
        PageVO<UserVO> users = usersFuture.join();
        PageVO<TopicVO> topics = topicsFuture.join();
        
        result.setPosts(posts);
        result.setUsers(users);
        result.setTopics(topics);
        result.setHotKeywords(getHotKeywords());
        
        boolean hasResults = (posts != null && posts.getRecords() != null && !posts.getRecords().isEmpty())
            || (users != null && users.getRecords() != null && !users.getRecords().isEmpty())
            || (topics != null && topics.getRecords() != null && !topics.getRecords().isEmpty());
        
        if (hasResults) {
            incrementKeywordCount(searchDTO.getKeyword());
        }
        
        return result;
    }
    
    private void validateSearchParams(SearchDTO searchDTO) {
        if (searchDTO.getKeyword() == null || searchDTO.getKeyword().trim().isEmpty()) {
            throw new BusinessException(400, "搜索关键词不能为空");
        }
        
        if (searchDTO.getPage() == null || searchDTO.getPage() < 1) {
            searchDTO.setPage(1);
        }
        
        if (searchDTO.getPageSize() == null || searchDTO.getPageSize() < 1) {
            searchDTO.setPageSize(10);
        }
        
        if (searchDTO.getPageSize() > MAX_PAGE_SIZE) {
            searchDTO.setPageSize(MAX_PAGE_SIZE);
        }
        
        int from = (searchDTO.getPage() - 1) * searchDTO.getPageSize();
        if (from >= MAX_RESULT_WINDOW) {
            throw new BusinessException(400, "分页深度超过限制，请使用更精确的搜索条件");
        }
    }
    
    @Override
    public PageVO<PostVO> searchPosts(SearchDTO searchDTO) {
        validateSearchParams(searchDTO);
        
        long startTime = System.currentTimeMillis();
        String keyword = searchDTO.getKeyword();
        
        try {
            int from = (searchDTO.getPage() - 1) * searchDTO.getPageSize();
            
            SearchRequest searchRequest = new SearchRequest(POST_INDEX);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("status", 1))
                .should(QueryBuilders.matchQuery("title", keyword).boost(3.0f))
                .should(QueryBuilders.matchQuery("content", keyword).boost(1.0f))
                .should(QueryBuilders.matchQuery("userNickname", keyword).boost(2.0f))
                .should(QueryBuilders.termQuery("topicName", keyword).boost(2.0f))
                .minimumShouldMatch(1);
            
            sourceBuilder.query(boolQuery);
            sourceBuilder.from(from);
            sourceBuilder.size(searchDTO.getPageSize());
            sourceBuilder.timeout(new TimeValue(SEARCH_TIMEOUT_SECONDS, TimeUnit.SECONDS));
            
            sourceBuilder.sort("_score");
            sourceBuilder.sort("createTime");
            
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("title");
            highlightBuilder.field("content").fragmentSize(200).numOfFragments(3);
            highlightBuilder.preTags("<em class=\"highlight\">");
            highlightBuilder.postTags("</em>");
            sourceBuilder.highlighter(highlightBuilder);
            
            searchRequest.source(sourceBuilder);
            
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            
            List<PostVO> posts = Arrays.stream(response.getHits().getHits())
                .map(this::convertHitToPostVO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            Long total = Math.min(response.getHits().getTotalHits().value, MAX_RESULT_WINDOW);
            
            long costTime = System.currentTimeMillis() - startTime;
            log.info("搜索帖子完成: keyword={}, total={}, cost={}ms", keyword, total, costTime);
            
            return PageVO.of(posts, total, searchDTO.getPage(), searchDTO.getPageSize());
            
        } catch (IOException e) {
            log.error("搜索帖子失败: keyword={}", keyword, e);
            throw new BusinessException(500, "搜索服务暂时不可用，请稍后重试");
        } catch (Exception e) {
            log.error("搜索帖子发生未知异常: keyword={}", keyword, e);
            throw new BusinessException(500, "搜索服务异常，请稍后重试");
        }
    }
    
    private PostVO convertHitToPostVO(org.elasticsearch.search.SearchHit hit) {
        try {
            String sourceAsString = hit.getSourceAsString();
            if (sourceAsString == null || sourceAsString.isEmpty()) {
                log.warn("搜索结果源数据为空");
                return null;
            }
            
            PostDocument doc = objectMapper.readValue(sourceAsString, PostDocument.class);
            
            PostVO vo = new PostVO();
            vo.setId(doc.getId());
            vo.setTitle(doc.getTitle());
            vo.setContent(doc.getContent());
            vo.setUserId(doc.getUserId());
            vo.setUsername(doc.getUsername());
            vo.setUserNickname(doc.getUserNickname());
            vo.setUserAvatar(doc.getUserAvatar());
            vo.setTopicId(doc.getTopicId());
            vo.setTopicName(doc.getTopicName());
            vo.setLikeCount(doc.getLikeCount());
            vo.setCommentCount(doc.getCommentCount());
            vo.setViewCount(doc.getViewCount());
            vo.setIsTop(doc.getIsTop() != null && doc.getIsTop() ? 1 : 0);
            vo.setIsEssence(doc.getIsEssence() != null && doc.getIsEssence() ? 1 : 0);
            vo.setCreateTime(convertToLocalDateTime(doc.getCreateTime()));
            
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (highlightFields != null) {
                if (highlightFields.containsKey("title")) {
                    Text[] fragments = highlightFields.get("title").getFragments();
                    vo.setHighlightTitle(Arrays.stream(fragments).map(Text::string).collect(Collectors.joining()));
                }
                if (highlightFields.containsKey("content")) {
                    Text[] fragments = highlightFields.get("content").getFragments();
                    vo.setHighlightContent(Arrays.stream(fragments).map(Text::string).collect(Collectors.joining("...")));
                }
            }
            
            return vo;
        } catch (Exception e) {
            log.error("转换帖子数据失败", e);
            return null;
        }
    }
    
    @Override
    public PageVO<UserVO> searchUsers(SearchDTO searchDTO) {
        validateSearchParams(searchDTO);
        
        long startTime = System.currentTimeMillis();
        String keyword = searchDTO.getKeyword();
        
        try {
            int from = (searchDTO.getPage() - 1) * searchDTO.getPageSize();
            
            SearchRequest searchRequest = new SearchRequest(USER_INDEX);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("status", 1))
                .should(QueryBuilders.matchQuery("nickname", keyword).boost(3.0f))
                .should(QueryBuilders.matchQuery("bio", keyword).boost(1.0f))
                .minimumShouldMatch(1);
            
            sourceBuilder.query(boolQuery);
            sourceBuilder.from(from);
            sourceBuilder.size(searchDTO.getPageSize());
            sourceBuilder.timeout(new TimeValue(SEARCH_TIMEOUT_SECONDS, TimeUnit.SECONDS));
            
            searchRequest.source(sourceBuilder);
            
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            
            List<UserVO> users = Arrays.stream(response.getHits().getHits())
                .map(hit -> {
                    try {
                        UserDocument doc = objectMapper.readValue(hit.getSourceAsString(), UserDocument.class);
                        return convertToUserVO(doc);
                    } catch (Exception e) {
                        log.error("转换用户数据失败", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            Long total = Math.min(response.getHits().getTotalHits().value, MAX_RESULT_WINDOW);
            
            long costTime = System.currentTimeMillis() - startTime;
            log.info("搜索用户完成: keyword={}, total={}, cost={}ms", keyword, total, costTime);
            
            return PageVO.of(users, total, searchDTO.getPage(), searchDTO.getPageSize());
            
        } catch (IOException e) {
            log.error("搜索用户失败: keyword={}", keyword, e);
            throw new BusinessException(500, "搜索服务暂时不可用，请稍后重试");
        } catch (Exception e) {
            log.error("搜索用户发生未知异常: keyword={}", keyword, e);
            throw new BusinessException(500, "搜索服务异常，请稍后重试");
        }
    }
    
    private UserVO convertToUserVO(UserDocument doc) {
        UserVO vo = new UserVO();
        vo.setId(doc.getId());
        vo.setUsername(doc.getUsername());
        vo.setNickname(doc.getNickname());
        vo.setAvatar(doc.getAvatar());
        vo.setBio(doc.getBio());
        vo.setGender(doc.getGender());
        vo.setStatus(doc.getStatus());
        vo.setFollowerCount(doc.getFollowerCount() != null ? doc.getFollowerCount().longValue() : 0L);
        return vo;
    }
    
    @Override
    public PageVO<TopicVO> searchTopics(SearchDTO searchDTO) {
        validateSearchParams(searchDTO);
        
        long startTime = System.currentTimeMillis();
        String keyword = searchDTO.getKeyword();
        
        try {
            int from = (searchDTO.getPage() - 1) * searchDTO.getPageSize();
            
            SearchRequest searchRequest = new SearchRequest(TOPIC_INDEX);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("status", 1))
                .should(QueryBuilders.matchQuery("name", keyword).boost(3.0f))
                .should(QueryBuilders.matchQuery("description", keyword).boost(1.0f))
                .minimumShouldMatch(1);
            
            sourceBuilder.query(boolQuery);
            sourceBuilder.from(from);
            sourceBuilder.size(searchDTO.getPageSize());
            sourceBuilder.timeout(new TimeValue(SEARCH_TIMEOUT_SECONDS, TimeUnit.SECONDS));
            
            searchRequest.source(sourceBuilder);
            
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            
            List<TopicVO> topics = Arrays.stream(response.getHits().getHits())
                .map(hit -> {
                    try {
                        TopicDocument doc = objectMapper.readValue(hit.getSourceAsString(), TopicDocument.class);
                        return convertToTopicVO(doc);
                    } catch (Exception e) {
                        log.error("转换话题数据失败", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            Long total = Math.min(response.getHits().getTotalHits().value, MAX_RESULT_WINDOW);
            
            long costTime = System.currentTimeMillis() - startTime;
            log.info("搜索话题完成: keyword={}, total={}, cost={}ms", keyword, total, costTime);
            
            return PageVO.of(topics, total, searchDTO.getPage(), searchDTO.getPageSize());
            
        } catch (IOException e) {
            log.error("搜索话题失败: keyword={}", keyword, e);
            throw new BusinessException(500, "搜索服务暂时不可用，请稍后重试");
        } catch (Exception e) {
            log.error("搜索话题发生未知异常: keyword={}", keyword, e);
            throw new BusinessException(500, "搜索服务异常，请稍后重试");
        }
    }
    
    private TopicVO convertToTopicVO(TopicDocument doc) {
        TopicVO vo = new TopicVO();
        vo.setId(doc.getId());
        vo.setName(doc.getName());
        vo.setDescription(doc.getDescription());
        vo.setIcon(doc.getIcon());
        vo.setCategory(doc.getCategory());
        vo.setFollowCount(doc.getFollowCount());
        vo.setPostCount(doc.getPostCount());
        vo.setIsHot(doc.getIsHot() != null && doc.getIsHot() ? 1 : 0);
        return vo;
    }
    
    @Override
    public List<String> getSuggestions(String prefix) {
        if (prefix == null || prefix.trim().isEmpty() || prefix.length() < SUGGESTION_MIN_LENGTH) {
            return Collections.emptyList();
        }
        
        try {
            SearchRequest searchRequest = new SearchRequest(POST_INDEX);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            
            CompletionSuggestionBuilder suggestionBuilder = SuggestBuilders
                .completionSuggestion("title.suggest")
                .prefix(prefix)
                .size(SUGGESTION_MAX_SIZE)
                .skipDuplicates(true);
            
            SuggestBuilder suggestBuilder = new SuggestBuilder();
            suggestBuilder.addSuggestion("title-suggest", suggestionBuilder);
            
            sourceBuilder.suggest(suggestBuilder);
            sourceBuilder.size(0);
            sourceBuilder.timeout(new TimeValue(SEARCH_TIMEOUT_SECONDS, TimeUnit.SECONDS));
            
            searchRequest.source(sourceBuilder);
            
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            
            Suggest suggest = response.getSuggest();
            if (suggest == null) {
                log.debug("搜索建议返回空: prefix={}", prefix);
                return Collections.emptyList();
            }
            
            CompletionSuggestion completionSuggestion = suggest.getSuggestion("title-suggest");
            if (completionSuggestion == null) {
                log.debug("搜索建议无匹配: prefix={}", prefix);
                return Collections.emptyList();
            }
            
            List<String> suggestions = completionSuggestion.getEntries().stream()
                .flatMap(entry -> entry.getOptions().stream())
                .map(option -> {
                    try {
                        Object title = option.getHit().getSourceAsMap().get("title");
                        return title != null ? title.toString() : null;
                    } catch (Exception e) {
                        log.warn("解析搜索建议失败", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .limit(SUGGESTION_MAX_SIZE)
                .collect(Collectors.toList());
            
            log.debug("获取搜索建议成功: prefix={}, count={}", prefix, suggestions.size());
            return suggestions;
                
        } catch (IOException e) {
            log.error("获取搜索建议失败: prefix={}", prefix, e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("获取搜索建议发生未知异常: prefix={}", prefix, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<String> getHotKeywords() {
        Set<Object> keywords = redisTemplate.opsForZSet()
            .reverseRange(HOT_KEYWORDS_KEY, 0, 9);
        
        if (keywords == null || keywords.isEmpty()) {
            return Collections.emptyList();
        }
        
        return keywords.stream()
            .map(Object::toString)
            .collect(Collectors.toList());
    }
    
    private void incrementKeywordCount(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            redisTemplate.opsForZSet().incrementScore(HOT_KEYWORDS_KEY, keyword.trim(), 1);
            redisTemplate.expire(HOT_KEYWORDS_KEY, 7, TimeUnit.DAYS);
        }
    }
    
    @Override
    public void syncPostToEs(Long postId) {
        Post post = postMapper.getById(postId);
        if (post != null) {
            try {
                PostDocument document = convertToPostDocument(post);
                String json = objectMapper.writeValueAsString(document);
                
                IndexRequest request = new IndexRequest(POST_INDEX)
                    .id(String.valueOf(post.getId()))
                    .source(json, XContentType.JSON);
                
                restHighLevelClient.index(request, RequestOptions.DEFAULT);
                log.info("同步帖子到ES成功: postId={}", postId);
            } catch (Exception e) {
                log.error("同步帖子到ES失败: postId={}", postId, e);
            }
        }
    }
    
    @Override
    public void syncUserToEs(Long userId) {
        User user = userMapper.getById(userId);
        if (user != null) {
            try {
                UserDocument document = convertToUserDocument(user);
                String json = objectMapper.writeValueAsString(document);
                
                IndexRequest request = new IndexRequest(USER_INDEX)
                    .id(String.valueOf(user.getId()))
                    .source(json, XContentType.JSON);
                
                restHighLevelClient.index(request, RequestOptions.DEFAULT);
                log.info("同步用户到ES成功: userId={}", userId);
            } catch (Exception e) {
                log.error("同步用户到ES失败: userId={}", userId, e);
            }
        }
    }
    
    @Override
    public void syncTopicToEs(Long topicId) {
        Topic topic = topicMapper.getById(topicId);
        if (topic != null) {
            try {
                TopicDocument document = convertToTopicDocument(topic);
                String json = objectMapper.writeValueAsString(document);
                
                IndexRequest request = new IndexRequest(TOPIC_INDEX)
                    .id(String.valueOf(topic.getId()))
                    .source(json, XContentType.JSON);
                
                restHighLevelClient.index(request, RequestOptions.DEFAULT);
                log.info("同步话题到ES成功: topicId={}", topicId);
            } catch (Exception e) {
                log.error("同步话题到ES失败: topicId={}", topicId, e);
            }
        }
    }
    
    @Override
    public void syncAllData() {
        log.info("开始同步所有数据到ES...");
        
        try {
            Map<Long, User> userMap = new HashMap<>();
            Map<Long, Topic> topicMap = new HashMap<>();
            
            List<User> users = userMapper.selectAll();
            for (User user : users) {
                userMap.put(user.getId(), user);
            }
            
            List<Topic> topics = topicMapper.selectAll();
            for (Topic topic : topics) {
                topicMap.put(topic.getId(), topic);
            }
            
            BulkRequest userBulkRequest = new BulkRequest();
            for (User user : users) {
                UserDocument doc = convertToUserDocument(user);
                String json = objectMapper.writeValueAsString(doc);
                userBulkRequest.add(new IndexRequest(USER_INDEX)
                    .id(String.valueOf(doc.getId()))
                    .source(json, XContentType.JSON));
            }
            BulkResponse userResponse = restHighLevelClient.bulk(userBulkRequest, RequestOptions.DEFAULT);
            log.info("同步用户完成: {} 条, 是否失败: {}", users.size(), userResponse.hasFailures());
            
            BulkRequest topicBulkRequest = new BulkRequest();
            for (Topic topic : topics) {
                TopicDocument doc = convertToTopicDocument(topic);
                String json = objectMapper.writeValueAsString(doc);
                topicBulkRequest.add(new IndexRequest(TOPIC_INDEX)
                    .id(String.valueOf(doc.getId()))
                    .source(json, XContentType.JSON));
            }
            BulkResponse topicResponse = restHighLevelClient.bulk(topicBulkRequest, RequestOptions.DEFAULT);
            log.info("同步话题完成: {} 条, 是否失败: {}", topics.size(), topicResponse.hasFailures());
            
            List<Post> posts = postMapper.selectAll();
            BulkRequest postBulkRequest = new BulkRequest();
            for (Post post : posts) {
                PostDocument doc = convertToPostDocumentOptimized(post, userMap, topicMap);
                String json = objectMapper.writeValueAsString(doc);
                postBulkRequest.add(new IndexRequest(POST_INDEX)
                    .id(String.valueOf(doc.getId()))
                    .source(json, XContentType.JSON));
            }
            BulkResponse postResponse = restHighLevelClient.bulk(postBulkRequest, RequestOptions.DEFAULT);
            log.info("同步帖子完成: {} 条, 是否失败: {}", posts.size(), postResponse.hasFailures());
            
            log.info("数据同步完成");
        } catch (Exception e) {
            log.error("数据同步失败", e);
            throw new BusinessException(500, "数据同步失败: " + e.getMessage());
        }
    }
    
    private PostDocument convertToPostDocument(Post post) {
        PostDocument doc = new PostDocument();
        doc.setId(post.getId());
        doc.setTitle(post.getTitle());
        doc.setTitleSuggest(post.getTitle());
        doc.setContent(post.getContent());
        doc.setUserId(post.getUserId());
        doc.setTopicId(post.getTopicId());
        doc.setLikeCount(post.getLikeCount());
        doc.setCommentCount(post.getCommentCount());
        doc.setViewCount(post.getViewCount());
        doc.setStatus(post.getStatus());
        doc.setIsTop(post.getIsTop() != null && post.getIsTop() == 1);
        doc.setIsEssence(post.getIsEssence() != null && post.getIsEssence() == 1);
        doc.setCreateTime(convertToDate(post.getCreateTime()));
        doc.setUpdateTime(convertToDate(post.getUpdateTime()));
        
        User user = userMapper.getById(post.getUserId());
        if (user != null) {
            doc.setUsername(user.getUsername());
            doc.setUserNickname(user.getNickname());
            doc.setUserAvatar(user.getAvatar());
        }
        
        if (post.getTopicId() != null) {
            Topic topic = topicMapper.getById(post.getTopicId());
            if (topic != null) {
                doc.setTopicName(topic.getName());
            }
        }
        
        return doc;
    }
    
    private PostDocument convertToPostDocumentOptimized(Post post, Map<Long, User> userMap, Map<Long, Topic> topicMap) {
        PostDocument doc = new PostDocument();
        doc.setId(post.getId());
        doc.setTitle(post.getTitle());
        doc.setTitleSuggest(post.getTitle());
        doc.setContent(post.getContent());
        doc.setUserId(post.getUserId());
        doc.setTopicId(post.getTopicId());
        doc.setLikeCount(post.getLikeCount());
        doc.setCommentCount(post.getCommentCount());
        doc.setViewCount(post.getViewCount());
        doc.setStatus(post.getStatus());
        doc.setIsTop(post.getIsTop() != null && post.getIsTop() == 1);
        doc.setIsEssence(post.getIsEssence() != null && post.getIsEssence() == 1);
        doc.setCreateTime(convertToDate(post.getCreateTime()));
        doc.setUpdateTime(convertToDate(post.getUpdateTime()));
        
        User user = userMap.get(post.getUserId());
        if (user != null) {
            doc.setUsername(user.getUsername());
            doc.setUserNickname(user.getNickname());
            doc.setUserAvatar(user.getAvatar());
        }
        
        if (post.getTopicId() != null) {
            Topic topic = topicMap.get(post.getTopicId());
            if (topic != null) {
                doc.setTopicName(topic.getName());
            }
        }
        
        return doc;
    }
    
    private UserDocument convertToUserDocument(User user) {
        UserDocument doc = new UserDocument();
        doc.setId(user.getId());
        doc.setUsername(user.getUsername());
        doc.setNickname(user.getNickname());
        doc.setAvatar(user.getAvatar());
        doc.setBio(user.getBio());
        doc.setGender(user.getGender());
        doc.setStatus(user.getStatus());
        doc.setFollowerCount(user.getFollowerCount() != null ? user.getFollowerCount().intValue() : 0);
        doc.setPostCount(user.getPostCount());
        doc.setCreateTime(convertToDate(user.getCreateTime()));
        return doc;
    }
    
    private TopicDocument convertToTopicDocument(Topic topic) {
        TopicDocument doc = new TopicDocument();
        doc.setId(topic.getId());
        doc.setName(topic.getName());
        doc.setNameSuggest(topic.getName());
        doc.setDescription(topic.getDescription());
        doc.setIcon(topic.getIcon());
        doc.setCategory(topic.getCategory());
        doc.setFollowCount(topic.getFollowCount());
        doc.setPostCount(topic.getPostCount());
        doc.setStatus(topic.getStatus());
        doc.setIsHot(topic.getIsHot() != null && topic.getIsHot() == 1);
        doc.setCreateTime(convertToDate(topic.getCreateTime()));
        return doc;
    }
    
    private LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    
    private Date convertToDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
