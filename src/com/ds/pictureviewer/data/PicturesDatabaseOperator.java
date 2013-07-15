package com.ds.pictureviewer.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

class PicturesDatabaseOperator {
	private static PicturesDatabaseOperator sIntance;
	private static Context sAppCtx;

	private SQLiteDatabase mSqlDbSync;
	private String mDbTabName;


	private PicturesDatabaseOperator(Context aCtx) {
		PicturesSQLOpenHelper tDb = new PicturesSQLOpenHelper(aCtx);
		mSqlDbSync = tDb.getWritableDatabase();
		mDbTabName = PicturesSQLOpenHelper.DB_TABLE;
	}

	public static void init (Context aAppContext) {
		if (sAppCtx != null) {
			return;
//			throw new IllegalArgumentException("multi-init");
		}
		sAppCtx = aAppContext;
	}
	
	public synchronized static PicturesDatabaseOperator getIntance() {
		if (sIntance == null) {
			if (sAppCtx == null) 
				throw new IllegalArgumentException("use app-ctx init first");
			sIntance = new PicturesDatabaseOperator(sAppCtx);
		}
		return sIntance;
	}

	public Cursor queryAll() {
		String[] columns = PicturesSQLOpenHelper.getAllColumnSelection();

		String indexKeyDownward = PicturesSQLOpenHelper.COLUMN_INDEX_KEY + " DESC";
		return mSqlDbSync.query(mDbTabName, columns, null, null, null, null, indexKeyDownward);
	}

	public long insert(ContentValues cv) {
		return mSqlDbSync.insert(mDbTabName, null, cv);
	}
	
	public Cursor query(int id) {
		Cursor cr = null;
		String[] columns = PicturesSQLOpenHelper.getAllColumnSelection();
		String where = PicturesSQLOpenHelper.COLUMN_ID + " == '" + id  + "'";

		cr = mSqlDbSync.query(mDbTabName, columns, where, null, null, null, null);
		
		return cr;
	}

	public void update(int id, ContentValues cv) {
		String where = PicturesSQLOpenHelper.COLUMN_ID + " == '" + id + "'";
		mSqlDbSync.update(mDbTabName, cv, where, null);
	}
	
	public void delete(int id) {
		String where = PicturesSQLOpenHelper.COLUMN_ID + " == '" + id + "'";
		mSqlDbSync.delete(mDbTabName, where, null);
	}

	public Cursor dump() {
		return mSqlDbSync.query(mDbTabName, null, null, null, null, null, null);
	}

	String getCursorStringValue(Cursor aCursor, String aColumnName) {
		int idx = aCursor.getColumnIndexOrThrow(aColumnName);
		return aCursor.getString(idx);
	}
	
	boolean getCursorBooleanValue(Cursor aCursor, String aColumnName) {
		int retval = this.getCursorIntValue(aCursor, aColumnName);
		return retval == 1 ? true : false;
	}
	
	int getCursorIntValue(Cursor aCursor, String aColumnName) {
		int idx = aCursor.getColumnIndexOrThrow(aColumnName);
		return aCursor.getInt(idx);
	}
	
	long getCursorLongValue(Cursor aCursor, String aColumnName) {
		int idx = aCursor.getColumnIndexOrThrow(aColumnName);
		return aCursor.getLong(idx);
	}
	
}
