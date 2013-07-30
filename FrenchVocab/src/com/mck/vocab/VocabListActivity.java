package com.mck.vocab;


import android.content.ContentValues;
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
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.mck.vocab.ChangeLanguageDialogFragment.ChangeLanguageCallback;

public class VocabListActivity extends FragmentActivity implements 
	ChangeLanguageCallback,OnSharedPreferenceChangeListener, LoaderCallbacks<Cursor> {
	private static final String TAG = "VocabListActivity";
	public static final int vocabCursorLoaderId = 0;
	public String[] AVAILABLE_CHAPTERS; 
	/*	{	"chap9nounsPart1.txt",
			"chap9nounsPart2.txt",
			"chap9nounsPart3.txt",
			"chap9adjectives.txt",
			"chap9verbs.txt",
			"chap9wordsAndExpressions.txt"			
		}; */
	SharedPreferences prefs;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG,"onCreate() has begun");
        
        // TODO available vocab set names here
        AVAILABLE_CHAPTERS = 
        		getResources().getStringArray(R.array.available_vocab_names);
        
        // create a content provider atleast once?
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
		case R.id.options_item_prefs:
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
			String vocabName = AVAILABLE_CHAPTERS[Integer.valueOf(currentChapter).intValue() - 1];
			values.put(VocabProvider.VALUES_UPDATE_TYPE, VocabProvider.UPDATE_TYPE_OPEN_VOCAB);
			values.put(VocabProvider.VALUES_VOCAB_NAME, vocabName);
			values.put(VocabProvider.VALUES_VOCAB_NUMBER, currentChapter);
			// get the content provider and update
			getContentResolver().update(VocabProvider.CONTENT_URI, values, null, null);
		}		
	}

	// to the content resolver.
	public void removeVocabWord(int position) {
		// get the right values for content values
		ContentValues values = new ContentValues();
		
		values.put(VocabProvider.VALUES_UPDATE_TYPE, VocabProvider.UPDATE_TYPE_REMOVE_ACTIVE_VOCAB_WORD);
		Integer vocabWordNumber = getVocabWordNumberFromPosition(Integer.valueOf(position));
		values.put(VocabProvider.VALUES_VOCAB_WORD_NUMBER, vocabWordNumber);
		// get the content provider and update
		getContentResolver().update(VocabProvider.CONTENT_URI, values, null, null);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] projection = {VocabProvider.C_ID, VocabProvider.C_AWORD};
		 
		return new CursorLoader(
				 				this, 
				 				Uri.parse(VocabProvider.AUTHORITY), // VocabProvider.ACTIVE_TABLE,
				 				projection,
				 				null,
				 				null,
				 				null );
		//return null;
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.v(TAG, "onLoadFinished has started");
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
		// TODO create content values
		ContentValues values = new ContentValues();
		values.put(VocabProvider.VALUES_UPDATE_TYPE, VocabProvider.UPDATE_TYPE_FLIP_ACTIVE_VOCAB_WORD);
		values.put(VocabProvider.VALUES_VOCAB_WORD_NUMBER, vocabWordNumber);
		// TODO ship to provider
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
}
