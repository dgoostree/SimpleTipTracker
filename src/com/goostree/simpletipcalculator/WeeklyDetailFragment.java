package com.goostree.simpletipcalculator;

import java.text.SimpleDateFormat;
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

public class WeeklyDetailFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View rootView = inflater.inflate(R.layout.fragment_view_weekly, container, false);
		ViewTipActivity viewActivity = (ViewTipActivity)getActivity();
		TipRecordv2 tipRecord = (TipRecordv2)viewActivity.getApplicationContext();//the tip record
		Calendar beginRange = (Calendar)viewActivity.earliestWeekBegin.clone();
		Calendar weeksEnd;
		Calendar endOfRange = (Calendar)viewActivity.toDate.clone();
		Calendar weekBegin = (Calendar)beginRange.clone();
		boolean infoAdded = false;
		
		double highest = 0;
		TextView highestDate = new TextView(getActivity());
		
		LinearLayout mainLayout = (LinearLayout)rootView.findViewById(R.id.weekly_layout2);
		
		while(weekBegin.compareTo(endOfRange) <= 0){  //only do if starting day is in the month
			
			weeksEnd = (Calendar)weekBegin.clone();//clone the beginning of the week
			weeksEnd.add(Calendar.DATE,  6);  //add six to get last day of the week
			
			int fromIndex = tipRecord.getRangeFromIndex(weekBegin.get(Calendar.YEAR), //get the beginning offset of entries for range
					weekBegin.get(Calendar.MONTH),
					weekBegin.get(Calendar.DATE));
			int toIndex = tipRecord.getRangeToIndex(weeksEnd.get(Calendar.YEAR), //get ending index into tipRecord
					weeksEnd.get(Calendar.MONTH), 
					weeksEnd.get(Calendar.DATE));
			ArrayList<TipEntryv2> currentWeekEntries = tipRecord.getTipInfoInRange(fromIndex, toIndex);//get snapshot of the weeks entries
			
			if(currentWeekEntries != null){  //do if entries for current week exist
			
				SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd.yy");
				
				TextView date = new TextView(getActivity());
				date.setTypeface(null, Typeface.BOLD);
				date.setText(dateFormat.format(weekBegin.getTime()) + " - " +
						dateFormat.format(weeksEnd.getTime()));
				
				if(infoAdded) {
					date.setPadding(0, 30, 0, 0);
				}
				
				double wageTotal = tipRecord.getWagesTotal(currentWeekEntries);
				double tipsTotal = tipRecord.getNetTipsTotal(currentWeekEntries);
				
				if( (wageTotal + tipsTotal) > highest ){
					highest = wageTotal + tipsTotal;
					highestDate = date;
				}
				
				TextView wages = new TextView(getActivity());
				wages.setText("Wages: " + wageTotal);
				
				TextView tips = new TextView(getActivity());
				tips.setText("Tips: " + tipsTotal);
				tips.setGravity(Gravity.CENTER);
				
				TextView dph = new TextView(getActivity());
				dph.setText("$/hr: " + 
						tipRecord.getDPHTotal(currentWeekEntries));
				dph.setGravity(Gravity.RIGHT);
				
				LinearLayout innerLayout = new LinearLayout(getActivity());
				innerLayout.setOrientation(LinearLayout.HORIZONTAL);
				
				
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				
				LinearLayout.LayoutParams innerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
				
				innerLayout.addView(wages, innerParams);
				innerLayout.addView(tips, innerParams);
				innerLayout.addView(dph, innerParams);
				
				mainLayout.addView(date);
				mainLayout.addView(innerLayout, params);
				
				infoAdded = true;
			}
			
			weekBegin.add(Calendar.DATE, 7);
		}
		
		char c = '\u2713';
		highestDate.setText( highestDate.getText().toString() + " " + Character.toString(c));
		
		return rootView;
	}
}
