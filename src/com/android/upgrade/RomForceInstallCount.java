package com.android.upgrade;

import java.io.File;
import java.io.IOException;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.RecoverySystem;

public class RomForceInstallCount extends CountDownTimer {
	private Context context;

	public RomForceInstallCount(Context context ,long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
		// TODO Auto-generated constructor stub
		this.context = context;
	}

	@Override
	public void onTick(long millisUntilFinished) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		try {
			RecoverySystem.installPackage(context, new File(XCDirectory.LAUNCHER_DATA + "/" + GlobalConsts.ROM_SAVE_NAME));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
