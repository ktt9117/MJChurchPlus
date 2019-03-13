package org.mukdongjeil.mjchurch.data.database;

import org.mukdongjeil.mjchurch.Const;

public class FirestoreDatabase {

    public interface Collection {
        String SERMON = Const.DEV_MODE ? "sermon-dev" : "sermon";
        String REPLIES = Const.DEV_MODE ? "replies-dev" : "replies";
        String BOARD = Const.DEV_MODE ? "board-dev" : "board";
        String APP_SETTINGS = Const.DEV_MODE ? "app-settings-dev" : "app-settings";
    }

    public interface Document {
        String LAST_SYNC_INFO = "last_sync_info";
    }

    public interface Field {
        String SERMON_SYNC_DATE = "sermon_sync_date";
        String BOARD_SYNC_DATE = "board_sync_date";
    }
}
