package dev.vality.adapter.flow.lib.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PayerInfo {

    private String ip;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String country;
    private String state;
    private String city;
    private String address;
    private String postalCode;
    private String dateOfBirth;
    private String documentId;
}
