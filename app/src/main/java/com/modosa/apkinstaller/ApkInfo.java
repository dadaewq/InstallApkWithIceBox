package com.modosa.apkinstaller;

import java.io.File;

class ApkInfo {
    private File apkFile;
    private boolean isFakePath;

    void setApkFile(File apkFile) {
        this.apkFile = apkFile;
    }

    void setFakePath(boolean fakePath) {
        isFakePath = fakePath;
    }
    boolean isFakePath() {
        return isFakePath;
    }

    File getApkFile() {
        return apkFile;
    }

}
