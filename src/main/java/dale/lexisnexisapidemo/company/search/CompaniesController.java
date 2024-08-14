package dale.lexisnexisapidemo.company.search;

import dale.lexisnexisapidemo.company.search.model.CompanyLookupRequest;
import dale.lexisnexisapidemo.company.search.model.CompanyOfficerLookupResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("api/v1/companies")
@AllArgsConstructor
public class CompaniesController {
  private final CompaniesService companiesService;

  @GetMapping("search")
  public ResponseEntity<CompanyOfficerLookupResponse> search(
    @RequestParam("showInactive") Optional<Boolean> showInactive,
    @RequestBody CompanyLookupRequest companyLookupRequest
  ) {
    return ResponseEntity.ok(
      companiesService.find(showInactive.orElse(false), companyLookupRequest)
    );
  }
}
