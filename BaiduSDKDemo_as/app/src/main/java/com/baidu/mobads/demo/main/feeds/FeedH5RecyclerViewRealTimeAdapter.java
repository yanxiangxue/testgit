package com.baidu.mobads.demo.main.feeds;

/**
 * Created by baidu on 16/12/16.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mobad.feeds.RequestParameters;
import com.baidu.mobads.BaiduNativeAdPlacement;
import com.baidu.mobads.BaiduNativeH5AdView;
import com.baidu.mobads.BaiduNativeH5AdViewManager;
import com.baidu.mobads.demo.main.R;

import java.util.List;

public class FeedH5RecyclerViewRealTimeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_AD = 0;
    private static final int TYPE_DATA = 1;
    private static final int TYPE_MAX_COUNT = TYPE_DATA + 1;
    private Context context;
    private List<Object> mRecyclerViewItems;

    public FeedH5RecyclerViewRealTimeAdapter(Context context, List<Object> recyclerViewItems) {
        this.context = context;
        this.mRecyclerViewItems = recyclerViewItems;
    }

    @Override
    public int getItemViewType(int position) {
        if (position % 3 == 0) {
            return TYPE_AD;
        } else {
            return TYPE_DATA;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_AD:
                RelativeLayout adviewRL = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout
                        .feed_h5_item_placement, null);
                return new BaiduNativeH5AdViewHolder(adviewRL);
            case TYPE_DATA:
                RelativeLayout itemRL = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout
                        .feed_h5_item, null);
                return new ItemViewHolder(itemRL);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case TYPE_AD:
                RelativeLayout rl = (RelativeLayout) holder.itemView;
                rl.removeAllViews();
                BaiduNativeH5AdView newAdView = BaiduNativeH5AdViewManager.getInstance().getBaiduNativeH5AdView
                        (context, (BaiduNativeAdPlacement) mRecyclerViewItems.get(position), R.drawable
                                .recmd_blue_bidu);
                if (newAdView.getParent() != null) {
                    ((ViewGroup) newAdView.getParent()).removeView(newAdView);
                }
                newAdView
                        .setEventListener(new BaiduNativeH5AdView.BaiduNativeH5EventListner() {

                            @Override
                            public void onAdClick() {

                            }

                            @Override
                            public void onAdFail(String arg0) {

                            }

                            @Override
                            public void onAdShow() {

                            }

                            @Override
                            public void onAdDataLoaded() {

                            }
                        });
                // 模板宽高比需要和http://mssp.baidu.com/上代码位的设置相同
                double scale = 1.0 * 7 / 5;
                int width = context.getResources().getDisplayMetrics().widthPixels - (int) (this.context.getResources()
                        .getDimension(R.dimen.activity_horizontal_margin) * 2);
                int height = (int) (width / scale);
                RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(width, height);
                newAdView.setLayoutParams(rllp);
                rl.addView(newAdView);
                RequestParameters requestParameters =
                        new RequestParameters.Builder().setWidth(width).setHeight(height).build();
                newAdView.makeRequest(requestParameters);
                newAdView.recordImpression();
                break;
            case TYPE_DATA:

                ItemViewHolder viewHolder = (ItemViewHolder) holder;
                FeedH5ContentInfo info = (FeedH5ContentInfo) mRecyclerViewItems.get(position);
                viewHolder.doSomething.setText(info.mDoSomething);
                viewHolder.time.setText(info.mTime);
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mRecyclerViewItems.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView time;
        public TextView doSomething;

        public ItemViewHolder(View itemView) {
            super(itemView);
            this.time = (TextView) itemView.findViewById(R.id.time);
            this.doSomething = (TextView) itemView.findViewById(R.id.doSomething);

        }
    }

    class BaiduNativeH5AdViewHolder extends RecyclerView.ViewHolder {

        public BaiduNativeH5AdViewHolder(View itemView) {
            super(itemView);
        }
    }


}
