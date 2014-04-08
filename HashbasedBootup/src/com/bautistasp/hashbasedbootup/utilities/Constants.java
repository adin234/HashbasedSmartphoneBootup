package com.bautistasp.hashbasedbootup.utilities;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class Constants {
	public static Integer getVersion(Context c){
		PackageInfo pInfo;
		try {
			pInfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);

			return pInfo.versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getAppName(Context c){
		PackageInfo pInfo;
		try {
			pInfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
			return pInfo.packageName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}
