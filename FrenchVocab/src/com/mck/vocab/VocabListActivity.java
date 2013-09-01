package com.mck.vocab;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mck.vocab.fragments.ChangeLanguageDialogFragment;
import com.mck.vocab.fragments.EasyDialogAnswerFragment;
import com.mck.vocab.fragments.EasyDialogQuestionFragment;
import com.mck.vocab.fragments.VocabListFragment;
import com.mck.vocab.fragments.ChangeLanguageDialogFragment.ChangeLanguageCallback;

public class VocabListActivity extends ActionBarActivity implements 
	ChangeLanguageCallback,OnSharedPreferenceChangeListener, LoaderCallbacks<Cursor>,
	EasyDialogAnswerFragment.EasyDialogFragmentCallback
	{


	private static final String TAG = "VocabListActivity";
	public static final int vocabCursorLoaderId = 0;
	private static final String PREFERENCES_SUB_ITEM_TITLES = "preferences_sub_item_titles";
	private static final String PREFERENCES_SUB_ITEM_VOCAB_NUMBERS = "preferences_sub_item_vocab_numbers";
	private static final String PREFERENCES_ITEM_SET = "preferences_item_set";
	private static final int PREFERENCES_ITEM_SET_GET_VOCAB = 1;
	private static final int PREFERENCES_ITEM_SET_NO_START = 2;
	private static final int PREFERENCES_ITEM_SET_NO_RESET = 3;
	private static final int PREFERENCES_ITEM_SET_DEFAULT = 4;
	public String[] AVAILABLE_VOCAB_FILE_NAMES; 
	SharedPreferences prefs;
	
	private DrawerLayout drawerLayout;
	private ListView drawerList;
	private ActionBarDrawerToggle drawerToggle;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG,"onCreate() has begun");
        //Log.v(TAG,"debug is on");
        //Debug.startMethodTracing();
        
        AVAILABLE_VOCAB_FILE_NAMES = getResources().getStringArray(R.array.available_vocab_file_names);
        
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
        setContentView(R.layout.vocab_list_activity_layout);
        
        // get the drawer layout, list  and actionBar toggle.
        // String[] drawerTitles =	getResources().getStringArray(R.array.drawer_row_titles);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);        
        drawerList = (ListView) findViewById(R.id.left_drawer);
        // set the list view adapter
        DrawerListArrayAdapter adapter = createDrawerListAdapter(this);
        drawerList.setAdapter(adapter);
        // set the list's clickListener
        drawerList.setOnItemClickListener(new DrawerItemClickListener(this));

        // set the support actionbar to display home.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer,
        		R.string.drawer_open, R.string.drawer_close){
        	public void onDrawerClosed(View view) {
                //getActionBar().setTitle(mTitle);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
            	//getActionBar().setTitle(mDrawerTitle);
            	supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

	public class DrawerItemClickListener implements ListView.OnItemClickListener {
		VocabListActivity activity;
		
		public DrawerItemClickListener(VocabListActivity activity){
			this.activity = activity;
		}
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Log.v(TAG, "drawerItemClickListener onItemClick is starting");
			
			Item item = ((DrawerListArrayAdapter)drawerList.getAdapter())
					.itemTable.get(position);
			if (item == null){
				item = ((DrawerListArrayAdapter)drawerList.getAdapter())
					.subItemsTable.get(position);
			}
			
			
			switch (item.itemTypeAction){
			case START:
				Log.v(TAG, "drawer item start selected");
				startDialogSequence();
				break;		
			case LANGUAGE:
				Log.v(TAG, "drawer item language selected");
				FragmentManager fragMan= getSupportFragmentManager();
				ChangeLanguageDialogFragment frag = new ChangeLanguageDialogFragment();
				frag.show(fragMan, ChangeLanguageDialogFragment.TAG);
				break;
			case RESET:
				Log.v(TAG, "drawer item reset selected");
				restartVocabList();
				break;
			case MORE_VOCAB:
				Log.v(TAG, "drawer item more vocab selected");
				startActivity(new Intent(activity, PrefsActivity.class));
				break;
			case HISTORY:
				Log.v(TAG, "drawer item history selected");
				return; // don't close the drawer, just return.
			case OPEN_PREVIOUS_VOCAB:
				//onSharedPreferenceChanged(SharedPreferences sharedPreferences
				// must be be a subItem, get it.
				prefs.edit().putString("current_chapter", String.valueOf(((SubItem)item).vocabNumber)).commit();			
				break;
			
			}
			
			activity.drawerLayout.closeDrawer(activity.drawerList);
		}
	}
    
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		 // always want to show the drawer if the list dne or is empty
	     //get the list from the fragment
	     ListFragment list = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.vocab_list_frag_container);
	     // if there is no list or f the list is empty
	     //if(list != null && list.getListView().getCount() == 0){
	     if(list == null || list.getListView().getCount() == 0){
	    	 // show the drawer
	     	drawerLayout.openDrawer(Gravity.LEFT);
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

	/* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        //boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// this line allows the actionbar icon touch to open the drawer
		if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
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
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	

	public void restartVocabList(){
		Log.v(TAG, "restartVocabList has begun");
		// get the right values for content values
		ContentValues values = new ContentValues();
		values.put(VocabProvider.VALUES_UPDATE_TYPE, VocabProvider.UPDATE_TYPE_RESET_VOCAB);
		// get the content provider and update
		getContentResolver().update(VocabProvider.CONTENT_URI, values, null, null);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.v(TAG, "onSharedPreferenceChanged method has begun with key " + key);
		if (key.equals("current_chapter")){
			// get info for drawerList history.			
			Cursor titleCursor = getContentResolver()
					.query(VocabProvider.TITLES_TABLE_URI, null, null, null, null);
			if(titleCursor != null){
				titleCursor.moveToFirst();
				String title;
				String number;
				int titleIndex = titleCursor.getColumnIndex(VocabProvider.C_TITLE);
				int numberIndex = titleCursor.getColumnIndex(VocabProvider.C_ID);
				title = titleCursor.getString(titleIndex);
				number = titleCursor.getString(numberIndex);
				String subItemTitles = prefs.getString(PREFERENCES_SUB_ITEM_TITLES, "");
				String subItemVocabNumbers= prefs.getString(PREFERENCES_SUB_ITEM_VOCAB_NUMBERS, "");
				// if number is there already, then remove it
				if (subItemVocabNumbers.contains(number)){
					// a counter notice that the split to happen means to start at 1
					int counter = 1;
					// get titles and numbers as arrays
					String[] titles = subItemTitles.split("\n");
					String[] numbers = subItemVocabNumbers.split("\n");
					// rebuild subItemTitles and subItemVocabNumbers with out number
					// if it's the first number
					if (number.equals(numbers[counter])){
						//try to start with the second spot
						if (numbers.length > 1){
							counter++;
						}
					}
					subItemTitles = "";
					subItemVocabNumbers = "";
					while (counter < numbers.length){
						if (!number.equals(numbers[counter])){
							subItemTitles = subItemTitles +"\n"+ titles[counter] ;
							subItemVocabNumbers = subItemVocabNumbers + "\n" + numbers[counter];
						}
						counter++;
					}
				}
				
				// TODO and check for max history.
				// if max history, remove first in
				subItemTitles = subItemTitles + "\n" + title;
				subItemVocabNumbers = subItemVocabNumbers + "\n" + number;
				prefs.edit()
					.putString(PREFERENCES_SUB_ITEM_TITLES, subItemTitles)
					.putString(PREFERENCES_SUB_ITEM_VOCAB_NUMBERS, subItemVocabNumbers)
					.commit();
				// update the drawerListAdapter and close the cursor
				drawerList.setAdapter(createDrawerListAdapter(this));
				titleCursor.close();
			}
			
			// get the right values for content values
			ContentValues values = new ContentValues();
			// file vocabName as name of file and current chapter
			String currentChapter = prefs.getString("current_chapter", "1");
			
			String vocabName = AVAILABLE_VOCAB_FILE_NAMES[Integer.valueOf(currentChapter).intValue() - 1];
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
		Log.v(TAG,"onCreateLoader has begun.");
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
		int count = cursor.getCount();
		Log.v(TAG, "onLoadFinished has started with " 
				+ String.valueOf(count) + " items");
		if (count == 0){ // if the list is empty
			// set the drawer list without start
			prefs.edit().putInt(PREFERENCES_ITEM_SET, PREFERENCES_ITEM_SET_NO_START).commit();
			drawerList.setAdapter(createDrawerListAdapter(getApplication()));
			// or open the drawer
			drawerLayout.openDrawer(Gravity.LEFT);
		} else { // there must be some stuff in the list
			// show default list view
			prefs.edit().putInt(PREFERENCES_ITEM_SET, PREFERENCES_ITEM_SET_DEFAULT).commit();
			drawerList.setAdapter(createDrawerListAdapter(getApplication()));
			// this is where the loading the list without the the reset should happen aswell.
			
		}
		// set the vocabList adpter to the cursor.
		VocabListFragment frag = ((VocabListFragment)getSupportFragmentManager()
									.findFragmentById(R.id.vocab_list_frag_container));
		((SimpleCursorAdapter) frag.getListAdapter()).changeCursor(cursor);
		// set the title to the active table inside an asyncTask
		new AsyncTask<Integer, Integer, Integer>() {
			Cursor titleCursor;
			@Override
			protected Integer doInBackground(Integer... params) {
				titleCursor = getContentResolver().query(VocabProvider.TITLES_TABLE_URI, null, null, null, null);
				return null;
			}

			/* (non-Javadoc)
			 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
			 */
			@Override
			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);
				if(titleCursor != null){
					titleCursor.moveToFirst();
					String title;
					int index = titleCursor.getColumnIndex(VocabProvider.C_TITLE);
					title = titleCursor.getString(index);
					getSupportActionBar().setTitle(title);
					titleCursor.close();
				} else { // since there is no title, there is no vocab, set the drawer to reflect this.
					prefs.edit().putInt(PREFERENCES_ITEM_SET, PREFERENCES_ITEM_SET_GET_VOCAB).commit();
					drawerList.setAdapter(createDrawerListAdapter(getApplication()));
					// let the user know that they need to load vocab
					
				}
			}
			}.execute();
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
		// Set up the active table in the other language using vocabProvider
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
			//display in short period of time
			Toast t = Toast .makeText(getApplicationContext(), "There is no active vocab, select some from options.", Toast.LENGTH_SHORT);
			t.setGravity(Gravity.CENTER, 0, 0);
			t.show();
			// not working possibly because the drawer isn't closed from start click seq.
			// might try a runnable on a timer? could be not in ui thread?
			// is there a way to printf the thread id?
			// drawerLayout.openDrawer(Gravity.LEFT);
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
	
	public DrawerListArrayAdapter createDrawerListAdapter(Context context){
		// need the following
		Hashtable<Integer, Item> itemTable = null;
		Hashtable<Integer, Item> subItemTable = null;
		ArrayList<Integer> itemIdList = new ArrayList<Integer>();
		int resource = 0;
		int viewTypeCount = 0;
		// to get the result
		DrawerListArrayAdapter result;
		// get items
		Item[] items = getItems();
		// get any sub items
		SubItem[] subItems = getSubItems();
		// A counter for number of adapter items.
		int itemCount = 0;
		// put items into itemsTable and update view type count
		viewTypeCount = 1;
		if (items != null && items.length != 0){
			itemTable = new Hashtable<Integer, Item>();
			for(Item i: items){
				i.id = itemCount;
				itemIdList.add(Integer.valueOf(itemCount));
				itemTable.put(Integer.valueOf(itemCount++), i);
			}
		}
		// put subItems into subItemsTable and update viewTypeCount
		if (subItems != null && items.length != 0){
			viewTypeCount = 3;
			subItemTable = new Hashtable<Integer, Item>();
			// put in the group item first
			GroupItem gItem = new GroupItem();
			gItem.text = getString(R.string.history);
			gItem.itemTypeAction = ItemTypeAction.HISTORY;
			gItem.id = itemCount;
			itemIdList.add(Integer.valueOf(itemCount));
			subItemTable.put(Integer.valueOf(itemCount++), gItem);
			// iterate through the sub items, putting them into the table
			for(Item i: subItems){ // with the right ids
				i.id = itemCount;
				itemIdList.add(Integer.valueOf(itemCount));
				subItemTable.put(Integer.valueOf(itemCount++), i);
			}
		}
		// the adapter needs some strings
		result = new DrawerListArrayAdapter(context, resource, 
							itemTable, subItemTable, itemIdList, viewTypeCount);
		return result;
	}

	/**
	 * Get the items to display in the drawer list.
	 * @return
	 */
	public Item[] getItems(){
		Item[] items;
		int itemSet = prefs.getInt(PREFERENCES_ITEM_SET, PREFERENCES_ITEM_SET_DEFAULT);
		switch(itemSet){
		// set to just show get vocab and no other items
		case (PREFERENCES_ITEM_SET_GET_VOCAB):
			items = new Item[1];
			items[0] = new Item();
			items[0].text = getString(R.string.get_vocab);			
			items[0].imageResource = R.drawable.ic_start_star;
			items[0].itemTypeAction = ItemTypeAction.MORE_VOCAB;
			break;
		// set to show all items but the reset
		case (PREFERENCES_ITEM_SET_NO_RESET):
			items = new Item[3];
			for(int x = 0; x < 3 ; x++){
				items[x] = new Item();
			}
			items[0].text = getString(R.string.start);
			items[1].text = getString(R.string.language);
			items[2].text = getString(R.string.more_vocab);			
			items[0].imageResource = R.drawable.ic_start_star;
			items[1].imageResource = R.drawable.ic_language_bubble;
			items[2].imageResource = R.drawable.ic_more_vocab;
			items[0].itemTypeAction = ItemTypeAction.START;
			items[1].itemTypeAction = ItemTypeAction.LANGUAGE;
			items[2].itemTypeAction = ItemTypeAction.MORE_VOCAB;
			
			
			break;
		// set to show no start item, but all the rest.
		case (PREFERENCES_ITEM_SET_NO_START):
			items = new Item[2];
			for(int x = 0; x < 2 ; x++){
				items[x] = new Item();
			}
			items[0].text = getString(R.string.restart);
			items[1].text = getString(R.string.more_vocab);			
			items[0].imageResource = R.drawable.ic_restart;
			items[1].imageResource = R.drawable.ic_more_vocab;
			items[0].itemTypeAction = ItemTypeAction.RESET;
			items[1].itemTypeAction = ItemTypeAction.MORE_VOCAB;
			
			
			break;
		// The default is to show all items
		case (PREFERENCES_ITEM_SET_DEFAULT):
		default:
			items = new Item[4];
			for(int x = 0; x < 4 ; x++){
				items[x] = new Item();
			}
			items[0].text = getString(R.string.start);
			items[1].text = getString(R.string.language);
			items[2].text = getString(R.string.restart);
			items[3].text = getString(R.string.more_vocab);			
			items[0].imageResource = R.drawable.ic_start_star;
			items[1].imageResource = R.drawable.ic_language_bubble;
			items[2].imageResource = R.drawable.ic_restart;
			items[3].imageResource = R.drawable.ic_more_vocab;
			items[0].itemTypeAction = ItemTypeAction.START;
			items[1].itemTypeAction = ItemTypeAction.LANGUAGE;
			items[2].itemTypeAction = ItemTypeAction.RESET;
			items[3].itemTypeAction = ItemTypeAction.MORE_VOCAB;
			
			break;
		}
		return items;
	}
	public SubItem[] getSubItems(){
		// TODO faster! faster! faster!
		String subItemTitles = prefs.getString(PREFERENCES_SUB_ITEM_TITLES, "");
		String subItemVocabNumbers= prefs.getString(PREFERENCES_SUB_ITEM_VOCAB_NUMBERS, "");
		if (subItemVocabNumbers.equals("")){
			return null;
		}else{
			SubItem[] result = null;
			// get the titles and numbers, modified for a blank leading title
			String[] titles = subItemTitles.split("\n");
			String[] numbers = subItemVocabNumbers.split("\n");
			result = new SubItem[titles.length-1];
			// for each, skipping first slot
			for(int x = 1; x < numbers.length; x++){
				SubItem subItem = new SubItem();
				subItem.text = titles[x];
				subItem.vocabNumber = Integer.valueOf(numbers[x]).intValue();
				subItem.itemTypeAction = ItemTypeAction.OPEN_PREVIOUS_VOCAB;
				result[x-1] = subItem;
			}
			return result;
		}
	}

	/**
	 * This extension of an ArrayAdapter is to be used with the drawer List. It
	 * provides 3 types of cells item, <headerItem,> subItem, and groupItem. Each cell is of type
	 * item and is responsible for it's getView.
	 * The adapter will have <a header followed> the list of items at the top. It will be followed by
	 * a groupItem, the group item is either in an open state of a closed state. if
	 * it is in the closed state then it is the last in the list. If it is in the open
	 * state then it is followed by subItems. Items are things like start, language, reset,
	 * options, etc. They should be titled and have a leading image. The groupItem is 
	 * labeled history and has a trailing arrow to toggle showing the subItems which are
	 * to be links to open previously opened vocab sets.
	 * @author Michael
	 *
	 */
	public class DrawerListArrayAdapter extends ArrayAdapter<Integer>{

		// This approach is too static and does not allow for easy manipulation of base items.
		// Items should bee in a dynamic list. position may change.
		Hashtable<Integer, Item> itemTable;
		Hashtable<Integer,Item> subItemsTable;
		int viewTypeCount = 0;
				
		public DrawerListArrayAdapter(Context context, int resource,
										Hashtable<Integer, Item> itemTable,
										Hashtable<Integer, Item> subItemTable,
										List<Integer> itemIdList,
										int viewTypeCount) {
			super(context, resource, itemIdList);
			// Use the itemTextArray and itemArray to build up the itemTable.
			this.itemTable = itemTable;
			this.subItemsTable = subItemTable;
			this.viewTypeCount = viewTypeCount;
		}
		
		@Override
		public int getCount() {
			if (subItemsTable != null){
				return itemTable.size() + subItemsTable.size() ;
			} else {
				return itemTable.size();
			}
		}

		@Override
		public int getViewTypeCount() {
			return viewTypeCount;
		}
		
		@Override
		public int getItemViewType(int position) {
			Item item = this.itemTable.get(Integer.valueOf(position));
			if (item == null){
				item = this.subItemsTable.get(Integer.valueOf(position));
			}
			return item.type;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View result;
			Item item = null;
			item = this.itemTable.get(Integer.valueOf(position));
			if (item == null){
				item = this.subItemsTable.get(Integer.valueOf(position));
			}
			result = item.getView(position, convertView, parent);
			return result;
		}
	}
	
	private enum ItemTypeAction{
		START,LANGUAGE,RESET,MORE_VOCAB,HISTORY,OPEN_PREVIOUS_VOCAB
	}

	private class Item {
		@SuppressWarnings("unused")
		public int id;
		public int type;
		public ItemTypeAction itemTypeAction;
		public String text;
		public int imageResource;
		
		public Item(){
			type = 0;
		}
		public View getView(int position, View convertView, ViewGroup parent){
			if (convertView == null){
				// create the new view
				LayoutInflater inflater = getLayoutInflater();
				convertView = inflater.inflate(R.layout.drawer_list_view_item_cell, null);
			}
			// not always getting the right convertView
			TextView textView = (TextView) convertView.findViewById(R.id.itemText);
			ImageView imageView = (ImageView) convertView.findViewById(R.id.itemImageView);

			textView.setText(text);
			imageView.setImageResource(imageResource);

			return convertView;
		}
		public String toString(){
			return text;
		}
	}

	private class GroupItem extends Item {
		public GroupItem(){
			type = 1;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			if (convertView == null){
				// create the new view
				LayoutInflater inflater = getLayoutInflater();
				convertView = inflater.inflate(R.layout.drawer_list_view_group_item_cell, null);
			}
			TextView textView = (TextView) convertView.findViewById(R.id.groupItemText);
			// Set the text view
			textView.setText(text);
			return convertView;
		}
	}

	private class SubItem extends Item {
		public int vocabNumber;
		public SubItem(){
			type = 2;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			if (convertView == null){
				// create the new view
				LayoutInflater inflater = getLayoutInflater();
				convertView = inflater.inflate(R.layout.drawer_list_view_sub_item_cell, null);
			}
			TextView textView = (TextView) convertView.findViewById(R.id.subItemText);

			textView.setText(text);

			return convertView;
		}
		}	
	}
