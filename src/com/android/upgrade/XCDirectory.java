package com.android.upgrade;

import android.content.Context;
import android.util.Log;

public class XCDirectory {
	public static boolean isSdcard;
	public final static int maxSize = 30 * 1024 * 1024;
	public final static int unrefineSize = 60 * 1024 * 1024;	//不细化时,存储文件的大小---暂定
	public static String SDCARDPATH;
	public static String LAUNCHER_DATA = "/data";
	
	public static void initDirectory(Context context){
		SDCARDPATH = android.os.Environment.getExternalStorageDirectory().getPath();
//		LAUNCHER_DATA = context.getApplicationContext().getFilesDir().getAbsolutePath();
		Log.d("TAG",LAUNCHER_DATA);
	}
}