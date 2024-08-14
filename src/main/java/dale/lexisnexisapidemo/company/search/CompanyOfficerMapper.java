package dale.lexisnexisapidemo.company.search;
import dale.lexisnexisapidemo.company.search.model.CompanyApiSearchResponse;
import dale.lexisnexisapidemo.company.search.model.CompanyOfficerLookupResponse;
import dale.lexisnexisapidemo.company.search.model.OfficerApiSearchResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompanyOfficerMapper {

  CompanyOfficerLookupResponse.CompanyItem mapToCompanyItem(
    CompanyApiSearchResponse.Company company,
    List<OfficerApiSearchResponse.Officer> officers
  );

}
