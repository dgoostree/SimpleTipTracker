//
//TODO: Change lists into hashmaps in the appropriate places, check your evernote if you forget
//TODO: Standardize getter and setter conventions and process, dataconverter really highlighted this as an issue
//TODO: Never assume you'll just leave a project alone when "finished", always code as if you'll maintain it.
//		You'll avoid the maintenance issues you've had here.
package com.goostree.simpletipcalculator.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import android.app.Application;
//
//  TipRecord - Extends Application so its data persists for entire scope of the app
//   This is not ideal, TODO 
//
public class TipRecordv2 extends Application implements Serializable {
	private static final long serialVersionUID = 1L;

	private ArrayList<TipEntryv2> tipLog;
	private ArrayList<String> tipeeList;
	private double payRate;
	private int weekStart;
	private boolean disableCalendar;

	public TipRecordv2(){
		tipLog = new ArrayList<TipEntryv2>();
		tipeeList = new ArrayList<String>();
		payRate = 0;
		weekStart = 1;
		disableCalendar = false;		
	}
	
	//
	// Insertion sort for a standard ArrayList - update this later please
	public void addRecordSorted(TipEntryv2 entry){
		if(tipLog.size() == 0){
			tipLog.add(entry); 	//if the tip record is empty, just add it
			return;				//and return
		}
		
		int i = getDateAddOffset(entry);
		if(i >= tipLog.size()){
			tipLog.add(entry);
		}
		else {
			tipLog.add(i, entry );
		}
	}
	
	
	public void addTipee(String name){
		tipeeList.add(name);
	}
	
	public void editTipee(int index, String name){
		tipeeList.set(index, name);
	}
	
	public void removeTipee(String name){
		tipeeList.remove(name);
	}
	
	//assign a new tipee list, and wage info from edit info
	public void setJobDetails( ArrayList<String> newList, double hourlyWage, int week){
		tipeeList = newList;
		payRate = hourlyWage;
		weekStart = week;
	}
	
	public double getPayRate() {
		return payRate;
	}
	
	public void removeTipEntry(int index){
		tipLog.remove(index);
	}
	
	public TipEntryv2 getTipEntryByOffset(int x){
		return tipLog.get(x);
	}
	
	//
	// Returns a reference to a tipentry in the log if an entry
	//	for the specified date is present.  Else returns null;
	public TipEntryv2 getTipEntryByDate(int year, int month, int day){
				
		int offset = 0;
		TipEntryv2 target = new TipEntryv2(year, month, day);	//temporary tipentry to find the offset
		offset = Collections.binarySearch(tipLog, target, new TipEntryComparatorv2<TipEntryv2>()); // find where it goes
		
		if (offset < 0) {
			return null;  //returns null if nothing found
		}
		else {
			return getTipEntryByOffset(offset);
		}
	}
	
	//
	// Returns a reference to a tipentry in the log if an entry
	//	for the specified date is present.  Else returns null;
	public int getTipEntryOffsetByDate(int year, int month, int day){		
		int offset = 0;
		TipEntryv2 target = new TipEntryv2(year, month, day);	//temporary tipentry to find the offset
		offset = Collections.binarySearch(tipLog, target, new TipEntryComparatorv2<TipEntryv2>()); // find where it goes
		
		if(offset >= 0){
			return offset;
		}
		else{		
			return -1;//since the record was pulled from the tiplog, no need to error check here
		}
	}
	
	public ArrayList<String> getTipeeList() {
		return tipeeList;
	}
	
	public ArrayList<TipEntryv2> getTipLog() {
		return tipLog;
	}
	
	//
	// Returns an arraylist of TipEntry with the range specified (both inclusive)
	//	Returns null if invalid parameters were specified.
	public ArrayList<TipEntryv2> getTipInfoInRange(int fromOffset, int toOffset){
		
		//if incorrect from and to dates were chosen
		if( fromOffset > toOffset || fromOffset >= tipLog.size() || toOffset < 0)
		{
			return null;
		}
				
		ArrayList<TipEntryv2> rangeList = new ArrayList<TipEntryv2>();	//create new ArrayList
		
		if(toOffset == tipLog.size()){		//if to offset == size, the to date was greater than all
			--toOffset;						//so decrement to last entry in list
		}
		
		for( int i = fromOffset; i <= toOffset; i++){
			rangeList.add(getTipEntryByOffset(i));
		}
		
		return rangeList;
	}
	
	
	//
	// find proper offset for addition
	private int getDateAddOffset(TipEntryv2 targetDate){
		
		int offset = 0;
		 offset = Collections.binarySearch(tipLog, targetDate, new TipEntryComparatorv2<TipEntryv2>()); // find where it goes
		 if (offset < 0) {		//if it doesnt exist in the list already
			 offset = ~offset;  //get its complement to return 
		 }
		 else {					//otherwise duplicate found, delete the old one - TipRecord doesn't support multiple shifts per day
			 tipLog.remove(offset);
		 }
		 
		 return offset;
	}
	
	//
	// Returns the index of the first element >= the specified date
	public int getRangeFromIndex(int year, int month, int day){
				
		int offset = 0;
		TipEntryv2 target = new TipEntryv2(year, month, day);	//temporary tipentry to find the offset
		offset = Collections.binarySearch(tipLog, target, new TipEntryComparatorv2<TipEntryv2>());
		
		if (offset < 0) {
			offset = ~offset;
		}
		
		return offset;
	}
	
	
	//
	// Returns the index of the first element >= the specified date
	public int getRangeToIndex(int year, int month, int day){
		
		TipEntryv2 target = new TipEntryv2(year, month, day);	//temporary tipentry to find the offset
		int offset = 0;
		offset = Collections.binarySearch(tipLog, target, new TipEntryComparatorv2<TipEntryv2>());
		
		if(offset < 0) {		//if specific date wasnt found
			offset = ~offset;			//if specific date not found, convert to next highest
			
			if(offset < tipLog.size()){
				if (offset == 0){										//if first index was returned
					if ( tipLog.get(offset).compareTo(target) > 0   ) {		//if the first record in the log is greater than target
						return -1;											//return invalid
					}
					else {
						return offset;										//else return the 0
					}
				}
			}
			
			return (offset-1);	//if offset was < 0, binarySearch returned the next highest offset,
								//  which isn't in our desired range
		}
		else {					//otherwise it was found
			return offset;
		}
				
	}
	
	//Adds up the gross tips from an arraylist of entries
	public double getGrossTipTotal(ArrayList<TipEntryv2> theList){
		int size = theList.size();
		double amt = 0;
		
		for (int i = 0; i < size; i++){
			amt += theList.get(i).getGrossTips();
		}
		
		return (Math.round(amt * 100) /100.0);
	}
	
	//
	//Returns a double of the total tipouts, and populates two arraylists (parameters) with corresponding
	//	tipout recipient and total.  Ensures each tipee is listed only once.
	//
	public double getTipoutTotals(ArrayList<TipEntryv2> theList, ArrayList<String> theNames, ArrayList<Double> theAmounts){
		double tipoutTotal = 0;
		int size = theList.size();
		
		for(int i = 0; i < size; i++){		//for each date saved
			TipEntryv2 currentEntry = theList.get(i);		//get the current tipentry
			ArrayList<TipoutRecordv2> currentEntryTipouts = currentEntry.getTipoutInfo();//get the tipout record for the entry
			
			
			if(currentEntryTipouts != null) {	//if we didn't get a bad object
				int temp = currentEntryTipouts.size();	//save its size
				for(int j = 0; j < temp; j++){			//for all records today
					TipoutRecordv2 currentRecord = currentEntryTipouts.get(j);//get the record
					String name = currentRecord.getName();					//store the name
					double amt = currentRecord.getAmount();					//store the amount
					
					if(theNames.contains(name)){	// if a record was already saved for the name
						int offset = theNames.indexOf(name);		//get it's current offset
						double y = theAmounts.get(theNames.indexOf(name));			//store its current value
						y += amt;									//add this tip info to it
						theAmounts.set(offset, y);					//replace it in the arraylist						
					}
					else {						//otherwise we'll add it
						theNames.add(name);
						theAmounts.add(amt);	//these should always correspond due to error checking in the ui
					}
					
					tipoutTotal += amt;		//add this tip's info to the net tipout total
				}
				
			}
			
		}
		
		
		return Math.round(tipoutTotal * 100) /100.0;
	}
	
	public double getNetTipsTotal(ArrayList<TipEntryv2> theList){
		double netTips = 0;
		int size = theList.size();
		
		
		for(int i = 0; i < size; i++){
			TipEntryv2 currentEntry = theList.get(i);		//get the current tipentry			
			netTips += currentEntry.getNetTips();			
		}
			
		return (Math.round(netTips*100) /100.0);   //this isn't rounded
	}
	
	public double getWagesTotal(ArrayList<TipEntryv2> theList){
		double totalWages = 0;
		int size = theList.size();
		
		for(int i=0; i<size; i++) {
			totalWages += theList.get(i).getWagesEarned();
		}
		
		return (Math.round(totalWages * 100) / 100.0);
	}
	
	public double getDPHTotal(ArrayList<TipEntryv2> theList){  //the total DPH of a period is the average of their
		double totalDPH = 0;								 //individual DPH's
		int size = theList.size();
		
		for(int i = 0; i < size; i++){
			totalDPH += theList.get(i).getDollarsPerHour(); //add each individual dollars per hour value
		}
		totalDPH = totalDPH/size;  //find their average
		
		return (Math.round(totalDPH * 100)/ 100.0);
	}
	
	public void replaceTipLog(ArrayList<TipEntryv2> newLog){
		tipLog = newLog;
	}
	
	/*public void deleteData(){		
		IOUtil.saveBackup(this);			//backup the file before we remove it
		
		tipLog = new ArrayList<TipEntry>();	//replace contents with blanks
		tipeeList = new ArrayList<String>();
		payRate = 0;
		weekStart = 1;
		IOUtil.save(this);							//save it
	}*/	
    
   public void setWeekStart(int n) {
	   weekStart = n;
   }
   
   public int getWeekStart(){
	   return weekStart;
   }
   
   public void toggleShowCalendar(){
	   disableCalendar = !disableCalendar;
   }
   
   public boolean isCalendarDisabled(){
	   return disableCalendar;
   }
    
   public long getSerialversionuid() {
		return serialVersionUID;
	}
}
     
