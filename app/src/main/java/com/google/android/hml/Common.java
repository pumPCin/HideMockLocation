package com.google.android.hml;

import android.annotation.SuppressLint;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Common {
    public static final String GMS_MOCK_KEY = "mockLocation"; // FusedLocationProviderApi.KEY_MOCK_LOCATION


    @SuppressLint("PrivateApi")
    public static Class<?> loadClassIfExist(XC_LoadPackage.LoadPackageParam lpparam, String name) {
        try {
            return lpparam.classLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
