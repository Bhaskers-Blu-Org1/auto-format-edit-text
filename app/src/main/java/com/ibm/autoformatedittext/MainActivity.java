package com.ibm.autoformatedittext;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;

import android.os.Bundle;
import android.util.Log;

import com.ibm.autoformatedittext.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        final FormatObservable observable = new FormatObservable();
        observable.format.set("+1(###) ###-####");
        observable.hideMask.set("+1(***) ***-[6][7][8][9]--[6-9]");
        binding.setObservable(observable);
    }
}
