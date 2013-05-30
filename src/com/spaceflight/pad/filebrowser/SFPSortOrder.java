package com.spaceflight.pad.filebrowser;

import java.util.Comparator;
import java.util.HashMap;

import android.content.Context;

import com.spaceflight.pad.R;
import com.spaceflight.pad.SFPApplication;
import com.spaceflight.pad.object.FileInfo;

public class SFPSortOrder implements Comparator<FileInfo> {
	private static SFPSortOrder instance;
	private HashMap<String,Integer> SFPRoot = new HashMap<String,Integer>();
	
	private SFPSortOrder(){
		Context mContext = SFPApplication.mContext;
		
		SFPRoot = new HashMap<String,Integer>();
		SFPRoot.put(mContext.getString(R.string.name_01),1);
		SFPRoot.put(mContext.getString(R.string.name_02),2);
		SFPRoot.put(mContext.getString(R.string.name_03),3);
		SFPRoot.put(mContext.getString(R.string.name_04),4);
		SFPRoot.put(mContext.getString(R.string.name_05),5);
		SFPRoot.put(mContext.getString(R.string.name_06),6);
		SFPRoot.put(mContext.getString(R.string.name_07),7);
		SFPRoot.put(mContext.getString(R.string.name_08),8);
		SFPRoot.put(mContext.getString(R.string.name_09),9);
		SFPRoot.put(mContext.getString(R.string.name_10),10);
		SFPRoot.put(mContext.getString(R.string.name_11),11);
		SFPRoot.put(mContext.getString(R.string.name_12),12);
		SFPRoot.put(mContext.getString(R.string.name_13),13);
		SFPRoot.put(mContext.getString(R.string.name_14),14);
		SFPRoot.put(mContext.getString(R.string.name_15),15);
		SFPRoot.put(mContext.getString(R.string.name_16),16);
	}
	
	public static SFPSortOrder getInstance(){
		if(instance == null){
			instance = new SFPSortOrder();
		}
		return instance;
	}

	public int compare(FileInfo file1, FileInfo file2) {
		Integer c1 = SFPRoot.get(file1.Name);
		Integer c2 = SFPRoot.get(file2.Name);
		
		if(SFPRoot != null && SFPRoot.size() <= 16){
			if(c1 != null && c2 != null && c1 > 0 && c2 > 0){
				return c1.compareTo(c2);
			}else{
				return file1.Name.compareTo(file2.Name);
			}
		}
		return file1.Name.compareTo(file2.Name);
	}
}
