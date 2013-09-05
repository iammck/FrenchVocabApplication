package com.mck.vocab.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.mck.vocab.R;

public class FirstLoadDialog extends DialogFragment implements OnClickListener {

	public static final String TAG = "FirstLoadDialog";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// inflate the view
		View view = inflater.inflate(R.layout.first_load_dialog_fragment_layout, container);
		// set the on click listener
		getDialog().setTitle("Thanks!");
		Button button = (Button) view.findViewById(R.id.buttonOk);
		button.setOnClickListener(this);
		return view;
	}

	@Override
	public void onClick(View v) {
		this.dismiss();
	}
}
