package com.android.launcher3;

import java.util.ArrayList;
import java.util.List;

public class FirstScreenInfo {
	
	public static List<String> desktopItems = new ArrayList<String>();
	public static List<String> folderItems = new ArrayList<String>();
	public static List<String> shortcutItems = new ArrayList<String>();
	public static String folderName;
	public static List<String> hidePkgs = new ArrayList<String>();
	// TODO 快捷栏未满情况下,候补的填空缺信息
	public static List<String> shortcutOtherItems = new ArrayList<String>();
//	public static List<String> multipleEntryPkgs = new ArrayList<String>();
	static {
//		desktopItems.add(LauncherApplication.appPsPkgname);
//		desktopItems.add("app.android.applicationxc");
//		desktopItems.add("com.gitvvideo");
		
		folderItems.add("com.starcor.mango");
		folderItems.add("com.tencent.tmgp.rxcq");
		folderItems.add("com.netease.ldxy");
//		folderItems.add("com.kingroot.kinguser");
//		folderItems.add("com.hw.video");
		
		folderName = "super_hw";
		
//		shortcutItems.add("com.android.gallery3d");
//		shortcutItems.add("com.fb.FileBrower");
		// TODO version 1.01
//		shortcutItems.add("com.android.music");
//		shortcutItems.add("com.android.calculator2");
//		shortcutItems.add("com.android.browser");
//		shortcutItems.add("com.android.deskclock");
//		shortcutItems.add("com.android.settings");
//		// TODO version 1.02
//		shortcutItems.add("com.android.calculator2");
// 		shortcutItems.add("com.android.deskclock");
//		shortcutItems.add("com.android.launcher1905");
//		shortcutItems.add("com.android.gallery3d");
// 		shortcutItems.add("com.android.settings");
//		// TODO version 1.03 test
//		shortcutItems.add("com.example.csdnblog4");
//		shortcutItems.add("com.hw.video");
//		shortcutItems.add("com.xiaocong.filemanager");
//		shortcutItems.add("com.android.deskclock");
//		shortcutItems.add("com.android.settings");
//		// TODO version 1.05 for test sdcard
//		shortcutItems.add("com.qihoo.appstore");
//		shortcutItems.add("net.myvst.v2");
//		shortcutItems.add("com.sohutv.tv");
//		shortcutItems.add("com.android.deskclock");
//		shortcutItems.add("com.android.settings");
		// TODO version 1.06 (QQ音乐,UC浏览器,观影神器,品胜购物商成,设置)
		shortcutItems.add("com.tencent.qqmusic");
		shortcutItems.add("com.UCMobile");
		shortcutItems.add("com.android.launcher1905");
		shortcutItems.add("com.pisen.shop");
		shortcutItems.add("com.android.settings");
		// hide icon
		hidePkgs.add("com.android.launcher3");
		hidePkgs.add("com.dzt.zxingdemo");
		hidePkgs.add("com.android.inputmethod.latin");
		
//		// multiple entry
//		multipleEntryPkgs.add("com.android.gallery3d");
	}
	
}
