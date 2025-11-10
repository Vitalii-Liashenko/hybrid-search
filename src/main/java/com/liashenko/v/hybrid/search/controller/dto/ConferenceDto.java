package com.liashenko.v.hybrid.search.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConferenceDto {

    private String id;
    private String name;
    private Integer groupId;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String formattedLocation;
    private String countryDescription;
    private Integer attendeesCount;
    private Integer companyAttendeesCount;
    private Integer investorAttendeesCount;
    private String attendeeNamesConcatString;
    private String industrySectorsConcatString;
    private String industryGroupsConcatString;
    private String industryCodesConcatString;
    private Double score;
}
