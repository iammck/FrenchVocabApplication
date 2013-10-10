package com.mck.vocab.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.mck.vocab.VocabProvider;
import com.mck.vocab.fragments.OptionsDialogFragment.OptionType;
import com.mck.vocab.fragments.OptionsDialogFragment.OptionsChangedCallback;

public class ChangeLanguageDialogFragment extends DialogFragment {
	public static final String TAG = "ChangeLanguageDialogFragment";
	String[] languages = {"English","French"};
	Activity activity;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
	}



	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Set Inital Language")
		.setItems(languages, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case 0:
						((OptionsChangedCallback) activity)
							.onOptionChangedCallback(OptionType.LANGUAGE, VocabProvider.C_EWORD, false);
						break;
					case 1:
						((OptionsChangedCallback) activity)
						.onOptionChangedCallback(OptionType.LANGUAGE, VocabProvider.C_FWORD, false);
					break;
				}
			}
		});
		return builder.create();
	}
}
