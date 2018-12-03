package com.example.wangh7.player;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

public class ImageFilter {
    //图片缩放比例
    private static final float BITMAP_SCALE = 0.4f;

    /**
     * 模糊图片的具体方法
     *
     * @param context 上下文对象
     * @param image   需要模糊的图片
     * @return 模糊处理后的图片
     */
    public static Bitmap blurBitmap(Context context, Bitmap image, float blurRadius) {
        // 计算图片缩小后的长宽
        int width = Math.round(image.getWidth() * BITMAP_SCALE);
        int height = Math.round(image.getHeight() * BITMAP_SCALE);

        // 将缩小后的图片做为预渲染的图片
        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        // 创建一张渲染后的输出图片
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        // 创建RenderScript内核对象
        RenderScript rs = RenderScript.create(context);
        // 创建一个模糊效果的RenderScript的工具对象
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        // 由于RenderScript并没有使用VM来分配内存,所以需要使用Allocation类来创建和分配内存空间
        // 创建Allocation对象的时候其实内存是空的,需要使用copyTo()将数据填充进去
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);

        // 设置渲染的模糊程度, 25f是最大模糊度
        blurScript.setRadius(blurRadius);
        // 设置blurScript对象的输入内存
        blurScript.setInput(tmpIn);
        // 将输出数据保存到输出内存中
        blurScript.forEach(tmpOut);

        // 将数据填充到Allocation中
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }

    /**
     * 根据饱和度、色相、亮度调整图片
     *
     * @param bm
     *            原图片
     * @param saturation
     *            饱和度，最小可设为0，此时对应的是灰度图(也就是俗话的“黑白图”)，
     *            为1表示饱和度不变，设置大于1，就显示过饱和
     * @param hue
     *            色相
     * @param lum
     *            亮度,色轮旋转的角度,正值表示顺时针旋转，负值表示逆时针旋转
     * @return 处理后的图片
     */
    public static Bitmap handleImage(Bitmap bm, int saturation, int hue, int lum) {
        Bitmap bmp = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        ColorMatrix mLightnessMatrix = new ColorMatrix();
        //ColorMatrix mSaturationMatrix = new ColorMatrix();
        ColorMatrix mHueMatrix = new ColorMatrix();
        ColorMatrix mAllMatrix = new ColorMatrix();
        //float mSaturationValue = saturation * 1.0F / 127;
        float mHueValue = hue * 1.0F / 127;
        //float mLumValue = (lum - 127) * 1.0F / 127 * 180;
        mHueMatrix.reset();
        mHueMatrix.setScale(mHueValue, mHueValue, mHueValue, 1);// 红、绿、蓝三分量按相同的比例,最后一个参数1表示透明度不做变化

        //mSaturationMatrix.reset();
        //mSaturationMatrix.setSaturation(mSaturationValue);
        //mLightnessMatrix.reset();

        //mLightnessMatrix.setRotate(0, mLumValue);
        //mLightnessMatrix.setRotate(1, mLumValue);
        //mLightnessMatrix.setRotate(2, mLumValue);

        mAllMatrix.reset();
        mAllMatrix.postConcat(mHueMatrix);
        //mAllMatrix.postConcat(mSaturationMatrix);
        //mAllMatrix.postConcat(mLightnessMatrix);

        paint.setColorFilter(new ColorMatrixColorFilter(mAllMatrix));
        canvas.drawBitmap(bm, 0, 0, paint);
        return bmp;
    }
}