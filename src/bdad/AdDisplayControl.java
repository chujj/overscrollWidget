package bdad;

import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.baidu.mobads.AdView;
import com.baidu.mobads.AdViewListener;

public class AdDisplayControl {
	private final static int AD_LOOPER_COUNT = 3;

	private AdView mAdView;
	private int mSwitchCount;
	private Handler mHandler;

	public AdDisplayControl(AdView aAdView) {
		mAdView = aAdView;
		mAdView.setListener(new AdViewListener() {

			public void onAdSwitch() {
				if((mSwitchCount++)  > AD_LOOPER_COUNT) {
					hideAd();
				}
				
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
				Log.w("", "onAdClick hideAd");
				hideAd();
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
		
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case 0x555:
						if (mAdView.getVisibility() == View.GONE) {
							showAd();
						}
						break;

					default:
						break;
				}
			}
			
		};
//		aAdView.set
	}
	
	final public void showAdDelay() {
		mHandler.sendEmptyMessageDelayed(0x555, 1000 * 20);
	}
	
	final public void hideAd() {
		mSwitchCount = 0;
		mAdView.setVisibility(View.GONE);
		showAdDelay();
	}
	
	final private void showAd() {
		mAdView.setVisibility(View.VISIBLE);
	}

}
