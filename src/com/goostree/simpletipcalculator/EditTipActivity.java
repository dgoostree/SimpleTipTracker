//
// This class needs the same love as add activity.  It used relativelayout as a means of exploring it,
//  but it is not the most appropriate layout given the design of the activity.
//
package com.goostree.simpletipcalculator;

import java.util.ArrayList;

import com.goostree.simpletipcalculator.base.TipEntryv2;
import com.goostree.simpletipcalculator.base.TipRecordv2;
import com.goostree.simpletipcalculator.base.TipoutRecordv2;
import com.goostree.simpletipcalculator.util.IOUtil;

import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EditTipActivity extends FragmentActivity {
	private final int TIPOUT_OFFSET = 1000;//odd that i chose a different range
	private final int TIPOUT_AMT_OFFSET = 1500;//for these values
	private int tipoutCount;
	private TipRecordv2 tipRecord;
	private TipEntryv2 tipEntry;
	private int year, month, day;
	private double tipOuts;
	private float scale;
	private String note;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_edit_tip);
		tipRecord = (TipRecordv2) getApplicationContext();	//get the state of the tipRecord
		scale = getBaseContext().getResources().getDisplayMetrics().density; //get screen density
		
		tipOuts = 0;		//netTips initially zero
		tipoutCount = 0;	//init tipoutCount to zero
		Intent intent = getIntent(); //get the intent
		String dateStr = intent.getStringExtra(MainActivity.FROM_DATE);		   //get date in string form
		String[] parts = dateStr.split("\\.");								//split string into month, day, year
		year = Integer.valueOf(parts[2]);									//store instances of the date components
		month = Integer.valueOf(parts[0])-1;
		day = Integer.valueOf(parts[1]);
		
		//get the entry in question
		tipEntry = tipRecord.getTipEntryByDate(year, month, day);				
		//if no tip info exists, exit
		if(tipEntry == null){
			Toast.makeText(getApplicationContext(), "No shift info for that day", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		if(tipEntry.hasNote()){  //if the entry has a note, retrieve it
			note = tipEntry.getNote();//and store it for future saving, if needed
		}
		
		EditText hourView = (EditText) findViewById(R.id.edit_hours_worked_content);
		hourView.setText( tipEntry.getHoursWorked()+"" );
		
		TextView currentView = (TextView) findViewById(R.id.edit_date_content);//reference the date label
		currentView.setText( dateStr );										   //set its text	
		
		currentView = (TextView) findViewById(R.id.edit_gross_tips_content);//reference the gross tips label
		currentView.setText( tipEntry.getGrossTips()+""); 					//set its text
		
		initializeTipoutLayout();//set up initial page state according to saved tip info
				
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.add_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		AlertDialog.Builder noteDialog = new AlertDialog.Builder(this);  //copy pasta from add activity.
		
		TextView noteTitle = new TextView(this);
		noteTitle.setGravity(Gravity.CENTER);    	
		noteTitle.setTextSize(20);
		noteTitle.setPadding(0, 0, 0, 30);
		noteTitle.setText("Add Note");
		
		final EditText noteText = new EditText(this);
		noteText.setSingleLine(false);
		noteText.setLines(4);
		noteText.setGravity(Gravity.LEFT);
		noteText.setGravity(Gravity.TOP);
		noteText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
		
		if(tipEntry.hasNote()){  //if the entry has a note, retrieve it
			noteText.setText(tipEntry.getNote());
			//note = tipEntry.getNote();//and store it for future saving, if needed
		}
		
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.addView(noteTitle);
		ll.addView(noteText);
		
		noteDialog.setView(ll);
		noteDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(noteText.getText().length() > 0){
					note = noteText.getText().toString();
				}
			}
			
		});
	
		
		
		noteText.requestFocus();
		noteText.postDelayed(new Runnable(){  //workaround to open the keyboard

			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(noteText, InputMethodManager.SHOW_IMPLICIT);
			}
			
		}, 100);
		
		
		noteDialog.show();
		return super.onOptionsItemSelected(item);
	}
	
	public void initializeTipoutLayout(){
		ArrayList<TipoutRecordv2> tipouts = tipEntry.getTipoutInfo();
		int count = tipouts.size();
		
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.edit_layout);
	
		for(int i = 0; i<count; i++){
			//build the spinner
			Spinner nameSpinner = new Spinner(this);
			nameSpinner.setId(TIPOUT_OFFSET + ++tipoutCount);
			ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>( this, android.R.layout.simple_spinner_item, tipRecord.getTipeeList() );
			nameSpinner.setAdapter(spinnerArrayAdapter);
			nameSpinner.setSelection(spinnerArrayAdapter.getPosition( tipouts.get(i).getName() ));//set default value
			
			//create the editText
			EditText amountField = new EditText(this);
			amountField.setId(TIPOUT_AMT_OFFSET + tipoutCount);
			amountField.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);	
			amountField.setGravity(Gravity.CENTER_HORIZONTAL);
			
			//set its value
			amountField.setText( tipouts.get(i).getAmount() + "");		
			
			//set spinner params
			RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 
					RelativeLayout.LayoutParams.WRAP_CONTENT); //default
			rlp.width =(int) (140 * scale + 0.5f);
			
			if(tipoutCount == 1){
				rlp.addRule(RelativeLayout.BELOW, R.id.edit_tipouts);				//if first added, add below the label
				rlp.setMargins(15, 10, 0, 0); 	//with a top margin as well
			}
			else{
				rlp.addRule(RelativeLayout.BELOW, TIPOUT_OFFSET + tipoutCount - 1); //otherwise add before previous
				rlp.setMargins(15, 0, 0, 0); 	//with a top margin as well
			}
				
			rl.addView(nameSpinner, rlp);		//add the spinner
			
			//set edittext params
			RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 
					RelativeLayout.LayoutParams.WRAP_CONTENT); //default
			rllp.addRule(RelativeLayout.ALIGN_BASELINE, TIPOUT_OFFSET + tipoutCount);	//align its baseline with the last added button
			rllp.addRule(RelativeLayout.RIGHT_OF, TIPOUT_OFFSET + tipoutCount);
			rllp.width = (int) (200 * scale + 0.5f);
			
			rl.addView(amountField, rllp); 		//add the edit text
						
			setPlusButtonPlacement(rl);
			
		}		
	}
	
	// Adds tipout to existing list.  Much the same as above but no looping, 
	//	and no setting of default spinner values/reading tip amounts
	public void addTipout(View view){	
		
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.edit_layout);
	
		//build the spinner
		Spinner nameSpinner = new Spinner(this);
		nameSpinner.setId(TIPOUT_OFFSET + ++tipoutCount);
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>( this, android.R.layout.simple_spinner_item, tipRecord.getTipeeList() );
		nameSpinner.setAdapter(spinnerArrayAdapter);
		nameSpinner.setSelection(tipoutCount - 1);

		//create the editText
		EditText amountField = new EditText(this);
		amountField.setId(TIPOUT_AMT_OFFSET + tipoutCount);
		amountField.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);	
		amountField.setGravity(Gravity.CENTER_HORIZONTAL);
			
		//set its value
		amountField.setText( "0.0");		
			
		//set spinner params
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 
				RelativeLayout.LayoutParams.WRAP_CONTENT); //default
		rlp.width = (int) (140 * scale + 0.5f);
			
		if(tipoutCount == 1){
			rlp.addRule(RelativeLayout.BELOW, R.id.edit_tipouts);				//if first added, add below the label
			rlp.setMargins(15, 10, 0, 0); 	//with a top margin as well
		}
		else{
			rlp.addRule(RelativeLayout.BELOW, TIPOUT_OFFSET + tipoutCount - 1); //otherwise add before previous
			rlp.setMargins(15, 0, 0, 0); 	//with a top margin as well
		}
			
		rl.addView(nameSpinner, rlp);		//add the spinner
			
		//set edittext params
		RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 
				RelativeLayout.LayoutParams.WRAP_CONTENT); //default
		rllp.addRule(RelativeLayout.ALIGN_BASELINE, TIPOUT_OFFSET + tipoutCount);	//align its baseline with the last added button
		rllp.addRule(RelativeLayout.RIGHT_OF, TIPOUT_OFFSET + tipoutCount);
		rllp.width =(int) (200 * scale + 0.5f);
		rl.addView(amountField, rllp); 		//add the edit text
		
		setPlusButtonPlacement(rl);
		
		
	}
	
	public void setPlusButtonPlacement(RelativeLayout rl){
		//add the plus button
		Button plusButton = (Button) findViewById(R.id.edit_plus_button); 	//get the plus button
		rl.removeView(plusButton);
				
		//plus button params
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT); // default required parameters
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					
		if( tipoutCount < tipRecord.getTipeeList().size()){ //if not at max tipout count or max number of tipees
			if(tipoutCount == 0){
				params.addRule(RelativeLayout.BELOW, R.id.edit_tipouts);//below tipouts label if none present
			}
			else {
				params.addRule(RelativeLayout.BELOW, TIPOUT_OFFSET + tipoutCount);//below last tipout otherwise
			}
						
			rl.addView(plusButton, params);	
		}
		
		//focus scrollview to bottom when new button added
		final ScrollView scroll = (ScrollView) findViewById(R.id.edit_tip_scroller);
		scroll.post(new Runnable(){

			@Override
			public void run() {
				scroll.scrollTo(0, scroll.getBottom());
				
			}
			
		});
	}
	
	//saveTip: saves the tip and returns to the main activity
	public void saveTip(View view){
			
		try {
			double gross;	
			double hours;
								
			//get gross tips amount		
			EditText textViewHolder = (EditText) findViewById(R.id.edit_gross_tips_content);
			gross = Double.valueOf( textViewHolder.getText().toString() );     //get the value of the gross tips field
				
			//get the new tipout record, which calculates net tips as well
			ArrayList<TipoutRecordv2> thisTipoutRecord = createNewTipoutRecord();
			
			//get hours worked
			EditText hourView = (EditText) findViewById(R.id.edit_hours_worked_content);
			hours = Double.valueOf( hourView.getText().toString()  );
			
			TipEntryv2 editedEntry =  new TipEntryv2(year, month, day, gross, 
					gross-tipOuts, hours, tipEntry.getWage(), thisTipoutRecord ) ;
			
			if ( note != null && note.length() > 0){
				editedEntry.setNote(note);
			}
			
			tipRecord.addRecordSorted( editedEntry);
			IOUtil.save(tipRecord);	//update tiprecord
			IOUtil.saveBackup(tipRecord);	//and backup
		}
		catch (NumberFormatException e){
			Toast.makeText(getApplicationContext(), "Invalid tip info entered", Toast.LENGTH_SHORT).show();
			return;
		}
		
			Toast.makeText(getApplicationContext(), "Tip Info Saved", Toast.LENGTH_SHORT).show();
			finish();		//if no exceptions, end the current activity
		}
	
	// Reads data from the tipout fields and creates an Arraylist<TipoutRecord> out of it
	private ArrayList<TipoutRecordv2> createNewTipoutRecord(){
		ArrayList<TipoutRecordv2> tipouts;
		double value;
		//get formatted ArrayList<TipRecord>
		tipouts = new ArrayList<TipoutRecordv2>();
		for(int i = 1; i <= tipoutCount; i++){
			Spinner name = (Spinner) findViewById(TIPOUT_OFFSET + i);		//get the spinner
			EditText amt = (EditText) findViewById(TIPOUT_AMT_OFFSET + i);		//and edittext
			
			value = Double.valueOf(amt.getText().toString());
			if(value > 0){
				tipOuts += value;															//increment netTips for this entry
				tipouts.add( new TipoutRecordv2( (String)name.getSelectedItem() , value ));  //add it to the record if the value was greater than zero
			}
		}
		
		return tipouts;
	}
	
	public void delete(View view){
		//new popup dialog
    	AlertDialog.Builder confirmDelete = new AlertDialog.Builder(this);

    	
    	TextView newView = new TextView(this);
    	newView.setGravity(Gravity.CENTER);    	
    	newView.setTextSize(20);
    	newView.setPadding(0, 30, 0, 30);
    	newView.setText("Delete this tip entry?");
    	
    	confirmDelete.setView(newView);
    	confirmDelete.setPositiveButton("Ok", new OnClickListener() {	//if confirmed, delete 

			@Override
			public void onClick(DialogInterface dialog, int which) {
				tipRecord.removeTipEntry( tipRecord.getTipEntryOffsetByDate(year, month, day) );//remove if user confirmed	
				IOUtil.save(tipRecord);
				Toast.makeText(getApplicationContext(), "Tip Deleted", Toast.LENGTH_SHORT).show();
				finish();
			}
      	});
    	
    	confirmDelete.setNegativeButton("Cancel", null);
    	confirmDelete.show();
	}


}
