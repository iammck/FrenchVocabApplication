package com.mck.vocab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class VocabListActivity extends FragmentActivity implements OnSharedPreferenceChangeListener, LoaderCallbacks<Cursor> {
	private static final String TAG = "VocabListActivity";
	public static final int vocabCursorLoaderId = 0;
	public static final String[] AVAILABLE_CHAPTERS = {"chapter7mainVocab.txt","chapter7expressions.txt"};
	public ChapterVocab chapterVocab;
	SharedPreferences prefs;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG,"onCreate() has begun");

        this.getSupportLoaderManager().initLoader(vocabCursorLoaderId, null, this);
        // TODO create the cursor loader
        
        
        
        
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
        
        // Create the chapterVocab with the right starting language
        prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        String language = prefs.getString( "language", "english");
        String s = prefs.getString("current_chapter", "1");
        int currentChapter = Integer.valueOf(s).intValue();
		chapterVocab = new ChapterVocab(this, language, currentChapter);
         
		// register to get changes to the prefs.
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		
        Log.v(TAG, "onCreate created chapter with language:" + language);
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
		// get the chapterVocab to relo ad itself
		chapterVocab.repopulate();
		// what kind of fragment is there?
		Fragment fragment = getSupportFragmentManager()
				.findFragmentById(R.id.vocab_list_frag_container);
		if(VocabListFragment.class.isInstance(fragment)){
			// get the vocabListFragment to restart/refresh it's adapter.
			(( VocabListFragment )getSupportFragmentManager()
					.findFragmentById(R.id.vocab_list_frag_container))
					.resetAdapter();		
		}
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.v(TAG, "onSharedPreferenceChanged method has begun with key " + key);
		// if the key value is for the langue then change the chapterVocab laguage
		if (key.equals("language")){
			chapterVocab.currentLanguage = prefs.getString("language", "english");
			chapterVocab.repopulate();
			// if it is a VocabListFragment
			Fragment fragment = getSupportFragmentManager()
					.findFragmentById(R.id.vocab_list_frag_container);
			
		   if(VocabListFragment.class.isInstance(fragment)){	
			   // get the vocabListFragment to restart/refresh it's adapter.
			   (( VocabListFragment )getSupportFragmentManager()
					.findFragmentById(R.id.vocab_list_frag_container))
					.resetAdapter();
			}
		}
		if (key.equals("current_chapter")){
			// Begin activating the new chapter.
			String s = prefs.getString("current_chapter", "1");
			int currentChapter = Integer.valueOf(s).intValue();
			//chapterVocab.setCurrentChapter(currentChapter);
			// load the current chapter into the listview cursor via
			chapterVocab.activateChapter(currentChapter);			
		}		
	}


	public void removeVocabWord(int position) {
		// get the vocabListFragment to restart/refresh it's adapter.
		chapterVocab.removeVocabWord(position);
		(( VocabListFragment )getSupportFragmentManager()
				.findFragmentById(R.id.vocab_list_frag_container))
				.resetAdapter();
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
		
		// return new CursorLoader(this, R.				);
		return null;
	}


	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}
}
