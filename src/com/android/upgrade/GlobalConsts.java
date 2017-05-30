package com.android.upgrade;

import java.util.ArrayList;

import com.android.push.NetworkStatus;
import com.android.push.WifiUtils.WifiState;


public class GlobalConsts {
	/*rom弹出框是否已经弹出*/
	public static boolean ROMISSHOW = false;
	/*rom升级弹出框*/
	public static RomInstallDialog romInstallDialog = null;
	public static String MD5_FILE_NAME = "MD5";
	public static String ROM_SAVE_NAME = "Update.zip";
	public static String PRODUCT_NAME= "";
	public static String ROM_VERSION = "";
	public static String ROM_NAME = "";
	public static String ROM_SN = "";//盒子的SN号,唯一标识
	public static String ROM_TYPE = "";//rom的类型 userdebug或user,只有user版本才会去请求rom升级
	public static boolean ROM_DOWNLOADED = false;
	public static boolean HAS_NEW_VERSION = false;
	public static boolean ROM_DOWNLOADING = false;
	public static String DECS = "";
	public static boolean FORCED = false;
	public static int battery = -1;
	//============================
	//网络相关
	//==============================
	public static int connectType = -1;//1 有线已连接 2 无线已连接 3 检测到无线网 4 检测不到可用的无线网
	public static boolean connectFirstShow = true;// 首次网络弹框
	public static boolean networkFlag = true;
	public static long networkChangeTime;//网络在有线网与无线网切换成功的当前时间
	public static WifiState wifiState;
	public static int currentFlag = 1;// 当前网络标志 1当前网络(默认) 2 正在连接网络
	public static int wifiFlag;// Wifi状态： 1 已连接 2 未连接(正在获取) 3 当前连接的网络断开 4
	public static boolean screenOffFlag;
	// 正在连接的出现连接失败
	public static ArrayList<NetworkStatus> netWorkList = new ArrayList<NetworkStatus>();
	public static String mac = "11:01:00:00:00:01";
	
}
