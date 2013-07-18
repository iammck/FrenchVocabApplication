/**
 * 
 */
package com.mck.vocab;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

/**
 * @author Michael
 *
 */
public class VocabProvider extends ContentProvider {

	public static final String AUTHORITY = "content://com.mck.vocab";
	public static final Uri CONTENT_URI = Uri.parse(AUTHORITY);
	
	public static final String DB_NAME = "vocab.db";
	public static final int DB_VERSION = 1;
	public static final String TABLE = "vocabword";
	public static final String C_ID = "_id";
	public static final String C_CHAPTER = "chapter";
	public static final String C_EWORD = "eword";
	public static final String C_FWORD = "fword";
	
	public static final String ACTIVE_TABLE = "activetable";
	public static final String C_AWORD = "aword";

	SQLiteDatabase db;
	DBHelper dbHelper;
	
	@Override
	public boolean onCreate() {
		dbHelper = new DBHelper(getContext());
		return true;
	}
	
	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		db = dbHelper.getWritableDatabase();
		long id = db.insertWithOnConflict(TABLE, null, values, 
				SQLiteDatabase.CONFLICT_IGNORE);
		if (id!=1) 
			return Uri.withAppendedPath(uri, Long.toString(id));
		 
		return uri;
		
//		db = dbHelper.getWritableDatabase();
//		ContentValues values = new ContentValues();
//		values.put(C_ID, vocabWordCount++);
//		values.put(C_CHAPTER, chapNumber);
//		values.put(C_EWORD, vocabWord.eWord);
//		values.put(C_FWORD, vocabWord.fWord);
//		Long result = db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);

// 		context.getContentResolver().insert(CONTENT_URI, values);		
		
	}
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE, projection, selection, selectionArgs, null, null, sortOrder);
		
//		return context.getContentResolver().query(vocabProvider.CONTENT_URI,
//				null,null,null, vocabProvider.C_ID + " DESC");
		
		return cursor;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	/* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	class DBHelper extends SQLiteOpenHelper{

		
		private static final String TAG = "SQLiteOpenHelper";

		private Context context;
		
		public DBHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
			this.context = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql = String.format("create table %s " +
							"(%s int primary key, %s int, %s text, %s text)",
							TABLE, C_ID, C_CHAPTER, C_EWORD, C_FWORD);
			Log.v(TAG, "dbHelper onCreate with SQL: " + sql);
			db.execSQL(sql);	
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// usually ALTER TABLE statement
			db.execSQL("drop if exists " + TABLE);
			onCreate(db);
		}

		/**
		 * Qeuries the database for the chapter with chapNumber as an Id.
		 * @param chapNumber
		 * @return
		 */
		public Cursor queryDatabaseForChapter(int chapNumber){
			String whereClaus = C_CHAPTER + "=" +chapNumber;
			db = getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, whereClaus, null, null, null, C_ID + " DESC");
			return cursor;
		}
		
		
		/**
		 * Gets the chapter from the file and returns it as a string.
		 * @param fileName
		 * @return
		 */
		private String getChapterFromFile(String fileName){
			String result = "";
			try{
				// First, get an input stream
				InputStream inputStream = context.getResources().getAssets().open(fileName);
				// get the input stream reader
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "Cp1252");
				// get a buffered reader
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				// Second, go line by line and save into the chapterText
				String inputLine;	// the input line.
				while((inputLine = bufferedReader.readLine()) != null){
					// get this line
					result += inputLine +"\n";
				}
				bufferedReader.close();
				Log.v(TAG, "getChapterFromFile() has completed with fileName " + fileName);
				
				return result;
				// finished reading the file with out getting the chapterText so close bufferedReader
			} catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}
		/**
		 * Given some chapter text and a number to store the chapter as, puts the 
		 * data in the database.
		 * @param chapText
		 * @param chapNumber
		 */
		private void putChapterInDatabase(String chapText, int chapNumber ){
			String eWord;
			String fWord;
			// split chapter text up by the new line character
			String[] chapData = chapText.split("\n");
			// for each statement
			for(int x = 0; x < chapData.length; x += 2){
				// collect the parts of a vocabWord, french word first
				fWord = chapData[x];
				eWord = chapData[x+1];
				// create, setup a vocabWord
				VocabWord vocabWord = new VocabWord();
				vocabWord.eWord = eWord;
				vocabWord.fWord = fWord;
				// sent off vocabWord with the chapNumber to be put in the db
				insertVocabWordIntoDb(vocabWord, chapNumber);
			}
			Log.v(TAG, "putChapterInDataBase() has put chapter in database as chapter number " + chapNumber );
		}
		
		private int vocabWordCount = 0; // TODO do something with this.
		
		private void insertVocabWordIntoDb(VocabWord vocabWord, int chapNumber){
			db = getWritableDatabase();
			
			ContentValues values = new ContentValues();
			values.put(C_ID, vocabWordCount++);
			values.put(C_CHAPTER, chapNumber);
			values.put(C_EWORD, vocabWord.eWord);
			values.put(C_FWORD, vocabWord.fWord);
		
			Long result = db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			if (result == -1){
				Log.e(TAG, "insertVocabWord() db.insert returned an error code -1");
			}
			db.close();
		}
		
	}
	// a Vocabulary word is several things
		public class VocabWord {
			String eWord;
			String fWord;
			Integer id;
			String wordType;
			String currentLanguage;
		}

}
