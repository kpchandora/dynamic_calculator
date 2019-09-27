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
    private static ArrayList<Class<?>> dymnamicClasses;

    public LoadDynamicClasses(Context context) {
        this.context = context;
        dymnamicClasses = new ArrayList<>();
    }

    public void loadClassesFromApk() {
        String apkPath = context.getFilesDir().getAbsolutePath() + "/app-debug.apk";

        final DexClassLoader classLoader = new DexClassLoader(apkPath, context.getCacheDir().getAbsolutePath(), null, this.getClass().getClassLoader());
        try {
            Class<?> classToLoad = (Class<?>) classLoader.loadClass("com.signzy.id_card_extraction.registry.Registry");
            Field field = classToLoad.getDeclaredField("_classes");
            ArrayList<Class<?>> classes = (ArrayList<Class<?>>) field.get(null);

            for (Class<?> cls : classes) {
                dymnamicClasses.add(cls);
                Log.d(TAG, "loadClassesFromApk: class name: " + cls.getName());
                for (Method m : cls.getDeclaredMethods()) {
                    Log.d(TAG, "loadClassesFromApk: method name: " + m.getName());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Class<?>> getDymnamicClasses() {
        return dymnamicClasses;
    }
}
