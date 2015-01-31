package com.goostree.simpletipcalculator.base;

import java.io.Serializable;
import java.util.Comparator;

public class TipEntryComparatorv2<T extends Comparable<T>> implements Serializable, Comparator<T> {
	private static final long serialVersionUID = 1L;

	public int compare(T a, T b) {
	    return a.compareTo(b);
	  }
	
}
