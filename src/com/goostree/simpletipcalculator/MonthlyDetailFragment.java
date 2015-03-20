package com.goostree.simpletipcalculator;

import java.util.ArrayList;
import java.util.Calendar;

import com.goostree.simpletipcalculator.base.TipEntryv2;
import com.goostree.simpletipcalculator.base.TipRecordv2;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MonthlyDetailFragment extends Fragment{
	private final String[] monthsOfYear = {"January", "February", "March",
			"April", "May", "June", "July", "August", "September", "October", "November",
			"December"};	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){ //This is big. Like big, big. 
		View rootView = inflater.inflate(R.layout.fragment_view_monthly, container, false);
		ViewTipActivity viewActivity = (ViewTipActivity)getActivity();
		TipRecordv2 tipRecord = (TipRecordv2)viewActivity.getApplicationContext();
		boolean valuesAdded = false;
		LinearLayout mainLayout = (LinearLayout)rootView.findViewById(R.id.monthly_layout);
		
		double highest = 0;
		TextView highestDate = new TextView(getActivity());
		
		Calendar firstMonth = viewActivity.dateList.get(0).getDate();//first date in the list
		Calendar lastMonth = viewActivity.dateList.get(viewActivity.dateList.size() -1).getDate();//last date in the list
		
		firstMonth = (Calendar)firstMonth.clone();
		lastMonth = (Calendar)lastMonth.clone();//clone them to leave originals intact (laziness consumes me)
		
		firstMonth.set(Calendar.DATE, 1);
		lastMonth.set(Calendar.DATE, 1);//set each to the first day of the month
		
		Calendar currentMonth = (Calendar)firstMonth.clone();
		
		while(currentMonth.compareTo(lastMonth) <= 0){  //do while we're within the months requested
			Calendar endOfMonth = (Calendar)currentMonth.clone();
			endOfMonth.set(Calendar.DATE, endOfMonth.getActualMaximum(Calendar.DATE)); //set the end of the month
			
			int from = tipRecord.getRangeFromIndex(currentMonth.get(Calendar.YEAR), //get indexes into the tiprecord
					currentMonth.get(Calendar.MONTH), 								//for begin and end entries in the range
					currentMonth.get(Calendar.DATE));
			int to = tipRecord.getRangeToIndex(endOfMonth.get(Calendar.YEAR), 
					endOfMonth.get(Calendar.MONTH), 
					endOfMonth.get(Calendar.DATE));
			
			ArrayList<TipEntryv2> currentList = tipRecord.getTipInfoInRange(from, to);
			
			if( currentList != null){
				TextView date = new TextView(getActivity());
				date.setTypeface(null, Typeface.BOLD);
				date.setText(monthsOfYear[currentMonth.get(Calendar.MONTH)] + " " + currentMonth.get(Calendar.YEAR));
				
				if( valuesAdded) {
					date.setPadding(0, 30, 0, 0);
				}
				
				double wageTotal = tipRecord.getWagesTotal(currentList);
				double tipsTotal = tipRecord.getNetTipsTotal(currentList);
				double hoursTotal = tipRecord.getHoursTotal(currentList);
				
				if( (wageTotal + tipsTotal) > highest ){
					highest = wageTotal + tipsTotal;
					highestDate = date;
				}
				
				TextView hours = new TextView(getActivity());
				hours.setText("Hrs: " + hoursTotal);
				
				TextView wages = new TextView(getActivity());
				wages.setText("Wages: " + 
						wageTotal);
				
				TextView tips = new TextView(getActivity());
				tips.setText("Tips: " + tipsTotal);
				tips.setGravity(Gravity.CENTER);
				
				TextView dph = new TextView(getActivity());
				dph.setText("$/hr: " + 
						tipRecord.getDPHTotal(currentList));
				dph.setGravity(Gravity.RIGHT);
				
				LinearLayout innerLayout = new LinearLayout(getActivity());
				innerLayout.setOrientation(LinearLayout.HORIZONTAL);
				
				
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				
				LinearLayout.LayoutParams innerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
				
				innerLayout.addView(hours, innerParams);
				innerLayout.addView(wages, innerParams);
				innerLayout.addView(tips, innerParams);
				innerLayout.addView(dph, innerParams);
				
				mainLayout.addView(date);
				mainLayout.addView(innerLayout, params);
				
				valuesAdded = true;
			}
			
			currentMonth.add(Calendar.MONTH, 1);
			
		}

		char c = '\u2713';
		highestDate.setText( highestDate.getText().toString() + " " + Character.toString(c));
		
		return rootView;
	}
	
}
