package com.mck.vocab;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mck.vocab.fragments.ChangeLanguageDialogFragment;
import com.mck.vocab.fragments.ChangeLanguageDialogFragment.ChangeLanguageCallback;
import com.mck.vocab.fragments.EasyDialogAnswerFragment;
import com.mck.vocab.fragments.EasyDialogQuestionFragment;
import com.mck.vocab.fragments.VocabListFragment;

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
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);        
        drawerList = (ListView) findViewById(R.id.left_drawer);
        // set the list view adapter
        DrawerListArrayAdapter adapter = cDrawerListAdapter(this);
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
					.getItemFromPosition(position);
			
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
			case NONE:
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
			// if there is a title, 
			if(titleCursor != null){
				titleCursor.moveToFirst();
				// get the title and its number.	
				String title;
				String number;
				int titleIndex = titleCursor.getColumnIndex(VocabProvider.C_TITLE);
				int numberIndex = titleCursor.getColumnIndex(VocabProvider.C_ID);
				title = titleCursor.getString(titleIndex);
				number = titleCursor.getString(numberIndex);
				// add the sub item to the adapter and notify data set changed.
				((DrawerListArrayAdapter)drawerList.getAdapter()).addSubItem(title,number);
				((BaseAdapter) drawerList.getAdapter()).notifyDataSetChanged();
				// close the cursor
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
			// set the drawer list without start, invalidating the adapter below
			((DrawerListArrayAdapter) drawerList.getAdapter()).setActiveTableToItemSet(PREFERENCES_ITEM_SET_NO_START);
			// and open the drawer
			drawerLayout.openDrawer(Gravity.LEFT);
		} else { // there must be some stuff in the list
			// show default list view , invalidating the adapter below
			((DrawerListArrayAdapter) drawerList.getAdapter()).setActiveTableToItemSet(PREFERENCES_ITEM_SET_DEFAULT);
			// TODO this is where the loading the list without the the reset should happen aswell.
			
		}
		// this should make all three possible changes appear as one.
		((BaseAdapter) drawerList.getAdapter()).notifyDataSetChanged();
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
				} else { // let the user know that they need to load vocab
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							// since there is no title, there is no vocab, set the drawer to reflect this.
							((DrawerListArrayAdapter) drawerList.getAdapter()).setActiveTableToItemSet(PREFERENCES_ITEM_SET_GET_VOCAB);
							((BaseAdapter) drawerList.getAdapter()).notifyDataSetChanged();
						}
					});
				}
				// this should make all three possible changes appear as one.
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
	public DrawerListArrayAdapter cDrawerListAdapter(Context context){
		
		int resource = 0;
		int viewTypeCount = 3;
		ArrayList<Integer> itemIdList = new ArrayList<Integer>();

		// get the item table
		Hashtable<Integer,Item> itemTable = new Hashtable<Integer,Item>();
		//start item
		Item item = new Item();
		item.text = getString(R.string.start);
		item.imageResource = R.drawable.ic_start_star;
		item.itemTypeAction = ItemTypeAction.START;
		item.id = 0;
		itemTable.put(0, item);
		itemIdList.add(0);
		// language item
		item = new Item();
		item.text = getString(R.string.language);
		item.imageResource = R.drawable.ic_language_bubble;
		item.itemTypeAction = ItemTypeAction.LANGUAGE;
		item.id = 1;
		itemTable.put(1, item);
		itemIdList.add(1);
		// restart/reset item
		item = new Item();
		item.text = getString(R.string.restart);
		item.imageResource = R.drawable.ic_restart;
		item.itemTypeAction = ItemTypeAction.RESET;
		item.id = 2;
		itemTable.put(2, item);
		itemIdList.add(2);
		// morevocab item
		item = new Item();
		item.text = getString(R.string.more_vocab);
		item.imageResource = R.drawable.ic_more_vocab;
		item.itemTypeAction = ItemTypeAction.MORE_VOCAB;
		item.id = 3;
		itemTable.put(3, item);
		itemIdList.add(3);
		
		
		// get the subItemTable
		LinkedHashMap<Integer,Item> subItemsMap = null;
		// No guarantee that the getStringSet is is ordered so using getString as opposed to getStringSet. 
		String subItemTitles = prefs.getString(PREFERENCES_SUB_ITEM_TITLES, "");
		String subItemVocabNumbers= prefs.getString(PREFERENCES_SUB_ITEM_VOCAB_NUMBERS, "");
		if (!subItemVocabNumbers.equals("")){
			// create the subItemMap
			subItemsMap = new LinkedHashMap<Integer,Item>();
			// put in the group item first
			GroupItem gItem = new GroupItem();
			gItem.text = getString(R.string.history);
			gItem.itemTypeAction = ItemTypeAction.NONE;
			subItemsMap.put(-1, gItem); // TODO make this -1 a constant
			itemIdList.add(4);
			
			// get the titles and numbers, modified for a blank leading title
			String[] titles = subItemTitles.split("\n");
			String[] numbers = subItemVocabNumbers.split("\n");
			// for each, skipping first slot
			for(int x = 1; x < numbers.length; x++){
				SubItem subItem = new SubItem();
				subItem.text = titles[x];
				subItem.vocabNumber = Integer.valueOf(numbers[x]).intValue();
				subItem.itemTypeAction = ItemTypeAction.OPEN_PREVIOUS_VOCAB;
				Integer number = Integer.valueOf(numbers[x]);
				subItemsMap.put(number, subItem);
				itemIdList.add(4+x);
			}
		}
		DrawerListArrayAdapter result = new DrawerListArrayAdapter(context, resource, 
				itemTable, subItemsMap, itemIdList, viewTypeCount);
		return result;
	}
	

	/**
	 * This extension of an ArrayAdapter is to be used with the drawer List. It
	 * provides 3 types of cells item, <headerItem,> subItem, and groupItem. Each cell is of type
	 * item and is responsible for it's getView.
	 * The adapter will have the list of items at the top. It will be followed by
	 * a groupItem, the group item. Items are things like start, language, reset,
	 * options, etc. They should be titled and have a leading image. The groupItem is 
	 * labeled history. The sub items are titles of vocab and their numbers.
	 * @author Michael
	 *
	 */
	public class DrawerListArrayAdapter extends ArrayAdapter<Integer>{
		Hashtable<Integer, Item> itemTable;
		LinkedHashMap<Integer,Item> subItemsMap;
		int[] activeArray;
		int viewTypeCount = 0;
		int MAX = 7;
		public DrawerListArrayAdapter(Context context, int resource,
										Hashtable<Integer, Item> itemTable,
										LinkedHashMap<Integer,Item> subItemMap,
										List<Integer> itemIdList,
										int viewTypeCount) {
			super(context, resource, itemIdList);
			
			// Set the tables.
			this.itemTable = itemTable;
			this.subItemsMap = subItemMap;
			
			// set the active array
			// get the item set from prefs.
			int itemSet = prefs.getInt(PREFERENCES_ITEM_SET, PREFERENCES_ITEM_SET_GET_VOCAB);
			// Set the active look up table to item set
			setActiveTableToItemSet(itemSet);
			// if not item set get vocab
//			if (itemSet != PREFERENCES_ITEM_SET_GET_VOCAB){
//				
//				// if there is a subItemTable
//				if (subItemMap != null && subItemMap.size() != 0){
//					for (Integer key: subItemMap.keySet()){
//						addSubItem(subItemMap.get(key));
//					}
//				}
//			}			
			this.viewTypeCount = viewTypeCount;
		}
		
		private void setActiveTableToItemSet(int itemSet){
			int subItemsLength = 0;
			int itemCount = 0;
			if (subItemsMap != null){
				subItemsLength = subItemsMap.size();
			}
			switch(itemSet){
			// set to just show get vocab and no other items
			case (PREFERENCES_ITEM_SET_GET_VOCAB):
				activeArray = new int[1 + subItemsLength];
				activeArray[0] = 3;
				itemCount = 1;
				break;
			// set to show all items but the reset
			case (PREFERENCES_ITEM_SET_NO_RESET):
				activeArray = new int[3 + subItemsLength];
				activeArray[0] = 0;
				activeArray[1] = 1;
				activeArray[2] = 3;
				itemCount = 3;
				break;
			// set to show no start item, but all the rest.
			case (PREFERENCES_ITEM_SET_NO_START):
				activeArray = new int[2 + subItemsLength];
				activeArray[0] = 2;
				activeArray[1] = 3;
				itemCount = 2;
				break;
			// The default is to show all items
			case (PREFERENCES_ITEM_SET_DEFAULT):
			default:
				activeArray = new int[4 + subItemsLength];
				activeArray[0] = 0;
				activeArray[1] = 1;
				activeArray[2] = 2;
				activeArray[3] = 3;
				itemCount = 4;
				break;
			}
			// now if there is a sub item list, then for each subItem add to active in reverse
			if (subItemsMap != null){
				
				int activeIndex = activeArray.length - 1; // index starts with 0
				Iterator<Integer> i = subItemsMap.keySet().iterator();
				// the group item goes first
				Integer first = i.next(); 
				activeArray[itemCount] = first;
				// now add the rest
				while (i.hasNext()){
					activeArray[activeIndex--] = i.next();
				}
			}
		}
		// TODO
		public void addSubItem(String title, String number) {
			SubItem subItem = new SubItem();
			subItem.text = title;
			subItem.vocabNumber = Integer.valueOf(number).intValue();
			addSubItem(subItem);
			String subItemTitles = prefs.getString(PREFERENCES_SUB_ITEM_TITLES, "");
			String subItemVocabNumbers= prefs.getString(PREFERENCES_SUB_ITEM_VOCAB_NUMBERS, "");
			subItemTitles = subItemTitles + "\n" + title;
			subItemVocabNumbers = subItemVocabNumbers + "\n" + number;
			prefs.edit()
				.putString(PREFERENCES_SUB_ITEM_TITLES, subItemTitles)
				.putString(PREFERENCES_SUB_ITEM_VOCAB_NUMBERS, subItemVocabNumbers)
				.commit();
		}
		
		private void addSubItem(Item item){
			// Will add item to the map by it's vocabNumber
			int vocabNumber = ((SubItem)item).vocabNumber;
			// The resulting active array will
			int[] result = null;
			// need to know the number of Items less sub items in the active array
			// and need to get this before any changes made to sub items are done.
			int subItemCount = 0;
			if (subItemsMap != null){
				subItemCount = subItemsMap.size();				
			}
			int itemCount = activeArray.length - subItemCount;
			// Might as well create the result array here. Plus two for flinching.
			result = new int[itemCount + subItemCount + 2 ]; // Group and new item.
			if (subItemsMap == null){
				// must be the first sub Item
				subItemsMap = new LinkedHashMap<Integer,Item>();
				// put the group item in first
				GroupItem gItem = new GroupItem();
				gItem.text = getString(R.string.history);
				gItem.itemTypeAction = ItemTypeAction.NONE;
				subItemsMap.put(-1, gItem);
				subItemsMap.put(vocabNumber,item);
			} else {
				// attempt to remove old item, then put it in again so that last in.
				subItemsMap.remove(vocabNumber);
				subItemsMap.put( vocabNumber, item);
				// Now if it is more than the max, remove the first in sub item.
				if (subItemsMap.size() > MAX){
					Iterator<Integer> i = subItemsMap.keySet().iterator();
					i.next();// the group item.
					int oldest = i.next(); // the oldest entry
					subItemsMap.remove(oldest);					
				}
			}
			
			// put itemsTable (via active array) stuff into result first
			for(int x = 0; x < itemCount; x++){
				result[x] = activeArray[x];
			}
			// for each subItem add to active
			int activeIndex = result.length - 1; // index starts with 0
			Iterator<Integer> i = subItemsMap.keySet().iterator();
			// the group item goes first
			Integer first = i.next(); 
			result[itemCount] = first;
			// now add the rest
			while (i.hasNext()){
				result[activeIndex--] = i.next();
			}
			
			
			
			
//			for(Integer key: subItemsMap.keySet()){
//				result[itemCount++] = key;
//			}
			// done so set activeArray to result
			activeArray = result;
		}


		@Override
		public int getCount() {
			int i =  activeArray.length;
			return i;
		}

		@Override
		public int getViewTypeCount() {
			return viewTypeCount;
		}
		
		@Override
		public int getItemViewType(int position) {
			int itemPosition = activeArray[position];
			Item item = this.itemTable.get(Integer.valueOf(itemPosition));
			if (item == null){
				item = this.subItemsMap.get(Integer.valueOf(itemPosition));
			}
			return item.type;
		}

		public Item getItemFromPosition(int position){
			int pos = activeArray[position];
			Item item = itemTable.get(pos);
			if (item == null){
				item = subItemsMap.get(pos);
			}
			return item;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			int itemPosition = activeArray[position];
			View result;
			Item item = null;
			item = this.itemTable.get(Integer.valueOf(itemPosition));
			if (item == null){
				item = this.subItemsMap.get(Integer.valueOf(itemPosition));
			}
			result = item.getView(itemPosition, convertView, parent);
			return result;
		}
	}
	
	private enum ItemTypeAction{
		START, LANGUAGE, RESET, MORE_VOCAB, NONE, OPEN_PREVIOUS_VOCAB
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
			itemTypeAction = ItemTypeAction.NONE;
		}
		public View getView(int position, View convertView, ViewGroup parent){
			if (convertView == null){
				// create the new view
				LayoutInflater inflater = getLayoutInflater();
				convertView = inflater.inflate(R.layout.drawer_list_view_item_cell, null);
			}
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
			itemTypeAction = ItemTypeAction.OPEN_PREVIOUS_VOCAB;
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
