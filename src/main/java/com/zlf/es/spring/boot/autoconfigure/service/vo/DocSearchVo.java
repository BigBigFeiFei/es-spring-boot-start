package com.zlf.es.spring.boot.autoconfigure.service.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.List;

/**
 * @author zlf
 * @description:
 * @time: 2022/06/24
 */
@Data
public class DocSearchVo implements Serializable {

    /**
     * 当前页
     */
    protected Integer currentPage = 1;

    /**
     * 每页多少条
     */
    protected Integer pageSize = 10;

    /**
     * 开始的记录数
     *
     * @return
     */
    @JsonIgnore
    public Integer getStart() {
        return (currentPage - 1) * pageSize;
    }


    /**
     * 复合查询多条件AND
     */
    @JsonIgnore
    public Operator op = Operator.AND;

    /**
     * 创建SearchSourceBuilder，并设置通用的属性
     *
     * @return
     */
    public SearchSourceBuilder createSearchSourceBuilder(Boolean isPage) {
        //这里可以给searchSourceBuilder设置一些通用的条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        if (isPage) {
            searchSourceBuilder.from(getStart().intValue());
            searchSourceBuilder.size(pageSize.intValue());
        }
        return searchSourceBuilder;
    }

    /**
     * 创建HighlightBuilder对象，并设置一些通用的属性
     *
     * @return
     */
    public HighlightBuilder createHighlightBuilder(String preTags, String postTags, List<String> fields) {
        //这里可以给HighlightBuilder设置一些通用的高亮配置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        if (CollectionUtils.isEmpty(fields)) {
            throw new IllegalArgumentException("==========没有设置高亮字段==========");
        }
        if (fields.size() > 1) {
            //如果要多个字段高亮,这项要为false(多次高亮)
            highlightBuilder.requireFieldMatch(false);
        } else {
            //如果要多个字段高亮,这项要为false
            highlightBuilder.requireFieldMatch(true);
        }
        for (String fd : fields) {
            HighlightBuilder.Field h = new HighlightBuilder.Field(fd).highlighterType("unified");
            //设置高亮字段
            highlightBuilder.field(h);
        }
        //下面这两项,如果你要高亮如文字内容等有很多字的字段,必须配置,不然会导致高亮不全,文章内容缺失等
        //最大高亮分片数
        highlightBuilder.fragmentSize(800000);
        //从第一个分片获取高亮片段
        highlightBuilder.numOfFragments(0);

        if (StringUtils.isNotEmpty(preTags) && StringUtils.isNotEmpty(postTags)) {
            highlightBuilder.preTags(preTags);
            highlightBuilder.postTags(postTags);
        } else {
            highlightBuilder.preTags("<span style='color:red'>");
            highlightBuilder.postTags("</span>");
        }
        return highlightBuilder;
    }


}