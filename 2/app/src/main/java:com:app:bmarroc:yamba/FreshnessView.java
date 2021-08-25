package com.app.bmarroc.yamba;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


public class FreshnessView extends View {
    private static final int LINE_HEIGHT = 30;
    private long timestamp = -1;
    private Paint paint;

    public FreshnessView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setARGB(255, 0, 255, 255);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(LINE_HEIGHT);

        setMinimumHeight(LINE_HEIGHT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (timestamp == -1) {
            return;
        }
        long delta = System.currentTimeMillis()-timestamp;
        double hours = delta/3600000.0;
        double multiplier = Math.abs(1-(Math.min(hours, 168.0)/168.0));
        int width = (int)(getWidth()*multiplier);
        canvas.drawLine(0, 0, width, 0, paint);
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        this.invalidate();
    }
}

