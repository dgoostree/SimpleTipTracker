package com.goostree.simpletipcalculator.base;

import java.io.Serializable;

public class TipoutRecordv2 implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private double amount;
	
	public TipoutRecordv2(){
		name = null;
		amount = 0;
	}
	
	public TipoutRecordv2(String nm, double amt){
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
