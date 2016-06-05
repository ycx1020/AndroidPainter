package ycx.com.painter;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class ColorPickerDialog extends Dialog {

    Context context;
    private int initialColor;//初始颜色
    private int initialAlpha;//初始透明度
    private int initialStrokeWidth;//初始宽度

    private OnColorChangedListener cListener;
    private OnAlphaChangedListener aListener;
    private ChangeStrokeWidth wListener;
    private Paint mCenterPaint;//中间圆画笔

    //TextView、seekBar、布局控件
    private static LinearLayout ll;
    private TextView alphaText;
    private TextView strokeWidthText;
    private SeekBar alphaSeekBar;
    private SeekBar strokeWidthSeekBar;

    //初始色为黑色
    public ColorPickerDialog(Context context,OnColorChangedListener clistener,OnAlphaChangedListener alistener,ChangeStrokeWidth wListener) {
        this(context, Color.BLACK,255,12, clistener,alistener,wListener);
    }
    public ColorPickerDialog(Context context, int initialColor,int initialAlpha,int initialStrokeWidth,
                             OnColorChangedListener cListener,OnAlphaChangedListener aListener,ChangeStrokeWidth wListener) {
        super(context);
        this.context = context;
        this.cListener = cListener;
        this.aListener = aListener;
        this.initialColor = initialColor;
        this.initialAlpha = initialAlpha;
        this.initialStrokeWidth = initialStrokeWidth;
        this.wListener = wListener;
    }

    //创建Dialog窗口
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewLayOut();
        setContentView(ll);
    }

    private void viewLayOut(){
        /*
         * 1、设置控件属性
         * 2、新建一个LinearLayout.LayoutParams
         * 3、将控件和LinearLayout.LayoutParams添加到ll上
         * */

        //创建一个布局
        ll = new LinearLayout(this.getContext());
        ll.setOrientation(LinearLayout.VERTICAL);

        //颜色拾取器
        LinearLayout.LayoutParams lpPickerColor = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        ColorPickerView myView = new ColorPickerView(context);
        ll.addView(myView, lpPickerColor);

        //透明度拖动条
        alphaSeekBar = new SeekBar(this.context);
        alphaSeekBar.setMax(255);
        alphaSeekBar.setProgress(mCenterPaint.getAlpha());
        alphaSeekBar.setPadding(100, 0, 100, 0);
        alphaSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                alphaText.setText("不透明度："+(int)((float)alphaSeekBar.getProgress()/255*100)+"%");
            }
            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                alphaText.setText("不透明度："+(int)((float)alphaSeekBar.getProgress()/255*100)+"%");
            }
            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                alphaText.setText("不透明度："+(int)((float)alphaSeekBar.getProgress()/255*100)+"%");
            }
        });
        LinearLayout.LayoutParams lpAlphaSeekBar = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lpAlphaSeekBar.setMargins(0, 0, 0, 20);
        //透明度文字显示
        alphaText = new TextView(this.context);
        alphaText.setText("不透明度："+(int)((float)alphaSeekBar.getProgress()/255*100)+"%");
        alphaText.setPadding(80, 0, 100, 0);
        alphaText.setTextSize(14);

        //画笔宽度
        strokeWidthSeekBar = new SeekBar(this.context);
        strokeWidthSeekBar.setMax(300);
        strokeWidthSeekBar.setProgress(initialStrokeWidth);//待定
        strokeWidthSeekBar.setPadding(100, 0, 100, 0);
        strokeWidthSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                strokeWidthText.setText("宽度："+strokeWidthSeekBar.getProgress()+"px");
            }
            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                strokeWidthText.setText("宽度："+strokeWidthSeekBar.getProgress()+"px");
            }
            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                strokeWidthText.setText("宽度："+strokeWidthSeekBar.getProgress()+"px");
            }
        });
        LinearLayout.LayoutParams lpStrokeWidthSeekBar = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lpStrokeWidthSeekBar.setMargins(0, 0, 0, 100);
        //透宽度文字显示
        strokeWidthText = new TextView(this.context);
        strokeWidthText.setText("宽度："+strokeWidthSeekBar.getProgress()+"px");
        strokeWidthText.setPadding(80, 0, 100, 0);
        strokeWidthText.setTextSize(14);

        ll.addView(alphaText);
        ll.addView(alphaSeekBar,lpAlphaSeekBar);
        ll.addView(strokeWidthText);
        ll.addView(strokeWidthSeekBar,lpStrokeWidthSeekBar);

    }








    //颜色拾取快类
    private class ColorPickerView extends View {

        private Paint mPaint;//渐变色环画笔

        private Paint mLinePaint;//分隔线画笔
        private Paint mRectPaint;//渐变方块画笔

        private Shader rectShader;//渐变方块渐变图像
        private float rectLeft;//渐变方块左x坐标
        private float rectTop;//渐变方块右x坐标
        private float rectRight;//渐变方块上y坐标
        private float rectBottom;//渐变方块下y坐标

        private final int[] mCircleColors ;//渐变色环颜色
        private final int[] mRectColors ;  //渐变方块颜色

        private int mHeight;//View高
        private int mWidth;//View宽
        private float r;//色环半径(paint中部)
        private float centerRadius;//中心圆半径

        private boolean downInCircle = true;//按在渐变环上
        private boolean downInRect;//按在渐变方块上
        private boolean adownInRect;//按在渐变方块上
        private boolean highlightCenter;//高亮
        private boolean highlightCenterLittle;//微亮

        public ColorPickerView(Context context) {
            super(context);

            //设置窗口的宽高
            DisplayMetrics dm = getResources().getDisplayMetrics();
            int height = (int) (dm.heightPixels* 0.7f);
            int width = (int) (dm.widthPixels * 0.8f);
            this.mHeight = (int) (height - (int)dm.heightPixels*0.2);
            this.mWidth = width;
            setMinimumHeight((int) (height - (int)dm.heightPixels*0.2));
            setMinimumWidth(width);

            //渐变色环参数
            mCircleColors = new int[] {0xFFFF0000, 0xFFFF00FF, 0xFF0000FF,0xFF00FFFF, 0xFF00FF00,0xFFFFFF00, 0xFFFF0000};

            //圆环的梯度颜色渲染
            Shader s = new SweepGradient(0, 0, mCircleColors, null);
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);	//新建抗锯齿画笔
            mPaint.setShader(s);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(50);
            r = width / 2 * 0.8f - mPaint.getStrokeWidth() * 0.5f;

            //中心圆参数
            mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCenterPaint.setColor(initialColor);
            mCenterPaint.setAlpha(initialAlpha);
            mCenterPaint.setStrokeWidth(5);
            centerRadius = (r - mPaint.getStrokeWidth() / 2 ) * 0.6f;

            //边框参数
            mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mLinePaint.setColor(Color.parseColor("#afafaf"));
            mLinePaint.setStrokeWidth(3);

            //黑白渐变参数
            mRectColors = new int[]{0xFFFFFFFF, mCenterPaint.getColor(), 0xFF000000};
            mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mRectPaint.setStrokeWidth(5);
            rectLeft = -r - mPaint.getStrokeWidth() * 0.5f;
            rectTop = r + mPaint.getStrokeWidth() * 0.5f + mLinePaint.getStrokeMiter() * 0.5f + 60;
            rectRight = r + mPaint.getStrokeWidth() * 0.5f;
            rectBottom = rectTop + 50;

        }

        @Override
        protected void onDraw(Canvas canvas) {

            //透明度的改变
            mCenterPaint.setAlpha(alphaSeekBar.getProgress());
            invalidate();

            //移动中心
            canvas.translate(mWidth / 2, mHeight / 2 - 50);

            //画中心圆
            canvas.drawCircle(0, 0, centerRadius,  mCenterPaint);

            //是否显示中心圆外的小圆环
            if (highlightCenter || highlightCenterLittle) {
                int c = mCenterPaint.getColor();
                mCenterPaint.setStyle(Paint.Style.STROKE);
                if(highlightCenter) {
                    mCenterPaint.setAlpha(0xFF);
                }else if(highlightCenterLittle) {
                    mCenterPaint.setAlpha(0x90);
                }
                canvas.drawCircle(0, 0, centerRadius + mCenterPaint.getStrokeWidth(),  mCenterPaint);
                mCenterPaint.setStyle(Paint.Style.FILL);
                mCenterPaint.setColor(c);
            }

            //画色环
            canvas.drawOval(new RectF(-r, -r, r, r), mPaint);

            //画黑白渐变块
            if(downInCircle) {
                Paint tempPaint = mCenterPaint;
                tempPaint.setAlpha(255);
                mRectColors[1] = tempPaint.getColor();
            }
            rectShader = new LinearGradient(rectLeft, rectTop, rectRight, rectBottom, mRectColors,null, Shader.TileMode.MIRROR);
            mRectPaint.setShader(rectShader);
            canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, mRectPaint);
            float offset = mLinePaint.getStrokeWidth() / 2;
            canvas.drawLine(rectLeft - offset, rectTop - offset * 2,  rectLeft - offset, rectBottom + offset * 2, mLinePaint);//左
            canvas.drawLine(rectLeft - offset * 2, rectTop - offset,  rectRight + offset * 2, rectTop - offset, mLinePaint);//上
            canvas.drawLine(rectRight + offset, rectTop - offset * 2, rectRight + offset, rectBottom + offset * 2, mLinePaint);//右
            canvas.drawLine(rectLeft - offset * 2, rectBottom + offset,rectRight + offset * 2, rectBottom + offset, mLinePaint);//下

            super.onDraw(canvas);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX() - mWidth / 2;
            float y = event.getY() - mHeight / 2 + 50;
            boolean inCircle = inColorCircle(x, y,r + mPaint.getStrokeWidth() / 2, r - mPaint.getStrokeWidth() / 2);
            boolean inCenter = inCenter(x, y, centerRadius);
            boolean inRect = inRect(x, y);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downInCircle = inCircle;
                    downInRect = inRect;
                    highlightCenter = inCenter;
                case MotionEvent.ACTION_MOVE:
                    if(downInCircle && inCircle) {//down按在渐变色环内, 且move也在渐变色环内
                        float angle = (float) Math.atan2(y, x);
                        float unit = (float) (angle / (2 * Math.PI));
                        if (unit < 0) {
                            unit += 1;
                        }
                        mCenterPaint.setColor(interpCircleColor(mCircleColors, unit));
                    }else if(downInRect && inRect) {//down在渐变方块内, 且move也在渐变方块内
                        mCenterPaint.setColor(interpRectColor(mRectColors, x));
                    }
                    if((highlightCenter && inCenter) || (highlightCenterLittle && inCenter)) {//点击中心圆, 当前移动在中心圆
                        highlightCenter = true;
                        highlightCenterLittle = false;
                    } else if(highlightCenter || highlightCenterLittle) {//点击在中心圆, 当前移出中心圆
                        highlightCenter = false;
                        highlightCenterLittle = true;
                    } else {
                        highlightCenter = false;
                        highlightCenterLittle = false;
                    }
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    if(highlightCenter && inCenter) {//点击在中心圆, 且当前启动在中心圆
                        if(cListener != null) {
                            cListener.colorChanged(mCenterPaint.getColor());
                            aListener.alphaChanged(mCenterPaint.getAlpha());
                            ColorPickerDialog.this.dismiss();
                        }
                        wListener.changeStrokeWidth(strokeWidthSeekBar.getProgress());

                    }
                    if(downInCircle) {
                        downInCircle = false;
                    }
                    if(downInRect) {
                        downInRect = false;
                    }
                    if(adownInRect) {
                        adownInRect = false;
                    }
                    if(highlightCenter) {
                        highlightCenter = false;
                    }
                    if(highlightCenterLittle) {
                        highlightCenterLittle = false;
                    }
                    invalidate();
                    break;
            }
            return true;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(mWidth, mHeight);
        }

        /**
         * 坐标是否在色环上
         * @param x 坐标
         * @param y 坐标
         * @param outRadius 色环外半径
         * @param inRadius 色环内半径
         * @return
         */
        private boolean inColorCircle(float x, float y, float outRadius, float inRadius) {
            double outCircle = Math.PI * outRadius * outRadius;
            double inCircle = Math.PI * inRadius * inRadius;
            double fingerCircle = Math.PI * (x * x + y * y);
            if(fingerCircle < outCircle && fingerCircle > inCircle) {
                return true;
            }else {
                return false;
            }
        }

        /**
         * 坐标是否在中心圆上
         * @param x 坐标
         * @param y 坐标
         * @param centerRadius 圆半径
         * @return
         */
        private boolean inCenter(float x, float y, float centerRadius) {
            double centerCircle = Math.PI * centerRadius * centerRadius;
            double fingerCircle = Math.PI * (x * x + y * y);
            if(fingerCircle < centerCircle) {
                return true;
            }else {
                return false;
            }
        }

        /**
         * 坐标是否在渐变色中
         * @param x
         * @param y
         * @return
         */
        private boolean inRect(float x, float y) {
            if( x <= rectRight && x >=rectLeft && y <= rectBottom && y >= rectTop) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * 获取圆环上颜色
         * @param colors
         * @param unit
         * @return
         */
        private int interpCircleColor(int colors[], float unit) {
            if (unit <= 0) {
                return colors[0];
            }
            if (unit >= 1) {
                return colors[colors.length - 1];
            }

            float p = unit * (colors.length - 1);
            int i = (int)p;
            p -= i;

            // now p is just the fractional part [0...1) and i is the index
            int c0 = colors[i];
            int c1 = colors[i+1];
            int a = alphaSeekBar.getProgress();//ave(Color.alpha(c0), Color.alpha(c1), p);
            int r = ave(Color.red(c0), Color.red(c1), p);
            int g = ave(Color.green(c0), Color.green(c1), p);
            int b = ave(Color.blue(c0), Color.blue(c1), p);

            return Color.argb(a, r, g, b);
        }

        /**
         * 获取渐变块上颜色
         * @param colors
         * @param x
         * @return
         */
        private int interpRectColor(int colors[], float x) {
            int a, r, g, b, c0, c1;
            float p;
            if (x < 0) {
                c0 = colors[0];
                c1 = colors[1];
                p = (x + rectRight) / rectRight;
            } else {
                c0 = colors[1];
                c1 = colors[2];
                p = x / rectRight;
            }
            a = ave(Color.alpha(c0), Color.alpha(c1), p);
            r = ave(Color.red(c0), Color.red(c1), p);
            g = ave(Color.green(c0), Color.green(c1), p);
            b = ave(Color.blue(c0), Color.blue(c1), p);
            return Color.argb(a, r, g, b);
        }

        private int ave(int s, int d, float p) {
            return s + Math.round(p * (d - s));
        }

    }









    public interface OnColorChangedListener {
        void colorChanged(int color);
    }
    public interface OnAlphaChangedListener{
        void alphaChanged(int alpha);
    }
    public interface ChangeStrokeWidth{
        void changeStrokeWidth(int strokeWidth);
    }



    public int getColorInitialColor() {
        return initialColor;
    }
    public void setColorInitialColor(int initialColor) {
        this.initialColor = initialColor;
    }

    public int getAlphaInitialColor() {
        return initialAlpha;
    }
    public void setAlphaInitialColor(int initialAlpha) {
        this.initialAlpha = initialAlpha;
    }

    public OnColorChangedListener getColorListener() {
        return cListener;
    }
    public OnAlphaChangedListener getAlphaListener() {
        return aListener;
    }

    public void setColorListener(OnColorChangedListener cListener) {
        this.cListener = cListener;
    }
    public void setAlphaListener(OnAlphaChangedListener aListener) {
        this.aListener = aListener;
    }
}