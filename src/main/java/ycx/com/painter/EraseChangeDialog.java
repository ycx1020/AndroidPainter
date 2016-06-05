package ycx.com.painter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;


public class EraseChangeDialog extends Dialog {

    Context context;
    private SeekBar eraseSeekBarStrokeWidth;
    private SeekBar eraseSeekBarAlpha;
    private TextView eraseStrokeWidthText;
    private TextView eraseAlphaText;
    private Paint ePaint;

    public EraseChangeDialog(Context context,Paint ePaint) {
        super(context);
        this.ePaint = ePaint;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.erase_change_dialog);
        eraseSeekBarAlpha = (SeekBar)findViewById(R.id.eraseAlpha);
        eraseSeekBarAlpha.setMax(255);
        eraseSeekBarAlpha.setProgress(255);
        eraseAlphaText = (TextView)findViewById(R.id.eraseAlphaText);
        eraseAlphaText.setText("不透明度："+(int)((float)ePaint.getAlpha()/255*100)+"%");
        eraseSeekBarAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ePaint.setAlpha(eraseSeekBarAlpha.getProgress());
                eraseAlphaText.setText("不透明度："+(int)((float)ePaint.getAlpha()/255*100)+"%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ePaint.setAlpha(eraseSeekBarAlpha.getProgress());
                eraseAlphaText.setText("不透明度："+(int)((float)ePaint.getAlpha()/255*100)+"%");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ePaint.setAlpha(eraseSeekBarAlpha.getProgress());
                eraseAlphaText.setText("不透明度："+(int)((float)ePaint.getAlpha()/255*100)+"%");
            }
        });


        eraseSeekBarStrokeWidth = (SeekBar)findViewById(R.id.eraseStrokeWidth);
        eraseSeekBarStrokeWidth.setMax(300);
        eraseSeekBarStrokeWidth.setProgress((int) ePaint.getStrokeWidth());
        eraseStrokeWidthText = (TextView)findViewById(R.id.eraseStrokeWidthText);
        eraseStrokeWidthText.setText("宽度："+ (int)ePaint.getStrokeWidth()+"px");
        eraseSeekBarStrokeWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ePaint.setStrokeWidth(eraseSeekBarStrokeWidth.getProgress());
                eraseStrokeWidthText.setText("宽度："+(int)ePaint.getStrokeWidth()+"px");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ePaint.setStrokeWidth(eraseSeekBarStrokeWidth.getProgress());
                eraseStrokeWidthText.setText("宽度："+(int)ePaint.getStrokeWidth()+"px");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ePaint.setStrokeWidth(eraseSeekBarStrokeWidth.getProgress());
                eraseStrokeWidthText.setText("宽度："+(int)ePaint.getStrokeWidth()+"px");
            }
        });
    }
}
