package com.zlf.es.spring.boot.autoconfigure.service.impl;

import com.alibaba.fastjson.JSON;
import com.zlf.es.spring.boot.autoconfigure.EsAutoConfigure;
import com.zlf.es.spring.boot.autoconfigure.service.DocService;
import com.zlf.es.spring.boot.autoconfigure.service.vo.EsPageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.replication.ReplicatedWriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author zlf
 * @description:
 * @time: 2022/06/24
 */
@Service
@Slf4j
public class DocServiceImpl<T> implements DocService {


    @Qualifier("esClient")
    @Autowired
    private RestHighLevelClient esClient;

    @Override
    public Boolean existsDoc(String indexName, String id) {
        GetRequest getRequest = new GetRequest(indexName, id);
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields("_none_");
        try {
            return esClient.exists(getRequest, EsAutoConfigure.COMMON_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("判断文档是否存在出错：indexName:{},id:{}", indexName, id);
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean deleteDoc(String indexName, String id) {
        DeleteRequest request = new DeleteRequest(indexName, id);
        DeleteResponse deleteResponse = null;
        try {
            deleteResponse = esClient.delete(request, EsAutoConfigure.COMMON_OPTIONS);
            return deleteResponse.getResult() == DocWriteResponse.Result.DELETED;
        } catch (IOException e) {
            e.printStackTrace();
            log.error("删除文档失败：indexName：{}，id:{}", indexName, id);
        }
        if (deleteResponse.getResult() == DocWriteResponse.Result.NOT_FOUND) {
            log.error("删除文档没有找到：indexName：{}，id:{}", indexName, id);
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean updateDoc(String indexName, String id, String upJson) {
        UpdateRequest request = new UpdateRequest(indexName, id);
        request.doc(upJson, XContentType.JSON);
        try {
            UpdateResponse updateResponse = esClient.update(request, EsAutoConfigure.COMMON_OPTIONS);
            return updateResponse.getResult() == DocWriteResponse.Result.UPDATED;
        } catch (ElasticsearchException | IOException e) {
            if (e instanceof ElasticsearchException) {
                if (((ElasticsearchException) e).status() == RestStatus.NOT_FOUND) {
                    log.error("更新文档没有找到：indexName：{}，id:{}", indexName, id);
                }
            }
            log.error("更新文档出错：indexName：{}，id:{}", indexName, id);
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean upsertDoc(String indexName, String id, String upserJson) {
        UpdateRequest request = new UpdateRequest(indexName, id);
        request.docAsUpsert(true);
        request.upsert(upserJson, XContentType.JSON);
        try {
            UpdateResponse updateResponse = esClient.update(request, EsAutoConfigure.COMMON_OPTIONS);
            if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
                log.info("更新文档不存在创建成功：indexName：{}，id:{}", indexName, id);
            } else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                log.info("更新文档存在,更新成功：indexName：{}，id:{}", indexName, id);
            }
            return Boolean.TRUE;
        } catch (ElasticsearchException | IOException e) {
            if (e instanceof ElasticsearchException) {
                if (((ElasticsearchException) e).status() == RestStatus.NOT_FOUND) {
                    log.error("更新文档没有找到：indexName：{}，id:{}", indexName, id);
                }
            }
            log.error("更新文档出错：indexName：{}，id:{}", indexName, id);
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean batchDoc(List<ReplicatedWriteRequest> requestList) {
        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                int numberOfActions = request.numberOfActions();
                log.debug("Executing bulk [{}] with {} requests",
                        executionId, numberOfActions);
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request,
                                  BulkResponse response) {
                if (response.hasFailures()) {
                    log.warn("Bulk [{}] executed with failures", executionId);
                    log.info("批量处理有文档处理异常request:{}", JSON.toJSONString(request));
                    log.info("批量处理有文档处理异常response:{}", JSON.toJSONString(response));
                } else {
                    log.debug("Bulk [{}] completed in {} milliseconds",
                            executionId, response.getTook().getMillis());
                }
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request,
                                  Throwable failure) {
                log.error("Failed to execute bulk", failure);
                log.info("批量处理异常request:{}", JSON.toJSONString(request));
            }
        };
        BulkProcessor bulkProcessor = BulkProcessor.builder(
                (request, bulkListener) ->
                        esClient.bulkAsync(request, EsAutoConfigure.COMMON_OPTIONS, bulkListener),
                listener, "bulk-processor-name").build();
        boolean terminated = false;
        try {
            if (CollectionUtils.isEmpty(requestList)) {
                return Boolean.FALSE;
            }
            for (ReplicatedWriteRequest request : requestList) {
                if (request instanceof IndexRequest) {
                    bulkProcessor.add((IndexRequest) request);
                }
                if (request instanceof DeleteRequest) {
                    bulkProcessor.add((DeleteRequest) request);
                }
                if (request instanceof DocWriteRequest) {
                    bulkProcessor.add((DocWriteRequest<?>) request);
                }
            }
            bulkProcessor.awaitClose(10, TimeUnit.MINUTES);
            return terminated;
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error("批量操作延迟关闭异常!");
        }
        return Boolean.FALSE;
    }

    @Override
    public Long count(String indexName, SearchSourceBuilder searchSourceBuilder) {
        log.info("count构造DSL：" + searchSourceBuilder.toString());
        CountRequest countRequest = new CountRequest(indexName);
        countRequest.source(searchSourceBuilder);
        try {
            CountResponse countResponse = esClient.count(countRequest, EsAutoConfigure.COMMON_OPTIONS);
            return countResponse.getCount();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取索引文旦总数异常！");
        }
        return 0L;
    }

    @Override
    public SearchResponse searchList(String indexName, SearchSourceBuilder searchSourceBuilder) {
        log.info("searchList构造DSL：" + searchSourceBuilder.toString());
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse searchResp = esClient.search(searchRequest, EsAutoConfigure.COMMON_OPTIONS);
            return searchResp;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("查询List出错！");
        }
        return null;
    }

    @Override
    public EsPageResult<T> searchPageList(String indexName, SearchSourceBuilder searchSourceBuilder, Class<?> clazz, Boolean usFastJson) {
        SearchResponse searchResponse = this.searchList(indexName, searchSourceBuilder);
        AnalysisSearchResponse<T> analysisSearchResponse = new AnalysisSearchResponse();
        List<T> sourceAsList = analysisSearchResponse.getSourceAsList(searchResponse, clazz, usFastJson);
        Long total = 0L;
        if (CollectionUtils.isEmpty(sourceAsList)) {
            sourceAsList = new ArrayList<>();
        } else {
            total = this.count(indexName, searchSourceBuilder);
        }
        EsPageResult<T> pageResult = new EsPageResult(sourceAsList, total.intValue());
        return pageResult;
    }


    @Override
    public GetResponse getDoc(String indexName, String id) {
        GetRequest request = new GetRequest(indexName, id);
        return getDocument(request);
    }

    @Override
    public GetResponse getDoc(String indexName, String[] fieldName, String id) {
        GetRequest request = new GetRequest(indexName, id);
        request.storedFields(fieldName);
        return getDocument(request);
    }

    @Override
    public GetResponse getDoc(String indexName, String id, String[] includeField, String[] excludeField) {
        GetRequest request = new GetRequest(indexName, id);
        FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includeField, excludeField);
        request.fetchSourceContext(fetchSourceContext);
        return getDocument(request);
    }

    /**
     * 公共方法
     *
     * @param request
     * @return
     */
    private GetResponse getDocument(GetRequest request) {
        GetResponse response = null;
        try {
            response = esClient.get(request, EsAutoConfigure.COMMON_OPTIONS);
            if (!response.isExists()) {
                log.info("文档不存在！");
                return null;
            }
        } catch (ElasticsearchException e) {
            e.printStackTrace();
            if (e.status() == RestStatus.NOT_FOUND) {
                log.error("获取文档出错！");
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("获取文档出错！");
        }
        return response;
    }


    @Override
    public MultiGetResponse getMultiDoc(String indexName, List<String> ids) {
        MultiGetRequest request = new MultiGetRequest();
        for (String id : ids) {
            request.add(new MultiGetRequest.Item(indexName, id));
        }
        MultiGetResponse response = null;
        try {
            response = esClient.mget(request, EsAutoConfigure.COMMON_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("获取多个文档出错！");
        }
        return response;
    }

    @Override
    public MultiGetResponse getMultiDoc(String indexName, String[] fieldName, List<String> ids) {
        MultiGetRequest request = new MultiGetRequest();
        for (String id : ids) {
            request.add(new MultiGetRequest.Item(indexName, id).storedFields(fieldName));
        }
        MultiGetResponse response = null;
        try {
            response = esClient.mget(request, EsAutoConfigure.COMMON_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("获取多个文档出错！");
        }
        return response;
    }


    @Override
    public MultiGetResponse getMultiDoc(String indexName, String[] includeField, String[] excludeField, List<String> ids) {
        MultiGetRequest request = new MultiGetRequest();
        FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includeField, excludeField);
        for (String id : ids) {
            request.add(new MultiGetRequest.Item(indexName, id).fetchSourceContext(fetchSourceContext));
        }
        MultiGetResponse response = null;
        try {
            response = esClient.mget(request, EsAutoConfigure.COMMON_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("获取多个文档出错！");
        }
        return response;
    }

    @Override
    public Boolean deleteByQueryDoc(String indexName, QueryBuilder query) {
        try {
            DeleteByQueryRequest request = new DeleteByQueryRequest(indexName).setQuery(query);
            BulkByScrollResponse bulkByScrollResponse = esClient.deleteByQuery(request, EsAutoConfigure.COMMON_OPTIONS);
            log.info("========deleteByQueryDoc().bulkByScrollResponse:{}==========", JSON.toJSONString(bulkByScrollResponse));
            return Boolean.TRUE;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("根据条件删除文档出错！");
        }
        return Boolean.FALSE;
    }

    @Override
    public SearchHit[] scrollQuery(String indexName, Integer size, SearchSourceBuilder searchSourceBuilder) {
        SearchRequest searchRequest = new SearchRequest(indexName);
        //初始化scroll
        //值不需要足够长来处理所有数据—它只需要足够长来处理前一批结果。每个滚动请求(带有滚动参数)设置一个新的过期时间。
        //设定滚动时间间隔
        Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        searchRequest.scroll(scroll);
        //设定每次返回多少条数据
        searchSourceBuilder.size(size);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;
        try {
            searchResponse = esClient.search(searchRequest, EsAutoConfigure.COMMON_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("=============scrollQuery查询失败==============");
        }
        String scrollId = searchResponse.getScrollId();
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        //遍历搜索命中的数据，直到没有数据
        while (searchHits != null && searchHits.length > 0) {
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            log.info("string:" + scrollRequest.toString());
            try {
                searchResponse = esClient.scroll(scrollRequest, EsAutoConfigure.COMMON_OPTIONS);
            } catch (IOException e) {
                e.printStackTrace();
                log.error("=============scrollQuery滚动失败==============");
            }
            scrollId = searchResponse.getScrollId();
            searchHits = searchResponse.getHits().getHits();
            if (searchHits != null && searchHits.length > 0) {
                for (SearchHit searchHit : searchHits) {
                    log.info("==============滚动每页数据：{}================:{}", searchHit.getSourceAsString());
                }
            }

        }
        //清除滚屏
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        //也可以选择setScrollIds()将多个scrollId一起使用
        clearScrollRequest.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse = null;
        try {
            clearScrollResponse = esClient.clearScroll(clearScrollRequest, EsAutoConfigure.COMMON_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("============scrollQuery清屏失败！================");
        }
        boolean succeeded = clearScrollResponse.isSucceeded();
        log.info("===========scrollQuery.succeeded=============={}", succeeded);
        return searchHits;
    }

}