package eu.faircode.backpacktrack2;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class GpsStatusService extends Service {
    private static final String TAG = "BPT2.GpsStatusService";

    public GpsStatusService() {
    }

    private GpsStatus.Listener mGpsStatusListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
                // Count fixed/visible satellites
                int fixed = 0;
                int visible = 0;
                LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
                for (GpsSatellite sat : lm.getGpsStatus(null).getSatellites()) {
                    visible++;
                    if (sat.usedInFix())
                        fixed++;
                }

                // Persist fixed/visible satellites
                Log.i(TAG, "Satellites fixed/visible=" + fixed + "/" + visible);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GpsStatusService.this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(SettingsFragment.PREF_SATS_FIXED, fixed);
                editor.putInt(SettingsFragment.PREF_SATS_VISIBLE, visible);
                editor.apply();

                // Send state changed intent
                Intent intent = new Intent(GpsStatusService.this, BackgroundService.class);
                intent.setAction(BackgroundService.ACTION_STATE_CHANGED);
                startService(intent);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "Requesting GPS status updates");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GpsStatusService.this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(SettingsFragment.PREF_SATS_FIXED);
        editor.remove(SettingsFragment.PREF_SATS_VISIBLE);
        editor.apply();
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        lm.addGpsStatusListener(mGpsStatusListener);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Stopping GPS status updates");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GpsStatusService.this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(SettingsFragment.PREF_SATS_FIXED);
        editor.remove(SettingsFragment.PREF_SATS_VISIBLE);
        editor.apply();
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        lm.removeGpsStatusListener(mGpsStatusListener);

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
