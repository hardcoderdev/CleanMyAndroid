package runtime.exception.cleanmyandroid.base.extensions

import android.content.pm.ApplicationInfo

import android.content.pm.PackageInfo

fun PackageInfo.isSystemPackage(): Boolean {
    return applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
}