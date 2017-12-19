package md.i0.krfam;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

public class AppPreferences extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(this);
        PreferenceCategory main = addCategory("KRFAM Settings", screen, "main", null, this);
        PreferenceScreen serversScreen = addScreen("Server Settings", main);
        PreferenceCategory enabledServers = addCategory("Server Settings", serversScreen, "enabled", null, this);
        PreferenceScreen overlayScreen = addScreen("Overlay Settings", main);
        PreferenceCategory overlaySettings = addCategory("Overlay Settings", overlayScreen, "overlay", null, this);
        PreferenceCategory overlaySettings2 = addCategory("Enabled Overlays",overlayScreen,"enabled",null,this);
        PreferenceScreen saveLoadScreen = addScreen("Save & Load Settings", main);
        PreferenceCategory savingSettings = addCategory("Save & Load Settings", saveLoadScreen, "saving", null, this);
        PreferenceCategory generalSettings = addCategory("General Settings", screen, "general", null, this);

        //Server Settings
        for (Server s : KRFAM.serverList) {
            String extraMessage = "";
            if (s.installed == false) {
                extraMessage = "\nThis version of Kirara Fantasia is not installed!";
            }
            CheckBoxPreference c = addCheckBox(s.name, enabledServers, s.codeName, null, "Accounts from this server will be displayed." + extraMessage, "Accounts from this server will not be displayed." + extraMessage, false, this);
            if (s.installed == false) {
                c.setChecked(false);
                c.setEnabled(false);
            }
        }

        //Overlay Settings
        addCheckBox("Enable Overlay", overlaySettings, "enable_overlay", null, "Enabled Overlays will be displayed", "No overlays will be displayed", false, this);
        final CheckBoxPreference overlayClose = addCheckBox("Close Button", overlaySettings2, "close_button", null, "Will show button to return to KRFAM", "Adds button to return to KRFAM", false, this);
        final CheckBoxPreference overlayName = addCheckBox("Display Account Name", overlaySettings2, "overlay_name", null, "Will show account name on overlay","Adds account name to the overlay", false, this);
        final CheckBoxPreference overlayRename = addCheckBox("Display Rename Button", overlaySettings2, "overlay_rename", null, "Will show button to rename loaded account", "Adds button to rename loaded account", false, this);
        overlayName.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                overlayRename.setChecked(false);
                overlayRename.setEnabled((Boolean) newValue);
                return true;
            }
        });

        //Save & Load Settings
        addCheckBox("Quick Save", savingSettings, "quick_save", null, "Account will use the current time as a name.", "Account will ask for name before saving.", false, this);
        addCheckBox("Alternate QS Name", savingSettings,"alternate_qs",null,"Will use numbers for account name.","Will use data/time for account name.",false,this);
        addCheckBox("Auto Start SIF", savingSettings, "auto_start", null, "SIF will launch as soon as account has been loaded.", "SIF will not open after an account has been loaded.", true, this);
        addCheckBox("Allow Duplicate Saves", savingSettings, "allow_duplicate_save", null, "KRFAM won't check for existing save.", "KRFAM will check if the current account is already saved before saving.", false, this);

        //General Settings
        addCheckBox("Folders on Bottom", generalSettings, "folders_on_bottom", null, "Folders will be under accounts in the list.", "Folders will be above accounts in the list.", false, this);
        CheckBoxPreference noWarnings = addCheckBox("Disable Warnings and Alerts", generalSettings, "no_warnings", null,
                "No alerts or confirmations will be displayed when performing actions.\nThis feature is intended to be used with macros and should not be used with your main accounts.",
                "Alerts will be displayed when performing an action that may not be ideal.\nEnabling this feature is generally a bad idea unless you know what you're doing.", false, this);
        final CheckBoxPreference noDeleteWarnings = addCheckBox("No Warning on Delete", generalSettings, "no_delete_warning", null,
                "NO CONFIRMATION WILL BE REQUESTED WHEN DELETING ACCOUNTS.\nThis does not include folders with accounts in them.",
                "Enabling this will disable the confirmation when deleting accounts.\nDon't blame me if you delete something important.",
                false, this);
        noDeleteWarnings.setEnabled(KRFAM.NO_WARNINGS);
        noWarnings.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                noDeleteWarnings.setChecked(false);
                noDeleteWarnings.setEnabled((Boolean)newValue);
                return true;
            }
        });


        setPreferenceScreen(screen);
    }

    private PreferenceCategory addCategory(String title, PreferenceScreen screen, String key, Drawable icon, Context context) {
        PreferenceCategory c = new PreferenceCategory(context);
        c.setTitle(title);
        if (null != key) c.setKey(key);
        if (null != icon) c.setIcon(icon);
        screen.addPreference(c);
        return c;
    }

    private CheckBoxPreference addCheckBox(String title, PreferenceCategory category, String key, Drawable icon, String onText, String offText, boolean defaultValue, Context context) {
        CheckBoxPreference c = new CheckBoxPreference(context);
        c.setTitle(title);
        c.setKey(key);
        if (null != icon) c.setIcon(icon);
        if (offText.equals(null) || onText.equals(null)) {
            if (offText.equals(null)) {
                c.setSummary(onText);
            } else if (onText.equals(null)) {
                c.setSummary(offText);
            }
        } else {
            c.setSummaryOn(onText);
            c.setSummaryOff(offText);
        }
        c.setDefaultValue(defaultValue);
        c.setChecked(KRFAM.sharedPreferences.getBoolean(key, defaultValue));
        category.addPreference(c);
        return c;
    }

    private PreferenceScreen addScreen(String title, Preference parent){
        PreferenceScreen s = getPreferenceManager().createPreferenceScreen(this);
        s.setTitle(title);
        if (parent instanceof PreferenceScreen){
          ((PreferenceScreen) parent).addPreference(s);
        }else if (parent instanceof PreferenceCategory){
            ((PreferenceCategory) parent).addPreference(s);
        }
        return s;
    }
}
