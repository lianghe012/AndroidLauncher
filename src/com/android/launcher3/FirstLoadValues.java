package com.android.launcher3;

public class FirstLoadValues {
	public ShortcutInfo sInfo;
	public FolderInfo fInfo;
	public long container;
	public int screen, cellX, cellY;
	
	public FirstLoadValues(ShortcutInfo item, long container, int screen,
			int cellX, int cellY) {
		this.sInfo = item;
		this.container = container;
		this.screen = screen;
		this.cellX = cellX;
		this.cellY = cellY;
	}
	
	public FirstLoadValues(FolderInfo item, long container, int screen,
			int cellX, int cellY) {
		this.fInfo = item;
		this.container = container;
		this.screen = screen;
		this.cellX = cellX;
		this.cellY = cellY;
	}
}
