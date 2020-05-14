/*
 * Copyright (C) 2016 - 2018 ResearchIME Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.lmu.ifi.researchime.base;

import com.google.gson.JsonObject;

import java.io.File;

import retrofit.Callback;
import retrofit.ResponseCallback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;

public interface ServerApi {

    /**
     * Post events to the server
     *
     * @param events an array containing the events
     * @param callback retrofit callback
     */
    @POST("/api/events")
    void postEvents(@Body JsonObject events, ResponseCallback callback);


    /**
     * Post the user to the server
     *
     * @param user the user object
     * @param callback retrofit callback
     */
    @POST("/api/user")
    void postUser(@Body JsonObject user, ResponseCallback callback);

    /**
     * Post the current keyboard state to the server
     *
     * @param state an array containing the keyboard state events
     * @param callback retrofit callback
     */
    @POST("/api/keyboard-state")
    void postKeyboardState(@Body JsonObject state, ResponseCallback callback);

    /**
     * Get configurations from the server
     *
     * @param callback configurations
     */
    @GET("/api/config")
    void getConfiguration(Callback<JsonObject> callback);


    //----- ResearchIME v2 - combined with Protium architecture
    // so the calls are handled synchronously by the background module threads
    /**
     * Get the dashboard data
     *
     * @param userId userId
     * @return response
     */
    @GET("/api/dashboard/{userId}")
    JsonObject getDashboard(@Path("userId") String userId);

    /**
     * Get the dynamic keyboard layout
     *
     *
     * @param userId userId
     * @param layoutId layoutId
     * @return repsonse
     */
    @GET("/api/layout/{userId}")
    JsonObject getLayout(@Path("userId") String userId, @Query("layoutId") String layoutId);

    /**
     * Post a message to the server
     *
     * @param message message containing the text the user entered
     * @param callback retrofit callback
     */
    @POST("/api/messaging")
    void postMessage(@Body JsonObject message, ResponseCallback callback);

    @GET("/api/contentabstraction/listconfig")
    JsonObject getContentAbstractionConfig();

    // --- is implemented with OkHttpClient in ConfigDownloader
//    /**
//     * attention: this method requires the RestClient to be initialized with RetrofitPlainTextConverter!
//     * @param logicallistId
//     * @return the same file as you specified in RetrofitPlainTextConverter as target file for the response data
//     */
//    @GET("/api/contentabstraction/logicalcategorylist/{logicallistId}")
//    File getLogicalCategoryListFile(@Path("logicallistId") Long logicallistId);
//
//    /**
//     * attention: this method requires the RestClient to be initialized with RetrofitPlainTextConverter!
//     * @param logicallistId
//     * @return the same file as you specified in RetrofitPlainTextConverter as target file for the response data
//     */
//    @GET("/api/contentabstraction/logicalwordlist/{logicallistId}")
//    File getLogicalWordListFile(@Path("logicallistId") Long logicallistId);

    @POST("/api/contentabstraction/abstractedactionevents")
    void postContentAbstractionEvents(@Body JsonObject events, ResponseCallback callback);

    @POST("/api/contentabstraction/wordfrequencies")
    void postWordFrequencies(@Body JsonObject wordFrequencies, ResponseCallback callback);

    @POST("/api/contentabstraction/messagestatistics")
    void postMessageStatistics(@Body JsonObject messageStatistics, ResponseCallback callback);
}
