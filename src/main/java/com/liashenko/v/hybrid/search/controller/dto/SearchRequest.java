package com.liashenko.v.hybrid.search.controller.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchRequest {

    public String queryText;
    public SearchType searchType = SearchType.HYBRID;
    private Integer limit = 20;
    private Integer offset = 0;

    public enum SearchType {
        HYBRID,
        VECTOR,
        KEYWORD
    }
}
