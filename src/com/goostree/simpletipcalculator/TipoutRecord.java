package com.goostree.simpletipcalculator;

import java.io.Serializable;
@Deprecated
public class TipoutRecord implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private double amount;
	
	public TipoutRecord(){
		name = null;
		amount = 0;
	}
	
	public TipoutRecord(String nm, double amt){
		name = nm;
		amount = Math.round(amt * 100) / 100.00;  //round to two decimal places
	}
	
	public String getName(){
		return name;
	}
	
	public double getAmount(){
		return amount;
	}
}
