package com.hificamera.thecamhi.utils;

import android.content.Context;
import android.os.Build;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.widget.EditText;

import java.lang.reflect.Method;
import java.util.Locale;

public class SystemUtils {
    public static final String TAG = "SystemUtils";
    private static final String HUAWEI_PROP = "ro.build.version.emui";
    private static final String XIAOMI_PROP = "ro.miui.ui.version.name";
    private static final String OPPO_PROP = "ro.build.version.opporom";
    private static final String VIVO_PROP = "ro.vivo.os.version";
    public static Boolean getBoolean(Context context, String key, boolean def) {
        Boolean result = def;
        try {
            ClassLoader classLoader = context.getClassLoader();
            @SuppressWarnings("rawtypes") Class SystemProperties = classLoader.loadClass("android.os.SystemProperties");
            @SuppressWarnings("rawtypes") Class[] paramTypes = new Class[2];
            paramTypes[0] = String.class;
            paramTypes[1] = boolean.class;
            Method getBoolean = SystemProperties.getMethod("getBoolean", paramTypes);
            Object[] params = new Object[2];
            params[0] = new String(key);
            params[1] = new Boolean(def);
            result = (Boolean) getBoolean.invoke(SystemProperties, params);
        } catch (IllegalArgumentException e) {
            //e.printStackTrace();
        } catch (Exception e) {
            result = def;
        }
        return result;
    }

    public static boolean isHuaweiMoblie(Context context) {
        return getBoolean(context, HUAWEI_PROP, false) || android.os.Build.BRAND.equalsIgnoreCase("HUAWEI") || Build.MANUFACTURER.equalsIgnoreCase("HUAWEI");
    }

    public static boolean isXiaomiMoblie(Context context) {
        return getBoolean(context, XIAOMI_PROP, false) || android.os.Build.BRAND.equalsIgnoreCase("XIAOMI") || Build.MANUFACTURER.equalsIgnoreCase("XIAOMI");
    }

    public static boolean isOPPOMoblie(Context context) {
        return getBoolean(context, OPPO_PROP, false) || android.os.Build.BRAND.equalsIgnoreCase("OPPO") || Build.MANUFACTURER.equalsIgnoreCase("OPPO");
    }

    public static boolean isVIVOMoblie(Context context) {
        return getBoolean(context, VIVO_PROP, false) || android.os.Build.BRAND.equalsIgnoreCase("VIVO") || Build.MANUFACTURER.equalsIgnoreCase("VIVO");
    }

    public static boolean isMEIZUMoblie(Context context) {
        return Build.DISPLAY.contains("FLYME") || android.os.Build.BRAND.equalsIgnoreCase("MEIZU") || Build.MANUFACTURER.equalsIgnoreCase("MEIZU");
    }

    public static boolean isZh(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;
    }

    public static void setEditTextInhibitInputSpace(EditText editText) {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (dstart == 0 && source.equals(" ")) {
                    return "";
                }
                return source;
            }
        };
        editText.setFilters(new InputFilter[]{filter});
    }
}
