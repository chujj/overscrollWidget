package com.example.waterfall;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

import com.ds.widget.ScrollOverPanel.IModel;
import com.ds.widget.ScrollOverPanel.IModelItem;

public class ColumnListView implements IModel {
	public static final int UI_COLUMNT = 2;
	
	private ItemDrawable[] mItems;

	public ColumnListView(Context context) {
		int size = BITMAPS.length;
		mItems = new ItemDrawable[size];
		for (int i = 0; i < size; i++) {
			mItems[i] = new ItemDrawable(context, BITMAPS[i]);
		}
	}
	
	
	private void layout(int aTotalWidth) {
		int l, t, r, b;
		int[] columns_height = new int[UI_COLUMNT];
		int singleWidth = (int) (aTotalWidth * 1.0 / UI_COLUMNT);
		
	}
	
	@Override
	public IModelItem[] getItemLists() {
		return mItems;
	}

	@Override
	public int getSingleHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void reloadData() {
		throw new RuntimeException("cannot reloadData");
	}

	@Override
	public int getTotalHeight() { 
		int last = mItems.length - 1;
		return Math.max(mItems[last].mBottom, mItems[last - 1].mBottom);// mustly it hit 
	}

	@Override
	public IModelItem[] getVisiableItems(int aFromY, int aToY) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int hitWhichItem(int aY) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onOverTop() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onOverBottom() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRegionRelease(int aFromY, int aToY) {
		// TODO Auto-generated method stub
		
	}
	
	public static class ItemDrawable extends Drawable implements IModelItem {

		Bitmap mBitmap;
		int mHeight, mWidth;
		int mLeft, mTop, mRight, mBottom;
		int mLeftMargin, mTopMargin, mRightMargin, mBottomMargin;
		
		public ItemDrawable(Context context, int aResId) {
			mBitmap = BitmapFactory.decodeResource(context.getResources(), aResId);
			mHeight = mBitmap.getHeight();
			mWidth = mBitmap.getWidth();
		}

		public void setMargin(int aBorderMargin) {
			mLeftMargin = mTopMargin = mRightMargin = mBottomMargin = aBorderMargin;
		}
		
		@Override
		public void setBounds(int left, int top, int right, int bottom) {
			mLeft = left;
			mTop = top; 
			mRight = right;
			mBottom = bottom;
		}

		@Override
		public void drawSelf(Canvas aCanvas, int aStart, int aEnd, int aTotalWidth) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keydown(int aOffsetX, int aOffsetY) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyUpCancel() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onClick() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean onLongClick() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void draw(Canvas canvas) {
			// TODO Auto-generated method stub
			
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
		
	}

	
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
