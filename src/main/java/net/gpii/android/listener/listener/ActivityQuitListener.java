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
package net.gpii.android.listener.listener;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

public class ActivityQuitListener implements OnClickListener {
	private final Activity activity;
	
	public ActivityQuitListener(Activity activity) {
		super();
		this.activity = activity;
	}

	@Override
	public void onClick(View v) {
		activity.finish();
	}
}
