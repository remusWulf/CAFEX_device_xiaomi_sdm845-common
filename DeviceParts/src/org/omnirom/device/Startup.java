/*
* Copyright (C) 2013 The OmniROM Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.omnirom.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import androidx.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;

public class Startup extends BroadcastReceiver {

    private static final String ASUS_GAMEMODE = "asus_gamemode";

    private static void restore(String file, boolean enabled) {
        if (file == null) {
            return;
        }
        Utils.writeValue(file, enabled ? "1" : "0");
    }

    private static void restore(String file, String value) {
        if (file == null) {
            return;
        }
        Utils.writeValue(file, value);
    }

    @Override
    public void onReceive(final Context context, final Intent bootintent) {
        restoreAfterUserSwitch(context);
        context.startService(new Intent(context, GripSensorServiceMain.class));
    }

    public static void restoreAfterUserSwitch(Context context) {

       String value = Settings.System.getString(context.getContentResolver(), DeviceSettings.FPS);
        if (TextUtils.isEmpty(value)) {
            value = DeviceSettings.DEFAULT_FPS_VALUE;
            Settings.System.putString(context.getContentResolver(), DeviceSettings.FPS, value);
            DeviceSettings.changeFps(context, Integer.valueOf(value));
        } else {
        Settings.System.putString(context.getContentResolver(), DeviceSettings.FPS, value);
        DeviceSettings.changeFps(context, Integer.valueOf(value));
        }

        value = Settings.Global.getString(context.getContentResolver(), ASUS_GAMEMODE);
        if (TextUtils.isEmpty(value)) {
            value = "0";
            if (Utils.isCNSKU()) {
                Settings.System.putString(context.getContentResolver(), "asus_grip_short_squeeze", "6");
                Settings.System.putString(context.getContentResolver(), "asus_grip_locked_short_squeeze", "6");
            } else {
                Settings.System.putString(context.getContentResolver(), "asus_grip_short_squeeze", "4");
                Settings.System.putString(context.getContentResolver(), "asus_grip_locked_short_squeeze", "4");
            }
            Settings.System.putString(context.getContentResolver(), "asus_grip_long_squeeze", "6");
            Settings.System.putString(context.getContentResolver(), "asus_grip_locked_long_squeeze", "6");
            Settings.Global.putString(context.getContentResolver(), ASUS_GAMEMODE, value);

            Settings.Global.putString(context.getContentResolver(), "air_trigger_squeeze_threshold_level", "5");
            Settings.Global.putString(context.getContentResolver(), "air_trigger_tap_left_threshold_level", "4");
            Settings.Global.putString(context.getContentResolver(), "air_trigger_tap_right_threshold_level", "4");
            Settings.Global.putString(context.getContentResolver(), "air_trigger_tap_threshold_level", "4");
        }
    }
}
