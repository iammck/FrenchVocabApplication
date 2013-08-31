/**
 * 
 */
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
import android.widget.Button;

import com.mck.vocab.R;
import com.mck.vocab.VocabProvider;

/**
 * @author Michael
 *
 */
public class ChangeLanguageDialogFragment extends DialogFragment implements OnClickListener {
	public static final String TAG = "ChangeLanguageDialogFragment";
	ChangeLanguageCallback clcb;
	public interface ChangeLanguageCallback{
		public void onChangeLanguageCallback(String language);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try{
			this.clcb = (ChangeLanguageCallback) activity;
		} catch (Exception e){
			e.printStackTrace();
			Log.e(TAG, "The ChangeLanguageDialogFragment on attach Activity needs to implement"
					+ " the ChangelanguageCallback interface.");
		}
	}	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// inflate the view
		View view = inflater.inflate(R.layout.change_language_dialog_fragment_layout, container);
		// set the on click listener
		Button button = (Button) view.findViewById(R.id.buttonEnglish);
		button.setOnClickListener(this);
		button = (Button) view.findViewById(R.id.buttonFrench);
		button.setOnClickListener(this);
		button = (Button) view.findViewById(R.id.buttonCancel);
		button.setOnClickListener(this);
		return view;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return super.onCreateDialog(savedInstanceState);
	}

	@Override
	public void onClick(View button) {
		int id = button.getId();
		switch (id){
			case R.id.buttonEnglish:
				this.dismiss();
				clcb.onChangeLanguageCallback(VocabProvider.C_EWORD);
				break;
			case R.id.buttonFrench:
				this.dismiss();
				clcb.onChangeLanguageCallback(VocabProvider.C_FWORD);
				break;
			case R.id.buttonCancel:
				this.dismiss();
				break;
		}
	}
	
	

}
