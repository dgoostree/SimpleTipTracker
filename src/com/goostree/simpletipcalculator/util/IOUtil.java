package com.goostree.simpletipcalculator.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import android.content.Context;

import com.goostree.simpletipcalculator.base.TipEntryv2;
import com.goostree.simpletipcalculator.base.TipRecordv2;

public class IOUtil  {
	public static final String FILENAME = "simpleTipTracker.ser";
	public static final String BACKUP_FILENAME = "simpleTipTrackerBackup.ser";
	public static final String REMOTE_BACKUP_FILENAME = "myTipRecord.backup";

	public static void save(Context context) {
		FileOutputStream fout;
		ObjectOutputStream oout;

		try {
			fout = context.openFileOutput(FILENAME, Context.MODE_PRIVATE); // open the
																	// outputstream
			oout = new ObjectOutputStream(fout); // instantiate the objectout
												 // stream

			oout.writeObject(context);
			oout.close();
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean loadBackup(Context context){
		return loadBackup(context, new File(context.getFilesDir(), BACKUP_FILENAME));
	}
	
	public static boolean loadBackup(Context context, File f) {//TODO change context to tiprecord
		TipRecordv2 tipRecord = (TipRecordv2) context;

		if(f.exists() && f.length() != 0){
			try {
				FileInputStream fin = new FileInputStream(f);
				ObjectInputStream oin = new ObjectInputStream(fin);
				TipRecordv2 savedTipRecord = (TipRecordv2) oin.readObject();
				oin.close();
				fin.close();

				if(savedTipRecord != null){ //if tiprecord found
					tipRecord.replaceTipLog(savedTipRecord.getTipLog()); //replace it with backup
					tipRecord.setJobDetails(savedTipRecord.getTipeeList(), savedTipRecord.getPayRate(), savedTipRecord.getWeekStart());//
					IOUtil.save(tipRecord);//and save it 
					return true;
				}
				else {
					return false;
				}

			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		return false;
	}
	
	public static void saveBackup(Context context){
		FileOutputStream fout;
    	ObjectOutputStream oout;

    	try {
    	  fout = context.openFileOutput(BACKUP_FILENAME, Context.MODE_PRIVATE);	//open the outputstream
    	  oout = new ObjectOutputStream(fout);							//instantiate the objectout stream
    	  
    	  oout.writeObject(context);
    	  
    	  oout.close();
      	  fout.close();
    	} catch (Exception e) {
    	  e.printStackTrace();
    	}
	}
	
	public static void sendBackup(Context context){
		
	}
	
	//
	// NIO file copy
	public static void copyFile(File sourceFile, File destFile, Context context) throws IOException {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;

	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}
	
	//
	//Reads the tiprecord object from disk, if exists
	public static void readRecordFromFile(Context context){
		TipRecordv2 tipRecord = (TipRecordv2) context;
		File f = new File(context.getFilesDir(),FILENAME);   

		if(f.exists()){
			try {
				if(f.length() != 0){
					FileInputStream fin = new FileInputStream(f);
					ObjectInputStream oin = new ObjectInputStream(fin);
					Object obj = oin.readObject();
					
					
					
					TipRecordv2 savedTipRecord = DataConverter.convert(obj);
					oin.close();
					fin.close();

					if(savedTipRecord != null){ //if exists, replace current activity's tip record
						tipRecord.replaceTipLog(savedTipRecord.getTipLog()); 
						tipRecord.setJobDetails(savedTipRecord.getTipeeList(), savedTipRecord.getPayRate(), savedTipRecord.getWeekStart());
					}
				} 
				else{
					f.createNewFile();		
				}
			}
			catch(IOException e){
				e.printStackTrace();
			}
			catch(ClassNotFoundException e){
				e.printStackTrace();
			}
		}
	}
	
	//
    //Creates backup tiprecord, if necessary
    public static void createBackup(Context context){
    	//if a backup file doesn't exist, create it
        File backup = new File(context.getFilesDir(), BACKUP_FILENAME);
        if( !backup.exists() ) {
        	try {
				backup.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
    
    public static void deleteData(Context context){		
		IOUtil.saveBackup(context);			//backup the file before we remove it
		TipRecordv2 tipRecord = (TipRecordv2) context;
		tipRecord.replaceTipLog(new ArrayList<TipEntryv2>());	//empty tipee list
		tipRecord.setJobDetails(new ArrayList<String>(), 0, 1);//default job details
		IOUtil.save(tipRecord);							//save it
	}
}
