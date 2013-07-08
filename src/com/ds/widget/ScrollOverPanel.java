package com.ds.widget;

import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

public class ScrollOverPanel extends View {
	private final static int LONG_CLICK_TICK = 600;
	private static final String BOTTOM_PROMT = "on Bottom";
	private static final String TOP_PROMT = "on Top";
	private static final float UI_OVER_PERCENT = 0.3f;
	
	private static enum TouchState {
		REST,
		SCROLLING,
		AUTO
	};

	private TouchState mTouchState = TouchState.REST;
	private int mLastY;
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		
//		height = mModel.getTotalHeight() > height ? mModel.getTotalHeight() : height;
		this.setMeasuredDimension(width, height);
	}

	private int mCurrOffsetY;
	
	private VelocityTracker mVelocityTracker;
	private Scroller mScroller;
	private boolean mSkipTouch = false;
	private MotionEvent mTouchEvent;
	private LongClickHandler mLongClickHandler;
	private IModel mModel;
	private int mDownIdx;
	private int mTouchSlop;
	
	public ScrollOverPanel(Context context) {
		this(context, null);
	}
	
	public ScrollOverPanel(Context context, IModel aModel) {
		super(context);
		
		mModel = aModel;
		mCurrOffsetY = 0;
		mVelocityTracker = VelocityTracker.obtain();
		mLongClickHandler = new LongClickHandler();
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		mScroller = new Scroller(context);
	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

			@Override
			public void run() {
				checkLimit();
				ScrollOverPanel.this.invalidate();
			}
		}, 500);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		drawSelf(canvas);
		
		if (mModel == null) {
			return;
		}
		//		BdLog.e("-----------------------------------------------");
		myDispatchDraw(canvas, 0, this.getMeasuredHeight(), this.getMeasuredHeight());

		//		BdLog.e(" " + mTouchState + " " + this.mScroller.isFinished() + " " + mCurrOffset_Y);
		if (mTouchState == TouchState.AUTO && mScroller.computeScrollOffset()) {
			mCurrOffsetY = mScroller.getCurrY();
			//			BdLog.e(" " + mCurrOffset_Y);
			this.checkLimit();
			this.invalidate();
		}

		if (mTouchState == TouchState.AUTO && this.mScroller.isFinished()) {
			mTouchState = TouchState.REST;
		}
	}

	private void drawSelf(Canvas canvas) {
		canvas.drawColor(0xff880000);
	}

	private void myDispatchDraw(Canvas canvas, int aStart, int aEnd, int aHeight) {
		IModelItem[] lists = mModel.getItemLists();
		int start_y, end_y = 0;
		int single_height = mModel.getSingleHeight();

		int from = -mCurrOffsetY;
		int to = -mCurrOffsetY + (aHeight);
		//				BdLog.e(from + " " + to);
		from = from / single_height;
		to = to / single_height + 1; // +1? , don't know why
		//				BdLog.e(from + " " + to + " " + lists.length);
		from = Math.max(from, 0);
		to = Math.min(to, lists.length);
		//				BdLog.e(from + " " + to);

		for (int i = from; i < to; i++) { // give me a better algorithem
			start_y = aStart + mCurrOffsetY + (i * single_height);
			end_y = start_y + single_height;
			if (end_y < aStart || start_y > aEnd)
				continue;

			Rect rect = canvas.getClipBounds();
			rect.top = start_y;
			rect.bottom = end_y;
			canvas.save();
			canvas.clipRect(rect);
			lists[i].drawSelf(canvas, start_y, end_y, this.getMeasuredWidth());
			canvas.restore();
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		//		BdLog.e(ev.getAction() + " ");
		mVelocityTracker.addMovement(ev);
		
		if (mModel == null) {
			return true;
		}

		if (!mSkipTouch)
			analysisTouchEvent(ev);

		analysisScrollEvent(ev);
		return true;
	}


	private void analysisTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mTouchEvent = MotionEvent.obtain(ev);

			int y = (int) mTouchEvent.getY();
			//				BdLog.e("" + y);
			if (y > 0) {
				// TODO: pick one item to selected
				int offset_x, offset_y;
				offset_x = (int) mTouchEvent.getX();
				offset_y = (y + (-mCurrOffsetY));
				mDownIdx = mModel.hitWhichItem(offset_y);
				//					BdLog.e(offset_x + " " + offset_y);
				mModel.getItemLists()[mDownIdx].keydown(offset_x, offset_y);
				mLongClickHandler.sendEmptyMessageDelayed(LONG_CLICK, LONG_CLICK_TICK);
				this.postInvalidate();
			} else {
				// touchEvent down < 0
				throw new RuntimeException("action-down y < 0");
			}
			break;
		case MotionEvent.ACTION_MOVE:
			float dis = spacing(ev, mTouchEvent);
			if (dis > mTouchSlop) {
				mLongClickHandler.removeMessages(LONG_CLICK);
				mSkipTouch = true;
				mTouchEvent = null;
				if (mModel.getItemLists().length > 0) {
					mModel.getItemLists()[mDownIdx].keyUpCancel();
				}
			} else {
				mLongClickHandler.sendEmptyMessageDelayed(LONG_CLICK, LONG_CLICK_TICK);
				long diff = SystemClock.uptimeMillis() - mTouchEvent.getEventTime();
				//					BdLog.e(dis + " hold: " + diff);
				if (diff > (LONG_CLICK_TICK - 100)) {
					perforeLongClick();
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			mLongClickHandler.removeMessages(LONG_CLICK);
			int yu = (int) mTouchEvent.getY();
			if (yu < 0) {
				throw new RuntimeException("action-up y < 0");
//				mTitlebar.unSelect();
//				mTitlebar.onClick(ev);
			} else {
				//					BdLog.e("" + mDownIdx);
				mModel.getItemLists()[mDownIdx].onClick();
				this.invalidate();
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			mLongClickHandler.removeMessages(LONG_CLICK);
			break;
		default:
			break;
		}
	}

	private float spacing(MotionEvent e1, MotionEvent e2) {
		float x = e1.getX(0) - e2.getX(0);
		float y = e1.getY(0) - e2.getY(0);
		return (float) Math.sqrt(x * x + y * y);
	}

	private void analysisScrollEvent(MotionEvent ev) {
		//		BdLog.e("" + mCurrOffset_Y);
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastY = (int) ev.getY();
			this.mTouchState = TouchState.REST;
			mScroller.forceFinished(true);
			break;
		case MotionEvent.ACTION_MOVE:
			int y = (int) ev.getY();
			mCurrOffsetY += (y - mLastY);
			checkLimit();
			this.invalidate();
			mLastY = y;
			break;

		case MotionEvent.ACTION_CANCEL:
			this.reset();
			break;
		case MotionEvent.ACTION_UP:
			mVelocityTracker.computeCurrentVelocity(1000);
			final int y_spd = (int) mVelocityTracker.getYVelocity();
			final int abs_y_spd = Math.abs(y_spd);
			int dd = (abs_y_spd & 0xffffff00) > 0 ? (abs_y_spd - 255) : 0;
			this.reset();
			mScroller.startScroll(0, this.mCurrOffsetY, 0, (y_spd > 0 ? dd : -dd), abs_y_spd);
			//				BdLog.e("" +  this.mCurrOffsetY + " 	" + y_spd + " " + dd);
			mTouchState = TouchState.AUTO;
			this.invalidate();
			break;

		default:
			break;
		}
	}
	private void checkLimit() {
		int temp = mCurrOffsetY;
		// FIXME: zhujj: getHeight() ?, give me sth defined
		int max = mModel.getTotalHeight() - this.getHeight();
		int slop = 0;
		mCurrOffsetY = temp > slop ? slop : temp < -(max + slop) ? -(max + slop) : temp;
//		BdLog.e(temp + " " + max + " " + mCurrOffset_Y);
	}

	private void perforeLongClick() {
		mLongClickHandler.removeMessages(LONG_CLICK);
		if ((int) mTouchEvent.getY() > 0) {
			if (mModel.getItemLists()[mDownIdx].onLongClick()) {
				mSkipTouch = true;
				mTouchEvent = null;
				this.invalidate();
				vibrator();
			}
		}
	}

	private void vibrator() {
		Vibrator vib = (Vibrator) this.getContext().getSystemService(Service.VIBRATOR_SERVICE);
		vib.vibrate(50); // SUPPRESS CHECKSTYLE
	}
	
	private void reset() {
		mVelocityTracker.clear();
		mTouchState = TouchState.REST;
		mSkipTouch = false;
	}

	private static final int LONG_CLICK = 1;
	private class LongClickHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LONG_CLICK:
				ScrollOverPanel.this.perforeLongClick();
				break;
			}
			super.handleMessage(msg);
		}
	}

	public interface IModel {
		public IModelItem[] getItemLists();
		public int getSingleHeight();
		public void reloadData();

		public int getTotalHeight();
		public IModelItem[] getVisiableItems(int aFromY, int aToY);
		public int hitWhichItem(int aY);
		public void onOverTop();
		public void onOverBottom();
		public void onRegionRelease(int aFromY, int aToY);
	}
	
	public interface IModelItem {
		public void drawSelf(Canvas aCanvas, int aStart, int aEnd, int aTotalWidth);
		public void keydown(int aOffsetX, int aOffsetY);
		public void keyUpCancel();
		public void onClick();
		public boolean onLongClick();
	}

}
