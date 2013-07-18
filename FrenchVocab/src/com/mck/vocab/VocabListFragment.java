/**
 * 
 */
package com.mck.vocab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mck.vocab.ChapterVocab.VocabWord;

/**
 * @author Michael
 *
 */
public class VocabListFragment extends ListFragment implements OnItemLongClickListener {
	private static final String TAG = "VocabListFragment";
	private VocabListActivity vocabListActivity; // obtained in onAttach

	// The Adapter
	public class VocabListAdapter extends ArrayAdapter<Integer>{

		public VocabListAdapter(Context context, int resource,
				int textViewResourceId, List<Integer> idList) {
			super(context, resource, textViewResourceId, idList);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.v(TAG, "getView called for position " + position);
			View rowView = convertView;
			if (rowView == null){
				Log.v(TAG, "getView to create a new view");
				// To create a new view, first get the layout inflater
				LayoutInflater inflater = (LayoutInflater) vocabListActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.vocab_word_cell, parent, false);
			}
			// get the view from the row view to show the text.
			TextView textView = (TextView) rowView.findViewById(R.id.vocab_word_text);			
			VocabWord vocabWord;
			
			// get the vocabWord for the position
			vocabWord =	vocabListActivity.chapterVocab.getVocabList()
										.get(Integer.valueOf(position));
			String englishKey = vocabListActivity.getResources().getString(R.string.display_english_language);
			String currentlyDisplayedLanguage = vocabWord.currentLanguage;
			if(currentlyDisplayedLanguage.equals(englishKey)){
				// show the english side of a vocabWord from chapterVocab
				textView.setText(vocabWord.eWord);
			}else{
				// show the french side of the vocabWord
				textView.setText(vocabWord.fWord);
			}
			
			return rowView;
		}
				
	}

	
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

		// get the current ChapterVocabList
		HashMap<Integer,ChapterVocab.VocabWord> vocab;
		vocab = vocabListActivity.chapterVocab.getVocabList();
		// for each get the id into an array list
		ArrayList<Integer> vocabIds = new ArrayList<Integer>();
		for(Integer vocabId: vocab.keySet()){
			vocabIds.add(vocabId);
		}

		// set it to the fragment with setListAdapter
		//this.setListAdapter(new VocabListAdapter(vocabListActivity, 
		//		R.layout.vocab_word_cell, R.id.vocab_word_text, vocabIds));
		
		
		// set the adapter to a simple list adapter TODO
		this.setListAdapter(new SimpleCursorAdapter(vocabListActivity, R.layout.vocab_word_cell, null, null, null, 0));
		
		
		
		// add item long press call back
		this.getListView().setLongClickable(true);
		this.getListView().setOnItemLongClickListener(this);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.v(TAG, "onListItemClick for position " + position);
		// get the vocabWord
		VocabWord vocabWord = vocabListActivity.chapterVocab.getVocabList().get(Integer.valueOf(position));
		// change the current language and update the textView the other language
		if (vocabWord.currentLanguage.equals("english")){
			// change to french stuff
			vocabWord.currentLanguage = "french";
			TextView textView = (TextView) v.findViewById(R.id.vocab_word_text);
			textView.setText(vocabWord.fWord);
		} else {
			// change to french stuff
			vocabWord.currentLanguage = "english";
			TextView textView = (TextView) v.findViewById(R.id.vocab_word_text);
			textView.setText(vocabWord.eWord);
		}
	}

	
	
	public void resetAdapter() {
		Log.v(TAG,"resetAdapter() has begun");

		// get the current ChapterVocabList
		HashMap<Integer,ChapterVocab.VocabWord> vocab;
		vocab = vocabListActivity.chapterVocab.getVocabList();
		// for each get the id into an array list
		ArrayList<Integer> vocabIds = new ArrayList<Integer>();
		for(Integer vocabId: vocab.keySet()){
			vocabIds.add(vocabId);
		}

		// set it to the fragment with setListAdapter
		this.setListAdapter(new VocabListAdapter(vocabListActivity, 
				R.layout.vocab_word_cell, R.id.vocab_word_text, vocabIds));
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> av, View v, int position,	long id) {
		Log.v(TAG, "onItemLongClick() clicked");
		vocabListActivity.removeVocabWord(position);
		return true;
	}
}
