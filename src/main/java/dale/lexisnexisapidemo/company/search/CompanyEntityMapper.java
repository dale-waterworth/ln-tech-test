package dale.lexisnexisapidemo.company.search;
import dale.lexisnexisapidemo.company.search.database.entity.CompanyEntity;
import dale.lexisnexisapidemo.company.search.model.CompanyOfficerLookupResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CompanyEntityMapper {

 // Mapping CompanyItem entity to CompanyItem record
  @Mapping(source = "companyNumber", target = "companyNumber")
  @Mapping(source = "companyType", target = "companyType")
  @Mapping(source = "title", target = "title")
  @Mapping(source = "companyStatus", target = "companyStatus")
  @Mapping(source = "dateOfCreation", target = "dateOfCreation")
  @Mapping(source = "address", target = "address")
  @Mapping(source = "officers", target = "officers")
  CompanyOfficerLookupResponse.CompanyItem  toRecord(CompanyEntity companyItem);


  // Mapping back from record to entity (optional if needed)
  @Mapping(source = "companyNumber", target = "companyNumber")
  @Mapping(source = "companyType", target = "companyType")
  @Mapping(source = "title", target = "title")
  @Mapping(source = "companyStatus", target = "companyStatus")
  @Mapping(source = "dateOfCreation", target = "dateOfCreation")
  @Mapping(source = "address", target = "address")
  @Mapping(source = "officers", target = "officers")
  CompanyEntity toEntity(CompanyOfficerLookupResponse.CompanyItem company);

}
