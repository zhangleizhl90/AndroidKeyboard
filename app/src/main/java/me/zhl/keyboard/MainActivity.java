package me.zhl.keyboard;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import me.zhl.libkeyboard.keyboard.SafeInputView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View rootView = findViewById(R.id.root_view);

        SafeInputView safeInputView = (SafeInputView) findViewById(R.id.safe_input_view);
        assert safeInputView != null;
        safeInputView.initKeyboard(rootView);
    }
}
