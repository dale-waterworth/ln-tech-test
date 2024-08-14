package dale.lexisnexisapidemo.company.search.database;

import dale.lexisnexisapidemo.company.search.database.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
  Optional<CompanyEntity> findByCompanyNumber(String companyNumber);
}
