package dale.lexisnexisapidemo.company.search.database.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class OfficerEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @Column(name = "officer_role")
  private String officerRole;

  @Column(name = "appointed_on")
  private String appointedOn;

  @Embedded
  private AddressEntity address;

}
