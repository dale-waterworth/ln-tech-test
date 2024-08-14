package dale.lexisnexisapidemo.company.search;

import dale.lexisnexisapidemo.company.search.model.CompanyApiSearchResponse;
import dale.lexisnexisapidemo.company.search.model.OfficerApiSearchResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class CompanyTestFixtures {

  public static CompanyApiSearchResponse getTestCompanyData(List<CompanyApiSearchResponse.Company> testCompanyCriteriaList) {
    return new CompanyApiSearchResponse(
      1,
      "company",
      testCompanyCriteriaList.size(),
      testCompanyCriteriaList
    );
  }

  public static List<CompanyApiSearchResponse.Company> getTestCompanies(List<TestCompanyCriteria> testCompanyCriteriaList) {
    return testCompanyCriteriaList.stream()
      .map(testCompanyCriteria -> new CompanyApiSearchResponse.Company(
        testCompanyCriteria.status(),
        "123 Test St, Testville",
        "2000-01-01",
        new HashMap<>(),
        "A Test company description",
        new CompanyApiSearchResponse.Company.Links(
          "https://Testcompany.com/self"
        ),
        testCompanyCriteria.companyNumber(),
        testCompanyCriteria.name(),
        "ltd",
        new CompanyApiSearchResponse.Company.Address(
          "Test Premises",
          "12345",
          "Testland",
          "Testville",
          "Test Region",
          "123 Test St",
          "Apt 101"
        ),
        "company",
        List.of(),
        null
      ))
      .toList();
  }

  public record TestCompanyCriteria(
    String name,
    String companyNumber,
    String status
  ) {
  }

  public record TestOfficerCriteria(
    String name,
    Optional<String> resignedOn
  ) {
  }

  public static List<OfficerApiSearchResponse.Officer> getOfficers(List<TestOfficerCriteria> testOfficerCriteriaList) {
    return testOfficerCriteriaList.stream()
      .map(testOfficerCriteria -> new OfficerApiSearchResponse.Officer(
        new OfficerApiSearchResponse.Officer.Address(
          "Test-premises",
          "12345",
          "Testland",
          "Testville",
          "123 Test St",
          "Apt 101"
        ),
        testOfficerCriteria.name(),
        "2020-01-01",
        "director",
        new OfficerApiSearchResponse.Officer.OfficerLinks(
          new OfficerApiSearchResponse.Officer.OfficerLinks.OfficerAppointments("Test-appointments-link")
        ),
        "Software Engineer",
        "American",
        new OfficerApiSearchResponse.Officer.DateOfBirth(1, 1980),
        "USA",
        testOfficerCriteria.resignedOn()
      ))
      .toList();
  }

  public static OfficerApiSearchResponse getOfficerData(List<OfficerApiSearchResponse.Officer> officers) {
    return new OfficerApiSearchResponse(
      "Test-etag",
      new OfficerApiSearchResponse.Links("Test-self-link"),
      "officer",
      10,
      officers,
      1,
      1,
      0
    );
  }
}
