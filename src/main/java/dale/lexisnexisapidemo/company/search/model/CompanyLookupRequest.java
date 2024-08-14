package dale.lexisnexisapidemo.company.search.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public record CompanyLookupRequest(
  @JsonProperty("companyName")
  Optional<String> companyName,
  @JsonProperty("companyNumber")
  Optional<String> companyNumber
) {
}
