package com.spaceflight.pad.activity;

import java.util.ArrayList;

import com.spaceflight.pad.R;
import com.spaceflight.pad.SFPApplication;
import com.spaceflight.pad.Utils;
import com.spaceflight.pad.filebrowser.FileUtil;
import com.spaceflight.pad.object.FileInfo;
import com.spaceflight.pad.object.SFPGallery;
import com.spaceflight.pad.object.SFPGallery.ViewSwitchListener;
import com.spaceflight.pad.object.SFPImageAdapter;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class ImageBrowser extends Activity{
	public static DisplayMetrics mMetric;
	private SFPGallery mSFPGallery;
	private SFPImageAdapter mSFPImageAdapter;
	
	private String mFolderName;
	private Toast mToast;
	
    private int currentOrientation;
    private int OrientationImageSwitch = -1;
    
    private ArrayList<FileInfo> mChildFileList;
    private ArrayList<FileInfo> mLandScapeList;
    private ArrayList<FileInfo> mPortraitList;
    
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        currentOrientation = getWindowManager().getDefaultDisplay().getOrientation();
        getWindow().getDecorView().setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
        
        mMetric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(mMetric);
        
        Intent intent = getIntent();
        mFolderName = intent.getStringExtra("FolderName");
        
        setContentView(R.layout.image_browser);
        mSFPGallery =(SFPGallery) findViewById(R.id.sfp_gallery);
        mSFPGallery.setOnViewSwitchListener(new ViewSwitchListener(){

			@Override
			public void onSwitched(View view, int position) {
				showToast(position);
			}
        });
        
        initAdapterData();
        
        mSFPImageAdapter = new SFPImageAdapter(this,currentOrientation,OrientationImageSwitch);
        mSFPGallery.setAdapter(mSFPImageAdapter);
        mSFPImageAdapter.updateAdapter(mChildFileList,mLandScapeList, mPortraitList);
        showToast(0);
	}
	
	private void showToast(int whichScreen){
		String message = "";
		if(whichScreen == 0){
			message = getResources().getString(R.string.first_page);
		}else if(whichScreen == mSFPImageAdapter.getCount() - 1){
			message = getResources().getString(R.string.last_page);
		}else{
			message = getResources().getString(R.string.current_page,whichScreen + 1);
		}
		
		if(mToast == null){
//			mToast = Toast.makeText(this,message,Toast.LENGTH_SHORT);
			View toastRoot = View.inflate(SFPApplication.mContext,R.layout.toast_view, null);
			mToast = new Toast(SFPApplication.mContext);
			mToast.setView(toastRoot);
		}
		
		TextView tv = (TextView)mToast.getView().findViewById(R.id.toastinfo);
		tv.setText(message);
		mToast.show();
	}
	
	private void initAdapterData(){
		try{
        	if(mFolderName != null){
        		mChildFileList = SFPApplication.mFolderChild.get(mFolderName);
        		
        		if(mChildFileList == null){
        			mChildFileList = FileUtil.getFiles(Utils._rootPath+"/"+mFolderName,false);
        		}
        		
        		int size = mChildFileList.size();
    			if(size == 1){
    				FileInfo file0 = mChildFileList.get(0);
    				if(file0.Name.equals(Utils._landPic) || file0.Name.equals(Utils._portPic)){
        				if(file0.IsDirectory){
        					mLandScapeList = mPortraitList = file0.mChildFiles;
        				}
        				OrientationImageSwitch = 1;
    				}
    			}else if(size == 2){
    				OrientationImageSwitch = 0;
    				FileInfo file1 = mChildFileList.get(0);
    				if(file1.Name.equals(Utils._landPic) || file1.Name.equals(Utils._portPic)){
    					if(file1.IsDirectory){
        					if(file1.Name.equals(Utils._landPic)){
        						mLandScapeList = file1.mChildFiles;
        						OrientationImageSwitch++;
        					}else if(file1.Name.equals(Utils._portPic)){
        						mPortraitList =  file1.mChildFiles;
        						OrientationImageSwitch++;
        					}
        				}
    				}
    				
    				FileInfo file2 = mChildFileList.get(1);
    				if(file2.Name.equals(Utils._landPic) || file2.Name.equals(Utils._portPic)){
    					if(file2.IsDirectory){
        					if(file2.Name.equals(Utils._landPic)){
        						mLandScapeList = file2.mChildFiles;
        						OrientationImageSwitch++;
        					}else if(file2.Name.equals(Utils._portPic)){
        						mPortraitList =  file2.mChildFiles;
        						OrientationImageSwitch++;
        					}
        				}
    				}
    			}
        			
            }
        }catch(Exception e){
        	e.printStackTrace();
        }
	}
	
	@SuppressWarnings("static-access")
	@Override
	protected void onResume() {
		super.onResume();
		if(FolderBrowser.mBackgroundPlayer != null){
			if(FolderBrowser.mBackgroundPlayer.mPlayerThread != null){
				if(!FolderBrowser.mBackgroundPlayer.mPlayerThread.isPlaying()){
					FolderBrowser.mBackgroundPlayer.mPlayerThread.pausePlayMusic();
				}
			}
		}
	}
	
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
        mMetric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(mMetric);
		
		if(newConfig.orientation != currentOrientation){
			currentOrientation = newConfig.orientation;
		}
		mSFPImageAdapter.setOrientation(currentOrientation, OrientationImageSwitch);
	}
	

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mSFPImageAdapter != null){
			mSFPImageAdapter.clear();
		}
	}
}
