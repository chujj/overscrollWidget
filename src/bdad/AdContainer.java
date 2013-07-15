package bdad;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mobads.AdSettings;
import com.baidu.mobads.AdView;
import com.baidu.mobads.AdViewListener;

public class AdContainer extends ViewGroup {

	private AdView mAdView;
	public AdContainer(Context context) {
		super(context);
		buildAdView(context);
	}
	
	public void setBottomView(View aBottomView) {
		this.removeAllViews();
		this.addView(aBottomView);
		this.addView(mAdView);
	}
	
	private void buildAdView(Context context) {
		AdSettings.setKey(new String[] {
				"美女",
				"游戏",
		});
		
		mAdView = new AdView(context);
		mAdView.setListener(new AdViewListener() {

			public void onAdSwitch() {
				Log.w("", "onAdSwitch");
			}

			public void onAdShow(JSONObject info) {
				Log.w("", "onAdShow " + info.toString());
			}

			public void onAdReady(AdView adView) {
				Log.w("", "onAdReady " + adView);
			}

			public void onAdFailed(String reason) {
				Log.w("", "onAdFailed " + reason);
			}

			public void onAdClick(JSONObject info) {
				Log.w("", "onAdClick " + info.toString());
			}

			public void onVideoStart() {
				Log.w("", "onVideoStart");
			}

			public void onVideoFinish() {
				Log.w("", "onVideoFinish");
			}

			@Override
			public void onVideoClickAd() {
				Log.w("", "onVideoFinish");

			}

			@Override
			public void onVideoClickClose() {
				Log.w("", "onVideoFinish");

			}

			@Override
			public void onVideoClickReplay() {
				Log.w("", "onVideoFinish");

			}

			@Override
			public void onVideoError() {
				Log.w("", "onVideoFinish");

			}
		});
		
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width, height;
		width = MeasureSpec.getSize(widthMeasureSpec);
		height = MeasureSpec.getSize(heightMeasureSpec);
		for (int i = 0; i < this.getChildCount(); i++) {
			View child = this.getChildAt(i);
			if (child != mAdView) {
				child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
					MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
			} else {
				child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
					MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
			}
			
		}
		this.setMeasuredDimension(width, height);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int width, height;
		width = this.getMeasuredWidth();
		height = this.getMeasuredHeight();
		
		for (int i = 0; i < this.getChildCount(); i++) {
			
			View child = this.getChildAt(i);
			if (child != mAdView) {
				child.layout(0, 0, width, height);
			} else {
				child.layout(0, height - child.getMeasuredHeight(), width, height);
			}
		}
	}

}
