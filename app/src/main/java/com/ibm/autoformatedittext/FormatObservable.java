package com.ibm.autoformatedittext;

import android.util.Log;
import android.view.View;

import androidx.databinding.ObservableField;

public class FormatObservable {
    public ObservableField<String> formattedText = new ObservableField<>();
    public ObservableField<String> unformattedText = new ObservableField<>();
    public ObservableField<String> format = new ObservableField<>();
    public ObservableField<Boolean> isTextMasked = new ObservableField<>(false);
    public ObservableField<String> hideMask = new ObservableField<>();

    public void onUnformattedValueChanged(String value) {
        unformattedText.set(value);
        Log.i("xxUnformatted", value + ":");
    }

    public void onTextChanged(String text) {
        Log.i("xxFormatted", text + ":");
    }

    public void onHideButtonClick(View v) {
        if (isTextMasked.get() != null) {
            isTextMasked.set(!isTextMasked.get());
        }
    }
}
