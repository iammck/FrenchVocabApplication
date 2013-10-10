package com.mck.vocab.fragments;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.mck.vocab.R;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * @author Michael
 *
 */
public class EasyDialogFragment extends DialogFragment implements OnClickListener {
	public static final String TAG = "EasyDialogQuestionFragment";
	public static final String QUESTION = "QUESTION";
	public static final String ANSWER = "ANSWER";
	public static final String WORDNUMBER = "wordnumber";

	// the activity
	public interface EasyDialogFragmentCallback{
		public void quizDialogNext(int current, boolean discardWord);
	}
	EasyDialogFragmentCallback edfcb;
	private String question;
	private String answer;
	private int wordNumber;
	private boolean isFlipped;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setStyle(STYLE_NO_TITLE, R.style.MyDialogTheme);
		//this.setStyle(STYLE_NO_TITLE, getTheme());
		//this.setStyle(STYLE_NORMAL,STYLE_NO_TITLE );
		//this.setStyle(STYLE_NO_TITLE, 0);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		edfcb = (EasyDialogFragmentCallback) activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// inflate the view
		View view = inflater.inflate(R.layout.easy_dialog_question_layout, container, false);
		// request on clicks for the buttons
		Button button;
		button= (Button) view.findViewById(R.id.buttonRemove);
		button.setOnClickListener(this);
		button = (Button) view.findViewById(R.id.buttonEasy);
		button.setOnClickListener(this);
		button = (Button) view.findViewById(R.id.buttonCancel);
		button.setOnClickListener(this);		
		// Get and set arguments
		Bundle b = getArguments();
		this.question = b.getString(EasyDialogFragment.QUESTION);
		this.answer =  b.getString(ANSWER);
		this.wordNumber =b.getInt(WORDNUMBER);
		// set the initial text view text.
		TextView tv = (TextView) view.findViewById(R.id.easy_dialog_question_textview);
		tv.setText(question);
		return view;
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onStart()
	 */
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
			break;
		case R.id.buttonEasy:
 			if (isFlipped == true){
				Log.v(TAG, "reacting to buttonNext");
				dismiss();
				edfcb.quizDialogNext(this.wordNumber, false);
			} else {
				flip();
			}
			break;
		case R.id.buttonRemove:
			Log.v(TAG, "reacting to buttonRemove");
			dismiss();
			edfcb.quizDialogNext(this.wordNumber, true);
			return;	
		}
	}
	
	private void flip(){
		// set isFlipped
		isFlipped = true;
		// Get the view
		View view = getView();
		// set up the buttons
		Button button;
		button= (Button) view.findViewById(R.id.buttonRemove);
		ObjectAnimator.ofFloat(button, "alpha", 0, 1).setDuration(180).start();
		button.setVisibility(View.VISIBLE);		
		button = (Button) view.findViewById(R.id.buttonEasy);
		button.setText(R.string.next);
		button = (Button) view.findViewById(R.id.buttonCancel);
		// Set arguments for text view
		TextView textView;
		textView =(TextView) view.findViewById(R.id.easy_dialog_question_mark_textview);
		textView.setVisibility(View.INVISIBLE);
		textView = (TextView) view.findViewById(R.id.easy_dialog_question_textview);
		textView.setText(question);
		textView = (TextView) view.findViewById(R.id.easy_dialog_answer_textview);
		textView.setText(answer);
		textView.setVisibility(View.VISIBLE);
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {	
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		return dialog;
	}
}