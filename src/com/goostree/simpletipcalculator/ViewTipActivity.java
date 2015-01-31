//
//  TODO: decouple the excel from the activity, create a utility class to do it. 
//
package com.goostree.simpletipcalculator;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import android.app.ActionBar.TabListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.goostree.simpletipcalculator.base.TipEntryv2;
import com.goostree.simpletipcalculator.base.TipRecordv2;
import com.goostree.simpletipcalculator.util.SpreadsheetUtil;

public class ViewTipActivity extends FragmentActivity implements ActionBar.TabListener, TabListener {
	private Calendar fromDate;
	protected Calendar toDate;
	public int fromDay, fromMonth, fromYear;
	public int toDay, toMonth, toYear;
	protected Calendar earliestWeekBegin;
	public TipRecordv2 tipRecord;
	public ArrayList<TipEntryv2> dateList;
	private ViewPager viewPager;
	private TabsPagerAdapter mAdapter;
	private android.app.ActionBar actionBar;
	private String[] tabs;
	TextView dates;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_view_tip);
		
		//Get instance of the data to be used
		tipRecord = (TipRecordv2)getApplicationContext();		//store the tip record		
		Intent intent = getIntent();
		fromDate = (Calendar) intent.getSerializableExtra(MainActivity.FROM_DATE);//get the dates from the intent
		toDate = (Calendar) intent.getSerializableExtra(MainActivity.TO_DATE);		
		fromDay = fromDate.get(Calendar.DAY_OF_MONTH);//store instance of both date details in int form
		fromMonth = fromDate.get(Calendar.MONTH);
		fromYear = fromDate.get(Calendar.YEAR);
		toDay = toDate.get(Calendar.DAY_OF_MONTH);
		toMonth = toDate.get(Calendar.MONTH);
		toYear = toDate.get(Calendar.YEAR);
		dates = (TextView)findViewById(R.id.view_date_range_content); //store content of the dates 
		
		// Sept 29 2014 multi tabs
		//this determines how many tabbed views to display
		Calendar twoMonths =(Calendar)fromDate.clone();//get first date selected
		twoMonths.add(Calendar.DATE, 57);  // pick date at least two months out from beginning
		
		Calendar oneMonth = (Calendar)fromDate.clone(); //get first data again
		oneMonth.add(Calendar.DATE, 13);  				//set test date to end of current month
		
		int numTabs = 0;
		
		if( toDate.compareTo(twoMonths) >= 0)  //if at least two months selected, show tabs for weekly and monthyly breakdown
		{
			tabs = new String[]{"Tipout Detail", "Daily", "Weekly", "Monthly"};
			numTabs =4;
		}
		else if( toDate.compareTo(oneMonth) >= 0) { //else if one month show weekly
			tabs = new String[]{"Tipout Detail", "Daily", "Weekly"};
			numTabs = 3;
		}
		else{
			tabs = new String[]{"Tipout Detail", "Daily OverView"};
			numTabs = 2;
		}
		
		earliestWeekBegin = (Calendar)fromDate.clone();
		
		//Adjust earlistWeekBegin to mark the beginning of a week that ends in desired range
		int weekStart = tipRecord.getWeekStart();
		int currentDOW = earliestWeekBegin.get(Calendar.DAY_OF_WEEK);
		
		if( currentDOW > weekStart){
			earliestWeekBegin.add(Calendar.DATE, (weekStart - currentDOW));
		}
		else if (currentDOW < weekStart){
			earliestWeekBegin.add(Calendar.DATE,  ((currentDOW + 7 - weekStart)*(-1)));
		}//we have desired beginning of earliest week ending in desired month
			
		dateList = tipRecord.getTipInfoInRange(tipRecord.getRangeFromIndex(fromYear, fromMonth, fromDay),
				tipRecord.getRangeToIndex(toYear, toMonth, toDay)); //get instance of the desired tip record ranges.
		
		if (dateList == null){
			Toast.makeText(getApplicationContext(), "No shifts available in specified range ", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		//set up the tabs, as per the tutorial at http://www.androidhive.info/2013/10/android-tab-layout-with-swipeable-views-1/
		viewPager = (ViewPager) findViewById(R.id.view_tips_pager);
		actionBar = getActionBar();
		mAdapter = new TabsPagerAdapter(getSupportFragmentManager(), numTabs);  /////
		
		viewPager.setAdapter(mAdapter);
		//actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		
		//Add the tabs
		for( String tab_name : tabs) {
			actionBar.addTab(actionBar.newTab().setText(tab_name)
				.setTabListener( this));
		}
		
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				actionBar.setSelectedNavigationItem(arg0);
				
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});		
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.view_activity_actions, menu);
	    return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) { //TODO break this up, I don't like how long it is
		String name = getFileNameDates(dateList) + "-Earnings.xls";
		File f = SpreadsheetUtil.createFile(name, Environment.getExternalStorageDirectory().toString());
		
		HSSFWorkbook workbook = SpreadsheetUtil.getNewWorkbook();
        HSSFSheet sheet =  SpreadsheetUtil.getNewSheet(name, workbook);

        SpreadsheetUtil.addRow(workbook, sheet, 0,  
        		new String[]{"Date", "Wages", "Gross Tips", "Tipouts", "Net Tips", "Earnings", "$/hr"});
       
        //vars for totals
        int max = dateList.size();
        double grossTot = 0;
        double netTot = 0;
        double wagesTot = 0;
        double tipoutTot = 0;
        double earnTot = 0;
        double dphTot = 0;
        int n = 0;
        
        //
        //populate for each entry
        int i;
        for( i = 0; i < max; i++){
        	TipEntryv2 today = dateList.get(i);
        	double gross = today.getGrossTips();
        	grossTot += gross;
        	double net = today.getNetTips();
        	netTot += net;
        	double wages = today.getWagesEarned();
        	wagesTot += wages;
        	tipoutTot += (gross - net);
        	earnTot += (wages+net);
        	double dollarPerHour = today.getDollarsPerHour();
        	dphTot += dollarPerHour;
        	
        	SpreadsheetUtil.addRow(workbook, sheet, i + 1, 
        			new String[]{today.getDateToString(), wages + "", gross + "", gross - net + "", 
        			net + "", wages + net + "", dollarPerHour + ""});
            n++;
        }
        
        
        
        //add the totals to the sheet
        SpreadsheetUtil.addRow(workbook, sheet, i + 1, 
        		new String[]{"Total", wagesTot + "", grossTot + "", + tipoutTot + "", netTot + "",
        		earnTot + "", Math.round( 100 * (dphTot/n))/100.0 + ""});
        
        i += 3;//currentRow is now i
        SpreadsheetUtil.addRow(workbook, sheet, i, new String[]{"Tipee", "Amount"});
        //get the total tipouts to each tipee
        ArrayList<String> tipees = new ArrayList<String>();
        ArrayList<Double> amts = new ArrayList<Double>();
        tipRecord.getTipoutTotals(dateList, tipees, amts);
                
        max = tipees.size();
        int j;
        for(j = 0; j < max; j++){
        	i++;
        	SpreadsheetUtil.addRow(workbook, sheet, i, new String[]{
        			tipees.get(j), amts.get(j) + ""});
        }
            
        SpreadsheetUtil.writeFile(f, workbook);
       
        //TODO: move this?
		Intent exportIntent = new Intent(android.content.Intent.ACTION_SEND);
		exportIntent.setType("application/excel");
		exportIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,  name ); 
		exportIntent.putExtra(android.content.Intent.EXTRA_TEXT,  "Thanks for using Simple Tip Tracker!");
		exportIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///"+ f));
		startActivity(Intent.createChooser(exportIntent, "Export Spreadsheet Using..."));
		
		return super.onOptionsItemSelected(item);
	}
	

	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
		
	}

	@Override
	public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
		viewPager.setCurrentItem(arg0.getPosition());		
	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onTabSelected(android.app.ActionBar.Tab tab,
			android.app.FragmentTransaction ft) {
		
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(android.app.ActionBar.Tab tab,
			android.app.FragmentTransaction ft) {
		
		
	}

	@Override
	public void onTabReselected(android.app.ActionBar.Tab tab,
			android.app.FragmentTransaction ft) {
		
		
	}
	
	private String getFileNameDates(ArrayList<TipEntryv2> list){
		TipEntryv2 first = list.get(0);
		TipEntryv2 last = list.get(list.size() -1);
		String title = first.getDateToString() + "-" + last.getDateToString();
		
		return title;
	}

}
