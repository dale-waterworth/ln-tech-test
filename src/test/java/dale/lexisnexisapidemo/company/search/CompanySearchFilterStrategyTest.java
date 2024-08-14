package dale.lexisnexisapidemo.company.search;

import dale.lexisnexisapidemo.company.search.model.CompanyLookupRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CompanySearchFilterStrategyTest {

  private final CompanySearchFilterStrategy strategy = new CompanySearchFilterStrategy();

  @Test
  void shouldFilterByNameAndReturnActive() {
    var testCompanyList = List.of(
      new CompanyTestFixtures.TestCompanyCriteria("company 1", "0001", "active"),
      new CompanyTestFixtures.TestCompanyCriteria("company 2", "0002", "active"),
      new CompanyTestFixtures.TestCompanyCriteria("company 3", "0003", "active"),
      new CompanyTestFixtures.TestCompanyCriteria("company 4", "0004", "other"),
      new CompanyTestFixtures.TestCompanyCriteria("no match", "0005", "active")
    );
    var companySearchResults = CompanyTestFixtures.getTestCompanyData(
      CompanyTestFixtures.getTestCompanies(testCompanyList)
    );
    var response = strategy.find(
      new CompanyLookupRequest(Optional.of("coMpaNy"), Optional.empty()),
      companySearchResults,
      false
    );

    assertEquals(3, response.size());
    List.of("company 1", "company 2", "company 3")
      .forEach(companyName -> assertTrue(response.stream()
        .anyMatch(company -> company.title().equalsIgnoreCase(companyName))));
  }

  @Test
  void shouldFilterByCompanyNumberAndReturnActive() {
    var testCompanyList = List.of(
      new CompanyTestFixtures.TestCompanyCriteria("company 1", "0001", "active"),
      new CompanyTestFixtures.TestCompanyCriteria("company 4", "0005", "other"),
      new CompanyTestFixtures.TestCompanyCriteria("no title match", "0005", "active")
    );
    var companySearchResults = CompanyTestFixtures.getTestCompanyData(
      CompanyTestFixtures.getTestCompanies(testCompanyList)
    );

    // act
    var response = strategy.find(
      new CompanyLookupRequest(Optional.empty(), Optional.of("0005")),
      companySearchResults,
      false
    );

    assertEquals(1, response.size());
    assertEquals("no title match", response.get(0).title());
    assertEquals("0005", response.get(0).companyNumber());
  }

  @Test
  void shouldFilterByCompanyNumberWhenBothRequestFieldsExistAndReturnInactive() {
    var testCompanyList = List.of(
      new CompanyTestFixtures.TestCompanyCriteria("company 1", "0001", "active"),
      new CompanyTestFixtures.TestCompanyCriteria("company 4", "0004", "other"),
      new CompanyTestFixtures.TestCompanyCriteria("no title match", "0005", "active")
    );
    var companySearchResults = CompanyTestFixtures.getTestCompanyData(
      CompanyTestFixtures.getTestCompanies(testCompanyList)
    );

    // act
    var response = strategy.find(
      new CompanyLookupRequest(Optional.of("coMpaNy"), Optional.of("0005")),
      companySearchResults,
      true
    );

    assertEquals(1, response.size());
    assertEquals("no title match", response.get(0).title());
    assertEquals("0005", response.get(0).companyNumber());
  }
}