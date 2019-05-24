package com.modosa.apkinstaller;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.catchingnow.icebox.sdk_client.IceBox;

import io.reactivex.disposables.Disposable;

public class InitializeActivity extends Activity {
    private Disposable mSubscribe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Log.e("----tagg----","onCreate");
        super.onCreate(savedInstanceState);

        PackageManager pm = getPackageManager();
        ComponentName comptName = new ComponentName(this, MainActivity.class);
        boolean isenabled = (pm.getComponentEnabledSetting(comptName) == (PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) || pm.getComponentEnabledSetting(comptName) == (PackageManager.COMPONENT_ENABLED_STATE_ENABLED));
        if (!isenabled) {
            pm.getComponentEnabledSetting(comptName);
            pm.setComponentEnabledSetting(comptName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
//            Log.e("--ComponentEnabled--", "MainActivity组件已启用");
//            Log.e("--isEnabled--", pm.getComponentEnabledSetting(comptName) + "");
        }
        this.requestPermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        updatePermissionState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //      Log.e("----tagg----","onResume");
        updatePermissionState();
    }

    @Override
    protected void onDestroy() {
        //     Log.e("----tagg----","onDestroy");
        super.onDestroy();
        disposeSafety();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{IceBox.SDK_PERMISSION, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                0x233);
    }

    private void updatePermissionState() {
        IceBox.SilentInstallSupport state = IceBox.querySupportSilentInstall(this);
//            Log.e("----state----",state+"");
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//             Log.e("permission==",permission == PackageManager.PERMISSION_GRANTED ? "PERMISSION_GRANTED" : "PERMISSION_DENIED");
        if ((state + "").contains("SUPPORTED") && (permission == 0)) {
            Toast.makeText(this, getString(R.string.hide_icon), Toast.LENGTH_LONG).show();
            PackageManager p = getPackageManager();
            p.setComponentEnabledSetting(getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
        finish();
    }

    private void disposeSafety() {
        if (mSubscribe != null && !mSubscribe.isDisposed()) mSubscribe.dispose();
        mSubscribe = null;
    }

}