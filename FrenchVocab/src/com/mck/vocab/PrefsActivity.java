/**
 * 
 */
package com.mck.vocab;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * @author Michael
 *
 */
public class PrefsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
	}

}
