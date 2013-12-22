/**
 * 
 */
package com.mck.vocab.fragments;


import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.mck.vocab.R;
import com.mck.vocab.VocabListActivity;
import com.mck.vocab.VocabProvider;


/**
 * @author Michael
 *
 */
public class VocabListFragment extends ListFragment implements OnItemLongClickListener {
	public static final String TAG = "VocabListFragment";	
	private VocabListActivity vocabListActivity; // obtained in onAttach
	
	/**
	 * Responsible for getting the activity
	 * @param activity
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		//Log.v(TAG,"onAttach() has begun");
		this.vocabListActivity = (VocabListActivity) activity;
	}

	/**
	 * gets the current chapter vocab from the
	 * 
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//Log.v(TAG,"onActivityCreated() has begun");
		Context context = this.getActivity().getApplicationContext();
		int cellview = R.layout.vocab_list_fragment_word_cell;
		Cursor cursor = null;
		String[] from = {VocabProvider.C_AWORD};
		int[] to = {R.id.vocab_word_text};
		int flags = 0;
		this.setListAdapter(new SimpleCursorAdapter(context,cellview,cursor,from,to, flags));
		// add item long press call back
		this.getListView().setLongClickable(true);
		this.getListView().setOnItemLongClickListener(this);
		
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		((VocabListActivity) vocabListActivity).onListItemClick(l, v, position, id);
	}	

	@Override
	public boolean onItemLongClick(AdapterView<?> av, View v, int position,	long id) {
		//Log.v(TAG, "onItemLongClick() clicked");
		vocabListActivity.removeVocabWord(position);
		return true;
	}
	
}
