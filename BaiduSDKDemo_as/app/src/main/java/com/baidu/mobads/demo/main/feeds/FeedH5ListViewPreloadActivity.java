package com.baidu.mobads.demo.main.feeds;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.baidu.mobad.feeds.RequestParameters;
import com.baidu.mobads.BaiduNativeAdPlacement;
import com.baidu.mobads.BaiduNativeH5AdView;
import com.baidu.mobads.BaiduNativeH5AdViewManager;
import com.baidu.mobads.demo.main.R;

import java.util.ArrayList;
import java.util.List;

public class FeedH5ListViewPreloadActivity extends Activity {
    // 重要：请填上您的广告位ID，代码位错误会导致无法请求到广告
    private static final String AD_PLACE_ID = "3143845"; // 点击广告后不会播放视频
    //    private static final String AD_PLACE_ID = ""; // 点击广告后会跳转到详情页播放视频
    public static final int INTERVAL = 6;
    private ListView mListView;
    private FeedH5ListViewPreloadAdapter mListViewAdapter;
    private List<Object> mList;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_h5_listview);

        mList = new ArrayList<Object>();

        queryContetForListView();
    }

    private void queryContetForListView() {
        // 请求App自己的内容数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i <= 15; i++) {
                    if (i % INTERVAL == 0) {
                        BaiduNativeAdPlacement placement = new BaiduNativeAdPlacement();
                        placement.setSessionId(1); // 设置页面id，可选
                        placement.setPositionId(i); // 设置广告在页面的位置，可选
                        placement.setApId(AD_PLACE_ID);
                        mList.add(placement);
                    } else {
                        mList.add(new FeedH5ContentInfo(i, "内容标题-" + i, "内容摘要-" + i));
                    }
                }
                onAllDataReady();
            }
        }).start();
    }

    private void onAllDataReady() {
        // 可以在这里对广告进行重排序
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mListView = (ListView) FeedH5ListViewPreloadActivity.this.findViewById(R.id.native_list_view);
                mListViewAdapter = new FeedH5ListViewPreloadAdapter(FeedH5ListViewPreloadActivity.this, mList);
                mListView.setAdapter(mListViewAdapter);
                loadAd(0);
            }
        });
    }

    private void loadAd(final int index) {
        if ((index + 1) >= mList.size()) {
            return;
        }
        Object obj = mList.get(index);
        if (obj instanceof BaiduNativeAdPlacement) {
            final BaiduNativeAdPlacement placement = (BaiduNativeAdPlacement) obj;
            final BaiduNativeH5AdView newAdView = BaiduNativeH5AdViewManager.getInstance().getBaiduNativeH5AdView
                    (this, placement, R.drawable.recmd_blue_bidu);
            newAdView
                    .setEventListener(new BaiduNativeH5AdView.BaiduNativeH5EventListner() {

                        @Override
                        public void onAdClick() {

                        }

                        @Override
                        public void onAdFail(String arg0) {
                            loadAd(index + INTERVAL);
                        }

                        @Override
                        public void onAdShow() {

                        }

                        @Override
                        public void onAdDataLoaded() {
                            loadAd(index + INTERVAL);
                        }
                    });
            // 模板宽高比需要和http://mssp.baidu.com/上代码位的设置相同
            double scale = 1.0 * 7 / 5;
            int width = getResources().getDisplayMetrics().widthPixels - (int) (this.getResources()
                    .getDimension(R.dimen.activity_horizontal_margin) * 2);
            int height = (int) (width / scale);
            RequestParameters requestParameters =
                    new RequestParameters.Builder().setWidth(width).setHeight(height).build();
            newAdView.makeRequest(requestParameters);
        }
    }

}
