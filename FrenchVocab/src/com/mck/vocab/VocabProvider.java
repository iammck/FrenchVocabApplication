/**
 * 
 */
package com.mck.vocab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

/**
 * @author Michael
 *
 */
@SuppressWarnings("deprecation")
public class VocabProvider extends ContentProvider {
	// Tag matches the class name
	public static final String TAG = "VocabProvider";
	
	public static final String AUTHORITY = "content://com.mck.vocab";
	public static final Uri CONTENT_URI = Uri.parse(AUTHORITY);

	// the database name and database version
	public static final String DB_NAME = "vocab.db";
	public static final int DB_VERSION = 1;
	// tables and table uri's
	public static final String VOCAB_TABLE = "vocabword";
	public static final Uri VOCAB_TABLE_URI = Uri.parse(AUTHORITY + "/" + VOCAB_TABLE);
	public static final String ACTIVE_TABLE = "activetable";
	public static final Uri ACTIVE_TABLE_URI = Uri.parse(AUTHORITY+ "/" + ACTIVE_TABLE);
	public static final String PREVIOUSLY_ACTIVE_TABLE = "previously_active";
	public static final Uri PREVIOUSLY_ACTIVE_TABLE_URI = Uri.parse(AUTHORITY+ "/" + PREVIOUSLY_ACTIVE_TABLE);
	public static final String TITLES_TABLE = "title";
	public static final Uri TITLES_TABLE_URI = Uri.parse(AUTHORITY+ "/" + TITLES_TABLE);
	// column names
	public static final String C_ID = "_id";
	public static final String C_VOCAB_NUMBER = "chapter";
	public static final String C_EWORD = "english";
	public static final String C_FWORD = "french";
	public static final String C_AWORD = "aword";
	public static final String C_TITLE = "title";
	// create table statements
	private static final String CREATE_VOCAB_TABLE_SQL_STATEMENT = String.format("create table %s " +
			"(%s int primary key, %s int, %s text, %s text)",
			VOCAB_TABLE, C_ID, C_VOCAB_NUMBER, C_EWORD, C_FWORD);
	private static final String CREATE_ACTIVE_TABLE_SQL_STATEMENT = String.format("create table %s " +
			"(%s int primary key, %s text)",
			ACTIVE_TABLE,C_ID, C_AWORD);
	private static final String CREATE_PREVIOUSLY_ACTIVE_TABLE_SQL_STATEMENT = String.format("create table %s " +
			"(%s int primary key,%s int, %s text)",
			PREVIOUSLY_ACTIVE_TABLE,C_ID, C_VOCAB_NUMBER, C_AWORD);
	private static final String CREATE_TITES_TABLE_SQL_STATEMENT = String.format("create table %s " +
			"(%s int primary key, %s text)",
			TITLES_TABLE,C_ID, C_TITLE);
	
	// update types
	public static final String UPDATE_TYPE_OPEN_VOCAB = "update_type_open_vocab";
	public static final String UPDATE_TYPE_RESET_VOCAB = "update_type_reset_vocab";
	public static final String UPDATE_TYPE_FLIP_ACTIVE_VOCAB_WORD = "update_type_flip_active_vocab_word";
	public static final String UPDATE_TYPE_VOCAB_LANGUAGE = "update_type_vocab_language";
	public static final String UPDATE_TYPE_REMOVE_ACTIVE_VOCAB_WORD = "update_type_remove_vocab_word";
	// the values found inside content values during an update() call.
	public static final String VALUES_UPDATE_TYPE = "update_type";
	public static final String VALUES_VOCAB_NAME = "vocab_name";
	public static final String VALUES_VOCAB_NUMBER = "vocab_number";
	public static final String VALUES_VOCAB_WORD_NUMBER = "values_vocab_word_numbr";
	public static final String VALUES_VOCAB_LANGUAGE = "vocab_language";
	// the values found in prefferences.
	public static final String PREFERENCES_IS_ENGLISH_ACTIVIE = "is_english_active";
	public static final String PREFERENCES_VOCAB_IN_TABLE = "preferences_vocab_in_table";
	public static final String PREFERENCES_VOCAB_IN_PREVIOUSLY_ACTIVE_TABLE = "preferences_vocab_in_previously_active_table";
	public static final String PREFERENCES_VOCAB_WORD_COUNT ="vocabWordCount";
	public static final String PREFERENCES_ACTIVE_VOCAB_NUMBER = "preferences_active_vocab_number";


	
	// the actual database and database helper.
	SQLiteDatabase db;
	DBHelper dbHelper;
	
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
		int wordCnt = prefs.getInt(VocabProvider.PREFERENCES_VOCAB_WORD_COUNT, -1);
		if (wordCnt == -1){// There is not so set one up.
			Log.v(TAG, "putting vocabWordCount in prefs and commiting.");
			prefs.edit().putInt(VocabProvider.PREFERENCES_VOCAB_WORD_COUNT, 0).commit();
		}
		// set initial language
		prefs.getBoolean(VocabProvider.PREFERENCES_IS_ENGLISH_ACTIVIE, true);
		
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
		Cursor cursor;
		String segment = uri.getLastPathSegment();
		// If the segment is to query the active table
		if (segment.equals(ACTIVE_TABLE)){
			Log.v(TAG, "A query has been made to the active table!");
			db = dbHelper.getReadableDatabase();
			cursor = db.query(ACTIVE_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
			// let any listeners know that an update happened.
			// I'm almost sure this should now match the passed in uri since that is what did work.
			// cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_URI);
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
			// to get to this if statement try something like
			//		return context.getContentResolver().query(vocabProvider.ACTIVE_TABLE-URI,
			//				null,null,null, vocabProvider.C_ID + " DESC");

			return cursor;
		}
		List<String> segments = uri.getPathSegments();
		if(segments.contains(TITLES_TABLE)){ // trying to get active table title?
			Log.v(TAG, "A query has been made for vocab title " + segment);

			// if just the active table
			if (segment.equals(TITLES_TABLE)){
				SharedPreferences prefs = getContext().getSharedPreferences(TAG , Context.MODE_PRIVATE);
				int vocabNumber = prefs.getInt(VocabProvider.PREFERENCES_ACTIVE_VOCAB_NUMBER, -1);
				if (vocabNumber == -1){
					Log.v(TAG,"attempting to query for active title, but none in preferences.");
					return null;
				}
				return dbHelper.queryTitleTableForVocabNumber(vocabNumber);
			} else {
				return dbHelper.queryTitleTableForVocabNumber(Integer.valueOf(segment).intValue());
			}
		}
		Log.v(TAG, "A query has been made for VocabWordNumber " + segment);
		//should be a vocab word number, query the db for number
		int vocabWordNumber = Integer.valueOf(segment).intValue();
		cursor = dbHelper.queryVocabTableForVocabWord(vocabWordNumber);
		return cursor;
	}

	/**
	 * Entry points for VocabListActivity
	 * This provider needs to handle getting methods for several requests by the activity, 
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
		
		Log.v(TAG,"Begining an " + reqType + " update in update()");		
		if(reqType != null && reqType.equals(UPDATE_TYPE_OPEN_VOCAB)){
			return this.openVocabAsActive(values);	
		}
		if(reqType != null && reqType.equals(UPDATE_TYPE_RESET_VOCAB)){
			// get prefs
			SharedPreferences prefs = this.getContext()
					.getSharedPreferences(TAG , Context.MODE_PRIVATE);
			// get preferences active table
			int vocabNumber = prefs.getInt(PREFERENCES_ACTIVE_VOCAB_NUMBER, -1);
			if (vocabNumber == -1){
				Log.v(TAG, "update request reset failed to get he active vocab number from prefs");
			}
			// set the active table to the vocab table and notify
			setActiveTableToVocabTable(vocabNumber);
			getContext().getContentResolver().notifyChange(CONTENT_URI, null);
			return 0;	
		} 
		if(reqType != null && reqType.equals(UPDATE_TYPE_VOCAB_LANGUAGE)){
			
			String vocabLanguage = values.getAsString(VALUES_VOCAB_LANGUAGE);
			// set the initial language from the prefs.
			SharedPreferences prefs = this.getContext()
					.getSharedPreferences(TAG , Context.MODE_PRIVATE);
			if(vocabLanguage.equals(C_FWORD)){
				prefs.edit().putBoolean(PREFERENCES_IS_ENGLISH_ACTIVIE, false).commit();	
			} else {
				prefs.edit().putBoolean(PREFERENCES_IS_ENGLISH_ACTIVIE, true).commit();	
			}
			setActiveTableToCurrentLanguage();
			return 0;	
		} // 
		if(reqType != null && reqType.equals(UPDATE_TYPE_FLIP_ACTIVE_VOCAB_WORD)){
			int vocabWordNumber = values.getAsInteger(VALUES_VOCAB_WORD_NUMBER);
			// get the row from the ActiveTable
			Cursor atCursor = dbHelper.queryActiveTableForVocabWord(vocabWordNumber);
			atCursor.moveToFirst();
			// get the row from the VocabTable
			Cursor vtCursor = dbHelper.queryVocabTableForVocabWord(vocabWordNumber);
			vtCursor.moveToFirst();
			// get the active word and the english word
			int  index = atCursor.getColumnIndex(C_AWORD);
			String activeLanguage = atCursor.getString(index);
			index = vtCursor.getColumnIndex(C_EWORD);
			String englishLanguage = vtCursor.getString(index);
			// is the active row word in english
			if (activeLanguage.equals(englishLanguage)){
				// get the french word from vocab
				index = vtCursor.getColumnIndex(C_FWORD);
				String frenchLanguage = vtCursor.getString(index);
				// update the active table with dbHelper method
				dbHelper.updateVocabWordInActiveTable(frenchLanguage, vocabWordNumber);
			} else {// else
				// use the english word from vocab
				dbHelper.updateVocabWordInActiveTable(englishLanguage, vocabWordNumber);
			}
			// notify
			getContext().getContentResolver().notifyChange(CONTENT_URI, null);
			return 0;	
		}		
		if(reqType != null && reqType.equals(UPDATE_TYPE_REMOVE_ACTIVE_VOCAB_WORD)){
			Integer vocabWordNumber = values.getAsInteger(VALUES_VOCAB_WORD_NUMBER);
			// get the new sample vocab into the active table and notify
			dbHelper.deleteVocabWordFromActiveTable(vocabWordNumber);
			getContext().getContentResolver().notifyChange(CONTENT_URI, null);
			return 0;	
		}
		Log.v(TAG, "update() complete");
		return 0;	
	}
	@SuppressLint("UseSparseArrays")
	private void setActiveTableToCurrentLanguage() {

		Log.v(TAG, "setActiveTableToCurrentLanguage has begun.");
		// Get the language from the prefs.
		SharedPreferences prefs = getContext().getSharedPreferences(TAG , Context.MODE_PRIVATE);
		boolean isEnglish = prefs.getBoolean(PREFERENCES_IS_ENGLISH_ACTIVIE, true);
		String vocabWordColumn = "";
		if(isEnglish){
			vocabWordColumn = C_EWORD;	
		} else {
			vocabWordColumn = C_FWORD;	
		}
		dbHelper.updateActiveTableToLanguage(vocabWordColumn);
		
	}

//		// get a cursor for active table
//		Cursor aCursor = dbHelper.queryActiveTable();
//		// if no active cursor, return
//		if (aCursor.getCount() <= 1){
//			Log.v(TAG, "setActiveTableToCurrentLanguage, but cursor says empty active table");
//			return;
//		}
//		// get the id's of the active vocab words
//		int avctiveIdColumnIndex = aCursor.getColumnIndex(C_ID);
//		ArrayList<Integer> activeIds = new ArrayList<Integer>();
//		aCursor.moveToFirst();
//		do {
//			activeIds.add(Integer.valueOf(aCursor.getInt(avctiveIdColumnIndex)));
//		} while (aCursor.moveToNext());
//		
//		HashMap<Integer,String> activeMap = new HashMap<Integer,String>();
//		int wordColumnIndex;
//		for(int wordNumber: activeIds){
//			Cursor vCursor = dbHelper.queryVocabTableForVocabWord(wordNumber);
//			vCursor.moveToFirst();
//			wordColumnIndex = vCursor.getColumnIndex(vocabWordColumn);
//			String word = vCursor.getString(wordColumnIndex);
//			activeMap.put(Integer.valueOf(wordNumber), word);			
//		}
//		// Having obtained the activeMap, set it as the active tabe
//		// Rewrite with sqliteStatement helper class.
//		// using the depreciated InsertHelper
//		InsertHelper iHelper;
//
//		// create and set up insert helper to make this a bulk insert
//		iHelper = new InsertHelper(db, VocabProvider.ACTIVE_TABLE);
//		int activeWordColumn = iHelper.getColumnIndex(C_AWORD);
//		int activeNumberColumn = iHelper.getColumnIndex(C_ID);
//		
//		// Begin the transaction
//		db.beginTransaction();
//		// for each element of the map
//		for(Integer wordNumber: activeMap.keySet()){
//			iHelper.prepareForInsert(); // Prepare for insert!
//			iHelper.bind(activeWordColumn, activeMap.get(wordNumber));
//			iHelper.bind(activeNumberColumn, wordNumber);
//			iHelper.execute();
//		}
//		// apply the batch transaction
//		db.setTransactionSuccessful();
//		db.endTransaction();
//
//		// notify of a change to the active table
//		getContext().getContentResolver().notifyChange(CONTENT_URI, null);


//	}
	private int openVocabAsActive(ContentValues values){
		// get what we need from values
		String vocabName = values.getAsString(VALUES_VOCAB_NAME);
		int vocabNumber = values.getAsInteger(VALUES_VOCAB_NUMBER);

		// Will use a flag when finding out if the vocabNumber is in the db.
		boolean isLoaded = false;
		// if the vocab is not already in the table
		SharedPreferences prefs = this.getContext().getSharedPreferences(TAG, Context.MODE_PRIVATE);
		String vocab = prefs.getString(VocabProvider.PREFERENCES_VOCAB_IN_TABLE, "");
		String[] vocabData = vocab.split(" ");
		for( int x = 1; x < vocabData.length; x++){ // skipping the first spot since will be blank.
			if (Integer.valueOf(vocabData[x]).intValue() == vocabNumber){
				// must have the vocab for the vocabNumber already.
				isLoaded = true;
				break;
			}
		}
		if (!isLoaded){ // if not loaded
			dbHelper.putFileIntoVocabTable(vocabName, vocabNumber);
			// now that the vocab is loaded, add the update to prefs.
			vocab += " " + String.valueOf(vocabNumber); // this is where the emty first cell is from
			prefs.edit().putString(PREFERENCES_VOCAB_IN_TABLE, vocab).commit();
		}
		// now set the active table to the vocabNumber
		setActiveTableToVocabNumber(vocabNumber);
		// notify users of the privider that the data has been changed.
		getContext().getContentResolver().notifyChange(CONTENT_URI, null);

		return 0;
	}
		
	private void setActiveTableToVocabNumber(int vocabNumber){
		// Will use a flag when finding out if the vocabNumber is in the db.
		boolean isPrevious = false;
		// if the vocab is not in the prevoius vocab table
		SharedPreferences prefs = getContext().getSharedPreferences(TAG, Context.MODE_PRIVATE);
		String vocab = prefs.getString(VocabProvider.PREFERENCES_VOCAB_IN_PREVIOUSLY_ACTIVE_TABLE, "");
		String[] vocabData = vocab.split(" ");
		for( int x = 1; x < vocabData.length; x++){ // skipping the first spot since will be blank.
			if (Integer.valueOf(vocabData[x]).intValue() == vocabNumber){
				// must have the vocab for the vocabNumber already.
				isPrevious = true;
				break;
			}
		}
		if (!isPrevious){ // if not loaded set from the vocab table
			setActiveTableToVocabTable(vocabNumber);
		} else { // set from the previously loaded table
			setActiveTableToPreviousVocabTable(vocabNumber);
		}
		// save the new active chapter in the shared prefs.
		prefs.edit().putInt(PREFERENCES_ACTIVE_VOCAB_NUMBER, vocabNumber).commit();
				
	}
	
	private void setActiveTableToVocabTable(int vocabId){
		Log.v(TAG, "setActiveTableToVocabTable " + vocabId + " is beggining.");
		// Get cursor for vocab table
		Cursor vCursor; //must do later? = dbHelper.queryVocabTable(vocabId);
		
		// get the initial language from the prefs.
		SharedPreferences prefs = this.getContext()
				.getSharedPreferences(TAG , Context.MODE_PRIVATE);
		boolean isEnglishActive = prefs.getBoolean(PREFERENCES_IS_ENGLISH_ACTIVIE, true);
		// use dbHelper to clear the active table 
		dbHelper.deleteActiveTableContent();
		vCursor = dbHelper.queryVocabTable(vocabId);
		if(vCursor.getCount()<1){
			return;
		}
		// Need  the index for the right language colum.
		int vocabWordColumn;
		if (isEnglishActive){
			vocabWordColumn = vCursor.getColumnIndex(C_EWORD);
		} else {
			vocabWordColumn = vCursor.getColumnIndex(C_FWORD);
		}
		int idColumn = vCursor.getColumnIndex(C_ID);			
		
		// Rewrite with sqliteStatement helper class.
		// using the depreciated InsertHelper
		InsertHelper iHelper;
					
		// add the first item then the rest
		vCursor.moveToFirst();
		String activeVocabWord = vCursor.getString(vocabWordColumn);
		String activeVocabWordNumber = vCursor.getString(idColumn);
		
		// create and set up insert helper to make this a bulk insert
		iHelper = new InsertHelper(db, VocabProvider.ACTIVE_TABLE);
		int activeWordColumn = iHelper.getColumnIndex(C_AWORD);
		int activeNumberColumn = iHelper.getColumnIndex(C_ID);
		// Begin the transaction
		db.beginTransaction();
		iHelper.prepareForInsert(); // Prepare for insert!
		iHelper.bind(activeWordColumn, activeVocabWord);
		iHelper.bind(activeNumberColumn, activeVocabWordNumber);
		iHelper.execute();
		//dbHelper.putVocabWordIntoActiveTable(vocabWord, vocabWordId);
		
		while(vCursor.moveToNext()){
			activeVocabWord = vCursor.getString(vocabWordColumn);
			activeVocabWordNumber = vCursor.getString(idColumn);
			iHelper.prepareForInsert();
			iHelper.bind(activeWordColumn, activeVocabWord);
			iHelper.bind(activeNumberColumn, activeVocabWordNumber);
			iHelper.execute();
		}
		// apply the batch transaction
		db.setTransactionSuccessful();
		db.endTransaction();
		
		/*
		if (isEnglishActive){
			// use dbHelper to clear the active table 
			dbHelper.deleteActiveTableContent();
			vCursor = dbHelper.queryVocabTable(vocabId);
			if(vCursor.getCount()<1){
				return;
			}
			// Need the column indexes.
			vocabWordcolumn = vCursor.getColumnIndex(C_EWORD);
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
		*/
		Log.v(TAG, "setActiveTableToVocabTable has completed.");
		
		
	}
	
	
	
	
	
	private void setActiveTableToPreviousVocabTable(int vocabId){
		Log.v(TAG, "setActiveTableToPreviousVocabTable " + vocabId + " is beggining.");
		// Get cursor for prvious vocab table
		Cursor pCursor;
		dbHelper.deleteActiveTableContent();
		pCursor = dbHelper.queryPreiouslyActiveTable(vocabId);
		if(pCursor.getCount()<1){
			return;
		}
		int vocabWordColumn = pCursor.getColumnIndex(C_AWORD);
		int idColumn = pCursor.getColumnIndex(C_ID);			
		
		// Rewrite with sqliteStatement helper class.
		// using the depreciated InsertHelper
		InsertHelper iHelper;
					
		// add the first item then the rest
		pCursor.moveToFirst();
		String activeVocabWord = pCursor.getString(vocabWordColumn);
		String activeVocabWordNumber = pCursor.getString(idColumn);
		
		// create and set up insert helper to make this a bulk insert
		iHelper = new InsertHelper(db, VocabProvider.ACTIVE_TABLE);
		int activeWordColumn = iHelper.getColumnIndex(C_AWORD);
		int activeNumberColumn = iHelper.getColumnIndex(C_ID);
		// Begin the transaction
		db.beginTransaction();
		iHelper.prepareForInsert(); // Prepare for insert!
		iHelper.bind(activeWordColumn, activeVocabWord);
		iHelper.bind(activeNumberColumn, activeVocabWordNumber);
		iHelper.execute();
		
		while(pCursor.moveToNext()){
			activeVocabWord = pCursor.getString(vocabWordColumn);
			activeVocabWordNumber = pCursor.getString(idColumn);
			iHelper.prepareForInsert();
			iHelper.bind(activeWordColumn, activeVocabWord);
			iHelper.bind(activeNumberColumn, activeVocabWordNumber);
			iHelper.execute();
		}
		// apply the batch transaction
		db.setTransactionSuccessful();
		db.endTransaction();
		Log.v(TAG, "setActiveTableToPreviousVocabTable has completed.");
	}
	
	
	
	
	
	
	
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		return 0;
	}
	
	
	
	
	class DBHelper extends SQLiteOpenHelper{
		private static final String TAG = "SQLiteOpenHelper";
		//private Context context;		
		
		
		public DBHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
			//this.context = context;
		}

		/**
		 * Creates vocab and active tables.
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.v(TAG, "dbHelper onCreate!!!!");
			db.execSQL(CREATE_VOCAB_TABLE_SQL_STATEMENT);
			db.execSQL(CREATE_ACTIVE_TABLE_SQL_STATEMENT);
			db.execSQL(CREATE_PREVIOUSLY_ACTIVE_TABLE_SQL_STATEMENT);
			db.execSQL(CREATE_TITES_TABLE_SQL_STATEMENT);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// usually ALTER TABLE statement
			db.execSQL("drop if exists " + VOCAB_TABLE);
			db.execSQL("drop if exists " + ACTIVE_TABLE);
			onCreate(db);
		}
		
//		/**
//		 * Puts a sing vocablulary word into the active table.
//		 * @param vocabWord the word as a string
//		 * @param vocabWordNumber the word id
//		 */
//		private void putVocabWordIntoActiveTable(String vocabWord, String vocabWordNumber) {
//			// we seem to  be getting the position in the table and not the c_id that we've want
//			db = getWritableDatabase();
//			ContentValues values = new ContentValues();
//			values.put(C_AWORD, vocabWord);
//			values.put(C_ID, Integer.valueOf(vocabWordNumber).intValue());
//			// if there is not one then make one?
//			Long result = db.insertWithOnConflict(ACTIVE_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
//			if (result == -1){
//				Log.e(TAG, "insertWordIntoActiveTable() db.insert returned an error code -1");
//			}
//			Log.v(TAG, "put " + vocabWord + " with id " + vocabWordNumber +" into active tabe");
//		}

//		/**
//		 * Puts a list of vocab into a vocab table giving them all the vocab number. 
//		 * data in the database.
//		 * @param vocab newline separated list of fWords and eWords, fWord first
//		 * @param vocabNumber vocabNumber to give to each item in vocab 
//		 */
//		private void putVocabIntoVocabTable(String vocab, int vocabNumber){
//			String eWord;
//			String fWord;
//			// split chapter text up by the new line character
//			String[] vocabData = vocab.split("\n");
//			Log.v(TAG,"putVocabInVocabTable() begining for loop to insert vocab into vocabtable");
//			// for each statement
//			for(int x = 0; x < vocabData.length; x += 2){
//				// collect the parts of a vocabWord, french word first
//				fWord = vocabData[x];
//				eWord = vocabData[x+1];
//				//db = getWritableDatabase();
//				SharedPreferences prefs = context.getSharedPreferences(TAG , Context.MODE_PRIVATE);
//				int vocabWordCount = prefs.getInt("vocabWordCount", 0);
//				ContentValues values = new ContentValues();
//				values.put(C_ID, vocabWordCount++);
//				values.put(C_VOCAB_NUMBER, vocabNumber);
//				values.put(C_EWORD, eWord);
//				values.put(C_FWORD, fWord);
//				Long result = putVocabWordIntoVocabTable(values);
//				if (result == -1){
//					Log.e(TAG, "putVocabStringInVocabTable() db.insert returned an error code -1");
//				}
//				prefs.edit().putInt("vocabWordCount", vocabWordCount).commit();
//			}
//			Log.v(TAG, "putVocabInVocabTable() has put vocab in vocab table as vocab number " + vocabNumber );
//		}
		
		
		private void putFileIntoVocabTable(String vocabName, int vocabNumber){
			// delete any vocab with that number already present in vocab table
			deleteVocabTableContent(vocabNumber);
			
			// Need the current word count.
			SharedPreferences prefs = getContext().getSharedPreferences(VocabProvider.TAG, Context.MODE_PRIVATE);
			int vocabWordCount = prefs.getInt(VocabProvider.PREFERENCES_VOCAB_WORD_COUNT, 0);
			// using the depreciated InsertHelper
			InsertHelper iHelper;
			
			
			InputStream inputStream;
			try {// First, get an input stream
				inputStream = getContext().getResources().getAssets().open(vocabName);
				// get the input stream reader
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "Cp1252");
				// get a buffered reader
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				// First two lines are the title
				String title = bufferedReader.readLine(); // first line says title
				title = bufferedReader.readLine(); // second line is the actual title.
				// now put the title in the title table using this vocabNumber.
				putTitleWithVocabNumberInTitleTable(title, vocabNumber);  
				db = getWritableDatabase();
				// create an insert helper to make this a bulk insert
				iHelper = new InsertHelper(db, VocabProvider.VOCAB_TABLE);
				int idColumn = iHelper.getColumnIndex(C_ID);
				int vocabNumberColumn = iHelper.getColumnIndex(VocabProvider.C_VOCAB_NUMBER);
				int eWordColumn = iHelper.getColumnIndex(C_EWORD);
				int	fWordColumn = iHelper.getColumnIndex(C_FWORD);
				// Second, go line by line and save into the right vocab table and active table
				String fWord, eWord;
				db.beginTransaction();
				// while has another line				
				while((fWord = bufferedReader.readLine()) != null){
					iHelper.prepareForInsert();
					
					// will also need the next line 
					eWord = bufferedReader.readLine();
					int vocabWordNumber = vocabWordCount++;

					iHelper.bind(idColumn, vocabWordNumber);
					iHelper.bind(vocabNumberColumn, vocabNumber);
					iHelper.bind(eWordColumn, eWord);
					iHelper.bind(fWordColumn, fWord);
					iHelper.execute();
				}

				// apply the batch transaction
				db.setTransactionSuccessful();
				db.endTransaction();
				
				// now that the vocab is loaded, add the updates to prefs.
				String vocab = prefs.getString(VocabProvider.PREFERENCES_VOCAB_IN_TABLE, "");
				vocab += " " + String.valueOf(vocabNumber); // this is where the emty first cell is from
				prefs.edit()
				.putString(PREFERENCES_VOCAB_IN_TABLE, vocab)
				.putInt(VocabProvider.PREFERENCES_VOCAB_WORD_COUNT, vocabWordCount)
				.commit();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
		
		
		private void putTitleWithVocabNumberInTitleTable(String title, int vocabNumber) {
			db = getWritableDatabase();
			
			String table =VocabProvider.TITLES_TABLE;
			ContentValues values = new ContentValues();
			values.put(C_TITLE, title);
			values.put(C_ID, Integer.valueOf(vocabNumber));
			int conflictAlgorithm = SQLiteDatabase.CONFLICT_REPLACE;
			Long result = db.insertWithOnConflict(table, null, values, conflictAlgorithm);
			if (result == -1){
				Log.e(TAG, "putTitleWithVocabNumberInTitleTable() db.insert returned an error code -1");
			}
		}

		private void putActiveTableIntoPreviouslyActiveTable(){
			Log.v(TAG, "putActiveTableIntoPreviouslyActiveTable is beggining.");
			// get a cursor to the active table
			Cursor aCursor = queryActiveTable();
			// if empty then don't do it!
			if (aCursor.getCount() < 1){
				Log.v(TAG, "empty active table, putActiveTableIntoReviouslyActiveTable has completed early.");
				return;
			}
			aCursor.moveToFirst();
			// get the column indexes
			int vocabWordColumn= aCursor.getColumnIndex(C_AWORD);
			int idColumn = aCursor.getColumnIndex(C_ID);			
			// previously active table also needs the vocabNumber.
			// add the first item then the rest, get the first row info too.
			int firstVocabWordNumber = aCursor.getInt(idColumn);
			
			
			// query the vocab table for the vocab table row
			Cursor vCursor = dbHelper.queryVocabTableForVocabWord(firstVocabWordNumber);
			vCursor.moveToFirst();
			int vocabNumberColumn= vCursor.getColumnIndex(C_VOCAB_NUMBER);
			int vocabNumber = vCursor.getInt(vocabNumberColumn); 
			// having the cursor and the chapter number, remove anything in the previously active table
			deletePreviouslyActiveTableContent(vocabNumber);
			// now do a bulk insert into the prev active table using the depreciated InsertHelper
			InsertHelper iHelper;
			// add the first item then the rest
			aCursor.moveToFirst();
			String activeVocabWord = aCursor.getString(vocabWordColumn);
			String activeVocabWordNumber = aCursor.getString(idColumn);
			// create and set up insert helper to make this a bulk insert
			iHelper = new InsertHelper(db, VocabProvider.PREVIOUSLY_ACTIVE_TABLE);
			int activeWordColumn = iHelper.getColumnIndex(C_AWORD);
			int activeNumberColumn = iHelper.getColumnIndex(C_ID);
			vocabNumberColumn = iHelper.getColumnIndex(C_VOCAB_NUMBER);
			
			// Begin the transaction
			db.beginTransaction();
			iHelper.prepareForInsert(); // Prepare for insert!
			iHelper.bind(activeWordColumn, activeVocabWord);
			iHelper.bind(activeNumberColumn, activeVocabWordNumber);
			iHelper.bind(vocabNumberColumn, vocabNumber);
			iHelper.execute();
			// keep going
			while(aCursor.moveToNext()){
				activeVocabWord = aCursor.getString(vocabWordColumn);
				activeVocabWordNumber = aCursor.getString(idColumn);
				iHelper.prepareForInsert();
				iHelper.bind(activeWordColumn, activeVocabWord);
				iHelper.bind(activeNumberColumn, activeVocabWordNumber);
				iHelper.bind(vocabNumberColumn, vocabNumber);
				iHelper.execute();
			}
			// apply the batch transaction
			db.setTransactionSuccessful();
			db.endTransaction();
			
			// now that the vocab is loaded, add the update to prefs, but only if needed
			SharedPreferences prefs = getContext().getSharedPreferences(VocabProvider.TAG, Context.MODE_PRIVATE);
			String vocab = prefs.getString(VocabProvider.PREFERENCES_VOCAB_IN_PREVIOUSLY_ACTIVE_TABLE, "");
			String[] vocabData = vocab.split(" ");
			boolean isLoaded = false;
			for( int x = 1; x < vocabData.length; x++){ // skipping the first spot since will be blank.
				if (Integer.valueOf(vocabData[x]).intValue() == vocabNumber){
					// must have the vocab for the vocabNumber already.
					isLoaded = true;
					break;
				}
			}
			if (isLoaded == false){
				vocab += " " + String.valueOf(vocabNumber); // this is where the emty first cell is from
				prefs.edit().putString(PREFERENCES_VOCAB_IN_PREVIOUSLY_ACTIVE_TABLE, vocab).commit();
			}
			Log.v(TAG, "putActiveTableIntoReviouslyActiveTable has completed.");
		}
		
		
//		/**
//		 * puts a vocab word into the vocab table. The vocabWord is expressed in a ContentValues.
//		 * @param values the values to add to the db. Uses CONFLICT_REPLACE
//		 * @return the result of the db. 
//		 */
//		private Long putVocabWordIntoVocabTable(ContentValues values){
//			db = getWritableDatabase();
//			return db.insertWithOnConflict(VOCAB_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
//		}

//		/**
//		 * Takes a string such as "parler\nto speak" and a number and put them in a ContentValues Object before returning.
//		 * @param vocab a string such as "Il faut\nOne must"
//		 * @param vocabNumber the number of the vocab to put the vocab word with
//		 * @return ContentValues containing the vocab word, its number, and the vocabNumber.
//		 */
//		private ContentValues getContentValuesFromStringAndVocabNumber(String vocab, int vocabNumber){
//			String eWord;
//			String fWord;
//			// split chapter text up by the new line character
//			String[] vocabData = vocab.split("\n");
//			fWord = vocabData[0];
//			eWord = vocabData[1];
//			SharedPreferences prefs = context.getSharedPreferences(TAG , Context.MODE_PRIVATE);
//			int vocabWordCount = prefs.getInt("vocabWordCount", 0);
//			ContentValues values = new ContentValues();
//			values.put(C_ID, vocabWordCount++);
//			values.put(C_VOCAB_NUMBER, vocabNumber);
//			values.put(C_EWORD, eWord);
//			values.put(C_FWORD, fWord);
//			Long result = putVocabWordIntoVocabTable(values);
//			if (result == -1){
//				Log.e(TAG, "getContentValuesFromStringAndVocabNumber() db.insert returned an error code -1");
//			}
//			prefs.edit().putInt("vocabWordCount", vocabWordCount).commit();
//			return values;
//		}

		/**
		 * updates the word associated with the number in the active table.
		 * @param vocabWord the word to use
		 * @param vocabWordNumber the number of the row to set.
		 */
		public int updateVocabWordInActiveTable(String vocabWord,int vocabWordNumber) {
			db = getWritableDatabase();
			
			String table =VocabProvider.ACTIVE_TABLE;
			ContentValues values = new ContentValues();
			values.put(C_AWORD, vocabWord);
			values.put(C_ID, Integer.valueOf(vocabWordNumber));
			String whereClause = C_ID + "=?";
			String[] whereArgs = {String.valueOf(vocabWordNumber)};
			int conflictAlgorithm = SQLiteDatabase.CONFLICT_IGNORE;
			return db.updateWithOnConflict(table, values, whereClause, whereArgs, conflictAlgorithm);
		}

		/**
		 * updates the active table to the passed in language. This method is slow.
		 * Need a faster way to batch query vocab tabe for all active ids.
		 * Consider also storing active boolean in each vocab table row. 
		 * @param vocabWordColumn
		 */
		@SuppressLint("UseSparseArrays")
		private void updateActiveTableToLanguage(String vocabWordColumn){
			// get a cursor for active table
			Cursor aCursor = queryActiveTable();
			// if no active cursor, return
			if (aCursor.getCount() <= 1){
				Log.v(TAG, "updateActiveTableToLanguage, but cursor says empty active table");
				return;
			}
			// array for ids from active table
			String[] wordNumbers = new String[aCursor.getCount()];
			// Get the first id into array 
			aCursor.moveToFirst();
			int activeIdColumnIndex = aCursor.getColumnIndex(C_ID);
			int wordNumber = aCursor.getInt(activeIdColumnIndex);
			wordNumbers[0] = String.valueOf(wordNumber);
			// array for words from the active table (gotten from vocab table)
			String[] words = new String[aCursor.getCount()];
			// get the first word from vocab table corresponding to idNumber
			Cursor vCursor = queryVocabTableForVocabWord(wordNumber);
			vCursor.moveToFirst();
			int vocabWordColumnIndex = vCursor.getColumnIndex(vocabWordColumn);
			words[0] = vCursor.getString(vocabWordColumnIndex);
			// get the rest of the words and ids.
			int counter = 1;
			while(aCursor.moveToNext()){
				// get a word numbers into array
				wordNumber = aCursor.getInt(activeIdColumnIndex);
				wordNumbers[counter] = String.valueOf(wordNumber);
				// get the word corresponding to the wordNumber into array
				vCursor = queryVocabTableForVocabWord(wordNumber);
				vCursor.moveToFirst();
				words[counter++] = vCursor.getString(vocabWordColumnIndex);
			}
			// Prepare for insert into actie table, clear the active table
			deleteActiveTableContent();
			InsertHelper iHelper;
			// create and set up insert helper to make this a bulk insert
			iHelper = new InsertHelper(db, VocabProvider.ACTIVE_TABLE);
			int activeWordColumn = iHelper.getColumnIndex(C_AWORD);
			int activeNumberColumn = iHelper.getColumnIndex(C_ID);
			// Begin the transaction
			db.beginTransaction();
			for(counter = 0; counter < wordNumbers.length; counter++){
				iHelper.prepareForInsert(); // Prepare for insert!
				iHelper.bind(activeWordColumn, words[counter]);
				iHelper.bind(activeNumberColumn, wordNumbers[counter]);
				iHelper.execute();				
			}// apply the batch transaction
			db.setTransactionSuccessful();
			db.endTransaction();
			// notify of a change to the active table
			getContext().getContentResolver().notifyChange(CONTENT_URI, null);
			Log.v(TAG, "updateActiveTableToLanguage has updated the active language to "
											+ vocabWordColumn);
		}
		
		/**
		 * Qeuries the database for the chapter with chapNumber as an Id.
		 * @param chapNumber
		 * @return
		 */
		public Cursor queryVocabTable(int chapNumber){
			String whereClaus = C_VOCAB_NUMBER + "=" +chapNumber;
			db = getWritableDatabase();
			Cursor cursor = db.query(VOCAB_TABLE, null, whereClaus, null, null, null, C_ID + " DESC");
			return cursor;
		}
		
		/**
		 * Qeuries the database for the chapter with chapNumber as an Id.
		 * @param chapNumber
		 * @return
		 */
		public Cursor queryPreiouslyActiveTable(int chapNumber){
			String whereClaus = C_VOCAB_NUMBER + "=" +chapNumber;
			db = getWritableDatabase();
			Cursor cursor = db.query(PREVIOUSLY_ACTIVE_TABLE, null, whereClaus, null, null, null, C_ID + " DESC");
			return cursor;
		}
		
		/**
		 * Returns a cursor for the row with the vocabNumber and vocabWordNumber.
		 * @param vocabNumber the number of the vocab item to pull the vocab word from.
		 * @param vocabWordNumber the number of the vocab word to retrieve.
		 * @return the cursor for the vocabWord
		 */
		public Cursor queryVocabTableForVocabWord(int vocabNumber, int vocabWordNumber){
			return queryVocabTableForVocabWord(vocabWordNumber);
		}
		
		/**
		 * Returns a cursor for the row with the vocabWordNumber
		 * @param vocabWordNumber the number of the vocab word to retrieve.
		 * @return the cursor for the vocabWord
		 */
		public Cursor queryVocabTableForVocabWord(int vocabWordNumber){
			Log.v(TAG, "query vocab table for vocabWordNumber "+ vocabWordNumber);
			db = getWritableDatabase();
			
			String[] columns = null; // want all columns for the row
			String selection = VocabProvider.C_ID + " = " + String.valueOf(vocabWordNumber);
			String[] selectionArgs = null; // {String.valueOf(vocabWordNumber)};
			String groupBy = null;
			String having = null;
			String sortBy = C_ID + " ASC";
			
			Cursor cursor = db.query(VocabProvider.VOCAB_TABLE, columns, selection, selectionArgs, groupBy, having, sortBy);
			return cursor;
		}
		
		
		
		
		/**
		 * Returns a cursor for the row with the vocabWordNumber
		 * @param vocabWordNumber the number of the vocab word to retrieve.
		 * @return the cursor for the vocabWord
		 */
		public Cursor queryActiveTableForVocabWord(int vocabWordNumber){
			Log.v(TAG, "query active table for vocabWordNumber "+ vocabWordNumber);
			db = getWritableDatabase();
			String[] columns = null; // want all columns for the row
			String selection = " " + VocabProvider.C_ID + " = " + String.valueOf(vocabWordNumber);
			String[] selectionArgs = null;//{String.valueOf(vocabWordNumber)};
			String groupBy = null;
			String having = null;
			String sortBy =  C_ID + " ASC";
			Cursor cursor = db.query(VocabProvider.ACTIVE_TABLE, columns, selection, selectionArgs, groupBy, having, sortBy);
			return cursor;
		}

		/**
		 * Returns a cursor for the row with the vocabWordNumber
		 * @param vocabNumber the number of the vocab word to retrieve.
		 * @return the cursor for the vocabWord
		 */
		public Cursor queryTitleTableForVocabNumber(int vocabNumber){
			Log.v(TAG, "queryTitleTableForVocabNumber for vocabNumber "+ vocabNumber);
			db = getWritableDatabase();
			String[] columns = null; // want all columns for the row
			String selection = " " + VocabProvider.C_ID + " = " + String.valueOf(vocabNumber);
			String[] selectionArgs = null;//{String.valueOf(vocabWordNumber)};
			String groupBy = null;
			String having = null;
			String sortBy =  C_ID + " ASC";
			Cursor cursor = db.query(VocabProvider.TITLES_TABLE, columns, selection, selectionArgs, groupBy, having, sortBy);
			Log.v(TAG, "queryTitleTableForVocabNumber found " + cursor.getCount() + " titles.");
			return cursor;
		}

		
		/**
		 * NOT WORKING :/
		 * Returns a cursor for the rowS with the vocabWordNumberS
		 * @param vocabWordNumber the number of the vocab word to retrieve.
		 * @return the cursor for the vocabWord
		 */
		public Cursor queryVocabTableForVocabWords(String[] vocabWordNumbers){
			Log.v(TAG, "query active table for vocabWordNumbers "+ vocabWordNumbers);
			String orStatement = "";
			for(int x= 0; x < vocabWordNumbers.length - 1;x++){
				orStatement += vocabWordNumbers[x] + " OR ";
			}
			if (vocabWordNumbers.length > 0 ){
				orStatement += vocabWordNumbers[vocabWordNumbers.length - 1];
			}
			
			db = getWritableDatabase();
			String[] columns = null; // want all columns for the row
			String selection = " " + VocabProvider.C_ID + " = " +  orStatement ;
			String[] selectionArgs = null; // = vocabWordNumbers;//{String.valueOf(vocabWordNumber)};
			String groupBy = null;
			String having = null;
			String sortBy =  C_ID + " ASC";
			Cursor cursor = db.query(VocabProvider.VOCAB_TABLE, columns, selection, selectionArgs, groupBy, having, sortBy);

			return cursor;
		}
		
		
		
		
		/**
		 * Qeuries the database for the chapter with chapNumber as an Id.
		 * @param chapNumber
		 * @return the cursor for the Active table
		 */
		public Cursor queryActiveTable(){
			db = getWritableDatabase();
			Cursor cursor = db.query(ACTIVE_TABLE, null, null, null, null, null, C_ID + " ASC");
			
			return cursor;
		}
		
		/**
		 * Adds previously deleted lists to the previously active table then
		 * deletes the elements in the table.
		 */
		private void deleteActiveTableContent(){
			putActiveTableIntoPreviouslyActiveTable();
			db = getWritableDatabase();
			db.delete(ACTIVE_TABLE, "1" , null);
		}
		
		/**
		 * deletes the contents of a vocab item from the vocabTable. the contents
		 * of a vocab item are those that share the same vocabNumber.
		 * @param vocabNumber the set of vocabWord to delete from table
		 */
		private void deleteVocabTableContent(Integer vocabNumber){
			String[] removeIds = {vocabNumber.toString()};
			db = getWritableDatabase();// it might be the number of databases.
			int d = db.delete(VOCAB_TABLE, VocabProvider.C_VOCAB_NUMBER + "=?",  removeIds);
			Log.v(TAG, String.valueOf(d) + " items removed from the active table");
		}

		/**
		 * deletes rows from the previously active table with the vocabNumber
		 * @param vocabNumber the set of vocabWord to delete from table
		 */
		private void deletePreviouslyActiveTableContent(int vocabNumber) {
			String[] removeIds = {String.valueOf(vocabNumber)};
			db = getWritableDatabase();// it might be the number of databases.
			int d = db.delete(PREVIOUSLY_ACTIVE_TABLE, VocabProvider.C_VOCAB_NUMBER + "=?",  removeIds);
			Log.v(TAG, String.valueOf(d) + " items removed from the previously active table");
		}

				/**
		 * deletes a vocabWord from the active table, given the vocabWordNumber
		 * @param vocabWordNumber the number associated with the vocabWord to delete from table
		 */
		private void deleteVocabWordFromActiveTable(Integer vocabWordNumber) {
			Log.v(TAG, "attempting to remove word id number "+ vocabWordNumber +"from active table");
			String[] removeIds = {String.valueOf(vocabWordNumber)};
			db = getWritableDatabase();// it might be the number of databases.
			int d = db.delete(ACTIVE_TABLE, VocabProvider.C_ID + "=?",  removeIds);
			Log.v(TAG, String.valueOf(d) + " items removed from the active table");
		}
	}
}
