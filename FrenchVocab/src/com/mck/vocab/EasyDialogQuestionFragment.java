package com.mck.vocab;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author Michael
 *
 */
public class EasyDialogQuestionFragment extends DialogFragment implements OnClickListener {
	public static final String TAG = "EasyDialogQuestionFragment";
	public static final String QUESTION = "QUESTION";
	// the activity
	EasyDialogAnswerFragment.EasyDialogFragmentCallback edfcb;
	private String question;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
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
		// inflate the view
		View view = inflater.inflate(R.layout.easy_dialog_question_layout, container, false);
		// request on clicks for the buttons
		Button button = (Button) view.findViewById(R.id.buttonTranslate);
		button.setOnClickListener(this);
		button = (Button) view.findViewById(R.id.buttonCancel);
		button.setOnClickListener(this);
		// Get and set arguments for text view
		Bundle b = getArguments();
		this.question = b.getString(EasyDialogQuestionFragment.QUESTION);
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
		if (viewId == R.id.buttonCancel){
			dismiss();
			return;
		}
		Log.v(TAG, "begining to translate");
		// dismiss this dialog frag and get the fragManager
		dismiss();
		FragmentManager fragMan = getFragmentManager();
		// Create and set up the answer in easyDialogAnswerFragment 
		EasyDialogAnswerFragment answerFrag = new EasyDialogAnswerFragment();
		// Set up the frags arguments
		answerFrag.setArguments(this.getArguments()); // questions come presupplied with answers.
		// show the easyDialogAnswerFragment
		answerFrag.show(fragMan, TAG);
		//ragMan.beginTransaction().remove(this).add(answerFrag, TAG).commit();
	}
	

}