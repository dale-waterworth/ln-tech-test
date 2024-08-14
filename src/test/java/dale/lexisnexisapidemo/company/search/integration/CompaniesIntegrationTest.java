package dale.lexisnexisapidemo.company.search.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import dale.lexisnexisapidemo.company.search.CompaniesController;
import dale.lexisnexisapidemo.company.search.model.CompanyLookupRequest;
import dale.lexisnexisapidemo.company.search.model.CompanyOfficerLookupResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional // This annotation rolls back the transaction after each test
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // Use H2 for tests
public class CompaniesIntegrationTest {
  @Autowired
  private CompaniesController companiesController;

  private WireMockServer wireMockServer;

  @Value("${wiremock.server.port}")
  private int wireMockPort;

  @BeforeEach
  void setup() {
    wireMockServer = new WireMockServer(wireMockPort);
    wireMockServer.start();
    WireMock.configureFor("localhost", wireMockPort);
  }

  @AfterEach
  void tearDown() {
    wireMockServer.stop();
  }

  public static Stream<Arguments> testCompanyLookupSearchRequest() {
    return Stream.of(
      Arguments.of("BBC LIMITED", "06500244", new CompanyLookupRequest(Optional.of("BBC LIMITED"), Optional.empty())),
      Arguments.of("06500244", "06500244", new CompanyLookupRequest(Optional.empty(), Optional.of("06500244"))),
      Arguments.of("06500244", "06500244", new CompanyLookupRequest(Optional.of("BBC LIMITED"), Optional.of("06500244")))
    );
  }
  @ParameterizedTest
  @MethodSource("testCompanyLookupSearchRequest")
  void testWithCompanyNameValid(String searchQuery, String companyNumber, CompanyLookupRequest companyLookupRequest) {
    // Arrange
    stubFor(
      get(
        urlPathMatching("/Companies/v1/Search")
      )
        .withQueryParam("query", WireMock.equalTo(searchQuery))
        .willReturn(aResponse()
          .withHeader("Content-Type", "application/json")
          .withBodyFile("companies_search_BBC.json")
        )
    );
    stubFor(
      get(
        urlPathMatching("/Companies/v1/Officers")
      )
        .withQueryParam("CompanyNumber", WireMock.equalTo(companyNumber))
        .willReturn(aResponse()
          .withHeader("Content-Type", "application/json")
          .withBodyFile("companies_officers_companyNumber_06500244.json")
        )
    );

    // Act
    CompanyOfficerLookupResponse companyApiSearchResponse = companiesController.search(
      Optional.of(false),
      companyLookupRequest
    ).getBody();

    // Assert
    verify(
      getRequestedFor(
        urlPathMatching("/Companies/v1/Search")
      )
        .withQueryParam("query", equalTo(searchQuery))
    );
    verify(
      getRequestedFor(
        urlPathMatching("/Companies/v1/Officers")
      )
        .withQueryParam("CompanyNumber", equalTo(companyNumber))
    );
    assertNotNull(companyApiSearchResponse);
    assertEquals(1, companyApiSearchResponse.items().size());
    assertEquals(2, companyApiSearchResponse.items().get(0).officers().size());
  }

  @Test
  void itShouldHandleCompanySearchError500(){
    var searchQuery = "BBC LIMITED";
    stubFor(
      get(
        urlPathMatching("/Companies/v1/Search")
      )
        .withQueryParam("query", WireMock.equalTo(searchQuery))
        .willReturn(aResponse()
          .withStatus(500)
          .withBody("{\"error\": \"Internal Server Error\"}")
        )
    );

    Exception thrown = assertThrows(
      RuntimeException.class,
      () ->companiesController.search(
        Optional.of(false),
        new CompanyLookupRequest(Optional.of("BBC LIMITED"), Optional.empty())
      ).getBody(),
      "Expected RuntimeException to be called but did not."
    );

    assertEquals("An unknown error happened searching companies", thrown.getMessage());

  }

  @Test
  void itShouldHandleOfficerSearchError500(){
    var searchQuery = "BBC LIMITED";
    String companyNumber = "06500244";
    stubFor(
      get(
        urlPathMatching("/Companies/v1/Search")
      )
        .withQueryParam("query", WireMock.equalTo(searchQuery))
        .willReturn(aResponse()
          .withHeader("Content-Type", "application/json")
          .withBodyFile("companies_search_BBC.json")
        )
    );

    stubFor(
      get(
        urlPathMatching("/Companies/v1/Officers")
      )
        .withQueryParam("CompanyNumber", WireMock.equalTo(companyNumber))
        .willReturn(aResponse()
          .withStatus(500)
          .withBody("{\"error\": \"Internal Server Error\"}")
        )
    );

    Exception thrown = assertThrows(
      RuntimeException.class,
      () ->companiesController.search(
        Optional.of(false),
        new CompanyLookupRequest(Optional.of("BBC LIMITED"), Optional.empty())
      ).getBody(),
      "Expected RuntimeException to be called but did not."
    );

    assertEquals("An unknown error happened searching officers", thrown.getMessage());

  }

  @Test
  void itShouldNotCallApiTwiceButGetCachedFromDB() {
    // Arrange
    String companyNumber = "06500244";
    stubFor(
      get(
        urlPathMatching("/Companies/v1/Search")
      )
        .withQueryParam("query", WireMock.equalTo(companyNumber))
        .willReturn(aResponse()
          .withHeader("Content-Type", "application/json")
          .withBodyFile("companies_search_BBC.json")
        )
    );
    stubFor(
      get(
        urlPathMatching("/Companies/v1/Officers")
      )
        .withQueryParam("CompanyNumber", WireMock.equalTo(companyNumber))
        .willReturn(aResponse()
          .withHeader("Content-Type", "application/json")
          .withBodyFile("companies_officers_companyNumber_06500244.json")
        )
    );

    // Act
    CompanyOfficerLookupResponse companyApiSearchResponse = companiesController.search(
      Optional.of(false),
      new CompanyLookupRequest(Optional.empty(), Optional.of("06500244"))
    ).getBody();

    CompanyOfficerLookupResponse companyApiSearchResponse2 = companiesController.search(
      Optional.of(false),
      new CompanyLookupRequest(Optional.empty(), Optional.of("06500244"))
    ).getBody();

    // Assert
    verify(1,
      getRequestedFor(
        urlPathMatching("/Companies/v1/Search")
      )
        .withQueryParam("query", equalTo(companyNumber))
    );
    verify(1,
      getRequestedFor(
        urlPathMatching("/Companies/v1/Officers")
      )
        .withQueryParam("CompanyNumber", equalTo(companyNumber))
    );
    assertNotNull(companyApiSearchResponse);
    assertEquals(1, companyApiSearchResponse.items().size());
    assertEquals(2, companyApiSearchResponse.items().get(0).officers().size());
    assertEquals(companyApiSearchResponse, companyApiSearchResponse2);
  }
}
