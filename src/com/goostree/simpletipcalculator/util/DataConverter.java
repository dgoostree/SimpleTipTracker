package com.goostree.simpletipcalculator.util;

import java.util.ArrayList;
import java.util.Calendar;

import com.goostree.simpletipcalculator.TipEntry;
import com.goostree.simpletipcalculator.TipRecord;
import com.goostree.simpletipcalculator.TipoutRecord;
import com.goostree.simpletipcalculator.base.TipEntryv2;
import com.goostree.simpletipcalculator.base.TipRecordv2;
import com.goostree.simpletipcalculator.base.TipoutRecordv2;
@SuppressWarnings("deprecation")
public class DataConverter {
	public static TipRecordv2 convert(Object obj){
		if(obj instanceof TipRecordv2){ //if no conversion necessary
			return (TipRecordv2)obj;    //just return i
		}
		else { //otherwise we need to convert it
			TipRecord old = (TipRecord)obj;
			TipRecordv2 newRecord = new TipRecordv2();
			
			//
			//Replace the contents of the tip log
			ArrayList<TipEntry> oldLog = old.getTipLog();  //reference old tip log
			ArrayList<TipEntryv2> newLog = new ArrayList<TipEntryv2>(); //create new tip log

			ArrayList<TipoutRecordv2> newTipee;
			for(TipEntry shift : oldLog){ //for every shift in the log
				newTipee = new ArrayList<TipoutRecordv2>(); //create new tipout list for it
				ArrayList<TipoutRecord> oldTipee = shift.getTipoutInfo();//reference the old tipout list
				for(TipoutRecord tipee : oldTipee){//for each tipout
					newTipee.add(new TipoutRecordv2(tipee.getName(), tipee.getAmount()));//add new tipee
				}//the shifts tipoutrecord is now complete
				
				Calendar day = shift.getDate();
				TipEntryv2 newEntry =new TipEntryv2(day.get(Calendar.YEAR), 
						day.get(Calendar.MONTH), 
						day.get(Calendar.DATE), 
						shift.getGrossTips(),
						shift.getNetTips(),
						shift.getHoursWorked(),
						shift.getWage(),
						newTipee);
				if(shift.hasNote()){
					newEntry.setNote(shift.getNote());
				} 
				
				newLog.add(newEntry);
				
			}
			newRecord.replaceTipLog(newLog);//new record has the log now
			ArrayList<String> tipees = old.getTipeeList();
			ArrayList<String> newTipees = new ArrayList<String>();
			for(String tipee : tipees){
				newTipees.add(tipee);
			}//new tipee list built
			newRecord.setJobDetails(newTipees, 
					old.getPayRate(), 
					old.getWeekStart()); //job details set
			if(newRecord.isCalendarDisabled() != old.isCalendarDisabled()){
				newRecord.toggleShowCalendar();
			}
			return newRecord;
		}//end conversion
	}
}
