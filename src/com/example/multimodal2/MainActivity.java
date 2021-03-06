package com.example.multimodal2;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import multimodal.schedule.Room;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener, OnInitListener {

	protected static final int VR_REQUEST = 999;
	private int MY_DATA_CHECK_CODE = 0;
	protected TextToSpeech tts;
	protected RDFModel rdfModel;
	private UserCommunication uc;
	
	/**
	 * Create the main Activity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button speechBtn = (Button) findViewById(R.id.speech_btn);
		Button addEvent = (Button) findViewById(R.id.add_event);
		
		this.rdfModel = new RDFModel(this);	
		this.uc = new UserCommunication(this);
		
		PackageManager packManager = getPackageManager();
		List<ResolveInfo> intActivities = packManager.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (intActivities.size() != 0) {
			speechBtn.setOnClickListener(this);
			addEvent.setOnClickListener(this);
		} else {
			speechBtn.setEnabled(false);
			Toast.makeText(this, "Oops - Speech recognition not supported!", Toast.LENGTH_LONG).show();
		}
		Intent checkTTSIntent = new Intent();
		checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
		
		Spinner spinner = (Spinner) findViewById(R.id.current_place);		
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item);

		for(Room room : rdfModel.createRoomsFromRDF() ) {
			spinnerArrayAdapter.add(room.getName());
		}		
		spinner.setAdapter(spinnerArrayAdapter);		
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {	
					uc.setLocationContext(((TextView) arg1).getText().toString());					
			}	
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
					
			}			
		});				
	}

	@Deprecated
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	/**
	 * setup TextToSpeech language
	 */
	@Override
	public void onInit(int arg0) {
		if (arg0 == TextToSpeech.SUCCESS)
			tts.setLanguage(Locale.UK);
	}

	/**
	 * onClick handlers for the speech recognition button
	 */
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.speech_btn) {
			uc.askForUserSpeechInput("What do you want to do?");
		} else if (v.getId() == R.id.add_event) {
			uc.InputFromUser((((EditText) findViewById(R.id.editText1)).getText().toString()));
		}
	}

	/**
	 * Receive the results of the other activities
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VR_REQUEST && resultCode == RESULT_OK)
		{
			ArrayList<String> suggestedWords = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			uc.InputFromUser(suggestedWords.get(0));
		} 
		else if (requestCode == MY_DATA_CHECK_CODE)
		{
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				tts = new TextToSpeech(this, this);
			} 
			else 
			{
				Intent installTTSIntent = new Intent();
				installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installTTSIntent);
			}
		} else if(requestCode == 3 && resultCode == RESULT_OK) {
			uc.InputFromUser("yes");
		} else if(requestCode == 3 && resultCode == RESULT_CANCELED) {
			uc.InputFromUser("no");
		}		
		super.onActivityResult(requestCode, resultCode, data);
	}	
}
