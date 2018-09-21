package com.shoppay.numc.tools;

import android.bluetooth.BluetoothAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.shoppay.numc.MyApplication;
import com.shoppay.numc.R;
import com.shoppay.numc.http.InterfaceBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.shoppay.numc.ui.BaseActivity.ac;

/**
 * Created by songxiaotao on 2018/9/21.
 */

public class NewDayinTools {
    public static void dayin(JSONObject jsonObject, InterfaceBack back) {
        try {
            if (jsonObject.getInt("printNumber") == 0) {
                back.onResponse("");
            } else {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter.isEnabled()) {
                    BluetoothUtil.connectBlueTooth(MyApplication.context);
                    List<byte[]> bytesList = new ArrayList<>();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inTargetDensity = 160;
                    options.inDensity = 160;
                    Bitmap bitmap1 = BitmapFactory.decodeResource(ac.getResources(), R.drawable.dayin, options);
                    byte[] center = ESCUtil.alignCenter();
                    byte[] nextLine = ESCUtil.nextLine(1);
                    byte[][] content = {nextLine, nextLine, nextLine, nextLine};
                    byte[] contentBytes = ESCUtil.byteMerger(content);
                    byte[][] end = {nextLine, nextLine};
                    byte[] endBytes = ESCUtil.byteMerger(content);
                    byte[][] bitmap = {nextLine, center, ESCUtil.selectBitmap(bitmap1, 33)};
                    byte[] headerBytes = ESCUtil.byteMerger(bitmap);
                    bytesList.add(headerBytes);
                    if (PreferenceHelper.readString(ac, "numc", "lagavage", "zh").equals("zh")) {
                        bytesList.add(DayinUtils.dayin(jsonObject.getString("printContent")));
                        bytesList.add(contentBytes);
                    } else {
                        bytesList.add(DayinUtils.dayin(jsonObject.getString("printContent")));
                        bytesList.add(contentBytes);
                    }
                    if (!jsonObject.getString("qrcode").equals("")) {
                        byte[][] qr = {nextLine, center, ESCUtil.getPrintQRCode(jsonObject.getString("qrcode"), 4, 3)};
                        byte[] qrBytes = ESCUtil.byteMerger(qr);
                        bytesList.add(qrBytes);
                    }
                    bytesList.add(endBytes);
                    BluetoothUtil.sendData(MergeLinearArraysUtil.mergeLinearArrays(bytesList), jsonObject.getInt("printNumber"));
                    back.onResponse("");
                } else {
                    back.onResponse("");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}