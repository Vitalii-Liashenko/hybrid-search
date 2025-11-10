package com.liashenko.v.hybrid.search.controller;

import com.google.common.base.Stopwatch;
import com.liashenko.v.hybrid.search.service.SearchService;
import com.liashenko.v.hybrid.search.service.CsvDataLoaderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for data loading operations.
 * Provides endpoints for loading countries and cities from CSV files.
 */
@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("admin/api/")
@AllArgsConstructor
public class DataLoadController {

    private final SearchService searchService;
    private final CsvDataLoaderService csvDataLoaderService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/load/data")
    public void loadData() {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            searchService.createIndexIfNeeded();
            csvDataLoaderService.loadToIndex();
            log.info("Data loaded successfully. Took: {}", stopwatch);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error during loading data: " + e.getMessage(), e);
        }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/delete/data")
    public void deleteData() {
        try {
            searchService.deleteData();
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error during loading data: " + e.getMessage(), e);
        }
    }
}
