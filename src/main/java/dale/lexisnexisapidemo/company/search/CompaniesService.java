package dale.lexisnexisapidemo.company.search;

import dale.lexisnexisapidemo.company.search.database.CompanyRepository;
import dale.lexisnexisapidemo.company.search.database.entity.CompanyEntity;
import dale.lexisnexisapidemo.exception.CompanyOfficerSearchException;
import dale.lexisnexisapidemo.company.search.model.CompanyApiSearchResponse;
import dale.lexisnexisapidemo.company.search.model.CompanyLookupRequest;
import dale.lexisnexisapidemo.company.search.model.CompanyOfficerLookupResponse;
import dale.lexisnexisapidemo.company.search.model.OfficerApiSearchResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class CompaniesService {
  private final RestTemplate thirdPartyRestTemplate;
  private final CompanySearchFilterStrategy companySearchFilterStrategy;
  private final CompanyOfficerMapper companyOfficerMapper;
  private final CompanyRepository companyRepository;
  private final CompanyEntityMapper companyEntityMapper;

  public CompanyOfficerLookupResponse find(Boolean showInactiveCompanies, CompanyLookupRequest companyLookupRequest) {
    if(getCompanyItem(companyLookupRequest).isPresent()){
      return getCompanyItem(companyLookupRequest).get();
    }

    var companySearchResults = getCompanies(determineQuery(companyLookupRequest));

    var companies = companySearchFilterStrategy.find(
      companyLookupRequest,
      companySearchResults,
      showInactiveCompanies
    );

    var officers = getOfficersFor(companies);

    var companyOfficerList = companies.stream()
      .map(companyItem -> companyOfficerMapper.mapToCompanyItem(
        companyItem,
        officers.get(companyItem.companyNumber()))
      )
      .collect(Collectors.toList());

    saveCompanies(companyOfficerList);

    return new CompanyOfficerLookupResponse(
      companies.size(),
      companyOfficerList
    );
  }

  @Transactional
  protected Optional<CompanyOfficerLookupResponse> getCompanyItem(CompanyLookupRequest companyLookupRequest) {
    return companyLookupRequest.companyNumber()
      .flatMap(companyRepository::findByCompanyNumber)
      .map(companyEntityMapper::toRecord)
      .map(companyItem -> new CompanyOfficerLookupResponse(
        1, List.of(companyItem)
      ));
  }

  private void saveCompanies(List<CompanyOfficerLookupResponse.CompanyItem> companyOfficerList) {
    companyOfficerList.forEach(companyOfficerEntry -> {
      var companyEntity = companyRepository.findByCompanyNumber(companyOfficerEntry.companyNumber())
        .orElseGet(() -> companyEntityMapper.toEntity(companyOfficerEntry));

      companyRepository.save(companyEntity);
    });
  }

  private Map<String, List<OfficerApiSearchResponse.Officer>> getOfficersFor(List<CompanyApiSearchResponse.Company> companies) {
    return companies.stream()
      .collect(Collectors.toMap(
        CompanyApiSearchResponse.Company::companyNumber,
        company -> filterActiveOfficers(searchOfficers(company.companyNumber()))
      ));
  }

  private CompanyApiSearchResponse getCompanies(String query) {
    try {
      return thirdPartyRestTemplate.getForObject("/Companies/v1/Search?query=" + query, CompanyApiSearchResponse.class);
    } catch (Exception e) {
      throw new CompanyOfficerSearchException("An unknown error happened searching companies");
    }
  }

  private OfficerApiSearchResponse searchOfficers(String companyNumber) {
    try {
      return thirdPartyRestTemplate.getForObject("/Companies/v1/Officers?CompanyNumber=" + companyNumber, OfficerApiSearchResponse.class);
    } catch (Exception e) {
      throw new CompanyOfficerSearchException("An unknown error happened searching officers");
    }
  }

  private List<OfficerApiSearchResponse.Officer> filterActiveOfficers(OfficerApiSearchResponse response) {
    return Optional.ofNullable(response.items()) // there are some searches returning null here
      .orElse(Collections.emptyList())
      .stream()
      .filter(Objects::nonNull)
      .filter(officer -> officer.resignedOn().isEmpty())
      .toList();
  }

  private String determineQuery(CompanyLookupRequest companyLookupRequest) {
    return companyLookupRequest.companyNumber()
      .orElseGet(() -> companyLookupRequest.companyName()
        .orElseThrow(() -> new RuntimeException("No company info to search"))
      );
  }
}

