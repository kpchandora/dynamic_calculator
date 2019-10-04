package com.example.calculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText firstInput;
    private EditText secondInput;
    private TextView resultTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firstInput = findViewById(R.id.first_input_et);
        secondInput = findViewById(R.id.second_input_et);
        resultTv = findViewById(R.id.result_tv);

        fetchVersionFile(new FileDownloadCallback() {
            @Override
            public void onDownloadSuccess(final int... i) {
                if (i[0] > getVersion()) {
                    savePrefs(i[0]);
                    getApkFile(new FileDownloadCallback() {
                        @Override
                        public void onDownloadSuccess(int... j) {
                            Log.d(TAG, "onDownloadSuccess: " + i[0]);
                            loadDynamicClasses();
                        }

                        @Override
                        public void onDownloadFailure(Exception e) {
                            Log.d(TAG, "onDownloadFailure: " + e.getMessage());
                        }
                    });
                }else {
                    loadDynamicClasses();
                }
            }

            @Override
            public void onDownloadFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }


    private void loadDynamicClasses() {
        new Thread() {
            @Override
            public void run() {
                new LoadDynamicClasses(MainActivity.this).loadClassesFromApk();
            }
        }.start();
    }

    public void add(View view) {
        if (LoadDynamicClasses.getDymnamicClass() != null) {
            try {
                int firstNum = Integer.parseInt(firstInput.getText().toString());
                int secondNum = Integer.parseInt(secondInput.getText().toString());
                Method addMethod = LoadDynamicClasses.getDymnamicClass().getMethod("add", double.class, double.class);
                double result = (double) addMethod.invoke(null, firstNum, secondNum);
                resultTv.setText(String.valueOf(result));
                Log.d(TAG, "callDynamicClasses: addResult: " + result);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private interface FileDownloadCallback {
        void onDownloadSuccess(int... i);

        void onDownloadFailure(Exception e);
    }


    private void fetchVersionFile(final FileDownloadCallback fileDownloadCallback) {

        final long size = 1024;//1024 kb

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference reference = storage.getReference().child("apk/version");
        reference.getBytes(size).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                try {
                    String str = new String(bytes, "UTF-8").replace("\"", "").trim();
                    Log.d(TAG, "onSuccess: " + str);
                    int version = Integer.parseInt(str);
                    fileDownloadCallback.onDownloadSuccess(version);
                } catch (Exception e) {
                    e.printStackTrace();
                    fileDownloadCallback.onDownloadSuccess(0);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                fileDownloadCallback.onDownloadFailure(e);
            }
        });
    }


    private void getApkFile(final FileDownloadCallback callback) {
        File file = new File(getFilesDir().getAbsolutePath(), "app-debug.apk");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference reference = storage.getReference().child("apk/app-debug.apk");
        reference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "onSuccess: " + taskSnapshot.getTotalByteCount());
                callback.onDownloadSuccess();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: ");
                callback.onDownloadFailure(e);
            }
        });
    }


    private void savePrefs(int version) {
        SharedPreferences preferences = getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
        preferences.edit().putInt("version", version).apply();
    }

    private int getVersion() {
        return getSharedPreferences("my_prefs", Context.MODE_PRIVATE).getInt("version", -1);
    }

}
