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
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class VocabListActivity extends FragmentActivity implements OnSharedPreferenceChangeListener, LoaderCallbacks<Cursor> {
	private static final String TAG = "VocabListActivity";
	public static final int vocabCursorLoaderId = 0;
	public static final String[] AVAILABLE_CHAPTERS = {"chapter7mainVocab.txt","chapter7expressions.txt"};
	SharedPreferences prefs;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG,"onCreate() has begun");
        
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
		//getContentResolver().insert(VocabProvider.CONTENT_URI, values);
		
		// TODO rewrite this
		
//		// get the chapterVocab to relo ad itself
//		chapterVocab.repopulate();
//		// what kind of fragment is there?
//		Fragment fragment = getSupportFragmentManager()
//				.findFragmentById(R.id.vocab_list_frag_container);
//		if(VocabListFragment.class.isInstance(fragment)){
//			// get the vocabListFragment to restart/refresh it's adapter.
//			(( VocabListFragment )getSupportFragmentManager()
//					.findFragmentById(R.id.vocab_list_frag_container))
//					.resetAdapter();		
//		}
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

	// TODO get the actual id from the cursor in listAdapter, then send that in
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


	/**
	 * Called by chapterVocab.ActivateChapter once chapter is in db and populates
	 * the vocabList.
	 * All that needs to be done is to have the vocabListFragment refresh its adapter
	 * 
	 */
	public void onChapterActivated() {
		
		VocabListFragment frag =
			(( VocabListFragment )getSupportFragmentManager()
				.findFragmentById(R.id.vocab_list_frag_container));
				frag.resetAdapter();
		
		Log.v(TAG, "onChapterActivated() completed chapter onPreferenceChange()" 
				+ " call to chapterVocab.activateChapter()");
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
		SimpleCursorAdapter adapter = ((SimpleCursorAdapter) frag.getListAdapter());
		if(adapter != null){
			adapter.changeCursor(null);
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
}
