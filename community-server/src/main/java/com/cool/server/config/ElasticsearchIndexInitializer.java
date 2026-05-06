package com.cool.server.config;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ElasticsearchIndexInitializer implements ApplicationRunner {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    private static final String POST_INDEX = "post";
    private static final String USER_INDEX = "user";
    private static final String TOPIC_INDEX = "topic";

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始初始化Elasticsearch索引...");
        
        createPostIndexIfNeeded();
        createUserIndexIfNeeded();
        createTopicIndexIfNeeded();
        
        log.info("Elasticsearch索引初始化完成");
    }

    private void createPostIndexIfNeeded() {
        try {
            GetIndexRequest getRequest = new GetIndexRequest(POST_INDEX);
            boolean exists = restHighLevelClient.indices().exists(getRequest, RequestOptions.DEFAULT);
            
            if (!exists) {
                CreateIndexRequest createRequest = new CreateIndexRequest(POST_INDEX);
                createRequest.settings("""
                    {
                        "number_of_shards": 1,
                        "number_of_replicas": 0,
                        "analysis": {
                            "analyzer": {
                                "ik_smart_analyzer": {
                                    "type": "custom",
                                    "tokenizer": "ik_smart"
                                },
                                "ik_max_word_analyzer": {
                                    "type": "custom",
                                    "tokenizer": "ik_max_word"
                                }
                            }
                        }
                    }
                    """, XContentType.JSON);
                
                createRequest.mapping("""
                    {
                        "properties": {
                            "id": { "type": "long" },
                            "title": { 
                                "type": "text", 
                                "analyzer": "ik_max_word", 
                                "search_analyzer": "ik_smart",
                                "fields": {
                                    "keyword": { "type": "keyword" },
                                    "suggest": { "type": "completion", "analyzer": "ik_max_word" }
                                }
                            },
                            "content": { 
                                "type": "text", 
                                "analyzer": "ik_max_word", 
                                "search_analyzer": "ik_smart" 
                            },
                            "userId": { "type": "long" },
                            "username": { "type": "keyword" },
                            "userNickname": { 
                                "type": "text", 
                                "analyzer": "ik_max_word" 
                            },
                            "userAvatar": { "type": "keyword" },
                            "topicId": { "type": "long" },
                            "topicName": { "type": "keyword" },
                            "likeCount": { "type": "integer" },
                            "commentCount": { "type": "integer" },
                            "viewCount": { "type": "integer" },
                            "status": { "type": "integer" },
                            "isTop": { "type": "boolean" },
                            "isEssence": { "type": "boolean" },
                            "createTime": { "type": "date" },
                            "updateTime": { "type": "date" }
                        }
                    }
                    """, XContentType.JSON);
                
                CreateIndexResponse response = restHighLevelClient.indices().create(createRequest, RequestOptions.DEFAULT);
                log.info("创建帖子索引成功: {}", response.isAcknowledged());
            } else {
                log.info("帖子索引已存在，跳过创建");
            }
        } catch (Exception e) {
            log.error("创建帖子索引失败", e);
        }
    }

    private void createUserIndexIfNeeded() {
        try {
            GetIndexRequest getRequest = new GetIndexRequest(USER_INDEX);
            boolean exists = restHighLevelClient.indices().exists(getRequest, RequestOptions.DEFAULT);
            
            if (!exists) {
                CreateIndexRequest createRequest = new CreateIndexRequest(USER_INDEX);
                createRequest.settings("""
                    {
                        "number_of_shards": 1,
                        "number_of_replicas": 0,
                        "analysis": {
                            "analyzer": {
                                "ik_smart_analyzer": {
                                    "type": "custom",
                                    "tokenizer": "ik_smart"
                                },
                                "ik_max_word_analyzer": {
                                    "type": "custom",
                                    "tokenizer": "ik_max_word"
                                }
                            }
                        }
                    }
                    """, XContentType.JSON);
                
                createRequest.mapping("""
                    {
                        "properties": {
                            "id": { "type": "long" },
                            "username": { "type": "keyword" },
                            "nickname": { 
                                "type": "text", 
                                "analyzer": "ik_max_word", 
                                "search_analyzer": "ik_smart",
                                "fields": {
                                    "keyword": { "type": "keyword" }
                                }
                            },
                            "avatar": { "type": "keyword" },
                            "bio": { 
                                "type": "text", 
                                "analyzer": "ik_max_word" 
                            },
                            "gender": { "type": "integer" },
                            "status": { "type": "integer" },
                            "followerCount": { "type": "integer" },
                            "postCount": { "type": "integer" },
                            "createTime": { "type": "date" }
                        }
                    }
                    """, XContentType.JSON);
                
                CreateIndexResponse response = restHighLevelClient.indices().create(createRequest, RequestOptions.DEFAULT);
                log.info("创建用户索引成功: {}", response.isAcknowledged());
            } else {
                log.info("用户索引已存在，跳过创建");
            }
        } catch (Exception e) {
            log.error("创建用户索引失败", e);
        }
    }

    private void createTopicIndexIfNeeded() {
        try {
            GetIndexRequest getRequest = new GetIndexRequest(TOPIC_INDEX);
            boolean exists = restHighLevelClient.indices().exists(getRequest, RequestOptions.DEFAULT);
            
            if (!exists) {
                CreateIndexRequest createRequest = new CreateIndexRequest(TOPIC_INDEX);
                createRequest.settings("""
                    {
                        "number_of_shards": 1,
                        "number_of_replicas": 0,
                        "analysis": {
                            "analyzer": {
                                "ik_smart_analyzer": {
                                    "type": "custom",
                                    "tokenizer": "ik_smart"
                                },
                                "ik_max_word_analyzer": {
                                    "type": "custom",
                                    "tokenizer": "ik_max_word"
                                }
                            }
                        }
                    }
                    """, XContentType.JSON);
                
                createRequest.mapping("""
                    {
                        "properties": {
                            "id": { "type": "long" },
                            "name": { 
                                "type": "text", 
                                "analyzer": "ik_max_word", 
                                "search_analyzer": "ik_smart",
                                "fields": {
                                    "keyword": { "type": "keyword" },
                                    "suggest": { "type": "completion", "analyzer": "ik_max_word" }
                                }
                            },
                            "description": { 
                                "type": "text", 
                                "analyzer": "ik_max_word" 
                            },
                            "icon": { "type": "keyword" },
                            "category": { "type": "keyword" },
                            "followCount": { "type": "integer" },
                            "postCount": { "type": "integer" },
                            "status": { "type": "integer" },
                            "isHot": { "type": "boolean" },
                            "createTime": { "type": "date" }
                        }
                    }
                    """, XContentType.JSON);
                
                CreateIndexResponse response = restHighLevelClient.indices().create(createRequest, RequestOptions.DEFAULT);
                log.info("创建话题索引成功: {}", response.isAcknowledged());
            } else {
                log.info("话题索引已存在，跳过创建");
            }
        } catch (Exception e) {
            log.error("创建话题索引失败", e);
        }
    }
}
