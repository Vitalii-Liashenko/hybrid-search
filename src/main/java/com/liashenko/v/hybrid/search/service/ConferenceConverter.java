package com.liashenko.v.hybrid.search.service;

import com.liashenko.v.hybrid.search.service.model.Conference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.util.List;

@Mapper(implementationName = "Default<CLASS_NAME>")
public interface ConferenceConverter {

    @Mapping(target = "score", ignore = true)
    @Mapping(target = "embedding", ignore = true)
    Conference convertToConference(ConferenceCsvParser.ConferenceCsvBean csvBean);

    List<Conference> convertToConferences(List<ConferenceCsvParser.ConferenceCsvBean> csvBeans);

    default LocalDate toLocalDate(String dateString) {
        return LocalDate.parse(dateString);
    }
}
