/**
 * 
 */
package com.mck.vocab;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
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
	
	public static final String TAG = "VocabProvider";

	public static final String AUTHORITY = "content://com.mck.vocab";
	public static final Uri CONTENT_URI = Uri.parse(AUTHORITY);
	
	public static final String DB_NAME = "vocab.db";
	public static final int DB_VERSION = 1;
	public static final String VOCAB_TABLE = "vocabword";
	public static final String C_ID = "_id";
	public static final String C_CHAPTER = "chapter";
	public static final String C_EWORD = "eword";
	public static final String C_FWORD = "fword";
	
	public static final String ACTIVE_TABLE = "activetable";
	public static final String C_AWORD = "aword";

	public static final String UPDATE_TYPE_OPEN_VOCAB = "update_type_open_vocab";

	public static final String VALUES_UPDATE_TYPE = "update_type";

	public static final String UPDATE_OPEN_VOCAB_NAME = "update_open_vocab_name";
	public static final String VALUES_VOCAB_NAME = "vocab_name";

	private static final int SAMPLE_SIZE = 10;

	public static final String VALUES_VOCAB_NUMBER = "vocab_number";

	public static final String VOCAB_LANGUAGE = "vocab_language";

	SQLiteDatabase db;
	DBHelper dbHelper;
	Integer vocabNumber;
	String vocabLanguage;
	
	/**
	 * Happens the first time that a VocabResolver is created.
	 */
	@Override
	public boolean onCreate() {
		Log.v(TAG, "onCreate has begun ");
		// this may be a horribly wrong way to load up the preference.
		//  If a preferences file by this name does not exist, it will be created when you retrieve an editor
		SharedPreferences prefs = this.getContext().getSharedPreferences(TAG , Context.MODE_PRIVATE);
		// Is there a word count variable handy? There should never be an index of -1.
		int wordCnt = prefs.getInt("vocabWordCount", -1);
		if (wordCnt == -1){// There is not so set one up.
			Log.v(TAG, "putting vocabWordCount in prefs and commiting.");
			prefs.edit().putInt("vocabWordCount", 0).commit();
		}
		// set initial language
		vocabLanguage = prefs.getString(VOCAB_LANGUAGE, "english");
		
		// now set up the datebase via a new db helper. It needs the context.
		dbHelper = new DBHelper(getContext());
		Log.v(TAG, "onCreate has finished ");
		return true;
	}
	@Override
	public String getType(Uri uri) {
		Log.v(TAG, "getType() has begun.");
		return null;
	}
	
	/**
	 * Entry for the the CursorLoader to get the active table. The query comes preloaded
	 * to do the query
	 * 
	 * @param uri
	 * @param projection
	 * @param selection
	 * @param selectionArgs
	 * @param sortOrder
	 * @return
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(ACTIVE_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
		
		//		return context.getContentResolver().query(vocabProvider.CONTENT_URI,
		//				null,null,null, vocabProvider.C_ID + " DESC");
		
		return cursor;
	}

	/**
	 * Entry points for VocabListActivity
	 * This provder needs to handle getting methods for several requests by the activity, 
	 * 	changing active table vocab, (loading new chapter) 
	 * 	change a active table row language, (change a single rows language)
	 * 	change active table language (change all item's language)
	 * 	removing a row from the active table,
	 * 	something i am forgetting atm.
	 * but this app has a min api of 8 so can not override call(). Thus, going to use 
	 * the update() method to perform the requests.
	 */
	
	
	
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
//		db = dbHelper.getWritableDatabase();
//		long id = db.insertWithOnConflict(VOCAB_TABLE, null, values, 
//				SQLiteDatabase.CONFLICT_IGNORE);
//		if (id!=1) 
//			return Uri.withAppendedPath(uri, Long.toString(id));
//		 
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
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		String reqType = (String) values.get(VALUES_UPDATE_TYPE);
		Log.v(TAG,"begining an" + reqType + " update in update()");		
		if(reqType.equals(UPDATE_TYPE_OPEN_VOCAB)){
			
			// get the sample items from the file into the vocab table
			String vocabName = values.getAsString(VALUES_VOCAB_NAME);
			int vocabNumber = values.getAsInteger(VALUES_VOCAB_NUMBER);
			getSampleVocabTableFromFile(vocabName, vocabNumber);
			
			// get the new sample vocab into the active table and notify
			setActiveTableToVocabTable(vocabNumber);
			getContext().getContentResolver().notifyChange(CONTENT_URI, null);
			
			// get the full vocab into the active table and notify
			getVocabTableFromFile(vocabName, vocabNumber);
			setActiveTableToVocabTable(vocabNumber);
			getContext().getContentResolver().notifyChange(CONTENT_URI, null);			
		}		
		Log.v(TAG, "update() complete");
		return 0;	
	}
	
	private void getSampleVocabTableFromFile(String vocabName, int chapterNumber) {
		String result = "";
		// first get from file
		try{
			// First, get an input stream
			InputStream inputStream = this.getContext().getResources().getAssets().open(vocabName);
			// get the input stream reader
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "Cp1252");
			// get a buffered reader
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			// Second, go line by line and save into the chapterText
			String inputLine;	// the input line.
			int x = 0;
			while(( (inputLine = bufferedReader.readLine()) != null) && (x++ < SAMPLE_SIZE)){
				// get this line
				result += inputLine +"\n";
			}
			bufferedReader.close();
			Log.v(TAG, "getChapterFromFile() has completed with fileName " + vocabName);
		} catch (Exception e){
			e.printStackTrace();
		}
		// now that the file is in memory, put it in the right table
		dbHelper.putVocabInVocabTable(result, chapterNumber);
		// notify?
		getContext().getContentResolver().notifyChange(CONTENT_URI, null);	
	}
	
	
	private void getVocabTableFromFile(String name, int chapterNumber){
		String result = "";
		try{
			// First, get an input stream
			InputStream inputStream = getContext().getResources().getAssets().open(name);
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
			Log.v(TAG, "getChapterFromFile() has completed with fileName " + name);
			
			
			// finished reading the file with out getting the chapterText so close bufferedReader
		} catch (Exception e){
			e.printStackTrace();
		}
		
	}
	
	// TODO working here!
	private void setActiveTableToVocabTable(int vocabId){
		Log.v(TAG, "setActiveTableToVocabTable " + vocabId + " is beggining.");
		// Get cursor for vocab table
		Cursor vCursor = dbHelper.queryVocabTable(vocabId);
		
		// get the initial language from the prefs.
		SharedPreferences prefs = this.getContext()
				.getSharedPreferences(TAG , Context.MODE_PRIVATE);
		String lang = prefs.getString(VOCAB_LANGUAGE, "english");
		// Store the vocab from the cursor in an arrayList.
		ArrayList<String> vocab = new ArrayList<String>();
		// Need the right language.
		if (lang.equals(C_EWORD)){
			// Need the column index.
			int index = vCursor.getColumnIndex(C_EWORD);
			vCursor.moveToFirst();
			// add the first item then the rest
			vocab.add(vCursor.getString(index));
			while(vCursor.moveToNext()){
				vocab.add(vCursor.getString(index));
			}
		} else {
			// Need the column index.
			int index = vCursor.getColumnIndex(C_FWORD);
			vCursor.moveToFirst();
			// add the first item then the rest
			vocab.add(vCursor.getString(index));
			while(vCursor.moveToNext()){
				vocab.add(vCursor.getString(index));
			}			
		}
		// use dbHelper to clear the active table
		dbHelper.deleteActiveTable();
		// insert into Active Table the vocab using the right language column.
		for(String vocabWord: vocab){
			dbHelper.insertWordIntoActiveTable(vocabWord);
		}
		
		Log.v(TAG, "setActiveTableToVocabTable has completed.");
	}
	
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		return 0;
	}
	
	
	
	
	class DBHelper extends SQLiteOpenHelper{
		private static final String TAG = "SQLiteOpenHelper";
		private Context context;		
		
		
		public DBHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
			this.context = context;
		}


		/**
		 * Creates vocab and active tables.
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			String vocab = String.format("create table %s " +
							"(%s int primary key, %s int, %s text, %s text)",
							VOCAB_TABLE, C_ID, C_CHAPTER, C_EWORD, C_FWORD);
			String active = String.format("create table %s " +
					"(%s int primary key, %s text)",
					ACTIVE_TABLE, C_ID, C_AWORD);

			Log.v(TAG, "dbHelper onCreate with statement: " + vocab);
			Log.v(TAG, "dbHelper onCreate with with statement: " + active );
			db.execSQL(vocab);
			db.execSQL(active);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// usually ALTER TABLE statement
			db.execSQL("drop if exists " + VOCAB_TABLE);
			db.execSQL("drop if exists " + ACTIVE_TABLE);
			onCreate(db);
		}

		public void insertWordIntoActiveTable(String vocabWord) {
			ContentValues values = new ContentValues();
			values.put(C_AWORD, vocabWord);
			
			Long result = db.insertWithOnConflict(ACTIVE_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			if (result == -1){
				Log.e(TAG, "insertWordIntoActiveTable() db.insert returned an error code -1");
			}

			db.close();

		}
		
		// The following methods are for working with geting vocab from files and into data base.
		
		
		/**
		 * Qeuries the database for the chapter with chapNumber as an Id.
		 * @param chapNumber
		 * @return
		 */
		public Cursor queryVocabTable(int chapNumber){
			String whereClaus = C_CHAPTER + "=" +chapNumber;
			db = getReadableDatabase();
			Cursor cursor = db.query(VOCAB_TABLE, null, whereClaus, null, null, null, C_ID + " DESC");
			return cursor;
		}
		
		/**
		 * Qeuries the database for the chapter with chapNumber as an Id.
		 * @param chapNumber
		 * @return
		 */
		public Cursor queryActiveTable(int chapNumber){
			String whereClaus = C_CHAPTER + "=" +chapNumber;
			db = getReadableDatabase();
			Cursor cursor = db.query(ACTIVE_TABLE, null, null, null, null, null, null);
			return cursor;
		}
		
		public void deleteActiveTable(){
			db.execSQL("DELETE FROM " + ACTIVE_TABLE + ";");
			Log.d(TAG, "deleteActiveTable() finishing.");
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
		private void putVocabInVocabTable(String chapText, int chapNumber){
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
				insertVocabWordIntoVocabTable(vocabWord, chapNumber);
				
			}
			Log.v(TAG, "putChapterInDataBase() has put chapter in database as chapter number " + chapNumber );
		}
		
		
		private void insertVocabWordIntoVocabTable(VocabWord vocabWord, int chapNumber){
			db = getWritableDatabase();
			
			SharedPreferences prefs = context.getSharedPreferences(TAG , Context.MODE_PRIVATE);
			int vocabWordCount = prefs.getInt("vocabWordCount", 0);

			ContentValues values = new ContentValues();
			values.put(C_ID, vocabWordCount++);
			values.put(C_CHAPTER, chapNumber);
			values.put(C_EWORD, vocabWord.eWord);
			values.put(C_FWORD, vocabWord.fWord);
			
			Long result = db.insertWithOnConflict(VOCAB_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			if (result == -1){
				Log.e(TAG, "insertVocabWord() db.insert returned an error code -1");
			}

			prefs.edit().putInt("vocabWordCount", vocabWordCount);
			db.close();
		}
		
	}

}
