package com.goostree.simpletipcalculator;

import java.io.Serializable;
import java.util.Comparator;
@Deprecated
public class TipEntryComparator<T extends Comparable<T>> implements Serializable, Comparator<T> {
	private static final long serialVersionUID = 1L;

	public int compare(T a, T b) {
	    return a.compareTo(b);
	  }
	
}
