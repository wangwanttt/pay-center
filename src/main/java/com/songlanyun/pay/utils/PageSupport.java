package com.songlanyun.pay.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import com.songlanyun.pay.domain.AliTrade;
import lombok.Data;

@Data
public class PageSupport<T> {

    public List list;
    public int pageNumber;
    public int pageSize;
    public long total;

    public <T> PageSupport(List<AliTrade> collectList, int pageNo, int pageSize, Long totals) {
        this.pageNumber = pageNo;
        this.pageSize = pageSize;
        this.total = totals;
        this.list = collectList;
    }


}
