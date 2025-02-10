package dev.vality.adapter.flow.lib.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class QrDisplayData {

    /**
     * Url for get qr code.
     */
    private String qrUrl;

    /**
     * Id for generate tag
     */
    private String tagId;

}
