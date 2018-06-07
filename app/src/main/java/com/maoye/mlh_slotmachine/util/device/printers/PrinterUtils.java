package com.maoye.mlh_slotmachine.util.device.printers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.icu.text.DecimalFormat;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.maoye.mlh_slotmachine.R;
import com.maoye.mlh_slotmachine.bean.BaseInfo;
import com.maoye.mlh_slotmachine.bean.BaseResult;
import com.maoye.mlh_slotmachine.bean.OrderDetialBean;
import com.maoye.mlh_slotmachine.bean.QuickOrderDetialsBean;
import com.maoye.mlh_slotmachine.util.CodeUtils;
import com.maoye.mlh_slotmachine.util.Constant;
import com.maoye.mlh_slotmachine.util.DateUtils;
import com.maoye.mlh_slotmachine.util.LogUtils;
import com.maoye.mlh_slotmachine.util.MD5;
import com.maoye.mlh_slotmachine.util.MyContext;
import com.maoye.mlh_slotmachine.util.TextUtil;
import com.maoye.mlh_slotmachine.util.httputil.BaseRetrofit;
import com.maoye.mlh_slotmachine.util.httputil.subscribers.BaseObserver;
import com.maoye.mlh_slotmachine.webservice.EnvConfig;
import com.maoye.mlh_slotmachine.webservice.URL;
import com.printsdk.cmd.PrintCmd;
import com.printsdk.usbsdk.UsbDriver;

import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Rs on 2018/4/28.
 */

public class PrinterUtils {
    public static PrinterUtils printerUtils;

    public PrinterUtils() {
    }

    public static synchronized PrinterUtils getInstanse() {
        if (printerUtils == null) {
            printerUtils = new PrinterUtils();
        }
        return printerUtils;
    }

    private int align = 0;
    private int underLine = 0;
    private int linespace = 50;// 默认40, 常用：30 40 50 60
    public int cutter = 0;       // 默认0，  0 全切、1 半切
    private int rotate = 0; // 默认为:0, 0 正常、1 90度旋转

    public UsbDevice mUsbDev1;
    public UsbDevice mUsbDev2;

    // 检测打印机状态
    private int getPrinterStatus(UsbDevice usbDev, UsbDriver mUsbDriver) {
        int iRet = -1;

        byte[] bRead1 = new byte[1];
        byte[] bWrite1 = PrintCmd.GetStatus1();
        if (mUsbDriver.read(bRead1, bWrite1, usbDev) > 0) {
            iRet = PrintCmd.CheckStatus1(bRead1[0]);
        }

        if (iRet != 0)
            return iRet;

        byte[] bRead2 = new byte[1];
        byte[] bWrite2 = PrintCmd.GetStatus2();
        if (mUsbDriver.read(bRead2, bWrite2, usbDev) > 0) {
            iRet = PrintCmd.CheckStatus2(bRead2[0]);
        }

        if (iRet != 0)
            return iRet;

        byte[] bRead3 = new byte[1];
        byte[] bWrite3 = PrintCmd.GetStatus3();
        if (mUsbDriver.read(bRead3, bWrite3, usbDev) > 0) {
            iRet = PrintCmd.CheckStatus3(bRead3[0]);
        }

        if (iRet != 0)
            return iRet;

        byte[] bRead4 = new byte[1];
        byte[] bWrite4 = PrintCmd.GetStatus4();
        if (mUsbDriver.read(bRead4, bWrite4, usbDev) > 0) {
            iRet = PrintCmd.CheckStatus4(bRead4[0]);
        }

        return iRet;
    }


    public String printData(OrderDetialBean bean) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("店员号：");
        buffer.append("茂乐惠机" + "\n");
        buffer.append("会员号：");
        buffer.append(bean.getMobile() + "" + "\n");
        buffer.append("订单号：");
        buffer.append(bean.getOrder_no() + "\n");
        buffer.append("下单时间：");
        buffer.append(bean.getCreated_at() + "" + "\n");
        buffer.append("打印时间：");
        buffer.append(DateUtils.format(System.currentTimeMillis(), DateUtils.Pattern.YYYY_MM_DD_HH_MM_SS) + "\n");
        buffer.append("本单所获积分：");
        buffer.append(bean.getGet_points_total() + "分" + "\n");
        buffer.append("流水号:");
        buffer.append(bean.getOut_trade_no() + "\n");
        buffer.append(Constant.DOT_LINE);
        for (OrderDetialBean.ProductListBean productListBean : bean.getProduct_list()) {
            buffer.append(productListBean.getProduct_name() + "\n");
            buffer.append("x" + productListBean.getNum() + "                      " + Double.valueOf(productListBean.getPrice()) * productListBean.getNum() + "元" + "\n");
        }
        //商品信息
        double goodsPrice = Double.valueOf(bean.getPaid_amount()) - Double.valueOf(bean.getFreight_amount());
        buffer.append(Constant.DOT_LINE);
        buffer.append("商品合计：              " + String.format("%.2f", goodsPrice) + "元" + "\n");
        if (Double.valueOf(bean.getFreight_amount()) != 0) {
            buffer.append("邮费：                  " + String.format("%.2f", Double.valueOf(bean.getFreight_amount())) + "元" + "\n");
        }
        buffer.append("优惠金额：              " + String.format("%.2f", Double.valueOf(bean.getDiscount_amount())) + "元" + "\n");
        buffer.append("应付金额：              " + bean.getPaid_amount() + "元" + "\n");
        if (bean.getPayment_type() == 1) {
            buffer.append("实付金额(微信支付)：         " + bean.getPaid_amount() + "元" + "\n");
        } else {
            buffer.append("实付金额(支付宝支付)：       " + bean.getPaid_amount() + "元" + "\n");
        }
        buffer.append(Constant.DOT_LINE);
        return buffer.toString();

    }


    /**
     * @param mUsbDriver
     * @param bean
     * @param context
     * @param from       来源 :0本机支付  1：快付
     */
    public boolean getPrintTicketData(UsbDriver mUsbDriver, OrderDetialBean bean, Context context, int from) {
        int iStatus = getPrinterStatus(mUsbDev1, mUsbDriver);
        if (checkStatus(iStatus) != 0)
            return false;

        try {
            mUsbDriver.write(PrintCmd.SetClean(), mUsbDev1);  // 初始化，清理缓存
            getCommonSettings(mUsbDev1, mUsbDriver);
            // 小票标题
            mUsbDriver.write(PrintCmd.SetBold(1), mUsbDev1);//0 不加粗、1 加粗
            mUsbDriver.write(PrintCmd.SetAlignment(1), mUsbDev1);//0 左、1 居中、2 右 对齐

            mUsbDriver.write(PrintCmd.PrintString(Constant.PRINT_TITLE, 0), mUsbDev1);
            mUsbDriver.write(PrintCmd.SetAlignment(0), mUsbDev1);
            mUsbDriver.write(PrintCmd.SetSizetext(0, 0), mUsbDev1);
            mUsbDriver.write(PrintCmd.PrintString(Constant.DOT_LINE, 1), mUsbDev1);

            // 小票主要内容
            mUsbDriver.write(PrintCmd.SetBold(0), mUsbDev1);
            mUsbDriver.write(PrintCmd.PrintString(printData(bean), 0), mUsbDev1);
            //  mUsbDriver.write(PrintCmd.PrintFeedline(0), mUsbDev1); // 打印走纸0行
            mUsbDriver.write(PrintCmd.SetLinespace(6), mUsbDev1);
            mUsbDriver.write(PrintCmd.SetAlignment(1), mUsbDev1);
            //打印发票二维码
            printCode(mUsbDriver, bean, context);
            // 走纸换行、切纸、清理缓存
            mUsbDriver.write(PrintCmd.PrintFeedline(3), mUsbDev1);
            SetFeedCutClean(cutter, mUsbDev1, mUsbDriver);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private void printCode(UsbDriver mUsbDriver, OrderDetialBean bean, Context context) {
        mUsbDriver.write(PrintCmd.SetLinespace(0));
        String url = EnvConfig.instance().getBaseUkfUrl() + "h5/index.html#/invoice?orderAmount=" + bean.getOrder_amount() +
                "&orderId=" + bean.getOrder_id() + "&orderNo=" + bean.getOrder_no() +
                "&key=" + MD5.MD5(bean.getOrder_no() + bean.getOrder_amount() + "maoye_mlhj" + bean.getOrder_id());
        View view = LayoutInflater.from(context).inflate(R.layout.layout_printcode, null);
        ImageView code1_img = view.findViewById(R.id.code1_img);
        ImageView code2_img = view.findViewById(R.id.code2_img);
        code1_img.setImageBitmap(CodeUtils.createQRCode(url, 205, 0));
        code2_img.setImageBitmap(CodeUtils.createQRCode(URL.VIP_CN, 205, 0));
        Bitmap bitmap = createBitmap(context, view);
        width = bitmap.getWidth();
        heigh = bitmap.getHeight();
        int iDataLen = width * heigh;
        int[] pixels = new int[iDataLen];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, heigh);
        mUsbDriver.write(PrintCmd.PrintDiskImagefile(pixels, width, heigh));
        mUsbDriver.write(PrintCmd.SetLeftmargin(0));
    }

    public static Bitmap createBitmap(Context mContext, View view) {
        Bitmap screenshot = null;
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        int w = view.getWidth();
        int h = view.getHeight();
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        screenshot = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(screenshot);
        view.draw(c);
        return screenshot;
    }


    /**
     * @param mUsbDriver
     * @param bean
     * @param context
     */
    public boolean getPrintTicketData(UsbDriver mUsbDriver, QuickOrderDetialsBean bean, Context context) {
        int iStatus = getPrinterStatus(mUsbDev1, mUsbDriver);
        if (checkStatus(iStatus) != 0)
            return false;

        try {
            mUsbDriver.write(PrintCmd.SetClean(), mUsbDev1);  // 初始化，清理缓存
            getCommonSettings(mUsbDev1, mUsbDriver);
            // 小票主要内容
            mUsbDriver.write(PrintCmd.SetAlignment(0), mUsbDev1);
            mUsbDriver.write(PrintCmd.SetSizetext(0, 0), mUsbDev1);
            mUsbDriver.write(PrintCmd.SetBold(0), mUsbDev1);
            mUsbDriver.write(PrintCmd.PrintString(printData(bean), 0), mUsbDev1);
            // 走纸换行、切纸、清理缓存
            SetFeedCutClean(cutter, mUsbDev1, mUsbDriver);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }


    public String printData(QuickOrderDetialsBean bean) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(bean.getShopName() + "顾客联" + "\n");
        buffer.append("店名：" + BaseInfo.getStoreName() + "\n");
        buffer.append("电话：" + bean.getTel() + "\n");
        buffer.append("收银员：" + bean.getUserNo() + "     ");
        buffer.append("机号：" + bean.getTerminalNo() + "\n");

        int integral = 0;
        for (QuickOrderDetialsBean.SaledListBean productListBean : bean.getSaledList()) {
            integral = integral + productListBean.getVipIntegral();
        }
        buffer.append(Constant.DOT_LINE);

        buffer.append("商品         单价   数量   金额" + "\n");//8,4,4
        double goodsPrice = 0.00, allDiscountAmount = 0.00, couponAmount = 0.00, saleAmount = 0.00, discAmount = 0.00, giveAmount = 0.00;
        for (QuickOrderDetialsBean.SaledListBean productListBean : bean.getSaledList()) {
            buffer.append(productListBean.getGoodsName() + "\n");
            buffer.append(productListBean.getBarcode() + " " + TextUtil.doubleTwoPoint(productListBean.getRealPrice()) + "   " + productListBean.getSaleNum() + "      " + TextUtil.doubleTwoPoint(productListBean.getSaleAmount()) + "\n");
            goodsPrice = goodsPrice + productListBean.getRealPrice() * productListBean.getSaleNum();
            couponAmount = couponAmount + productListBean.getCouponAmount();
            discAmount = discAmount + productListBean.getDiscAmount();
            giveAmount = giveAmount + productListBean.getGiveAmount();
            saleAmount = saleAmount + productListBean.getSaleAmount();
        }
        allDiscountAmount = couponAmount + discAmount + giveAmount;
        //商品信息
        buffer.append(Constant.DOT_LINE);
        buffer.append("应付：" + TextUtil.doubleTwoPoint(goodsPrice) + "\n");
        buffer.append("折扣:" + TextUtil.doubleTwoPoint(discAmount) + "   ");
        buffer.append("满减:" + TextUtil.doubleTwoPoint(giveAmount));
        buffer.append("\n");
        buffer.append("实付：" + String.format("%.2f", goodsPrice - allDiscountAmount) + "\n");
        //支付方式
        QuickOrderDetialsBean.SalepayListBean salepayListBean1 = bean.getSalepayList().get(0);
        buffer.append(salepayListBean1.getPaymodeName() + ":" + salepayListBean1.getPayAmount() + "\n");
        if (couponAmount != 0) {
            buffer.append("抵扣券:" + TextUtil.doubleTwoPoint(couponAmount) + "\n");
        }
        buffer.append(Constant.DOT_LINE);

        buffer.append("交易时间：");
        buffer.append(DateUtils.format(bean.getCreateTime(), DateUtils.Pattern.YYYY_MM_DD_HH_MM_SS) + "" + "\n");
        buffer.append("流水号：");
        buffer.append(bean.getSaleNo() + "\n");
        int count = bean.getPrintCount() + 1;
        buffer.append("打印次数：" + count + "\n");
        return buffer.toString();

    }


    private void PrintDiskImgFile(UsbDriver mUsbDriver, Context mContext, String codeContent) {
        int[] data1 = getBitmapParamsData(mContext, codeContent);
        mUsbDriver.write(PrintCmd.PrintDiskImagefile(data1, width, heigh));
    }

    int width, heigh;

    private int[] getBitmapParamsData(Context mContext, String codeContent) {
        Bitmap bitmap;
        int[] pixels;
        bitmap = CodeUtils.createQRCode(codeContent, 250, 250, BitmapFactory.decodeResource(MyContext.appContext.getResources(), R.mipmap.code_logo), 0);
        Bitmap bm = convertToBlackWhite(bitmap); // 2.彩色转黑白图 ----20170615
        width = bm.getWidth();
        heigh = bm.getHeight();
        int iDataLen = width * heigh;
        pixels = new int[iDataLen];
        bm.getPixels(pixels, 0, width, 0, 0, width, heigh);

        return pixels;
    }


    /**
     * 将彩色图转换为纯黑白二色
     *
     * @return 返回转换好的位图
     */
    public static Bitmap convertToBlackWhite(Bitmap bmp) {
        int width = bmp.getWidth(); // 获取位图的宽
        int height = bmp.getHeight(); // 获取位图的高
        int[] pixels = new int[width * height]; // 通过位图的大小创建像素点数组

        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                // 分离三原色
                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                // 转化成灰度像素
                grey = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }
        // 新建图片
        Bitmap newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565); // RGB_565
        // 设置图片数据
        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(newBmp, width, width);
        return resizeBmp;
    }


    // 走纸换行、切纸、清理缓存
    private void SetFeedCutClean(int iMode, UsbDevice usbDev, UsbDriver mUsbDriver) {
        mUsbDriver.write(PrintCmd.PrintFeedline(5), usbDev);      // 走纸换行
        mUsbDriver.write(PrintCmd.PrintCutpaper(iMode), usbDev);  // 切纸类型
        mUsbDriver.write(PrintCmd.SetClean(), usbDev);            // 清理缓存
    }

    // 常规设置
    private void getCommonSettings(UsbDevice usbDev, UsbDriver mUsbDriver) {
        mUsbDriver.write(PrintCmd.SetAlignment(align), usbDev);    // 对齐方式
        mUsbDriver.write(PrintCmd.SetRotate(rotate), usbDev);      // 字体旋转
        mUsbDriver.write(PrintCmd.SetUnderline(underLine), usbDev);// 下划线
        mUsbDriver.write(PrintCmd.SetLinespace(linespace), usbDev);// 行间距
        mUsbDriver.write(PrintCmd.SetSizetext(0, 0), mUsbDev1);//文本放大1-8

    }


    private int checkStatus(int iStatus) {
        int iRet = -1;
        StringBuilder sMsg = new StringBuilder();

        //0 打印机正常 、1 打印机未连接或未上电、2 打印机和调用库不匹配
        //3 打印头打开 、4 切刀未复位 、5 打印头过热 、6 黑标错误 、7 纸尽 、8 纸将尽
        switch (iStatus) {
            case 0:
                questStatus(0);
                // sMsg.append(Constant.Normal_CN);       // 正常
                iRet = 0;
                break;
            case 8:
                questStatus(1);
                sMsg.append(Constant.PaperWillExhausted_CN); // 纸将尽
                iRet = 0;
                break;
            case 3:
                questStatus(2);
                sMsg.append(Constant.PrintHeadOpen_CN); //打印头打开
                break;
            case 4:
                questStatus(2);
                sMsg.append(Constant.CutterNotReset_CN);
                break;
            case 5:
                questStatus(2);
                sMsg.append(Constant.PrintHeadOverheated_CN);
                break;
            case 6:
                questStatus(2);
                sMsg.append(Constant.BlackMarkError_CN);
                break;
            case 7:
                questStatus(1);
                sMsg.append(Constant.PaperExhausted_CN);     // 纸尽==缺纸
                break;
            case 1:
                questStatus(2);
                sMsg.append(Constant.NoConnectedOrNoOnPower_CN);
                break;
            default:
                sMsg.append(Constant.Abnormal_CN);     // 异常
                break;
        }
        if (iStatus != 0)
            com.maoye.mlh_slotmachine.util.Toast.getInstance().toast(MyContext.appContext, sMsg + "", 2);
        return iRet;

    }

    private void questStatus(int iStatus) {
        Observable<BaseResult> baseResultObservable = BaseRetrofit.getInstance().mServletApi.printer_status(iStatus);
        baseResultObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    BaseObserver<BaseResult> observer = new BaseObserver<BaseResult>(MyContext.appContext) {
        @Override
        protected void onBaseNext(BaseResult data) {

        }

        @Override
        protected void onBaseError(Throwable t) {

        }
    };

    // Get UsbDriver(UsbManager) service
    public boolean PrintConnStatus(UsbDriver mUsbDriver, UsbManager mUsbManager) {
        boolean blnRtn = false;
        try {
            if (!mUsbDriver.isConnected()) {
//				UsbManager m_UsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                // USB线已经连接
                for (UsbDevice device : mUsbManager.getDeviceList().values()) {
                    if ((device.getProductId() == 8211 && device.getVendorId() == 1305)
                            || (device.getProductId() == 8213 && device
                            .getVendorId() == 1305)) {
                        blnRtn = mUsbDriver.usbAttached(device);
                        if (blnRtn == false)
                            break;
                        blnRtn = mUsbDriver.openUsbDevice(device);
                        // 打开设备
                        if (blnRtn) {
                            if (device.getProductId() == 8211)
                                mUsbDev1 = device;
                            else
                                mUsbDev2 = device;
                            break;
                        } else {
                            com.maoye.mlh_slotmachine.util.Toast.getInstance().toast(MyContext.appContext, "USB driver failed", 2);
                            break;
                        }
                    }
                }
            } else {
                blnRtn = true;
            }
        } catch (Exception e) {
            Toast.makeText(MyContext.appContext, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            return true;
        }
        return blnRtn;
    }
}
