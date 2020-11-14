package com.bitgam.app.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bitgam.app.R;
import com.bitgam.app.utils.ThemeHelper;

import static com.bitgam.app.ui.XmppActivity.configureActionBar;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(ThemeHelper.find(this));

        setContentView(R.layout.activity_about);
        setSupportActionBar(findViewById(R.id.toolbar));
        configureActionBar(getSupportActionBar());
        setTitle(getString(R.string.title_activity_about_x, getString(R.string.app_name)));
    }
}
