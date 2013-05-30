package com.spaceflight.pad.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.spaceflight.pad.R;
import com.spaceflight.pad.SFPApplication;
import com.spaceflight.pad.Utils;
import com.spaceflight.pad.filebrowser.FileUtil;
import com.spaceflight.pad.filebrowser.GridViewAdapter;
import com.spaceflight.pad.object.FileInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

public class FolderBrowser extends Activity{
	public static MyHandler mHandler;
	public static BackgroundPlayer mBackgroundPlayer;
	private ArrayList<FileInfo> _files;
	private String _currentPath = Utils._rootPath;
	
	private GridView mGridView;
	private RelativeLayout _fileContainer;
	private GridViewAdapter mAdapter;
	
	private boolean longClickAction;
	private int currentOrientation;
	
	private ProgressDialog mDialog;
	
	private ActivityManager am;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);  
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		
		mHandler = new MyHandler();
		currentOrientation = getWindowManager().getDefaultDisplay().getOrientation();
		setContentView(R.layout.folder_browser);
		
		initUI();
		InitData();
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		if(Utils._firstTime.exists()){
			playBackgroundMusic();
		}else{
			try {
				Utils._firstTime.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("static-access")
	private void playBackgroundMusic(){
		if(mBackgroundPlayer == null){
			mBackgroundPlayer = BackgroundPlayer.getSington(this);
		}
		
		if(mBackgroundPlayer.mPlayerThread != null){
			if(mBackgroundPlayer.mPlayerThread.isPlaying()){
				return;
			}else{
				mBackgroundPlayer.mPlayerThread.pausePlayMusic();
			}
		}
	}
	
	private void initUI(){
		_files = new ArrayList<FileInfo>();
		_fileContainer = (RelativeLayout) findViewById(R.id.file_container);
		mGridView = (GridView) findViewById(R.id.my_gridview);
		
		mGridView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(!longClickAction){
					FileInfo file = _files.get(position);
					if (file.IsDirectory) {
						if(SFPApplication.SFPFiles.containsKey(file.Name)){
							Intent newIntent = new Intent(FolderBrowser.this,ImageBrowser.class);
							newIntent.putExtra("FolderName", file.Name);
							startActivity(newIntent);
						}else{
							ArrayList<FileInfo> mChildFiles = file.mChildFiles;
							if(mChildFiles != null && mChildFiles.size() > 0){
								viewFiles(file.Path);
			        		}
						}
					}else {
						openFile(file.Path);
					}
				}
			}
		});
	}
	
	private void InitData(){
		File sdcardPath = new File(Utils._sdcardPath);
		File rootPath = new File(Utils._rootPath);
		if(sdcardPath.exists() || !rootPath.exists()){
			mDialog = ProgressDialog.show(this, "",getString(R.string.please_wait_for_data_copy));
		}else{
			viewRootFolder();
		}
	}
	public class MyHandler extends Handler{
		public final static int UPDATE_VIEW = 0;
		public final static int DISMISS_DIALOG = UPDATE_VIEW + 1;

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case UPDATE_VIEW : {
				viewRootFolder();
				break;
			}
			case DISMISS_DIALOG : {
				if(mDialog != null){
					Log.e("lilong","will dismiss dialog");
					mDialog.dismiss();
					mDialog = null;
				}
				viewRootFolder();
				playBackgroundMusic();
				break;
			}
			}
		}
	}
	
	private void viewRootFolder(){
		_files = SFPApplication.mRootFolder;
		if(_files != null && _files.size() >= Utils._folderNum){
			updateGridView(_files);
		}else{
			viewFiles(Utils._rootPath);
		}
	}
	
	private void viewFiles(String filePath) {
		_currentPath = filePath;
		
		ArrayList<FileInfo> newFolderData = null;
		if(Utils._rootPath.equals(_currentPath)){
			newFolderData = FileUtil.getFiles(filePath,true);
		}else{
			newFolderData = FileUtil.getFiles(filePath,false);
		}
		updateGridView(newFolderData);
	}
	
	private void updateGridView(ArrayList<FileInfo> newFolderData){
		if (newFolderData != null) {
			_files = newFolderData;
			// 绑定数据
			mAdapter = new GridViewAdapter(this,mGridView.getId(),_files);
			if(currentOrientation == Configuration.ORIENTATION_UNDEFINED
					|| currentOrientation == Configuration.ORIENTATION_LANDSCAPE){
				mGridView.setNumColumns(6);
				mGridView.setVerticalSpacing(35);
				mGridView.setColumnWidth(100);
			}else if(currentOrientation == Configuration.ORIENTATION_SQUARE
					|| currentOrientation == Configuration.ORIENTATION_PORTRAIT){
				mGridView.setNumColumns(4);
				mGridView.setVerticalSpacing(20);
				mGridView.setColumnWidth(100);
			}
			
			mGridView.setAdapter(mAdapter);
			_fileContainer.removeAllViews();
			_fileContainer.addView(mGridView);
		}
		
	}

	/** 打开文件 **/
	private void openFile(String path) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);

		File file = new File(path);
		String type = FileUtil.getMimeTypeOfFile(file.getName());
		intent.setDataAndType(Uri.fromFile(file), type);
		startActivity(intent);
	}
	
	
	
	@SuppressWarnings("static-access")
	@Override
	protected void onStop() {
		super.onStop();
		Log.d("lilong","---- onStop()");
		ComponentName currentCN = am.getRunningTasks(1).get(0).topActivity;
		if(!currentCN.getPackageName().equals(getPackageName())){
			if(mBackgroundPlayer.mPlayerThread != null){
				if(FolderBrowser.mBackgroundPlayer.mPlayerThread.isPlaying()){
					FolderBrowser.mBackgroundPlayer.mPlayerThread.pausePlayMusic();
				}
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mHandler != null){
			mHandler = null;
		}
		
		if(mDialog != null){
			mDialog.dismiss();
			mDialog = null;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			if(mBackgroundPlayer.mPlayerThread != null){
//				mBackgroundPlayer.mPlayerThread.pausePlayMusic();
//			}
			
			toLauncher();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
    private void toLauncher(){
    	ComponentName cn = new ComponentName("com.android.launcher","com.android.launcher2.Launcher");
		Intent intent = new Intent();
		intent.setComponent(cn);
		intent.setAction("android.intent.action.MAIN");
		startActivity(intent);
    }
}
