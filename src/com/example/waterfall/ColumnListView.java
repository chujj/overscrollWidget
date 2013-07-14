package com.example.waterfall;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.ds.theard.WorkThread;
import com.ds.widget.ScrollOverPanel.IModel;
import com.ds.widget.ScrollOverPanel.IModelItem;
import com.ds.widget.ScrollOverPanel.OverAction;
import com.example.waterfall.LiulanqiRSSResourceDumper.BitmapGroupBean;

public class ColumnListView implements IModel {
	public static final int UI_COLUMNT = 2;
	
	private Context mContext;
	private int mTotalWidth;
	private View mDisplayingView;

	/**
	 * NOTE! makesure access mItems thread-safe
	 */
	private ItemDrawable[] mItems;
	private int[] columns_bottom, columns_top;
	private int mBottomIdx, mTopIdx, mStartIdx;
	private WorkHandler mWorkHandler;

	public ColumnListView(Context context) {
		mContext = context;
		mWorkHandler = new WorkHandler();
		mItems = new ItemDrawable[0];
		mWorkHandler.sendEmptyMessage(WorkHandler.FIRST_QUERY);

		columns_bottom = new int[UI_COLUMNT];
		for (int i = 0; i < columns_bottom.length; i++) {
			columns_bottom[i] = 0;
		}
		
		columns_top = new int[UI_COLUMNT];
		for (int i = 0; i < columns_top.length; i++) {
			columns_top[i] = 0;
		}

		mBottomIdx = 0;
		mTopIdx = 0;
		mStartIdx = 0;
	}

	public void setDisplayingView(View aRView) {
		mDisplayingView = aRView;
	}
	
	@Override
	public void layoutVisiableItems(int aTotalWidth, int aFromY, int aToY) {
		mTotalWidth = aTotalWidth;
		layoutDownward(aTotalWidth, true);
	}
	
	private void layoutDownward(int aTotalWidth, boolean aLayoutAll) {
		int l, t, r, b, tmp, startIdx;
		int[] columns_height = columns_bottom;
		startIdx = mBottomIdx;
		
		if (aLayoutAll) { // reset
			for (int i = 0; i < columns_height.length; i++) {
				columns_height[i] = 0;
			}
			startIdx = 0;
		}
		
		final int singleWidth = (int) (aTotalWidth * 1.0 / UI_COLUMNT);
		
		for (int i = startIdx; i < mItems.length; i++) {
			tmp = mItems[i].getHeightScaleWidth(singleWidth);
			int[] bundle = getMinColumnHeightIdx(columns_height);
			
			l = bundle[0] * singleWidth;
			r = l + singleWidth;
			t = bundle[1];
			b = t + tmp;
			mItems[i].setBounds(l, t, r, b); // update DrawableItem
			
			columns_height[bundle[0]] = b;  // update culumnt_height
		}
		mBottomIdx = mItems.length;
		dumpAllItems();
	}
	
	private void layoutUpward(int aTotalWidth, boolean aLayoutAll) {
		int l, t, r, b, tmp, startIdx;
		int[] columns_height = columns_top;
		startIdx = mTopIdx;

		if (aLayoutAll) { // reset
			for (int i = 0; i < columns_height.length; i++) {
				columns_height[i] = 0;
			}
			startIdx = mStartIdx;
		}

		final int singleWidth = (int) (aTotalWidth * 1.0 / UI_COLUMNT);

		for (int i = startIdx; i >= 0; i--) {
			tmp = mItems[i].getHeightScaleWidth(singleWidth);
			int[] bundle = getMaxColumnHeightIdx(columns_height);

			l = bundle[0] * singleWidth;
			r = l + singleWidth;
			b = bundle[1];
			t = b - tmp;
			mItems[i].setBounds(l, t, r, b); // update DrawableItem

			columns_height[bundle[0]] = t;  // update culumnt_height
		}
		mTopIdx = 0;
		//		dumpAllItems();
	}
	
	private void dumpAllItems() {
		for (int i = 0; i < mItems.length; i++) {
			Log.e("zhujj" , mItems[i].toString());
		}
	}
	
	private int[] getMinColumnHeightIdx(int[] aColumns) {
		int idx, min;
		idx = 0;
		min = aColumns[0];
		for (int i = 1; i < aColumns.length; i++) {
			if (aColumns[i] < min) {
				idx = i;
				min = aColumns[i];
			}
		}
		
		return new int[] {idx, min};
	}
	
	private int[] getMaxColumnHeightIdx(int[] aColumns) {
		int idx, min;
		idx = 0;
		min = aColumns[0];
		for (int i = 1; i < aColumns.length; i++) {
			if (aColumns[i] > min) {
				idx = i;
				min = aColumns[i];
			}
		}

		return new int[] {idx, min};
	}
	
	@Override
	public IModelItem[] getItemLists() {
		return mItems;
	}

	@Override
	public void reloadData() {
		throw new RuntimeException("cannot reloadData");
	}

	@Override
	public int getTotalHeight() { 
		return getBottomLedge() + (-getTopLedge());
	}
	
	@Override
	public int getTopLedge() {
		return mItems[0].mTop;
	}

	@Override
	public int getBottomLedge() {
		int last = mItems.length - 1;
		return Math.max(mItems[last].mBottom, mItems[last - 1].mBottom);// mustly it hit 
	}

	@Override
	public IModelItem[] getVisiableItems(int aFromY, int aToY) {
		// TODO zhujj need to improve
		return mItems;
	}

	@Override
	public int hitWhichItem(int aX, int aY) {
		return findHit(0, mItems.length - 1, aX, aY);
	}
	
	private int findHit(int aStart, int aEnd, int aX, int aY) {
		if (aStart >= aEnd) {
			if (aStart == aEnd) {
				return hitItem(aStart, aX, aY) ? aStart : 0;
			} else {
				return 0;
			}
		} else {
			int tmp = (aStart + aEnd) / 2;
			if (hitItem(tmp, aX, aY)) {
				return tmp;
			} else {
				int next_start, next_end;
				if (mItems[tmp].mBottom < aY) {
					next_start = tmp + 1;
					next_end = aEnd;
				} else {
					next_start = aStart;
					next_end = tmp - 1;
				}
				return findHit(next_start, next_end, aX, aY);
			}
		}
	}

	final private boolean hitItem(int aWhich, int aX, int aY) {
		return mItems[aWhich].mRect.contains(aX, aY);
	}
	@Override
	public void onOverTop(OverAction aHandle) {
		int before = mItems.length;
		int more = BITMAPS.length;
		int new_length = before + more;
		ItemDrawable[] dest = new ItemDrawable[new_length];
		System.arraycopy(mItems, 0, dest, more, before);
		for (int i = 0; i < more; i++) {
			dest[i] = new ItemDrawable(mContext, BITMAPS[i]);
		}
		mItems = dest;
		mBottomIdx += more;
		mStartIdx += more;
		mTopIdx += (more - 1);
		layoutUpward(mTotalWidth, false);

		aHandle.done();
	}

	@Override
	public void onOverBottom(OverAction aHandle) {
		int before = mItems.length;
		int more = BITMAPS.length;
		int new_length = before + more;
		ItemDrawable[] dest = new ItemDrawable[new_length];
		System.arraycopy(mItems, 0, dest, 0, before);
		for (int i = before; i < new_length; i++) {
			dest[i] = new ItemDrawable(mContext, BITMAPS[i - before]);
		}
		mItems = dest;
		layoutDownward(mTotalWidth, false);
		
		aHandle.done();
	}
	
	@Override
	public void onRegionRelease(int aFromY, int aToY) {
		// TODO Auto-generated method stub
		
	}
	
	public static SparseArray sBitmapMap = new SparseArray<Bitmap>();
	private static Bitmap getBitmap(Context context, int aResId) {
		if (sBitmapMap.get(aResId) != null) {
			return (Bitmap) sBitmapMap.get(aResId);
		}
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), aResId);
		sBitmapMap.put(aResId, bitmap);
		
		return bitmap;
	}
	public static class ItemDrawable extends Drawable implements IModelItem {

		private BitmapGroupBean mGroup;
		Bitmap mBitmap;
		int mHeight, mWidth;
		int mLeft, mTop, mRight, mBottom;
		
		int mLeftMargin, mTopMargin, mRightMargin, mBottomMargin;
		Rect mRect;
		private boolean mIsClicked;
		private static Paint mPressedPaint, mBgPaint;
		
		public ItemDrawable(Context context, int aResId) {
			mBitmap = getBitmap(context, aResId);
			mHeight = mBitmap.getHeight();
			mWidth = mBitmap.getWidth();
			mRect = new Rect();
			setMargin(0);
			mIsClicked = false;
			if (mPressedPaint == null) {
				mPressedPaint = new Paint();
				mPressedPaint.setColor(0xff0000ff);
				mBgPaint = new Paint();
				mBgPaint.setColor(0xff00aa00);
			}
		}
		
		public ItemDrawable(Context context, BitmapGroupBean aGroup) {
			mHeight = aGroup.mH;
			mWidth = aGroup.mW;
			mGroup = aGroup;
			mRect = new Rect();
			setMargin(0);
			mIsClicked = false;
			if (mPressedPaint == null) {
				mPressedPaint = new Paint();
				mPressedPaint.setColor(0xff0000ff);
				mBgPaint = new Paint();
				mBgPaint.setColor(0xff00aa00);
			}
		}

		public void setMargin(int aBorderMargin) {
			mLeftMargin = mTopMargin = mRightMargin = mBottomMargin = aBorderMargin;
		}
		
		public int getHeightScaleWidth(int aWidth) {
			double scaleFactor = aWidth * 1.0 / mWidth;
			return (int) (mHeight * scaleFactor);
		}
		
		@Override
		public void setBounds(int left, int top, int right, int bottom) {
			mLeft = left;
			mTop = top; 
			mRight = right;
			mBottom = bottom;
			setMargin(5);
		}

		@Override
		public void drawSelf(Canvas aCanvas, int aStart, int aEnd, int aTotalWidth, int aOffset) {
			mRect.set(mLeft, mTop , mRight, mBottom);
			mRect.offset(0, aOffset);
			if (mIsClicked) {
				aCanvas.drawRect(mRect, mPressedPaint);
			}
			mRect.inset(5, 5);
			aCanvas.drawRect(mRect, mBgPaint);
			if (mBitmap != null)
				aCanvas.drawBitmap(mBitmap, null, mRect, null);
		}

		@Override
		public void keydown(int aOffsetX, int aOffsetY) {
			this.mIsClicked = true;
		}

		@Override
		public void keyUpCancel() {
			this.mIsClicked = false;
		}

		@Override
		public void onClick() {
			this.mIsClicked = false;
		}

		@Override
		public boolean onLongClick() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void draw(Canvas canvas) {
			throw new RuntimeException("don't call me");
//			drawSelf(can, aStart, aEnd, a, 0);
		}

		@Override
		public int getOpacity() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void setAlpha(int alpha) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String toString() {
			return mLeft + " " + mTop + " - " + mRight + " " + mBottom;
		}
		
	}

	private class WorkHandler extends Handler {
		
		public final static int FIRST_QUERY = 0;
		public WorkHandler() {
			super(WorkThread.getsWorkLooper());
		}
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case FIRST_QUERY:
				synchronized (ColumnListView.class) {
					BitmapGroupBean[] first = new LiulanqiRSSResourceDumper().firstQuery();
					int size = first.length;
					mItems = new ItemDrawable[size];
					for (int i = 0; i < size; i++) {
						mItems[i] = new ItemDrawable(mContext, first[i]);
					}
				}
				
				if (mTotalWidth != 0) {
					layoutDownward(mTotalWidth, true);
					if (mDisplayingView != null) {
						mDisplayingView.postInvalidate();
					}
				}
				break;

			default:
				break;
			}
		}
		
	};

	private static final int[] BITMAPS = {
		R.drawable.home_icon_163,
		R.drawable.home_icon_application_center,
		R.drawable.home_icon_baidu,
		R.drawable.home_icon_baopindao,
		R.drawable.home_icon_bookshelf,
		R.drawable.home_icon_bookshelf_updated,
		R.drawable.home_icon_bound_shadow,
		R.drawable.home_icon_budejie,
		R.drawable.home_icon_center_folder,
		R.drawable.home_icon_center_novel,
		R.drawable.home_icon_center_video,
		R.drawable.home_icon_danhuaer,
		R.drawable.home_icon_danhua,
		R.drawable.home_icon_default,
		R.drawable.home_icon_douban,
		R.drawable.home_icon_fls,
		R.drawable.home_icon_folder,
		R.drawable.home_icon_ganji,
		R.drawable.home_icon_hao123,
		R.drawable.home_icon_huahuacaicai,
		R.drawable.home_icon_huangjinkuanggong,
		R.drawable.home_icon_lengxiaohua,
		R.drawable.home_icon_moko,
		R.drawable.home_icon_mop,
		R.drawable.home_icon_naniwang,
		R.drawable.home_icon_neihan,
		R.drawable.home_icon_pan,
		R.drawable.home_icon_plus,
		R.drawable.home_icon_qidian,
		R.drawable.home_icon_qiubai,
		R.drawable.home_icon_qiushi,
		R.drawable.home_icon_qiyi,
		R.drawable.home_icon_qq,
		R.drawable.home_icon_qrcode,
		R.drawable.home_icon_renren,
		R.drawable.home_icon_rsssub,
		R.drawable.home_icon_searchhot,
		R.drawable.home_icon_sina,
		R.drawable.home_icon_sohu,
		R.drawable.home_icon_taobao,
		R.drawable.home_icon_tianya,
		R.drawable.home_icon_tieba,
		R.drawable.home_icon_video,
		R.drawable.home_icon_webbackground,
		R.drawable.home_icon_weibo,
		R.drawable.home_icon_zhuangjibibei,
		R.drawable.land_home_icon_baidu,
		R.drawable.land_home_icon_bookshelf,
		R.drawable.land_home_icon_danhuaer,
		R.drawable.land_home_icon_default,
		R.drawable.land_home_icon_folder,
		R.drawable.land_home_icon_hao123,
		R.drawable.land_home_icon_moko,
		R.drawable.land_home_icon_pan,
		R.drawable.land_home_icon_plus,
		R.drawable.land_home_icon_qidian,
		R.drawable.land_home_icon_qiubai,
		R.drawable.land_home_icon_qiyi,
		R.drawable.land_home_icon_searchhot,
		R.drawable.land_home_icon_sina,
		R.drawable.land_home_icon_taobao,
		R.drawable.land_home_icon_tianya,
		R.drawable.land_home_icon_tieba,
		R.drawable.land_home_icon_video,
		R.drawable.land_home_icon_weibo,
	};

}
