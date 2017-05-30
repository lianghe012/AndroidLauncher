package com.android.upgrade;

import java.io.File;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;

/**
 * 升级信息界面
 */
public class UpgradeInfoActivity extends Activity {

    /**
     * 新版本
     */
    private TextView updateInfo;
    /**
     * 当前版本
     */
    private TextView currentInfo;
    /**
     * 下载新版本
     */
    private Button downBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.update_layout);
        updateInfo = (TextView) findViewById(R.id.update_info);
        currentInfo = (TextView) findViewById(R.id.currentInfo);
        currentInfo.setTextColor(Color.BLACK);
        updateInfo.setTextColor(Color.BLACK);
        updateInfo.setTextSize(20);
        currentInfo.setTextSize(20);
        downBtn = (Button) findViewById(R.id.downBtn);
        downBtn.setText("确定"); 
        downBtn.setVisibility(View.GONE);
        currentInfo.setText("当前版本:"+GlobalConsts.ROM_NAME);
        updateInfo.setText("检测新版本中");
        // 如果有新版本,launcher3请求过了
        if (GlobalConsts.HAS_NEW_VERSION) {
//            Toast.makeText(this, "正在下载", Toast.LENGTH_LONG).show();
//            downBtn.setText("正在下载");
        } else if (GlobalConsts.ROM_NAME.equals("") || GlobalConsts.ROM_NAME == null) {// 没有版本信息,检测升级
            if (Launcher.checkNewVersion != null)
                Launcher.checkNewVersion.reCheck(currentInfo,updateInfo, true);
//            downBtn.setText("下载");
        } else if (GlobalConsts.ROM_NAME != null && !GlobalConsts.ROM_NAME.equals("")) {
            currentInfo.setText("当前版本:"+GlobalConsts.ROM_NAME);
            // 正在下载
            if (GlobalConsts.ROM_DOWNLOADING) {
//                Toast.makeText(this, "正在下载", Toast.LENGTH_LONG).show();
//                downBtn.setText("正在下载");
            } else {// 未下载,检测版本信息
                if (Launcher.checkNewVersion != null)
                    Launcher.checkNewVersion.reCheck(currentInfo,updateInfo, true);
            }
        }
    }

}
