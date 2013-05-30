package com.spaceflight.pad;

import java.util.ArrayList;
import java.util.HashMap;

import com.spaceflight.pad.activity.FolderBrowser;
import com.spaceflight.pad.object.FileInfo;
import com.spaceflight.pad.scanfiles.ScanFilesService;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.spaceflight.pad.R;

public class SFPApplication extends Application {
	public static HashMap<String,Integer> SFPFiles;
	public static Context mContext;
	
	public static ArrayList<FileInfo> mRootFolder;
	public static HashMap<String,ArrayList<FileInfo>> mFolderChild;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		mFolderChild = new HashMap<String,ArrayList<FileInfo>>();
		SFPFiles = new HashMap<String,Integer>();
		SFPFiles.put(getString(R.string.name_01), R.drawable.icon_01);
		SFPFiles.put(getString(R.string.name_02), R.drawable.icon_02);
		SFPFiles.put(getString(R.string.name_03), R.drawable.icon_03);
		SFPFiles.put(getString(R.string.name_04), R.drawable.icon_04);
		SFPFiles.put(getString(R.string.name_05), R.drawable.icon_05);
		SFPFiles.put(getString(R.string.name_06), R.drawable.icon_06);
		SFPFiles.put(getString(R.string.name_07), R.drawable.icon_07);
		SFPFiles.put(getString(R.string.name_08), R.drawable.icon_08);
		SFPFiles.put(getString(R.string.name_09), R.drawable.icon_09);
		SFPFiles.put(getString(R.string.name_10), R.drawable.icon_10);
		SFPFiles.put(getString(R.string.name_11), R.drawable.icon_11);
		SFPFiles.put(getString(R.string.name_12), R.drawable.icon_12);
		SFPFiles.put(getString(R.string.name_13), R.drawable.icon_13);
		SFPFiles.put(getString(R.string.name_14), R.drawable.icon_14);
		SFPFiles.put(getString(R.string.name_15), R.drawable.icon_15);
		SFPFiles.put(getString(R.string.name_16), R.drawable.icon_16);
		
		if(!Utils.mAlreadyStart){
			Intent mIntent = new Intent(this,ScanFilesService.class);
			startService(mIntent);
		}
		
		registeScreenStatusIntent();
	}
	
	private void registeScreenStatusIntent(){
		IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        
        registerReceiver(mReceiver, intentFilter);
	}
	
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            	Log.d("lilong","ScanFilesReceiver : "+action);
            	pauseMusic();
            } 
        }
    };
    
    @SuppressWarnings("static-access")
	private void pauseMusic(){
    	if(FolderBrowser.mBackgroundPlayer != null){
			if(FolderBrowser.mBackgroundPlayer.mPlayerThread != null){
				if(FolderBrowser.mBackgroundPlayer.mPlayerThread.isPlaying()){
					FolderBrowser.mBackgroundPlayer.mPlayerThread.pausePlayMusic();
				}
			}
		}
    }
}
