package com.ibm.autoformatedittext;

@SuppressWarnings("WeakerAccess")
public class InputMask {
    private char placeholder;
    private String maskString;
    private Integer unformattedLength;

    public InputMask(String maskString, char placeholder) {
        this.maskString = maskString;
        this.placeholder = placeholder;
    }

    //Returns true if the character in the format at the specified index is the placeholder character
    public boolean isPlaceholder(int index) {
        return index < maskString.length() && maskString.charAt(index) == placeholder;
    }

    //Returns true if the specified string matches the format
    public boolean matches(String formattedString) {
        if (maskString.length() != formattedString.length()) {
            return false;
        }

        for (int i = 0; i < maskString.length(); i++) {
            char currentChar = maskString.charAt(i);
            if (currentChar != placeholder && currentChar != formattedString.charAt(i)) {
                return false;
            }
        }

        return true;
    }

    public int getUnformattedLength() {
        if (unformattedLength == null) {
            unformattedLength = 0;

            for (int i = 0; i < maskString.length(); i++) {
                if (maskString.charAt(i) == placeholder) {
                    unformattedLength++;
                }
            }
        }

        return unformattedLength;
    }

    public String formatText(String unformattedText) {
        StringBuilder builder = new StringBuilder();

        if (unformattedText.length() == 0) {
            return "";
        }

        int unformattedTextPosition = 0;
        for (int i = 0; i < maskString.length(); i++) {
            if (maskString.charAt(i) == placeholder) {
                if (unformattedTextPosition == unformattedText.length()) {
                    break;
                }

                builder.append(unformattedText.charAt(unformattedTextPosition));
                unformattedTextPosition++;
            }else {
                builder.append(maskString.charAt(i));
            }
        }

        return builder.toString();
    }

    public String unformatText(CharSequence formattedText, int start, int end) {
        StringBuilder builder = new StringBuilder();

        if (formattedText.length() > 0) {
            for (int i = start; i < end; i++) {
                if (maskString.charAt(i) == placeholder) {
                    builder.append(formattedText.charAt(i));
                }
            }
        }

        return builder.toString();
    }

    public String getMaskString() {
        return maskString;
    }

    public void setMaskString(String maskString) {
        this.maskString = maskString;
        this.unformattedLength = null;
    }
}