package com.cool.server.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration(exclude = {ElasticsearchRestClientAutoConfiguration.class})
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris:localhost:9200}")
    private String elasticsearchUris;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        String[] hosts = elasticsearchUris.split(",");
        HttpHost[] httpHosts = new HttpHost[hosts.length];
        
        for (int i = 0; i < hosts.length; i++) {
            String host = hosts[i].trim();
            String protocol = "http";
            String hostname = host;
            int port = 9200;
            
            if (host.startsWith("https://")) {
                protocol = "https";
                hostname = host.substring(8);
            } else if (host.startsWith("http://")) {
                hostname = host.substring(7);
            }
            
            if (hostname.contains(":")) {
                String[] parts = hostname.split(":");
                hostname = parts[0];
                port = Integer.parseInt(parts[1]);
            }
            
            httpHosts[i] = new HttpHost(hostname, port, protocol);
        }
        
        RestClientBuilder builder = RestClient.builder(httpHosts);
        
        return new RestHighLevelClient(builder);
    }
}
