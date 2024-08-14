package dale.lexisnexisapidemo.company.search.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CompanyApiSearchResponse(
  @JsonProperty("page_number") int pageNumber,
  @JsonProperty("kind") String kind,
  @JsonProperty("total_results") int totalResults,
  @JsonProperty("items") List<Company> items
) {
  public record Company(
    @JsonProperty("company_status") String companyStatus,
    @JsonProperty("address_snippet") String addressSnippet,
    @JsonProperty("date_of_creation") String dateOfCreation,
    @JsonProperty("matches") Map<String, List<Integer>> matches,
    @JsonProperty("description") String description,
    @JsonProperty("links") Links links,
    @JsonProperty("company_number") String companyNumber,
    @JsonProperty("title") String title,
    @JsonProperty("company_type") String companyType,
    @JsonProperty("address") Address address,
    @JsonProperty("kind") String kind,
    @JsonProperty("description_identifier") List<String> descriptionIdentifier,
    @JsonProperty("date_of_cessation") String dateOfCessation
  ) {
    public record Links(
      @JsonProperty("self") String self
    ) {
    }

    public record Address(
      @JsonProperty("premises") String premises,
      @JsonProperty("postal_code") String postalCode,
      @JsonProperty("country") String country,
      @JsonProperty("locality") String locality,
      @JsonProperty("region") String region,
      @JsonProperty("address_line_1") String addressLine1,
      @JsonProperty("address_line_2") String addressLine2
    ) {
    }
  }
}

