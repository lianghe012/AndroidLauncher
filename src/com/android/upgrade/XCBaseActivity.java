package com.android.upgrade;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

public abstract class XCBaseActivity extends Activity implements ActivityInnerIntf{
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		if (getCondition()) {
			finish();
		}
		Intent intent = getIntent();
		Bundle bundle = null;
		if (getBaseLayout() != 0) {
			setContentView(getBaseLayout());
		}
		getBundle(bundle);
		initComponents();
		initEvents();
		initLogic();
	}
	

	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		return super.dispatchKeyEvent(event);
	}

	public abstract boolean getCondition();

	public abstract int getBaseLayout();

	public abstract void getBundle(Bundle bundle);// 传递数据
	
	public boolean isCloseAtivityNew(){//按home键是否结束
		return true;
	}
}
