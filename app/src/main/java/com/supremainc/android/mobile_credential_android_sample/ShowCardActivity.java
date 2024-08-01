package com.supremainc.android.mobile_credential_android_sample;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class ShowCardActivity extends AppCompatActivity {
    Animation aniFlow = null;
    ImageView imageView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_card);

        aniFlow = AnimationUtils.loadAnimation(this, R.anim.ani_flow);
        imageView = (ImageView)findViewById(R.id.imageView1);

        imageView.startAnimation(aniFlow);

        Button backBtn = (Button)findViewById(R.id.backButton);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
