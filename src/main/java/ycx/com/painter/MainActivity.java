package ycx.com.painter;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                    ColorPickerDialog.OnColorChangedListener,
                    ColorPickerDialog.OnAlphaChangedListener,
                    ColorPickerDialog.ChangeStrokeWidth {

    private Paint mPaint;
    private ColorPickerDialog colorDialog;
    private EraseChangeDialog eraseDialog;
    CanvasView cv;

    private int navId = 1;
    private static final int paintNavId = 1;
    private static final int eraseNavId = 2;
    private static final int figureNavId = 3;

    private int prepaintStrokeWidth = 12;
    private int preeraseStrokeWidth = 12;
    private int prepaintAlpha = 255;
    private int preeraseAlpha = 255;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        cv = (CanvasView)findViewById(R.id.canvasView);
        mPaint = cv.getmPaint();
        eraseDialog = new EraseChangeDialog(this,mPaint);

        //中间功能按钮
        final FloatingActionButton funcitonFab = (FloatingActionButton) findViewById(R.id.function);
        funcitonFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                funcitonFabBtn(navId);
            }
        });

        //左边撤销按钮
        FloatingActionButton undoFab = (FloatingActionButton) findViewById(R.id.undo);
        undoFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cv.unDo();
                }
            });

        //右边反撤销按钮
        FloatingActionButton redoFab = (FloatingActionButton) findViewById(R.id.redo);
        redoFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               cv.reDo();
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    //中间功能按钮函数实现
    public void funcitonFabBtn(int navId){
        switch (navId){
            case paintNavId:
                paintTools();
                break;
            case eraseNavId:
                eraseToole();
                break;
            case figureNavId:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //画笔工具
    public void paintTools(){
        colorDialog = new ColorPickerDialog(this, mPaint.getColor(), mPaint.getAlpha(), (int)mPaint.getStrokeWidth(),
                new ColorPickerDialog.OnColorChangedListener() {
                    @Override
                    public void colorChanged(int color) {
                        mPaint.setColor(color);
                    }

                },
                new ColorPickerDialog.OnAlphaChangedListener(){
                    @Override
                    public void alphaChanged(int alpha) {
                        mPaint.setAlpha(alpha);
                    }
                },
                new ColorPickerDialog.ChangeStrokeWidth() {
                    @Override
                    public void changeStrokeWidth(int strokeWidth) {
                        mPaint.setStrokeWidth(strokeWidth);
                    }
        });
        colorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        colorDialog.show();
    }

    //橡皮擦
    public void eraseToole(){
        eraseDialog.show();
    }

    //导航栏选择
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        mPaint.setXfermode(null);
        if (id == R.id.nav_clear_all) {
            cv.clearAll(this);
        } else if (id == R.id.nav_export_pic) {
            Toast.makeText(this,"图片保存到:"+cv.savePicture()+"中",Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_brush) {
            cv.setErase(false);
            preeraseStrokeWidth = (int) mPaint.getStrokeWidth();
            preeraseAlpha = mPaint.getAlpha();
            mPaint.setStrokeWidth(prepaintStrokeWidth);
            mPaint.setAlpha(prepaintAlpha);
            navId = paintNavId;
        } else if (id == R.id.nav_eraser) {
            cv.setErase(true);
            prepaintStrokeWidth = (int)mPaint.getStrokeWidth();
            prepaintAlpha = mPaint.getAlpha();
            mPaint.setStrokeWidth(preeraseStrokeWidth);
            mPaint.setAlpha(preeraseAlpha);
            navId = eraseNavId;
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        } else if (id == R.id.nav_figure) {
            navId = figureNavId;
        } else if (id == R.id.nav_about) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void colorChanged(int color) {
        mPaint.setColor(color);
    }
    public void alphaChanged(int alpha) {
        mPaint.setAlpha(alpha);;
    }
    public void changeStrokeWidth(int strokeWidth) {
        mPaint.setStrokeWidth(strokeWidth);
    }
}