package io.github.seao3oiio.guitartuna.popupblocker;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class PopupBlockerHook implements IXposedHookLoadPackage {
    private static final String TAG = "GuitarTunaPopupSuppressor";
    private static final String TARGET_PACKAGE = "com.ovelin.guitartuna";
    private static final String PLAYER_PREFS_NAME =
            "com.ovelin.guitartuna.v2.playerprefs";
    private static final String LAST_DISPLAYED_KEY =
            "LastSessionIndexPopupConversionDisplayed";
    private static final String CURRENT_SESSION_KEY =
            "PreviousAppStartupSessionIndex";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (!TARGET_PACKAGE.equals(loadPackageParam.packageName)
                || !TARGET_PACKAGE.equals(loadPackageParam.processName)) {
            return;
        }

        hookApplicationAttach();
        hookPopupMarkerReads();
        XposedBridge.log(TAG + ": hook active for " + TARGET_PACKAGE);
    }

    private static void hookApplicationAttach() {
        XposedHelpers.findAndHookMethod(
                Application.class,
                "attach",
                Context.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        try {
                            preArmPopupMarker((Context) param.args[0]);
                        } catch (Throwable throwable) {
                            XposedBridge.log(TAG + ": failed to pre-arm popup marker");
                            XposedBridge.log(throwable);
                        }
                    }
                }
        );
    }

    private static void preArmPopupMarker(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(
                PLAYER_PREFS_NAME,
                Context.MODE_PRIVATE
        );

        if (!preferences.contains(CURRENT_SESSION_KEY)) {
            XposedBridge.log(TAG + ": session key unavailable during attach");
            return;
        }

        int currentSession = preferences.getInt(CURRENT_SESSION_KEY, 0);
        int nextSession = currentSession == Integer.MAX_VALUE
                ? Integer.MAX_VALUE
                : currentSession + 1;
        boolean committed = preferences.edit()
                .putInt(LAST_DISPLAYED_KEY, nextSession)
                .commit();

        XposedBridge.log(
                TAG + ": pre-armed popup marker; currentSession="
                        + currentSession + ", nextSession=" + nextSession
                        + ", committed=" + committed
        );
    }

    private static void hookPopupMarkerReads() {
        XposedHelpers.findAndHookMethod(
                "android.app.SharedPreferencesImpl",
                null,
                "getInt",
                String.class,
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (!LAST_DISPLAYED_KEY.equals(param.args[0])) {
                            return;
                        }

                        try {
                            returnCurrentSession(param);
                        } catch (Throwable throwable) {
                            XposedBridge.log(TAG + ": failed to intercept popup marker read");
                            XposedBridge.log(throwable);
                        }
                    }
                }
        );
    }

    private static void returnCurrentSession(XC_MethodHook.MethodHookParam param) {
        SharedPreferences preferences = (SharedPreferences) param.thisObject;
        if (!preferences.contains(CURRENT_SESSION_KEY)
                || !preferences.contains(LAST_DISPLAYED_KEY)) {
            return;
        }

        int storedLastDisplayed = (Integer) param.getResult();
        int currentSession = preferences.getInt(
                CURRENT_SESSION_KEY,
                storedLastDisplayed
        );

        param.setResult(currentSession);
        XposedBridge.log(
                TAG + ": suppressed popup; lastDisplayed="
                        + storedLastDisplayed + ", currentSession=" + currentSession
        );
    }
}
