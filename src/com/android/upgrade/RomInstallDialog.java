package com.android.upgrade;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.RecoverySystem;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;

public class RomInstallDialog extends XCBaseActivity implements OnClickListener {
	private RelativeLayout rom_install_layout;
	private LinearLayout btn_layout, content;
	private TextView title;
	private Button cancle, confirm;
	private ScrollView desc_scroll;
	private boolean forced;
	private RelativeLayout rom_install_layout_forced;
	private LinearLayout btn_layout_forced;
	private TextView fored_text_tip;
	private Button confirm_forced ,cancle_forced;
	private String romVersionName;

	private void initView() {
		Log.d("TAG", "initView");
		Intent intent = getIntent();
		forced = intent.getBooleanExtra("forced", false);
		if (intent.hasExtra("romVersionName")) {
			romVersionName = intent.getStringExtra("romVersionName");
		}
		rom_install_layout = (RelativeLayout) findViewById(R.id.rom_install_layout);
		rom_install_layout_forced = (RelativeLayout) findViewById(R.id.rom_install_layout_forced);
		btn_layout = (LinearLayout) findViewById(R.id.btn_layout);
		content = (LinearLayout) findViewById(R.id.content);
		btn_layout_forced = (LinearLayout) findViewById(R.id.btn_layout_forced);
		desc_scroll = (ScrollView) findViewById(R.id.desc_scroll);
		title = (TextView) findViewById(R.id.title);
		fored_text_tip = (TextView) findViewById(R.id.fored_text_tip);
		cancle = (Button) findViewById(R.id.cancle);
		confirm = (Button) findViewById(R.id.confirm);
		confirm_forced = (Button) findViewById(R.id.confirm_forced);
		cancle_forced = (Button) findViewById(R.id.cancle_forced);

//		UIUtils.setViewSizeX(rom_install_layout, (int) (GlobalConsts.Rx * 880));
//		UIUtils.setViewSizeY(rom_install_layout, (int) (GlobalConsts.Ry * 450));
//		UIUtils.setViewSizeX(rom_install_layout_forced, (int) (GlobalConsts.Rx * 880));
//		UIUtils.setViewSizeY(rom_install_layout_forced, (int) (GlobalConsts.Ry * 450));
//		UIUtils.setViewSizeX(cancle, (int) (GlobalConsts.Rx * 230));
//		UIUtils.setViewSizeY(cancle, (int) (GlobalConsts.Ry * 110));
//		UIUtils.setViewSizeX(confirm, (int) (GlobalConsts.Rx * 180));
//		UIUtils.setViewSizeY(confirm, (int) (GlobalConsts.Ry * 110));
//		UIUtils.setViewSizeX(confirm_forced, (int) (GlobalConsts.Rx * 280));
//		UIUtils.setViewSizeY(confirm_forced, (int) (GlobalConsts.Ry * 150));
//		UIUtils.setViewSizeX(cancle_forced, (int) (GlobalConsts.Rx * 280));
//		UIUtils.setViewSizeY(cancle_forced, (int) (GlobalConsts.Ry * 150));
//		UIUtils.setViewSizeY(desc_scroll, (int) (GlobalConsts.Ry * 140));
//
//		DoTextSize.setTextSize(title, 40);
//		DoTextSize.setTextSize(fored_text_tip, 40);
//		DoTextSize.setTextSize(cancle, 30);
//		DoTextSize.setTextSize(confirm, 30);
//		DoTextSize.setTextSize(cancle_forced, 30);
//		DoTextSize.setTextSize(confirm_forced, 30);
		
		if (forced) {
			rom_install_layout.setVisibility(View.GONE);
			rom_install_layout_forced.setVisibility(View.VISIBLE);
		}else {
			rom_install_layout.setVisibility(View.VISIBLE);
			rom_install_layout_forced.setVisibility(View.GONE);
		}

//		try {
//			UIUtils.setViewMarginSize(rom_install_layout, 0,
//					(int) (GlobalConsts.Rx * 265), 0, 0);
//			UIUtils.setViewMarginSize(rom_install_layout_forced, 0,
//					(int) (GlobalConsts.Rx * 265), 0, 0);
//			UIUtils.setViewMarginSize(btn_layout, 0, 0, 0,
//					(int) (GlobalConsts.Rx * 36));
//			UIUtils.setViewMarginSize(btn_layout_forced, 0, 0, 0,
//					(int) (GlobalConsts.Rx * 36));
//			UIUtils.setViewMarginSize(desc_scroll,
//					(int) (GlobalConsts.Rx * 90),
//					(int) (GlobalConsts.Rx * 115),
//					(int) (GlobalConsts.Rx * 90), 0);
//			UIUtils.setViewMarginSize(fored_text_tip,0,(int) (GlobalConsts.Rx * 150),0, 0);
//			UIUtils.setViewMarginSize(cancle, (int) (GlobalConsts.Rx * 55), 0,
//					0, 0);
//			UIUtils.setViewMarginSize(cancle_forced, (int) (GlobalConsts.Rx * 55), 0,
//					0, 0);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		cancle.setOnClickListener(this);
		confirm.setOnClickListener(this);
		confirm_forced.setOnClickListener(this);
		cancle_forced.setOnClickListener(this);

		initUpgradeMessage(GlobalConsts.DECS);
		
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
			RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(500, 350);
			rl.topMargin = 200;
			rl.addRule(RelativeLayout.CENTER_HORIZONTAL);
			rom_install_layout.setLayoutParams(rl);
		}else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(500, 350);
			rl.topMargin = 70;
			rl.addRule(RelativeLayout.CENTER_HORIZONTAL);
			rom_install_layout.setLayoutParams(rl);
		}
	}

	private void initUpgradeMessage(String string) {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		 ArrayList<String> upgradeMessage = getDescribe(string);
//		ArrayList<String> list = new ArrayList<String>();
//		for (int i = 0; i < 100; i++) {
//			list.add("生生世世事实上是事实上是事实上是事实上");
//		}
		 content.removeAllViews();
		for (int i = 0; i < upgradeMessage.size(); i++) {
			LinearLayout lin = new LinearLayout(this);
			lin.setLayoutParams(params);
			lin.setOrientation(LinearLayout.HORIZONTAL);
			TextView tv = new TextView(this);
			tv.setText(upgradeMessage.get(i));
			tv.setTextSize(15);
//			DoTextSize.setTextSize(tv, 30);
			tv.setTextColor(getResources().getColor(R.color.content_color));
//			params2.bottomMargin = (int) (GlobalConsts.Ry * 10);
			lin.addView(tv, params2);
			content.addView(lin);
		}
	}

	private ArrayList<String> getDescribe(String string) {
		ArrayList<String> updateDes = new ArrayList<String>();
		if (string != null && !"".equals(string)) {
			String[] des = string.split("\\@");
			for (int i = 0; i < des.length; i++) {
				updateDes.add(des[i]);
			}
		}
		return updateDes;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.cancle:
            if (Launcher.checkNewVersion != null) {
                RomCount romCount = Launcher.checkNewVersion.romCount;
                if (romVersionName != null && Launcher.checkNewVersion.romCount != null) {
                    if (romCount.romVersionName.equals("") || romCount.romVersionName.equals(romVersionName)) {
                        Launcher.checkNewVersion.romCount.romCancleCount++;
                        Launcher.checkNewVersion.romCount.romVersionName = romVersionName;
                    } else {
                        Launcher.checkNewVersion.romCount.romCancleCount = 0;
                        Launcher.checkNewVersion.romCount.romVersionName = romVersionName;
                    }
                }
            }
			finish();
			break;

		case R.id.confirm:
			try {
				RecoverySystem.installPackage(RomInstallDialog.this,new File(XCDirectory.LAUNCHER_DATA + "/" + GlobalConsts.ROM_SAVE_NAME));
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case R.id.confirm_forced:
			try {
				RecoverySystem.installPackage(RomInstallDialog.this, new File(XCDirectory.LAUNCHER_DATA + "/" + GlobalConsts.ROM_SAVE_NAME));
			} catch (IOException e) {
				e.printStackTrace();
			}
			finish();
			break;
		case R.id.cancle_forced:
			new RomForceInstallCount(RomInstallDialog.this, 30 * 1000, 1000).start();
			finish();
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (forced) {
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void initComponents() {
		// TODO Auto-generated method stub
		GlobalConsts.ROMISSHOW = true;
		GlobalConsts.romInstallDialog = this;
		initView();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		GlobalConsts.ROMISSHOW = false;
		GlobalConsts.romInstallDialog = null;
	}

	@Override
	public void initEvents() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initLogic() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getCondition() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getBaseLayout() {
		// TODO Auto-generated method stub
		return R.layout.rominstall;
	}

	@Override
	public void getBundle(Bundle bundle) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isCloseAtivityNew() {
		if (forced) {
			return false;
		}
		return true;
	}
	
	public void setContent(String versionName,boolean force){
		forced = force;
		if (force) {
			rom_install_layout.setVisibility(View.GONE);
			rom_install_layout_forced.setVisibility(View.VISIBLE);
		}else {
			rom_install_layout.setVisibility(View.VISIBLE);
			rom_install_layout_forced.setVisibility(View.GONE);
		}
		this.romVersionName = versionName;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(500, 350);
			rl.topMargin = 70;
			rl.addRule(RelativeLayout.CENTER_HORIZONTAL);
			rom_install_layout.setLayoutParams(rl);
		}else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
			RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(500, 350);
			rl.topMargin = 200;
			rl.addRule(RelativeLayout.CENTER_HORIZONTAL);
			rom_install_layout.setLayoutParams(rl);
		}
		super.onConfigurationChanged(newConfig);
	}
}
