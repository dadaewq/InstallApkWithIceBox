package com.modosa.apkinstaller;

interface ICommanderCallback {
    void onStartParseApk();

    void onApkParsed();

    void onApkPreInstall(ApkInfo apkInfo);
}
