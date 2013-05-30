package com.spaceflight.pad;

import java.io.File;

public class Utils {
	public static String _dataFlashPath = "/data/data/com.spaceflight.pad";
	public static String _defaultWallpaper = "/sdcard/sfp_wallpaper.jpg";
	public static String _rootPath = _dataFlashPath+"/HKPAD";
	public static String _sdcardPath = "/sdcard/HKPAD";
	public static String _landPic = "LandScape";
	public static String _portPic = "Portrait";
	
	public static boolean mAlreadyStart = false;
	public static int _folderNum = 16;
	
	public static File _firstTime = new File(_dataFlashPath+"/firstTime");
	public static final int COPY_BUFFER_MAXSIZE = 0x40000;
	
	public final static String click_01_intent = "com.spaceflight.pad.widget.click01";
	public final static String click_02_intent = "com.spaceflight.pad.widget.click02";
	public final static String click_03_intent = "com.spaceflight.pad.widget.click03";
	public final static String click_04_intent = "com.spaceflight.pad.widget.click04";
	public final static String click_05_intent = "com.spaceflight.pad.widget.click05";
	public final static String click_06_intent = "com.spaceflight.pad.widget.click06";
	public final static String click_07_intent = "com.spaceflight.pad.widget.click07";
	public final static String click_08_intent = "com.spaceflight.pad.widget.click08";
	public final static String click_09_intent = "com.spaceflight.pad.widget.click09";
	public final static String click_10_intent = "com.spaceflight.pad.widget.click10";
	public final static String click_11_intent = "com.spaceflight.pad.widget.click11";
	public final static String click_12_intent = "com.spaceflight.pad.widget.click12";
	public final static String click_13_intent = "com.spaceflight.pad.widget.click13";
	public final static String click_14_intent = "com.spaceflight.pad.widget.click14";
	public final static String click_15_intent = "com.spaceflight.pad.widget.click15";
	public final static String click_16_intent = "com.spaceflight.pad.widget.click16";
}
