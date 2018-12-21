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
package org.mukdongjeil.mjchurch.data.network;

import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * These utilities will be used to communicate with the weather servers.
 */
public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    public static final String BASE_URL = "http://mukdongjeil.hompee.org";
    private static final String BASE_URL_APPENDIX_PREFIX = "m/board/index.hpc";

    private static final String MENU_ID_PARAM = "menuId";
    private static final String TOP_MENU_ID_PARAM = "topMenuId";
    private static final String MENU_TYPE_PARAM = "menuType";
    private static final String NEW_MENU_AT_PARAM = "newmenuAt";

    private static final String TOP_MENU_ID = "2";
    private static final String MENU_TYPE = "1";
    private static final String NEW_MENU_AT = "true";

    private static final String WELCOME_URL = BASE_URL + "/m/html/index.hpc?menuId=1749&topMenuId=1&menuType=27&newmenuAt=false&tPage=1";
    private static final String TRAINING_URL = BASE_URL + "/m/html/index.hpc?menuId=10005607&topMenuId=3&menuType=27&newmenuAt=true&tPage=1";

    private static final int SUNDAY_MORNING_WORSHIP_ID = 10004043;

    public static URL getUrl() {
        String menuIdQuery = Integer.toString(SUNDAY_MORNING_WORSHIP_ID);
        return buildUrlWithLocationQuery(menuIdQuery);
    }

    public static URL getWelcomeUrl() {
        try {
            URL url = new URL(WELCOME_URL);
            Log.v(TAG, "getWelcomUrl : " + url);
            return url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static URL getTrainingUrl() {
        try {
            URL url = new URL(TRAINING_URL);
            Log.v(TAG, "getTrainingUrl : " + url);
            return url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static URL makeCompleteUrl(String queryParams) {
        Uri queryUri = Uri.parse(BASE_URL).buildUpon()
                .appendEncodedPath(queryParams)
                .build();

        try {
            URL queryUrl = new URL(queryUri.toString());
            Log.v(TAG, "URL: " + queryUrl);
            return queryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static URL buildUrlWithLocationQuery(String menuIdQuery) {
        Uri queryUri = Uri.parse(BASE_URL).buildUpon()
                .appendEncodedPath(BASE_URL_APPENDIX_PREFIX)
                .appendQueryParameter(MENU_ID_PARAM, menuIdQuery)
                .appendQueryParameter(TOP_MENU_ID_PARAM, TOP_MENU_ID)
                .appendQueryParameter(MENU_TYPE_PARAM, MENU_TYPE)
                .appendQueryParameter(NEW_MENU_AT_PARAM, NEW_MENU_AT)
                .build();

        try {
            URL queryUrl = new URL(queryUri.toString());
            Log.v(TAG, "URL: " + queryUrl);
            return queryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response, null if no response
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            String response = null;
            if (hasInput) {
                response = scanner.next();
            }
            scanner.close();
            return response;
        } finally {
            urlConnection.disconnect();
        }
    }
}