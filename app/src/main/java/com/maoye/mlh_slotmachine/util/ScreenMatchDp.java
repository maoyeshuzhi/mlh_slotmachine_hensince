package com.maoye.mlh_slotmachine.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.nio.Buffer;

/**
 * Created by Rs on 2018/5/31.
 */

public class ScreenMatchDp {
    public  static void screen( int strings){
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i <900 ; i++) {
            //strings[0]*
           String str = "\""+"dimen"+i+"\"";
            buffer.append( "<dimen name="+str+">"+ String.format("%.2f",div(i,1.75,2))+"dp</dimen>"+"\n");
        }
        getString(buffer.toString());
        ScreenMatchDp.e("green:",buffer.toString());
    }

    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
     * 定精度，以后的数字四舍五入。
     * @param v1 被除数
     * @param v2 除数
     * @param scale 表示表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */
    public static double div(double v1, double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2).doubleValue();
    }

    /** * 截断输出日志 * @param msg */
    public static void e(String tag, String msg) {
        if (tag == null || tag.length() == 0 || msg == null || msg.length() == 0)
            return; int segmentSize = 3 * 1024;
            long length = msg.length();
            if (length <= segmentSize )
            {
                // 长度小于等于限制直接打印
                Log.e(tag, msg);
            }else { while (msg.length() > segmentSize )
            {// 循环分段打印日志
               String logContent = msg.substring(0, segmentSize );
               msg = msg.replace(logContent, "");
               Log.e(tag, logContent); }
               Log.e(tag, msg);
            // 打印剩余日志
                }
    }



    public static void getString(String str) {

        String filePath = null;

        boolean hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);

        if (hasSDCard) {

            filePath =Environment.getExternalStorageDirectory().toString() + File.separator +"hello.txt";

        } else

            filePath =Environment.getDownloadCacheDirectory().toString() + File.separator +"hello.txt";



        try {

            File file = new File(filePath);

            if (!file.exists()) {

                File dir = new File(file.getParent());

                dir.mkdirs();

                file.createNewFile();

            }

            FileOutputStream outStream = new FileOutputStream(file);

            outStream.write(str.getBytes());

            outStream.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    }

