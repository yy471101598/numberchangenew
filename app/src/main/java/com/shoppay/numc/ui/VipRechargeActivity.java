package com.shoppay.numc.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.shoppay.numc.MyApplication;
import com.shoppay.numc.R;
import com.shoppay.numc.bean.VipInfo;
import com.shoppay.numc.bean.VipInfoMsg;
import com.shoppay.numc.card.ReadCardOpt;
import com.shoppay.numc.http.InterfaceBack;
import com.shoppay.numc.modle.ImpObtainCurrency;
import com.shoppay.numc.modle.ImpObtainPaytype;
import com.shoppay.numc.modle.ImpObtainRechargeId;
import com.shoppay.numc.modle.ImpObtainVipMsg;
import com.shoppay.numc.modle.ImpVipRecharge;
import com.shoppay.numc.nbean.Currency;
import com.shoppay.numc.nbean.PayType;
import com.shoppay.numc.tools.ActivityStack;
import com.shoppay.numc.tools.BluetoothUtil;
import com.shoppay.numc.tools.CommonUtils;
import com.shoppay.numc.tools.DateUtils;
import com.shoppay.numc.tools.DayinUtils;
import com.shoppay.numc.tools.DialogUtil;
import com.shoppay.numc.tools.LogUtils;
import com.shoppay.numc.tools.NoDoubleClickListener;
import com.shoppay.numc.tools.PreferenceHelper;
import com.shoppay.numc.tools.UrlTools;
import com.shoppay.numc.view.MyGridViews;
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
import cz.msebera.android.httpclient.Header;

/**
 * Created by songxiaotao on 2017/6/30.
 */

public class VipRechargeActivity extends BaseActivity  {
    @Bind(R.id.rl_left)
    RelativeLayout rlLeft;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.rl_right)
    RelativeLayout rlRight;
    @Bind(R.id.viprecharge_tv_cardnum)
    TextView viprechargeTvCardnum;
    @Bind(R.id.viprecharge_et_cardnum)
    EditText viprechargeEtCardnum;
    @Bind(R.id.viprecharge_tv_name)
    TextView viprechargeTvName;
    @Bind(R.id.viprecharge_et_name)
    TextView viprechargeEtName;
    @Bind(R.id.viprecharge_tv_bizhong)
    TextView viprechargeTvBizhong;
    @Bind(R.id.viprecharge_et_bingzhong)
    TextView viprechargeEtBingzhong;
    @Bind(R.id.viprecharge_tv_yue)
    TextView viprechargeTvYue;
    @Bind(R.id.viprecharge_et_yue)
    TextView viprechargeEtYue;
    @Bind(R.id.rb_1)
    RadioButton rb1;
    @Bind(R.id.rb_2)
    RadioButton rb2;
    @Bind(R.id.rb_3)
    RadioButton rb3;
    @Bind(R.id.rb_4)
    RadioButton rb4;
    @Bind(R.id.radiogroup)
    RadioGroup radiogroup;
    @Bind(R.id.vip_tv_money)
    TextView vipTvMoney;
    @Bind(R.id.et_money)
    EditText etMoney;
    @Bind(R.id.consumption_rl_money)
    RelativeLayout consumptionRlMoney;
    @Bind(R.id.viprecharge_rl_recharge)
    RelativeLayout viprechargeRlRecharge;
    private boolean isSuccess=false;
    private int vipid;
    private int currid;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    try {
                        JSONObject jso = new JSONObject(msg.obj.toString());
                        vipid=jso.getInt("userid");
                        viprechargeTvName.setText(jso.getString("name"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    isSuccess = true;
                    break;
                case 2:
                    viprechargeTvName.setText("");
                    isSuccess = false;
                    break;
            }
        }
    };
    private MyApplication app;
    private Activity ac;
    private String editString;
    private List<PayType> paylist = new ArrayList<>();
    private List<Currency> currlist = new ArrayList<>();
    private PayType paytype;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aactivity_viprecharge);
        ButterKnife.bind(this);
        ac = this;
        app = (MyApplication) getApplication();
        if (app.getPayType().size() == 0) {
            ImpObtainPaytype paytype = new ImpObtainPaytype();
            paytype.obtainPayType(ac, new InterfaceBack() {
                @Override
                public void onResponse(Object response) {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<PayType>>() {
                    }.getType();
                    List<PayType> sllist = gson.fromJson(response.toString(), listType);
                    paylist.addAll(sllist);
                    handlePayType(paylist);
                }

                @Override
                public void onErrorResponse(Object msg) {

                }
            });

        } else {
            paylist.addAll(app.getPayType());
            handlePayType(paylist);
        }

        if (app.getCurrency().size() == 0) {
            ImpObtainCurrency currency = new ImpObtainCurrency();
            currency.obtainCurrency(ac, new InterfaceBack() {
                @Override
                public void onResponse(Object response) {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<Currency>>() {
                    }.getType();
                    List<Currency> sllist = gson.fromJson(response.toString(), listType);
                    currlist.addAll(sllist);
                }

                @Override
                public void onErrorResponse(Object msg) {

                }
            });
        } else {
            currlist.addAll(app.getCurrency());
        }
        dialog = DialogUtil.loadingDialog(VipRechargeActivity.this, 1);
        ActivityStack.create().addActivity(VipRechargeActivity.this);
        initView();
        viprechargeTvCardnum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (delayRun != null) {
                    //每次editText有变化的时候，则移除上次发出的延迟线程
                    handler.removeCallbacks(delayRun);
                }
                editString = editable.toString();

                //延迟800ms，如果不再输入字符，则执行该线程的run方法

                handler.postDelayed(delayRun, 800);
            }
        });
    }

    private void handlePayType(List<PayType> paylist) {
        switch (paylist.size()) {
            case 1:
                rb1.setVisibility(View.VISIBLE);
                paytype=paylist.get(0);
                rb1.setChecked(true);
                if (PreferenceHelper.readString(ac, "numc", "lagavage", "zh").equals("zh")) {
                    rb1.setText(paylist.get(0).PayTypeName);
                } else {
                    rb1.setText(paylist.get(0).EnPayTypeName);
                }
                break;
            case 2:
                rb1.setVisibility(View.VISIBLE);
                rb2.setVisibility(View.VISIBLE);
                paytype=paylist.get(0);
                rb1.setChecked(true);
                if (PreferenceHelper.readString(ac, "numc", "lagavage", "zh").equals("zh")) {
                    rb1.setText(paylist.get(0).PayTypeName);
                    rb2.setText(paylist.get(1).PayTypeName);
                } else {
                    rb1.setText(paylist.get(0).EnPayTypeName);
                    rb2.setText(paylist.get(1).EnPayTypeName);
                }
                break;
            case 3:
                rb1.setVisibility(View.VISIBLE);
                rb2.setVisibility(View.VISIBLE);
                rb3.setVisibility(View.VISIBLE);
                rb1.setChecked(true);
                paytype=paylist.get(0);
                if (PreferenceHelper.readString(ac, "numc", "lagavage", "zh").equals("zh")) {
                    rb1.setText(paylist.get(0).PayTypeName);
                    rb2.setText(paylist.get(1).PayTypeName);
                    rb3.setText(paylist.get(2).PayTypeName);
                } else {
                    rb1.setText(paylist.get(0).EnPayTypeName);
                    rb2.setText(paylist.get(1).EnPayTypeName);
                    rb3.setText(paylist.get(2).EnPayTypeName);
                }
                break;
            case 4:
                rb1.setVisibility(View.VISIBLE);
                rb2.setVisibility(View.VISIBLE);
                rb3.setVisibility(View.VISIBLE);
                rb4.setVisibility(View.VISIBLE);
                paytype=paylist.get(0);
                rb1.setChecked(true);
                if (PreferenceHelper.readString(ac, "numc", "lagavage", "zh").equals("zh")) {
                    rb1.setText(paylist.get(0).PayTypeName);
                    rb2.setText(paylist.get(1).PayTypeName);
                    rb3.setText(paylist.get(2).PayTypeName);
                    rb4.setText(paylist.get(3).PayTypeName);
                } else {
                    rb1.setText(paylist.get(0).EnPayTypeName);
                    rb2.setText(paylist.get(1).EnPayTypeName);
                    rb3.setText(paylist.get(2).EnPayTypeName);
                    rb4.setText(paylist.get(3).EnPayTypeName);
                }
                break;
        }
    }

    /**
     * 延迟线程，看是否还有下一个字符输入
     */
    private Runnable delayRun = new Runnable() {

        @Override
        public void run() {
            //在这里调用服务器的接口，获取数据
            ontainVipInfo();
        }
    };

    private void ontainVipInfo() {
        ImpObtainVipMsg vipmsg=new ImpObtainVipMsg();
        vipmsg.obtainVipMsg(VipRechargeActivity.this, editString, new InterfaceBack() {
            @Override
            public void onResponse(Object response) {
                Message msg = handler.obtainMessage();
                msg.what = 1;
                msg.obj = response;
                handler.sendMessage(msg);
            }

            @Override
            public void onErrorResponse(Object msg1) {
                Message msg = handler.obtainMessage();
                msg.what = 2;
                handler.sendMessage(msg);
            }
        });
    }


    private void initView() {

        radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.rb_1:
                       paytype=paylist.get(0);
                        break;
                    case R.id.rb_2:
                        paytype=paylist.get(1);
                        break;
                    case R.id.rb_3:
                        paytype=paylist.get(2);
                        break;
                    case R.id.rb_4:
                        paytype=paylist.get(4);
                        break;
                }
            }
        });

        rlRight.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View view) {
                Intent mipca = new Intent(ac, MipcaActivityCapture.class);
                startActivityForResult(mipca, 111);
            }
        });
        viprechargeRlRecharge.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View view) {
                if (!isSuccess) {
                    Toast.makeText(getApplicationContext(), "请输入会员卡号",
                            Toast.LENGTH_SHORT).show();
                } else if (etMoney.getText().toString() == null || etMoney.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "请输入充值金额",
                            Toast.LENGTH_SHORT).show();
                } else if(viprechargeTvBizhong.getText().toString().equals("请选择")){
                    Toast.makeText(getApplicationContext(), "请选择充值币种",
                            Toast.LENGTH_SHORT).show();
                }else {
                        if (CommonUtils.checkNet(getApplicationContext())) {
                            ImpObtainRechargeId rechargeid=new ImpObtainRechargeId();
                            rechargeid.obtainRechargeId(VipRechargeActivity.this, new InterfaceBack() {
                                @Override
                                public void onResponse(Object response) {
                                    int rechargeid=-1;
                                    try {
                                        JSONObject jso = new JSONObject(response.toString());
                                      rechargeid=jso.getInt("rechargeid");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    dialog.show();
                                    ImpVipRecharge recharge=new ImpVipRecharge();
                                    recharge.vipRecharge(VipRechargeActivity.this, dialog, rechargeid, vipid, "", currid, paytype.PayTypeID, Double.parseDouble(etMoney.getText().toString()), new InterfaceBack() {
                                        @Override
                                        public void onResponse(Object response) {
                                            //打印
//                                            if (jsonObject.getInt("printNumber") == 0) {
//                                                finish();
//                                            } else {
//                                                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//                                                if (bluetoothAdapter.isEnabled()) {
//                                                    BluetoothUtil.connectBlueTooth(MyApplication.context);
//                                                    BluetoothUtil.sendData(DayinUtils.dayin(jsonObject.getString("printContent")), jsonObject.getInt("printNumber"));
//                                                    ActivityStack.create().finishActivity(VipRechargeActivity.class);
//                                                } else {
//                                                    ActivityStack.create().finishActivity(VipRechargeActivity.class);
//                                                }
//                                            }
                                        }

                                        @Override
                                        public void onErrorResponse(Object msg) {

                                        }
                                    });
                                }

                                @Override
                                public void onErrorResponse(Object msg) {

                                }
                            });

                        } else {
                            Toast.makeText(getApplicationContext(), "请检查网络是否可用",
                                    Toast.LENGTH_SHORT).show();
                        }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 111:
                if (resultCode == RESULT_OK) {
                    viprechargeEtCardnum.setText(data.getStringExtra("codedata"));
                }
                break;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        new ReadCardOpt(viprechargeEtCardnum);
    }

    @Override
    protected void onStop() {
        try {
            new ReadCardOpt().overReadCard();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        super.onStop();
        if (delayRun != null) {
            //每次editText有变化的时候，则移除上次发出的延迟线程
            handler.removeCallbacks(delayRun);
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
        ActivityStack.create().finishActivity(VipRechargeActivity.class);
    }

}
