package com.mwiacek.polskie.przepisy;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class PreferencesActivity extends PreferenceActivity {
    final Activity MyActivity5 = this;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.sett2);

    	try {
        	  PackageManager manager = getPackageManager();
        	  PackageInfo info = manager.getPackageInfo(getPackageName(), 0);

        	  setTitle("Polskie przepisy "+info.versionName);
        } catch (Exception e) {
        	  
        }
  	        
        OnSharedPreferenceChangeListener  listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        	//	if (prefs.getBoolean("Obrot", false)) {
        			//setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        		//} else {
//        			setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);        				  
        		//}
        	}
        };
                      
        SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(listener); 
        //if (!sp.getBoolean("Obrot", false)) {
//        	setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);                	            		
        //}    
        
        Preference customPref = (Preference) findPreference("Czyszczenie");
        customPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

        		public boolean onPreferenceClick(Preference preference) {
        			Intent intent = new Intent(Intent.ACTION_VIEW);
        			intent.setData(Uri.parse("http://mwiacek.com/www/?q=node/121"));
        			MyActivity5.startActivity(intent);

        		//	SearchRecentSuggestions suggestions = new SearchRecentSuggestions(MyActivity5,
        			       // SearchHistoryProvider.AUTHORITY, SearchHistoryProvider.MODE);
        			//suggestions.clearHistory();
        			return true;
        		}

        });       
	}
	
}	
    	