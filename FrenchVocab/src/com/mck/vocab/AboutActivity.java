/**
 * 
 */
package com.mck.vocab;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

/**
 * @author Michael
 *
 */
public class AboutActivity extends FragmentActivity {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_activity_layout);		
	}

	public void backFromAboutButton(View view){
		this.finish();
	}

}
