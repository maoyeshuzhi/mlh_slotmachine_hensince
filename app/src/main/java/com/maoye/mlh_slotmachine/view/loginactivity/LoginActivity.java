package com.maoye.mlh_slotmachine.view.loginactivity;


import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.maoye.mlh_slotmachine.R;
import com.maoye.mlh_slotmachine.bean.AdvertBean;
import com.maoye.mlh_slotmachine.mvp.MVPBaseActivity;
import com.maoye.mlh_slotmachine.util.Constant;
import com.maoye.mlh_slotmachine.util.TextUtil;
import com.maoye.mlh_slotmachine.util.Toast;
import com.maoye.mlh_slotmachine.view.confirmorderactivity.ConfirmOrderActivity;
import com.maoye.mlh_slotmachine.view.goodsdetialsactivity.GoodsdetialsActivity;
import com.maoye.mlh_slotmachine.view.h5activity.H5Activity;
import com.maoye.mlh_slotmachine.view.homeactivity.GlideImageLoader;
import com.maoye.mlh_slotmachine.widget.banner.Banner;
import com.maoye.mlh_slotmachine.widget.banner.listener.OnBannerListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.functions.Function;


public class LoginActivity extends MVPBaseActivity<LoginContract.View, LoginPresenter> implements LoginContract.View, RadioGroup.OnCheckedChangeListener {

    @BindView(R.id.accountlogin_rb)
    RadioButton accountloginRb;
    @BindView(R.id.phonelogin_rb)
    RadioButton phoneloginRb;
    @BindView(R.id.rg)
    RadioGroup rg;
    @BindView(R.id.account_et)
    EditText accountEt;
    @BindView(R.id.account_view)
    View accountView;
    @BindView(R.id.psw_et)
    EditText pswEt;
    @BindView(R.id.psw_view)
    View pswView;
    @BindView(R.id.phone_et)
    EditText phoneEt;
    @BindView(R.id.phone_view)
    View phoneView;
    @BindView(R.id.login_bt)
    Button loginBt;
    @BindView(R.id.back_imgbt)
    ImageButton backImgbt;
    @BindView(R.id.login_tv)
    TextView loginTv;
    @BindView(R.id.banner)
    Banner banner;
    @BindView(R.id.verifi_code_tv)
    TextView verifiCodeTv;
    @BindView(R.id.phone_ll)
    LinearLayout phoneLl;
    @BindView(R.id.verrifi_code_et)
    EditText verrifiCodeEt;
    private CountDownTimer timer;

    private int loginType;
    public static final int ACCOUNT_LOIGN = 0;
    public static final int MOBILE_LOIGN = 1;//手机登录
    private List<AdvertBean> list = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        initData();
        timer = new CountDownTimer(Constant.countDownTime,1000) {
            @Override
            public void onTick(long l) {
               long ontickTime =  l/1000;
                verifiCodeTv.setText(String.format("%ds",ontickTime - 1));
            }

            @Override
            public void onFinish() {
                verifiCodeTv.setText("获取验证码");
                verifiCodeTv.setEnabled(true);
                verifiCodeTv.setClickable(true);
            }
        };
    }

    protected void initData() {
        mPresenter.getBannerData(1);
        banner.setImageLoader(new GlideImageLoader());
        banner.start();
        rg.setOnCheckedChangeListener(this);
        accountEt.addTextChangedListener(accountTextWatcher);
        pswEt.addTextChangedListener(pswTextWatcher);
        phoneEt.addTextChangedListener(phoneTw);
        verrifiCodeEt.addTextChangedListener(verifiCodeTw);

        banner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int position) {
                if (list.size() == 0) return;
                AdvertBean advertBean = list.get(position);
                mPresenter.statisticClickNum(advertBean.getId());
                if (!TextUtils.isEmpty(advertBean.getLink_url())) {
                    //调外部链接
                    Intent intent = new Intent(LoginActivity.this, H5Activity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(Constant.KEY, advertBean.getLink_url());
                    startActivity(intent);
                } else if (advertBean.getProduct_id() != 0) {
                    Intent intent = new Intent(LoginActivity.this, GoodsdetialsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(Constant.GOODS_ID, advertBean.getProduct_id());
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        pswEt.setText("");
        accountEt.setText("");
        phoneEt.setText("");
    }

    TextWatcher verifiCodeTw = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            isCanLogin(loginType == MOBILE_LOIGN && editable.length() > 0&&!TextUtils.isEmpty(phoneEt.getText()));

        }
    };

    TextWatcher phoneTw = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            isCanLogin(loginType == MOBILE_LOIGN && editable.length() > 0&&!TextUtils.isEmpty(verifiCodeTv.getText()));
        }
    };


    TextWatcher accountTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if(!TextUtils.isEmpty(editable)&&editable.length()==11){
                pswEt.requestFocus();
            }
            isCanLogin(loginType == ACCOUNT_LOIGN && editable.length() > 0 && !TextUtils.isEmpty(pswEt.getText()));
        }
    };
    TextWatcher pswTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            isCanLogin(loginType == ACCOUNT_LOIGN && editable.length() > 0 && !TextUtils.isEmpty(accountEt.getText()));
        }
    };

    private void isCanLogin(boolean iscanlogin) {
        if (iscanlogin) {
            loginTv.setVisibility(View.GONE);
            loginBt.setVisibility(View.VISIBLE);
            loginBt.setClickable(true);
            loginBt.setEnabled(true);
        } else {
            loginBt.setVisibility(View.GONE);
            loginTv.setVisibility(View.VISIBLE);
            loginBt.setClickable(false);
            loginBt.setEnabled(false);
        }
    }


    @Override
    public void onSuccess(Object o) {
        Intent intent = new Intent(this, ConfirmOrderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constant.KEY, getIntent().getSerializableExtra(Constant.KEY));
        intent.putExtra(Constant.FROM, getIntent().getIntExtra(Constant.FROM, 0));
        startActivity(intent);
    }

    @Override
    public void onFail(Throwable throwable) {

    }

    @OnClick({R.id.login_bt, R.id.back_imgbt,R.id.verifi_code_tv})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_bt:
                if (islogin()) {
                    if (loginType == ACCOUNT_LOIGN)
                        mPresenter.accountLogin(accountEt.getText().toString(), pswEt.getText().toString());
                    if (loginType == MOBILE_LOIGN)
                        mPresenter.mobileLogin(phoneEt.getText().toString(), 2,verrifiCodeEt.getText()+"");
                }
                break;
            case R.id.back_imgbt:
                finish();
                break;
            case R.id.verifi_code_tv:
                if(TextUtils.isEmpty(phoneEt.getText())){
                    Toast.getInstance().toast(getApplicationContext(),"请输入手机号码",2);
                    break;
                }
                if(!TextUtil.isMobile(phoneEt.getText()+"")){
                    Toast.getInstance().toast(getApplicationContext(),"请输入正确的手机号码",2);
                    break;
                }
                mPresenter.getVerrifiCode(phoneEt.getText().toString());
                break;
        }
    }

    private boolean islogin() {
        if (loginType == MOBILE_LOIGN && !TextUtil.isMobile(phoneEt.getText().toString())) {
            Toast.getInstance().toast(this, Constant.PLEASE_INPUT_RIGHT_PHONE, 2);
            return false;
        }
        if (loginType == ACCOUNT_LOIGN && !TextUtil.isMobile(accountEt.getText() + "")) {
            Toast.getInstance().toast(this, Constant.PLEASE_INPUT_RIGHT_PHONE, 2);
            return false;
        }
        return true;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (i) {
            case R.id.accountlogin_rb:
                loginType = ACCOUNT_LOIGN;
                switchLoginState(true);

                break;
            case R.id.phonelogin_rb:
                loginType = MOBILE_LOIGN;
                switchLoginState(false);
                break;
        }
    }

    private void switchLoginState(boolean isAccount) {
        if (isAccount) {
            accountloginRb.setTextColor(getResources().getColor(R.color.white));
            phoneloginRb.setTextColor(getResources().getColor(R.color.color_1e1e1e));
            phoneLl.setVisibility(View.GONE);
            verrifiCodeEt.setVisibility(View.GONE);
            phoneView.setVisibility(View.GONE);
            accountEt.setVisibility(View.VISIBLE);
            accountView.setVisibility(View.VISIBLE);
            pswEt.setVisibility(View.VISIBLE);
            pswView.setVisibility(View.VISIBLE);
        } else {
            accountloginRb.setTextColor(getResources().getColor(R.color.color_1e1e1e));
            phoneloginRb.setTextColor(getResources().getColor(R.color.white));
            verrifiCodeEt.setVisibility(View.VISIBLE);
            phoneLl.setVisibility(View.VISIBLE);
            phoneView.setVisibility(View.VISIBLE);
            accountEt.setVisibility(View.GONE);
            accountView.setVisibility(View.GONE);
            pswEt.setVisibility(View.GONE);
            pswView.setVisibility(View.GONE);
        }
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

    @Override
    public void getVerifiCode() {
        timer.start();
        verifiCodeTv.setClickable(false);
        verifiCodeTv.setEnabled(false);
    }


}
