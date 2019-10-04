package com.example.calculator;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import dalvik.system.DexClassLoader;

public class LoadDynamicClasses {

    private Context context;
    private static final String TAG = "LoadDynamicClasses";
    private static Class<?> dymnamicClass;

    public LoadDynamicClasses(Context context) {
        this.context = context;
        dymnamicClass = null;
    }

    public void loadClassesFromApk() {
        String apkPath = context.getFilesDir().getAbsolutePath() + "/app-debug.apk";

        final DexClassLoader classLoader = new DexClassLoader(apkPath, context.getCacheDir().getAbsolutePath(), null, this.getClass().getClassLoader());
        try {
            dymnamicClass = classLoader.loadClass("com.example.calculator.DynamicCalculator");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Class<?> getDymnamicClass() {
        return dymnamicClass;
    }
}
