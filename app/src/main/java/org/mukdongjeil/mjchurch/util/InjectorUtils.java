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
import org.mukdongjeil.mjchurch.data.network.SermonNetworkDataSource;
import org.mukdongjeil.mjchurch.data.network.SermonReplyNetworkDataSource;
import org.mukdongjeil.mjchurch.ui.introduce.IntroduceViewModelFactory;
import org.mukdongjeil.mjchurch.ui.sermon_detail.SermonDetailActivityViewModelFactory;
import org.mukdongjeil.mjchurch.ui.sermons.SermonViewModelFactory;
import org.mukdongjeil.mjchurch.ui.training.TrainingViewModelFactory;

/**
 * Provides static methods to inject the various classes needed for Sunshine
 */
public class InjectorUtils {

    public static SermonNetworkDataSource provideSermonNetworkDataSource(Context context) {
        provideRepository(context.getApplicationContext());
        AppExecutors executors = AppExecutors.getInstance();
        return SermonNetworkDataSource.getInstance(context.getApplicationContext(), executors);
    }

    public static SermonReplyNetworkDataSource provideSermonReplyNetworkDataSource(Context context) {
        provideRepository(context.getApplicationContext());
        AppExecutors executors = AppExecutors.getInstance();
        return SermonReplyNetworkDataSource.getInstance(context.getApplicationContext(), executors);
    }

    public static ChurchRepository provideRepository(Context context) {
        ChurchDatabase database = ChurchDatabase.getInstance(context.getApplicationContext());
        AppExecutors executors = AppExecutors.getInstance();
        SermonNetworkDataSource networkDataSource =
                SermonNetworkDataSource.getInstance(context.getApplicationContext(), executors);
        SermonReplyNetworkDataSource replyNetworkDataSource =
                SermonReplyNetworkDataSource.getInstance(context.getApplicationContext(), executors);
        return ChurchRepository.getInstance(database.sermonDao(),
                networkDataSource, replyNetworkDataSource, executors);
    }

    public static SermonViewModelFactory provideSermonViewModelFactory(Context context) {
        ChurchRepository repository = provideRepository(context.getApplicationContext());
        return new SermonViewModelFactory(repository);
    }

    public static IntroduceViewModelFactory provideIntroduceViewModelFactory(Context context) {
        ChurchRepository repository = provideRepository(context.getApplicationContext());
        return new IntroduceViewModelFactory(repository);
    }

    public static TrainingViewModelFactory provideTrainingViewModelFactory(Context context) {
        ChurchRepository repository = provideRepository(context.getApplicationContext());
        return new TrainingViewModelFactory(repository);
    }

    public static SermonDetailActivityViewModelFactory
        provideSermonDetailActivityViewModelFactory(Context context, int bbsNo) {
        ChurchRepository repository = provideRepository(context.getApplicationContext());
        return new SermonDetailActivityViewModelFactory(repository, bbsNo);
    }
}