package com.mck.vocab;
import com.mck.vocab.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 
 */

/**
 * The place hoder frag for when the list is being initialized.
 * @author Michael
 *
 */
public class LoadingFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		return inflater.inflate(R.layout.loading_fragment_layout, container, false);
		
	}

}
