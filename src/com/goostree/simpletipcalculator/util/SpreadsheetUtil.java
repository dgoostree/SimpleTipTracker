package com.goostree.simpletipcalculator.util;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Font;

public class SpreadsheetUtil {
	private static final String[] boldRows = {"Date", "Total", "Tipee"};
	public static HSSFWorkbook getNewWorkbook(){
		return new HSSFWorkbook();
	}
	
	public static HSSFSheet getNewSheet(String name, HSSFWorkbook book){
		return book.createSheet(name);
	}
	
	public static void addRow(HSSFWorkbook book, HSSFSheet sheet, int rowNum, String[] values){
		Boolean boldRow = isBoldRow(values[0]);
		HSSFCellStyle style = book.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		HSSFRow row = sheet.createRow(rowNum);
		for(int i = 0; i < values.length; i++){
			HSSFCell cell = row.createCell(i);
			cell.setCellValue(values[i]);
			
			if(boldRow){
				Font font = book.createFont();
		        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		        style.setFont(font);
			}
			
			cell.setCellStyle(style);
		}
	
	}
	
	private static boolean isBoldRow(String current){
		for(String bold : boldRows){
			if (current.equals(bold)){
				return true;
			}
		}
		return false; 
	}
	
	public static File createFile(String name, String root){
    	File dir = new File(root + "/SimpleTipTracker/");
    	dir.mkdirs();
    	return new File(dir, name);
    }
	
	public static void writeFile(File f, HSSFWorkbook book){
		FileOutputStream fileOut = null;
		try {
			if(f.exists()){
				f.delete();
			}
			
			fileOut = new FileOutputStream(f);
			book.write(fileOut);
			fileOut.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
