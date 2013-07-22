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
import android.os.AsyncTask;
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
	public static final String VOCAB_LANGUAGE = "vocab_language";
	
	public static final String C_ID = "_id";
	public static final String C_CHAPTER = "chapter";
	public static final String C_EWORD = "english";
	public static final String C_FWORD = "french";
	
	public static final String ACTIVE_TABLE = "activetable";
	public static final String C_AWORD = "aword";

	public static final String UPDATE_TYPE_OPEN_VOCAB = "update_type_open_vocab";
	public static final String UPDATE_TYPE_RESET_VOCAB = "update_type_reset_vocab";
	public static final String UPDATE_TYPE_FLIP_ACTIVE_VOCAB_WORD = "update_type_flip_active_vocab_word";
	public static final String UPDATE_TYPE_VOCAB_LANGUAGE = "update_type_vocab_language";
	public static final String UPDATE_TYPE_REMOVE_VOCAB_WORD = "update_type_remove_vocab_word";

	public static final String UPDATE_OPEN_VOCAB_NAME = "update_open_vocab_name";

	public static final String VALUES_UPDATE_TYPE = "update_type";
	public static final String VALUES_VOCAB_NAME = "vocab_name";
	public static final String VALUES_VOCAB_NUMBER = "vocab_number";
	public static final String VALUES_VOCAB_WORD_NUMBER = "values_vocab_word_numbr";
	public static final String VALUES_VOCAB_LANGUAGE = "vocab_language";

	public static final int SAMPLE_SIZE = 10;
	private static final String CREATE_VOCAB_TABLE_SQL_STATEMENT = String.format("create table %s " +
			"(%s int primary key, %s int, %s text, %s text)",
			VOCAB_TABLE, C_ID, C_CHAPTER, C_EWORD, C_FWORD);
	private static final String CREATE_ACTIVE_TABLE_SQL_STATEMENT = String.format("create table %s " +
			"(%s int primary key, %s text)",
			ACTIVE_TABLE,C_ID, C_AWORD);



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
		vocabLanguage = prefs.getString(VOCAB_LANGUAGE, VocabProvider.C_EWORD);
		
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
		
		cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_URI);
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
		return uri;		
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		String reqType = (String) values.get(VALUES_UPDATE_TYPE);
		
		Log.v(TAG,"begining an " + reqType + " update in update()");		
		if(reqType != null && reqType.equals(UPDATE_TYPE_OPEN_VOCAB)){
			return this.openVocabAsActive(values);	
		}
		if(reqType != null && reqType.equals(UPDATE_TYPE_RESET_VOCAB)){
			int vocabNumber = values.getAsInteger(VALUES_VOCAB_NUMBER);
			// get the new sample vocab into the active table and notify
			setActiveTableToVocabTable(vocabNumber);
			getContext().getContentResolver().notifyChange(CONTENT_URI, null);
			return 0;	
		} 
		if(reqType != null && reqType.equals(UPDATE_TYPE_VOCAB_LANGUAGE)){
			String vocabLanguage = values.getAsString(VALUES_VOCAB_LANGUAGE);
			// set the initial language from the prefs.
			SharedPreferences prefs = this.getContext()
					.getSharedPreferences(TAG , Context.MODE_PRIVATE);
			prefs.edit().putString(VOCAB_LANGUAGE, vocabLanguage).commit();
			// get the active vocab number from prefs
			int vocabNumber = prefs.getInt(VALUES_VOCAB_NUMBER, 1);
			// get the vocab into the active table and notify
			setActiveTableToVocabTable(vocabNumber);
			getContext().getContentResolver().notifyChange(CONTENT_URI, null);
			return 0;	
		} // TODO
		if(reqType != null && reqType.equals(UPDATE_TYPE_FLIP_ACTIVE_VOCAB_WORD)){
			int vocabWordNumber = values.getAsInteger(VALUES_VOCAB_WORD_NUMBER);
			// get the new sample vocab into the active table and notify
			setActiveTableToVocabTable(vocabWordNumber);
			getContext().getContentResolver().notifyChange(CONTENT_URI, null);
			return 0;	
		}		
		if(reqType != null && reqType.equals(UPDATE_TYPE_REMOVE_VOCAB_WORD)){
			int vocabWordNumber = values.getAsInteger(VALUES_VOCAB_WORD_NUMBER);
			// get the new sample vocab into the active table and notify
			dbHelper.deleteVocabWordFromActiveTable(vocabWordNumber);
			getContext().getContentResolver().notifyChange(CONTENT_URI, null);
			return 0;	
		}
		Log.v(TAG, "update() complete");
		return 0;	
	}
	private int openVocabAsActive(ContentValues values){
		// get the sample items from the file into the vocab table
		String vocabName = values.getAsString(VALUES_VOCAB_NAME);
		int vocabNumber = values.getAsInteger(VALUES_VOCAB_NUMBER);
		// and save them
		SharedPreferences prefs = this.getContext()
				.getSharedPreferences(TAG , Context.MODE_PRIVATE);
		prefs.edit().putString(VALUES_VOCAB_NAME, vocabName)
			.putInt(VALUES_VOCAB_NUMBER, vocabNumber).commit();

		// TODO if need to reload vocab table.
		// get some sample data
		getSampleVocabTableFromFile(vocabName, vocabNumber);
		
		// get the new sample vocab into the active table and notify
		setActiveTableToVocabTable(vocabNumber);
		getContext().getContentResolver().notifyChange(CONTENT_URI, null);
		
		new AsyncTask<String, Integer, Integer>(){
			@Override
			protected Integer doInBackground(String... arg0) {
				// get the full vocab into the active table and notify
				String vocabName = arg0[0];
				Integer vocabNumber = Integer.valueOf(arg0[1]);
				dbHelper.deleteVocabTableContent(vocabNumber);
				getVocabTableFromFile(vocabName, vocabNumber);
				return vocabNumber;
			}

			@Override
			protected void onPostExecute(Integer vocabNumber) {
				super.onPostExecute(vocabNumber);
				setActiveTableToVocabTable(vocabNumber.intValue());
				getContext().getContentResolver().notifyChange(CONTENT_URI,
						null);
				
			}
		
		}.execute(vocabName,Integer.valueOf(vocabNumber).toString());
		return 0;	
	}
	private void getSampleVocabTableFromFile(String vocabName, int vocabNumber) {
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
			Log.v(TAG, "getSampleVocabTableFromFile() has completed with vocab name " + vocabName
					+ " and number " + vocabNumber);
			Log.v(TAG, "getSampleVocabTableFromFile() has result: " + result.toString());
		} catch (Exception e){
			e.printStackTrace();
		}
		// now that the file is in memory, put it in the right table
		dbHelper.putVocabIntoVocabTable(result, vocabNumber);	
	}
	

	private void getVocabTableFromFile(String vocabName, int vocabNumber){
		String result = "";

		try{
			// First, get an input stream
			InputStream inputStream = getContext().getResources().getAssets().open(vocabName);
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
			Log.v(TAG, "getVocabTableFromFile() has completed with fileName " + vocabName);
			Log.v(TAG, "getSampleVocabTableFromFile() has completed with vocab name " + vocabName
					+ " and number " + vocabNumber);
			Log.v(TAG, "getSampleVocabTableFromFile() has result: " + result.toString());

			// finished reading the file with out getting the chapterText so close bufferedReader
		} catch (Exception e){
			e.printStackTrace();
		}
		// now that the file is in memory, put it in the right table
		dbHelper.putVocabIntoVocabTable(result, vocabNumber);	

	}
	

	private void setActiveTableToVocabTable(int vocabId){
		Log.v(TAG, "setActiveTableToVocabTable " + vocabId + " is beggining.");
		// Get cursor for vocab table
		Cursor vCursor; //must do later? = dbHelper.queryVocabTable(vocabId);
		
		// get the initial language from the prefs.
		SharedPreferences prefs = this.getContext()
				.getSharedPreferences(TAG , Context.MODE_PRIVATE);
		String lang = prefs.getString(VOCAB_LANGUAGE, VocabProvider.C_EWORD);
		// Store the vocab from the cursor in an arrayList.
		ArrayList<String> vocab = new ArrayList<String>();
		
		// Need the right language.
		if (lang.equals(C_EWORD)){
			// use dbHelper to clear the active table 
			dbHelper.deleteActiveTableContent();
			vCursor = dbHelper.queryVocabTable(vocabId);
			if(vCursor.getCount()<1){
				return;
			}
			// Need the column indexes.
			int vIndex = vCursor.getColumnIndex(C_EWORD);
			int idIndex = vCursor.getColumnIndex(C_ID);			
			// add the first item then the rest
			vCursor.moveToFirst();
			String vocabWord = vCursor.getString(vIndex);
			String vocabWordId = vCursor.getString(idIndex);
			dbHelper.putVocabWordIntoActiveTable(vocabWord, vocabWordId);
			while(vCursor.moveToNext()){
				vocabWord = vCursor.getString(vIndex);
				vocabWordId = vCursor.getString(idIndex);
				dbHelper.putVocabWordIntoActiveTable(vocabWord, vocabWordId);
			}
		} else {
			// use dbHelper to clear the active table
			dbHelper.deleteActiveTableContent();			
			vCursor = dbHelper.queryVocabTable(vocabId);
			if(vCursor.getCount()<1){ // if empty do nothing
				return;
			}
			// Need the column indexes.
			int vIndex = vCursor.getColumnIndex(C_FWORD);
			int idIndex = vCursor.getColumnIndex(C_ID);			
			// add the first item then the rest
			vCursor.moveToFirst();
			String vocabWord = vCursor.getString(vIndex);
			String vocabWordId = vCursor.getString(idIndex);
			dbHelper.putVocabWordIntoActiveTable(vocabWord, vocabWordId);
			while(vCursor.moveToNext()){
				vocabWord = vCursor.getString(vIndex);
				vocabWordId = vCursor.getString(idIndex);
				dbHelper.putVocabWordIntoActiveTable(vocabWord, vocabWordId);
			}
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
			Log.v(TAG, "dbHelper onCreate with statement: " + CREATE_VOCAB_TABLE_SQL_STATEMENT);
			Log.v(TAG, "dbHelper onCreate with with statement: " + CREATE_ACTIVE_TABLE_SQL_STATEMENT );
			db.execSQL(CREATE_VOCAB_TABLE_SQL_STATEMENT);
			db.execSQL(CREATE_ACTIVE_TABLE_SQL_STATEMENT);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// usually ALTER TABLE statement
			db.execSQL("drop if exists " + VOCAB_TABLE);
			db.execSQL("drop if exists " + ACTIVE_TABLE);
			onCreate(db);
		}
		
		/**
		 * Qeuries the database for the chapter with chapNumber as an Id.
		 * @param chapNumber
		 * @return
		 */
		public Cursor queryVocabTable(int chapNumber){
			String whereClaus = C_CHAPTER + "=" +chapNumber;
			db = getWritableDatabase();
			Cursor cursor = db.query(VOCAB_TABLE, null, whereClaus, null, null, null, C_ID + " DESC");
			return cursor;
		}
		
		/**
		 * Qeuries the database for the chapter with chapNumber as an Id.
		 * @param chapNumber
		 * @return
		 */
		public Cursor queryActiveTable(){
			db = getWritableDatabase();
			Cursor cursor = db.query(ACTIVE_TABLE, null, null, null, null, null, null);
			
			return cursor;
		}
		
		public void deleteActiveTableContent(){
			db = getWritableDatabase();
			db.delete(ACTIVE_TABLE, "1" , null);
		}
		
		public void deleteVocabTableContent(Integer vocabNumber){
			String[] removeIds = {vocabNumber.toString()};
			db = getWritableDatabase();// it might be the number of databases.
			int d = db.delete(VOCAB_TABLE, VocabProvider.C_CHAPTER + "=?",  removeIds);
			Log.v(TAG, String.valueOf(d) + " items removed from the active table");
		}

		private void deleteVocabWordFromActiveTable(int vocabWordNumber) {
			String[] removeIds = {String.valueOf(vocabWordNumber)};
			db = getWritableDatabase();// it might be the number of databases.
			int d = db.delete(ACTIVE_TABLE, VocabProvider.C_ID + "=?",  removeIds);
			Log.v(TAG, String.valueOf(d) + " items removed from the active table");
		}
		
		/**
		 * Puts a sing vocablulary word into the active table.
		 * @param vocabWord the word as a string
		 * @param vocabWordId the word id
		 */
		private void putVocabWordIntoActiveTable(String vocabWord, String vocabWordId) {
			db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(C_AWORD, vocabWord);
			values.put(C_ID, Integer.valueOf(vocabWordId).intValue());
			// if there is not one then make one?
			Long result = db.insertWithOnConflict(ACTIVE_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			if (result == -1){
				Log.e(TAG, "insertWordIntoActiveTable() db.insert returned an error code -1");
			}
		}

		
		/**
		 * Puts a list of vocab into a vocab table giving them all the vocab number. 
		 * data in the database.
		 * @param vocab newline separated list of fWords and eWords, fWord first
		 * @param vocabNumber vocabNumber to give to each item in vocab 
		 */
		private void putVocabIntoVocabTable(String vocab, int vocabNumber){
			String eWord;
			String fWord;
			// split chapter text up by the new line character
			String[] vocabData = vocab.split("\n");
			Log.v(TAG,"putVocabInVocabTable() begining for loop to insert vocab into vocabtable");
			// for each statement
			for(int x = 0; x < vocabData.length; x += 2){
				// collect the parts of a vocabWord, french word first
				fWord = vocabData[x];
				eWord = vocabData[x+1];
				//db = getWritableDatabase();
				SharedPreferences prefs = context.getSharedPreferences(TAG , Context.MODE_PRIVATE);
				int vocabWordCount = prefs.getInt("vocabWordCount", 0);
				ContentValues values = new ContentValues();
				values.put(C_ID, vocabWordCount++);
				values.put(C_CHAPTER, vocabNumber);
				values.put(C_EWORD, eWord);
				values.put(C_FWORD, fWord);
				Long result = putVocabWordIntoVocabTable(values);
				if (result == -1){
					Log.e(TAG, "putVocabStringInVocabTable() db.insert returned an error code -1");
				}
				prefs.edit().putInt("vocabWordCount", vocabWordCount).commit();
			}
			Log.v(TAG, "putVocabInVocabTable() has put vocab in vocab table as vocab number " + vocabNumber );
		}
		
		private Long putVocabWordIntoVocabTable(ContentValues values){
			db = getWritableDatabase();
			return db.insertWithOnConflict(VOCAB_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
		}
	}
}
