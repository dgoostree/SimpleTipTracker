package com.goostree.simpletipcalculator;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter {
	private int tabCount;
	
	public TabsPagerAdapter(FragmentManager fm, int count) {
		super(fm);
		tabCount = count;
	}
	
	@Override
	public Fragment getItem(int index) {
		switch (index) {
		case 0:
			return new TipoutDetailFragment();
		case 1:
			return new DailyDetailFragment();
		case 2:
			return new WeeklyDetailFragment();
		case 3:
			return new MonthlyDetailFragment();
		}
		
		return null;
	}
	
	@Override
	public int getCount() {
		return tabCount;
	}
	
}