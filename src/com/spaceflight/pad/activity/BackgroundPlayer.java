package com.spaceflight.pad.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import android.content.Context;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.util.Log;

public class BackgroundPlayer {
	private Context mContext;
	private MediaPlayer myMediaPlayer;
	private String _rootMusic;
	private final static String _musicName = "sfp_background_music.mp3";
	private final static String _sdcardMusic = "/sdcard/"+_musicName;
	
	private File sdcardMusic;
	private File rootMusic;
	
	public static InitBackgroundMusic mPlayerThread;
    private static BackgroundPlayer mBackgroundPlayer;
	
	private BackgroundPlayer(final Context mContext) {
		this.mContext = mContext;
		_rootMusic = mContext.getFilesDir().getAbsolutePath() + File.separator + _musicName;
		sdcardMusic = new File(_sdcardMusic);
		rootMusic = new File(_rootMusic);
		
		myMediaPlayer = new MediaPlayer();
		myMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				if(rootMusic != null && mPlayerThread != null){
					mPlayerThread.playMusic(rootMusic);
				}
			}
		});
		
		mPlayerThread = new InitBackgroundMusic();
		mPlayerThread.execute(null,null);
	}
	
	public static BackgroundPlayer getSington(Context mContext){
		if(mBackgroundPlayer == null){
			mBackgroundPlayer = new BackgroundPlayer(mContext);
		}
		
		return mBackgroundPlayer;
	}
	
	public class InitBackgroundMusic extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... parm) {
			Log.d("lilong","PlayerThread()");
			if(sdcardMusic.exists()){
				try {
					if(rootMusic.exists()){
						rootMusic.delete();	
					}
					
					Log.d("lilong","PlayerThread : copy from '"+sdcardMusic+"' to '"+rootMusic+"'");
					FileInputStream fin = new FileInputStream(sdcardMusic);
					FileOutputStream fout = new FileOutputStream(rootMusic);
					if(fin != null && fout != null){
						int count;
						byte[] buf = new byte[1024 * 5];
						while ((count = fin.read(buf)) != -1) {
							fout.write(buf, 0, count);
						}
						fin.close();
						fout.flush();
						fout.close();
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if(!rootMusic.exists()){
				try{
					AssetManager asset_manager = mContext.getAssets();
					InputStream fin = asset_manager.open(_musicName);
					FileOutputStream fout = new FileOutputStream(rootMusic);
					
					if(fin != null && fout != null){
						int count;
						byte[] buf = new byte[1024 * 5];
						while ((count = fin.read(buf)) != -1) {
							fout.write(buf, 0, count);
						}
						fin.close();
						fout.flush();
						fout.close();
					}
					
					Runtime.getRuntime().exec("chmod -R 777 "+rootMusic.getPath());
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
			playMusic(rootMusic);
			return null;
		}
		
		private void playMusic(File file ) {
			try {
				FileInputStream fis = new FileInputStream(file);
				myMediaPlayer.reset();
				myMediaPlayer.setDataSource(fis.getFD());
				myMediaPlayer.prepare();
				myMediaPlayer.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void pausePlayMusic(){
			if(myMediaPlayer != null){
				if(myMediaPlayer.isPlaying()){
					myMediaPlayer.pause();
				}else{
					myMediaPlayer.start();
				}
			}
		}
		
		public boolean isPlaying(){
			if(myMediaPlayer != null){
				return myMediaPlayer.isPlaying();
			}
			return false;
		}
	}
}
