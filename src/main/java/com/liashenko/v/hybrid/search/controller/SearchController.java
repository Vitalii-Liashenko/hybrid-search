package com.liashenko.v.hybrid.search.controller;

import com.liashenko.v.hybrid.search.controller.dto.SearchRequest;
import com.liashenko.v.hybrid.search.controller.dto.SearchResponse;
import com.liashenko.v.hybrid.search.model.Conference;
import com.liashenko.v.hybrid.search.service.SearchService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RequestMapping("/api")
@RestController
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final SearchMapper mapper;

    @ResponseStatus(OK)
    @PostMapping("/search")
    public SearchResponse match(@Valid @RequestBody SearchRequest searchRequest) throws IOException, InterruptedException {
        List<Conference> documents = searchService.search(
                searchRequest.getQueryText(),
                searchRequest.searchType.name(),
                searchRequest.getOffset(),
                searchRequest.getLimit());
        return new SearchResponse(mapper.map(documents));
    }
}


