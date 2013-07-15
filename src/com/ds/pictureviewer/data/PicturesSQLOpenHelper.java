package com.ds.pictureviewer.data;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ds.io.DsLog;

public class PicturesSQLOpenHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = "pictures.db";
	protected static final String DB_TABLE = "cover";

	private static final int DB_VERSION = 1;

	/** field */
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_INDEX_KEY = "server_index"; // int
	public static final String COLUMN_COVER_BITMAP_URL_KEY = "img"; // String
	public static final String COLUMN_DESCRIPT_KEY = "title"; // String
	public static final String COLUMN_DATE_KEY = "date"; // String
	public static final String COLUMN_COVER_BITMAP_SIZE_KEY = "imgsize"; // String
	public static final String COLUMN_CHILDREN_KEY = "link"; // String
	public static final String COLUMN_CHILDREN_COUNT_KEY = "arraysize"; // String
	public static final String COLUMN_LOCAL_FLIE = "localfile"; // String

	/** end of field */
	
	PicturesSQLOpenHelper(Context aContext) {
		super(aContext, DB_NAME, null, DB_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase aDb) {
		try {
			createDLTable(aDb);
		} catch (SQLException e) {
			e.printStackTrace();
			Math.abs(1 / 0);
			DsLog.e("create error");
		}
	}
	
	private void createDLTable(SQLiteDatabase db) throws SQLException {
		
		String sql = "CREATE TABLE IF NOT EXISTS " + DB_TABLE + " ( " + 
				COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				COLUMN_INDEX_KEY + " INTEGER, " +
				COLUMN_COVER_BITMAP_URL_KEY + " TEXT, " +
				COLUMN_DESCRIPT_KEY + " TEXT, " + 
				COLUMN_DATE_KEY + " TEXT, " + 
				COLUMN_COVER_BITMAP_SIZE_KEY + " TEXT, " + 
				COLUMN_CHILDREN_KEY + " TEXT, " + 
				COLUMN_CHILDREN_COUNT_KEY + " TEXT, " +
				COLUMN_LOCAL_FLIE + " TEXT "
			+ " );";

//		DsLog.e("" + sql);
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase aDb, int aOldVersion, int aNewVersion) {
		DsLog.e("" + aOldVersion + " " + aNewVersion);
	}
	
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		DsLog.e("");
		// ignore exception
	}

	static String[] getAllColumnSelection() {
		String[] columns = new String[] {
				COLUMN_ID, 
				COLUMN_INDEX_KEY, 
				COLUMN_COVER_BITMAP_URL_KEY,
				COLUMN_DESCRIPT_KEY, 
				COLUMN_DATE_KEY, 
				COLUMN_COVER_BITMAP_SIZE_KEY, 
				COLUMN_CHILDREN_KEY,
				COLUMN_CHILDREN_COUNT_KEY,
				COLUMN_LOCAL_FLIE,
		};
		return columns;
	}
	
}