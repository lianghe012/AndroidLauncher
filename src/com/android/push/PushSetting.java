package com.android.push;

import java.util.LinkedHashSet;
import java.util.Set;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

import com.android.launcher3.LauncherApplication;
/**
 * 处理推送 设置tag 
 */
public class PushSetting {

    private final int MSG_SET_TAGS = 1002;
    private final String TAG = "pushSetting";
    private final String tagName = "launcher3";
    public void pushSettingTag(Context context) {
//    	StringBuffer strBuff = new StringBuffer();
//        strBuff.append(context.getPackageName()+",");
//        // 检查 tag 的有效性
//        if (TextUtils.isEmpty(String.valueOf(strBuff))) {
//            Toast.makeText(context, R.string.error_tag_empty, Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // ","隔开的多个 转换成 Set
//        String[] sArray = String.valueOf(strBuff).split(",");
        Set<String> tagSet = new LinkedHashSet<String>();
//        for (String sTagItme : sArray) {
//            if (!ExampleUtil.isValidTagAndAlias(sTagItme)) {
//                Toast.makeText(context, R.string.error_tag_gs_empty, Toast.LENGTH_SHORT).show();
//                return;
            tagSet.add(tagName);
//        }
        // 调用JPush API设置Tag
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_TAGS, tagSet));
    }

    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case MSG_SET_TAGS:
                JPushInterface.setAliasAndTags(LauncherApplication.application, null, (Set<String>) msg.obj, mTagsCallback);
                break;
            default:
                break;
            }
        };
    };

    private final TagAliasCallback mTagsCallback = new TagAliasCallback() {

        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs;
            switch (code) {
            case 0:
                logs = "Set tag and alias success";
                Log.i(TAG, logs);
                break;
            case 6002:
                logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                Log.i(TAG, logs);
                if (ExampleUtil.isConnected(LauncherApplication.application)) {
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_TAGS, tags), 1000 * 60);
                } else {
                    Log.i(TAG, "No network");
                }
                break;

            default:
                logs = "Failed with errorCode = " + code;
                Log.e(TAG, logs);
            }
            //显示是否成功
//            ExampleUtil.showToast(logs, LauncherApplication.context);
        }
    };

    
    
    
    
}
