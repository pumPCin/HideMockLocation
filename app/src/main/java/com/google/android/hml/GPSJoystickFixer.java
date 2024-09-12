package com.google.android.hml;

import static com.google.android.hml.Common.loadClassIfExist;

import android.app.Service;
import android.location.LocationManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class GPSJoystickFixer {

    // makes not kill process. but, joystick app uses for terminate threading.
    static boolean fixService(XC_LoadPackage.LoadPackageParam lpparam) {
        String packageName = lpparam.packageName;
        Class<?> joystick_MapOverlayService = loadClassIfExist(lpparam, packageName + ".service.MapOverlayService");
        Class<?> joystick_OverlayService = loadClassIfExist(lpparam, packageName + ".service.OverlayService");
        if (joystick_MapOverlayService != null && joystick_OverlayService != null) {
            Method joystick_MapOverlayService_onDestroy = XposedHelpers.findMethodExactIfExists(
                    joystick_MapOverlayService,
                    "onDestroy"
            );
            Method joystick_OverlayService_onDestroy = XposedHelpers.findMethodExactIfExists(
                    joystick_OverlayService,
                    "onDestroy"
            );
            if (joystick_MapOverlayService_onDestroy != null) {
                XposedBridge.hookMethod(joystick_MapOverlayService_onDestroy, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Service thisObject = (Service) param.thisObject;
                        Method _internalDestroyMethod = XposedHelpers.findMethodExactIfExists(
                                thisObject.getClass(),
                                // 4.3.2
                                "p"
                        );
                        if (_internalDestroyMethod != null) {
                            try {
                                _internalDestroyMethod.invoke(thisObject);
                                param.setResult(null);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                // do nothing.
                            }
                        } else {
                            XposedBridge.log("Failed to call internal destroy method(Joystick@MapOverlayService)");
                        }
                    }
                });
            }

            if (joystick_OverlayService_onDestroy != null) {
                XposedBridge.hookMethod(joystick_OverlayService_onDestroy, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Service thisObject = (Service) param.thisObject;
                        Method _internalDestroyMethod = XposedHelpers.findMethodExactIfExists(
                                thisObject.getClass(),
                                // 4.3.2
                                "D"
                        );
                        if (_internalDestroyMethod != null) {
                            try {
                                _internalDestroyMethod.invoke(thisObject);
                                param.setResult(null);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                // do nothing.
                            }
                        } else {
                            XposedBridge.log("Failed to call internal destroy method(Joystick2OverlayService)");
                        }
                    }
                });
            }
            XposedBridge.log("fixService()");
            return true;
        }
        return false;
    }

    static boolean isJoystickApp(XC_LoadPackage.LoadPackageParam lpparam) {
        String packageName = lpparam.packageName;
        Class<?> joystick_MapOverlayService = loadClassIfExist(lpparam, packageName + ".service.MapOverlayService");
        Class<?> joystick_OverlayService = loadClassIfExist(lpparam, packageName + ".service.OverlayService");
        return joystick_MapOverlayService != null && joystick_OverlayService != null;
    }

    static boolean fixTestProviderUpdates(XC_LoadPackage.LoadPackageParam lpparam) {
        String packageName = lpparam.packageName;
        if (!isJoystickApp(lpparam))
            return false;
        Class<?> joystick_MockLocationManager = loadClassIfExist(lpparam, packageName + ".b.u");
        if (joystick_MockLocationManager != null) {
            Method updateLocationMethod = XposedHelpers.findMethodExactIfExists(joystick_MockLocationManager,
                    "a",
                    double.class, // d
                    double.class, // d2
                    double.class, // d3
                    float.class, // f
                    boolean.class, // z
                    float.class, // f2
                    float.class, // f3
                    boolean.class // z2

            );
            Method addTestProviderMethod = XposedHelpers.findMethodExactIfExists(joystick_MockLocationManager,
                    "b"
            );

            if (addTestProviderMethod != null) {
                XposedBridge.hookMethod(addTestProviderMethod, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);

                        @SuppressWarnings("unchecked")
                        List<String> providers = (List<String>) XposedHelpers.getObjectField(param.thisObject, "j");
                        providers.clear();
                        providers.add(LocationManager.GPS_PROVIDER);
                        providers.add(LocationManager.NETWORK_PROVIDER);
                    }
                });
            }
            if (updateLocationMethod != null) {
                XposedBridge.hookMethod(updateLocationMethod, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        Object o = param.thisObject;

                        LocationManager locationManager = (LocationManager) XposedHelpers.getObjectField(o, "d");
                        @SuppressWarnings("unchecked")
                        List<String> providers = (List<String>) XposedHelpers.getObjectField(o, "j");

                        try {
                            for (String next : providers) {
                                if (!locationManager.isProviderEnabled(next)) {
                                    locationManager.setTestProviderEnabled(next, true);
                                }

                            }
                        } catch (Exception e) {
                            // removeProviders
                            XposedHelpers.callMethod(o, "c");
                            // addProviders
                            XposedHelpers.callMethod(o, "b");
                        }
                    }
                });
                return true;
            }
        }
        return false;
    }

    static boolean tryFixJoystickApp(XC_LoadPackage.LoadPackageParam lpparam) {
        return fixTestProviderUpdates(lpparam);
    }
}
