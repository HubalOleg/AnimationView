package com.oleg.hubal.animationview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DyingLightProgressView progressView =
                (DyingLightProgressView) findViewById(R.id.rectangle_custom_view);

        progressView.setOnProgressStateListener(new DyingLightProgressView.OnProgressStateListener() {
            @Override
            public void onViewNarrowed() {
                Toast.makeText(getApplicationContext(), "ViewNarrowed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onViewExpanded() {
                Toast.makeText(getApplicationContext(), "ViewExpanded", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
