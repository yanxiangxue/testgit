package com.baidu.mobads.demo.main.basevideo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.baidu.mobad.feeds.NativeErrorCode;
import com.baidu.mobad.feeds.RequestParameters;
import com.baidu.mobad.nativevideo.BaiduVideoNative;
import com.baidu.mobad.nativevideo.BaiduVideoNative.BaiduVideoNetworkListener;
import com.baidu.mobad.nativevideo.BaiduVideoResponse;
import com.baidu.mobad.nativevideo.BaiduVideoResponse.PrerollMaterialType;
import com.baidu.mobads.demo.main.R;
import com.baidu.mobads.demo.main.feeds.FeedNativeVideoView.VideoPlayCallbackImpl;
import com.baidu.mobads.demo.main.tools.VisibilityChecker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PrerollNativeActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "PrerollNativeActivity";
    PrerollVideoLayout videoLayout;
    private RelativeLayout rl;
    private BaiduVideoResponse mNrAd;
    AQuery aq;
    FrameLayout container;
    boolean firstPlay = true;
    private boolean isReady = false;
    private float density;
    private Button mBt;

    private VideoPlayCallbackImpl mVideoPlayCallback = new VideoPlayCallbackImpl() {
        @Override
        public void onCloseVideo(int progress) {
            if (mNrAd != null) {
                mNrAd.onClose(PrerollNativeActivity.this, progress);
            }
        }

        @Override
        public void onFullScreen(int progress) {
            if (mNrAd != null) {
                mNrAd.onFullScreen(PrerollNativeActivity.this, progress);
            }
        }

        @Override
        public void onStart() {
            if (mNrAd != null) {
                mNrAd.onStart(PrerollNativeActivity.this);
            }
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            if (mNrAd != null) {
                mNrAd.onComplete(PrerollNativeActivity.this);
            }
            videoLayout.setVisibility(View.GONE);
            if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                videoLayout.toggleFullScreen();
            }
        }

        @Override
        public void onError(MediaPlayer mp, int what, int extra) {
            if (mNrAd != null) {
                mNrAd.onError(PrerollNativeActivity.this, what, extra);
            }
        }

        @Override
        public void onClickAd() {
            if (mNrAd != null) {
                mNrAd.onClickAd(PrerollNativeActivity.this);
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preroll_video_native);
        
        // 默认请求http广告，若需要请求https广告，请设置AdSettings.setSupportHttps为true
        // AdSettings.setSupportHttps(true);
        
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        density = metric.density;
        videoLayout = (PrerollVideoLayout) findViewById(R.id.preroll_videoview);
        videoLayout.setActivity(this);
        container = (FrameLayout) findViewById(R.id.preroll_container);
        videoLayout.setVideoPlayCallback(mVideoPlayCallback);
        rl = (RelativeLayout) findViewById(R.id.preroll_native_outer_view);
        mBt = (Button) findViewById(R.id.bt);
        // 发送日志示例
        mBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNrAd.onFullScreen(PrerollNativeActivity.this, videoLayout.getCurrentPosition());

            }
        });
        fetchAd(this);
    }

    private void startConfig(BaiduVideoResponse ad) {
        configAd(ad);
        videoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNrAd.handleClick(v, videoLayout.getCurrentPosition());
            }
        });
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        videoLayout.pause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        videoLayout.setVisibility(View.GONE);

        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            videoLayout.toggleFullScreen();
        }
    }

    void realPlay() {
        Uri videoUri = Uri.parse(mNrAd.getVideoUrl());
        try {
            videoLayout.setVideoURI(videoUri);
            firstPlay = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    List<BaiduVideoResponse> nrAdList = new ArrayList<BaiduVideoResponse>();
    private static String YOUR_AD_PLACE_ID = "2058634";

    public void fetchAd(Activity activity) {
        /**
         * Step 1. 创建BaiduVideoNative对象，参数分别为： 上下文context，广告位ID,
         * BaiduVideoNetworkListener监听（监听广告请求的成功与失败）
         * 注意：请将YOUR_AD_PALCE_ID替换为自己的广告位ID
         */
        BaiduVideoNative baidu = new BaiduVideoNative(activity, YOUR_AD_PLACE_ID, new BaiduVideoNetworkListener() {

            @Override
            public void onAdFail(NativeErrorCode arg0) {
                Toast.makeText(PrerollNativeActivity.this, "没有收到广告，请检查", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAdLoad(List<BaiduVideoResponse> arg0) {
                if (arg0 != null && arg0.size() > 0) {
                    nrAdList = arg0;
                    mNrAd = nrAdList.get(0);
                    android.util.Log.i(TAG, "mNrAd.getMaterialType() is " + mNrAd.getMaterialType());
                    if (mNrAd.getMaterialType() == PrerollMaterialType.VIDEO) {
                        Toast.makeText(PrerollNativeActivity.this,
                                "收到视频广告:" + mNrAd.getVideoUrl() + ",duration:" + mNrAd.getDuration(), Toast.LENGTH_LONG)
                                .show();
                        android.util.Log.i(TAG, "send message ");
                        myHandler.sendEmptyMessage(CHECK_VISIBILITY);
                    } else if (mNrAd.getMaterialType() == PrerollMaterialType.NORMAL) {
                        updateview(mNrAd);
                        Toast.makeText(PrerollNativeActivity.this, "收到图片贴片广告" + mNrAd.getImageUrl(), Toast.LENGTH_LONG)
                                .show();
                    } else if (mNrAd.getMaterialType() == PrerollMaterialType.GIF) {
                        // 自行渲染gif图片
                        Toast.makeText(PrerollNativeActivity.this, "收到gif贴片广告" + mNrAd.getImageUrl(), Toast.LENGTH_LONG)
                                .show();
                    } else {
                        Toast.makeText(PrerollNativeActivity.this, "物料类型不支持", Toast.LENGTH_LONG).show();
                    }
                }
            }

        });

        // /**
        // * Step 2. 创建requestParameters对象，并将其传给baidu.makeRequest来请求广告
        // */
        RequestParameters requestParameters = new RequestParameters.Builder()
                .setWidth(getResources().getDisplayMetrics().widthPixels)
                .setHeight((int) (250 * getResources().getDisplayMetrics().density))
                // .downloadAppConfirmPolicy(
                // RequestParameters.DOWNLOAD_APP_CONFIRM_ONLY_MOBILE) //
                // 用户点击下载类广告时，是否弹出提示框让用户选择下载与否
                .build();

        baidu.makeRequest(requestParameters);
    }

    private void configAd(BaiduVideoResponse ad) {
        aq = new AQuery(PrerollNativeActivity.this);
        videoLayout.configTipView(ad.isDownloadApp());
        aq.id(R.id.preroll_adlogo).image(ad.getAdLogoUrl(), false, true);
        aq.id(R.id.preroll_baidulogo).image(ad.getBaiduLogoUrl(), false, true);
        videoLayout.setTipViewClickedListener(new PrerollVideoLayout.TipViewClickedListener() {
            @Override
            public void onTipViewClicked(View v) {
                mNrAd.handleClick(v, videoLayout.getCurrentPosition());
            }
        });
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Integer time = Integer.parseInt(aq.id(R.id.timecount).getText().toString());
            if (time > 0) {
                time--;
                aq.id(R.id.timecount).text(time.toString());
                handler.postDelayed(runnable, 1000);
            } else {
                container.setVisibility(View.GONE);
            }
        }
    };

    private void updateview(final BaiduVideoResponse ad) {
        aq = new AQuery(PrerollNativeActivity.this);
        aq.id(R.id.preroll_big_pic).image(ad.getImageUrl(), false, true);
        aq.id(R.id.preroll_adlogo).image(ad.getAdLogoUrl(), false, true);
        aq.id(R.id.preroll_baidulogo).image(ad.getBaiduLogoUrl(), false, true);

        // 警告：调用该函数来发送展现，勿漏！
        ad.recordImpression(container);
        container.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 点击响应
                ad.handleClick(view);
            }
        });
        aq.id(R.id.timecount).visible();
        handler.postDelayed(runnable, 1000);

    }

    private int showState = -1;
    private static int CHECK_VISIBILITY = 0;
    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == CHECK_VISIBILITY) {
                startConfig(mNrAd);
                isReady = true;
                try {
                    showState = VisibilityChecker.getViewState(container);
                    if (showState == VisibilityChecker.SHOW_STATE_SHOW) {
                        // 警告：调用该函数来发送展现，勿漏！
                        mNrAd.recordImpression(container);
                        videoLayout.setVisibility(View.VISIBLE);
                        realPlay();
                    } else {
                        myHandler.sendEmptyMessageDelayed(CHECK_VISIBILITY, 1000);
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onClick(View v) {
        if (isReady == true) {
            videoLayout.setVisibility(View.VISIBLE);
            if (firstPlay == true) {
                realPlay();
            } else {
                videoLayout.start();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                videoLayout.toggleFullScreen();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

}
