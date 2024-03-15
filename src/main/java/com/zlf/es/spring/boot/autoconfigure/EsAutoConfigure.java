package com.zlf.es.spring.boot.autoconfigure;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zlf
 * @description:
 * @time: 2022/06/24
 */
@Configuration
@RefreshScope
public class EsAutoConfigure {

    /**
     * 创建单例模式的RequestOptions，使得所有请求共用
     */
    public static final RequestOptions COMMON_OPTIONS;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        COMMON_OPTIONS = builder.build();
    }

    private final String NPE = "null";

    /**
     * 协议
     */
    @Value("${elasticsearch.schema:http}")
    private String schema;

    /**
     * 集群地址，如果有多个用“,”隔开
     */
    @Value("${elasticsearch.address}")
    private String address;

    /**
     * 账户
     */
    @Value("${elasticsearch.username:null}")
    private String username;

    /**
     * 密码
     */
    @Value("${elasticsearch.password:null}")
    private String password;

    /**
     * 连接超时时间
     */
    @Value("${elasticsearch.connectTimeout:5000}")
    private int connectTimeout;

    /**
     * Socket 连接超时时间
     */
    @Value("${elasticsearch.socketTimeout:30000}")
    private int socketTimeout;

    /**
     * 获取连接的超时时间
     */
    @Value("${elasticsearch.connectionRequestTimeout:5000}")
    private int connectionRequestTimeout;

    /**
     * 最大连接数
     */
    @Value("${elasticsearch.maxConnectNum:100}")
    private int maxConnectNum;

    /**
     * 最大路由连接数
     */
    @Value("${elasticsearch.maxConnectPerRoute:100}")
    private int maxConnectPerRoute;

    @Bean("esClient")
    @ConditionalOnClass(value = {RequestOptions.class, RestHighLevelClient.class})
    public RestHighLevelClient restHighLevelClient() {
        // 拆分地址
        List<HttpHost> hostLists = new ArrayList<>();
        if (address.equals(NPE)) {
            throw new RuntimeException("es的address列表没有配置,请检查配置");
        }
        String[] hostList = address.split(",");
        if (hostList.length == 1) {
            String host = hostList[0].split(":")[0];
            String port = hostList[0].split(":")[1];
            hostLists.add(new HttpHost(host, Integer.parseInt(port), schema));
        } else {
            for (String addr : hostList) {
                String host = addr.split(":")[0];
                String port = addr.split(":")[1];
                hostLists.add(new HttpHost(host, Integer.parseInt(port), schema));
            }
        }
        // 转换成 HttpHost 数组
        HttpHost[] httpHost = hostLists.toArray(new HttpHost[]{});
        // 构建连接对象
        RestClientBuilder builder = RestClient.builder(httpHost);
        if (StringUtils.isNotEmpty(username) && !NPE.equals(username) && StringUtils.isNotEmpty(password) && !NPE.equals(password)) {
            //设置用户名和密码
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.disableAuthCaching();
                return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            });
        }
        // 异步连接延时配置
        builder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(connectTimeout);
            requestConfigBuilder.setSocketTimeout(socketTimeout);
            requestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeout);
            return requestConfigBuilder;
        });
        // 异步连接数配置
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.setMaxConnTotal(maxConnectNum);
            httpClientBuilder.setMaxConnPerRoute(maxConnectPerRoute);
            return httpClientBuilder;
        });
        return new RestHighLevelClient(builder);
    }

}