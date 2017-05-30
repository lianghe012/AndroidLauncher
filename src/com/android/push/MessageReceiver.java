package com.android.push;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.launcher3.Launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class MessageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (PushContents.MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
            String messge = intent.getStringExtra(PushContents.KEY_MESSAGE);
//            String extras = intent.getStringExtra(KEY_EXTRAS);
            try {
               PushJson pushjson = PushJson.fromJson(new JSONObject(String.valueOf(messge)));
             int pushType =  pushjson.code;
               switch (pushType) {
            case 1://apk升级(ok)
            	
                break;
            case 2://rom升级 launcher才有rom升级(ok)
                if (Launcher.checkNewVersion != null) {
                    Log.e("pushInfo", " rom update ");
                    Launcher.checkNewVersion.reCheck();
                }
                break;
            case 3://请求首页数据(ok)
            	
                break;
            case 4://ui变灰,首页数据也需要改变
//            
                break;
            case 5://账号绑定sn号

                break;
            case 6://消息中心
                
                break;
            case 7://活动弹框,应该先下载图片,然后再弹框(暂时还没有)

                break;
            }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
