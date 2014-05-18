package main.kotlin.com.ne0fhyklabs.freeflight.utils;

import android.os.Build;

public class GlassUtils {
	
	public static boolean isGlassDevice() {
		return Build.MODEL.contains("Glass");
	}

}
