package com.ibm.autoformatedittext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
public class StaticMask {
    private String maskString;

    public StaticMask() {
        this.maskString = "";
    }

    public StaticMask(String maskString) {
        this.maskString = maskString;
    }

    public String formatText(String s) {
        String format = maskString;
        format = replaceSingles(s, format);
        format = replaceRanges(s, format);
        return format;
    }

    public String replaceSingles(String s, String format) {
        final String SINGLE_PATTERN_STR = "\\[[0-9]+]";
        Pattern sPattern = Pattern.compile(SINGLE_PATTERN_STR);

        String replacement;

        do {
            replacement = null;
            Matcher m = sPattern.matcher(format);
            if(m.find()) {
                String matchingSection = m.group();
                String digitStr = matchingSection.substring(1, matchingSection.length() - 1);

                //Assuming startStr/endStr can be parsed as int, since pattern specified only digits 0-9
                int index = Integer.parseInt(digitStr);

                if (index < s.length()) {
                    replacement = s.substring(index, index + 1);
                    format = format.replaceFirst(SINGLE_PATTERN_STR, replacement);
                }
            }
        }while(replacement != null);

        return format;
    }

    public String replaceRanges(String s, String format) {
        final String RANGE_INDICATOR = "-";
        final String RANGE_PATTERN_STR = "\\[[0-9]"+RANGE_INDICATOR+"[0-9]+]";
        Pattern rPattern = Pattern.compile(RANGE_PATTERN_STR);

        String replacement;

        do {
            replacement = null;
            Matcher m = rPattern.matcher(format);
            if (m.find()) {
                String matchingSection = m.group();
                int rangeIndicatorIndex = matchingSection.indexOf(RANGE_INDICATOR);

                //Assuming index > 0 since pattern included RANGE_INDICATOR
                String startStr = matchingSection.substring(1, rangeIndicatorIndex);
                String endStr = matchingSection.substring(rangeIndicatorIndex + 1, matchingSection.length() - 1);

                //Assuming startStr/endStr can be parsed as int, since pattern specified only digits 0-9
                int startIndex = Integer.parseInt(startStr);
                int endIndex = Integer.parseInt(endStr);

                if (startIndex >= 0 && endIndex < s.length() && startIndex < endIndex) {
                    StringBuilder replacementBuilder = new StringBuilder();
                    for (int i = startIndex; i <= endIndex; i++) {
                        replacementBuilder.append(s.substring(i, i + 1));
                    }

                    replacement = replacementBuilder.toString();
                    format = format.replaceFirst(RANGE_PATTERN_STR, replacement);
                }
            }
        }while(replacement != null);

        return format;
    }

    public void setMaskString(String maskString) {
        this.maskString = maskString;
    }
}
