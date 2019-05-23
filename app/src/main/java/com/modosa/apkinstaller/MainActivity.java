package com.modosa.apkinstaller;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.FileProvider;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.catchingnow.icebox.sdk_client.IceBox;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends Activity implements ICommanderCallback {
    private Disposable mSubscribe;
    private APKCommander apkCommander;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getData() != null) {
            apkCommander = new APKCommander(this, getIntent().getData(), this);
        } else {
            showToast("读取 APK 失败");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }

    private void installApp(ApkInfo mapkinfo, String path) {
        String authority = getPackageName() + ".FILE_PROVIDER";
        Uri uri = FileProvider.getUriForFile(this, authority, new File(path));
        disposeSafety();
        mSubscribe = Single.fromCallable(() -> IceBox.installPackage(this, uri))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    Toast.makeText(this, success ? "安装成功" : "安装失败", Toast.LENGTH_SHORT).show();
                    if(mapkinfo.isFakePath())
                        deleteSingleFile(mapkinfo.getApkFile().getPath());
                }, Throwable::printStackTrace);
    }

    private void deleteSingleFile(String filePath$Name) {
        File file = new File(filePath$Name);
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                Log.e("--Method--", "Copy_Delete.deleteSingleFile: 删除单个文件" + filePath$Name + "成功！");
            }
//            else {
//                Toast.makeText(getApplicationContext(), "删除单个文件" + filePath$Name + "失败！", Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            Toast.makeText(getApplicationContext(), "删除单个文件失败：" + filePath$Name + "不存在！", Toast.LENGTH_SHORT).show();
        }
    }


    private void showToast(final String text) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show());
    }
    private void disposeSafety() {
        if (mSubscribe != null && !mSubscribe.isDisposed()) mSubscribe.dispose();
        mSubscribe = null;
    }

    @Override
    public void onStartParseApk() {
        showToast(getString(R.string.parsing));
    }

    @Override
    public void onApkParsed() {
        apkCommander.startInstall();
    }

    @Override
    public void onApkPreInstall(ApkInfo apkInfo) {
        showToast(getString(R.string.start_install, apkInfo.getApkFile().getPath()));
    }
    class APKCommander {

        private final Context context;
        private final Uri uri;
        private ApkInfo mApkInfo;
        private final ICommanderCallback callback;
        private final Handler handler;

        APKCommander(Context context, Uri uri, ICommanderCallback commanderCallback) {
            this.context = context;
            this.uri = uri;
            this.callback = commanderCallback;
            handler = new Handler(Looper.getMainLooper());
            new ParseApkTask().start();
        }

        void startInstall() {
            new InstallApkTask().start();
        }

        private class InstallApkTask extends Thread {
            @Override
            public void run() {
                super.run();
                handler.post(() -> callback.onApkPreInstall(mApkInfo));
                new Thread(() -> {
                    handler.post(() -> installApp(mApkInfo,mApkInfo.getApkFile().getPath()));
                    finish();
                }).start();
            }
        }

        private class ParseApkTask extends Thread {
            @Override
            public void run() {
                super.run();
                try {
                    handler.post(callback::onStartParseApk);
                    mApkInfo = new ApkInfo();
                    String apkSourcePath = ContentUriUtils.getPath(context, uri);
                    if (apkSourcePath == null) {
                        mApkInfo.setFakePath(true);
                        File tempFile = new File(context.getExternalCacheDir(), System.currentTimeMillis() + ".apk");
                        try {
                            InputStream is = context.getContentResolver().openInputStream(uri);
                            if (is != null) {
                                OutputStream fos = new FileOutputStream(tempFile);
                                byte[] buf = new byte[4096 * 1024];
                                int ret;
                                while ((ret = is.read(buf)) != -1) {
                                    fos.write(buf, 0, ret);
                                    fos.flush();
                                }
                                fos.close();
                                is.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mApkInfo.setApkFile(tempFile);
                    } else {
                        mApkInfo.setApkFile(new File(apkSourcePath));
                    }
                    handler.post(callback::onApkParsed);
                } catch (Exception e) {
                    handler.post(callback::onApkParsed);
                    e.printStackTrace();
                    throw new AndroidRuntimeException(e);

                }
            }
        }
    }
}