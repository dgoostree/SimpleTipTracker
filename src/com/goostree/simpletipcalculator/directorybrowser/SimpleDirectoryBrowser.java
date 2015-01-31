package com.goostree.simpletipcalculator.directorybrowser;

import java.io.File;
import java.util.Arrays;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.goostree.simpletipcalculator.R;
import com.goostree.simpletipcalculator.base.TipRecordv2;
import com.goostree.simpletipcalculator.util.IOUtil;

public class SimpleDirectoryBrowser {
	private SimpleDirectoryButton currentSelection;
	
	public SimpleDirectoryBrowser(final Context context, final TipRecordv2 tr){
		File f = new File(Environment.getExternalStorageDirectory().toString());
		int scale = Math.round(context.getResources().getDisplayMetrics().density);
		
			
		AlertDialog.Builder diag = new AlertDialog.Builder(context);
		diag.setPositiveButton("Ok", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(currentSelection != null){
					String msg = null;
					if(IOUtil.loadBackup(tr, currentSelection.getReference())){
						msg = "Backup loaded";
					}
					else {
						msg = "Error loading backup";
					}
					Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
				}				
			}
		});
		diag.setNegativeButton("Cancel", null);
		
		currentSelection = null;
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View directoryDialog = inflater.inflate(R.layout.fragment_directory_browser, null);
		update(context, directoryDialog, f);
		diag.setView(directoryDialog);
		AlertDialog d = diag.show();
		d.getWindow().setLayout(scale*360, scale*500);
	}
	
	//
	//Removes old directory contents from the layout, repopulates current directory contents
	private void update(Context context, View view, File newDir){
		File[] childrens = newDir.listFiles();
		
		if(childrens == null ){
			return;
		}
		Arrays.sort(childrens);
		View ruler;
				
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT );
		
		TextView currentLoc = (TextView) view.findViewById(R.id.current_directory);
		currentLoc.setText(newDir.getPath() + "/");
		
		LinearLayout layout = (LinearLayout) view.findViewById(R.id.directory_contents);
		layout.removeAllViews();
		
		addUpButton(context, layout, view, newDir, params); //add up button of appropriate
		ViewGroup.LayoutParams rulerParams= new ViewGroup.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, 2);
		for(File child : childrens){
			ruler = new View(context); 
			ruler.setBackgroundColor(0xbdbdbdbd);
			layout.addView(ruler, rulerParams);
			
			SimpleDirectoryButton btn = new SimpleDirectoryButton(context,child);
			btn.setText(child.getName());
			
			addListener(context, view, btn);
			if(child.isDirectory()){
				btn.setText(btn.getText().toString() + "/");
			}
			
			btn.setBackgroundColor(Color.TRANSPARENT);
			layout.addView(btn, params);
		}
		
		ruler = new View(context); 
		ruler.setBackgroundColor(0xbdbdbdbd);
		layout.addView(ruler, rulerParams);
	}
	
	//
	// Add ../ button if current directory has parent
	private void addUpButton(Context context, LinearLayout layout, View view, File newDir, LinearLayout.LayoutParams params){
		if(newDir.getParent() != null){
			View ruler = new View(context); 
			ruler.setBackgroundColor(0xbdbdbdbd);
			layout.addView(ruler,
					new ViewGroup.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, 2));
			SimpleDirectoryButton btn = new SimpleDirectoryButton(context, newDir.getParentFile());
			btn.setText("../");
			btn.setBackgroundColor(Color.TRANSPARENT);
			
			addListener(context, view, btn);
			
			layout.addView(btn, params);
			
		}
	}
	
	private void addListener( final Context context, final View view, final SimpleDirectoryButton btn){
		btn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(btn.getReference().isDirectory()){
					updateSelected(null);
					update(context, view, btn.getReference());  //if directory display its contents
				}
				else {  //otherwise file, select it
					updateSelected(btn);
				}
			}		
		});
	}
	
	//
	// Selects or deselects button
	private void updateSelected(SimpleDirectoryButton btn){		
		if(currentSelection != null){//if currently selected item exists
			currentSelection.setBackgroundColor(Color.TRANSPARENT); //transparent background
			currentSelection.setTextColor(0xffffffff);//holo dark text color
			
			if(currentSelection == btn){ //if reselecting an item
				currentSelection = null; //deselect and quit
				return;
			}
		}
		
		currentSelection = btn;
		
		if(currentSelection != null) {
			currentSelection.setBackgroundColor(currentSelection.getCurrentTextColor());
			currentSelection.setTextColor(0xFF000000);
		}
	}
}
