package com.abmsa.common;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private long total;
    private List<T> records;

    private PageResult(long total, List<T> records) {
        this.total = total;
        this.records = records;
    }

    public static <T> PageResult<T> of(long total, List<T> records) {
        return new PageResult<>(total, records);
    }
}
