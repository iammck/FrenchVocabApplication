package com.mck.vocab;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.mck.vocab.ChangeLanguageDialogFragment.ChangeLanguageCallback;

public class VocabListActivity extends ActionBarActivity implements 
	ChangeLanguageCallback,OnSharedPreferenceChangeListener, LoaderCallbacks<Cursor>,
	EasyDialogAnswerFragment.EasyDialogFragmentCallback
	{
	
	


	private static final String TAG = "VocabListActivity";
	public static final int vocabCursorLoaderId = 0;
	public String[] AVAILABLE_VOCAB; 
	SharedPreferences prefs;
	
	private DrawerLayout drawerLayout;
	private ListView drawerList;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG,"onCreate() has begun");
        //Log.v(TAG,"debug is on");
        //Debug.startMethodTracing();

		
        
        AVAILABLE_VOCAB = getResources().getStringArray(R.array.available_vocab_names);
        
        // create a content provider at least once?
		getContentResolver().update(VocabProvider.CONTENT_URI, new ContentValues(), null, null);

        this.getSupportLoaderManager().initLoader(vocabCursorLoaderId, null, this);
        // try to get the vocabListFragment 
        VocabListFragment vocabListFragment =
        		(VocabListFragment) getSupportFragmentManager().findFragmentById(R.id.vocab_list_frag_container);
        // if there is no vocabListFragment then make a new one and attach it to view via frag manager
        if (vocabListFragment == null){
        	vocabListFragment = new VocabListFragment();
        	this.getSupportFragmentManager()
        		.beginTransaction()
        		.add(R.id.vocab_list_frag_container, vocabListFragment, vocabListFragment.getTag())
        		.commit();
        	Log.v(TAG, "created and added a new VocabListFragment to FragmentManager");
        }
        // register to get changes to the prefs.
        prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		prefs.registerOnSharedPreferenceChangeListener(this);
        setContentView(R.layout.activity_vocab_list);
        
     // get the drawer list and layout.
        String[] rowTitles =	getResources().getStringArray(R.array.drawer_row_titles);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);
        // set the list view adapter
        drawerList.setAdapter(new ArrayAdapter<String>(this, 
        		android.R.layout.simple_list_item_1, rowTitles));
		// set the list's clickListener
        drawerList.setOnItemClickListener(new DrawerItemClickListener());

    }
    
    



	public class DrawerItemClickListener implements ListView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			Log.v(TAG, "drawerItemClickListener onItemClick is starting");
		}

	}
    

    @Override
	protected void onStop() {
		super.onStop();
		Log.v(TAG,"debug is off");
       //Debug.stopMethodTracing();
	}


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.vocab_list, menu);
        return true;
    }


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
		case R.id.options_item_more_vocab:
			Log.v(TAG, "options item preferences selected");
			startActivity(new Intent(this, PrefsActivity.class));
			//do stuff then return true
			return true;		
		case R.id.options_item_restart:
			Log.v(TAG, "options item restart selected");
			restartVocabList();
			return true;
			
		case R.id.options_item_language:
			Log.v(TAG, "options item language selected");
			FragmentManager fragMan= getSupportFragmentManager();
			ChangeLanguageDialogFragment frag = new ChangeLanguageDialogFragment();
			frag.show(fragMan, ChangeLanguageDialogFragment.TAG);
			return true;
		case R.id.options_item_start:	
			Log.v(TAG, "options item start selected");
			startDialogSequence();
		default:
			return false;
		}
	}
	
	

	public void restartVocabList(){
		Log.v(TAG, "restartVocabList has begun");
		// get the right values for content values
		ContentValues values = new ContentValues();
		// file vocabName as name of file and current chapter
		String currentChapter = prefs.getString("current_chapter", "1");
		values.put(VocabProvider.VALUES_UPDATE_TYPE, VocabProvider.UPDATE_TYPE_RESET_VOCAB);
		values.put(VocabProvider.VALUES_VOCAB_NUMBER, currentChapter);
		// get the content provider and update
		getContentResolver().update(VocabProvider.CONTENT_URI, values, null, null);
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.v(TAG, "onSharedPreferenceChanged method has begun with key " + key);
		// if the key value is for the langue then change the chapterVocab laguage
		if (key.equals("language")){
			String currentLanguage = prefs.getString("language", "english");
			
			ContentValues values = new ContentValues();
			// file vocabName as name of file and current chapter
			values.put(VocabProvider.VALUES_UPDATE_TYPE, VocabProvider.UPDATE_TYPE_VOCAB_LANGUAGE);
			values.put(VocabProvider.VALUES_VOCAB_LANGUAGE, currentLanguage);
			// get the content provider and update
			getContentResolver().update(VocabProvider.CONTENT_URI, values, null, null);			
		}
		if (key.equals("current_chapter")){
			// get the right values for content values
			ContentValues values = new ContentValues();
			// file vocabName as name of file and current chapter
			String currentChapter = prefs.getString("current_chapter", "1");
			String vocabName = AVAILABLE_VOCAB[Integer.valueOf(currentChapter).intValue() - 1];
			values.put(VocabProvider.VALUES_UPDATE_TYPE, VocabProvider.UPDATE_TYPE_OPEN_VOCAB);
			values.put(VocabProvider.VALUES_VOCAB_NAME, vocabName);
			values.put(VocabProvider.VALUES_VOCAB_NUMBER, currentChapter);
			// get the content provider and update
			getContentResolver().update(VocabProvider.CONTENT_URI, values, null, null);
		}		
	}

	// to the content resolver.
	public void removeVocabWord(int position) {
		Integer vocabWordNumber = getVocabWordNumberFromPosition(Integer.valueOf(position));
		removeVocabWordWithWordNumber(vocabWordNumber);
	}

	// to the content resolver.
	public void removeVocabWordWithWordNumber(int vocabWordNumber) {
		// get the right values for content values
		ContentValues values = new ContentValues();
		
		values.put(VocabProvider.VALUES_UPDATE_TYPE, VocabProvider.UPDATE_TYPE_REMOVE_ACTIVE_VOCAB_WORD);
		values.put(VocabProvider.VALUES_VOCAB_WORD_NUMBER, vocabWordNumber);
		// get the content provider and update
		getContentResolver().update(VocabProvider.CONTENT_URI, values, null, null);
	}
	

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] projection = {VocabProvider.C_ID, VocabProvider.C_AWORD};
		return new CursorLoader(
				 				this, 
				 				VocabProvider.ACTIVE_TABLE_URI,
				 				projection,
				 				null,
				 				null,
				 				null );
	}
	

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.v(TAG, "onLoadFinished has started with " 
				+ String.valueOf(cursor.getCount()) + " items");
		VocabListFragment frag = ((VocabListFragment)getSupportFragmentManager()
									.findFragmentById(R.id.vocab_list_frag_container));
		((SimpleCursorAdapter) frag.getListAdapter()).changeCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		VocabListFragment frag = ((VocabListFragment)getSupportFragmentManager()
				.findFragmentById(R.id.vocab_list_frag_container));
		if (frag != null){
			SimpleCursorAdapter adapter = ((SimpleCursorAdapter) frag.getListAdapter());
			if(adapter != null){
				adapter.changeCursor(null);
			}
		}
	}
	public Integer getVocabWordNumberFromPosition(Integer position){
		VocabListFragment frag = ((VocabListFragment)getSupportFragmentManager()
				.findFragmentById(R.id.vocab_list_frag_container));
		if (frag == null){
			return null;
		}
		SimpleCursorAdapter adapter = ((SimpleCursorAdapter) frag.getListAdapter());
		if(adapter == null){
			return null;
		}
		// the _id column is made by the VocabProvider with an Integer (<long) value.
		return Integer.valueOf((int) adapter.getItemId(position.intValue()));
	}
	
	public void onListItemClick(ListView l, View v, int position, long id) {
		// Provider stores _id as an integer, so no data loss.
		Integer vocabWordNumber = Integer.valueOf( (int) id);
		ContentValues values = new ContentValues();
		values.put(VocabProvider.VALUES_UPDATE_TYPE, VocabProvider.UPDATE_TYPE_FLIP_ACTIVE_VOCAB_WORD);
		values.put(VocabProvider.VALUES_VOCAB_WORD_NUMBER, vocabWordNumber);
		getContentResolver().update(VocabProvider.CONTENT_URI, values, null, null);
	}


	@Override
	public void onChangeLanguageCallback(String language) {
		Log.v(TAG, "onChangeLanguageCallback called with language " + language);
		
//		String language;
//		// get the current language from the vocabProvider's shared prefs
//		SharedPreferences prefs = getSharedPreferences(VocabProvider.TAG, Context.MODE_PRIVATE);
//		String currentLanguage = 
//				prefs.getString(VocabProvider.VOCAB_LANGUAGE, VocabProvider.C_EWORD);
//		// then use the other language
//		if (currentLanguage.equals(VocabProvider.C_EWORD)){
//			language = VocabProvider.C_FWORD;
//		} else {
//			language = VocabProvider.C_EWORD;
//		}
		// now set up the active table in the other language using vocabProvider
		ContentValues values = new ContentValues();
		// file vocabName as name of file and current chapter
		values.put(VocabProvider.VALUES_UPDATE_TYPE, VocabProvider.UPDATE_TYPE_VOCAB_LANGUAGE);
		values.put(VocabProvider.VALUES_VOCAB_LANGUAGE, language);
		// get the content provider and update
		getContentResolver().update(VocabProvider.CONTENT_URI, values, null, null);
	}


	int lastDiscarded = -1; // last discarded should never be -1 
	@Override
	public void easyDialogNext(int current, boolean discardWord) {
		// if discardWord, then remove it
		if (discardWord){
			// this can start a race condition if the prev word isn't removed
			// prior to getting the new word, so keep last discarded wordNumber
			lastDiscarded = current;
			removeVocabWordWithWordNumber(current);
		}
		// start easyDialog
		startEasyDialog();
	}


	private void startDialogSequence() {
		// TODO Auto-generated method stub
		startEasyDialog();
	}

	public void startEasyDialog() {
		Log.v(TAG, "startEasyDialog() begining");
		// Get a random vocab word number uri from the list of vocab words.
		int vocabWordNumber = -1;
		// will need current list adapter
		ListAdapter adapter = ((VocabListFragment)getSupportFragmentManager()
				.findFragmentById(R.id.vocab_list_frag_container))
				.getListAdapter();
		// how many items are in the vocabListFragment 
		int listCount = adapter.getCount();
		if (listCount == 0){
			Log.v(TAG, "no list items so not starting easy dialog");
			return;
		}
		do {
			int random = (int) (Math.random() * 1000) % listCount ;
			// use the random number to  get vocabWordNumber
			vocabWordNumber = (int) adapter.getItemId(random);
		} while (vocabWordNumber == lastDiscarded && listCount > 1);
		if( listCount == 1 && vocabWordNumber == lastDiscarded){
			Log.v(TAG, "no list items so not starting easy dialog");
			return;
		}
		// get the  word number uri
		String uriString = VocabProvider.VOCAB_TABLE_URI.toString() + "/" + String.valueOf(vocabWordNumber);
		Uri uri = Uri.parse(uriString);
		
		// Query for the english and french words then set as the 
		// question/answer for the easy dialog sequence.
		String question = "",answer = "";
		Cursor cursor = getContentResolver().query(uri, null, null, null, null);
		SharedPreferences prefs = getSharedPreferences(VocabProvider.TAG , Context.MODE_PRIVATE);
		// TODO working here
		// if english active
		if (prefs.getBoolean(VocabProvider.PREFERENCES_IS_ENGLISH_ACTIVIE, true)){ 
			// put english as question and french as anser
			cursor.moveToFirst();
			int index = cursor.getColumnIndex(VocabProvider.C_EWORD);
			question = cursor.getString(index);
			index = cursor.getColumnIndex(VocabProvider.C_FWORD);
			answer = cursor.getString(index);
		} else { // else do the reverse.
			cursor.moveToFirst();
			int index = cursor.getColumnIndex(VocabProvider.C_FWORD);
			question = cursor.getString(index);
			index = cursor.getColumnIndex(VocabProvider.C_EWORD);
			answer = cursor.getString(index);
		}
		// Load up the arguments into a new EasyDialogQeustionFramgent 
		FragmentManager fragMan= getSupportFragmentManager();
		EasyDialogQuestionFragment questionFrag = new EasyDialogQuestionFragment();
		Bundle bundle = new Bundle();
		bundle.putString(EasyDialogQuestionFragment.QUESTION, question);
		bundle.putString(EasyDialogAnswerFragment.ANSWER, answer);
		bundle.putInt(EasyDialogAnswerFragment.WORDNUMBER, vocabWordNumber);
		questionFrag.setArguments(bundle);
		// and show.
		questionFrag.show(fragMan, TAG);	
	}

}
