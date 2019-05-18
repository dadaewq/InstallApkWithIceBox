package com.modosa.apkinstaller;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.widget.Toast;
import java.io.File;

import com.catchingnow.icebox.sdk_client.IceBox;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends Activity {
    private Disposable mSubscribe;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String apkPath = getSourceApkInfo();
        if (apkPath != null) {
            final File apkFile = new File(apkPath);
            showToast("开始安装：" + apkFile.getPath());
            new Thread(() -> {
                installApp(apkFile.getPath());
                finish();
            }).start();
        } else {
            showToast("读取 APK 失败");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }
    private void installApp(String path) {
        String authority = getPackageName() + ".FILE_PROVIDER";
        Uri uri = FileProvider.getUriForFile(this, authority, new File(path));
        disposeSafety();
        mSubscribe = Single.fromCallable(() -> IceBox.installPackage(this, uri))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> Toast.makeText(this, success ? "安装成功" : "安装失败", Toast.LENGTH_SHORT).show(), Throwable::printStackTrace);
    }

    private void showToast(final String text) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show());
    }

    private String getSourceApkInfo() {
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri != null) {
            return uri.getPath();
        }
        return null;
    }
    private void disposeSafety() {
        if (mSubscribe != null && !mSubscribe.isDisposed()) mSubscribe.dispose();
        mSubscribe = null;
    }
}
