package com.goostree.simpletipcalculator;

import java.util.Calendar;

import com.goostree.simpletipcalculator.base.TipEntryv2;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DailyDetailFragment extends Fragment {
	private final String[] daysOfWeek = {"","Sunday", "Monday", "Tuesday", 
		"Wednesday", "Thursday", "Friday", "Saturday"}; //corresponds to Calendar's DAY_OF_WEEK value
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View rootView = inflater.inflate(R.layout.fragment_view_daily, container, false);
		ViewTipActivity viewActivity = (ViewTipActivity)getActivity();
				
		int count = viewActivity.dateList.size();
		double highest = 0;
		TextView highestDate = new TextView(getActivity());
		
		for (int i = 0; i < count; i++) {
			TipEntryv2 currentEntry = viewActivity.dateList.get(i);
			
			LinearLayout mainLayout = (LinearLayout) rootView.findViewById(R.id.daily_layout);
			
			//date textview
			TextView date = new TextView(getActivity());
			date.setTypeface(null, Typeface.BOLD);
			date.setGravity(Gravity.LEFT);
			date.setText( currentEntry.getDateToString());
			
			TextView dayName = new TextView(getActivity());
			dayName.setTypeface(null, Typeface.BOLD);
			dayName.setText(daysOfWeek[currentEntry.getDate().get(Calendar.DAY_OF_WEEK)]);
			
			TextView notePresent = new TextView(getActivity());//
			if( currentEntry.hasNote()){
				notePresent.setTypeface(null, Typeface.BOLD);
				notePresent.setGravity(Gravity.RIGHT);
				char c = '\u270E';
				
				notePresent.setText(Character.toString(c));
				notePresent.setTag(currentEntry.getNote());
				notePresent.setClickable(true);
				notePresent.setFocusable(false);
				notePresent.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						showNoteDialog((String)v.getTag());			
					}
				});
			}
			
			
			if(i > 0) {
				date.setPadding(0, 30, 0, 0);
			}
						
			//hours worked
			TextView hours = new TextView(getActivity());
			hours.setText("Hrs: " + currentEntry.getHoursWorked());
			//hours.setGravity(Gravity.LEFT);
			
			//wages 
			TextView wages = new TextView(getActivity());
			wages.setText("Wages: " + currentEntry.getWagesEarned());
			//wages.setGravity(Gravity.CENTER);
			
			
			//net tips
			TextView tips = new TextView(getActivity());
			tips.setText("Tips: " + currentEntry.getNetTips());
			//tips.setGravity(Gravity.RIGHT);
			
			//dollars per hour
			TextView dph = new TextView(getActivity());
			dph.setText("$/hr: " + currentEntry.getDollarsPerHour());
			//dph.setGravity(Gravity.RIGHT);
			
			LinearLayout topInnerLayout = new LinearLayout(getActivity());
			
			LinearLayout innerLayout = new LinearLayout(getActivity());
			innerLayout.setOrientation(LinearLayout.HORIZONTAL);
			
			LinearLayout.LayoutParams innerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
			
			//add views to the layouts
			topInnerLayout.addView(date, innerParams);
			topInnerLayout.addView(dayName, innerParams);
			topInnerLayout.addView(notePresent, innerParams);
			
			innerLayout.addView(hours, innerParams);
			innerLayout.addView(wages, innerParams);
			innerLayout.addView(tips, innerParams);
			innerLayout.addView(dph, innerParams);
			
			//add layouts to outer layouts
			mainLayout.addView(topInnerLayout);
			mainLayout.addView(innerLayout);
			
			// track most profitable day
			if( (currentEntry.getWagesEarned() + currentEntry.getNetTips()) > highest){//if higher than current
				highestDate = date;//save the view
				highest = currentEntry.getWagesEarned() + currentEntry.getNetTips();
			}
			
		}
		
		char a = '\u2713';
		
		highestDate.setText( highestDate.getText().toString() + " " + Character.toString(a)   );
		
		return rootView;
	} 
	
	
	private void showNoteDialog(String s){
		AlertDialog.Builder noteDialog = new AlertDialog.Builder(getActivity());
		TextView noteView = new TextView(getActivity());
		noteView.setText(s);
		noteView.setGravity(Gravity.CENTER);
		noteView.setPadding(0, 30, 0, 30);
		noteDialog.setView(noteView);
		AlertDialog dialog = noteDialog.show();
		dialog.setCanceledOnTouchOutside(true);
	}
}
