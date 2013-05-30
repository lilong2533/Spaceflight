package com.spaceflight.pad.object;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.AbsListView.LayoutParams;
import android.widget.ImageView.ScaleType;

public class SFPImageAdapter extends BaseAdapter {
    private Context mContext;
    private Bitmap srcBitmap;
    
    private int currentImageSwitch;
    private int currentOrientation;
    private MediaMetadataRetriever retriever;
    
    private ArrayList<FileInfo> currentList;
    private ArrayList<FileInfo> mPortraitList;
    private ArrayList<FileInfo> mLandScapeList;
    private HashMap<Integer,Bitmap> bitmapCache;
    
	public SFPImageAdapter(Context context,int orientation,int imageSwitch) {
		mContext = context;
		currentOrientation = orientation;
		currentImageSwitch = imageSwitch;
		
		currentList = new ArrayList<FileInfo>();
		mLandScapeList = new ArrayList<FileInfo>();
		mPortraitList = new ArrayList<FileInfo>();
		bitmapCache = new HashMap<Integer,Bitmap>();
	}
	
	public void updateAdapter(ArrayList<FileInfo> mCurrent,ArrayList<FileInfo> mLand,ArrayList<FileInfo> mPortrait) {
		if(mCurrent != null){
			currentList.clear();
			currentList.addAll(mCurrent);
		}
		if(mLand != null){
			mLandScapeList.clear();
			mLandScapeList.addAll(mLand);
		}
		if(mPortrait != null){
			mPortraitList.clear();
			mPortraitList.addAll(mPortrait);
		}
		
		notifyDataSetChanged();
	}
	
	public void setOrientation(int orientation,int imageSwitch){
		currentOrientation = orientation;
		currentImageSwitch = imageSwitch;
		if(currentImageSwitch > 0){
			if(currentOrientation == Configuration.ORIENTATION_UNDEFINED
					|| currentOrientation == Configuration.ORIENTATION_LANDSCAPE){
				currentList = mLandScapeList;
			}else if(currentOrientation == Configuration.ORIENTATION_SQUARE
					|| currentOrientation == Configuration.ORIENTATION_PORTRAIT){
				currentList = mPortraitList;
			}
		}
		for(int position : bitmapCache.keySet()){
			bitmapCache.put(position,createBitmapByMIME(currentList.get(position)));
		}
	}
	
	@Override
	public int getCount() {
		return currentList.size();
	}

	@Override
	public Object getItem(int position) {
		return currentList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    SFPImageView imageView = new SFPImageView(mContext);
		LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT); 
		imageView.setLayoutParams(params);
		imageView.setScaleType(ScaleType.FIT_XY);
		
		if(currentImageSwitch > 0){
			if(currentOrientation == Configuration.ORIENTATION_UNDEFINED
					|| currentOrientation == Configuration.ORIENTATION_LANDSCAPE){
				currentList = mLandScapeList;
			}else if(currentOrientation == Configuration.ORIENTATION_SQUARE
					|| currentOrientation == Configuration.ORIENTATION_PORTRAIT){
				currentList = mPortraitList;
			}
		}
		
		if(currentList != null && position >= 0 && position < currentList.size()){
			srcBitmap = getImageSrc(position);
			imageView.setImageBitmap(srcBitmap);
		}
		
		return imageView;
	}
	
	private Bitmap getImageSrc(int position){
		Log.i("lilong", "getImageSrc("+position+")");
		Bitmap ret = bitmapCache.get(position);
		if(ret != null){
			return ret;
		}else{
			int cache1 = -1;
			int cache2 = -1;
			if(currentList.size() >= 3){
				if(position == 0){
					cache1 = position + 1;
					cache2 = position + 2;
				}else if(position == currentList.size()-1){
					cache1 = position - 1;
					cache2 = position - 2;
				}else{
					cache1 = position - 1;
					cache2 = position + 1;
				}
			}else if(currentList.size() == 2){
				cache1 = position + 1;
			}
			
			clearBitmapCache(position);
			
			if(cache1 >= 0){
				Log.i("lilong", "getImageSrc( add "+cache1+")");
				bitmapCache.put(cache1,createBitmapByMIME(currentList.get(cache1)));
			}
			
			if(position >= 0){
				Log.i("lilong", "getImageSrc( add "+position+")");
				ret = createBitmapByMIME(currentList.get(position));
				bitmapCache.put(position,ret);
			}
			
			if(cache2 >= 0){
				Log.i("lilong", "getImageSrc( add "+cache2+")");
				bitmapCache.put(cache2,createBitmapByMIME(currentList.get(cache2)));
			}
		}
		
		if(ret == null){
			ret = createBitmapByMIME(currentList.get(position));
		}
	
		return ret;
	}
	
	private Bitmap createBitmapByMIME(FileInfo file){
		Bitmap srcBitmap = null;
		if(file != null && !file.IsDirectory && file.MIMEType != null){
			try{
				if(file.MIMEType.startsWith(FileInfo.MIME_IMAGE)){
					srcBitmap = BitmapFactory.decodeFile(file.Path);
				}else if(file.MIMEType.startsWith(FileInfo.MIME_VIDEO)){
					retriever = new MediaMetadataRetriever();
					try {
						retriever.setDataSource(file.Path);
						srcBitmap = retriever.getFrameAtTime();
					}catch(Exception e) {
						e.printStackTrace();
					}finally{
					    try{
							retriever.release();
						}catch(RuntimeException e){
							e.printStackTrace();
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return srcBitmap;
	}
	
	private void clearBitmapCache(int position){
		int space = 4;
		if((position <= 0) || (position >= currentList.size()-1)){
			space = 4;
		}else{
			space = 3;
		}
//		Log.e("lilong", "clearBitmapCache("+position+" : "+space+")");
		ArrayList<Integer> recycledList = new ArrayList<Integer>();
		for(int index : bitmapCache.keySet()){
			if(Math.abs(position - index) >= space){
				recycledList.add(index);
			}
		}
		
		for(int clearIndex : recycledList){
			Log.e("lilong", "clearIndex = "+clearIndex);
			Bitmap tmp = bitmapCache.remove(clearIndex);
			if (tmp != null && !tmp.isRecycled()) {
				tmp.recycle();	
				tmp = null;
		    }
		}
	}
	
	public void clear(){
		if (srcBitmap != null && !srcBitmap.isRecycled()) {
			srcBitmap.recycle();	
			srcBitmap = null;
	    }
	}
}
