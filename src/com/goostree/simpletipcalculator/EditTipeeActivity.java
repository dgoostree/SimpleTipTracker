//
//  As with the other activities, the goal of this is functionality
//   and learning more about android.  To this end, the "dynamic" nature 
//   of this is achieved by statically defining the various textview's
//   locations and modifying their visibility as needed.  The plus button's
//   location is programatically placed, however.
//
//  This approach imposes a cap of 20 support staff members at any given time. 
package com.goostree.simpletipcalculator;

import java.util.ArrayList;

import com.goostree.simpletipcalculator.base.TipRecordv2;
import com.goostree.simpletipcalculator.util.IOUtil;

import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EditTipeeActivity extends ActionBarActivity {
	private TipRecordv2 tipRecord;
	private int existingTipeeCount;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_tipee);
		
		
		tipRecord = (TipRecordv2)getApplicationContext();
		existingTipeeCount = tipRecord.getTipeeList().size();	//get the number of current tipees
		
		//wage stuff
		EditText hourlyWageText = (EditText)findViewById(R.id.edit_hourly_wage_content);
		hourlyWageText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);//only one decimal possible
		hourlyWageText.setText( tipRecord.getPayRate()+""  );
		
		//populate and set the spinner for starting day		
		ArrayList<String> days = new ArrayList<String>(); //create possible days
		days.add("Sunday");
		days.add("Monday");
		days.add("Tuesday");
		days.add("Wednesday");
		days.add("Thursday");
		days.add("Friday");
		days.add("Saturday");
		
		ArrayAdapter<String> dayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, days);
		Spinner daySpin = (Spinner)findViewById(R.id.day_spinner);
		daySpin.setAdapter(dayAdapter);
		daySpin.setSelection(tipRecord.getWeekStart() - 1);
		
		
		setInitialLayout();
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.edit_tipee, menu);
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	//
	// Creates a dialog to capture new tipee name
	//  Calls add function if a correct value was entered
	public void getNewTipeeName(View view) {		
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setMessage("Enter New Tipee Name");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		input.setLines(1);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
			
			String value = input.getText().toString();
			
			if(value.length() == 0){
				return;
			}
			
			int newPosition = ++existingTipeeCount;
			String nameField = "name" + newPosition;	//create name of the desired name textview
			String minusField = "minus" + newPosition;	//similar for id
			
			int theId = getResources().getIdentifier(nameField, "id", getPackageName());	//get the name textviews id
			TextView newText = (TextView) findViewById(theId);								//get the view
			newText.setText(value);															//set its text
			newText.setVisibility(View.VISIBLE);											//it's now visible
			
			theId = getResources().getIdentifier(minusField, "id", getPackageName());		//same for the minusButton
			newText = (TextView) findViewById(theId);										//yeah, beeyotch
			newText.setVisibility(View.VISIBLE);
			
			setPlusButtonPlacement();
			//focus scrollview to bottom when new button added			
			final ScrollView scroll = (ScrollView) findViewById(R.id.scrollview);
			//scroll.fullScroll(View.FOCUS_DOWN);
			scroll.post(new Runnable(){

				@Override
				public void run() {
					scroll.scrollTo(0, scroll.getBottom());
					
				}
				
			});
			
		  }
		});

		alert.setNegativeButton("Cancel", null);		//not worried about doing anything on cancel

		alert.show();
	}
	
	//removes the selected tipee
	public void removeTipee(View view){
		int totalTipees = existingTipeeCount;
		TextView first;
		TextView second;
		int firstId;
		int secondId;
		
		int intId = view.getId(); 								//get the id of the minus button that was pressed
		String strId = getResources().getResourceName(intId);	//get the name of the resource eg: com.goostree.blahblah.id/idName
		int buttonNumber = strId.lastIndexOf("/");				//get the location of the '/' before the text id
		strId = strId.substring(buttonNumber+6);				//we want to start with the first character after "/minus"
		buttonNumber = Integer.valueOf(strId);					//and get the integer representation
		
		if(buttonNumber != totalTipees){		//if it isn't the last element in the list			
							
			//shift everything up one slot
			for(int i = buttonNumber; i < totalTipees; i++){//from current to last-1
				firstId = getResources().getIdentifier("name"+i, "id", getPackageName());	//get first's id
				first = (TextView) findViewById(firstId);									//get first's view				
				
				secondId = getResources().getIdentifier("name" + (i+1), "id", getPackageName());//get the second textviews id
				second = (TextView) findViewById(secondId);		//get the textview
				
				first.setText( second.getText()); 				//second's text becomes first's				
				first = second;									//second becomes first and then the process repeats				
			}
			
		}
		
		//now set the last items' visibility to gone
		first = (TextView) findViewById( getResources().getIdentifier("name" + totalTipees, "id", getPackageName()) );
		second = (TextView) findViewById( getResources().getIdentifier("minus" + totalTipees, "id", getPackageName()) );
		first.setVisibility(View.INVISIBLE);
		second.setVisibility(View.INVISIBLE);
		
		--existingTipeeCount;				//decrement added tipee count
		setPlusButtonPlacement();
		
	}
	
	public void saveTipeeList(View view){
		ArrayList<String> newTipeeList = new ArrayList<String>(); //to hold the 
		double payRate = 0.0;
		
		//get the pay rate info
		EditText payRateView = (EditText)findViewById(R.id.edit_hourly_wage_content);
		
		if(payRateView.getText().length() == 0){
			Toast.makeText(getApplicationContext(), "Invalid Wage Entered", Toast.LENGTH_SHORT).show();
			return;
		}
		
		try {
			payRate = Double.valueOf( payRateView.getText().toString()   );
		}
		catch(NumberFormatException e){
			Toast.makeText(getApplicationContext(), "Invalid Wage Entered", Toast.LENGTH_SHORT).show();
			return;
		}
		
		// Save starting day of week
		Spinner spin = (Spinner)findViewById(R.id.day_spinner);
		
		//save tipee list
		for(int i = 1; i <= existingTipeeCount; i++ ){//for all active tipees
			TextView name = (TextView) findViewById( getResources().getIdentifier("name"+i, "id", getPackageName()) );//get the view
			newTipeeList.add(name.getText().toString());	//add the name to the arraylist
		}
		
		tipRecord.setJobDetails(newTipeeList, payRate, spin.getSelectedItemPosition() + 1);	//make it the tipRecord's tipee list
		IOUtil.save(tipRecord);
		IOUtil.saveBackup(tipRecord);
		Toast.makeText(getApplicationContext(), "Job Details Updated", Toast.LENGTH_SHORT).show();
		finish();		//if no exceptions, end the current activity
	}
	
	private void setInitialLayout(){
			
		int nameId;
		int minusId;
		String name;
		String minus;
		TextView item;
				
		for(int i = 1; i <= existingTipeeCount; i++){
			
			name = "name" + i;						//name1 for the first id, name2 for the second
			minus = "minus"+ i;						//similar
			nameId = getResources().getIdentifier(name , "id", getPackageName());	//get the id for the name textview
			minusId = getResources().getIdentifier(minus, "id", getPackageName());	//similar for minus 
			
			item = (TextView) findViewById(nameId);			//get its view
			item.setText(tipRecord.getTipeeList().get( i-1));	//set it to its saved value
			item.setVisibility(View.VISIBLE);				//and visible
			
			item = (TextView) findViewById(minusId);
			item.setVisibility(View.VISIBLE);				//minus visible
		}	
		
		setPlusButtonPlacement();
		
	}
	
	//
	//places the plus button accordingly
	private void setPlusButtonPlacement(){
				
		int temp = existingTipeeCount;			//get int for last plus button number
		String str = "minus" + temp;							//convert it to the buttons name
		int id = getResources().getIdentifier(str, "id", getPackageName());//get the id
		
		RelativeLayout theLayout = (RelativeLayout) findViewById(R.id.edit_tipee_layout);	//get the layout
		TextView plusButton = (TextView) findViewById(R.id.plus_textview);	//reference the item in question
		
		if(temp == 20){
			plusButton.setVisibility(View.INVISIBLE);
		}
		else if(temp == 19){
			plusButton.setVisibility(View.VISIBLE);		//this is redundant if we just went from 18 -> 19
		}												//  
		
		
		
		RelativeLayout.LayoutParams rlp =  new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);	//set default parameters
		rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);		//align right
		rlp.addRule(RelativeLayout.ALIGN_BASELINE, id);		//baseline with last existing minus
		theLayout.removeView(plusButton);
		theLayout.addView(plusButton, rlp);
		
	}
}
