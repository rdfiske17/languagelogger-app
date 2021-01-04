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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedByteArray;

public class RestClient {

    private static Map<String,RestClient> instances = new HashMap<>();

    private ServerApi serverApi;
    //strong reference so it doesn't get garbage collected
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceListener;

    public static final Converter defaultConverter = new GsonConverter(new Gson());

    public static synchronized ServerApi get(Context context) {
        if (instances.get(defaultConverter.getClass().getSimpleName()) == null){
            instances.put(defaultConverter.getClass().getSimpleName(), new RestClient(context, null));
        }
        return instances.get(defaultConverter.getClass().getSimpleName()).serverApi;
    }

    public static synchronized ServerApi get(Context context, Converter converter) {
        if (instances.get(converter.getClass().getSimpleName()) == null){
            instances.put(converter.getClass().getSimpleName(), new RestClient(context, converter));
        }
        return instances.get(converter.getClass().getSimpleName()).serverApi;
    }

    private RestClient(Context context, Converter converter) {
        setupRestClient(context.getApplicationContext(), converter);
    }

    private void setupRestClient(final Context context, final Converter converter) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferenceListener = new SharedPreferences.OnSharedPreferenceChangeListener(){
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(context.getString(R.string.research_preference_key_server_address))){
                    setupRestClient(context, converter);
                }
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(preferenceListener);

        final OkHttpClient client = new OkHttpClient();

        try {
            // loading CAs from an InputStream
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream cert = context.getResources().openRawResource(R.raw.research_crt); // TODO replace this file's content with your server's certificate
            Certificate ca;
            try {
                ca = cf.generateCertificate(cert);
            } finally {
                cert.close();
            }

            // creating a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // creating a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // creating an SSLSocketFactory that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
        //    client.setSslSocketFactory(sslContext.getSocketFactory()); TODO Put your certificate into research_crt.crt and uncomment this
            client.setReadTimeout(Long.valueOf(context.getString(R.string.okhttp_read_timeout_secs)), TimeUnit.SECONDS);

        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException | KeyManagementException e) {
            e.printStackTrace();
        }

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(getServerAddress(context))
                .setClient(new OkClient(client))
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addHeader("User-Agent", context.getString(R.string.research_api_user_agent));
                    }
                })
                //.setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(converter != null ? converter : defaultConverter)
                .build();

        serverApi = restAdapter.create(ServerApi.class);
    }

    public static String getErrorDescription(RetrofitError error){
        try{
            return new String(((TypedByteArray)error.getResponse().getBody()).getBytes());
        }
        catch (Exception e){
            return error.toString();
        }
    }

    private static String getServerAddress(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.research_preference_key_server_address),
                context.getString(R.string.research_default_server_address));

    }
}
