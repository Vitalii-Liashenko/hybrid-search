package com.liashenko.v.hybrid.search.service;

import com.liashenko.v.hybrid.search.model.Conference;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Reader;
import java.util.List;

@Slf4j
public class ConferenceCsvParser {

    private final ConferenceConverter conferenceConverter = new DefaultConferenceConverter();

    public List<Conference> parseConferences(Reader reader) {
        List<ConferenceCsvBean> csvBeans = new CsvToBeanBuilder<ConferenceCsvBean>(reader)
                .withType(ConferenceCsvBean.class)
                .withIgnoreLeadingWhiteSpace(true)
                .withIgnoreEmptyLine(true)
                .withSkipLines(1) // Skip header line
                .build()
                .parse();

        return conferenceConverter.convertToConferences(csvBeans);
    }

    @Data
    @NoArgsConstructor
    public static class ConferenceCsvBean {

        @CsvBindByPosition(position = 0)
        private String id;
        @CsvBindByPosition(position = 1)
        private Integer groupId;
        @CsvBindByPosition(position = 2)
        private String name;
        @CsvBindByPosition(position = 3)
        private String description;
        @CsvBindByPosition(position = 4)
        private String startDate;
        @CsvBindByPosition(position = 5)
        private String endDate;
        @CsvBindByPosition(position = 6)
        private String formattedLocation;
        @CsvBindByPosition(position = 7)
        private String countryDescription;
        @CsvBindByPosition(position = 8)
        private Integer attendeesCount;
        @CsvBindByPosition(position = 9)
        private Integer companyAttendeesCount;
        @CsvBindByPosition(position = 10)
        private Integer investorAttendeesCount;
        @CsvBindByPosition(position = 11)
        private String attendeeNamesConcatString;
        @CsvBindByPosition(position = 12)
        private String industrySectorsConcatString;
        @CsvBindByPosition(position = 13)
        private String industryGroupsConcatString;
        @CsvBindByPosition(position = 14)
        private String industryCodesConcatString;
    }
}
