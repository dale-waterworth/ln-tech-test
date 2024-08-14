package dale.lexisnexisapidemo.company.search.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CompanyOfficerLookupResponse(
  @JsonProperty("total_results") int totalResults,
  @JsonProperty("items") List<CompanyItem> items
) {
  public record CompanyItem(
    @JsonProperty("company_number") String companyNumber,
    @JsonProperty("company_type") String companyType,
    @JsonProperty("title") String title,
    @JsonProperty("company_status") String companyStatus,
    @JsonProperty("date_of_creation") String dateOfCreation,
    @JsonProperty("address") Address address,
    @JsonProperty("officers") List<Officer> officers
  ) {
  }

  public record Address(
    @JsonProperty("locality") String locality,
    @JsonProperty("postal_code") String postalCode,
    @JsonProperty("premises") String premises,
    @JsonProperty("address_line_1") String addressLine1,
    @JsonProperty("country") String country
  ) {
  }

  public record Officer(
    @JsonProperty("name") String name,
    @JsonProperty("officer_role") String officerRole,
    @JsonProperty("appointed_on") String appointedOn,
    @JsonProperty("address") Address address
  ) {
  }
}
