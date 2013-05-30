package com.spaceflight.pad.object;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import com.spaceflight.pad.filebrowser.DefaultSortOrder;
import com.spaceflight.pad.filebrowser.FileUtil;

public class FileInfo{
	public static final String MIME_VIDEO = "video";
	public static final String MIME_IMAGE = "image";
	
	public String Name;
	public String Path;
	public long Size;
	public boolean IsDirectory;
	public String MIMEType = "unknow";
	public byte[] bitmapBytes;
	public ArrayList<FileInfo> mChildFiles;
	
	public FileInfo(File file){
	    this.Name = file.getName();
	    this.Path = file.getPath();
	    this.Size = file.length();
	    this.IsDirectory = file.isDirectory();
	    if(IsDirectory){
	    	mChildFiles = new ArrayList<FileInfo>();
			new ScanChildFiles(Path).start();
		}else{
			MIMEType = FileUtil.getMimeTypeOfFile(Name);
		}
	}
	
	class ScanChildFiles extends Thread{
		private String path;
		
		public ScanChildFiles(String path){
			this.path = path;
		}

		@Override
		public void run() {
			try {
				File root_file = new File(path);
				File[] files = root_file.listFiles();
				if (files == null) { 
					return;
				}
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					if(!"Thumbs.db".equals(file.getName())){
						FileInfo fileInfo = new FileInfo(file);
						mChildFiles.add(fileInfo);
					}
				}

				Collections.sort(mChildFiles, new DefaultSortOrder());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
}