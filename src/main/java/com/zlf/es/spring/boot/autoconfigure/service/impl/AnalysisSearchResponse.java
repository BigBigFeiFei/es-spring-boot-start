package com.zlf.es.spring.boot.autoconfigure.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.math.Stats;
import com.zlf.es.spring.boot.autoconfigure.config.FastJsonConfig;
import com.zlf.es.spring.boot.autoconfigure.service.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zlf
 * @description:
 * @time: 2022/06/24
 */
@Slf4j
public class AnalysisSearchResponse<T> {

    /**
     * 解析查询结果1
     *
     * @param searchResponse
     * @return
     */
    public List<Map<String, Object>> getSourceAsMap(SearchResponse searchResponse) {
        List<Map<String, Object>> result = new ArrayList<>();
        SearchHits hits = searchResponse.getHits();
        log.info("count1:" + hits.getTotalHits());
        SearchHit[] h = hits.getHits();
        for (SearchHit hit : h) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            result.add(sourceAsMap);
        }
        return result;
    }

    /**
     * 解析查询结果2
     * 返回后反序列化为一个实体对象
     *
     * @param searchResponse
     * @return
     */
    public String getSourceAsString(SearchResponse searchResponse) {
        StringBuilder sb = new StringBuilder();
        SearchHits hits = searchResponse.getHits();
        log.info("count2:" + hits.getTotalHits());
        SearchHit[] h = hits.getHits();
        for (SearchHit hit : h) {
            sb.append(hit.getSourceAsString());
        }
        return sb.toString();
    }

    /**
     * 解析查询结果3
     * 返回后反序列化为List
     *
     * @param searchResponse
     * @param clazz
     * @param usFastJson
     * @return
     */
    public List<T> getSourceAsList(SearchResponse searchResponse, Class<?> clazz, Boolean usFastJson) {
        //StringBuilder sb = new StringBuilder();
        List<T> result = new ArrayList<>();
        SearchHits hits = searchResponse.getHits();
        log.info("count3:" + hits.getTotalHits());
        SearchHit[] h = hits.getHits();
        if (h.length == 0) {
            return null;
        }
        for (SearchHit hit : h) {
            T t;
            if (usFastJson) {
                FastJsonConfig fs = new FastJsonConfig();
                t = (T) JSON.parseObject(hit.getSourceAsString(), clazz);
            } else {
                t = (T) JsonUtils.getObjectByJson(hit.getSourceAsString(), clazz);
            }
            result.add(t);
            //sb.append(hit.getSourceAsString());
        }
        //String s = sb.toString();
       /* 注意：这里不能直接反序列化一个s,因为s是一个字符串，但是不是一个json字符串，所以需要一个hit反序列化之后用一个List集合收集然后返回
        1.使用fastJson反序列化为一个List
        List<T> result = JSONObject.parseObject(JSONObject.toJSONString(s), new TypeReference<List<T>>() {
        });
        2.使用jackson反序列化为一个List
        2.1 List<T> result = (List<T>) JsonUtils.getListByJson(s, List.class, clazz);
        2.2 JsonUtils<T> jsonUtils = new JsonUtils<>();
        List<T> result= jsonUtils.getListByJson(s, clazz);
        */
        return result;
    }

    /**
     * 解析查询结果4
     * 返回后根据设置的高亮字段取值设置到返回实体的字段中即可
     *
     * @param searchResponse
     * @return
     */
    public List<Map<String, HighlightField>> getHighlightFields(SearchResponse searchResponse) {
        List<Map<String, HighlightField>> result = new ArrayList<>();
        SearchHits hits = searchResponse.getHits();
        log.info("count4:" + hits.getTotalHits());
        SearchHit[] h = hits.getHits();
        for (SearchHit hit : h) {
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            result.add(highlightFields);
        }
        return result;
    }


    /**
     * 解析查询结果5
     *
     * @param searchResponse
     * @param mapList
     * @param methodName:stat/count(其它统计方法需要自行实现)
     * @return
     */
    public Map<String, String> getAggregations(SearchResponse searchResponse, Map<String, List<String>> mapList, String methodName) {
        Map<String, String> result = new HashMap<>();
        Aggregations aggregations = searchResponse.getAggregations();
        if (CollectionUtil.isEmpty(mapList)) {
            throw new IllegalArgumentException("mapList不为空!");
        }
        log.info("==========getAggregations.mapList:{}==========", JSON.toJSONString(mapList));
        int i = 1;
        for (Map.Entry<String, List<String>> entry : mapList.entrySet()) {
            Terms terms = aggregations.get(methodName + "_" + entry.getKey() + i);
            List<? extends Terms.Bucket> buckets = terms.getBuckets();
            for (Terms.Bucket bucket : buckets) {
                Stats statAge = bucket.getAggregations().get(methodName + "_" + i);
                StringBuilder sb = new StringBuilder();
                String stateJson = JSON.toJSONString(statAge);
                sb.append(stateJson).append("|");
                for (int j = 1; j <= mapList.size(); j++) {
                    Terms termJ = bucket.getAggregations().get(mapList.get(j) + String.valueOf(j));
                    List<? extends ParsedTerms> bucketsJ = (List<? extends ParsedTerms>) termJ.getBuckets();
                    for (ParsedTerms parsedBucket : bucketsJ) {
                        Map<String, Object> metadata = parsedBucket.getMetadata();
                        String parsedMetaData = JSON.toJSONString(metadata);
                        sb.append(parsedMetaData);
                    }
                }
                result.put(String.valueOf(bucket.getKey()), sb.toString());
            }
            i++;
        }
        log.info("==========getAggregations.result:{}==========", JSON.toJSONString(result));
        return result;
    }


}