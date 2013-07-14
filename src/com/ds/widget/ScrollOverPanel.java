package com.ds.widget;

import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

public class ScrollOverPanel extends View {
	private final static int LONG_CLICK_TICK = 600;
	private static final float UI_OVER_PERCENT = 0.3f;
	private static final int UI_ROLLBACK_ANIM_TIME = 500;
	private static final int UI_SCROLL_ANIM_TIME = 500;
	
	private static final String BOTTOM_PROMT = "on Bottom";
	private static final String TOP_PROMT = "on Top";
	
	private static enum TouchState {
		REST,
		AUTO
	};

	private TouchState mTouchState = TouchState.REST;
	private int mLastY;

	private int mCurrOffsetY;
	
	private VelocityTracker mVelocityTracker;
	private Scroller mScroller;
	private boolean mSkipTouch = false;
	private MotionEvent mTouchEvent;
	private LongClickHandler mLongClickHandler;
	private IModel mModel;
	private int mDownIdx;
	private int mTouchSlop;
	
	private TopActionDone mTopAction;
	private BottomActionDone mBottomAction;

	public ScrollOverPanel(Context context, IModel aModel) {
		super(context);
		
		mModel = aModel;
		mCurrOffsetY = 0;
		mVelocityTracker = VelocityTracker.obtain();
		mLongClickHandler = new LongClickHandler();
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		mScroller = new Scroller(context);
		mTopAction = new TopActionDone();
		mBottomAction = new BottomActionDone();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);

		if (mModel != null) {
			mModel.layoutVisiableItems(width, -mCurrOffsetY, height + (-mCurrOffsetY));
		}
		this.setMeasuredDimension(width, height);
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
			mylog("last frame");
			mTouchState = TouchState.REST;
			rollbackIfOverScroll();
		}
	}

	private void rollbackIfOverScroll() {
		int min = this.getMeasuredHeight() - mModel.getBottomLedge();
		int max = -mModel.getTopLedge();

		int dy = 0;
		if (mCurrOffsetY > max) {
			dy = max - mCurrOffsetY;
		} else if (mCurrOffsetY < min) {
			dy = min - mCurrOffsetY;
		}
		
		if (dy != 0) {
			playScorllAnimation(dy, UI_ROLLBACK_ANIM_TIME);
		}
	}

	private void drawSelf(Canvas canvas) {
		canvas.drawColor(0xffaaaaaa);
	}

	private void myDispatchDraw(Canvas canvas, int aStart, int aEnd, int aHeight) {
		aStart += -mCurrOffsetY;
		aEnd += -mCurrOffsetY;
		
		IModelItem[] lists = mModel.getVisiableItems(aStart, aEnd);
		if(lists == null) {
			return; 
		}
	
		for (int i = 0; i < lists.length; i++) {
			lists[i].drawSelf(canvas, aStart, aEnd, this.getMeasuredWidth(), mCurrOffsetY);
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
				mDownIdx = mModel.hitWhichItem(offset_x, offset_y);
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
//		Log.e("", "" +  mCurrOffsetY);
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
			mVelocityTracker.computeCurrentVelocity(UI_SCROLL_ANIM_TIME);
			int y_spd = (int) mVelocityTracker.getYVelocity();
			y_spd = y_spd >> 2;
			this.reset();
			
			playScorllAnimation(y_spd, UI_SCROLL_ANIM_TIME);
			break;

		default:
			break;
		}
	}

	final private void playScorllAnimation(int aDy, int aDuring) {
		mylog(aDy + " || t: " + aDuring);
		if (Math.abs(aDy) < 10) {
			aDuring = 0;
		}
		mScroller.startScroll(0, this.mCurrOffsetY, 0, aDy, aDuring);
		mTouchState = TouchState.AUTO;
		this.postInvalidate();
	}

	private void checkLimit() {
		int temp = mCurrOffsetY;

		int min = this.getMeasuredHeight() - mModel.getBottomLedge();
		int max = -mModel.getTopLedge();
		// call overScroll listeners
		if (temp > max) {
			onOverTop();
		} else if (temp < min) {
			onOverBottom();
		}
		
		// FIXME store this value, for speed up, note the land/port condition
		int overRange = (int) (UI_OVER_PERCENT * this.getMeasuredHeight());
		max += overRange;
		min -= overRange;
		
		// test if we try to over max range
		boolean hitBufferedRangeLedge = overLimit(temp, min, max);//temp > max ? true : temp < min ? true : false;

		mCurrOffsetY = getLimitedValue(temp, min, max);
		
		if (mTouchState == TouchState.AUTO && hitBufferedRangeLedge) { // if auto anim, stop continue scroll
			mylog("auto anim && hit range");
			mScroller.forceFinished(true);
		}
	}

	private void onOverBottom() {
		mylog("onOverBottom");
		if (mModel != null && !mBottomAction.isBottomAcitonProcess) {
			mBottomAction.isBottomAcitonProcess = true;
			mModel.onOverBottom(mBottomAction);
		}
	}

	private void onOverTop() {
		mylog("onOverTop");
		if (mModel != null && !mTopAction.isTopAcitonProcess) {
			mTopAction.isTopAcitonProcess = true;
			mModel.onOverTop(mTopAction);
		}
	}

	final private boolean overLimit(int aCurr, int aMin, int aMax) {
		return aCurr > aMax ? true : aCurr < aMin ? true : false;
	}
	
	final private int getLimitedValue(int aCurr, int aMin, int aMax) {
		return aCurr > aMax ? aMax : aCurr < aMin ? aMin : aCurr;
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

	public static class TopActionDone implements OverAction {
		boolean isTopAcitonProcess;
		public TopActionDone() {
			done();
		}
		public void done() {
			isTopAcitonProcess = false;
		}
	}

	public static class BottomActionDone implements OverAction {
		boolean isBottomAcitonProcess;
		public BottomActionDone() {
			done();
		}
		public void done() {
			isBottomAcitonProcess = false;
		}
	}
	
	public interface OverAction {
		public void done();
	}

	public interface IModel {
		public IModelItem[] getItemLists();
		public void reloadData();

		public int getTotalHeight();
		/** offset from zero, should less than 0
		 * @return
		 */
		public int getTopLedge();
		/** offset from zero, should bigger than 0
		 * @return
		 */
		public int getBottomLedge();
		public IModelItem[] getVisiableItems(int aFromY, int aToY);
		public int hitWhichItem(int aX, int aY);
		/** call when draw, so return as fast as you can
		 * @param aHandle
		 */
		public void onOverTop(OverAction aHandle);
		/** call when draw, so return as fast as you can
		 * @param aHandle
		 */
		public void onOverBottom(OverAction aHandle);
		public void onRegionRelease(int aFromY, int aToY);
		public void layoutVisiableItems(int aTotalWidth, int aFromY, int aToY);
	}
	
	public interface IModelItem {
		public void drawSelf(Canvas aCanvas, int aStart, int aEnd, int aTotalWidth, int aOffset);
		public void keydown(int aOffsetX, int aOffsetY);
		public void keyUpCancel();
		public void onClick();
		public boolean onLongClick();
	}

	public static final void mylog(String aMsg) {
		Log.e(ScrollOverPanel.class.getSimpleName(), aMsg);
	}
}
