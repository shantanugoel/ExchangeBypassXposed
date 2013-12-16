package com.shantz.exchangebypass;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;
import static de.robv.android.xposed.XposedHelpers.setIntField;


/**
 * Created by shantanu on 10/13/13.
 */
public class XposedModule implements IXposedHookLoadPackage {
    XC_MethodReplacement returnTrue = new XC_MethodReplacement() {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param)
                throws Throwable {
            //XposedBridge.log("returntrue hook run");
            return true;
        }
    };

    XC_MethodReplacement returnZero = new XC_MethodReplacement() {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param)
                throws Throwable {
            //XposedBridge.log("returnzero hook run");
            return 0;
        }
    };

    XC_MethodHook updatePolicy = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            //XposedBridge.log("Normalize hook run");
            setIntField(param.thisObject, "mPasswordMode", 0);
            setBooleanField(param.thisObject, "mRequireRemoteWipe", false);
            setBooleanField(param.thisObject, "mRequireEncryption", false);
            setBooleanField(param.thisObject, "mRequireEncryptionExternal", false);
            setBooleanField(param.thisObject, "mRequireManualSyncWhenRoaming", false);
            setBooleanField(param.thisObject, "mDontAllowCamera", false);
            setBooleanField(param.thisObject, "mDontAllowAttachments", false);
            setBooleanField(param.thisObject, "mDontAllowHtml", false);
        }
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {

        }
    };

    public void handleLoadPackage(final LoadPackageParam lpparam)
            throws Throwable {
        if (lpparam.packageName.equals("com.google.android.email") || lpparam.packageName.equals("com.android.email")) {
            XposedBridge.log("Loaded app: " + lpparam.packageName);
            hookEmail(lpparam);
        } else if (lpparam.packageName.equals("com.google.android.exchange") || lpparam.packageName.equals("com.android.exchange")) {
            XposedBridge.log("Loaded app: " + lpparam.packageName);
            hookExchange(lpparam);
        }
    }

    private void hookExchange(LoadPackageParam lpparam) {
        findAndHookMethod("com.android.exchange.adapter.ProvisionParser",
                lpparam.classLoader, "hasSupportablePolicySet", returnTrue);
    }

    private void hookEmail(LoadPackageParam lpparam) {
        findAndHookMethod("com.android.emailcommon.provider.Policy",
                lpparam.classLoader, "normalize", updatePolicy);
        findAndHookMethod("com.android.email.SecurityPolicy",
                lpparam.classLoader, "getInactiveReasons", "com.android.emailcommon.provider.Policy",
                returnZero);
        if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT ) {
            findAndHookMethod("com.android.email.SecurityPolicy",
                    lpparam.classLoader, "isActiveAdmin", returnTrue);
            findAndHookMethod("com.android.email.SecurityPolicy",
                    lpparam.classLoader, "isActive", "com.android.emailcommon.provider.Policy",
                    returnTrue);
            findAndHookMethod("com.android.email.SecurityPolicy",
                    lpparam.classLoader, "remoteWipe",
                    de.robv.android.xposed.XC_MethodReplacement.DO_NOTHING);
            findAndHookMethod("com.android.email.SecurityPolicy",
                    lpparam.classLoader, "setAccountPolicy", long.class, "com.android.emailcommon.provider.Policy", String.class,
                    de.robv.android.xposed.XC_MethodReplacement.DO_NOTHING);
        }
    }
}
