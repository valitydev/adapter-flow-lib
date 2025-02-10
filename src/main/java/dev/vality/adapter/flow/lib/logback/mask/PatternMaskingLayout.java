package dev.vality.adapter.flow.lib.logback.mask;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PatternMaskingLayout extends PatternLayout {

    private Pattern multilinePattern;
    private List<String> maskPatterns = new ArrayList<>();
    private static final String DELIMITER = "|";

    public void addMaskPattern(String maskPattern) {
        maskPatterns.add(maskPattern);
        multilinePattern = Pattern.compile(String.join(DELIMITER, maskPatterns), Pattern.MULTILINE);
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        return super.doLayout(new MaskedEvent(event,
                MaskingMessageWithPattern.maskMessage(event.getFormattedMessage(), multilinePattern)));
    }
}
