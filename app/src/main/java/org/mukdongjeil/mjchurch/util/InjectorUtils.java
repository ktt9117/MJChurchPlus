/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mukdongjeil.mjchurch.util;

import android.content.Context;

import org.mukdongjeil.mjchurch.AppExecutors;
import org.mukdongjeil.mjchurch.data.ChurchRepository;
import org.mukdongjeil.mjchurch.data.database.ChurchDatabase;
import org.mukdongjeil.mjchurch.data.network.BoardNetworkDataSource;
import org.mukdongjeil.mjchurch.data.network.SermonNetworkDataSource;
import org.mukdongjeil.mjchurch.data.network.ReplyNetworkDataSource;
import org.mukdongjeil.mjchurch.ui.board_detail.BoardDetailViewModelFactory;
import org.mukdongjeil.mjchurch.ui.boards.BoardViewModelFactory;
import org.mukdongjeil.mjchurch.ui.introduce.IntroduceViewModelFactory;
import org.mukdongjeil.mjchurch.ui.sermon_detail.SermonDetailActivityViewModelFactory;
import org.mukdongjeil.mjchurch.ui.sermons.SermonViewModelFactory;
import org.mukdongjeil.mjchurch.ui.training.TrainingViewModelFactory;

/**
 * Provides static methods to inject the various classes needed for Sunshine
 */
public class InjectorUtils {

    public static final SermonNetworkDataSource provideSermonNetworkDataSource(Context context) {
        provideRepository(context.getApplicationContext());
        AppExecutors executors = AppExecutors.getInstance();
        return SermonNetworkDataSource.getInstance(context.getApplicationContext(), executors);
    }

    public static final ReplyNetworkDataSource provideSermonReplyNetworkDataSource(Context context) {
        provideRepository(context.getApplicationContext());
        AppExecutors executors = AppExecutors.getInstance();
        return ReplyNetworkDataSource.getInstance(context.getApplicationContext(), executors);
    }

    public static final BoardNetworkDataSource provideBoardNetworkDataSource(Context context) {
        provideRepository(context.getApplicationContext());
        AppExecutors executors = AppExecutors.getInstance();
        return BoardNetworkDataSource.getInstance(context.getApplicationContext(), executors);
    }

    public static final ChurchRepository provideRepository(Context context) {
        ChurchDatabase database = ChurchDatabase.getInstance(context.getApplicationContext());
        AppExecutors executors = AppExecutors.getInstance();
        SermonNetworkDataSource networkDataSource =
                SermonNetworkDataSource.getInstance(context.getApplicationContext(), executors);
        ReplyNetworkDataSource replyNetworkDataSource =
                ReplyNetworkDataSource.getInstance(context.getApplicationContext(), executors);
        BoardNetworkDataSource boardNetworkDataSource =
                BoardNetworkDataSource.getInstance(context.getApplicationContext(), executors);
        return ChurchRepository.getInstance(database.sermonDao(),
                networkDataSource, replyNetworkDataSource, boardNetworkDataSource, executors);
    }

    public static final SermonViewModelFactory provideSermonViewModelFactory(Context context) {
        ChurchRepository repository = provideRepository(context.getApplicationContext());
        return new SermonViewModelFactory(repository);
    }

    public static final IntroduceViewModelFactory provideIntroduceViewModelFactory(Context context) {
        ChurchRepository repository = provideRepository(context.getApplicationContext());
        return new IntroduceViewModelFactory(repository);
    }

    public static final TrainingViewModelFactory provideTrainingViewModelFactory(Context context) {
        ChurchRepository repository = provideRepository(context.getApplicationContext());
        return new TrainingViewModelFactory(repository);
    }

    public static final BoardViewModelFactory provideBoardViewModelFactory(Context context) {
        ChurchRepository repository = provideRepository(context.getApplicationContext());
        return new BoardViewModelFactory(repository);
    }

    public static final BoardDetailViewModelFactory provideBoardDetailViewModelFactory(Context context, String boardId) {
        ChurchRepository repository = provideRepository(context.getApplicationContext());
        return new BoardDetailViewModelFactory(repository, boardId);
    }

    public static final SermonDetailActivityViewModelFactory
        provideSermonDetailActivityViewModelFactory(Context context, String bbsNo) {
        ChurchRepository repository = provideRepository(context.getApplicationContext());
        return new SermonDetailActivityViewModelFactory(repository, bbsNo);
    }
}