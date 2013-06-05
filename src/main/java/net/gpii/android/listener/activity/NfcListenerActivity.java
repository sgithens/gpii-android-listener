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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import net.gpii.android.listener.Constants;
import net.gpii.android.listener.R;
import net.gpii.android.listener.listener.ActivityQuitListener;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import android.app.Activity;
import android.net.http.AndroidHttpClient;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class NfcListenerActivity extends Activity {
	private ProgressBar progressView;
	private TextView statusTextView;

	// not really thread safe correctly yet, but ok for this demo hopefully
	// these 3 variables all keep the state
	private static Date lastLogin = null;
	private static boolean logging_in_or_out = false;
	private static boolean logged_in = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.nfc);

		statusTextView = (TextView) findViewById(R.id.nfcStatusText);
		progressView = (ProgressBar) findViewById(R.id.nfcProgressBar);
		
		Button closeButton = (Button) findViewById(R.id.nfcExitButton);
		closeButton.setOnClickListener(new ActivityQuitListener(this));
		
		try
		{
			Parcelable[] messages = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			NdefMessage message = (NdefMessage) messages[0];
			NdefRecord record = message.getRecords()[0];
			
			byte[] payload = record.getPayload();
			//		String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
			String textEncoding = "UTF-8";
			int languageCodeLength = payload[0] & 0077;
			
			String username = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
			if (username.trim().length() == 0) {
				String errorMessage = "Empty username found on NFC tag.";
				statusTextView.setText(errorMessage);
				progressView.setVisibility(View.INVISIBLE);
			}
			else {
				GpiiLoginAsyncTask task = new GpiiLoginAsyncTask();
				task.execute(username.trim());
			}
		}
		catch (UnsupportedEncodingException e)
		{
			String errorMessage = "Error unpacking NFC text record.";
			statusTextView.setText(errorMessage);
			progressView.setVisibility(View.INVISIBLE);
			Log.e(Constants.TAG, errorMessage, e);
		}
	}
	
	private class GpiiLoginAsyncTask extends AsyncTask<String,String,Long> {
		@Override
		protected Long doInBackground(String... params) {
			if (lastLogin != null && ((((new Date()).getTime()-lastLogin.getTime())/1000)>5)) {
				logging_in_or_out = false;
			}
			if (logging_in_or_out == false) {
				logging_in_or_out = true;
				lastLogin = new Date();
				String username = params[0];
				String resultMessage = "...";
				try
				{
					URL loginUrl = null;
					if (logged_in == false) {
						loginUrl = new URL(Constants.USER_REST_URL + "/" + username + "/login");
						logged_in = true;
					}
					else {
						loginUrl = new URL(Constants.USER_REST_URL + "/" + username + "/logout");
						logged_in = false;
					}
					// Try to log in 
					AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Android Device");
					HttpGet httpGet = new HttpGet(loginUrl.toURI());
					HttpResponse httpResponse = httpClient.execute(httpGet);
	
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					switch (statusCode) {
						case HttpStatus.SC_OK:
						case HttpStatus.SC_ACCEPTED:
							resultMessage = "Logged in as user '" + username + "'." ;
							Log.d(Constants.TAG, resultMessage);
							break;
						case HttpStatus.SC_NOT_FOUND:
							resultMessage = "GPII server not found.";
							Log.e(Constants.TAG, resultMessage);
							break;
						case HttpStatus.SC_INTERNAL_SERVER_ERROR:
							resultMessage = "Unknown error logging in, status code " + statusCode;
							Log.e(Constants.TAG, resultMessage);
							break;
						default:
							resultMessage = "Unknown login results with status code " + statusCode;
							Log.e(Constants.TAG, resultMessage);
							break;
					}
				}
				catch (URISyntaxException e)
				{
					resultMessage = "Error converting login URL to URI.";
					Log.e(Constants.TAG, resultMessage, e);
				}
				catch (IOException e)
				{
					resultMessage = "GPII server not found.";
					Log.e(Constants.TAG, resultMessage, e);
				}
				
				publishProgress(resultMessage);
			}	
			return 0L;
		}
		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			
			statusTextView.setText(values[0]);
			progressView.setVisibility(View.INVISIBLE);
		}
	}
	
}
