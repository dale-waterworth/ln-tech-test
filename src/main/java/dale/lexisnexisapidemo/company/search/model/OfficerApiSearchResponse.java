package dale.lexisnexisapidemo.company.search.model;
// OfficerApiSearchResponse

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

public record OfficerApiSearchResponse(
  @JsonProperty("etag") String etag,
  @JsonProperty("links") Links links,
  @JsonProperty("kind") String kind,
  @JsonProperty("items_per_page") int itemsPerPage,
  @JsonProperty("items") List<Officer> items,
  @JsonProperty("active_count") int activeCount,
  @JsonProperty("total_results") int totalResults,
  @JsonProperty("resigned_count") int resignedCount
) {
  public record Links(
    @JsonProperty("self") String self
  ) {}

  public record Officer(
    @JsonProperty("address") Address address,
    @JsonProperty("name") String name,
    @JsonProperty("appointed_on") String appointedOn,
    @JsonProperty("officer_role") String officerRole,
    @JsonProperty("links") OfficerLinks links,
    @JsonProperty("occupation") String occupation,
    @JsonProperty("nationality") String nationality,
    @JsonProperty("date_of_birth") DateOfBirth dateOfBirth,
    @JsonProperty("country_of_residence") String countryOfResidence,
    @JsonProperty("resigned_on") Optional<String> resignedOn
  ) {
    public record Address(
      @JsonProperty("premises") String premises,
      @JsonProperty("postal_code") String postalCode,
      @JsonProperty("country") String country,
      @JsonProperty("locality") String locality,
      @JsonProperty("address_line_1") String addressLine1,
      @JsonProperty("address_line_2") String addressLine2
    ) {}

    public record OfficerLinks(
      @JsonProperty("officer") OfficerAppointments officer
    ) {
      public record OfficerAppointments(
        @JsonProperty("appointments") String appointments
      ) {}
    }

    public record DateOfBirth(
      @JsonProperty("month") Integer month,
      @JsonProperty("year") Integer year
    ) {}
  }
}

