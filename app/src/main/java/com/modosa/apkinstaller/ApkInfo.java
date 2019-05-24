package com.modosa.apkinstaller;

import java.io.File;

class ApkInfo {
    private File apkFile;
    private boolean isFakePath;

    void setFakePath() {
        isFakePath = true;
    }

    boolean isFakePath() {
        return isFakePath;
    }

    File getApkFile() {
        return apkFile;
    }

    void setApkFile(File apkFile) {
        this.apkFile = apkFile;
    }

}
