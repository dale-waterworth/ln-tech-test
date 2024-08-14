package dale.lexisnexisapidemo.company.search.database.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CompanyEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "company_number")
  private String companyNumber;

  @Column(name = "company_type")
  private String companyType;

  private String title;

  @Column(name = "company_status")
  private String companyStatus;

  @Column(name = "date_of_creation")
  private String dateOfCreation;

  @Embedded
  private AddressEntity address;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "company_item_id")
  private List<OfficerEntity> officers;
}