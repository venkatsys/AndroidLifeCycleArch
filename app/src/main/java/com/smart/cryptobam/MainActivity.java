package com.smart.cryptobam;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.cryptobam.entities.CryptoCoinEntity;
import com.smart.cryptobam.recview.CoinModel;
import com.smart.cryptobam.recview.Divider;
import com.smart.cryptobam.recview.MyCryptoAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends TrackingActivity {

    private MyCryptoAdapter mAdapter;
    private RecyclerView recView;
    private static final String TAG = MainActivity.class.getSimpleName();
    private RequestQueue mQueue;
    public final String CRYPTO_URL_PATH = "https://files.coinmarketcap.com/static/img/coins/128x128/%s.png";
    public final String ENDPOINT_FETCH_CRYPTO_DATA = "https://api.coinmarketcap.com/v1/ticker/?limit=3";
    private String DATA_FILE_NAME = "crypto.data";
    private final ObjectMapper mObjMapper = new ObjectMapper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        fetchData();
    }

    private void bindViews() {
        Toolbar toolbar = this.findViewById(R.id.toolbar);
        recView = this.findViewById(R.id.recView);
        mAdapter = new MyCryptoAdapter();
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        recView.setLayoutManager(lm);
        recView.setAdapter(mAdapter);
        recView.addItemDecoration(new Divider(this));
        setSupportActionBar(toolbar);
    }

    private void fetchData() {
        Log.d(TAG, "fetchData() called" + "fetchData");
        if(mQueue == null){
            mQueue = Volley.newRequestQueue(this);
            final JsonArrayRequest jsonObjReq = new JsonArrayRequest(ENDPOINT_FETCH_CRYPTO_DATA,
                    response -> {
                        writeDataToInternalStorage(response);
                        //Log.d(TAG, "fetchData() called" + response);
                        ArrayList<CryptoCoinEntity> data = parseJSON(response.toString());
                        Log.d(TAG, "fetchData() called" + data);
                        new EntityToModelMapperTask().execute(data);
                    },
                    error ->{
                        try {
                            JSONArray data =  readDataFromStorage();
                            ArrayList<CryptoCoinEntity> entities = parseJSON(data.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });

            mQueue.add(jsonObjReq);
        }
    }

    public ArrayList<CryptoCoinEntity> parseJSON(String jsonStr){
        ArrayList<CryptoCoinEntity> data = null;

        try{
            data = mObjMapper.readValue(jsonStr, new TypeReference<ArrayList<CryptoCoinEntity>>() {
            });
        }catch (Exception e){
        }

        return data;
    }

    private void writeDataToInternalStorage(JSONArray data){
        FileOutputStream fos = null;

        try{
            fos = openFileOutput(DATA_FILE_NAME, Context.MODE_PRIVATE);
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }

        try{
            fos.write(data.toString().getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSONArray readDataFromStorage() throws JSONException{
        FileInputStream fis = null;
        try{
            fis = openFileInput(DATA_FILE_NAME);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return new JSONArray(sb.toString());
    }

    private class EntityToModelMapperTask extends AsyncTask<List<CryptoCoinEntity>,Void,List<CoinModel>> {
        @Override
        protected List<CoinModel> doInBackground(List<CryptoCoinEntity>... data) {
            final ArrayList<CoinModel> listData = new ArrayList<>();
            CryptoCoinEntity entity;
            for(int i = 0 ; i < data[0].size(); i++){
                entity = data[0].get(i);
                listData.add(new CoinModel(entity.getName() , entity.getSymbol() , String.format(CRYPTO_URL_PATH , entity.getId()) , entity.getPriceUsd() , entity.getPercentChange24h()));
            }
            return listData;
        }

        @Override
        protected void onPostExecute(List<CoinModel> data) {
            Log.d(TAG, "onPostExecute() called with: data = [" + data + "]");
            mAdapter.setItems(data);
            mAdapter.notifyDataSetChanged();
            super.onPostExecute(data);
        }
    }

}
