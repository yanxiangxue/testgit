package com.baidu.mobads.demo.main.feeds;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.baidu.mobads.BaiduNativeAdPlacement;
import com.baidu.mobads.demo.main.R;

import java.util.ArrayList;
import java.util.List;

public class FeedH5ListViewRealTimeActivity extends Activity {
    private static final String AD_PLACE_ID = "3143845"; // 重要：请填上您的广告位ID，代码位错误会导致无法请求到广告
    private ListView mListView;
    private FeedH5ListViewRealTimeAdapter mListViewAdapter;
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
                    if (i % 3 == 0) {
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
        FeedH5ListViewRealTimeActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mListView = (ListView) FeedH5ListViewRealTimeActivity.this.findViewById(R.id.native_list_view);
                mListViewAdapter = new FeedH5ListViewRealTimeAdapter(FeedH5ListViewRealTimeActivity.this, mList);
                mListView.setAdapter(mListViewAdapter);
            }
        });
    }

}
