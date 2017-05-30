package com.android.upgrade;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BatteryReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			int current = intent.getExtras().getInt("level");// 获得当前电量
			int total = intent.getExtras().getInt("scale");// 获得总电量
			int percent = current * 100 / total;
			GlobalConsts.battery = percent;
//			Log.d("super_hw", "当前电量 " + GlobalConsts.battery + "%");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}