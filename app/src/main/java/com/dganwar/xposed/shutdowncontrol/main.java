package com.dganwar.xposed.shutdowncontrol;

/**
 * Created by deepak on 3/2/18.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.content.res.XModuleResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import static de.robv.android.xposed.XposedHelpers.findClass;




public class main implements IXposedHookZygoteInit, IXposedHookLoadPackage {

    static XModuleResources mModRes;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        mModRes = XModuleResources.createInstance(startupParam.modulePath, null);
    }


    private final static String PKG_BATTERY_SERVICE = "com.android.server.BatteryService";
    private final static String PKG_ACTIVITY_MANAGER_NATIVE = "android.app.ActivityManagerNative";

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        XposedBridge.log("Loaded app: " + lpparam.packageName);
        final Class<?> batteryService = findClass(PKG_BATTERY_SERVICE, lpparam.classLoader);
        final Class<?> amn = findClass(PKG_ACTIVITY_MANAGER_NATIVE, lpparam.classLoader);

        XposedBridge.hookAllMethods(batteryService, "shutdownIfNoPowerLocked", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                final int battLevel = (Integer) XposedHelpers.callMethod(param.thisObject, "getBatteryLevel");
                final boolean isPowered = (Boolean) XposedHelpers.callMethod(param.thisObject, "isPowered");
                final boolean isSystemReady = (Boolean) XposedHelpers.callStaticMethod(amn, "isSystemReady");


                // shut down gracefully if our battery is critically low and we are not powered.
                // wait until the system has booted before attempting to display the shutdown dialog.
                if (battLevel == 0 && !isPowered && isSystemReady) {
//                    notifyUser();
                    XposedBridge.log("battery is 0%. Starting shutdown");
//                    param.setResult(null);
                }
            }
        });

    }

}
