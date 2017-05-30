package com.android.upgrade;

import android.content.Context;
import android.content.Loader;
import android.content.Loader.OnLoadCompleteListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class RomUpgradeInfo implements OnLoadCompleteListener<UpgradeRom>{
	private Handler handler;
	private RomUpgradeLoader romUpgradeLoader;
	
	public void init(Context context,String rom,String version,String SerialNumber,Handler handler){
		this.handler = handler;
		romUpgradeLoader = new RomUpgradeLoader(context, version, rom,SerialNumber);
		romUpgradeLoader.registerListener(0, RomUpgradeInfo.this);
	}
	
	@Override
	public void onLoadComplete(Loader<UpgradeRom> loader, UpgradeRom data) {
		if (data != null && data.status == 200) {
			if (data.data.upgrade) {
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.obj = data.data;
					msg.what = 500;
					handler.sendMessage(msg);
				}
			}else {
				handler.sendEmptyMessage(501);
			}
		}else {
			handler.sendEmptyMessage(502);
		}
	}
	
	public void startLoad(){
		romUpgradeLoader.startLoading();
	}
}
