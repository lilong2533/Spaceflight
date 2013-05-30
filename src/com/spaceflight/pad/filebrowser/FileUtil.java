package com.spaceflight.pad.filebrowser;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import com.spaceflight.pad.R;
import com.spaceflight.pad.SFPApplication;
import com.spaceflight.pad.object.FileInfo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.webkit.MimeTypeMap;

public class FileUtil {
	private static final String TAG ="lilong";
	public static HashMap<String,File> DirSampleCache = new HashMap<String,File>();
	
	private static final FileFilter sffHidden = new FileFilter(){
		public boolean accept(File pathname) {
			if (pathname.isHidden())
				return false;
			else
				return true;
		}};
	
	public static ArrayList<FileInfo> getFiles(String path,boolean sfp) {
		File f = null;
		File[] files = null;
		try { // 读取文件
			f = new File(path);
			files = f.listFiles();
			if (files == null) { 
				return null;
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}

		ArrayList<FileInfo> fileList = new ArrayList<FileInfo>();
		// 获取文件列表
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if(!"Thumbs.db".equals(file.getName())){
				FileInfo fileInfo = new FileInfo(file);
				fileList.add(fileInfo);
			}
		}

		// 排序
		if(sfp){
			Collections.sort(fileList, SFPSortOrder.getInstance());
		}else{
			Collections.sort(fileList, new DefaultSortOrder());
		}
		return fileList;
	}
	
	public static Drawable getDefaultIconForUserDir(File file,Context mContext){
		Context context = null;
		if(mContext == null){
			context = SFPApplication.mContext;
		}else{
			context = mContext;
		}
		File example = getSampleFileFromDir(context, file);
		Drawable icon = null;

		if (null == example) {
			icon = context.getResources().getDrawable(R.drawable.icon_folder_empty);
		} else {
			Log.i(TAG, "the sample file is " + example.getAbsolutePath());

			if (example.isDirectory())
				icon = context.getResources().getDrawable(R.drawable.icon_folder_folder);
			else{
				String mimetype = getMimeTypeOfFile(example);

				if (null == mimetype) {
					icon = context.getResources().getDrawable(R.drawable.icon_folder_unknown);
				} else {
					if (mimetype.startsWith("image"))
						icon = context.getResources().getDrawable(R.drawable.icon_folder_picture);
					else if (mimetype.startsWith("video"))
						icon = context.getResources().getDrawable(R.drawable.icon_folder_video);
					else if (mimetype.startsWith("audio"))
						icon = context.getResources().getDrawable(R.drawable.icon_folder_music);
					else if (mimetype.equals("application/java-archive") || mimetype.equals("text/vnd.sun.j2me.app-descriptor"))
						icon = context.getResources().getDrawable(R.drawable.icon_folder_package);
					else if (mimetype.equals("text/html")|| mimetype.equals("text/php"))
						icon = context.getResources().getDrawable(R.drawable.icon_folder_web);
					else if (mimetype.equals("text/plain") || mimetype.equals("text/csv")
							|| mimetype.equals("text/xml"))
						icon = context.getResources().getDrawable(R.drawable.icon_folder_text);
					else if (mimetype.startsWith("contacts"))
						icon = context.getResources().getDrawable(R.drawable.icon_folder_contact);
					else if (mimetype.equals("application/vnd.android.package-archive"))
						icon = context.getResources().getDrawable(R.drawable.icon_folder_andrid);
					else if (mimetype.equals("application/msword"))
						icon = context.getResources().getDrawable(R.drawable.icon_folder_word);
					else if (mimetype.equals("application/mspowerpoint"))
						icon = context.getResources().getDrawable(R.drawable.icon_folder_ppt);
					else if (mimetype.equals("application/msexcel"))
						icon = context.getResources().getDrawable(R.drawable.icon_folder_excel);
					else if (mimetype.equals("application/pdf")
							|| mimetype.equals("application/epub+zip"))
						icon = context.getResources().getDrawable(R.drawable.icon_folder_ebook);
					else if (mimetype.equals("compressor/zip")
							|| mimetype.equals("application/gzip"))
						icon = context.getResources().getDrawable(R.drawable.icon_folder_compress);
					else if (mimetype.equals("ics/calendar"))
						icon = context.getResources().getDrawable(R.drawable.icon_folder_calendar);
					else
						icon = context.getResources().getDrawable(R.drawable.icon_folder_unknown);
				}
			}
		}

		return icon;
	}
	
	public static File getSampleFileFromDir(Context context, File dir){
		if (null == dir || !dir.exists() || !dir.isDirectory() || null == context){
			Log.e(TAG, "invalid parameter in getSampleFileFromDir");
			return null;
		}

		File sample = null;

		// get from cache
		Object cache = DirSampleCache.get(dir.getPath());
		if (null != cache){
			sample = (File)cache;
			if (sample.exists())
				return sample;
			else
				sample = null;
		}

		// get from list
		File[] files = dir.listFiles(sffHidden);
		if (null == files || 0 == files.length){
			sample = null;
		}else {
			if (2 >= files.length){
				sample = files[0];
			}else{
				File st = files[0];
				File en = files[files.length - 1];
				File md = files[files.length / 2];

				// image at first
				if (null == sample){
					if (isImageFile(context, st))
						sample = st;
					else if (isImageFile(context, md))
						sample = md;
					else if (isImageFile(context, en))
						sample = en;

					if (null != sample)
						Log.i(TAG, "use image file as sample : " + sample.getAbsolutePath());
				}

				// same mimetype secondly
				if (null == sample){
					String mmtSt = getMimeTypeOfFile(st);
					String mmtMd = getMimeTypeOfFile(md);
					String mmtEn = getMimeTypeOfFile(en);

					if ((null == mmtSt && null == mmtMd) ||
							(null != mmtSt && null != mmtMd && mmtSt.equals(mmtMd)))
						sample = st;
					else if ((null == mmtEn && null == mmtMd) ||
							(null != mmtEn && null != mmtMd && mmtMd.equals(mmtEn)))
						sample = md;
					else if ((null == mmtEn && null == mmtSt) ||
							(null != mmtEn && null != mmtSt && mmtEn.equals(mmtSt)))
						sample = st;

					if (null != sample)
						Log.i(TAG, "use same mimetype as sample : " + sample.getAbsolutePath());
				}

				// return the first file at last, if we can not get sample file
				// by above two condition
				if (null == sample)
					sample = st;
			}
		}

		if (null != sample){
			DirSampleCache.put(dir.getPath(), sample);
		}

		return sample;
	}
	
	public static String getMimeTypeOfFile(File file) {
		if (null == file || !file.exists()) {
			Log.e(TAG, "invalid file in getMimeTypeOfFile");
			return null;
		}else{
			if (file.exists() && file.isDirectory()){
				Log.i(TAG, "can not get mimetype for a folder");
				return null;
			}else
				return FileUtil.getMimeTypeOfFile(file.getName());
		}
	}
	
	public static boolean isImageFile (Context context, File file) {
		if ( null == file || !file.isFile())
			return false;
		else {
			String mimetype = getMimeTypeOfFile(file);

			if (null == mimetype)
				return false;
			else {
				if (mimetype.startsWith("image"))
					return true;
				else
					return false;
			}
		}
	}
	
	public static String getMimeTypeOfFile(String name){
		if (null == name || 0 == name.trim().length()){
			Log.i("FileInfo", "invalid name in getMimeTypeOfFile");
			return null;
		}else{
			 String lowerCase = name.toLowerCase();

	         String extension = null;
			 int dot = lowerCase.lastIndexOf(".");
	         if (dot >= 0)
	        	 extension = lowerCase.substring(dot + 1);

	         String mimetype = null;
	         if (extension != null)
	        	 mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

	         return mimetype;
		}
	}
}
