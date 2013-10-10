/**
 * 
 */
package com.mck.vocab.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

/**
 * @author Michael
 *
 */
public class OptionsDialogFragment extends DialogFragment {
	Activity activity;
	String[] options = {"Set Quiz Type","Set Inital Language"};
	public static final String TAG = "OptionsDialogFragment";
	
	public interface OptionsChangedCallback{
		public void onOptionChangedCallback(OptionType type, String language, boolean isEasy);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Options").setItems(options, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
				case 0: // quiz type
					FragmentManager fragMan = getFragmentManager();
					ChangeQuizTypeDialogFragment frag = new ChangeQuizTypeDialogFragment();
					frag.show(fragMan, ChangeQuizTypeDialogFragment.TAG);
					break;
				case 1: // initial language
					FragmentManager fragMan2 = getFragmentManager();
					ChangeLanguageDialogFragment frag2 = new ChangeLanguageDialogFragment();
					frag2.show(fragMan2, ChangeLanguageDialogFragment.TAG);
					break;
				}
			}			
		});
		return builder.create();
	}

	public enum OptionType {
		LANGUAGE, QUIZ
	}

}
