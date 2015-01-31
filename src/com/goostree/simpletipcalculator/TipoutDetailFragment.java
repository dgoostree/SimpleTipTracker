package com.goostree.simpletipcalculator;

import java.util.ArrayList;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TipoutDetailFragment extends Fragment {
	private double scale;
	private static final int TIPOUT_OFFSET = 3235;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_view_tipout_detail, container, false);
		ViewTipActivity viewActivity = (ViewTipActivity)getActivity();

		scale = viewActivity.getBaseContext().getResources().getDisplayMetrics().density; //get screen density


		//set the date label
		TextView currentText = (TextView)rootView.findViewById(R.id.view_date_range_content);
		currentText.setText((viewActivity.fromMonth+1) + "." + viewActivity.fromDay + "." + viewActivity.fromYear + " - " + (viewActivity.toMonth+1) 
				+ "." + viewActivity.toDay + "." + viewActivity.toYear);		


		double grossTips = viewActivity.tipRecord.getGrossTipTotal(viewActivity.dateList);	//get the gross tips total
		ArrayList<String>tipeeNames = new ArrayList<String>();						//this will hold tipout list
		ArrayList<Double> tipeeAmt = new ArrayList<Double>();						//this will hold tipout amounts
		double tipouts = viewActivity.tipRecord.getTipoutTotals(viewActivity.dateList, tipeeNames, tipeeAmt);//get the net tips, list of names, corresponding tipout values
		double netTips = viewActivity.tipRecord.getNetTipsTotal(viewActivity.dateList);	//get netTips
		double wages =viewActivity.tipRecord.getWagesTotal(viewActivity.dateList);		//get wages

		TextView tempView = (TextView)rootView.findViewById(R.id.view_wages_content);	//get wages view
		tempView.setText(wages+"");														//set its text
		tempView.setVisibility(View.VISIBLE);											//and visible


		tempView = (TextView)rootView.findViewById(R.id.view_gross_tips_content);	//get gross tips amount field into holder textview
		tempView.setText(grossTips + "");											//set its text
		tempView.setVisibility(View.VISIBLE);										//and its visible

		tempView = (TextView)rootView.findViewById(R.id.view_tipouts_content);		//get tipouts content field
		tempView.setText( tipouts + "");											//set it's text
		tempView.setVisibility(View.VISIBLE);

		tempView = (TextView)rootView.findViewById(R.id.view_net_tips_content);		//likewise for netTips
		tempView.setText(netTips+""); 												// woot
		tempView.setVisibility(View.VISIBLE);

		tempView = (TextView) rootView.findViewById(R.id.view_earnings_content)	;	//and for earnings
		tempView.setText((Math.round((netTips + wages)*100) /100.0)+"");
		tempView.setVisibility(View.VISIBLE);


		//
		//Dynamically display correct amount of data
		int totalTipoutCount = tipeeNames.size(); 											//get tipout count

		//RelativeLayout to add to
		RelativeLayout rl = (RelativeLayout)rootView.findViewById(R.id.view_detail_layout);


		//for each record, set name and amount values
		for(int i = 0; i < totalTipoutCount; i++){

			//Create and place the name label
			TextView name = new TextView(getActivity());
			name.setId(TIPOUT_OFFSET + i);
			name.setText( tipeeNames.get(i));

			//Parameters for the name label
			RelativeLayout.LayoutParams nameParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);

			nameParams.setMargins((int) (15 * scale), //margins to 15, and 3 dp
					(int) (3 * scale), 
					(int) (3 * scale), 
					(int) (3 * scale));				

			nameParams.width = (int) (90 * scale);

			if(i > 0) {
				nameParams.addRule(RelativeLayout.BELOW, TIPOUT_OFFSET + i - 1);
			}
			else {
				nameParams.addRule(RelativeLayout.BELOW, R.id.tipout0);
			}

			rl.addView(name, nameParams);				

			//Create and place the amount label
			TextView amt = new TextView(getActivity());
			amt.setText(tipeeAmt.get(i)+"");
			amt.setGravity(Gravity.CENTER);

			//Parameters for the name label
			RelativeLayout.LayoutParams amtParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);

			amtParams.width = (int) (150 * scale + 0.5f);						//150dp wide
			amtParams.addRule(RelativeLayout.ALIGN_BASELINE,TIPOUT_OFFSET + i);	//aligned baseline with the name
			amtParams.addRule(RelativeLayout.RIGHT_OF, TIPOUT_OFFSET + i);		//and to right of it

			rl.addView(amt, amtParams);
		}




		return rootView;
	}	
}
