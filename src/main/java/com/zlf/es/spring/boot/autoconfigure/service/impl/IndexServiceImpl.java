package com.zlf.es.spring.boot.autoconfigure.service.impl;

import com.alibaba.fastjson.JSON;
import com.zlf.es.spring.boot.autoconfigure.service.IndexService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.ReindexRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author zlf
 * @description:
 * @time: 2022/06/24
 */
@Service
@Slf4j
public class IndexServiceImpl implements IndexService {


    @Qualifier("esClient")
    @Autowired
    private RestHighLevelClient esClient;

    private final RequestOptions options = RequestOptions.DEFAULT;


    @Override
    public Boolean createIndex(String indexName, String settings, String mappings) throws Exception {
        try {
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
            createIndexRequest.settings(settings, XContentType.JSON);
            createIndexRequest.mapping("_doc", mappings, XContentType.JSON);
            CreateIndexResponse createIndexResponse = esClient.indices().create(createIndexRequest, options);
            log.info("============createIndex().createIndexResponse:{}=============", JSON.toJSONString(createIndexResponse));
            return createIndexResponse.isAcknowledged();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("创建索引失败！");
            return Boolean.FALSE;
        }
    }

    @Override
    public Boolean deleteIndex(String indexName) throws Exception {
        try {
            DeleteIndexRequest request = new DeleteIndexRequest(indexName);
            AcknowledgedResponse acknowledgedResponse = esClient.indices().delete(request, options);
            log.info("=========deleteIndex().acknowledgedResponse=======:{}", JSON.toJSONString(acknowledgedResponse));
            return acknowledgedResponse.isAcknowledged();
        } catch (ElasticsearchException exception) {
            if (exception.status() == RestStatus.NOT_FOUND) {
                log.error("删除索引未找到！");
                return Boolean.FALSE;
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean existsIndex(String indexName) throws Exception {
        GetIndexRequest request = new GetIndexRequest(indexName);
        request.humanReadable(true);
        return esClient.indices().exists(request, options);
    }

    @Override
    public Boolean reindex(String sourceIndexName, String targetIndexName, String sourceSettings, String sourceMappings, SearchSourceBuilder searchSourceBuilder) throws Exception {
        try {
            // 如果目标索引不存在，创建新的索引
            if (!existsIndex(targetIndexName)) {
                this.createIndex(targetIndexName, sourceSettings, sourceMappings);
            }
            // 把已有的数据复制到新的索引
            // 更新新的索引的数据
            ReindexRequest request = new ReindexRequest();
            request.setSourceIndices(sourceIndexName);
            request.setDestIndex(sourceIndexName);
            // 设置版本冲突时继续
            request.setConflicts("proceed");
            // 调用reindex后刷新索引
            request.setRefresh(true);
            //异步调用监听
            ActionListener<BulkByScrollResponse> listener = new ActionListener<BulkByScrollResponse>() {
                @Override
                public void onResponse(BulkByScrollResponse bulkResponse) {
                    log.info("reindex success. {}", bulkResponse.getTotal());
                }

                @Override
                public void onFailure(Exception e) {
                    log.error("reindex failed. ", e);
                }
            };
            SearchRequest searchRequest = request.getSearchRequest();
            if (Objects.nonNull(searchSourceBuilder)) {
                searchRequest.source(searchSourceBuilder);
            }
            // 异步reindex
            esClient.reindexAsync(request, options, listener);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("===============reindex异常！=================");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

}