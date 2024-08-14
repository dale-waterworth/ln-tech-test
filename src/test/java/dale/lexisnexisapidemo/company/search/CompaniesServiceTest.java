package dale.lexisnexisapidemo.company.search;

import dale.lexisnexisapidemo.company.search.database.CompanyRepository;
import dale.lexisnexisapidemo.company.search.model.CompanyApiSearchResponse;
import dale.lexisnexisapidemo.company.search.model.CompanyLookupRequest;
import dale.lexisnexisapidemo.company.search.model.OfficerApiSearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompaniesServiceTest {
  private final RestTemplate thirdPartyRestTemplate = mock(RestTemplate.class);
  private final CompanyRepository companyRepository = mock(CompanyRepository.class);
  private final CompanySearchFilterStrategy companySearchFilterStrategy = mock(CompanySearchFilterStrategy.class);
  private CompaniesService companiesService;

  @BeforeEach
  public void setUp() {
    var companyOfficerMapper = Mappers.getMapper(CompanyOfficerMapper.class);
    var companyEntityMapper = Mappers.getMapper(CompanyEntityMapper.class);
    companiesService = new CompaniesService(
      thirdPartyRestTemplate,
      companySearchFilterStrategy,
      companyOfficerMapper,
      companyRepository,
      companyEntityMapper
    );
  }

  @Test
  void shouldThrowErrorIfNoCompanyLookupRequest() {
    RuntimeException exception = assertThrows(RuntimeException.class,
      () -> companiesService.find(false, new CompanyLookupRequest(Optional.empty(), Optional.empty())));

    assertEquals("No company info to search", exception.getMessage());
  }

  @Test
  void shouldHandleNoMatches() {
    // arrange
    var expectedQuery = "coMpaNy";
    when(thirdPartyRestTemplate.getForObject(
        "/Companies/v1/Search?query=" + expectedQuery,
        CompanyApiSearchResponse.class
      )
    )
      .thenReturn(CompanyTestFixtures.getTestCompanyData(CompanyTestFixtures.getTestCompanies(List.of())));

    // act
    var response = companiesService.find(false, new CompanyLookupRequest(Optional.of(expectedQuery), Optional.empty()));

    // assert
    assertEquals(0, response.totalResults());

    verify(thirdPartyRestTemplate, times(1)).getForObject(
      eq("/Companies/v1/Search?query=" + expectedQuery),
      eq(CompanyApiSearchResponse.class)
    );

    verify(thirdPartyRestTemplate, times(0)).getForObject(
      contains("/Companies/v1/Officers?CompanyNumber"),
      eq(OfficerApiSearchResponse.class)
    );

  }

  @Test
  void shouldCallOfficerApiForEachMatch() {
    // arrange
    var expectedQuery = "coMpaNy";
    var showInactiveCompanies = false;
    var companyLookupRequest = new CompanyLookupRequest(Optional.of(expectedQuery), Optional.empty());
    var testCompanyList = List.of(
      new CompanyTestFixtures.TestCompanyCriteria("company 1", "0001", "active"),
      new CompanyTestFixtures.TestCompanyCriteria("company 2", "0002", "active"),
      new CompanyTestFixtures.TestCompanyCriteria("company 3", "0003", "active")
    );
    var companySearchResults = CompanyTestFixtures.getTestCompanyData(
      CompanyTestFixtures.getTestCompanies(testCompanyList)
    );

    when(thirdPartyRestTemplate.getForObject(
      "/Companies/v1/Search?query=" + expectedQuery,
      CompanyApiSearchResponse.class
    ))
      .thenReturn(companySearchResults);

    when(companySearchFilterStrategy.find(
      companyLookupRequest,
      companySearchResults,
      showInactiveCompanies
    ))
      .thenReturn(companySearchResults.items());

    testCompanyList.forEach(testCompany -> when(thirdPartyRestTemplate.getForObject(
      "/Companies/v1/Officers?CompanyNumber=" + testCompany.companyNumber(),
      OfficerApiSearchResponse.class
    ))
      .thenReturn(
        CompanyTestFixtures.getOfficerData(
          CompanyTestFixtures.getOfficers(List.of(
            new CompanyTestFixtures.TestOfficerCriteria(testCompany.companyNumber(), Optional.empty())
          ))
        )
      ));

    // act
    var response = companiesService.find(showInactiveCompanies, companyLookupRequest);

    // assert
    assertEquals(3, response.totalResults());

    verify(thirdPartyRestTemplate, times(1)).getForObject(
      eq("/Companies/v1/Search?query=" + expectedQuery),
      eq(CompanyApiSearchResponse.class)
    );

    IntStream.range(0, testCompanyList.size()).forEach(index -> {
      var testCompany = testCompanyList.get(index);

      verify(thirdPartyRestTemplate, times(1)).getForObject(
        contains("/Companies/v1/Officers?CompanyNumber=" + testCompany.companyNumber()),
        eq(OfficerApiSearchResponse.class)
      );

      assertEquals(testCompany.companyNumber(), response.items().get(index).officers().get(0).name());
    });
  }

  @Test
  void shouldNotReturnResignedOfficers() {
    // arrange
    var expectedQuery = "coMpaNy";
    var companyNumber = "0001";
    var showInactiveCompanies = false;
    var companyLookupRequest = new CompanyLookupRequest(Optional.of(expectedQuery), Optional.empty());
    var testCompanyList = List.of(
      new CompanyTestFixtures.TestCompanyCriteria("company 1", companyNumber, "active")
    );
    var companySearchResults = CompanyTestFixtures.getTestCompanyData(
      CompanyTestFixtures.getTestCompanies(testCompanyList)
    );

    when(thirdPartyRestTemplate.getForObject(
      "/Companies/v1/Search?query=" + expectedQuery,
      CompanyApiSearchResponse.class
    ))
      .thenReturn(companySearchResults);

    when(companySearchFilterStrategy.find(
      companyLookupRequest,
      companySearchResults,
      showInactiveCompanies
    ))
      .thenReturn(companySearchResults.items());

    when(thirdPartyRestTemplate.getForObject(
      "/Companies/v1/Officers?CompanyNumber=" + companyNumber,
      OfficerApiSearchResponse.class
    ))
      .thenReturn(
        CompanyTestFixtures.getOfficerData(
          CompanyTestFixtures.getOfficers(List.of(
              new CompanyTestFixtures.TestOfficerCriteria("resigned", Optional.of("2022-01-01")),
              new CompanyTestFixtures.TestOfficerCriteria("valid", Optional.empty())
            )
          )
        ));

    // act
    var response = companiesService.find(showInactiveCompanies, companyLookupRequest);

    // assert
    assertEquals(1, response.totalResults());

    verify(thirdPartyRestTemplate, times(1)).getForObject(
      eq("/Companies/v1/Search?query=" + expectedQuery),
      eq(CompanyApiSearchResponse.class)
    );

    var testCompany = testCompanyList.get(0);

    verify(thirdPartyRestTemplate, times(1)).getForObject(
      contains("/Companies/v1/Officers?CompanyNumber=" + testCompany.companyNumber()),
      eq(OfficerApiSearchResponse.class)
    );

    assertEquals(1, response.items().get(0).officers().size());
    assertEquals("valid", response.items().get(0).officers().get(0).name());
  }
}


