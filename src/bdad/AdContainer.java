package bdad;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mobads.AdSettings;
import com.baidu.mobads.AdView;

public class AdContainer extends ViewGroup {

	private AdView mAdView;
	private AdDisplayControl mControl;
	
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
		mControl = new AdDisplayControl(mAdView);
		mControl.hideAd();
		
		
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
