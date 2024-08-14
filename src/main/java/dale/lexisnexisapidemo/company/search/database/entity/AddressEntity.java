package dale.lexisnexisapidemo.company.search.database.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class AddressEntity {

  private String locality;

  @Column(name = "postal_code")
  private String postalCode;

  private String premises;

  @Column(name = "address_line_1")
  private String addressLine1;

  private String country;

}