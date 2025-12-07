package com.liashenko.v.hybrid.search.service;

import com.liashenko.v.hybrid.search.model.Conference;

import java.util.List;

public interface EmbeddingService {

    List<Conference> embed(List<Conference> conferences);

    List<Float> embed(String text);
}
