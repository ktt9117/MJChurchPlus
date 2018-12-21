package org.mukdongjeil.mjchurch.data.network;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import org.mukdongjeil.mjchurch.util.InjectorUtils;

import androidx.annotation.Nullable;

public class SermonSyncIntentService extends IntentService {
    private static final String TAG = SermonSyncIntentService.class.getSimpleName();

    public static final String INTENT_KEY_FETCH_TYPE = "fetchType";
    public static final String INTENT_VALUE_FETCH_TYPE_SERMON = "sermon";
    public static final String INTENT_VALUE_FETCH_TYPE_SERMON_REPLY = "sermonReply";

    public static final String INTENT_KEY_BBS_NO = "bbsNo";

    public SermonSyncIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "Sync IntentService started");
        String fetchType = intent.getStringExtra(INTENT_KEY_FETCH_TYPE);
        Log.d(TAG, "fetchType : " + fetchType);
        if (TextUtils.isEmpty(fetchType)) {
            Log.e(TAG, "Error caused by fetch type is empty");
            return;
        }

        if (fetchType.equals(INTENT_VALUE_FETCH_TYPE_SERMON)) {
            SermonNetworkDataSource networkDataSource =
                    InjectorUtils.provideSermonNetworkDataSource(this.getApplicationContext());
            networkDataSource.fetch();

        } else if (fetchType.equals(INTENT_VALUE_FETCH_TYPE_SERMON_REPLY)) {
            int bbsNo = intent.getIntExtra(INTENT_KEY_BBS_NO, -1);
            if (bbsNo == -1) {
                Log.e(TAG, "Error caused by bbsNo is empty");
                return;
            }

            SermonReplyNetworkDataSource networkDataSource =
                    InjectorUtils.provideSermonReplyNetworkDataSource(this.getApplicationContext());
            networkDataSource.fetch(bbsNo);
        }

    }
}
