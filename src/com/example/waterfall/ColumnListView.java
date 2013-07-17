package com.example.waterfall;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.ds.bitmaputils.BitmapGetter;
import com.ds.bitmaputils.BitmapGetter.BitmapGotCallBack;
import com.ds.theard.WorkThread;
import com.ds.widget.ScrollOverPanel.BottomActionDone;
import com.ds.widget.ScrollOverPanel.IModel;
import com.ds.widget.ScrollOverPanel.IModelItem;
import com.ds.widget.ScrollOverPanel.OverAction;
import com.example.waterfall.LiulanqiRSSResourceDumper.BitmapGroupBean;

public class ColumnListView implements IModel {
	public static final int UI_COLUMNT = 2;
	
	private Context mContext;
	private int mTotalWidth;
	private View mDisplayingView;
	private LiulanqiRSSResourceDumper mBitmapResource;

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
		mBitmapResource = new LiulanqiRSSResourceDumper(this);
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
	final private void refreshUI() {
		if (mDisplayingView != null) {
			mDisplayingView.postInvalidate();
		}
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
		return 0;
//		return findHit(0, mItems.length - 1, aX, aY);
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
//		int before = mItems.length;
//		int more = BITMAPS.length;
//		int new_length = before + more;
//		ItemDrawable[] dest = new ItemDrawable[new_length];
//		System.arraycopy(mItems, 0, dest, more, before);
//		for (int i = 0; i < more; i++) {
//			dest[i] = new ItemDrawable(mContext, BITMAPS[i]);
//		}
//		mItems = dest;
//		mBottomIdx += more;
//		mStartIdx += more;
//		mTopIdx += (more - 1);
//		layoutUpward(mTotalWidth, false);

		aHandle.done();
	}

	@Override
	public void onOverBottom(OverAction aHandle) {
		Message.obtain(mWorkHandler, WorkHandler.OLDER_QUERY, aHandle).sendToTarget();
	}
	
	@Override
	public void onRegionRelease(int aFromY, int aToY) {
		// TODO Auto-generated method stub
		
	}

	private static Paint mPressedPaint, mBgPaint, mBorderPaint, mTextPaint;
	public class ItemDrawable extends Drawable implements IModelItem {
		private static final int UI_BORDER_MARGIN = 20;
		private static final int UI_BG_COLOR = 0xfff7f7f7;
		private static final int UI_PRESS_COLOR = 0xff0000ff;
		private static final int UI_TEXT_COLOR = 0xff000000;
		private static final int UI_TEXT_BOTTOM_UP = 10;
		
		private BitmapGroupBean mGroup;
		Bitmap mBitmap;
		int mHeight, mWidth;
		int mLeft, mTop, mRight, mBottom;
		
		int mLeftMargin, mTopMargin, mRightMargin, mBottomMargin;
		Rect mRect;
		private boolean mIsClicked;

		public ItemDrawable(Context context, BitmapGroupBean aGroup) {
			mHeight = aGroup.mH;
			mWidth = aGroup.mW;
			mGroup = aGroup;
			mRect = new Rect();
			setMargin(0);
			mIsClicked = false;
			if (mPressedPaint == null) {
				mPressedPaint = new Paint();
				mPressedPaint.setColor(UI_PRESS_COLOR);
				
				mBgPaint = new Paint();
				mBgPaint.setColor(UI_BG_COLOR);
				
				mBorderPaint = new Paint();
				mBorderPaint.setColor(0xff000000);
				mBorderPaint.setStyle(Style.STROKE);
				
				mTextPaint = new Paint();
				mTextPaint.setAntiAlias(true);
				mTextPaint.setTextSize(UI_BORDER_MARGIN);
				mTextPaint.setColor(UI_TEXT_COLOR);
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
			setMargin(UI_BORDER_MARGIN);
		}

		@Override
		public void drawSelf(Canvas aCanvas, int aStart, int aEnd,
				int aTotalWidth, int aOffset) {
			mRect.set(mLeft, mTop, mRight, mBottom);
			
			if (mRect.top > aEnd || mRect.bottom < aStart) {
				mBitmap = null;
				BitmapGetter.releaseBitmap(mGroup);
				return;
			}
			mRect.offset(0, aOffset);
			
			aCanvas.drawRect(mRect, mBorderPaint);
//			if (mIsClicked) {
//				aCanvas.drawRect(mRect, mPressedPaint);
//			}
			mRect.inset(UI_BORDER_MARGIN, UI_BORDER_MARGIN);
			mRect.offset(0, -UI_BORDER_MARGIN);
			aCanvas.drawRect(mRect, mBgPaint);
			if (mBitmap != null) {
				aCanvas.drawBitmap(mBitmap, null, mRect, null);
			} else {
				mBitmap = BitmapGetter.tryGetBitmapFromUrlOrCallback(
						mGroup, new BitmapGotCallBack() {
							@Override
							public void onBitmapGot(Bitmap aBitmap) {
								mBitmap = aBitmap;
								ColumnListView.this.refreshUI();
							}
						});
			}
			
			if (mGroup.mDescript != null) {
				aCanvas.drawText(mGroup.mDescript, mRect.left, mBottom - UI_TEXT_BOTTOM_UP + aOffset, mTextPaint);
			}
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
		public final static int OLDER_QUERY = 1;
		public final static int NEWER_QUERY = 2;
		
		public WorkHandler() {
			super(WorkThread.getsWorkLooper());
		}
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case FIRST_QUERY:
				BitmapGroupBean[] first = mBitmapResource.firstQuery(getLastVisitIndex());
				synchronized (ColumnListView.class) {
					int size = first.length;
					mItems = new ItemDrawable[size];
					for (int i = 0; i < size; i++) {
						mItems[i] = new ItemDrawable(mContext, first[i]);
					}
				}
				
				if (mTotalWidth != 0) {
					layoutDownward(mTotalWidth, true);
					refreshUI();
				}
				break;
			case OLDER_QUERY:
				BitmapGroupBean[] older = mBitmapResource.getOlder(mItems[mItems.length - 1].mGroup.mIdx);
				synchronized (ColumnListView.class) {
					int before = mItems.length;
					int more = older.length;
					int new_length = before + more;
					ItemDrawable[] dest = new ItemDrawable[new_length];
					System.arraycopy(mItems, 0, dest, 0, before);
					for (int i = before; i < new_length; i++) {
						dest[i] = new ItemDrawable(mContext, older[i - before]);
					}
					mItems = dest;
				}
				layoutDownward(mTotalWidth, false);

				((BottomActionDone) msg.obj ).done();
				break;
			case NEWER_QUERY:
				break;
			default:
				break;
			}
		}
		
	};
	
	
	private static final String LAST_VISIT_INDEX = "last_visit_item_index";
	private static final int DEFAULT_LAST_VISIT_INDEX = 0;
	
	private int getLastVisitIndex() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sp.getInt(LAST_VISIT_INDEX, DEFAULT_LAST_VISIT_INDEX);
	}

	public void saveLastVisitIndex() {
		this.setLastVisitIndex(mItems[0].mGroup.mIdx);
	}
	
	public void saveLastVisitIndex(int aIdx) {
		this.setLastVisitIndex(aIdx);
	}

	private void setLastVisitIndex(int aIndex) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		Editor ed = sp.edit();
		ed.putInt(LAST_VISIT_INDEX, aIndex);
		ed.commit();
	}
}
