package com.spaceflight.pad.scanfiles;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

import com.spaceflight.pad.SFPApplication;
import com.spaceflight.pad.Utils;
import com.spaceflight.pad.activity.FolderBrowser;
import com.spaceflight.pad.filebrowser.FileUtil;
import com.spaceflight.pad.object.FileInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class ScanFilesService extends Service {
	private static final String TAG = "ScanFilesService";
	private FileInitThread mFileInitThread;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Utils.mAlreadyStart = true;
		mFileInitThread = new FileInitThread(this);
		mFileInitThread.start();
	}

	class FileInitThread extends Thread {
		private Context mContext;
		private AssetManager asset_manager;
		private File rootPath;
		private InputStream fin;
		private File dataFile;
		private ReadableByteChannel readableChannel;
		private FileChannel outChannel;
		protected PowerManager.WakeLock mWakeLock;
		
		public FileInitThread(Context mContext){
			this.mContext = mContext;
			asset_manager = mContext.getAssets();
			threadInt();
		}
		
		private void threadInt() {
            PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
                            | PowerManager.ON_AFTER_RELEASE, TAG);
        }
		
		 @Override
        public void finalize() {
            if (mWakeLock != null && mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }

		@SuppressWarnings("static-access")
		@Override
		public void run() {
			mWakeLock.acquire();
			// 判断_rootPath是否需要更新
			File sdcardPath = new File(Utils._sdcardPath);
			rootPath = new File(Utils._rootPath);
			if (sdcardPath.exists()) {
				if (rootPath.exists()) {
					deleteDir(new File(Utils._rootPath));
				}

				copyFolder(Utils._sdcardPath, Utils._rootPath);

				if (FolderBrowser.mHandler != null) {
					FolderBrowser.mHandler
							.sendEmptyMessage(FolderBrowser.mHandler.DISMISS_DIALOG);
				}

				initDataFinish(rootPath,true);
				deleteDir(new File(Utils._sdcardPath));
				return;
			}else if(!rootPath.exists()){
				try{
					InputStream tmpIn = asset_manager.open("file.list");
					InputStreamReader tmpInR = new InputStreamReader(tmpIn);
					BufferedReader br = new BufferedReader(tmpInR);
				    String line = "";
				    while((line = br.readLine()) != null){
				    	Log.d("lilong","file = "+line);
				    	copyFromAssets(line);
				    }
				    br.close();
				    tmpInR.close();
				    tmpIn.close();
				}catch(Exception e){
					e.printStackTrace();
				}
				initDataFinish(rootPath,true);
				return;
			}
			initDataFinish(rootPath,false);
		}

		@SuppressWarnings("static-access")
		private void initDataFinish(File rootPath ,boolean dismissDialog) {
			try {
				Runtime.getRuntime().exec("chmod -R 777 " + Utils._rootPath);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (rootPath.exists()) {
				SFPApplication.mRootFolder = FileUtil.getFiles(Utils._rootPath,true);

				if (FolderBrowser.mHandler != null) {
					if(dismissDialog){
						FolderBrowser.mHandler.sendEmptyMessage(FolderBrowser.mHandler.DISMISS_DIALOG);
					}else{
						FolderBrowser.mHandler.sendEmptyMessage(FolderBrowser.mHandler.UPDATE_VIEW);
					}
				}

				for (FileInfo file : SFPApplication.mRootFolder) {
					if (file.IsDirectory) {
						ArrayList<FileInfo> files = file.mChildFiles;
						SFPApplication.mFolderChild.put(file.Name, files);
					}
				}
			}
		}
		
		private void copyFromAssets(String fileName){
			if(asset_manager == null){
				asset_manager = getAssets();
			}
			
			dataFile = new File("/data/data/"+getPackageName()+"/"+fileName);
			if(!dataFile.getParentFile().exists()){
				dataFile.getParentFile().mkdirs();
			}
			
			try {
				fin = asset_manager.open(fileName);
				
				readableChannel = Channels.newChannel(fin);
				outChannel = new FileOutputStream(dataFile,true).getChannel();  
	            int nOffset = 0;
	            int size = fin.available();

                while (nOffset < size) {
                	Thread.currentThread().join(400);
                    if (nOffset + Utils.COPY_BUFFER_MAXSIZE < size) {
                    	outChannel.transferFrom(readableChannel, nOffset,
                    			Utils.COPY_BUFFER_MAXSIZE);
                        nOffset += Utils.COPY_BUFFER_MAXSIZE;
                    }else {
                    	outChannel.transferFrom(readableChannel, nOffset,
                    			size - nOffset);
                        break;
                    }
                }
				
				readableChannel.close();  
				outChannel.close();  
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private boolean copyFolder(String src, String des) {
			File in = new File(src);
			File out = new File(des);
			if (!in.exists()) {
				return false;
			}

			if (!out.exists()) {
				out.mkdirs();
			}
			File[] files = in.listFiles();
			for (int i = 0; i < files.length; i++) {
				File srcFile = files[i];
				Log.d("lilong", "copyFolder(src = "+srcFile.getName()+")");
				if (srcFile.isFile() && !"Thumbs.db".equals(srcFile.getName())) {
					File desFile = new File(des + "/" + files[i].getName());
					try {
						fin = new FileInputStream(srcFile);
						readableChannel = Channels.newChannel(fin);
						outChannel = new FileOutputStream(desFile, true).getChannel();
						
						int nOffset = 0;
                        int size = fin.available();
						
						while (nOffset < size) {
							Thread.currentThread().join(400);
							if (nOffset + Utils.COPY_BUFFER_MAXSIZE < size) {
								outChannel.transferFrom(readableChannel,nOffset, Utils.COPY_BUFFER_MAXSIZE);
								nOffset += Utils.COPY_BUFFER_MAXSIZE;
							} else {
								outChannel.transferFrom(readableChannel,
										nOffset, size - nOffset);
								break;
							}
						}
						readableChannel.close();
						outChannel.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else if(srcFile.isDirectory()){
					copyFolder(src + "/" + files[i].getName(), des + "/"
							+ files[i].getName());
				}
			}

			return true;
		}

		private boolean deleteDir(File dir) {
			if (dir.isDirectory()) {
				String[] children = dir.list();
				for (int i = 0; i < children.length; i++) {
					boolean success = deleteDir(new File(dir, children[i]));
					if (!success) {
						return false;
					}
				}
			}
			return dir.delete();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Utils.mAlreadyStart = false;
	}
}
