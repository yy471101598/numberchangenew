package com.shoppay.numc.ui;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shoppay.numc.R;
import com.shoppay.numc.adapter.ZhidianDuihuanAdapter;
import com.shoppay.numc.bean.LipinMsg;
import com.shoppay.numc.bean.ZhidianMsg;
import com.shoppay.numc.card.ReadCardOptHander;
import com.shoppay.numc.db.DBAdapter;
import com.shoppay.numc.dialog.CurrChoseDialog;
import com.shoppay.numc.dialog.PwdDialog;
import com.shoppay.numc.http.ContansUtils;
import com.shoppay.numc.http.InterfaceBack;
import com.shoppay.numc.modle.ImpObtainVipMsg;
import com.shoppay.numc.modle.ImpObtainZDDHCurrency;
import com.shoppay.numc.modle.ImpObtainZDDHId;
import com.shoppay.numc.modle.ImpObtainZDDHShopmsg;
import com.shoppay.numc.modle.ImpObtainZDYuemoney;
import com.shoppay.numc.modle.ImpZDDuihuan;
import com.shoppay.numc.tools.ActivityStack;
import com.shoppay.numc.tools.CommonUtils;
import com.shoppay.numc.tools.DialogUtil;
import com.shoppay.numc.tools.LogUtils;
import com.shoppay.numc.tools.NoDoubleClickListener;
import com.shoppay.numc.tools.PreferenceHelper;
import com.shoppay.numc.tools.ToastUtils;
import com.shoppay.numc.wxcode.MipcaActivityCapture;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by songxiaotao on 2017/6/30.
 */

public class ZhidianDuihuanActivity extends BaseActivity {
    @Bind(R.id.balance_tv_n)
    TextView mBalanceTvN;
    @Bind(R.id.tv_allnum)
    TextView mTvAllnum;
    @Bind(R.id.balance_tv_z)
    TextView mBalanceTvZ;
    @Bind(R.id.tv_allmoney)
    TextView mTvAllmoney;
    @Bind(R.id.rl_duihuan)
    RelativeLayout mRlDuihuan;
    @Bind(R.id.balance_rl_d)
    RelativeLayout mBalanceRlD;
    @Bind(R.id.img_left)
    ImageView mImgLeft;
    @Bind(R.id.rl_left)
    RelativeLayout mRlLeft;
    @Bind(R.id.tv_title)
    TextView mTvTitle;
    @Bind(R.id.rl_right)
    RelativeLayout mRlRight;
    @Bind(R.id.vip_tv_card)
    TextView mVipTvCard;
    @Bind(R.id.vip_et_card)
    TextView mVipEtCard;
    @Bind(R.id.vip_tv_name)
    TextView mVipTvName;
    @Bind(R.id.tv_bizhong)
    TextView mTvBizhong;
    @Bind(R.id.et_bingzhong)
    TextView mEtBingzhong;
    @Bind(R.id.rl_currchose)
    RelativeLayout mRlCurrchose;
    @Bind(R.id.tv_yue)
    TextView mTvYue;
    @Bind(R.id.et_yue)
    TextView mEtYue;
    @Bind(R.id.vip_tv_jifennum)
    TextView mVipTvJifennum;
    @Bind(R.id.vip_tv_dingwei)
    TextView mVipTvDingwei;
    @Bind(R.id.vip_et_code)
    EditText mVipEtCode;
    @Bind(R.id.listview)
    ListView mListview;
    private boolean isSuccess = false;
    private int vipid;
    private String pwd = "";
    private int currid = -1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    try {
                        JSONObject jso = new JSONObject(msg.obj.toString());
                        vipid = jso.getInt("userid");
                        pwd = jso.getString("paypassword");
                        mVipTvName.setText(jso.getString("name"));
                        mVipEtCard.setText(jso.getString("bankcard"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    isSuccess = true;
                    break;
                case 2:
                    mVipEtCard.setText("");
                    mVipTvName.setText("");
                    isSuccess = false;
                    break;
            }
        }
    };
    private Activity ac;
    private String editString;
    private String title, entitle;
    private List<ZhidianMsg> zdlist = new ArrayList<>();
    private String shopcode;
    private ShopChangeReceiver shopChangeReceiver;
    private int num = 0;
    private int point = 0;
    private DBAdapter dbAdapter;
    private List<LipinMsg> list = new ArrayList<>();
    private ZhidianDuihuanAdapter adapter;
    private static final int CAMERA_PERMISSIONS_REQUEST_CODE = 0x03;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aactivity_zhidianduihuan);
        ButterKnife.bind(this);
        ac = this;
        dialog = DialogUtil.loadingDialog(ZhidianDuihuanActivity.this, 1);
        ActivityStack.create().addActivity(ZhidianDuihuanActivity.this);
        title = getIntent().getStringExtra("title");
        entitle = getIntent().getStringExtra("entitle");
        if (PreferenceHelper.readString(ac, "numc", "lagavage", "zh").equals("zh")) {
            mTvTitle.setText(title);
        } else {
            mTvTitle.setText(entitle);
        }
        dbAdapter = DBAdapter.getInstance(ac);
        dbAdapter.deleteJifenShopCar();
        initView();
        obtainDHzhidian("no");
        // 注册广播
        shopChangeReceiver = new ShopChangeReceiver();
        IntentFilter iiiff = new IntentFilter();
        iiiff.addAction("com.shoppay.wy.jifenduihuan");
        registerReceiver(shopChangeReceiver, iiiff);


        adapter = new ZhidianDuihuanAdapter(ac, list);
        mListview.setAdapter(adapter);
//        mVipEtCard.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                if (delayRun != null) {
//                    //每次editText有变化的时候，则移除上次发出的延迟线程
//                    handler.removeCallbacks(delayRun);
//                }
//                editString = editable.toString();
//
//                //延迟800ms，如果不再输入字符，则执行该线程的run方法
//
//                handler.postDelayed(delayRun, 800);
//            }
//        });

        mVipEtCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mEtBingzhong.getText().toString().equals(res.getString(R.string.chose))) {
                    ToastUtils.showToast(ac, res.getString(R.string.chosefkzd));
                } else {
                    if (codeRun != null) {
                        //每次editText有变化的时候，则移除上次发出的延迟线程
                        handler.removeCallbacks(codeRun);
                    }
                    shopcode = editable.toString();

                    //延迟800ms，如果不再输入字符，则执行该线程的run方法

                    handler.postDelayed(codeRun, 800);
                }
            }
        });
    }

    private Runnable codeRun = new Runnable() {

        @Override
        public void run() {
            //在这里调用服务器的接口，获取数据
            obtainDuihuanMsgByCode();
        }
    };


//    /**
//     * 延迟线程，看是否还有下一个字符输入
//     */
//    private Runnable delayRun = new Runnable() {
//
//        @Override
//        public void run() {
//            //在这里调用服务器的接口，获取数据
//            ontainVipInfo();
//        }
//    };

    private void obtainDuihuanMsgByCode() {
        ImpObtainZDDHShopmsg shopmsg = new ImpObtainZDDHShopmsg();
        shopmsg.obtainZDDHShopmsg(ac, shopcode, currid, new InterfaceBack() {
            @Override
            public void onResponse(Object response) {
                try {
                    LipinMsg lipin = new LipinMsg();
                    boolean ishas = true;
                    JSONObject jso = new JSONObject(response.toString());
                    if (list.size() == 0) {
                        ishas = false;
                    } else {
                        for (int i = 0; i < list.size(); i++) {
                            if (!list.get(i).staid.equals(jso.getString("staid"))) {
                                ishas = false;
                            }
                        }
                    }
                    if (!ishas) {
                        lipin.staid = jso.getString("staid");
                        lipin.stockcode = jso.getString("stockcode");
                        lipin.title = jso.getString("title");
                        lipin.entitle = jso.getString("entitle");
                        lipin.price = jso.getString("price");
                        lipin.stock = jso.getString("stock");
                        lipin.num = "0";
                        list.add(lipin);
                        adapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onErrorResponse(Object msg) {

            }
        });


    }

    private void obtainDHzhidian(final String type) {
        ImpObtainZDDHCurrency currency = new ImpObtainZDDHCurrency();
        currency.obtainZDDHCurrency(ac, new InterfaceBack() {
            @Override
            public void onResponse(Object response) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<ZhidianMsg>>() {
                }.getType();
                List<ZhidianMsg> sllist = gson.fromJson(response.toString(), listType);
                zdlist.addAll(sllist);
                if (type.equals("no")) {

                } else {
                    String[] tft = new String[zdlist.size()];
                    for (int i = 0; i < zdlist.size(); i++) {
                        if (PreferenceHelper.readString(ac, "numc", "lagavage", "zh").equals("zh")) {
                            tft[i] = zdlist.get(i).StockCodeName;
                        } else {
                            tft[i] = zdlist.get(i).EnStockCodeName;
                        }
                    }
                    CurrChoseDialog.currChoseDialog(ZhidianDuihuanActivity.this, tft, 2, new InterfaceBack() {
                        @Override
                        public void onResponse(Object response) {
                            for (ZhidianMsg curr : zdlist) {
                                if (PreferenceHelper.readString(ac, "numc", "lagavage", "zh").equals("zh")) {
                                    if (curr.StockCodeName.equals(response.toString())) {
                                        currid = curr.StockCodeID;
                                    }
                                } else {
                                    if (curr.EnStockCodeName.equals(response.toString())) {
                                        currid = curr.StockCodeID;
                                    }
                                }
                            }
                            mEtBingzhong.setText(response.toString());
                            mVipEtCode.setText("");
                            dbAdapter.deleteJifenShopCar();
                            dialog.show();
                            ImpObtainZDYuemoney yue = new ImpObtainZDYuemoney();
                            yue.obtainCurrency(ZhidianDuihuanActivity.this, vipid, currid, new InterfaceBack() {
                                @Override
                                public void onResponse(Object response) {
                                    dialog.dismiss();
                                    mEtYue.setText(response.toString());
                                }

                                @Override
                                public void onErrorResponse(Object msg) {
                                    mEtYue.setText("");
                                    dialog.dismiss();
                                }
                            });


                        }

                        @Override
                        public void onErrorResponse(Object msg) {

                        }
                    });

                }
            }

            @Override
            public void onErrorResponse(Object msg) {
                if (type.equals("no")) {

                } else {
                    Toast.makeText(ac, ac.getResources().getString(R.string.zdlistfalse), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
//
//    private void ontainVipInfo() {
//        ImpObtainVipMsg vipmsg = new ImpObtainVipMsg();
//        vipmsg.obtainVipMsg(ZhidianDuihuanActivity.this, editString, new InterfaceBack() {
//            @Override
//            public void onResponse(Object response) {
//                Message msg = handler.obtainMessage();
//                msg.what = 1;
//                msg.obj = response;
//                handler.sendMessage(msg);
//            }
//
//            @Override
//            public void onErrorResponse(Object msg1) {
//                Message msg = handler.obtainMessage();
//                msg.what = 2;
//                handler.sendMessage(msg);
//            }
//        });
//    }


    private void initView() {
        mRlCurrchose.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View view) {
                if (isSuccess) {

                    if (zdlist.size() == 0) {
                        obtainDHzhidian("yes");
                    } else {
                        String[] tft = new String[zdlist.size()];
                        for (int i = 0; i < zdlist.size(); i++) {
                            if (PreferenceHelper.readString(ac, "numc", "lagavage", "zh").equals("zh")) {
                                tft[i] = zdlist.get(i).StockCodeName;
                            } else {
                                tft[i] = zdlist.get(i).EnStockCodeName;
                            }
                        }
                        CurrChoseDialog.currChoseDialog(ZhidianDuihuanActivity.this, tft, 2, new InterfaceBack() {
                            @Override
                            public void onResponse(Object response) {
                                for (ZhidianMsg curr : zdlist) {
                                    if (PreferenceHelper.readString(ac, "numc", "lagavage", "zh").equals("zh")) {
                                        if (curr.StockCodeName.equals(response.toString())) {
                                            currid = curr.StockCodeID;
                                        }
                                    } else {
                                        if (curr.EnStockCodeName.equals(response.toString())) {
                                            currid = curr.StockCodeID;
                                        }
                                    }
                                }
                                mEtBingzhong.setText(response.toString());
                                mVipEtCode.setText("");
                                dbAdapter.deleteJifenShopCar();
                                dialog.show();
                                ImpObtainZDYuemoney yue = new ImpObtainZDYuemoney();
                                yue.obtainCurrency(ZhidianDuihuanActivity.this, vipid, currid, new InterfaceBack() {
                                    @Override
                                    public void onResponse(Object response) {
                                        dialog.dismiss();
                                        mEtYue.setText(response.toString());
                                    }

                                    @Override
                                    public void onErrorResponse(Object msg) {
                                        mEtYue.setText("");
                                        dialog.dismiss();
                                    }
                                });


                            }

                            @Override
                            public void onErrorResponse(Object msg) {

                            }
                        });
                    }
                } else {
                    ToastUtils.showToast(ac, res.getString(R.string.vipmsgfalse));
                }
            }
        });
        mRlRight.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View view) {
                Intent mipca = new Intent(ac, MipcaActivityCapture.class);
                startActivityForResult(mipca, 111);
            }
        });
        mVipTvDingwei.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View view) {
                Intent duihuan = new Intent(ac, MipcaActivityCapture.class);
                startActivityForResult(duihuan, 222);
                if (ContextCompat.checkSelfPermission(ac, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(ac, Manifest.permission.CAMERA)) {
                        ToastUtils.showToast(ac, "您已经拒绝过一次");
                    }
                    ActivityCompat.requestPermissions(ac, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSIONS_REQUEST_CODE);
                } else {//有权限直接调用系统相机拍照
                    Intent mipca = new Intent(ac, MipcaActivityCapture.class);
                    startActivityForResult(mipca, 111);
                }
            }
        });
        mRlDuihuan.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View view) {
                if (!isSuccess) {
                    Toast.makeText(getApplicationContext(), res.getString(R.string.inputvip),
                            Toast.LENGTH_SHORT).show();
                } else if (mEtBingzhong.getText().toString().equals(res.getString(R.string.chose))) {
                    Toast.makeText(getApplicationContext(), res.getString(R.string.chosefkzd),
                            Toast.LENGTH_SHORT).show();
                } else if (mTvAllnum.getText().toString().equals("0")) {
                    ToastUtils.showToast(ac, res.getString(R.string.choselipin));
                } else if (Double.parseDouble(mTvAllmoney.getText().toString()) > Double.parseDouble(mEtYue.getText().toString().equals("") ? "0" : mEtYue.getText().toString())) {
                    ToastUtils.showToast(ac, res.getString(R.string.xfzdbigyue));
                } else {
                    if (CommonUtils.checkNet(getApplicationContext())) {
                        PwdDialog.pwdDialog(ZhidianDuihuanActivity.this, pwd, 1, new InterfaceBack() {
                            @Override
                            public void onResponse(Object response) {
                                dialog.show();
                                ImpObtainZDDHId rechargeid = new ImpObtainZDDHId();
                                rechargeid.obtainZDDHId(ZhidianDuihuanActivity.this, new InterfaceBack() {
                                    @Override
                                    public void onResponse(Object response) {
                                        int rechargeid = -1;
                                        try {
                                            JSONObject jso = new JSONObject(response.toString());
                                            rechargeid = jso.getInt("exchangeid");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        //拼接礼品信息
                                        StringBuffer sb = new StringBuffer();
                                        List<LipinMsg> lplist = dbAdapter.getListJifenShopCar(currid + "");
                                        for (int i = 0; i < lplist.size(); i++) {
                                            if (i == 0) {
                                                if (lplist.get(0).num.equals("0") || lplist.get(0).num.equals("")) {

                                                } else {
                                                    sb.append(lplist.get(0).staid);
                                                    sb.append(",");
                                                    sb.append(lplist.get(0).num);
                                                }
                                            } else {
                                                if (lplist.get(i).num.equals("0") || lplist.get(i).num.equals("")) {

                                                } else {
                                                    sb.append("|");
                                                    sb.append(lplist.get(i).staid);
                                                    sb.append(",");
                                                    sb.append(lplist.get(i).num);
                                                }
                                            }
                                        }

                                        ImpZDDuihuan recharge = new ImpZDDuihuan();
                                        recharge.zdDuihuan(ZhidianDuihuanActivity.this, dialog, rechargeid, vipid, pwd, currid, sb.toString(), new InterfaceBack() {
                                            @Override
                                            public void onResponse(Object response) {
                                                finish();

                                            }

                                            @Override
                                            public void onErrorResponse(Object msg) {

                                            }
                                        });
                                    }

                                    @Override
                                    public void onErrorResponse(Object msg) {
                                        dialog.dismiss();
                                    }
                                });

                            }

                            @Override
                            public void onErrorResponse(Object msg) {

                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), res.getString(R.string.internet),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            //调用系统相机申请拍照权限回调
            case CAMERA_PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent mipca = new Intent(ac, MipcaActivityCapture.class);
                    startActivityForResult(mipca, 111);
                } else {

                    ToastUtils.showToast(this, "请允许打开相机！！");
                }
                break;


            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 111:
                if (resultCode == RESULT_OK) {
                    dialog.show();
                    ImpObtainVipMsg vipmsg = new ImpObtainVipMsg();
                    vipmsg.obtainVipMsg(ZhidianDuihuanActivity.this, data.getStringExtra("codedata"), new InterfaceBack() {
                        @Override
                        public void onResponse(Object response) {
                            dialog.dismiss();
                            Message msg = handler.obtainMessage();
                            msg.what = 1;
                            msg.obj = response;
                            handler.sendMessage(msg);
                        }

                        @Override
                        public void onErrorResponse(Object msg1) {
                            dialog.dismiss();
                            Message msg = handler.obtainMessage();
                            msg.what = 2;
                            handler.sendMessage(msg);
                        }
                    });
                }
                break;
            case 222:
                if (resultCode == RESULT_OK) {
                    mVipEtCode.setText(data.getStringExtra("codedata"));
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new ReadCardOptHander(new InterfaceBack() {
            @Override
            public void onResponse(Object response) {
                dialog.show();
                ImpObtainVipMsg vipmsg = new ImpObtainVipMsg();
                vipmsg.obtainVipMsg(ZhidianDuihuanActivity.this, response.toString(), new InterfaceBack() {
                    @Override
                    public void onResponse(Object response) {
                        dialog.dismiss();
                        Message msg = handler.obtainMessage();
                        msg.what = 1;
                        msg.obj = response;
                        handler.sendMessage(msg);
                    }

                    @Override
                    public void onErrorResponse(Object msg1) {
                        dialog.dismiss();
                        Message msg = handler.obtainMessage();
                        msg.what = 2;
                        handler.sendMessage(msg);
                    }
                });
            }

            @Override
            public void onErrorResponse(Object msg) {

            }
        });
    }

    @Override
    protected void onStop() {
        try {
            new ReadCardOptHander().overReadCard();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        super.onStop();
//        if (delayRun != null) {
//            //每次editText有变化的时候，则移除上次发出的延迟线程
//            handler.removeCallbacks(delayRun);
//        }
        if (codeRun != null) {
            //每次editText有变化的时候，则移除上次发出的延迟线程
            handler.removeCallbacks(codeRun);
        }
    }


    private class ShopChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("xx", "ShopChangeReceiver");
            List<LipinMsg> listss = dbAdapter.getListJifenShopCar(currid + "");
            num = 0;
            point = 0;
            for (LipinMsg shopCar : listss) {
                if (shopCar.num.equals("0")) {

                } else {
                    num = num + Integer.parseInt(shopCar.num);
                    point = point + Integer.parseInt(CommonUtils.multiply(shopCar.price, shopCar.num));
                }
            }
            mTvAllmoney.setText(point + "");
            mTvAllnum.setText(num + "");

        }
    }

    public static String getStringDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    @OnClick(R.id.rl_left)
    public void onViewClicked() {
        ActivityStack.create().finishActivity(ZhidianDuihuanActivity.class);
    }

}
