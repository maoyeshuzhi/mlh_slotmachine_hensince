package com.maoye.mlh_slotmachine.view.quickprintv2activity;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.maoye.mlh_slotmachine.R;
import com.maoye.mlh_slotmachine.bean.AdvertBean;
import com.maoye.mlh_slotmachine.bean.BaseResult;
import com.maoye.mlh_slotmachine.bean.QuickOrderDetialsBean;
import com.maoye.mlh_slotmachine.mvp.MVPBaseActivity;
import com.maoye.mlh_slotmachine.util.Constant;
import com.maoye.mlh_slotmachine.util.Toast;
import com.maoye.mlh_slotmachine.util.device.printers.PrinterUtils;
import com.maoye.mlh_slotmachine.view.goodsdetialsactivity.GoodsdetialsActivity;
import com.maoye.mlh_slotmachine.view.h5activity.H5Activity;
import com.maoye.mlh_slotmachine.view.homeactivity.GlideImageLoader;
import com.maoye.mlh_slotmachine.view.homeactivity.HomeActivity;
import com.maoye.mlh_slotmachine.view.mapguidesactivity.MapguidesActivity;
import com.maoye.mlh_slotmachine.view.searchgoodsactivity.SearchgoodsActivity;
import com.maoye.mlh_slotmachine.widget.banner.Banner;
import com.maoye.mlh_slotmachine.widget.banner.listener.OnBannerListener;
import com.printsdk.usbsdk.UsbDriver;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class Quickprintv2Activity extends MVPBaseActivity<Quickprintv2Contract.View, Quickprintv2Presenter> implements Quickprintv2Contract.View {

    @BindView(R.id.code_et)
    EditText codeEt;
    @BindView(R.id.goshop_bt)
    Button goshopBt;
    @BindView(R.id.back_bt)
    ImageView backBt;

    @BindView(R.id.time_tv)
    TextView timeTv;
    @BindView(R.id.succ_tv)
    TextView succTv;
    @BindView(R.id.time_back_tv)
    TextView timeBackTv;
    @BindView(R.id.search_goods_bt)
    Button searchGoodsBt;
    @BindView(R.id.brand_guides_bt)
    Button brandGuidesBt;
    @BindView(R.id.banner)
    Banner banner;
    @BindView(R.id.search_goods_ll)
    LinearLayout searchGoodsLl;
    @BindView(R.id.print_ll)
    LinearLayout printLl;
    private UsbManager mUsbManager;
    UsbDriver mUsbDriver;
    private static final String ACTION_USB_PERMISSION = "com.usb.sample.USB_PERMISSION";
    public static final String TIME1 = "返回首页%ds";
    public static final String TIME2 = "剩余操作时长%d秒";
    private CountDownTimer countDownTimer1, countDownTimer2;
    private String saleNo = "";
    private QuickOrderDetialsBean bean;
    private List<AdvertBean> list = new ArrayList<>();

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quickprintv2);
        ButterKnife.bind(this);
        getUsbDriverService();
        initData();

    }


    private void getUsbDriverService() {
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mUsbDriver = new UsbDriver(mUsbManager, getContext());
        PendingIntent permissionIntent1 = PendingIntent.getBroadcast(getContext(), 0,
                new Intent(ACTION_USB_PERMISSION), 0);
        mUsbDriver.setPermissionIntent(permissionIntent1);

    }

    private void initData() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(codeEt.getWindowToken(), 0);
        mPresenter.getBannerData(4);
        banner.setImageLoader(new GlideImageLoader());
        banner.start();
        countDownTimer1 = new CountDownTimer(11 * 1000, 1000) {
            @Override
            public void onTick(long l) {
                timeBackTv.setText(String.format(TIME1, l / 1000 - 1));
            }

            @Override
            public void onFinish() {
                openActivity(HomeActivity.class);

            }
        };
        countDownTimer2 = new CountDownTimer(241 * 1000, 1000) {
            @Override
            public void onTick(long l) {
                timeTv.setText(String.format(TIME2, l / 1000 - 1) + "");
            }

            @Override
            public void onFinish() {
                openActivity(HomeActivity.class);
            }
        }.start();


        codeEt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (KeyEvent.KEYCODE_ENTER == i && KeyEvent.ACTION_DOWN == keyEvent.getAction()) {
                    //处理事件
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(codeEt.getWindowToken(), 0);
                    String authCode = codeEt.getText().toString();
                    codeEt.setText("");
                    if (authCode.startsWith("saleNo=")) {
                        String[] split = authCode.split("=");
                        if (split[split.length - 1] != null && !saleNo.equals(split[split.length - 1])) {
                            try {
                                saleNo = split[split.length - 1];
                                String[] saleSpit = saleNo.split(",");
                                if (saleSpit.length == 2) {
                                    String s0 = saleSpit[0];
                                    String substring = s0.substring(0, s0.length() - 8);
                                    mPresenter.orderData(substring + saleSpit[1]);
                                } else {
                                    mPresenter.orderData(saleNo);
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.getInstance().toast(getApplicationContext(), "请勿重复打印！", 2);
                        }
                    } else {
                        codeEt.setText("");
                    }
                    return true;
                }
                return false;
            }
        });

        banner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int position) {
                if (list.size() == 0) return;
                AdvertBean advertBean = list.get(position);
                mPresenter.statisticClickNum(advertBean.getId());
                if (!TextUtils.isEmpty(advertBean.getLink_url())) {
                    //调外部链接
                    Intent intent = new Intent(Quickprintv2Activity.this, H5Activity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(Constant.KEY, advertBean.getLink_url());
                    startActivity(intent);
                } else if (advertBean.getProduct_id() != 0) {
                    Intent intent = new Intent(Quickprintv2Activity.this, GoodsdetialsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(Constant.GOODS_ID, advertBean.getProduct_id());
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        countDownTimer1.cancel();
        countDownTimer2.cancel();
    }


    @Override
    public void onSuccess(Object o) {

    }

    @Override
    public void onFail(Throwable throwable) {

    }

    @Override
    public void getOrderDetials(BaseResult<QuickOrderDetialsBean> beanBaseResult) {
        bean = beanBaseResult.getData();
        if (bean == null) {
            codeEt.setText("");
            Toast.getInstance().toast(getApplicationContext(), "不存在此订单", 2);
            return;
        }


        if (bean.getPrintCount() >= 3) {
            Toast.getInstance().toast(this, "纸质小票已打印三次，无法再打印！", 2);
            codeEt.setText("");
            return;
        }


        PrinterUtils printerUtils = PrinterUtils.getInstanse();
        if (!printerUtils.PrintConnStatus(mUsbDriver, mUsbManager)) {
            saleNo = "";
            codeEt.setText("");
            return;
        }
        boolean printTicketData = printerUtils.getPrintTicketData(mUsbDriver, bean, this);
        if (printTicketData) {
            Toast.getInstance().toast(this, Constant.PRINT_SUCC, 2);
            mPresenter.markPrint(bean.getSaleNo());
            printLl.setVisibility(View.GONE);
            codeEt.setVisibility(View.GONE);
            succTv.setVisibility(View.VISIBLE);
            timeBackTv.setVisibility(View.VISIBLE);
            mPresenter.markPrint(bean.getSaleNo());
            countDownTimer2.cancel();
            countDownTimer1.start();
            codeEt.setText("");
        } else {
            saleNo = "";
            codeEt.setText("");
        }

    }

    @Override
    public void getOrderDetialsFial(Throwable t) {
        saleNo = "";
        codeEt.setText("");
    }

    @Override
    public void getBannerData(List<AdvertBean> list) {
        this.list = list;
        List<String> stringList = new ArrayList<>();
        for (AdvertBean advertBean : list) {
            stringList.add(advertBean.getImage_url());
        }
        banner.update(stringList);
    }

    @OnClick({R.id.goshop_bt, R.id.back_bt, R.id.search_goods_bt, R.id.brand_guides_bt})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.goshop_bt:
            case R.id.back_bt:
                //finish();
                openActivity(HomeActivity.class);
                break;

            case R.id.search_goods_bt:
                openActivity(SearchgoodsActivity.class);
                break;
            case R.id.brand_guides_bt:
                openActivity(MapguidesActivity.class);
                break;
        }
    }
}
