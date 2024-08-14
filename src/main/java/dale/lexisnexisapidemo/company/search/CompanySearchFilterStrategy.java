package dale.lexisnexisapidemo.company.search;

import dale.lexisnexisapidemo.company.search.model.CompanyApiSearchResponse;
import dale.lexisnexisapidemo.company.search.model.CompanyLookupRequest;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Predicate;

import static dale.lexisnexisapidemo.company.search.CompanySearchFilterStrategy.filterInactive;

interface CompanyFilterStrategy {
  List<CompanyApiSearchResponse.Company> filterResults(CompanyApiSearchResponse searchResponse, CompanyLookupRequest query, Boolean showInactiveCompanies);
}

@Service
public class CompanySearchFilterStrategy {

  public static final String ACTIVE_FLAG = "active";

  public List<CompanyApiSearchResponse.Company> find(CompanyLookupRequest companyLookupRequest, CompanyApiSearchResponse companySearchResults, Boolean showInactiveCompanies) {
    CompanyOfficerLookupContext context = lookupContext(companyLookupRequest);
    return context.executeStrategy(companySearchResults, companyLookupRequest, showInactiveCompanies);
  }

  private CompanyOfficerLookupContext lookupContext(CompanyLookupRequest companyLookupRequest) {
    CompanyOfficerLookupContext context = new CompanyOfficerLookupContext();

    if (companyLookupRequest.companyNumber().isPresent()) {
      context.setStrategy(new CompanyNumberFilterStrategy());
    } else if (companyLookupRequest.companyName().isPresent()) {
      context.setStrategy(new CompanyNameFilterStrategy());
    }

    return context;
  }

  static Predicate<CompanyApiSearchResponse.Company> filterInactive(Boolean showInactiveCompanies) {
    return company -> showInactiveCompanies || company.companyStatus().equals(ACTIVE_FLAG);
  }
}

@Setter
class CompanyOfficerLookupContext {
  private CompanyFilterStrategy strategy;

  public List<CompanyApiSearchResponse.Company> executeStrategy(CompanyApiSearchResponse searchResponse, CompanyLookupRequest query, Boolean showInactiveCompanies) {
    return strategy.filterResults(searchResponse, query, showInactiveCompanies);
  }
}

class CompanyNameFilterStrategy implements CompanyFilterStrategy {
  @Override
  public List<CompanyApiSearchResponse.Company> filterResults(CompanyApiSearchResponse searchResponse, CompanyLookupRequest query, Boolean showInactiveCompanies) {
    return query.companyName()
      .map(name -> searchResponse.items().stream()
        .filter(filterInactive(showInactiveCompanies))
        .filter(company -> company.title().toLowerCase().contains(name.toLowerCase()))
        .toList()
      )
      .orElseThrow(() -> new RuntimeException("Company not found"));
  }
}

class CompanyNumberFilterStrategy implements CompanyFilterStrategy {
  @Override
  public List<CompanyApiSearchResponse.Company> filterResults(CompanyApiSearchResponse searchResponse, CompanyLookupRequest query, Boolean showInactiveCompanies) {
    return query.companyNumber()
      .map(number -> searchResponse.items().stream()
        .filter(filterInactive(showInactiveCompanies))
        .filter(company -> company.companyNumber().equals(number))
        .toList()
      )
      .orElseThrow(() -> new RuntimeException("Company number not found"));
  }
}