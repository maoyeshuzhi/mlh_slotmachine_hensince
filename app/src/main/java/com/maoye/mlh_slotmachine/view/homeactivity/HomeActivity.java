package com.maoye.mlh_slotmachine.view.homeactivity;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.maoye.mlh_slotmachine.R;
import com.maoye.mlh_slotmachine.apkmanager.DownLoadApk;
import com.maoye.mlh_slotmachine.bean.BaseInfo;
import com.maoye.mlh_slotmachine.bean.BaseResult;
import com.maoye.mlh_slotmachine.bean.CacheBean;
import com.maoye.mlh_slotmachine.bean.HomeBean;
import com.maoye.mlh_slotmachine.bean.SapIdBean;
import com.maoye.mlh_slotmachine.bean.VersionInfoBean;
import com.maoye.mlh_slotmachine.mvp.MVPBaseActivity;
import com.maoye.mlh_slotmachine.util.Constant;
import com.maoye.mlh_slotmachine.util.DensityUtil;
import com.maoye.mlh_slotmachine.util.DeviceInfoUtil;
import com.maoye.mlh_slotmachine.util.LogUtils;
import com.maoye.mlh_slotmachine.util.MD5;
import com.maoye.mlh_slotmachine.util.ScreenMatchDp;
import com.maoye.mlh_slotmachine.util.VersionManagerUtil;
import com.maoye.mlh_slotmachine.util.httputil.BaseRetrofit;
import com.maoye.mlh_slotmachine.util.httputil.cache.CacheUtil;
import com.maoye.mlh_slotmachine.util.httputil.subscribers.BaseObserver;
import com.maoye.mlh_slotmachine.view.cartactivity.CartActivity;
import com.maoye.mlh_slotmachine.view.goodsactivity.GoodsActivity;
import com.maoye.mlh_slotmachine.view.goodsdetialsactivity.GoodsdetialsActivity;
import com.maoye.mlh_slotmachine.view.h5activity.H5Activity;
import com.maoye.mlh_slotmachine.view.mapguidesactivity.MapguidesActivity;
import com.maoye.mlh_slotmachine.view.print_select_activity.PrintSelectActivity;
import com.maoye.mlh_slotmachine.view.quickprintv2activity.Quickprintv2Activity;
import com.maoye.mlh_slotmachine.view.searchgoodsactivity.SearchgoodsActivity;
import com.maoye.mlh_slotmachine.webservice.URL;
import com.maoye.mlh_slotmachine.widget.BadgeView;
import com.maoye.mlh_slotmachine.widget.banner.Banner;
import com.maoye.mlh_slotmachine.widget.banner.BannerConfig;
import com.maoye.mlh_slotmachine.widget.banner.ViewBanner;
import com.maoye.mlh_slotmachine.widget.banner.listener.OnBannerListener;
import com.maoye.mlh_slotmachine.widget.banner.transformer.AccordionTransformer;
import com.maoye.mlh_slotmachine.widget.banner.transformer.BackgroundToForegroundTransformer;
import com.maoye.mlh_slotmachine.widget.banner.transformer.CubeInTransformer;
import com.maoye.mlh_slotmachine.widget.banner.transformer.CubeOutTransformer;
import com.maoye.mlh_slotmachine.widget.banner.transformer.DefaultTransformer;
import com.maoye.mlh_slotmachine.widget.banner.transformer.DepthPageTransformer;
import com.maoye.mlh_slotmachine.widget.banner.transformer.FlipHorizontalTransformer;
import com.maoye.mlh_slotmachine.widget.banner.transformer.FlipVerticalTransformer;
import com.maoye.mlh_slotmachine.widget.banner.transformer.RotateUpTransformer;
import com.maoye.mlh_slotmachine.widget.banner.transformer.ScaleInOutTransformer;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class HomeActivity extends MVPBaseActivity<HomeContract.View, HomePresenter> implements HomeContract.View {

    @BindView(R.id.test)
    TextView test;

    @BindView(R.id.print_bill_bt)
    Button printBillBt;
    @BindView(R.id.brand_guides_bt)
    Button brandGuidesBt;
    @BindView(R.id.search_goods_bt)
    Button searchGoodsBt;
    @BindView(R.id.leftpage_img)
    ImageView leftpageImg;
    @BindView(R.id.rightpage_img)
    ImageView rightpageImg;

    @BindView(R.id.cart_img)
    ImageView cartImg;
    @BindView(R.id.print_ll)
    LinearLayout printLl;
    @BindView(R.id.head_banner)
    Banner headBanner;
    @BindView(R.id.banner)
    ViewBanner banner;
    private BadgeView goodsNumView;
    private List<List<HomeBean.ListBeanX>> goodList = new ArrayList<>();
    private List<HomeBean.AdvertBean> advertBeanList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        LogUtils.e("当前设备设备号：" + DeviceInfoUtil.getDeviceId());


        goodsNumView = new BadgeView(HomeActivity.this);
        goodsNumView.setStyle(1);
        goodsNumView.setTargetView(cartImg);
        goodsNumView.setBadgeMargin(10, 0, 0, 10);
        headBanner.setImageLoader(new GlideImageLoader());
        banner.setViewLoader(new HomeBrandVH());
        banner.setBannerStyle(BannerConfig.NOT_INDICATOR);
        //  banner.setPageTransformer(true, new ScaleInOutTransformer());
        banner.setPageTransformer(true, new BackgroundToForegroundTransformer());//沉浮加缩放
        //  banner.setPageTransformer(true, new CubeOutTransformer());//3d折叠(理想)
        // banner.setPageTransformer(true, new FlipHorizontalTransformer());//翻转
        banner.start();
        headBanner.start();
        headBanner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int position) {
                if (advertBeanList.size() == 0) return;
                HomeBean.AdvertBean advertBean = advertBeanList.get(position);
                mPresenter.statisticClickNum(advertBean.getId());
                if (!TextUtils.isEmpty(advertBean.getLink_url())) {
                    //调外部链接
                    Intent intent = new Intent(HomeActivity.this, H5Activity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(Constant.KEY, advertBean.getLink_url());
                    startActivity(intent);
                } else if (advertBean.getProduct_id() != 0) {
                    Intent intent = new Intent(HomeActivity.this, GoodsdetialsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(Constant.GOODS_ID, advertBean.getProduct_id());
                    startActivity(intent);
                }
            }
        });

        initData();
    }


    private void initData() {

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;     // 屏幕宽度（像素）
        int height = metric.heightPixels;   // 屏幕高度（像素）
        float density = metric.density;      // 屏幕密度（0.75 / 1.0 / 1.5）
        int densityDpi = metric.densityDpi;  // 屏幕密度DPI（120 / 160 / 240）
        ScreenMatchDp.screen(0);

        Log.e("tag", "width: "+width +"height "+height +"density: " +density+"densityDpi:" +densityDpi);
        test.setText("width: "+width +"height "+height +"density: " +density+"densityDpi:" +densityDpi);

        if (BaseInfo.getSapId() == null) baseInfo();
        Object query = CacheUtil.query(CacheUtil.HOME_ACTIVITY, HomeBean.class);
        if (query != null) {
            HomeBean bean = (HomeBean) query;
            goodsNumView.setBadgeCount(bean.getCartNum());
            goodsData(bean);
            getHeadBannerData(bean);
        }
    }

    public void baseInfo() {
        Observable<BaseResult<SapIdBean>> baseResultObservable = BaseRetrofit.getInstance().mServletApi.querySapId();
        baseResultObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(20)
                .subscribe(new BaseObserver<BaseResult<SapIdBean>>(getApplicationContext(), false) {
                    @Override
                    protected void onBaseNext(BaseResult<SapIdBean> data) {
                        BaseInfo.setSapId(data.getData().getSap_id());
                        BaseInfo.setStoreName(data.getData().getShop_name());
                    }

                    @Override
                    protected void onBaseError(Throwable t) {

                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.homedata(true);
        mPresenter.versionInfo();

    }

    @OnClick({R.id.print_bill_bt, R.id.leftpage_img, R.id.rightpage_img, R.id.cart_img, R.id.search_goods_bt, R.id.brand_guides_bt})
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.print_bill_bt:
                //补打小票
                openActivity(Quickprintv2Activity.class);
              //  openActivity(PrintSelectActivity.class);
                break;
            case R.id.leftpage_img:
                if (goodList.size() > 0)
                    banner.scrollLeft();
                break;
            case R.id.rightpage_img:
                if (goodList.size() > 0)
                    banner.scrollRight();
                break;
            case R.id.cart_img:
                //购物车
                openActivity(CartActivity.class);
                break;
            case R.id.search_goods_bt:
                openActivity(SearchgoodsActivity.class);
                break;
            case R.id.brand_guides_bt:
                openActivity(MapguidesActivity.class);
                break;
        }
    }


    /**
     * goods banner
     *
     * @param data
     */
    private void goodsData(HomeBean data) {
        goodList = mPresenter.handerGoodsData(data.getList());
        if (goodList.size() > 1) {
            rightpageImg.setVisibility(View.VISIBLE);
            leftpageImg.setVisibility(View.VISIBLE);
        } else {
            rightpageImg.setVisibility(View.GONE);
            leftpageImg.setVisibility(View.GONE);
        }
        if (goodList.size() == 0) {
            return;
        }
        banner.update(goodList);

    }


    private void getHeadBannerData(HomeBean data) {
        advertBeanList = data.getAdvert();
        List<String> picList = new ArrayList<>();
        for (HomeBean.AdvertBean advertBean : advertBeanList) {
            picList.add(advertBean.getImage_url());
        }
        headBanner.update(picList);
    }

    @Override
    public void onSuccess(Object o) {
        if (o == null) return;
        HomeBean data = (HomeBean) o;
        goodsNumView.setBadgeCount(data.getCartNum());
        goodsData(data);
        getHeadBannerData(data);

        String json = new Gson().toJson(data).toString();
        CacheBean bean = new CacheBean();
        bean.setId(CacheUtil.HOME_ACTIVITY_ID);
        bean.setJsonUrl(json);
        bean.setName(CacheUtil.HOME_ACTIVITY);
        CacheUtil.put(bean);
    }

    @Override
    public void onFail(Throwable throwable) {
    }

    @Override
    public void onBackPressed() {
        //  super.onBackPressed();
    }

    @Override
    public void getVersionInfo(VersionInfoBean versionInfoBean) {
        if (versionInfoBean != null && versionInfoBean.getVersionName() != null && !versionInfoBean.getVersionName().equals(VersionManagerUtil.getVersion(this))) {
            DownLoadApk.download(this, URL.APK_LOADDOWN, "茂乐惠机", "mlhj");

        }
    }
}
