/**
 * 
 */
package com.mck.vocab.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mck.vocab.R;

/**
 * @author Michael
 *
 */
public class EasyDialogAnswerFragment extends DialogFragment implements OnClickListener {

	public static final String TAG = "EasyDialogAnswerFragment";
	public static final String ANSWER = "ANSWER";
	public static final String WORDNUMBER = "wordnumber";


	private String question;
	private String answer;
	private int wordNumber;
	private EasyDialogFragmentCallback edfcb;
	public interface EasyDialogFragmentCallback{
		public void easyDialogNext(int current, boolean discardWord);
	}

	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setStyle(STYLE_NO_TITLE, getTheme());
		//this.setStyle(STYLE_NO_TITLE, 0);
	
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		edfcb = (EasyDialogAnswerFragment.EasyDialogFragmentCallback) activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.v(TAG , "onCreateView() has started");
		//Inflate the view
		View view = inflater.inflate(R.layout.easy_dialog_answer_layout, container, false);

		// request on clicks for the buttons
		Button button = (Button) view.findViewById(R.id.buttonRemove);
		button.setOnClickListener(this);
		button = (Button) view.findViewById(R.id.buttonNext);
		button.setOnClickListener(this);
		button = (Button) view.findViewById(R.id.buttonCancel);
		button.setOnClickListener(this);

		// Get and set arguments for text view
		Bundle b = getArguments();
		this.question =  b.getString(EasyDialogQuestionFragment.QUESTION);
		this.answer =  b.getString(EasyDialogAnswerFragment.ANSWER);
		this.wordNumber =b.getInt(EasyDialogAnswerFragment.WORDNUMBER);
		TextView qtv = (TextView) view.findViewById(R.id.easy_dialog_question_textview);
		qtv.setText("( "+ question + " )");
		TextView atv = (TextView) view.findViewById(R.id.easy_dialog_answer_textview);
		atv.setText(answer);

		return view;
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onStart()
	 */
	@Override
	public void onStart() {
		super.onStart();
		this.getDialog().setTitle(answer);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id){
		case R.id.buttonCancel:
			Log.v(TAG, "reacting to buttonCancel");
			dismiss();
			return;
		case R.id.buttonRemove:
			Log.v(TAG, "reacting to buttonRemove");
			dismiss();
			edfcb.easyDialogNext(this.wordNumber, true);
			return;
		case R.id.buttonNext:
			Log.v(TAG, "reacting to buttonNext");
			dismiss();
			edfcb.easyDialogNext(this.wordNumber, false);
		}
	}
}
