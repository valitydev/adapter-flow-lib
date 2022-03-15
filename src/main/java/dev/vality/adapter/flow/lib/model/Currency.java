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
     * Currency in symbolic formats (example: "USD").
     */
    private String symbolicCode;
    /**
     * Currency in symbolic formats (example: "USD").
     */
    private Short numericCode;

}
