package com.zlf.es.spring.boot.autoconfigure.service.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zlf
 * @description:
 * @time: 2022/06/24
 */
@Data
public class EsPageResult<T> extends DocSearchVo implements Serializable {

    /**
     * 具体的数据
     */
    private List<T> data;
    /**
     * 总数据量
     */
    private Integer totalCount;
    /**
     * 总页数
     */
    private Integer totalPage;


    public EsPageResult() {

    }

    public EsPageResult(List<T> data, Integer totalCount) {
        this.data = data;
        this.totalCount = totalCount;
        this.totalPage = this.totalCount % this.pageSize == 0 ?
                this.totalCount / this.pageSize :
                this.totalCount / this.pageSize + 1;
    }

    public static EsPageResult empty() {
        return new EsPageResult(new ArrayList(), 0);
    }

}