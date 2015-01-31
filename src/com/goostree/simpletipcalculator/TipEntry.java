package com.goostree.simpletipcalculator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
@Deprecated
public class TipEntry implements Serializable, Comparable<TipEntry> {
	private static final long serialVersionUID = 1L;
	private Calendar Date;
	private double grossTips;
	private double netTips;
	private double hoursWorked;
	private double hourlyWage;
	private String note;
	private ArrayList<TipoutRecord> tipOuts;
	
	
	//default constructor
	public TipEntry() {
		Date = null;
		grossTips = 0;
		netTips = 0;
		tipOuts = null;
		
	}
	
	//standard constructor for this class
	public TipEntry(int year, int month, int day, double gTips, double nTips, double hours, double wage, ArrayList<TipoutRecord> tips){
		Date =  Calendar.getInstance();
		Date.clear();
		Date.set(year, month, day);
		grossTips = Math.round(gTips * 100) /100.0;	//rounded to two decimal places
		netTips = Math.round(nTips * 100) /100.0;
		tipOuts = tips;
		hoursWorked = Math.round(hours * 100) /100.0;
		hourlyWage = Math.round(wage * 100) /100.0;
	}
	
	//calendar only constructor
	public TipEntry(int year, int month, int day){
		Date =  Calendar.getInstance();
		Date.clear();
		Date.set(year, month, day);
		grossTips = 0;
		tipOuts = null;
	}
		
	public Calendar getDate(){
		return Date;
	}
	
	public String getDateToString(){
		return (
				(Date.get(Calendar.MONTH)+1) + "."
				+ Date.get(Calendar.DAY_OF_MONTH) + "."
				+ Date.get(Calendar.YEAR));
	}
		
	public double getGrossTips(){
		return grossTips;
	}
	
	public double getNetTips(){
		return netTips;
	}
	
	public double getHoursWorked(){
		return hoursWorked;
	}
	
	public double getWage(){
		return hourlyWage;
	}
	
	public double getDollarsPerHour(){
		double dph = (getWagesEarned() + netTips)/hoursWorked;
		return (Math.round( dph * 100) / 100.0);
	}
	
	public double getWagesEarned(){
		return Math.round( (hoursWorked * hourlyWage) * 100 ) /100.0;  //unnecessary parens, but w/e
	}
	
	public ArrayList<TipoutRecord> getTipoutInfo(){
		return tipOuts;
	}

	public int compareTo(TipEntry other) {
		Calendar otherDate = other.getDate();
		if( Date.before(otherDate)){
			return -1;			//if invoked class before other, return -1
		}
		else if (Date.after(otherDate)){
			return 1;			//if it's after, return 1
		}		
			return 0;			//otherwise same date		
	}
	
	public String getNote(){ //returns the note for the shift
		return note;
	}
	
	public void setNote(String s){//sets the note
		note = s;
	}
	
	public boolean hasNote(){//returns whether the note exists
		if(note != null && note.length() > 0){
			return true;
		}
		else{
			return false;
		}
	}
}
