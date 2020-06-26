package com.ibm.autoformatedittext;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.databinding.BindingAdapter;

import com.carljmont.lib.R;

public class AutoFormatEditText extends AbstractAutoEditText {
    private static final char DEFAULT_PLACEHOLDER = '#';
    private static final String DEFAULT_INPUT_MASK = "";
    private static final String DEFAULT_STATIC_MASK = "***";

    private InputMask inputMask;
    private StaticMask staticMask;

    public AutoFormatEditText(Context context) {
        super(context);
    }

    public AutoFormatEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AutoFormatEditText);
            String placeholder = a.getString(R.styleable.AutoFormatEditText_placeholder);
            String inputMaskString = a.getString(R.styleable.AutoFormatEditText_inputMask);
            String staticMaskString = a.getString(R.styleable.AutoFormatEditText_staticMask);
            a.recycle();

            String inputMaskStringParam = inputMaskString != null ? inputMaskString : DEFAULT_INPUT_MASK;
            String staticMaskStringParam = staticMaskString != null ? staticMaskString : DEFAULT_STATIC_MASK;
            char placeholderParam = placeholder != null && placeholder.length() > 0 ?
                    placeholder.charAt(0) : DEFAULT_PLACEHOLDER;

            inputMask = new InputMask(inputMaskStringParam, placeholderParam);
            staticMask = new StaticMask(staticMaskStringParam);
        }
    }

    private void setInputMask(String inputMaskString) {
        String unformattedText = getUnformattedText();
        inputMask.setMaskString(inputMaskString);

        //If length of unformatted text is smaller than the format, it must be trimmed
        if (unformattedText != null && unformattedText.length() > inputMaskString.length()) {
            unformattedText = unformattedText.substring(0, inputMaskString.length());
        }

        setText(unformattedText); //Will cause re-formatting
    }

    private void setStaticMask(String maskString) {
        staticMask.setMaskString(maskString);
    }

    @Override
    EditTextState onInputFormat(String textBefore, String textAfter, int selectionStart, int selectionLength, int replacementLength) {
        //Case where no format exists, so the text can be entered without restriction
        if (inputMask == null || inputMask.getMaskString() == null || inputMask.getMaskString().isEmpty()) {
            return new EditTextState(textAfter, textAfter, selectionStart + replacementLength);
        }

        //Case where user is attempting to enter text beyond the length of the format
        if (textAfter.length() > inputMask.getMaskString().length()
            && selectionLength != replacementLength && selectionStart > 0
            && !inputMask.matches(textAfter)) {
                String newUnformattedText = inputMask.unformatText(textBefore, 0, textBefore.length());
                return new EditTextState(textBefore, newUnformattedText, selectionStart);
        }

        CharSequence insertedText = textAfter.subSequence(selectionStart, selectionStart + replacementLength);
        String leftUnformatted = inputMask.unformatText(textBefore, 0, selectionStart);
        String rightUnformatted = inputMask.unformatText(textBefore, selectionStart + selectionLength, textBefore.length());

        //Special case where user has backspaced in front of a character added by the format
        //Remove next character to the left
        if (leftUnformatted.length() > 0 &&
                leftUnformatted.length() <= inputMask.getUnformattedLength() &&
                !inputMask.isPlaceholder(selectionStart) &&
                selectionLength == 1 && replacementLength == 0) {
            leftUnformatted = leftUnformatted.substring(0, leftUnformatted.length() - 1);
        }

        String newUnformattedText = leftUnformatted + insertedText + rightUnformatted;
        String newFormattedText = inputMask.formatText(newUnformattedText);
        int cursorPos = inputMask.formatText(leftUnformatted + insertedText).length();

        return new EditTextState(newFormattedText, newUnformattedText, cursorPos);
    }

    @Override
    String getStaticFormattedText() {
        return staticMask.formatText(getUnformattedText());
    }

    @BindingAdapter("inputMask")
    public static void setInputMask(AutoFormatEditText editText, String inputMaskString) {
        editText.setInputMask(inputMaskString);
    }

    @BindingAdapter("staticMask")
    public static void setStaticMask(AutoFormatEditText editText, String hideMask) {
        editText.setStaticMask(hideMask);
    }
}