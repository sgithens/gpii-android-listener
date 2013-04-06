/**
 * ${project.name}
 *
 * Copyright 2013 Tony Atkins
 *
 * Licensed under the New BSD license. You may not use this file except in
 * compliance with this License.
 *
 * You may obtain a copy of the License at
 * https://github.com/gpii/universal/LICENSE.txt
 */
package net.gpii.android.listener.activity;

import net.gpii.android.listener.R;
import net.gpii.android.listener.listener.ActivityQuitListener;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

public class StartupActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.startup);

		Button exitButton = (Button) findViewById(R.id.dummy_button);
		exitButton.setOnClickListener(new ActivityQuitListener(this));
	}
}
