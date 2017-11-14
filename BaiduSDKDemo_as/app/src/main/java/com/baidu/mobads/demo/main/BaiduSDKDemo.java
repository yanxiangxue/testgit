package com.baidu.mobads.demo.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;

import com.baidu.mobads.BaiduManager;
import com.baidu.mobads.demo.main.banner.BannerDemoActivity;
import com.baidu.mobads.demo.main.basevideo.PrerollActivity;
import com.baidu.mobads.demo.main.basevideo.PrerollNativeActivity;
import com.baidu.mobads.demo.main.feeds.FeedH5ChuChuangActivity;
import com.baidu.mobads.demo.main.feeds.FeedH5ListViewRealTimeActivity;
import com.baidu.mobads.demo.main.feeds.FeedH5LunBoActivity;
import com.baidu.mobads.demo.main.feeds.FeedH5RecyclerViewRealTimeActivity;
import com.baidu.mobads.demo.main.feeds.FeedNativeListViewActivity;
import com.baidu.mobads.demo.main.feeds.FeedNativeOriginActivity;
import com.baidu.mobads.demo.main.feeds.FeedNativeVideoActivity;
import com.baidu.mobads.demo.main.jssdk.HybridInventoryActivity;


public class BaiduSDKDemo extends Activity {

    public static final String TAG = "BaiduSDKDemo";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        try {
            // API 19（4.4）, only for debug
            if (Build.VERSION.SDK_INT >= 19) {
                WebView.class.getMethod("setWebContentsDebuggingEnabled", boolean.class).invoke(null, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 广告展现前先调用sdk初始化方法，可以有效缩短广告第一次展现所需时间
        BaiduManager.init(this);

        Button btnListView = (Button) this.findViewById(R.id.btnList);
        btnListView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 开启信息流广告形式
                startActivity(new Intent(BaiduSDKDemo.this, FeedNativeListViewActivity.class));
            }

        });
        Button btnOrigin = (Button) this.findViewById(R.id.btnOrigin);
        btnOrigin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 开启原生字段形式
                startActivity(new Intent(BaiduSDKDemo.this, FeedNativeOriginActivity.class));
            }

        });

        Button btnVideoFeed = (Button) this.findViewById(R.id.btnVideoFeed);
        btnVideoFeed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 开启非WiFi下二次确认下载activity
                startActivity(new Intent(BaiduSDKDemo.this, FeedNativeVideoActivity.class));
            }
        });

        Button btnFeedH5ListView = (Button) this.findViewById(R.id.btnFeedH5ListView);
        btnFeedH5ListView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(BaiduSDKDemo.this, FeedH5ListViewPreloadActivity.class));
                startActivity(new Intent(BaiduSDKDemo.this, FeedH5ListViewRealTimeActivity.class));
            }
        });

        Button btnFeedH5RecyclerView = (Button) this.findViewById(R.id.btnFeedH5RecyclerView);
        btnFeedH5RecyclerView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(BaiduSDKDemo.this, FeedH5RecyclerViewPreloadActivity.class));
                startActivity(new Intent(BaiduSDKDemo.this, FeedH5RecyclerViewRealTimeActivity.class));
            }
        });


        Button btnHtmlFeedLunBo = (Button) this.findViewById(R.id.btnHTMLFeedLunBo);
        btnHtmlFeedLunBo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BaiduSDKDemo.this, FeedH5LunBoActivity.class);
                startActivity(intent);
            }

        });

        Button btnHtmlFeedChuChuang = (Button) this.findViewById(R.id.btnHTMLFeedChuChuang);
        btnHtmlFeedChuChuang.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BaiduSDKDemo.this, FeedH5ChuChuangActivity.class);
                startActivity(intent);
            }

        });


        Button simpleCoding = (Button) findViewById(R.id.simple_coding);
        simpleCoding.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(BaiduSDKDemo.this, BannerDemoActivity.class);
                startActivity(intent);
            }
        });

        Button simpleInter = (Button) findViewById(R.id.simple_inters);
        simpleInter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(BaiduSDKDemo.this, InterstitialAdActivity.class);
                startActivity(intent);
            }
        });

        Button simpleInterForVideoApp = (Button) findViewById(R.id.simple_inters_for_videoapp);
        simpleInterForVideoApp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(BaiduSDKDemo.this,
                        InterstitialAdForVideoAppActivity.class);
                startActivity(intent);
            }
        });

        Button prerollVideo = (Button) findViewById(R.id.preroll);
        prerollVideo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String url = "http://211.151.146.65:8080/wlantest/shanghai_sun/Cherry/dahuaxiyou.mp4";
                Intent intent = new Intent(BaiduSDKDemo.this, PrerollActivity.class);
                intent.putExtra(PrerollActivity.EXTRA_CONTENT_VIDEO_URL, url);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });


        Button prerollNative = (Button) findViewById(R.id.prerollnative);
        prerollNative.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(BaiduSDKDemo.this, PrerollNativeActivity.class);
                startActivity(intent);
            }
        });

        Button simpleNRLM = (Button) findViewById(R.id.nrlm);
        simpleNRLM.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(BaiduSDKDemo.this, CpuAdActivity.class);
                startActivity(intent);
            }
        });


        final Button jssdk = (Button) findViewById(R.id.jssdk);
        jssdk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(jssdk.getContext(), HybridInventoryActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");

        // 通过BaiduXAdSDKContext.exit()来告知AdSDK，以便AdSDK能够释放资源.
        com.baidu.mobads.production.BaiduXAdSDKContext.exit();

        super.onDestroy();
    }
}

// /:~