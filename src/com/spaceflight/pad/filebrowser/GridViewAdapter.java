package com.spaceflight.pad.filebrowser;

import java.util.ArrayList;

import com.spaceflight.pad.R;
import com.spaceflight.pad.SFPApplication;
import com.spaceflight.pad.object.FileInfo;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class GridViewAdapter extends ArrayAdapter<FileInfo> {
	private Context mContext;
	private ArrayList<FileInfo> mDataList;
	
	public int from = -1;
	public int to = -1;
	public int aniFrom = -1;
	public int aniTo = -1;
	public boolean doingAni = false;
	
	public GridViewAdapter(Context mContext, int viewID,ArrayList<FileInfo> mArray){
		super(mContext, viewID,mArray);
		this.mContext = mContext;
		mDataList = mArray;
	}
	
	public ArrayList<FileInfo> getList() {
		return mDataList;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView = null;
		if (convertView == null) {
			convertView = new ImageView(mContext);
		}
		
		imageView = (ImageView) convertView;
		LayoutParams params = new LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT,AbsListView.LayoutParams.WRAP_CONTENT); 
		params.width = 180;  
        params.height = 180;  
		imageView.setLayoutParams(params);
		FileInfo mFileInfo = mDataList.get(position);
		
		if(mFileInfo.IsDirectory){
			if(SFPApplication.SFPFiles.containsKey(mFileInfo.Name)){
				Integer resID = SFPApplication.SFPFiles.get(mFileInfo.Name);
				imageView.setImageResource(resID);
			}
		}else{
			imageView.setImageResource(R.drawable.doc);
		}
		
		// 目的：把from位置上的图隐藏 ，其它的显示
		if (position == from) {
			imageView.setVisibility(View.INVISIBLE);
		} else {
			imageView.setVisibility(View.VISIBLE);
		}
		
		// 给View添加animation
		Animation an = null;
		if (position > aniTo && position <= aniFrom) {
			if (position % 5 == 0) {
				//设置位移动画的参数
				an = new TranslateAnimation(255, 0, -85, 0);
			} else {
				//设置位移动画的参数
				an = new TranslateAnimation(-60, 0, 0, 0);
			}
		} else if (position < aniTo && position >= aniFrom) {
			if (position % 5 == 4) {
				//设置位移动画的参数
				an = new TranslateAnimation(-255, 0, 85, 0);
			} else {
				//设置位移动画的参数
				an = new TranslateAnimation(60, 0, 0, 0);
			}
		}

		if (an != null) {
			an.setDuration(300);
			// 做动画的时候不能做位置交换
			an.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					doingAni = true;
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					doingAni = false;
				}
			});
			imageView.setAnimation(an);
		}
		return imageView;
	}
}
