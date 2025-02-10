package dev.vality.adapter.flow.lib.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Currency {

    /**
     * Currency name
     */
    private String name;

    /**
     * Currency code ISO 4217 in symbolic format (e.g., "USD" for United States dollar)
     */
    private String symbolicCode;

    /**
     * Currency code ISO 4217 in numeric format (e.g., "840" for United States dollar)
     */
    private Short numericCode;

    /**
     * Number of decimal places between the smallest defined currency unit
     * and a whole currency unit ("2" for most currencies)
     */
    private Short exponent;
}
