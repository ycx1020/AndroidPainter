package ycx.com.painter;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
//画布
public class CanvasView extends View {

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;// 画布的画笔
    private Paint mPaint;// 真实的画笔
    private float mX, mY;// 临时点坐标
    private static final float TOUCH_TOLERANCE = 4;

    // 保存Path路径的集合,用List集合来模拟栈
    private ArrayList<DrawPath> savePath;
    private ArrayList<DrawPath> deletePath;

    private DrawPath dp;    // 记录Path路径的对象

    private int screenWidth, screenHeight;


    private class DrawPath {
        public Path path;   // 路径
        public Paint paint; // 画笔

    }

    private boolean isErase = false;


    public CanvasView(Context context,AttributeSet attrs) {
        super(context,attrs);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        // 保存一次一次绘制出来的图形
        mCanvas = new Canvas(mBitmap);

        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);

        savePath = new ArrayList<DrawPath>();
        deletePath = new ArrayList<DrawPath>();
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);// 将前面已经画过得显示出来
        if (mPath != null) {
            // 实时的显示
            canvas.drawPath(mPath, mPaint);
        }
    }

    private void touch_start(float x, float y) {
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(mY - y);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            // 从x1,y1到x2,y2画一条贝塞尔曲线，更平滑(直接用mPath.lineTo也是可以的)
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        mCanvas.drawPath(mPath, mPaint);
        savePath.add(dp);//保存路劲
        mPath = null;// 重新置空
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath = new Path();// 每次down下去重新new一个Path
                dp = new DrawPath();//每一次记录的路径对象是不一样的
                dp.path = mPath;
                dp.paint = new Paint();
                dp.paint.setAntiAlias(true);
                dp.paint.setDither(true);
                dp.paint.setStyle(Paint.Style.STROKE);
                dp.paint.setStrokeJoin(Paint.Join.ROUND);
                dp.paint.setStrokeCap(Paint.Cap.ROUND);

                dp.paint.setColor(mPaint.getColor());
                dp.paint.setAlpha(mPaint.getAlpha());
                dp.paint.setStrokeWidth(mPaint.getStrokeWidth());
                if(isErase){
                   dp.paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
                }else{
                   dp.paint.setXfermode(null);
                }

                //如果有改变画布则把反撤销清空
                deletePath.clear();

                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    /**
     * 撤销的核心思想就是将画布清空，
     * 将保存下来的Path路径最后一个移除掉，
     * 重新将路径画在画布上面。
     */
    public void unDo() {
        if (savePath != null && savePath.size() > 0) {
            DrawPath drawPath = savePath.get(savePath.size() - 1);
            deletePath.add(drawPath);
            savePath.remove(savePath.size() - 1);
            redrawOnBitmap();
        }
    }

    //反撤销
    public void reDo(){
        if(deletePath != null && deletePath.size() > 0) {
            //将删除的路径列表中的最后一个，也就是最顶端路径取出（栈）,并加入路径保存列表中
            DrawPath dp = deletePath.get(deletePath.size() - 1);
            savePath.add(dp);
            //将取出的路径重绘在画布上
            redrawOnBitmap();
            //将该路径从删除的路径列表中去除
            deletePath.remove(deletePath.size() - 1);
        }
    }

    //清空所有
    public void clearAll(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("确定清空画布上所有内容吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (savePath != null && savePath.size() > 0) {
                    savePath.clear();
                    if(deletePath != null && deletePath.size() > 0){
                        deletePath.clear();
                    }
                    redrawOnBitmap();
                }
                invalidate();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void redrawOnBitmap(){
        mBitmap = Bitmap.createBitmap(screenWidth, screenHeight,Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(mBitmap);// 重新设置画布，相当于清空画布
        Iterator<DrawPath> iter = savePath.iterator();
        while (iter.hasNext()) {
            DrawPath drawPath = iter.next();
            mCanvas.drawPath(drawPath.path, drawPath.paint);
        }
        invalidate();
    }

    //保存图片
    public String  savePicture(){
        //获得系统当前时间，并以该时间作为文件名
        SimpleDateFormat formatter   =   new   SimpleDateFormat   ("yyyyMMddHHmmss");
        Date curDate   =   new   Date(System.currentTimeMillis());//获取当前时间
        String   str   =   formatter.format(curDate);
        String paintPath = "";
        str = str + "paint.png";
        File dir = new File("/sdcard/pictures/");
        File file = new File("/sdcard/pictures/",str);
        if (!dir.exists()) {
            dir.mkdir();
        }
        else{
            if(file.exists()){
                file.delete();
            }
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            //保存绘图文件路径
            paintPath = "/sdcard/pictures/" + str;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return paintPath;
    }



    public Paint getmPaint() {
        return mPaint;
    }

    public void setmPaint(Paint mPaint) {
        this.mPaint = mPaint;
    }

    public Canvas getmCanvas() {
        return mCanvas;
    }

    public void setmCanvas(Canvas mCanvas) {
        this.mCanvas = mCanvas;
    }

    public boolean isErase() {
        return isErase;
    }

    public void setErase(boolean erase) {
        isErase = erase;
    }

    public ArrayList<DrawPath> getSavePath() {
        return savePath;
    }

    public void setSavePath(ArrayList<DrawPath> savePath) {
        this.savePath = savePath;
    }

    public ArrayList<DrawPath> getDeletePath() {
        return deletePath;
    }

    public void setDeletePath(ArrayList<DrawPath> deletePath) {
        this.deletePath = deletePath;
    }
}
