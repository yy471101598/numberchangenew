package com.shoppay.numc.modle;

import android.app.Activity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.shoppay.numc.http.ContansUtils;
import com.shoppay.numc.http.InterfaceBack;
import com.shoppay.numc.tools.LogUtils;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by songxiaotao on 2018/9/5.
 */

public class ImpObtainZDDkQIxian {
    public void obtainZdDkQixian(final Activity ac,String StockCode,
                               final InterfaceBack back) {

        AsyncHttpClient client = new AsyncHttpClient();
//        final PersistentCookieStore myCookieStore = new PersistentCookieStore(ac);
//        client.setCookieStore(myCookieStore);
        RequestParams params = new RequestParams();
        params.put("StockCode", StockCode);
        client.post(ContansUtils.BASE_URL + "pos/CoinLoanMaturity.ashx",params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    LogUtils.d("xxCunqiS", new String(responseBody, "UTF-8"));
                    JSONObject jso = new JSONObject(new String(responseBody, "UTF-8"));
                    back.onResponse(jso.getString("vdata"));
                } catch (Exception e) {
                    back.onErrorResponse("");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                back.onErrorResponse("");
            }
        });
    }
}
