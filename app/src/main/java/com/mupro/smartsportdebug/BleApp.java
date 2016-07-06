package com.mupro.smartsportdebug;

import android.app.Application;

public class BleApp extends Application {

	public BleAppManager manager;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		manager = new BleAppManager(getApplicationContext());
	}

}
