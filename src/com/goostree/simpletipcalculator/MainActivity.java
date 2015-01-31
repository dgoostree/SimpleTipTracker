package com.goostree.simpletipcalculator;


import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.goostree.simpletipcalculator.base.TipEntryv2;
import com.goostree.simpletipcalculator.base.TipRecordv2;
import com.goostree.simpletipcalculator.directorybrowser.SimpleDirectoryBrowser;
import com.goostree.simpletipcalculator.util.IOUtil;

//
//SimpleTipTracker
//Programmer: Darren Goostree
//Purpose:  Keeps a record of wages earned, gross tips received, and tipouts to each tipee (busser, bartender, etc) 
//	for the purpose of reviewing this information in a user-defined periods of time.
//
public class MainActivity extends ActionBarActivity {
	public static final String FROM_DATE = "com.goostree.simpletipcalculator.FROM_DATE";
	public static final String TO_DATE = "com.goostree.simpletipcalculator.TO_DATE";
	public TipRecordv2 tipRecord;
	private EditText dateView;  //TODO: remove the need for this 
	private Activity mainActivity;
		
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        mainActivity = this;
        tipRecord = (TipRecordv2)getApplicationContext();
        IOUtil.readRecordFromFile(tipRecord);
        IOUtil.createBackup(tipRecord); 
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.menu_delete_tip_record){
        	deleteTipRecord();
        	return true;
        }
        else if( id == R.id.load_backup){
        	loadBackup();
        	return true;
        }
        else if(id == R.id.toggle_calendar_shown){
        	tipRecord.toggleShowCalendar();
        	return true;
        }
        else if(id == R.id.load_backup_from_file){
        	new SimpleDirectoryBrowser(mainActivity, tipRecord);
        	return true;
        }
        else if(id == R.id.remote_backup_record){
        	IOUtil.saveBackup(tipRecord);
        	File destFile = new File(Environment.getExternalStorageDirectory() + "/SimpleTipTracker/", IOUtil.REMOTE_BACKUP_FILENAME);
        	try {
				IOUtil.copyFile(new File(tipRecord.getFilesDir(),
						IOUtil.BACKUP_FILENAME), 
						destFile, 
						mainActivity);
				
				//TODO move this?
				Intent exportIntent = new Intent(android.content.Intent.ACTION_SEND);
				exportIntent.setType("application/java-serialized-object");
				exportIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,  "Your backup earnings from SimpleTipTracker" ); 
				exportIntent.putExtra(android.content.Intent.EXTRA_TEXT,  "Thanks for using Simple Tip Tracker!\n\n"
						+ "A copy of this file has been saved in the SimpleTipTracker folder in the root directory of your "
						+ "phone's internal storage. To restore it, select 'Load Backup From File' from the main screen menu. "
						+ "If you need to restore it from your email or cloud storage, just copy it to anywhere in your phone, and then navigate to the location you "
						+ "copied it to. With the correct file selected, hit 'Ok' and you're good to go.");
				exportIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///"+ destFile));
				startActivity(Intent.createChooser(exportIntent, "Choose Backup Method"));
				
			} catch (Exception e) {
				e.printStackTrace();
			}
        	
        	return true;
        }
        else {
        	return super.onOptionsItemSelected(item);
        }
    }

    // change activities to add tip activity
    public void addTip(View view){
    	Intent intent = new Intent(this, AddActivity.class);    //must add this
    	startActivity(intent);
    }
    
    //change activity to edit tip activity after gathering input from alert
    public void editTip(View view){
    	if( tipRecord.getTipLog().size() == 0 ){
    		Toast.makeText(getApplicationContext(), "No tip info to edit", Toast.LENGTH_SHORT).show();
			return;
    	}    	
    	    	
    	//new dialog	
    	AlertDialog.Builder getDateAlert = new AlertDialog.Builder(this);
    		
    	//build the layout for the dialog, get reference to the edit text
    	EditText theText = buildEditDialog(getDateAlert);
    	
    	getDateAlert.setPositiveButton("Ok",new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(dateView == null){
					Toast.makeText(getApplicationContext(), "No date set", Toast.LENGTH_SHORT).show();
					return;
				}
				String dateStr = dateView.getText().toString();
				
				if(dateStr.length() == 0){
					Toast.makeText(getApplicationContext(), "No date set", Toast.LENGTH_SHORT).show();
					return;
				}
				
				Intent intent = new Intent(mainActivity, EditTipActivity.class);
				intent.putExtra(FROM_DATE, dateStr );  //attach the date to the intent
		    	startActivity(intent);   									 //start the activity
			}
       	});//new OnClick
    	
    	getDateAlert.setNegativeButton("Cancel", null);
    	
    	getDateAlert.show();
        	
    	//open date picker when button first pushed
    	dateView = theText;
    	DialogFragment newFragment = new GetMainDateFragment();
		newFragment.show(getSupportFragmentManager(), "DatePicker");
    }
        
    //change activity to edit tipee
    //  nothing special to do here yet.  
    public void editTipee(View view){    	
    	Intent intent = new Intent(this, EditTipeeActivity.class);
    	startActivity(intent);
    }
    
    //
    // TODO: This is massive, break it up
    public void viewTip(View view){
    	if( tipRecord.getTipLog().size() == 0 ){
    		Toast.makeText(getApplicationContext(), "No tip info to view", Toast.LENGTH_SHORT).show();
			return;
    	}    	    	
    	
    	//new popup dialog
    	AlertDialog.Builder getDatesDiag = new AlertDialog.Builder(this);
    	
    	RelativeLayout rl = new RelativeLayout(this);// get the relativelayout

    	//label
    	TextView newView = new TextView(this);
    	newView.setId(8999);
    	newView.setGravity(Gravity.CENTER);    	
    	newView.setTextSize(20);
    	newView.setPadding(0, 0, 0, 30);
    	newView.setText("Select date range");
    	
    	//label stuff
    	RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT, 
    			RelativeLayout.LayoutParams.WRAP_CONTENT);//default layout params for from date  
    	lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
    	rl.addView(newView,lp);//add the label
    	
    	//the "from" edit text
    	final EditText fromText = new EditText(this);
    	fromText.setId(9001); 	//over nine thousand
    	fromText.setHint("Enter beginning date");
    	fromText.setGravity(Gravity.CENTER);
    	fromText.setKeyListener(null);
    	fromText.setFocusable(false);
    	fromText.setOnClickListener(new DateListener());
    	
    	RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT, 
    			RelativeLayout.LayoutParams.WRAP_CONTENT);//default layout params for from date  
    	rlp.addRule(RelativeLayout.BELOW, 8999);
    	rl.addView(fromText, rlp); //add the fromText
    	
    	//the "to" edit text
    	EditText toText = new EditText(this);
    	toText.setId(9002); 	//over nine thousand and one
    	toText.setHint("Enter ending date");
    	toText.setGravity(Gravity.CENTER);
    	toText.setKeyListener(null);
    	toText.setFocusable(false);
    	toText.setOnClickListener(new DateListener() );
    		
    	
    	RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT, 
    			RelativeLayout.LayoutParams.WRAP_CONTENT);//default layout params for to date
    	rllp.addRule(RelativeLayout.BELOW, 9001);
    	rllp.setMargins(0, 30, 0, 17);
    	rl.addView(toText, rllp);
    	
    	
    	//							//
    	// New: September 4, 2014	//
    	// This week button
    	float dp = getBaseContext().getResources().getDisplayMetrics().density;
    	Button thisWeek = new Button(this);
    	thisWeek.setText("This Week");
    	thisWeek.setTextSize(16);
    	thisWeek.setMinimumWidth(0);
    	thisWeek.setWidth((int)(dp * 170));
    	thisWeek.setGravity(Gravity.CENTER);
    	thisWeek.setBackgroundColor(Color.TRANSPARENT);
    	thisWeek.setId(9003);
    	
    	LinearLayout linLay = new LinearLayout(this);
    	linLay.setId(13371337);
    	LinearLayout.LayoutParams linparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 
    			LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
    	
    	//onclick
    	thisWeek.setOnClickListener(new OnClickListener(){  //get the current week taking into account custom week start

			@Override
			public void onClick(View v) {
				int begin;
				int delta;
				
				Calendar from = Calendar.getInstance();
				begin = tipRecord.getWeekStart();   //get the begin of the week
				
				if(begin < 1){			//if begin set to zero (new field in first run of app since update)
					begin = 1;			//use 1 instead
					tipRecord.setWeekStart(begin);//set the records weekstart to 1
					IOUtil.save(tipRecord);		//save it for future runs
					IOUtil.saveBackup(tipRecord);
				}
				
				
				delta = from.get(Calendar.DAY_OF_WEEK);//the from day of the week
				
				//calculate how many days to add (negative)
				if( begin > delta)
					delta = (delta + 1) * (-1);
				else
					delta = begin - delta;
				
				from.add(Calendar.DATE, delta); //add delta
				
				Calendar to = (Calendar)from.clone();  //clone from, which gives us first day of the work week
				to.add(Calendar.DATE, 6); 			   //add six, and voila
				
				startViewActivity(from, to);				
			}    		
    	});
    	
    	RelativeLayout.LayoutParams anotherRL = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.WRAP_CONTENT,
    			RelativeLayout.LayoutParams.WRAP_CONTENT); //layout params for this week button
    	anotherRL.addRule(RelativeLayout.BELOW, 9002);
    	
    	linLay.addView(thisWeek, linparams);
    	
    	
    	//two weeks
    	Button twoWeeks = new Button(this);
    	twoWeeks.setText("Two Weeks");
    	twoWeeks.setTextSize(16);
    	twoWeeks.setMinimumWidth(0);
    	twoWeeks.setWidth((int)(dp * 170));
    	twoWeeks.setGravity(Gravity.CENTER);
    	twoWeeks.setBackgroundColor(Color.TRANSPARENT);
    	twoWeeks.setId(9009);
    	
    	//onclick
    	twoWeeks.setOnClickListener(new OnClickListener(){  //get the current week taking into account custom week start

			@Override
			public void onClick(View v) {
				int begin;
				int delta;
				
				Calendar from = Calendar.getInstance();
				begin = tipRecord.getWeekStart();   //get the begin of the week
				
				if(begin < 1){			//if begin set to zero (new field in first run of app since update)
					begin = 1;			//use 1 instead
					tipRecord.setWeekStart(1);//set the records weekstart to 1
					IOUtil.save(tipRecord);		//save it for future runs
					IOUtil.saveBackup(tipRecord);
				}
				
				
				delta = from.get(Calendar.DAY_OF_WEEK);//the current day of the week
				
				//calculate how many days to add (negative or zero
				if( begin > delta)
					delta = (delta + 1) * (-1);
				else
					delta = begin - delta;
				
				from.add(Calendar.DATE, delta); //add delta
				
				Calendar to = (Calendar)from.clone();  //clone from, which gives us first day of the work week
				to.add(Calendar.DATE, 6); 			   //add six, and voila
				
				from.add(Calendar.DATE, -7);  //add -7 for the second week
				
				startViewActivity(from, to);				
			}    		
    	});
    	
    	linLay.addView(twoWeeks, linparams);
    	rl.addView(linLay, anotherRL);
    	
    	// this month
    	Button thisMonth = new Button(this);
    	thisMonth.setText("This Month");
    	thisMonth.setTextSize(16);
    	thisMonth.setGravity(Gravity.CENTER);
    	thisMonth.setWidth((int)(dp * 170));
    	thisMonth.setBackgroundColor(Color.TRANSPARENT);
    	thisMonth.setId(9004);
    	
    	linLay = new LinearLayout(this);
    	linLay.setId(73317331);
    	
    	anotherRL = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.WRAP_CONTENT,
			RelativeLayout.LayoutParams.WRAP_CONTENT); //layout params for this week button
	    anotherRL.addRule(RelativeLayout.BELOW, 13371337);
    	
    	thisMonth.setOnClickListener(new OnClickListener(){ //onclick listener for the button

			@Override
			public void onClick(View v) {
				Calendar from = Calendar.getInstance();
				from.set(Calendar.DAY_OF_MONTH, 1); // get first day of month
				
				Calendar to = Calendar.getInstance();
				to.set(Calendar.DATE, to.getActualMaximum(Calendar.DATE));
				
				startViewActivity(from, to);
			}    		
    	});
    	
    	linLay.addView(thisMonth, linparams);
    	
    	// this Year
    	Button thisYear = new Button(this);
    	thisYear.setText("This Year");
    	thisYear.setTextSize(16);
    	thisYear.setGravity(Gravity.CENTER);
    	thisYear.setWidth((int)(dp*170));
    	thisYear.setBackgroundColor(Color.TRANSPARENT);
    	thisYear.setPadding(0, 0, 0, 20);
    	thisYear.setId(9005);
    	
    	thisYear.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Calendar from =Calendar.getInstance();
				from.set(Calendar.MONTH, 0);
				from.set(Calendar.DATE, 1);
				
				Calendar to = Calendar.getInstance();
				to.set(Calendar.MONTH, 11);
				to.set(Calendar.DATE, 31);
				
				startViewActivity(from, to);
			}    		
    	});
    	
    	linLay.addView(thisYear, linparams);
    	rl.addView(linLay, anotherRL);
    	
    	// All time
    	Button allTime = new Button(this);
    	allTime.setText("All Time");
    	allTime.setTextSize(16);
    	allTime.setGravity(Gravity.CENTER);
    	allTime.setWidth((int)(dp*170));
    	allTime.setBackgroundColor(Color.TRANSPARENT);
    	allTime.setPadding(0,  0, 0, 20);
    	allTime.setId(9006);
    	
    	allTime.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				ArrayList<TipEntryv2> theLog = tipRecord.getTipLog();
				
				Calendar from = theLog.get(0).getDate();
				Calendar to = theLog.get(theLog.size() - 1).getDate();
				
				startViewActivity(from, to);
			}    		
    	});
    	
    	anotherRL = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.WRAP_CONTENT,
    			RelativeLayout.LayoutParams.WRAP_CONTENT); //layout params for all time button
    	anotherRL.addRule(RelativeLayout.BELOW, 73317331);
    	anotherRL.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
    	rl.addView(allTime, anotherRL);
    	//
    	
    	getDatesDiag.setView(rl);		
    	getDatesDiag.setPositiveButton("Ok", new ViewRangeOnClickListener(fromText, toText){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String from = fromField.getText().toString();
				String to = toField.getText().toString();
				
				//check if either is empty
				if(from.length() == 0 || to.length() == 0){
					Toast.makeText(getApplicationContext(), "Invalid Date Range Entered", Toast.LENGTH_SHORT).show();
					return;
				}
				
				//get instance of from date
				String[] parts = from.split("\\.");
				Calendar fromCalendar = Calendar.getInstance();
				fromCalendar.clear();
				fromCalendar.set(Integer.valueOf(parts[2]), Integer.valueOf(parts[0])-1, Integer.valueOf(parts[1]) );
				
				//get to date
				parts = to.split("\\.");
				Calendar toCalendar = Calendar.getInstance();
				toCalendar.clear();
				toCalendar.set(Integer.valueOf(parts[2]), Integer.valueOf(parts[0])-1, Integer.valueOf(parts[1]) );
						
				//if from > to, invalid
				if(fromCalendar.after(toCalendar)){
					Toast.makeText(getApplicationContext(), "Invalid Date Range Entered", Toast.LENGTH_SHORT).show();
					return;
				}
				
				Intent intent = new Intent(mainActivity, ViewTipActivity.class);
				intent.putExtra(FROM_DATE, fromCalendar);
				intent.putExtra(TO_DATE, toCalendar);
		    	startActivity(intent);
			}
		});//end onclick
		
    	getDatesDiag.setNegativeButton("Cancel", null);    	
    	getDatesDiag.show(); 								//display it
		
    	return;
    }
    
    
    
    //
    // Deletes the entire tip record at user's request. Stores a backup just in case
    private void deleteTipRecord() {
    	//new popup dialog	
    	AlertDialog.Builder confirmDelete = new AlertDialog.Builder(this);

    	TextView newView = new TextView(this);
    	newView.setGravity(Gravity.CENTER);    	
    	newView.setTextSize(20);
    	newView.setPadding(0, 30, 0, 30);
    	newView.setText("Delete entire tip record?");
    	    	
    	confirmDelete.setView(newView);
    	confirmDelete.setPositiveButton("Ok", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				IOUtil.deleteData(tipRecord);
				Toast.makeText(getApplicationContext(), "Record deleted, backup saved", Toast.LENGTH_SHORT).show();
			}
    	});
    	
    	confirmDelete.setNegativeButton("Cancel", null);  //if user cancels, close and do nothing
 
    	confirmDelete.show();
    	
    }
    
    //
    //Defines the listener for the confirmation dialog
    //
    public void loadBackup() {
    	AlertDialog.Builder confirmLoad = new AlertDialog.Builder(this);

    	TextView newView = new TextView(this);
    	newView.setGravity(Gravity.CENTER);    	
    	newView.setTextSize(20);
    	newView.setPadding(0, 30, 0, 30);
    	newView.setText("Load backed up record?");
    	
    	confirmLoad.setView(newView);
    	confirmLoad.setPositiveButton("Ok", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(IOUtil.loadBackup(tipRecord) == true) {
					Toast.makeText(getApplicationContext(), "Backup loaded", Toast.LENGTH_SHORT).show();
				}
				else {
					Toast.makeText(getApplicationContext(), "No backup file to load", Toast.LENGTH_SHORT).show();
				}
			}
    	});
    	
    	confirmLoad.setNegativeButton("Cancel", null);  //if user cancels, close and do nothing
    	confirmLoad.show();
    }
    
    //
    // Builds the dialog for Edit Tip. Returns reference to the edittext.
    //
    private EditText buildEditDialog(AlertDialog.Builder diag){
    	//TODO: change this to a linear layout
    	RelativeLayout rl = new RelativeLayout(this);// get the relativelayout
    	
    	//label
    	TextView newView = new TextView(this);
    	newView.setId(8999);//more hard coded id's... TODO
    	newView.setGravity(Gravity.CENTER);    	
    	newView.setTextSize(20);
    	newView.setPadding(0, 0, 0, 30);
    	newView.setText("Select date to edit");
    	
    	//label stuff
    	RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT, 
    			RelativeLayout.LayoutParams.WRAP_CONTENT);//default layout params for from date  
    	lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
    	rl.addView(newView,lp);//add the label
    	
    	//the date's edit text
    	final EditText theText = new EditText(this);
    	theText.setHint("mm.dd.yyyy");
    	theText.setGravity(Gravity.CENTER);
    	theText.setKeyListener(null);
    	theText.setFocusable(false);
    	theText.setOnClickListener(new DateListener());
    	
    	
    	//edit text params
    	lp = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT, 
    			RelativeLayout.LayoutParams.WRAP_CONTENT);//default layout params for from date  
    	lp.addRule(RelativeLayout.BELOW, 8999);
    	rl.addView(theText, lp); //add the fromText
    	
    	diag.setView(rl);
    	
    	return theText;
    }
    
    private void startViewActivity(Calendar f, Calendar t){
    	Intent intent = new Intent(mainActivity, ViewTipActivity.class);
		intent.putExtra(FROM_DATE, f);
		intent.putExtra(TO_DATE, t);
    	startActivity(intent);
    }
    
    public class DateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState){			
			final Calendar calendar = Calendar.getInstance();			//get calendar object representing now - this isn't defaulting to today for some reason
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			return new DatePickerDialog(getActivity(), this, year, month, day);  //current activity, then the context
		}
		
		public void onDateSet(DatePicker view, int year, int month, int day){
			dateView.setText((month+1)+"."+day+"."+year );
		}
	}
    
    public class DateListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			dateView = (EditText) v;
			//DialogFragment newFragment = new DateFragment();
			//newFragment.show(getSupportFragmentManager(), "DatePicker");
			DialogFragment newFragment = new GetMainDateFragment();
			newFragment.show(getSupportFragmentManager(), "DatePicker");
		}
	}
                
    
    public abstract class ViewRangeOnClickListener implements DialogInterface.OnClickListener {
    	protected EditText fromField;
    	protected EditText toField;
    	
    	public ViewRangeOnClickListener(EditText f, EditText t){
    		fromField = f;
    		toField = t;
    	}
    }
    
    public class GetMainDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState){			
			final Calendar calendar = Calendar.getInstance();			//get calendar object representing now - this isn't defaulting to today for some reason
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
			dateView.setText((month+1)+"."+day+"."+year);	//change text of calling view		}
		}
    }
        
}
