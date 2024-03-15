package com.zlf.es.spring.boot.autoconfigure.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.BoostingQueryBuilder;
import org.elasticsearch.index.query.FuzzyQueryBuilder;
import org.elasticsearch.index.query.MatchPhrasePrefixQueryBuilder;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

/**
 * @author zlf
 * @description:
 * @time: 2022/6/24
 * 客户端也提供一个QueryBuilders类可以使用的,还有其它的一些Builders可以用的
 */
public class QueryBuild {

    private SearchSourceBuilder sourceBuilder;

    public QueryBuild(SearchSourceBuilder sourceBuilder) {
        this.sourceBuilder = sourceBuilder;
    }

    /**
     * 查询某个字段里含有某个关键词的文档
     *
     * @param fieldName  字段名称
     * @param fieldValue 字段值
     * @return
     */
    public SearchSourceBuilder termQuery(String fieldName, String fieldValue) {
        sourceBuilder.query(QueryBuilders.termQuery(fieldName, fieldValue));
        return sourceBuilder;
    }

    /**
     * 查询某个字段里含有多个关键词的文档
     *
     * @param fieldName  字段名称
     * @param fieldValue 字段值
     * @return
     */
    public SearchSourceBuilder termsQuery(String fieldName, String... fieldValue) {
        sourceBuilder.query(QueryBuilders.termsQuery(fieldName, fieldValue));
        return sourceBuilder;
    }

    /**
     * 查询所有文档
     */
    public SearchSourceBuilder queryAll() {
        sourceBuilder.query(matchAllQuery());
        return sourceBuilder;
    }

    /**
     * match 搜索
     *
     * @param field   字段
     * @param keyWord 搜索关键词
     */
    public SearchSourceBuilder queryMatch(String field, String keyWord) {
        sourceBuilder.query(QueryBuilders.matchQuery(field, keyWord));
        return sourceBuilder;
    }

    /**
     * 布尔match查询
     *
     * @param field   字段名称
     * @param keyWord 关键词
     * @param op      该参数取值为or 或 and
     */
    public SearchSourceBuilder queryMatchWithOperate(String field, String keyWord, Operator op) {
        sourceBuilder.query(QueryBuilders.matchQuery(field, keyWord).operator(op));
        return sourceBuilder;
    }

    /**
     * 该查询通过字段fields参数作用在多个字段上。
     * type的默认值是：BEST_FIELDS
     * multi_match 用于查询词匹配多个属性. 这里涉及到几种匹配策略:
     * <p>
     * best-fields
     * doc的某个属性匹配尽可能多的关键词, 那么这个doc会优先返回.
     * most-fields
     * 某个关键词匹配doc尽可能多的属性, 那么这个doc会优先返回.
     * cross_fields
     * 跨越多个field搜索一个关键词.
     * <p>
     * best-fields和most-fields的区别:
     * 比如, doc1的field1匹配的三个关键词, doc2的field1, field2都匹配上了同一个关键词. 如果是best-fields策略, 则doc1的相关度分数要更高, 如果是most-fields策略, 则doc2的相关度分数要更高.
     *
     * @param keyWord    关键字
     * @param type       类型
     * @param fieldNames 字段
     */
    public SearchSourceBuilder queryMulitMatch(String keyWord, String type, String... fieldNames) {
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(keyWord, fieldNames);
        if (StringUtils.isNotEmpty(type) && MultiMatchQueryBuilder.Type.MOST_FIELDS.equals(type)) {
            multiMatchQueryBuilder.type(MultiMatchQueryBuilder.Type.MOST_FIELDS);
        } else if (StringUtils.isNotEmpty(type) && MultiMatchQueryBuilder.Type.CROSS_FIELDS.equals(type)) {
            multiMatchQueryBuilder.type(MultiMatchQueryBuilder.Type.CROSS_FIELDS);
        }
        sourceBuilder.query(multiMatchQueryBuilder);
        return sourceBuilder;
    }

    /**
     * 对查询词语分析后构建一个短语查询
     *
     * @param fieldName 字段名称
     * @param keyWord   关键字
     */
    public SearchSourceBuilder queryMatchPhrase(String fieldName, String keyWord) {
        sourceBuilder.query(QueryBuilders.matchPhraseQuery(fieldName, keyWord));
        return sourceBuilder;
    }

    /**
     * 默认使用 match_phrase 时会精确匹配查询的短语，需要全部单词和顺序要完全一样，标点符号除外
     * max_expansions 控制着可以与前缀匹配的词的数量，默认值是 50。
     * slop 参数告诉 match_phrase 查询词条相隔多远时仍然能将文档视为匹配
     * analyzer 使用分词器指定
     *
     * @param fieldName
     * @param keyWord
     * @return
     */
    public SearchSourceBuilder queryMatchPrefixQuery(String fieldName, String keyWord, String analyzer, int maxExpansions, int slop) {
        MatchPhrasePrefixQueryBuilder matchPhrasePrefixQueryBuilder = QueryBuilders.matchPhrasePrefixQuery(fieldName, keyWord);
        if (StringUtils.isNotEmpty(analyzer)) {
            matchPhrasePrefixQueryBuilder.analyzer(analyzer);
        }
        if (Objects.nonNull(maxExpansions) && maxExpansions > 1) {
            matchPhrasePrefixQueryBuilder.maxExpansions(maxExpansions);
        }
        if (Objects.nonNull(slop) && slop > 1) {
            matchPhrasePrefixQueryBuilder.slop(slop);
        }
        sourceBuilder.query(matchPhrasePrefixQueryBuilder);
        return sourceBuilder;
    }

    /**
     * 查出指定_id的文档
     *
     * @param ids ids值
     */

    public SearchSourceBuilder idsQuery(String... ids) {
        sourceBuilder.query(QueryBuilders.idsQuery().addIds(ids));
        return sourceBuilder;
    }

    /**
     * 查找某字段以某个前缀开头的文档
     *
     * @param field  字段
     * @param prefix 前缀
     */
    public SearchSourceBuilder prefixQuery(String field, String prefix) {
        sourceBuilder.query(QueryBuilders.prefixQuery(field, prefix));
        return sourceBuilder;
    }

    /**
     * 查找某字段以某个前缀开头的文档
     *
     * @param field        字段
     * @param value        查询关键字
     * @param prefixLength 前缀长度
     */
    public SearchSourceBuilder fuzzyQuery(String field, String value, int prefixLength) {
        FuzzyQueryBuilder fuzzyQueryBuilder = QueryBuilders.fuzzyQuery(field, value);
        if (Objects.nonNull(prefixLength) && prefixLength > 0) {
            fuzzyQueryBuilder.prefixLength(prefixLength);
        }
        sourceBuilder.query(fuzzyQueryBuilder);
        return sourceBuilder;
    }

    /**
     * 以通配符来查询
     *
     * @param fieldName 字段名称
     * @param wildcard  通配符
     */
    public SearchSourceBuilder wildCardQuery(String fieldName, String wildcard) {
        sourceBuilder.query(QueryBuilders.wildcardQuery(fieldName, wildcard));
        return sourceBuilder;
    }

    /**
     * 自定义：
     * key : fieldName-must/mustNot/filter
     * ----------key: from,value:value/(vlue|(gte/gt))
     * --------------------------只有from时：value中指定 gte >=还是 gt >
     * ----------to:   to, value:value/(vlue|(lte/lt))
     * -------------------------只有to时：value中指定 lte <=还是 lt <
     *
     * @param mapMap
     */
    public SearchSourceBuilder rangeBoolQuery(Map<String, Map<String, Object>> mapMap) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (CollectionUtil.isEmpty(mapMap)) {
            for (Map.Entry<String, Map<String, Object>> mapEntry : mapMap.entrySet()) {
                String key = mapEntry.getKey();
                if (StringUtils.isNotEmpty(key)) {
                    String[] split = key.split("-");
                    if (split.length == 2) {
                        key = split[0];
                        String ruleName = split[1];
                        Map<String, Object> vMap = mapEntry.getValue();
                        if (CollectionUtil.isNotEmpty(vMap)) {
                            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(key);
                            if ("must".equals(ruleName)) {
                                for (Map.Entry<String, Object> vEntry : vMap.entrySet()) {
                                    String key1 = vEntry.getKey();
                                    Object value = vEntry.getValue();
                                    if ("from".equals(key1)) {
                                        this.paraFromValue(rangeQueryBuilder, value);
                                    }
                                    if ("to".equals(key1)) {
                                        this.paraToValue(rangeQueryBuilder, value);
                                    }
                                }
                                boolQueryBuilder.must(boolQueryBuilder);
                            }
                            if ("mustNot".equals(ruleName)) {
                                for (Map.Entry<String, Object> vEntry : vMap.entrySet()) {
                                    String key1 = vEntry.getKey();
                                    Object value = vEntry.getValue();
                                    if ("from".equals(key1)) {
                                        this.paraFromValue(rangeQueryBuilder, value);
                                    }
                                    if ("to".equals(key1)) {
                                        this.paraToValue(rangeQueryBuilder, value);
                                    }
                                }
                                boolQueryBuilder.mustNot(boolQueryBuilder);
                            }
                            if ("filter".equals(ruleName)) {
                                for (Map.Entry<String, Object> vEntry : vMap.entrySet()) {
                                    String key1 = vEntry.getKey();
                                    Object value = vEntry.getValue();
                                    if ("from".equals(key1)) {
                                        this.paraFromValue(rangeQueryBuilder, value);
                                    }
                                    if ("to".equals(key1)) {
                                        this.paraToValue(rangeQueryBuilder, value);
                                    }
                                }
                                boolQueryBuilder.filter(rangeQueryBuilder);
                            }
                        }
                    }
                }
            }
        }
        sourceBuilder.query(boolQueryBuilder);
        return sourceBuilder;
    }

    /**
     * 解析FromValue的值和规则
     *
     * @param rangeQueryBuilder
     * @param value
     */
    private void paraFromValue(RangeQueryBuilder rangeQueryBuilder, Object value) {
        String vs = String.valueOf(value);
        String[] split1 = vs.split("\\|");
        if (split1.length == 1) {
            rangeQueryBuilder.from(value);
        } else {
            String s = split1[1];
            if ("gte".equals(s)) {
                rangeQueryBuilder.gte(s);
            } else if ("gt".equals(s)) {
                rangeQueryBuilder.gt(s);
            }
        }
    }

    /**
     * 解析ToValue的值和规则
     *
     * @param rangeQueryBuilder
     * @param value
     */
    private void paraToValue(RangeQueryBuilder rangeQueryBuilder, Object value) {
        String vs = String.valueOf(value);
        String[] split1 = vs.split("\\|");
        if (split1.length == 1) {
            rangeQueryBuilder.to(value);
        } else {
            String s = split1[1];
            if ("gte".equals(s)) {
                rangeQueryBuilder.lte(s);
            } else if ("gt".equals(s)) {
                rangeQueryBuilder.lt(s);
            }
        }
    }

    /**
     * 范围查询 between
     *
     * @param fieldName 字段名称
     * @param from
     * @param to
     */
    public SearchSourceBuilder rangeQueryBetween(String fieldName, Object from, Object to) {
        sourceBuilder.query(QueryBuilders.rangeQuery(fieldName).from(from).to(to));
        return sourceBuilder;
    }

    /**
     * 范围查询 gte >=
     *
     * @param fieldName 字段名称
     * @param from
     */
    public SearchSourceBuilder rangeQueryGte(String fieldName, Object from) {
        sourceBuilder.query(QueryBuilders.rangeQuery(fieldName).gte(from));
        return sourceBuilder;
    }

    /**
     * 范围查询 gt >
     *
     * @param fieldName 字段名称
     * @param from
     */
    public SearchSourceBuilder rangeQueryGt(String fieldName, Object from) {
        sourceBuilder.query(QueryBuilders.rangeQuery(fieldName).gt(from));
        return sourceBuilder;
    }

    /**
     * 范围查询 lte <=
     *
     * @param fieldName 字段名称
     * @param to
     */
    public SearchSourceBuilder rangeQueryLte(String fieldName, Object to) {
        sourceBuilder.query(QueryBuilders.rangeQuery(fieldName).lte(to));
        return sourceBuilder;
    }

    /**
     * 范围查询 lt <
     *
     * @param fieldName 字段名称
     * @param to
     */
    public SearchSourceBuilder rangeQueryLt(String fieldName, Object to) {
        sourceBuilder.query(QueryBuilders.rangeQuery(fieldName).lt(to));
        return sourceBuilder;
    }


    /**
     * 正则表达示查询
     *
     * @param fieldName 字段名称
     * @param regexp    正则表达示
     */
    public SearchSourceBuilder regexpQuery(String fieldName, String regexp) {
        sourceBuilder.query(QueryBuilders.regexpQuery(fieldName, regexp));
        return sourceBuilder;
    }

    /**
     * @param fieldNames
     * @param likeTexts
     * @return min_term_freq：一篇文档中一个词语至少出现次数，小于这个值的词将被忽略，默认是2
     */
    public SearchSourceBuilder moreLikeThisQuery(String[] fieldNames, String[] likeTexts, int minTermFreq) {
        MoreLikeThisQueryBuilder moreLikeThisQueryBuilder = QueryBuilders.moreLikeThisQuery(likeTexts);
        if (Objects.nonNull(minTermFreq)) {
            moreLikeThisQueryBuilder.minTermFreq(minTermFreq);
        }
        sourceBuilder.query(moreLikeThisQueryBuilder);
        return sourceBuilder;
    }

    /**
     * 有四种特殊的实现（Field-，Score-，GeoDistance-和ScriptSortBuilder）。
     *
     */

    /**
     * 根据score排序--升序
     * 默认值:分数降序
     *
     * @return
     */
    public SearchSourceBuilder scoreSort() {
        sourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));
        return sourceBuilder;
    }

    /**
     * 根据字段排序
     * 默认是降序
     *
     * @param filedMaps
     * @return
     */
    public SearchSourceBuilder filedSort(HashMap<String, String> filedMaps) {
        if (CollectionUtil.isEmpty(filedMaps)) {
            throw new IllegalArgumentException("没有设置排序!");
        }
        for (Map.Entry<String, String> entry : filedMaps.entrySet()) {
            if (SortOrder.ASC.toString().equals(entry.getValue())) {
                sourceBuilder.sort(new FieldSortBuilder(entry.getKey()).order(SortOrder.ASC));
            } else if (SortOrder.DESC.toString().equals(entry.getValue())) {
                sourceBuilder.sort(new FieldSortBuilder(entry.getKey()).order(SortOrder.DESC));
            }
        }
        return sourceBuilder;
    }

    /**
     * 根据字段排序
     * 默认是降序
     *
     * @param fieldName
     * @param sortValue
     * @return
     */
    public SearchSourceBuilder filedSortOne(String fieldName, String sortValue) {
        if (SortOrder.ASC.toString().equals(sortValue)) {
            sourceBuilder.sort(new FieldSortBuilder(fieldName).order(SortOrder.ASC));
        } else if (SortOrder.DESC.toString().equals(sortValue)) {
            sourceBuilder.sort(new FieldSortBuilder(fieldName).order(SortOrder.DESC));
        }
        return sourceBuilder;
    }

    /**
     * 复合查询:
     * bool查询
     * 将多个查询条件以一定的逻辑组合在一起
     * must：表示and的意思，所有的条件都符合才能找到
     * must_not：把满足条件的都去掉的结果
     * should：表示or的意思
     *
     * @param boolQueryBuilder <p>
     *                         栗子：
     * @return //BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
     * //boolQueryBuilder.should(QueryBuilders.termQuery("province", "河南"));
     * //boolQueryBuilder.should(QueryBuilders.termQuery("province", "北京"));
     * //boolQueryBuilder.mustNot(QueryBuilders.termQuery("operatorId", "2"));
     * //boolQueryBuilder.must(QueryBuilders.matchQuery("smsContent", "中国"));
     * //boolQueryBuilder.must(QueryBuilders.matchQuery("smsContent", "移动"));
     * <p>
     */
    public SearchSourceBuilder boolQueryBuilder(BoolQueryBuilder boolQueryBuilder) {
        sourceBuilder.query(boolQueryBuilder);
        return sourceBuilder;
    }

    /**
     * 复合查询:
     * boolsting查询
     * 分数查询，比如math查询,结果有一个匹配度分数.
     * boolsting 查询可以针对内容，让其分数大，或者小，达到排前，排后的效果
     * <p>
     * positive： 指定查询条件,  只有匹配到positive的内容，才会放到结果集，也就是放查询条件的地方
     * negative：如果匹配到的positive和negative，就会降低文档的分数
     * negative_boost：对匹配到的positive和negative的内容, 指
     * <p>
     * 定降低分数的系数. 这个系数必须小于1.0，比如：10分 这个系数为0.5就会变为5分
     * <p>
     * 关于分数的计算：
     * 关键字在文档出现的频次越高，分数越高
     * 文档的内容越短，分数越高
     * 搜索时候，指定的关键字会被分词，分词内容匹配分词库，匹配的个数越多，分数就越高
     * <p>
     * 栗子：
     * BoostingQueryBuilder boostingQueryBuilder = QueryBuilders.boostingQuery(
     * //相当于positivequery
     * QueryBuilders.matchQuery("smsContent", "电话").operator(Operator.OR),
     * QueryBuilders.matchQuery("smsContent", "订单")
     * ).negativeBoost(0.1f);
     */
    public SearchSourceBuilder boostingQueryBuilder(BoostingQueryBuilder boostingQueryBuilder) {
        sourceBuilder.query(boostingQueryBuilder);
        return sourceBuilder;
    }

    /**
     * 复合查询:
     * filter查询:  如果结果不需要匹配度
     * 与query查询相比, 会计算分数, 并对结果排序
     * filter不计算分数, 结果也不排序,
     * 所以这就是filter查询比query查询快的原因
     * 栗子：
     * boolQueryBuilder.filter(QueryBuilders.termQuery("corpName", "网易"));
     */
    public SearchSourceBuilder filter(List<QueryBuilder> queryBuilders) {
        if (CollectionUtil.isEmpty(queryBuilders)) {
            throw new IllegalArgumentException("filter查询参数异常");
        }
        //先创建Boolean查询对象
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        for (QueryBuilder queryBuilder : queryBuilders) {
            boolQueryBuilder.filter(queryBuilder);
        }
        sourceBuilder.query(boolQueryBuilder);
        return sourceBuilder;
    }

    //TODO 聚合统计分析（todo other）

    /**
     * 根据一个或者多个字段group by Stat统计
     *
     * @return
     */
    public SearchSourceBuilder aggStat(Map<String, List<String>> mapList, Integer size) {
        if (CollectionUtil.isEmpty(mapList)) {
            throw new IllegalArgumentException("aggStat聚合统计参数不为空");
        }
        int i = 1;
        for (Map.Entry<String, List<String>> entry : mapList.entrySet()) {
            TermsAggregationBuilder agg1 = AggregationBuilders.terms(entry.getKey() + i)
                    //text类型不能用于索引或排序，必须转成keyword类型
                    .field(entry.getKey() + i + ".keyword")
                    .size(size);
            agg1.subAggregation(AggregationBuilders.stats("stat_" + entry.getKey() + i))
                    .field("stat_" + entry.getKey() + i);
            this.valueListTosubAgg(i, entry, agg1);
            sourceBuilder.aggregation(agg1);
            i++;
        }
        return sourceBuilder;
    }

    /**
     * 根据一个或者多个字段group by count统计
     *
     * @return
     */
    public SearchSourceBuilder aggCount(Map<String, List<String>> mapList, Integer size) {
        if (CollectionUtil.isEmpty(mapList)) {
            throw new IllegalArgumentException("aggCount聚合统计参数不为空");
        }
        int i = 1;
        for (Map.Entry<String, List<String>> entry : mapList.entrySet()) {
            TermsAggregationBuilder agg1 = AggregationBuilders.terms(entry.getKey() + i)
                    //text类型不能用于索引或排序，必须转成keyword类型
                    .field(entry.getKey() + i + ".keyword")
                    .size(size)
                    .order(BucketOrder.aggregation("count", true));
            agg1.subAggregation(AggregationBuilders.count("count_" + entry.getKey() + i))
                    .field("count_" + entry.getKey() + i);
            this.valueListTosubAgg(i, entry, agg1);
            sourceBuilder.aggregation(agg1);
            i++;
        }
        return sourceBuilder;
    }

    /**
     * 将map中List的values设置到子聚合中
     *
     * @param i
     * @param entry
     * @param agg1
     */
    private void valueListTosubAgg(int i, Map.Entry<String, List<String>> entry, TermsAggregationBuilder agg1) {
        List<String> values = entry.getValue();
        if (CollectionUtil.isNotEmpty(values)) {
            for (int j = 1; j <= values.size(); j++) {
                agg1.subAggregation(AggregationBuilders.terms(values.get(i) + i).field(values.get(i) + i).size(1));
            }
        }
    }

}