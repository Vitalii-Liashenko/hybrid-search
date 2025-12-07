package com.liashenko.v.hybrid.search.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

import java.time.LocalDate;
import java.util.List;

@With
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Conference {
    public static final String ID_FIELD = "id";
    public static final String NAME_FIELD = "name";
    public static final String EMBEDDING_FIELD = "embedding";

    public static final String GROUP_ID_FIELD = "groupId";

    public static final String DESCRIPTION_FIELD = "description";
    public static final String START_DATE_FIELD = "startDate";
    public static final String END_DATE_FIELD = "endDate";
    public static final String FORMATTED_LOCATION_FIELD = "formattedLocation";
    public static final String COUNTRY_DESCRIPTION_FIELD = "countryDescription";
    public static final String ATTENDEES_COUNT_FIELD = "attendeesCount";
    public static final String COMPANY_ATTENDEES_COUNT_FIELD = "companyAttendeesCount";
    public static final String INVESTOR_ATTENDEES_COUNT_FIELD = "investorAttendeesCount";
    public static final String ATTENDEES_CONCAT_STRING_FIELD = "attendeeNamesConcatString";
    public static final String INDUSTRY_SECTORS_CONCAT_STRING_FIELD = "industrySectorsConcatString";
    public static final String INDUSTRY_GROUPS_CONCAT_STRING_FIELD = "industryGroupsConcatString";
    public static final String INDUSTRY_CODES_CONCAT_STRING_FIELD = "industryCodesConcatString";

    @JsonProperty(ID_FIELD)
    private String id;
    @JsonProperty(NAME_FIELD)
    private String name;
    @JsonProperty(EMBEDDING_FIELD)
    private List<Float> embedding;
    @JsonProperty(GROUP_ID_FIELD)
    private Integer groupId;
    @JsonProperty(DESCRIPTION_FIELD)
    private String description;
    @JsonProperty(START_DATE_FIELD)
    private LocalDate startDate;
    @JsonProperty(END_DATE_FIELD)
    private LocalDate endDate;
    @JsonProperty(FORMATTED_LOCATION_FIELD)
    private String formattedLocation;
    @JsonProperty(COUNTRY_DESCRIPTION_FIELD)
    private String countryDescription;
    @JsonProperty(ATTENDEES_COUNT_FIELD)
    private Integer attendeesCount;
    @JsonProperty(COMPANY_ATTENDEES_COUNT_FIELD)
    private Integer companyAttendeesCount;
    @JsonProperty(INVESTOR_ATTENDEES_COUNT_FIELD)
    private Integer investorAttendeesCount;
    @JsonProperty(ATTENDEES_CONCAT_STRING_FIELD)
    private String attendeeNamesConcatString;
    @JsonProperty(INDUSTRY_SECTORS_CONCAT_STRING_FIELD)
    private String industrySectorsConcatString;
    @JsonProperty(INDUSTRY_GROUPS_CONCAT_STRING_FIELD)
    private String industryGroupsConcatString;
    @JsonProperty(INDUSTRY_CODES_CONCAT_STRING_FIELD)
    private String industryCodesConcatString;
    private Double score;
}
