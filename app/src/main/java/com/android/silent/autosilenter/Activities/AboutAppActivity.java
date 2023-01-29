package com.android.silent.autosilenter.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.silent.autosilenter.R;

public class AboutAppActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);

        TextView title = findViewById(R.id.btn_title);
        ImageView backBtn  = findViewById(R.id.btn_back);

        backBtn.setOnClickListener(view -> {
            finish();
        });
        title.setText(R.string.setting_aboutApp);
    }
}