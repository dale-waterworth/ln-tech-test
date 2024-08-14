## Goal
Create a company search application using Spring Boot 3.1.3 or higher.

Expose an endpoint that uses the `TruProxyAPI` to do a company and officer lookup 
via name or registration number.

## Criteria
* The result of the search is returned as JSON
* A request parameter has to be added to decide whether only active companies should be returned
* The officers of each company have to be included in the company details (new field `officers`) 
* Only include officers that are active (`resigned_on` is not present in that case)
* Paging can be ignored
* Please add unit tests and integrations tests, e.g. using WireMock to mock `TruProxyAPI` calls

**Expected Request**

* The name and registration/company number are passed in via body
* The API key is passed in via header `x-api-key`
* If both fields are provided `companyNumber` is used

<pre>
{
    "companyName" : "BBC LIMITED",
    "companyNumber" : "06500244"
}
</pre>

**Expected Response**

* Not all fields that are returned from `TruProxyAPI` are required.
The final JSON should look like this :

<pre>

{
    "total_results": 1,
    "items": [
        {
            "company_number": "06500244",
            "company_type": "ltd",
            "title": "BBC LIMITED",
            "company_status": "active",
            "date_of_creation": "2008-02-11",
            "address": {
                "locality": "Retford",
                "postal_code": "DN22 0AD",
                "premises": "Boswell Cottage Main Street",
                "address_line_1": "North Leverton",
                "country": "England"
            },
            "officers": [
                {
                    "name": "BOXALL, Sarah Victoria",
                    "officer_role": "secretary",
                    "appointed_on": "2008-02-11",
                    "address": {
                        "premises": "5",
                        "locality": "London",
                        "address_line_1": "Cranford Close",
                        "country": "England",
                        "postal_code": "SW20 0DP"
                    }
                }
            ]
        }
    ]
}
</pre>

## Bonus
* Save the companies (by `company_number`) and its officers and addresses in a database 
and return the result from there if the endpoint is called with `companyNumber`.

 
## Example API Requests

**Search for Company:**  
`https://exercise.trunarrative.cloud/TruProxyAPI/rest/Companies/v1/Search?Query={search_term}`

<details>
  <summary>Response Example</summary>

  <pre>
  {
    "page_number": 1,
    "kind": "search#companies",
    "total_results": 20,
    "items": [
        {
            "company_status": "active",
            "address_snippet": "Boswell Cottage Main Street, North Leverton, Retford, England, DN22 0AD",
            "date_of_creation": "2008-02-11",
            "matches": {
                "title": [
                    1,
                    3
                ]
            },
            "description": "06500244 - Incorporated on 11 February 2008",
            "links": {
                "self": "/company/06500244"
            },
            "company_number": "06500244",
            "title": "BBC LIMITED",
            "company_type": "ltd",
            "address": {
                "premises": "Boswell Cottage Main Street",
                "postal_code": "DN22 0AD",
                "country": "England",
                "locality": "Retford",
                "address_line_1": "North Leverton"
            },
            "kind": "searchresults#company",
            "description_identifier": [
                "incorporated-on"
            ]
        }]
  }
  </pre>
</details>

**Get Company Officers:**  
`https://exercise.trunarrative.cloud/TruProxyAPI/rest/Companies/v1/Officers?CompanyNumber={number}`
<details>
  <summary>Response Example</summary>

  <pre>
  {
    "etag": "6dd2261e61776d79c2c50685145fac364e75e24e",
    "links": {
        "self": "/company/10241297/officers"
    },
    "kind": "officer-list",
    "items_per_page": 35,
    "items": [
        {
            "address": {
                "premises": "The Leeming Building",
                "postal_code": "LS2 7JF",
                "country": "England",
                "locality": "Leeds",
                "address_line_1": "Vicar Lane"
            },
            "name": "ANTLES, Kerri",
            "appointed_on": "2017-04-01",
            "resigned_on": "2018-02-12",
            "officer_role": "director",
            "links": {
                "officer": {
                    "appointments": "/officers/4R8_9bZ44w0_cRlrxoC-wRwaMiE/appointments"
                }
            },
            "date_of_birth": {
                "month": 6,
                "year": 1969
            },
            "occupation": "Finance And Accounting",
            "country_of_residence": "United States",
            "nationality": "American"
        }]
  }
  </pre>
</details>

## API documentation

**Authentication:**\
Use the API key provided in your request header when calling the endpoints. <br>
Example: curl -s -H 'x-api-key: xxxxxxxxxxxxx' "https://exercise.trunarrative.cloud/TruProxyAPI/rest/Companies/v1/Officers?CompanyNumber=10241297"<br>

*API credentials will be provided seperately*

## Do not check the API Key into the repository!

## Flow

![Wireframe](https://raw.githubusercontent.com/TruNarrative/spring-exercise/main/spring_exercise.png)



---

# Solution Overview

To run the project execute:

`./gradlew bootRun`

Run unit and integration tests:

`./gradlew test --info`

A new endpoint was created `/api/v1/companies/search?showInactive=false` which also takes body json request:
```
{
    "companyName": "some very new ltd",
    "companyNumber": "08181763"
}
```


A postman collection has been exported and can be found at `/LexisNexisTest.postman_collection.json` Simply add the x-api-key and it will.


## Approach

The `TruProxyAPI` API was called to get the structure of the data and the models were created from this: `java/dale/lexisnexisapidemo/model`

Next an integration test was created. WireMock was added and configured to return sample data that matches the api calls found in `src/test/resources/__files`

`ParameterizedTest` was used to test the combinations of options that can be passed into to the search eg. name and or company number

A new controller was added (as described above) and the options and passed down the service layer.

The `CompaniesService` utilises the Strategy pattern (`CompanySearchFilterStrategy`) to filter the data based on the search criteria also allowing easier extension.

It was assumed that the search will return multiple entries for name but a single search for company number.

For each company search result there will be a call to get the officers and then to filter them.

Finally, the company and officers and combined to match the sample response using the flexible library `org.mapstruct.Mapper`

The file are combined into the module package pattern but have also been placed in controller, service, models directories.

There would still be tests outstanding to cover all edge cases but this should hopefully demonstrate the kind test i would do.

There is little exception / error testing but i would usually use spring `@ControllerAdvice` to hide sensitive details. As it was my first time using Wiremock i couldn't get that to work properly so i threw a custom `CompanyOfficerSearchException` for now.

Sample tests:

CompaniesServiceTest
- shouldCallOfficerApiForEachMatch
- shouldThrowErrorIfNoCompanyLookupRequest
- shouldNotReturnResignedOfficers
- shouldHandleNoMatches

 
CompanySearchFilterStrategyTest
- shouldFilterByCompanyNumberWhenBothRequestFieldsExistAndReturnInactive
- shouldFilterByNameAndReturnActive
- shouldFilterByCompanyNumberAndReturnActive

CompaniesIntegrationTest
- itShouldHandleOfficerSearchError500
- testWithCompanyNameValid
- itShouldHandleCompanySearchError500

### Bonus task

A H2 DB was added for this feature and an integration (`itShouldNotCallApiTwiceButGetCachedFromDB`).

The test is to call the endpoint twice and the API call should only be called once as it should retrieve the entry second time around from the DB.

The both responses are compared and pass if they are the same.

JPA entities were used to persist the data and created another mapper convert between each.

Usually flyway or something like would be used to create the DB, tables, relationships etc.

Done :)

Note: If trying this out simply add the key to the `api.key` field in the `application.properties` file