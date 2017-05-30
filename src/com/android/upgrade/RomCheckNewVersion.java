package com.android.upgrade;

import java.io.File;

import org.json.JSONObject;

import com.android.launcher3.R;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class RomCheckNewVersion {
	private Context context;
	private boolean countFinished = false;
	private boolean inited = false;
	private RomUpgradeInfo romUpgradeInfo;
	private boolean needUpdate = false;
	public RomCount romCount;
	private String romVersionName = "";
	public LogcatHelper logCatHelper;
	public boolean checkRomAgain = false;
	public boolean checkFirst = true;
	public RomCheckNewVersion(Context context){
		this.context = context;
		romUpgradeInfo = new RomUpgradeInfo();
		romCount = new RomCount();
//		romUpgradeInfo.init(context,GlobalConsts.ROM_NAME, GlobalConsts.ROM_VERSION, GlobalConsts.ROM_SN,handler);
		//2016-1-12 yf 更新为使用MAC地址请求ROM信息
		romUpgradeInfo.init(context,GlobalConsts.ROM_NAME, GlobalConsts.ROM_VERSION, GlobalConsts.mac,handler);
//		romUpgradeInfo.init(context, "S812","0.0.0", GlobalConsts.ROM_SN,handler);
		new RomUpgradeTimer(30 * 1000, 1000).start();
		logCatHelper =new LogcatHelper(context,"upgrdeRom");
        logCatHelper.start();
	}
	
	class RomUpgradeTimer extends CountDownTimer {
		public RomUpgradeTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onTick(long millisUntilFinished) {
		}

		@Override
		public void onFinish() {
			countFinished = true;
			Log.e("upgrdeRom", " 启动rom升级轮询");
			new MyThread().start();
			if (GlobalConsts.ROM_TYPE.equals("user") && !inited&&GlobalConsts.networkFlag ) {
//			if (!inited && GlobalConsts.networkFlag) {
//			if (!inited) {
//				inited = true;
				romUpgradeInfo.startLoad();
			}
		}
	}

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case 500:
                Log.e("upgrdeRom", "  下载MD5文件");
                inited = true;
                GlobalConsts.HAS_NEW_VERSION = true;
                RomUpgradeData data = (RomUpgradeData) msg.obj;
                needUpdate = data.upgrade;
                romVersionName = data.version;
                if (currentInfo!=null) {
                    currentInfo.setText("最新版本:"+romVersionName);
//                    updateView.setTag("有新版本");
                    updateView.setVisibility(View.GONE);
                }
                GlobalConsts.FORCED = data.forced;
                GlobalConsts.DECS = data.desc;
                if (down) {
                    DownloadRom downloadMD5 = new DownloadRom(context, XCAPI.BASE_URL_XC + data.downloadUrl + ".md5",
                            XCDirectory.SDCARDPATH + "/", GlobalConsts.MD5_FILE_NAME, data.version, handler, 1, data);
                    downloadMD5.download();
                }
                break;
            case 501:// 没有新版本
                Log.e("upgrdeRom", "  没有新版本");
                inited = true;
                needUpdate = false;
                GlobalConsts.HAS_NEW_VERSION = false;
                if (logCatHelper != null)
                    logCatHelper.stop();
                if (updateView!=null) {
                    Toast.makeText(context, "已是最新版本", Toast.LENGTH_LONG).show();
                    updateView.setText("已是最新版本");
                }
                break;
            case 502:// 请求异常
                Log.e("upgrdeRom", "  请求异常,返回 status !=200 或者请求结果为 null");
                inited = true;
                if (logCatHelper != null)
                    logCatHelper.stop();
                if (updateView!=null) {
                    updateView.setText("请求异常,请稍后重试");
                }
                break;
            case 600:// 升级包下载成功,验证MD5
                if (logCatHelper!=null) {
                    logCatHelper.start();
                }
//                if (updateView!=null) {
//                    updateView.setText("升级包下载成功 ,验证中")
//                }
                Log.e("upgrdeRom", "  验证MD5 ");
                GlobalConsts.ROM_DOWNLOADING = false;
                new Thread() {
                    public void run() {
                        File file = new File(XCDirectory.SDCARDPATH + "/" + GlobalConsts.MD5_FILE_NAME);
                        if (file.exists()) {
                            try {
                                String md5Str = FileUtils.readFile(context, XCDirectory.SDCARDPATH + "/"
                                        + GlobalConsts.MD5_FILE_NAME);
                                JSONObject json = new JSONObject(md5Str);
                                if (json.has("md5")) {
                                    String md5 = json.getString("md5");
                                    // String fileMD5 =
                                    // FileUtils.md5sum("/sdcard/Update.zip");
                                    String fileMD5 = FileUtils.md5sum(XCDirectory.LAUNCHER_DATA + "/"
                                            + GlobalConsts.ROM_SAVE_NAME);
                                    Log.e("upgrdeRom", " 本地文件的 MD5 值 fileMD5 是：" + fileMD5);
                                    Log.e("upgrdeRom", " 返回信息中的 值 md5 是：" + md5);
                                    if (fileMD5 != null && fileMD5.equals(md5)) {
                                        Log.e("upgrdeRom", "  md5 校验成功！");
                                        handler.sendEmptyMessage(602);
                                        GlobalConsts.ROM_DOWNLOADED = true;
                                    } else {
                                        Log.e("upgrdeRom", "  md5 校验失败！");
                                        if (logCatHelper!=null) {
                                            logCatHelper.stop();
                                        }
                                        File updateFIle = new File(XCDirectory.LAUNCHER_DATA + "/"
                                                + GlobalConsts.ROM_SAVE_NAME);
                                        if (updateFIle.exists()) {
                                            updateFIle.delete();
                                        }
                                        if (GlobalConsts.ROM_TYPE.equals("user")) {
                                            romUpgradeInfo.startLoad();
                                        }
                                    }
                                } else {
                                    if (GlobalConsts.ROM_TYPE.equals("user")) {
                                        romUpgradeInfo.startLoad();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            handler.sendEmptyMessage(602);
                            GlobalConsts.ROM_DOWNLOADED = true;
                        }
                    };
                }.start();
                break;
            case 601:// md5 下载成功,开始下载升级包
                Log.e("upgrdeRom", "   md5  下载md5 成功,开始下载升级包");
                if(checkFirst){
                    checkFirst = false;
                    Toast.makeText(context, " 系统有新版本啦!", Toast.LENGTH_LONG).show();
                }
                // 判断剩余空间,在下载的时候处理，这里不判断
                RomUpgradeData romData = (RomUpgradeData) msg.obj;
                DownloadRom downloadRom = new DownloadRom(context, XCAPI.BASE_URL_XC + romData.downloadUrl,
                        XCDirectory.LAUNCHER_DATA + "/", GlobalConsts.ROM_SAVE_NAME, romData.version, handler, 0,
                        romData);
                // DownloadRom downloadRom = new
                // DownloadRom(context,XCAPI.BASE_URL_XC + romData.downloadUrl
                // ,"/sdcard/", "Update.zip",romData.version,
                // handler,0,romData);
                downloadRom.download();
                if (logCatHelper!=null) 
                    logCatHelper.stop();
                break;
            case 602:// md5验证成功,提示安装
                Log.e("upgrdeRom", " DownloadRom MD5验证成功,提示安装");
                if (isTopActivity(context)) {
                    //这个值的位置暂时放这里，需要考虑一下
                    checkRomAgain = false;
                    GlobalConsts.HAS_NEW_VERSION = false;
                    needUpdate = false;
                    Log.e("upgrdeRom", romCount.romCancleCount + "");
                    if (GlobalConsts.battery <= 0) {
                        handler.sendEmptyMessageDelayed(602, 15 * 1000);
                        return;
                    } else if (GlobalConsts.battery <= 20) {
                        // TODO
                        showBattery();
                        return;
                    }
                    if (romCount.romCancleCount % 12 == 0) {
                        if (GlobalConsts.ROMISSHOW) {
                            Log.e("upgrdeRom", " DownloadRom ,提示框已弹出,设置相关信息");
                            if (GlobalConsts.romInstallDialog != null) {
                                GlobalConsts.romInstallDialog.setContent(romVersionName, GlobalConsts.FORCED);
                            }
                        } else {
                            Log.e("upgrdeRom", "  弹出升级框 DownloadRom romVersionName =" + romVersionName);
                            Intent intent = new Intent(context, RomInstallDialog.class);
                            if (!"".equals(romVersionName)) {
                                intent.putExtra("romVersionName", romVersionName);
                            }
                            intent.putExtra("forced", GlobalConsts.FORCED);
                            context.startActivity(intent);
                        }
                    } else {
                        romCount.romCancleCount++;
                    }
                }else{
                    checkRomAgain = true;
                }
                if (logCatHelper != null)
                    logCatHelper.stop();
                break;
            case 605:// 测试使用
                // updateInstallDialogss();
//                readSDCard();
                break;
            case 606:// 提示用户
                if (msg.arg1 != 0) {
                    int clearSize = msg.arg1;
                    Toast.makeText(context, " 需要清理" + clearSize + "M空间，用于系统升级!", Toast.LENGTH_LONG).show();
                }
                break;
            }
        };
    };
	
	/**
	 * @测试使用出版本需要注释
	 */
    private void updateInstallDialogss() {
        Log.e("upgrdeRom", " updateInstallDialogss");
        if (isTopActivity(context)) {
            Intent intents = new Intent(context, RomInstallDialog.class);
            // if(!"".equals(romVersionName)){
            intents.putExtra("romVersionName", "1.12");
            // }
            intents.putExtra("forced", GlobalConsts.FORCED);
            context.startActivity(intents);
        } else {
            Toast.makeText(context, " 不是launcher3顶层activity", Toast.LENGTH_LONG).show();
        }
    }

	public void reCheck(){
	    Log.e("upgrdeRom", " 检测rom ");
	    Log.e("upgrdeRom", " GlobalConsts.ROM_DOWNLOADING = "+GlobalConsts.ROM_DOWNLOADING);
		if (GlobalConsts.ROM_TYPE.equals("user")  && !GlobalConsts.ROM_DOWNLOADING) {
//		if (needUpdate && !GlobalConsts.ROM_DOWNLOADING) {
		    Log.e("upgrdeRom", " startload");
			romUpgradeInfo.startLoad();
		}else {
		    Log.e("upgrdeRom", " １reCheck 未请求接口");
		    if (logCatHelper!=null) 
                logCatHelper.stop();
        }
		if (GlobalConsts.ROM_TYPE.equals("user") && countFinished && !inited) {
//		if (countFinished && !inited) {
		    Log.e("upgrdeRom", " countFinished ＝true !inited = true 请求接口信息");
			countFinished = true;
			inited = true;
			romUpgradeInfo.startLoad();
		}else {
            Log.e("upgrdeRom", " 2 reCheck 未请求接口");
            if (logCatHelper!=null) 
                logCatHelper.stop();
        }
	}
	
	   private TextView currentInfo;
	   private TextView updateView;
	   private boolean down = true;
	   public void reCheck(TextView currentInfo,TextView updateView, boolean down){
	        this.currentInfo = currentInfo;
	        this.down = down;
	        this.updateView = updateView;
	        checkFirst = true;
	        Log.e("upgrdeRom", "  settting 检测rom 版本 ");
	        Log.e("upgrdeRom", "  settting GlobalConsts.ROM_DOWNLOADING = "+GlobalConsts.ROM_DOWNLOADING);
	        if (GlobalConsts.ROM_TYPE.equals("user")  && !GlobalConsts.ROM_DOWNLOADING) {
//	      if (needUpdate && !GlobalConsts.ROM_DOWNLOADING) {
	            Log.e("upgrdeRom", " settting startLoad ");
	            romUpgradeInfo.startLoad();
	        }else {
	            Log.e("upgrdeRom", " １reCheck 未请求接口");
	            if (logCatHelper!=null) 
	                logCatHelper.stop();
	        }
	        if (GlobalConsts.ROM_TYPE.equals("user") && countFinished && !inited) {
//	      if (countFinished && !inited) {
	            Log.e("upgrdeRom", " countFinished ＝true !inited = true 请求接口信息");
	            countFinished = true;
	            inited = true;
	            romUpgradeInfo.startLoad();
	        }else {
	            Log.e("upgrdeRom", " 2 reCheck 未请求接口");
	            if (logCatHelper!=null) 
	                logCatHelper.stop();
	        }
	        if (GlobalConsts.ROM_DOWNLOADING) {
                Toast.makeText(context, "正在下载", Toast.LENGTH_LONG).show();
            }
	    }
	   
	private void showBattery() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage("" + context.getString(R.string.str_battery_toast));
		builder.setTitle("" + context.getString(R.string.str_toast));
		builder.setPositiveButton("" + context.getString(R.string.str_btn_true), new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("" + context.getString(R.string.str_btn_false), new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
	
    class MyThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    sleep(1000 *60*60*2);
                    if (logCatHelper != null) {
                        logCatHelper.stop();
                    }
                    checkFirst = true;
                    logCatHelper = new LogcatHelper(context, "upgrdeRom");
                    logCatHelper.start();
                    checkRomAgain = true;
                    Log.e("upgrdeRom", "before rom升级轮询");
                    Log.e("upgrdeRom", "GlobalConsts.ROM_TYPE = " + GlobalConsts.ROM_TYPE);
                    Log.e("upgrdeRom", "rom pkg download status : ROM_DOWNLOADING  =" + GlobalConsts.ROM_DOWNLOADING);
                    Log.e("upgrdeRom", " update message : romUpgradeInfo =" + romUpgradeInfo);
                    Log.e("upgrdeRom", "network status : GlobalConsts.networkFlag =" + GlobalConsts.networkFlag);
                    if (GlobalConsts.ROM_TYPE.equals("user") && !GlobalConsts.ROM_DOWNLOADING && romUpgradeInfo != null
                            && GlobalConsts.networkFlag) {
                        // if (!GlobalConsts.ROM_DOWNLOADING && romUpgradeInfo
                        // != null && GlobalConsts.networkFlag) {
                        Log.e("upgrdeRom", "begin request rom update message ");
                        romUpgradeInfo.startLoad();
                    } else {
                        logCatHelper.stop();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 判断是否是顶层launcher3
     * @param context
     * @return
     */
  @SuppressWarnings("deprecation")
    private boolean isTopActivity(Context context) {
        boolean isTop = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String className = cn.getClassName();
        if (className.contains("com.android.launcher3.Launcher")) {
            isTop = true;
        }
        return isTop;
    }
  
}