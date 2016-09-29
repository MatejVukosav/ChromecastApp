package vuki.com.chromecastapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;

import vuki.com.chromecastapp.utils.Utils;

/**
 * Created by mvukosav on 3.8.2016..
 */
public class CastPreference extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        addPreferencesFromResource( R.xml.application_preference );
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener( this );

        EditTextPreference versionPref = (EditTextPreference) findPreference( "app_version" );
        versionPref.setTitle( getString( R.string.version, Utils.getAppVersionName( this ) ) );

    }

    @Override
    public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String s ) {
        //Handle changed preferences
    }
}
