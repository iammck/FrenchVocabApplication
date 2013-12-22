package com.mck.vocab.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mck.vocab.R;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class MultiDialogQuestionFragment extends DialogFragment implements
		OnClickListener, OnItemClickListener {
	public static final String TAG = "MultiDialogQuestionFragment";
	public static final String QUESTION = "QUESTION";
	public static final String ANSWER = "ANSWER";
	public static final String STATEMENT = "STATEMENT";
	public static final String WRONG_ANSWER = "WRONG_ANSWER";
	public static final String WORDNUMBER = "WORDNUMBER";
	
	MultiDialogFragmentCallback mdfcb;
	// the activity
	String question;
	String answer;
	String statement;
	String[] wAnswers;
	int wordNumber;
	int answerPosition;
	boolean isAnswered;
	AnswerArrayAdapter adapter;
	
	public interface MultiDialogFragmentCallback{
		public void quizDialogNext(int current, boolean discardWord);
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setStyle(STYLE_NO_TITLE, R.style.MyDialogTheme);
		//this.setStyle(STYLE_NO_TITLE, getTheme());
		//this.setStyle(STYLE_NORMAL,STYLE_NO_TITLE );
		//this.setStyle(STYLE_NO_TITLE, 0);
		isAnswered = false;
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mdfcb = (MultiDialogFragmentCallback) activity;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// inflate the view
		View view = inflater.inflate(R.layout.multi_dialog_question_layout, container, false);
		// request on clicks for the buttons
		Button button = (Button) view.findViewById(R.id.buttonNext);
		button.setOnClickListener(this);
		
		// Get and set arguments for text view
		Bundle b = getArguments();
		question = b.getString(QUESTION);
		statement = b.getString(STATEMENT);
		answer = b.getString(ANSWER);
		wAnswers = b.getStringArray(WRONG_ANSWER);
		wordNumber = b.getInt(WORDNUMBER);
		
		TextView tv = (TextView) view.findViewById(R.id.multi_dialog_question_textview);
		tv.setText(question);
		tv = (TextView) view.findViewById(R.id.multi_dialog_statement_textview);
		tv.setText(statement);
		
		// get number of wrong answers plus right answers available
		int count = wAnswers.length + 1; 
		// pick position for right answer
		answerPosition = (int) (Math.random() * 1000) % count;
		// create an array for word list data
		String[] wordList = new String[count];
		// create an array for boolean word picked.
		boolean[] wordPicked = new boolean[count];
		// also need object numbers
		Integer[] objectNumbers = new Integer[count];
		// fill up the answers list and word picked list (as all false)
		for(int x = 0, mod = 0; x < count; x++){
			if(answerPosition == x){
				wordList[x] = answer;
				mod = 1;
			}else{
				wordList[x] = wAnswers[x - mod];
			}
			wordPicked[x] = false;
			objectNumbers[x] = Integer.valueOf(x);
		}
		// create the adapter
		AnswerArrayAdapter adapter;
		adapter = new AnswerArrayAdapter(getActivity(), 0, wordList, wordPicked, objectNumbers);
		// create and set up the list view
		ListView list = (ListView) view.findViewById(R.id.multi_posssible_answer_list);
		// attach adapter.
		this.adapter = adapter;
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
		return view;
	}	

	@Override
	public void onStart() {
		super.onStart();
		this.getDialog().setTitle(question);
	}
	
	@Override
	public void onClick(View view) {
		// get the id of the view
		int viewId = view.getId();
		switch (viewId){
		case R.id.buttonCancel:
			dismiss();
			return;
		case R.id.buttonRemove:
			//Log.v(TAG, "reacting to buttonRemove");
			dismiss();
			mdfcb.quizDialogNext(this.wordNumber, true);
			return;
		case R.id.buttonNext:
			// if there is no button cancel, then next is to cancel.
			if (isAnswered == false){
				dismiss();
				return;
			}
			//Log.v(TAG, "reacting to buttonNext");
			dismiss();
			mdfcb.quizDialogNext(this.wordNumber, false);
		}
		//Log.v(TAG, "onclick and not button cancel.");
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// if the position is correct, load up the multiDialogAnswerFragment
		if (position == answerPosition){
			// having the dialog update itself while faded out may require an asynchTask
			// and since it is messing with the ui it needs to be sure to run on UI thread.
			// fade out TODO
			// get the dialog view
			View dialogView = getView();
			// now create the fade effect and get it started.
			ObjectAnimator fadeOut = ObjectAnimator.ofFloat(dialogView, "alpha", .0f);
			fadeOut.setDuration(200);
			ObjectAnimator fadeIn = ObjectAnimator.ofFloat(dialogView, "alpha", 0, 1);
			fadeIn.addListener(new CorrectAnswerAnimationCallback(this));
			fadeIn.setStartDelay(10);
			fadeIn.setDuration(220);
			AnimatorSet animation = new AnimatorSet();
			animation.playSequentially(fadeOut, fadeIn);
			animation.start();

//			// set answered
//			adapter.answer();
//			// get the buttons
//			Button next = (Button) getView().findViewById(R.id.buttonNext);
//			Button remove = (Button) getView().findViewById(R.id.buttonRemove);
//			next.setClickable(true);
//			remove.setClickable(true);
//			next.setVisibility(View.VISIBLE);
//			remove.setVisibility(View.VISIBLE);
//			next.setOnClickListener(this);
//			remove.setOnClickListener(this);
//			// notify the adapter of the update
//			adapter.notifyDataSetChanged();	
//			// fade  
		} else { 
			((AnswerArrayAdapter)adapter).wordPicked[position] = true;
			// notify the adapter of the update
			adapter.notifyDataSetChanged();	
		}
				
	}

	public class AnswerArrayAdapter extends ArrayAdapter<Integer>{
		String[] wordList;
		boolean[] wordPicked;
		
		public AnswerArrayAdapter(Context context, int resource,
				String[] wordList, boolean[] wordPicked, Integer[] objectNumbers) {
			super(context, resource, objectNumbers);
			this.wordList = wordList;
			this.wordPicked = wordPicked;
		}
		
		public void answer() {
			wordList = new String[1];
			wordList[0] = answer;
			wordPicked = new boolean[1];
			wordPicked[0] = true;
			answerPosition = 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				// create the new view
				LayoutInflater inflater = (LayoutInflater) getContext()
									.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(
						R.layout.multi_dialog_question_cell_layout, null);
			}
			
			TextView textView = (TextView) convertView.findViewById(R.id.textView);
			ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
			textView.setText(wordList[position]);
			textView.setVisibility(View.VISIBLE);
			if(wordPicked[position] == true){
				if(position == answerPosition){
					imageView.setVisibility(View.VISIBLE);
					imageView.setImageResource(R.drawable.ic_correct);
				} else { 
					imageView.setVisibility(View.VISIBLE);
					imageView.setImageResource(R.drawable.ic_incorrect);
				}
			}else{				
				imageView.setVisibility(View.INVISIBLE);
				imageView.setImageResource(R.drawable.ic_question_mark_blue_md);
			}
			return convertView;
		}

		@Override
		public int getCount() {
			return wordList.length;
		}
	}


private class CorrectAnswerAnimationCallback implements AnimatorListener{
	private MultiDialogQuestionFragment parent;

	public CorrectAnswerAnimationCallback(MultiDialogQuestionFragment parent){
		this.parent = parent;
	}
	
	@Override
	public void onAnimationStart(Animator animation) {
		// set answered
		isAnswered = true;
		
		adapter.answer();
		// get the buttons
		Button next = (Button) getView().findViewById(R.id.buttonNext);
		Button remove = (Button) getView().findViewById(R.id.buttonRemove);
		Button cancel = (Button) getView().findViewById(R.id.buttonCancel);
		next.setText(R.string.next);
		
		cancel.setClickable(true);
		remove.setClickable(true);
		cancel.setVisibility(View.VISIBLE);
		remove.setVisibility(View.VISIBLE);
		cancel.setOnClickListener(parent);
		remove.setOnClickListener(parent);
		// notify the adapter of the update
		adapter.notifyDataSetChanged();			
		
			
	}

	@Override
	public void onAnimationEnd(Animator animation) {
	}

	@Override
	public void onAnimationCancel(Animator animation) {
		
	}

	@Override
	public void onAnimationRepeat(Animator animation) {
	}
	
}


	

}
