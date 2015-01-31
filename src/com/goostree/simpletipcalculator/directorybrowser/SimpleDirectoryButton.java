package com.goostree.simpletipcalculator.directorybrowser;

import java.io.File;

import android.content.Context;
import android.widget.Button;

public class SimpleDirectoryButton extends Button{
	private File fileReference;
	
	public SimpleDirectoryButton(Context context, File reference) {
		super(context);
		fileReference = reference;
	}
	
	public File getReference(){
		return fileReference;
	}

}
