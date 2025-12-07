package com.liashenko.v.hybrid.search.service;

import com.liashenko.v.hybrid.search.model.Conference;

import java.io.IOException;
import java.util.List;

public interface SearchService {
    void index(List<Conference> docs);

    List<Conference> search(String queryText, String type, int offset, int limit) throws IOException, InterruptedException;

    void deleteData();

    void createIndexIfNeeded();
}
