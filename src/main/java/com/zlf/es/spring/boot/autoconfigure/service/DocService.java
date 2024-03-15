package com.zlf.es.spring.boot.autoconfigure.service;

import com.zlf.es.spring.boot.autoconfigure.service.vo.EsPageResult;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.replication.ReplicatedWriteRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.List;

/**
 * @author zlf
 * @description:
 * @time: 2022/06/24
 */
public interface DocService {

    /**
     * 根据文档id判断文档是否存在
     *
     * @param indexName
     * @param id
     * @return
     */
    Boolean existsDoc(String indexName, String id);

    /**
     * 根据文档id删除文档
     *
     * @param indexName
     * @param id
     * @return
     */
    Boolean deleteDoc(String indexName, String id);

    /**
     * 根据文档id部分更新文档数据
     *
     * @param indexName
     * @param id
     * @param upJson
     * @return
     */
    Boolean updateDoc(String indexName, String id, String upJson);

    /**
     * 根据文档id部分更新文档数据,如果文档不存在则新增一个文档
     *
     * @param indexName
     * @param id
     * @param upserJson
     * @return
     */
    Boolean upsertDoc(String indexName, String id, String upserJson);


    /**
     * 批量新增、修改和删除文档
     * 只支持insert和delete的批量
     * 请参看官方api接口文档
     *
     * @param requestList
     * @return
     */
    Boolean batchDoc(List<ReplicatedWriteRequest> requestList);


    /**
     * 根据条件获取文档总数
     *
     * @param indexName
     * @param searchSourceBuilder
     * @return
     */
    Long count(String indexName, SearchSourceBuilder searchSourceBuilder);


    /**
     * 公共查一个List
     *
     * @param indexName
     * @param searchSourceBuilder
     * @return
     */
    SearchResponse searchList(String indexName, SearchSourceBuilder searchSourceBuilder);


    /**
     * 按条件分页查询,返回一个分页对象
     *
     * @param indexName
     * @param searchSourceBuilder
     * @param clazz
     * @param usFastJson
     * @return
     */
    EsPageResult searchPageList(String indexName, SearchSourceBuilder searchSourceBuilder, Class<?> clazz,Boolean usFastJson);


    /**
     * 根据indexName和id获取一个文档
     *
     * @param indexName
     * @param id
     * @return
     */
    GetResponse getDoc(String indexName, String id);

    /**
     * 根据indexName和id获取一个文档
     *
     * @param indexName
     * @param id
     * @param fieldName 设置包含的字段
     * @return
     */
    GetResponse getDoc(String indexName, String[] fieldName, String id);


    /**
     * 根据indexName和id获取一个文档
     *
     * @param indexName
     * @param id
     * @param includeField 设置包含的字段
     * @param excludeField 设置排除的字段
     * @return
     */
    GetResponse getDoc(String indexName, String id, String[] includeField, String[] excludeField);

    /**
     * 根据indexName和文档ids获取多个文档
     *
     * @param indexName
     * @param ids
     * @return
     */
    MultiGetResponse getMultiDoc(String indexName, List<String> ids);

    /**
     * 根据indexName、包含字段和文档ids获取多个文档
     *
     * @param indexName
     * @param fieldName
     * @param ids
     * @return
     */
    MultiGetResponse getMultiDoc(String indexName, String[] fieldName, List<String> ids);

    /**
     * 根据indexName、包含字段、排除字段和文档ids获取多个文档
     *
     * @param indexName
     * @param includeField
     * @param excludeField
     * @param ids
     * @return
     */
    MultiGetResponse getMultiDoc(String indexName, String[] includeField, String[] excludeField, List<String> ids);

    /**
     * 根据条件删除指定索引名的doc数据
     *
     * @param indexName
     * @param query
     * @return
     */
    Boolean deleteByQueryDoc(String indexName, QueryBuilder query);

    /**
     * 滚动查询
     *
     * @param indexName
     * @param size
     * @param searchSourceBuilder
     * @return
     */
    SearchHit[] scrollQuery(String indexName, Integer size, SearchSourceBuilder searchSourceBuilder);

}