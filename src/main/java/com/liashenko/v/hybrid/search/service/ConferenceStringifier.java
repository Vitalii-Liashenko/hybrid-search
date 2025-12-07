package com.liashenko.v.hybrid.search.service;

import com.liashenko.v.hybrid.search.model.Conference;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConferenceStringifier {

    public static String getInput(Conference conf) {
        return """
                name: %s
                location: %s
                description: %s
                country: %s
                industries: %s, %s, %s,
                attendingCompanies: %s
                """.formatted(
                conf.getName(),
                conf.getFormattedLocation(),
                conf.getDescription(),
                conf.getCountryDescription(),
                conf.getIndustryCodesConcatString(),
                conf.getIndustrySectorsConcatString(),
                conf.getIndustryGroupsConcatString(),
                conf.getAttendeeNamesConcatString()
        );
    }

    public static String getShortInput(Conference conf) {
        return """
                name: %s
                location: %s
                country: %s
                industries: %s, %s, %s
                """.formatted(
                conf.getName(),
                conf.getFormattedLocation(),
                conf.getCountryDescription(),
                conf.getIndustryCodesConcatString(),
                conf.getIndustrySectorsConcatString(),
                conf.getIndustryGroupsConcatString()
        );
    }
}
