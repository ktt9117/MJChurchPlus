package org.mukdongjeil.mjchurch.data.database;

import org.mukdongjeil.mjchurch.Const;

public class FirestoreDatabase {

    public interface Collection {
        String SERMON = Const.DEBUG_MODE ? "sermon-dev" : "sermon";
        String REPLIES = Const.DEBUG_MODE ? "replies-dev" : "replies";
        String BOARD = Const.DEBUG_MODE ? "board-dev" : "board";
    }

    public interface Document {
        String BBS_NO = "bbsNo";
    }
}
