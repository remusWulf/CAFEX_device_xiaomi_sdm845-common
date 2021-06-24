/*
 *  Poco Extras Settings Module
 *  Made by @shivatejapeddi 2019
 */

package com.lineageos.settings.pocopref;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.os.SELinux;
import android.os.Handler;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreference;
import androidx.preference.TwoStatePreference;
import com.lineageos.settings.pocopref.BframeworkActivity;
import com.lineageos.settings.pocopref.SecureSettingListPreference;
import com.lineageos.settings.pocopref.SuShell;
import com.lineageos.settings.pocopref.SuTask;
import com.lineageos.settings.pocopref.CustomSeekBarPreference;
import com.lineageos.settings.pocopref.Utils;
import android.os.FileUtils;
import com.lineageos.settings.pocopref.VibratorStrengthPreference;
import android.provider.Settings;
import android.view.Menu;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.view.MenuItem;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;
import android.util.Slog;
import android.util.Log;
import android.os.SystemProperties;
import java.io.*;
import android.widget.Toast;

import com.lineageos.settings.pocopref.R;

public class PocoPrefSettings extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {
	private static final boolean DEBUG = false;
	private static final String TAG = "PocoPref";
    public static final String PREF_BFRAMEWORK = "device_bframework";	
    public static final String CATEGORY_DISPLAY = "display";    
    private static final String SYSTEM_PROPERTY_NVT_FW = "persist.nvt_fw";
    private static final String SYSTEM_PROPERTY_NVT_ESD = "persist.nvt_esd";
    private static final String SYSTEM_PROPERTY_DOLBY = "persist.baikal.dolby.enable";
    private static final String SELINUX_CATEGORY = "selinux";
    private static final String PREF_SELINUX_MODE = "selinux_mode";
    private static final String SEEK_BAR = "seek_bar";    
    private static final String PREF_SELINUX_PERSISTENCE = "selinux_persistence";
    public static final String PREF_CHARGING_SWITCH = "smart_charging";
    public static final String PREF_RESET_STATS = "reset_stats";
    public static final String KEY_VIBSTRENGTH = "vib_strength";
    public static final String KEY_WAVEFORM = "vib_waveform";
    public static final String DEFAULT_KEY_WAVEFORM = "3e 3e 3e 3e be be a0 90";        
    public static final String SMART_CHARGING_PATH = "/sys/class/power_supply/battery/input_suspend";    

    public static final String KEY_WAVEFORM_PATH = "/sys/devices/platform/soc/c440000.qcom,spmi/spmi-0/spmi0-03/c440000.qcom,spmi:qcom,pmi8998@3:qcom,haptics@c000/leds/vibrator/effect_samp";    
    
    private Context mContext;
    private Preference mSystemSettings;
    private Preference mKcal;
    private Preference mBframework;	
    private SwitchPreference mNvtFw;
    private SwitchPreference mNvtESD;
    private SwitchPreference mSelinuxMode;
    private SwitchPreference mDolby;
    private SwitchPreference mSelinuxPersistence;    
    private SharedPreferences mPrefs; 
    private SwitchPreference mSmartChargingSwitch;
    private VibratorStrengthPreference mVibratorStrength;
    private SecureSettingListPreference mWaveForm;    
            
    private SwitchPreference mResetStats;
    private String mWaveFormValue;
    private CustomSeekBarPreference mSeekBarPreference;
    
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.poco_settings, rootKey);	
        mContext = this.getContext();
         mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        ContentResolver resolver = getActivity().getContentResolver();        

        PreferenceCategory displayCategory = (PreferenceCategory) findPreference(CATEGORY_DISPLAY);

        mSystemSettings = findPreference("systemsettings");
                mSystemSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                     @Override
                     public boolean onPreferenceClick(Preference preference) {
                         Intent intent = new Intent(getActivity().getApplicationContext(), SystemSettingsActivity.class);
                         startActivity(intent);
                         return true;
                     }
                });


        mBframework = findPreference(PREF_BFRAMEWORK);

        mBframework.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity().getApplicationContext(), BframeworkActivity.class);
            startActivity(intent);
            return true;
        });	        	

        mVibratorStrength = (VibratorStrengthPreference) findPreference(KEY_VIBSTRENGTH);
        if (mVibratorStrength != null) {
            mVibratorStrength.setEnabled(VibratorStrengthPreference.isSupported());
        }
 
         // SELinux
        Preference selinuxCategory = findPreference(SELINUX_CATEGORY);
        mSelinuxMode = (SwitchPreference) findPreference(PREF_SELINUX_MODE);
        mSelinuxMode.setChecked(SELinux.isSELinuxEnforced());
        mSelinuxMode.setOnPreferenceChangeListener(this);

        mSelinuxPersistence =
        (SwitchPreference) findPreference(PREF_SELINUX_PERSISTENCE);
        mSelinuxPersistence.setOnPreferenceChangeListener(this);
        mSelinuxPersistence.setChecked(getContext()
        .getSharedPreferences("selinux_pref", Context.MODE_PRIVATE)
        .contains(PREF_SELINUX_MODE));
                   
        mDolby = (SwitchPreference) findPreference(SYSTEM_PROPERTY_DOLBY);
        mDolby.setChecked(SystemProperties.getBoolean(SYSTEM_PROPERTY_DOLBY, false));
        mDolby.setOnPreferenceChangeListener(this);
		
        mWaveFormValue = mPrefs.getString(KEY_WAVEFORM, DEFAULT_KEY_WAVEFORM);  
        mWaveForm = (SecureSettingListPreference) findPreference(KEY_WAVEFORM);
        mWaveForm.setValue(mWaveFormValue);
        mWaveForm.setOnPreferenceChangeListener(this);       

     }

    public static void restore(Context context) {
       String profile = PreferenceManager
              .getDefaultSharedPreferences(context).getString(PocoPrefSettings.KEY_WAVEFORM, DEFAULT_KEY_WAVEFORM);
             try {
            FileUtils.stringToFile(KEY_WAVEFORM_PATH, profile);
            } catch (IOException e) {
            Slog.e(TAG, "Error writing ", e);
            }                          
     }
                    
    private void setSystemPropertyBoolean(String key, boolean value) {
    	if(value) {
 	      SystemProperties.set(key, "true");
    	} else {
    		SystemProperties.set(key, "false");
    	}
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        ContentResolver resolver = getActivity().getContentResolver();    
        final String key = preference.getKey();      
        switch (key) {

            case PREF_SELINUX_MODE:
                  if (preference == mSelinuxMode) {
                  boolean enabled = (Boolean) value;
                  new SwitchSelinuxTask(getActivity()).execute(enabled);
                  setSelinuxEnabled(enabled, mSelinuxPersistence.isChecked());
                  return true;
                } else if (preference == mSelinuxPersistence) {
                  setSelinuxEnabled(mSelinuxMode.isChecked(), (Boolean) value);
                  return true;
                }
                break;

            case SYSTEM_PROPERTY_DOLBY:
                ((SwitchPreference)preference).setChecked((Boolean) value);
                setSystemPropertyBoolean(SYSTEM_PROPERTY_DOLBY, (Boolean) value);
                break;

            case KEY_WAVEFORM:
            mWaveFormValue = value.toString();
            mPrefs.edit().putString(KEY_WAVEFORM, mWaveFormValue).commit();            
            try {
            FileUtils.stringToFile(KEY_WAVEFORM_PATH, mWaveFormValue);
            } catch (IOException e) {
            Slog.e(TAG, "Error writing ", e);
            }
                break;
				
            default:
                break;
        }
        return true;
    }

        private void setSelinuxEnabled(boolean status, boolean persistent) {
          SharedPreferences.Editor editor = getContext()
              .getSharedPreferences("selinux_pref", Context.MODE_PRIVATE).edit();
          if (persistent) {
            editor.putBoolean(PREF_SELINUX_MODE, status);
          } else {
            editor.remove(PREF_SELINUX_MODE);
          }
          editor.apply();
          mSelinuxMode.setChecked(status);
        }

        private class SwitchSelinuxTask extends SuTask<Boolean> {
          public SwitchSelinuxTask(Context context) {
            super(context);
          }
          @Override
          protected void sudoInBackground(Boolean... params) throws SuShell.SuDeniedException {
            if (params.length != 1) {
              Log.e(TAG, "SwitchSelinuxTask: invalid params count");
              return;
            }
            if (params[0]) {
              SuShell.runWithSuCheck("setenforce 1");
            } else {
              SuShell.runWithSuCheck("setenforce 0");
            }
          }

          @Override
          protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (!result) {
              // Did not work, so restore actual value
              setSelinuxEnabled(SELinux.isSELinuxEnforced(), mSelinuxPersistence.isChecked());
            }
          }
        }      
}
