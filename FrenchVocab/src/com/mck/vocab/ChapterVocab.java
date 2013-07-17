package com.mck.vocab;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;
/**
 * 
 */

/**
 * @author Michael
 *
 */
@SuppressLint("UseSparseArrays")
public class ChapterVocab {
	private static final String TAG = "ChapterVocab";
	
	private HashMap<Integer, VocabWord> vocabList;
	public String currentLanguage;
	public int currentChapterNumber;
	private Context context;
	private DBHelper dbHelper;
	private SQLiteDatabase db;
	
	// a Vocabulary word is several things
	public class VocabWord {
		String eWord;
		String fWord;
		Integer id;
		String wordType;
		String currentLanguage;
	}

	/**
	 * initializes the ChapterVocab variables
	 *  -stores the context.
	 *  -creates the dbHelper
	 *  -sets the starting chapter to currentChapter.
	 *  -sets the current language
	 *  -initializes a vocabList
	 *  
	 * @param context
	 * @param currentLanguage
	 * @param currentChapter
	 */
	public ChapterVocab(Context context, String currentLanguage, int currentChapter){
		this.context = context;
		this.dbHelper = new DBHelper();
		this.currentChapterNumber = currentChapter; 
		this.currentLanguage = currentLanguage;
		// initialize the vocabulary list
		vocabList = new HashMap<Integer, VocabWord>();
		Log.v(TAG, "ChaptVocab constructor completed");
	}
	
	/**
	 *  -gets a cursor for the current chapter from the database
	 *  -fills the vocabList with the curser
	 *  -closes the cursor and returns.
	 */
	public void populate() {
		Log.v(TAG, "populate has begun");
		int listId = 0;
		Cursor cursor = dbHelper.queryDatabaseForChapter(currentChapterNumber);
		listId = cursor.getCount() - 1 ;
		vocabList.clear();
		while (cursor.moveToNext()){
			VocabWord vocabWord = new VocabWord();
			vocabWord.eWord = cursor.getString(cursor.getColumnIndex(DBHelper.C_EWORD));
			vocabWord.fWord = cursor.getString(cursor.getColumnIndex(DBHelper.C_FWORD));
			vocabWord.id = Integer.valueOf(listId--);
			vocabWord.wordType = "basic";
			vocabWord.currentLanguage = currentLanguage;
			vocabList.put(Integer.valueOf(vocabWord.id), vocabWord);
		}
		cursor.close();
	}
	
	/**
	 *  -clears the vocabList
	 *  -calls populate
	 */
	public void repopulate() {
		vocabList.clear();
		populate();
	}

	/**
	 * gets the ChapterVocab's vocabList
	 * @return the chapterVocab's vocabList
	 */
	public HashMap<Integer,VocabWord> getVocabList(){
		return vocabList;
	}
	
	/**
	 * Sets this.currentChapter to the currentChapter
	 * gets the string representation of the file with the chapter number
	 * calls dbHelper  put chapter in database
	 * @param currentChapter
	 */
	public void setCurrentChapter(int currentChapter) {
		this.currentChapterNumber = currentChapter;
		//String chapters = VocabListActivity.AVAILABLE_CHAPTERS[currentChapter-1];
		//putChapterInDatabase(chapters,currentChapter);
	}
	
	public void activateChapter(int chapterNumber){
		this.currentChapterNumber = chapterNumber;
		
		new AsyncTask<Integer,Integer, Integer>(){
			@Override
			protected Integer doInBackground(Integer... chapters) {
				int chapterNumber = chapters[0].intValue();
				String chapterTitle = VocabListActivity.AVAILABLE_CHAPTERS[chapterNumber-1];
				// put chapter in database
				putChapterInDatabase(chapterTitle,chapterNumber);
				// set current capter
				currentChapterNumber = Integer.valueOf(chapterNumber);
				// (re)populate the chapter
				repopulate();
				Log.v(TAG, "asynchTask doInBackGround() complete");
				return null;
			}
			@Override
			protected void onPostExecute(Integer result) {
				//super.onPostExecute(result);
				// get rid of the dialog
				Log.v(TAG, "asynchTask onPostExecute to relay task complete status to activity.");
				// tell activity the chapter is activated
				((VocabListActivity)context).onChapterActivated();
			}
			/* (non-Javadoc)
			 * @see android.os.AsyncTask#onPreExecute()
			 */
			@Override
			protected void onPreExecute() {
				// TODO sample chapter
				
				// want to fill the vocabList with a sample of the chapter data
				// then tell vocabActivity to reload the vocabListFragment.
				// once this happens then do the big load here.
				// this means that their may be a race condition between the loading
				// of the sample and the loading of the full thing. want the sample
				// to load first, or not at all if the list is busy. The finished
				// write should then be added.
				// The list should have synchronous access.
				
				// TODO do next.
				
				super.onPreExecute();
			}		
		}.execute(Integer.valueOf(chapterNumber));
	}
	
	public void putChapterInDatabase(String chapterTitle, int chapterNumber){
		String testFile  = dbHelper.getChapterFromFile(chapterTitle);
		dbHelper.putChapterInDatabase(testFile, chapterNumber);
		Log.v(TAG, "putChapterInDatabase() finished putting " + chapterTitle);
	}

	/**
	 * removes the word in position from the vocabList
	 * @param position
	 */
	public void removeVocabWord(int position) {
		HashMap<Integer,VocabWord> temp = this.vocabList;
		this.vocabList = new HashMap<Integer,VocabWord>();
		for(int x = 0; x < position; x++){
			this.vocabList.put(x, temp.get(x));
		}
		for(int x = position + 1; x < temp.size() ; x++){
			VocabWord w = temp.get(Integer.valueOf(x));
			w.id = Integer.valueOf(x-1);
			this.vocabList.put(w.id, w);
		}
		Log.v(TAG, "removeVocabWord() for position " + position + " is to begin");
	}

	
	
	class DBHelper extends SQLiteOpenHelper{

		public static final String DB_NAME = "vocab.db";
		public static final int DB_VERSION = 1;
		public static final String TABLE = "vocabword";
		public static final String C_ID = "_id";
		public static final String C_CHAPTER = "chapter";
		public static final String C_EWORD = "eword";
		public static final String C_FWORD = "fword";
		private static final String TAG = "SQLiteOpenHelper";

		public DBHelper() {
			super(context, DB_NAME, null, DB_VERSION);
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
		public Cursor queryDatabaseForChapter(int chapNumber){ // TODO maybe in the caller?
			String whereClaus = C_CHAPTER + "=" +chapNumber;
			db = dbHelper.getReadableDatabase();
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
			db = dbHelper.getWritableDatabase();
			
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



}

