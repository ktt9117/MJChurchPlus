package org.mukdongjeil.mjchurch.data.network;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.mukdongjeil.mjchurch.util.InjectorUtils;

import androidx.annotation.Nullable;

public class SermonSyncIntentService extends IntentService {
    private static final String TAG = SermonSyncIntentService.class.getSimpleName();

    public SermonSyncIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "Intent service started");
        SermonNetworkDataSource networkDataSource = InjectorUtils.provideNetworkDataSource(this.getApplicationContext());
        networkDataSource.fetchSermon();
    }
}
