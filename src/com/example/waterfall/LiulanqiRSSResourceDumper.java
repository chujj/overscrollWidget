package com.example.waterfall;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

public class LiulanqiRSSResourceDumper {
	/*
	http://shahe.baidu.com/rssfeed/fetch.php?type=entry_list&imglistonly=1&channel=EN_2085&num=50&dir=up

{img -> cover
     title -> descript
 link -> detail some pictures}

http://rss.cbs.baidu.com/rssfeed/fetch.php?type=entry_list&imglistonly=1&channel=EN_0&num=50&dir=up
http://rss.cbs.baidu.com/rssfeed/fetch.php?type=entry_list&imglistonly=1&channel=EN_2191&num=50&dir=down
http://rss.cbs.baidu.com/rssfeed/fetch.php?type=entry_list&imglistonly=1&channel=EN_2176&num=50&dir=down
http://rss.cbs.baidu.com/rssfeed/fetch.php?type=entry_list&imglistonly=1&channel=EN_2161&num=50&dir=down

*/
	private final static String  DEFAULT_QUERY_URL = "http://rss.cbs.baidu.com/rssfeed/fetch.php?type=entry_list&imglistonly=1&channel=EN_0&num=50&dir=up";
	private final static int BUFFER_LENGTH = 500;
	private ByteArrayOutputStream mBuffer;
	private byte[] buffer;
	private int mBottomIdx, mTopIdx;
	
	public LiulanqiRSSResourceDumper() {
		buffer = new byte[BUFFER_LENGTH];
		mBuffer = new ByteArrayOutputStream();
	}

	public BitmapGroupBean[] firstQuery() {
		BitmapGroupBean[] retval = parseNetData(queryNet(DEFAULT_QUERY_URL));
		mTopIdx = retval[0].mIdx;
		mBottomIdx = retval[retval.length - 1].mIdx;
		dumpGroup(retval);
		return retval;
	}
	
	private String getOlderQueryUrl() {
		StringBuilder sb = new StringBuilder();
		sb.append("http://rss.cbs.baidu.com/rssfeed/fetch.php?type=entry_list&imglistonly=1&channel=EN_");
		sb.append(mBottomIdx);
		sb.append("&num=50&dir=down");
		return sb.toString();
	}
	
	private void dumpGroup(BitmapGroupBean[] aGroup) {
		for (int i = 0; i < aGroup.length; i++) {
			mylog(aGroup[i].toString());
		}
	}
	
	public BitmapGroupBean[] getOlder() {
		BitmapGroupBean[] retval = parseNetData(queryNet(getOlderQueryUrl()));
		mBottomIdx = retval[retval.length - 1].mIdx;
		dumpGroup(retval);
		return retval;
	}
	
	public void getNewer() {
		
	}

	private static String buildQueryString(String id, boolean aOrient) {
		return null;
	}

	private String queryNet(String aUrl) {
		String retval = null;
		try {
			mBuffer.reset();
			URL test = new URL(aUrl);
//			HttpURLConnection hc = (HttpURLConnection) test.openConnection();
			
			InputStream is = /*hc.getInputStream(); */ test.openStream ();
			
			int read;
			while ((read = is.read(buffer, 0, BUFFER_LENGTH)) != -1) {
				mBuffer.write(buffer, 0, read);
			}
			
			mBuffer.flush();
			is.close ();
			
			retval = mBuffer.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return retval;
	}

	private static void mylog(String tag) {
		System.out.println(tag);
	}
	
	private BitmapGroupBean[] parseNetData(String aData) {
		BitmapGroupBean[] retval = null;
		try {
			JSONArray jsonarr = new JSONArray(aData);

			for (int i = 0; i < 1 /*jsonarr.length()*/; i = i + 1) {
				
				JSONObject chanel = (JSONObject) jsonarr.get(i);
				
				int id = 0;
				int num = chanel.getInt("num");
				if (num != 0) {
					id = chanel.getInt("id");
				}

				JSONArray items = chanel.getJSONArray("data");
				retval = new BitmapGroupBean[items.length()];
				for (int j = 0; j < items.length(); j = j + 1) {
					BitmapGroupBean group = new BitmapGroupBean();
					retval[j] = group;
					JSONObject item = (JSONObject) items.get(j);
					
					group.mChildren = item.getString(BitmapGroupBean.children_key);
					group.mChoildrenCount = item.getInt(BitmapGroupBean.children_count_key);
					group.mDate = item.getString(BitmapGroupBean.data_key);
					group.mCoverUrl = item.getString(BitmapGroupBean.cover_bitmap_url_key);
					group.mDescript = item.getString(BitmapGroupBean.descript_key);
					group.mIdx = item.getInt(BitmapGroupBean.index_key);
					String size = item.getString(BitmapGroupBean.cover_bitmap_size_key);
					group.setupSize(size);
				}
				
				mylog("ok1");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return retval;
	}
	
	public static class BitmapGroupBean {
		private static final String index_key = "index";
		private static final String descript_key = "title";
		private static final String cover_bitmap_url_key = "img";
		private static final String data_key = "date";
		private static final String children_key = "link";
		private static final String children_count_key = "arraysize";
		private static final String cover_bitmap_size_key= "imgsize";

		String mChildren;
		String mDescript;
		String mCoverUrl;
		String mDate;
		int mIdx;
		int mChoildrenCount;
		int mW, mH;
		
		private void setupSize(String aSize) {
			int w = 0, h = 0;
			if (aSize == null || aSize.equals("")) {
				w = h = 0;
			} else {
				String[] value =  aSize.split(",");
				w = Integer.parseInt(value[0]);
				h = Integer.parseInt(value[1]);
			}
			
			mW =w;
			mH = h;
		}

		@Override
		public String toString() {
			
			return mIdx + mDescript +  " count: "+ mChoildrenCount + " size: " + mW + "|"+ mH;
		}
	}
	
	public static class BitmapItem {
		private static final String bitmap_key = "url";
		String mUrl;
	}
	
}
