package dev.vality.adapter.flow.lib.logback.mask;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MaskingMessageWithPattern {

    public static String maskMessage(String message, Pattern multilinePattern) {
        if (multilinePattern == null) {
            return message;
        }
        StringBuilder sb = new StringBuilder(message);
        Matcher matcher = multilinePattern.matcher(sb);
        while (matcher.find()) {
            IntStream.rangeClosed(1, matcher.groupCount())
                    .filter(group -> matcher.group(group) != null)
                    .forEach(
                            group -> IntStream.range(matcher.start(group), matcher.end(group))
                                    .forEach(i -> sb.setCharAt(i, '*'))
                    );
        }
        return sb.toString();
    }
}
