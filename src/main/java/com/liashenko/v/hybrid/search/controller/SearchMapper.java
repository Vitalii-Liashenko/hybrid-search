package com.liashenko.v.hybrid.search.controller;

import com.liashenko.v.hybrid.search.controller.dto.ConferenceDto;
import com.liashenko.v.hybrid.search.model.Conference;
import org.mapstruct.Mapper;

import java.util.List;


@Mapper(implementationName = "Default<CLASS_NAME>")
public interface SearchMapper {

    ConferenceDto map(Conference conference);

    List<ConferenceDto> map(List<Conference> conferences);
}
