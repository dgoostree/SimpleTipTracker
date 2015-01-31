//
// Used this class as a way to learn about programmatically manipulating 
//  android layouts.  
//
// TODO: Rewrite me.  Relativelayout was used to gain experience using it, but the process of
//	adding/removing elements based on user input would be best handled by android by use of
//	nested linearlayouts for tipee line items, and the plus/minus button in another layout positioned 
//	beneath it.  Reinventing the wheel for education's sake, I suppose. 
//
package com.goostree.simpletipcalculator;

import java.util.ArrayList;
import java.util.Calendar;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.goostree.simpletipcalculator.base.TipEntryv2;
import com.goostree.simpletipcalculator.base.TipRecordv2;
import com.goostree.simpletipcalculator.base.TipoutRecordv2;
import com.goostree.simpletipcalculator.util.IOUtil;

public class AddActivity extends FragmentActivity {
	private EditText valueText;
	private TipRecordv2 tipRecord;
	private String note;  //text to hold the note, if added
	private int dynamicButtons = 0;  //number of tip outs added
	private final int TIPEE_NAME_ID_OFFSET = 100; //these values impose a limit on how many tippees can exist for a single shift
	private final int TIPEE_AMT_ID_OFFSET = 200;  //I don't expect this to be a problem in practice
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add);
				
		//
		tipRecord = (TipRecordv2)getApplicationContext();
		 
		//remove plus button if tipee list is empty
		if(tipRecord.getTipeeList().size() == 0){
			findViewById(R.id.plus_button).setVisibility(View.GONE);
		}
		
		//ensure only single decimal place in hours field
		EditText hoursWorked = (EditText)findViewById(R.id.hours_worked_edit_text);
		hoursWorked.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);//only one decimal possible
		
		
		//respond to changes in the gross tips field
		valueText = (EditText) findViewById(R.id.gross_tips_edit_text);  	
		valueText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);//only one decimal possible
		valueText.addTextChangedListener( new TipWatcher() );//addTextChangedListener
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
		
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		AlertDialog.Builder noteDialog = new AlertDialog.Builder(this);
		
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
		
		if(note != null && note.length() > 0){
			noteText.setText(note);
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
	
	//handles the clicking of the date line
	public void setDate(View view){
		DialogFragment newFragment = new GetDateFragment();
		newFragment.show(this.getSupportFragmentManager(), "DatePicker");
	}
	
	void setDateText(int year, int month, int day){
		TextView text = (TextView) findViewById(R.id.date_edit_text);		//obtain reference to the text field
		text.setText(month+"."+day+"."+year);							    //change its contents
	}

	//adds fields for new tipees. This method needs a cleanup. 
	public void addTipee(View view){	
		final float scale = getBaseContext().getResources().getDisplayMetrics().density;  //the scale for dynamic alignment
					
		//build the spinner
		Spinner nameSpinner = new Spinner(this);
		nameSpinner.setId(TIPEE_NAME_ID_OFFSET + ++dynamicButtons);
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>( this, android.R.layout.simple_spinner_item, tipRecord.getTipeeList() );
		nameSpinner.setAdapter(spinnerArrayAdapter);
		nameSpinner.setSelection(dynamicButtons - 1); //set default value

		//create the editText
		EditText amountField = new EditText(this);
		amountField.setId(TIPEE_AMT_ID_OFFSET + dynamicButtons); //set an id
		amountField.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);	
		amountField.addTextChangedListener( new TipWatcher() );
		amountField.setGravity(Gravity.CENTER_HORIZONTAL);
		
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.add_page_layout);  //get the layout view
		
		// spinner relative layout params		
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT); // default required parameters
		rlp.width = (int) (140 * scale + 0.5f);
		rlp.leftMargin = 15;
		
		//edittext relative layout params
		RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT); // default required parameters
		rllp.width = (int) (200 * scale + 0.5f);
		
		
		if(dynamicButtons == 1){
			rlp.addRule(RelativeLayout.BELOW, R.id.add_tip_tipee);   //if it's the first spinner place it below the label
			rllp.addRule(RelativeLayout.BELOW, R.id.add_tip_tipee);   //if it's the first edittext place it below the label
		}
		else {
			rlp.addRule(RelativeLayout.BELOW, (nameSpinner.getId() - 1) );  //otherwise place it below the last button added
			rllp.addRule(RelativeLayout.BELOW, (nameSpinner.getId() - 1) );  //otherwise place it below the last button added
		}
		
		rl.addView(nameSpinner, rlp);		//add the new button to the layout. CHANGED THIS TO SPINNER
		rllp.addRule(RelativeLayout.RIGHT_OF, nameSpinner.getId()); //set the text field to be the right
		rl.addView(amountField, rllp);  //add the text field
		
		Button plusButton = (Button) findViewById(R.id.plus_button); 	//get the plus button
		Button minusButton = (Button) findViewById(R.id.minus_button);  //get reference to the button
		
		//minus button parameters
		RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT); // default required parameters
		params2.addRule(RelativeLayout.BELOW, nameSpinner.getId());		   // below the latest button, like plus button
		
		//plus button params
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT); // default required parameters
		params.addRule(RelativeLayout.BELOW, nameSpinner.getId());//below the name selection
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		
				
		//handle button display depending on button count
		if( dynamicButtons < tipRecord.getTipeeList().size()){   					
			params2.addRule(RelativeLayout.LEFT_OF, R.id.plus_button);			
		}	
		else {
			params2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			plusButton.setVisibility(View.GONE);
			
		}
		
		rl.removeView(plusButton);		//remove the button
		rl.addView(plusButton, params);//re-add plusButton new position
				
		if(dynamicButtons > 0){
			minusButton.setVisibility(View.VISIBLE);  //set visibility 
		}
		
		rl.removeView(minusButton);  			//remove that bad bitch, even if it was invisible it must be removed			
		minusButton.setVisibility(View.VISIBLE);//set it to visible
		rl.addView(minusButton, params2);		
		
		//focus scrollview to bottom when new button added
		final ScrollView scroll = (ScrollView) findViewById(R.id.view_scroller);
		scroll.post(new Runnable(){

			@Override
			public void run() {
				scroll.scrollTo(0, scroll.getBottom());
				
			}
			
		});
		
		
	}//addTipee
	
	public void removeTipee(View view) {
		Spinner theSpinner = (Spinner) findViewById(TIPEE_NAME_ID_OFFSET + dynamicButtons);		//reference to spiner to be removed
		EditText theEditText = (EditText) findViewById(TIPEE_AMT_ID_OFFSET + dynamicButtons);	//likewise for edittext
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.add_page_layout);				//get the layout
		dynamicButtons--;																		//decrement the amount of tip outs
		
		//remove the last spinner and edittext added
		rl.removeView(theSpinner);
		rl.removeView(theEditText);
		
		Button plusButton = (Button) findViewById(R.id.plus_button);	//get reference to the plus button
		if( dynamicButtons >= tipRecord.getTipeeList().size() - 3){
			plusButton.setVisibility(View.VISIBLE); 					//if it's not visible, make it so
		}		
		rl.removeView(plusButton); 										//remove it from the layout		
		
		Button minusButton = (Button) findViewById(R.id.minus_button);	//get reference to the minus button
		rl.removeView(minusButton);										//remove it from the layout
		
		//create layout params for the plus button
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT); // default required parameters
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		
		//create layout params for the minus button
		//minus button parameters
		RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT); // default required parameters
		
		//add one or both to the correct spot depending on dynamicButtons value
		if(dynamicButtons > 0){	
			params.addRule(RelativeLayout.BELOW, TIPEE_NAME_ID_OFFSET + dynamicButtons);//below the new last name selection
			params2.addRule(RelativeLayout.BELOW, TIPEE_NAME_ID_OFFSET + dynamicButtons);// below the last button, like plus button
			params2.addRule(RelativeLayout.LEFT_OF, R.id.plus_button);// below the last button, like plus button
		}
		else {
			params.addRule(RelativeLayout.BELOW, R.id.add_tip_tipee);//otherwise below the label
			minusButton.setVisibility(View.GONE);			
		}
		
		rl.addView(plusButton, params);		
		rl.addView(minusButton, params2);	
		
		setNetTips();
	}
	
	//saveTip: saves the tip and returns to the main activity
	public void saveTip(View view){
		
		try {
			int year;
			int month;
			int day;
			double grossTips;	
			double netTips;
			double hours;
			
			//get the date in int form
			TextView dateText = (TextView) findViewById(R.id.date_edit_text);	//get the date's view
			if(dateText.getText().length() == 0){
				throw new IllegalArgumentException();		//see if date was entered, if not exception (yeah yeah...)
			}
			
			String temp = dateText.getText().toString();	//get it in string format
			String[] temp2 = temp.split("\\.");				//split it into it's parts
			month = Integer.valueOf( temp2[0]) - 1;			//Calendar months start at 0, so subtract 1 
			day = Integer.valueOf( temp2[1] );
			year = Integer.valueOf( temp2[2] );				//store the date in int format
			
			//get hours worked
			EditText hoursView = (EditText) findViewById(R.id.hours_worked_edit_text);
			
			
			hours = Double.valueOf( hoursView.getText().toString()  );
			
			//get gross tips amount		
			EditText grossTipsView = (EditText) findViewById(R.id.gross_tips_edit_text);
			grossTips = Double.valueOf( grossTipsView.getText().toString() );     //get the value of the gross tips field
			
			//get net tips amount
			TextView netTipsView = (TextView) findViewById(R.id.net_tips_display);
			netTips = Double.valueOf(netTipsView.getText().toString());
			
			TipEntryv2 newEntry = new TipEntryv2(year, month, day, grossTips, netTips, 
					hours, tipRecord.getPayRate(), createNewTipRecord()); //create the tip entry
			
			if(note != null && note.length() > 0){
				newEntry.setNote(note);
			}
			
			
			tipRecord.addRecordSorted( newEntry );
			IOUtil.save(tipRecord);
			IOUtil.saveBackup(tipRecord);
		}
		catch (NumberFormatException e){
			Toast.makeText(getApplicationContext(), "Invalid info entered", Toast.LENGTH_SHORT).show();
			return;
		}
		catch (IllegalArgumentException e2){
			Toast.makeText(getApplicationContext(), "Invalid date entered", Toast.LENGTH_SHORT).show();
			return;
		}
		
		Toast.makeText(getApplicationContext(), "Tip info saved", Toast.LENGTH_SHORT).show();
		finish();		//if no exceptions, end the current activity
	}
	
	
	
	private void setNetTips(){
		TextView netText;
		
		try {
		
			double grossTips = 0;			
			double netTips = 0;
			double tipOuts = 0;
			
			valueText = (EditText) findViewById(R.id.gross_tips_edit_text);  		//get the view for the gross tips
			if ((valueText.getText().toString() != "") && (valueText.getText().toString() != null) ){
				grossTips = Double.parseDouble(valueText.getText().toString());	  		//get the gross tip value
				
				if(dynamicButtons > 0){
					for(int i = 1; i <= dynamicButtons; i++){							//for every tip out entered
						valueText = (EditText) findViewById(TIPEE_AMT_ID_OFFSET + i);	//get the view for the edittext
						tipOuts += Double.parseDouble(valueText.getText().toString() );	//add the total to netTips
					}
				}//we now have total of tip outs
				
				netTips = grossTips - tipOuts;		//find the net amount
				netTips = Math.round(netTips * 100);//
				netTips = netTips/100;				//these enforce two decimal places
				netText = (TextView) findViewById(R.id.net_tips_display);	//get the view of the edittext to display it
				netText.setText(String.valueOf(netTips));//display it
			}
		}//try
		catch( NumberFormatException e){
			netText = (TextView) findViewById(R.id.net_tips_display);	//get the view of the edittext to display it
			netText.setText("0.0");
		}
	}
	
		
	private ArrayList<TipoutRecordv2> createNewTipRecord(){
		ArrayList<TipoutRecordv2> tipouts;
		
		//get formatted ArrayList<TipRecord>
		tipouts = new ArrayList<TipoutRecordv2>();
		for(int i = 1; i <= dynamicButtons; i++){
			Spinner name = (Spinner) findViewById(TIPEE_NAME_ID_OFFSET + i);		//get the spinner
			EditText amt = (EditText) findViewById(TIPEE_AMT_ID_OFFSET + i);		//and edittext
								
			tipouts.add( new TipoutRecordv2( (String)name.getSelectedItem() , Double.valueOf(amt.getText().toString()) ));
		}
		
		return tipouts;
	}
		
	//
	// Code to generate and display the the date picker dialog
	public class GetDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState){			
			final Calendar calendar = Calendar.getInstance();//get calendar object
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			
			DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
			
			boolean disableCalendar = tipRecord.isCalendarDisabled();
			dialog.getDatePicker().setCalendarViewShown(!disableCalendar);
			dialog.getDatePicker().setSpinnersShown(disableCalendar);
			return dialog;
		}
		
		public void onDateSet(DatePicker view, int year, int month, int day){
			setDateText(year, month+1, day);
		}
	}
		
	//checks for errors in tip entry, calculates net tips
	public class TipWatcher implements TextWatcher{
			
		//verify the input was valid
		public void afterTextChanged(Editable s){
				
			try{
				Double.parseDouble(s.toString());				}
			catch(NumberFormatException e){
				//do nothing if invalid entry, change this and other occurrences of this to isNaN() TODO
			}
						
			setNetTips();//calculate and display the net tips
		}
			
		//required for TextWatcher
		public void beforeTextChanged(CharSequence s, int start, int count, int after){
			//do nothing
		}
			
		//required for TextWatcher
		public void onTextChanged(CharSequence s, int start, int before, int count  ){
			//do nothing
		}
	}
	
	
}
