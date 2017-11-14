package com.baidu.mobads.demo.main.feeds;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.baidu.mobads.BaiduNativeAdPlacement;
import com.baidu.mobads.demo.main.R;

import java.util.ArrayList;
import java.util.List;

public class FeedH5RecyclerViewRealTimeActivity extends Activity {
    // 重要：请填上您的广告位ID，代码位错误会导致无法请求到广告
    private static final String AD_PLACE_ID = "3143840"; // 点击广告后不会播放视频
    //    private static final String AD_PLACE_ID = ""; // 点击广告后会跳转到详情页播放视频
    private RecyclerView mRecyclerView;
    private List<Object> mList;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_h5_recyclerview);
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
                        placement.setSessionId(1); // 设置页面id，不同listview不同，从1开始的正整数，可选
                        placement.setPositionId(i + 1); // 设置广告在页面的楼层，从1开始的正整数，可选
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
        FeedH5RecyclerViewRealTimeActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRecyclerView = (RecyclerView) FeedH5RecyclerViewRealTimeActivity.this.findViewById(R.id.recycler_view);
                mRecyclerView.setHasFixedSize(true);

                // Specify a linear layout manager.
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(FeedH5RecyclerViewRealTimeActivity
                        .this);
                mRecyclerView.setLayoutManager(layoutManager);

                RecyclerView.Adapter mAdapter =
                        new FeedH5RecyclerViewRealTimeAdapter(FeedH5RecyclerViewRealTimeActivity.this, mList);
                mRecyclerView.setAdapter(mAdapter);
            }
        });
    }

}
