package com.ibm.autoformatedittext;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.databinding.BindingAdapter;
import androidx.databinding.BindingMethod;
import androidx.databinding.BindingMethods;

import com.carljmont.lib.R;

@BindingMethods({
        @BindingMethod(type = AbstractAutoEditText.class, attribute = "onTextChanged", method = "setTextChangedListener"),
        @BindingMethod(type = AbstractAutoEditText.class, attribute = "onUnformattedValueChanged", method = "setUnformattedValueChangedListener")
})
public abstract class AbstractAutoEditText extends AppCompatEditText {
    private UnformattedValueListener onUnformattedValueListener;
    private TextChangedListener onTextChangedListener;

    private TextWatcher textWatcher;
    private boolean inputFormatEnabled, staticFormatEnabled;

    private String textAfter, formattedText = "", unformattedText;
    private int selectionStart, selectionLength, replacementLength;

    public AbstractAutoEditText(Context context) {
        super(context);
        init(context, null);
    }

    public AbstractAutoEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    void init(Context context, AttributeSet attrs) {
        setUpTextWatcher();

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AbstractAutoEditText);
            String inputFormatEnabledStr = a.getString(R.styleable.AbstractAutoEditText_inputFormatEnabled);
            String staticFormatEnabledStr = a.getString(R.styleable.AbstractAutoEditText_staticFormatEnabled);
            a.recycle();

            inputFormatEnabled = inputFormatEnabledStr != null && inputFormatEnabledStr.equalsIgnoreCase("true");
            staticFormatEnabled = staticFormatEnabledStr != null && staticFormatEnabledStr.equalsIgnoreCase("true");
        }

        //Prevents edge case where multiple callbacks are occurring for input type 'text'
        if (getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE) ||
                getInputType() == InputType.TYPE_CLASS_TEXT || getInputType() == InputType.TYPE_TEXT_FLAG_MULTI_LINE) {
            setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }
    }

    abstract EditTextState onInputFormat(String textBefore, String textAfter, int selectionStart, int selectionLength, int replacementLength);

    abstract String getStaticFormattedText();

    private void setUpTextWatcher() {
        removeTextChangedListener(textWatcher);

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handleOnTextChanged(s, start, before, count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                handleAfterTextChanged();
            }
        };

        addTextChangedListener(textWatcher);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeTextChangedListener(textWatcher);
        textWatcher = null;
    }

    public void handleOnTextChanged(CharSequence s, int start, int before, int count) {
        textAfter = s.toString();
        selectionStart = start;
        selectionLength = before;
        replacementLength = count;
    }

    public void handleAfterTextChanged() {
        EditTextState newEditTextState = onInputFormat(formattedText, textAfter, selectionStart, selectionLength, replacementLength);

        if (unformattedText == null || !unformattedText.equals(newEditTextState.getUnformattedText())) {
            unformattedText = newEditTextState.getUnformattedText(); //New unformatted text
            if (onUnformattedValueListener != null) {
                onUnformattedValueListener.onUnformattedValueChanged(newEditTextState.getUnformattedText());
            }
        }

        if (formattedText == null || !formattedText.equals(newEditTextState.getFormattedText())) {
            formattedText = newEditTextState.getFormattedText();
            if (onTextChangedListener != null) {
                onTextChangedListener.onTextChanged(newEditTextState.getFormattedText());
            }
        }

        //Removing/re-adding listener will prevent never ending loop
        setNewTextNoFormat(staticFormatEnabled ? getUnformattedText() : formattedText);

        //Setting text programmatically resets the cursor, so this will reposition it
        setSelection(newEditTextState.getCursorStart(), newEditTextState.getCursorEnd());
    }

    private void setNewText(CharSequence s) {
        if (s != null && getText() != null &&
                !getText().toString().equals(s.toString())) {
            setText(s);
        }
    }

    private void setNewTextNoFormat(String s) {
        removeTextChangedListener(textWatcher);
        setNewText(s);
        addTextChangedListener(textWatcher);
    }

//    private void setTextHidden(boolean textHidden) {
//        this.textHidden = textHidden;
//        String newText = textHidden ? getStaticFormattedText() : formattedText;
//        setNewTextNoFormat(newText);
//
//        if (disableOnHide) {
//            setEnabled(!textHidden);
//        }
//    }

//    private void setDisableOnHide(boolean disableOnHide) {
//        this.disableOnHide = disableOnHide;
//    }

    private void setInputFormatEnabled(boolean inputFormatEnabled) {
        this.inputFormatEnabled = inputFormatEnabled;
    }

    private void setStaticFormatEnabled(boolean staticFormatEnabled) {
        this.staticFormatEnabled = staticFormatEnabled;
        String newText = staticFormatEnabled ? getStaticFormattedText() : formattedText;
        setNewTextNoFormat(newText);
    }

    public String getUnformattedText() {
        return unformattedText;
    }

    public String getFormattedText() {
        return formattedText;
    }

    public void setUnformattedValueChangedListener(UnformattedValueListener listener) {
        onUnformattedValueListener = listener;
    }

    public void setTextChangedListener(TextChangedListener listener) {
        onTextChangedListener = listener;
    }

    @BindingAdapter("android:text")
    public static void setTextAndroid(AbstractAutoEditText editText, String newText) {
        editText.setNewText(newText);
    }

//    @BindingAdapter("textHidden")
//    public static void setTextHidden(AbstractAutoEditText editText, boolean textHidden) {
//        editText.setTextHidden(textHidden);
//    }

//    @BindingAdapter("disableOnHide")
//    public static void disableOnHide(AbstractAutoEditText editText, boolean disableOnHide) {
//        editText.setDisableOnHide(disableOnHide);
//    }


    @BindingAdapter("inputFormatEnabled")
    public static void inputFormatEnabled(AbstractAutoEditText editText, boolean inputFormatEnabled) {
        editText.setInputFormatEnabled(inputFormatEnabled);
    }

    @BindingAdapter("staticFormatEnabled")
    public static void staticFormatEnabled(AbstractAutoEditText editText, boolean staticFormatEnabled) {
        editText.setStaticFormatEnabled(staticFormatEnabled);
    }


    public interface UnformattedValueListener {
        void onUnformattedValueChanged(String value);
    }

    public interface TextChangedListener {
        void onTextChanged(String text);
    }

    @SuppressWarnings("WeakerAccess")
    public static class EditTextState {
        private String formattedText, unformattedText;
        private int cursorStart, cursorEnd;

        EditTextState(String formattedText, String unformattedText, int cursorStart, int cursorEnd) {
            this.formattedText = formattedText;
            this.unformattedText = unformattedText;
            this.cursorStart = cursorStart;
            this.cursorEnd = cursorEnd;
        }

        EditTextState(String formattedText, String unformattedText, int cursorPos) {
            this(formattedText, unformattedText, cursorPos, cursorPos);
        }

        String getFormattedText() {
            return formattedText;
        }

        String getUnformattedText() {
            return unformattedText;
        }

        int getCursorStart() {
            return cursorStart;
        }

        int getCursorEnd() {
            return cursorEnd;
        }
    }
}