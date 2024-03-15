package com.zlf.es.spring.boot.autoconfigure.service;

import org.elasticsearch.search.builder.SearchSourceBuilder;

/**
 * @author zlf
 * @description:
 * @time: 2022/06/24
 */
public interface IndexService {

    /**
     * 创建索引
     *
     * @param indexName
     * @param settings
     * @param mappings
     * @throws Exception
     */
    Boolean createIndex(String indexName, String settings, String mappings) throws Exception;

    /**
     * 删除索引
     *
     * @param indexName
     * @throws Exception
     */
    Boolean deleteIndex(String indexName) throws Exception;

    /**
     * 判断索引是否存在
     *
     * @param indexName
     * @return
     * @throws Exception
     */
    Boolean existsIndex(String indexName) throws Exception;


    /**
     * @param sourceIndexName     源索引名称
     * @param targetIndexName     目标索引--->要求索引必须存在
     * @param sourceSettings      源索引的settings设置
     * @param sourceMappings      源索引的mappings设置
     * @param searchSourceBuilder 该参数可以不传,传null也是可以的
     *                            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
     *                            String[] includes = {};
     *                            String[] excludes = {"isbnNo"};
     *                            searchSourceBuilder.fetchSource(includes, excludes);
     *                            改参数可以提取源索引中的文档字段和根据设置的条件获取到源索引中你想要的文档数据
     */
    Boolean reindex(String sourceIndexName, String targetIndexName, String sourceSettings, String sourceMappings, SearchSourceBuilder searchSourceBuilder) throws Exception;

}
