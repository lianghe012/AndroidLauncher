package com.android.upgrade;

import android.content.Context;

public class RomUpgradeLoader extends BaseAPILoader<UpgradeRom>{
	private String version;
	private String rom;
	private String SerialNumber;

	public RomUpgradeLoader(Context context , String version,String rom,String SerialNumber) {
		super(context);
		// TODO Auto-generated constructor stub
		this.version = version;
		this.rom = rom;
		this.SerialNumber = SerialNumber;
	}
	
	@Override
	public UpgradeRom loadInBackground() {
		// TODO Auto-generated method stub
		UpgradeRom upgradeRom = null;
		try {
			upgradeRom = XCAPI.upgrdeRom(rom, version,SerialNumber);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return upgradeRom;
	}

}
