package com.baidu.mobads.demo.main.banner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.baidu.mobads.demo.main.R;


public class BannerDemoActivity extends Activity {

    public static final String TAG = "BannerDemoActivity";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.banner_main);

        Button btn1 = (Button) this.findViewById(R.id.btn1);
        btn1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 开启信息流广告形式
                startActivity(new Intent(BannerDemoActivity.this, BannerAd1Activity.class));
            }

        });
        Button btn2 = (Button) this.findViewById(R.id.btn2);
        btn2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 开启原生字段形式
                startActivity(new Intent(BannerDemoActivity.this, BannerAd2Activity.class));
            }

        });      
        
        Button btn3 = (Button) this.findViewById(R.id.btn3);
        btn3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 开启非WiFi下二次确认下载activity
                startActivity(new Intent(BannerDemoActivity.this, BannerAd3Activity.class));
            }
        });
        
        Button btn4 = (Button) this.findViewById(R.id.btn4);
        btn4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BannerDemoActivity.this, BannerAd4Activity.class));
            }
        });

    }

}

// /:~