/**
 * 
 */
package com.mck.vocab;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;


/**
 * @author Michael
 *
 */
public class VocabListFragment extends ListFragment implements OnItemLongClickListener {
	private static final String TAG = "VocabListFragment";
	
	private VocabListActivity vocabListActivity; // obtained in onAttach

	

	
	/**
	 * Responsible for getting the activity
	 * @param activity
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.v(TAG,"onAttach() has begun");
		this.vocabListActivity = (VocabListActivity) activity;
		
	}

	/**
	 * gets the current chapter vocab from the
	 * 
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.v(TAG,"onActivityCreated() has begun");

//		// get the current ChapterVocabList
//		HashMap<Integer,ChapterVocab.VocabWord> vocab;
//		vocab = vocabListActivity.chapterVocab.getVocabList();
//		// for each get the id into an array list
//		ArrayList<Integer> vocabIds = new ArrayList<Integer>();
//		for(Integer vocabId: vocab.keySet()){
//			vocabIds.add(vocabId);
//		}

		// set it to the fragment with setListAdapter
		//this.setListAdapter(new VocabListAdapter(vocabListActivity, 
		//		R.layout.vocab_word_cell, R.id.vocab_word_text, vocabIds));
		
		
		// set the adapter to a simple list adapter TODO
		//this.setListAdapter(new SimpleCursorAdapter(vocabListActivity, R.layout.vocab_word_cell, null, null, null, 0));
		
		// should i put this in an earlier lifecycle method?
		// set the adapter to a simple cursor adapter
		Context context = this.getActivity().getApplicationContext();
		int cellview = R.layout.vocab_word_cell;
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
		// TODO rewrite this method to work with the VocabProvider
//		Log.v(TAG, "onListItemClick for position " + position);
//		// get the vocabWord
//		VocabWord vocabWord = vocabListActivity.chapterVocab.getVocabList().get(Integer.valueOf(position));
//		// change the current language and update the textView the other language
//		if (vocabWord.currentLanguage.equals("english")){
//			// change to french stuff
//			vocabWord.currentLanguage = "french";
//			TextView textView = (TextView) v.findViewById(R.id.vocab_word_text);
//			textView.setText(vocabWord.fWord);
//		} else {
//			// change to french stuff
//			vocabWord.currentLanguage = "english";
//			TextView textView = (TextView) v.findViewById(R.id.vocab_word_text);
//			textView.setText(vocabWord.eWord);
//		}
	}

	
	
	public void resetAdapter() {
		// TODO Rewrite resetAdapter to work with SimpleCursorAdapter
//		Log.v(TAG,"resetAdapter() has begun");
//
//		// get the current ChapterVocabList
//		HashMap<Integer,ChapterVocab.VocabWord> vocab;
//		vocab = vocabListActivity.chapterVocab.getVocabList();
//		// for each get the id into an array list
//		ArrayList<Integer> vocabIds = new ArrayList<Integer>();
//		for(Integer vocabId: vocab.keySet()){
//		8	vocabIds.add(vocabId);
//		}
//
//		// set it to the fragment with setListAdapter
//		this.setListAdapter(new VocabListAdapter(vocabListActivity, 
//				R.layout.vocab_word_cell, R.id.vocab_word_text, vocabIds));
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> av, View v, int position,	long id) {
		// TODO make updates in vocabListActivity.removeVocabWord() 
		Log.v(TAG, "onItemLongClick() clicked");
		vocabListActivity.removeVocabWord(position);
		return true;
	}
}
