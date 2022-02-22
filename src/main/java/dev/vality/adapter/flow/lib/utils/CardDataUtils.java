package dev.vality.adapter.flow.lib.utils;

import dev.vality.cds.storage.SessionData;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CardDataUtils {

    public static String extractCvv2(SessionData sessionData) {
        if (sessionData == null
                || sessionData.getAuthData() == null
                || !sessionData.getAuthData().isSetCardSecurityCode()) {
            return null;
        }
        return sessionData.getAuthData().getCardSecurityCode().getValue();
    }

}
