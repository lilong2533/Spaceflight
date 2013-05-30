package com.spaceflight.pad.scanfiles;

import com.spaceflight.pad.Utils;
import com.spaceflight.pad.activity.FolderBrowser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScanFilesReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if(!Utils.mAlreadyStart){
			Intent mIntent = new Intent(context,ScanFilesService.class);
			context.startService(mIntent);
		}
		
		if(Intent.ACTION_BOOT_COMPLETED.equals(action)){ 
			Intent mIntent = new Intent(context,FolderBrowser.class);
			mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS); 
			context.startActivity(mIntent);
		}
	}
}
