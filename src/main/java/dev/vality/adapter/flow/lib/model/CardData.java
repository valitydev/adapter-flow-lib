package dev.vality.adapter.flow.lib.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CardData {

    @ToString.Exclude
    private String pan;
    @ToString.Exclude
    private Byte expMonth;
    @ToString.Exclude
    private Short expYear;
    @ToString.Exclude
    private String cvv2;
    @ToString.Exclude
    private String cardHolder;
    @ToString.Exclude
    private String cardToken;

}
