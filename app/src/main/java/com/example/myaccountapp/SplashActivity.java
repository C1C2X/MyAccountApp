package com.example.myaccountapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        // 设置点击事件
        findViewById(R.id.splash_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击后跳转到 MainActivity
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();  // 结束当前页面，防止返回到 NewActivity
            }
        });
    }
}