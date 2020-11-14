package com.bitgam.app.ui.shape;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.bitgam.app.R;

public class SquircleShape extends androidx.appcompat.widget.AppCompatImageView {

    int radius = 1;
    int power = 1;
    int clr = 0;
    float rt = 0;
    float w = 0;
    float h = 0;

    public SquircleShape(Context context) {
        super(context);
    }

    public SquircleShape(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray ar = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SquircleShape, 0, 0);
        power = ar.getInteger(R.styleable.SquircleShape_sqPower, 1);
        radius = ar.getInteger(R.styleable.SquircleShape_sqRadius, 1);
        clr = ar.getInteger(R.styleable.SquircleShape_sqColor, 1);
        rt = 1.0f/power;
        w = getWidth()/2.0f;
        h = getHeight()/2.0f;
    }

    public SquircleShape(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ar = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SquircleShape, 0, 0);
        power = ar.getInteger(R.styleable.SquircleShape_sqPower, 1);
        radius = ar.getInteger(R.styleable.SquircleShape_sqRadius, 1);
        clr = ar.getInteger(R.styleable.SquircleShape_sqColor, 1);
        rt = 1.0f/power;
        w = getWidth()/2.0f;
        h = getHeight()/2.0f;
    }

//    public SquircleShape(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Path squirclePath = getSquirclePath(getLeft(), getTop(), radius, power);
        Paint mPaint = new Paint();
        mPaint.setColor(clr);
        canvas.drawPath(squirclePath, mPaint);
    }

    private Path getSquirclePath(int left, int top, int radius, float n){
        //Formula: (|x|)^3 + (|y|)^3 = radius^3
        final double radiusToPow = Math.pow(radius,1);
        final float a = (float) (Math.pow(w,n)/radius);
        final float b = (float) (Math.pow(h,n)/radius);


        Path path = new Path();
        path.moveTo(-w, 0);
        for (int x = (int) -w; x <= w ; x++)
            path.lineTo(x, ((float) Math.pow((radiusToPow - Math.abs(Math.pow(x,n))/a)*b,rt)));
        for (int x = (int) w; x >= -w ; x--)
            path.lineTo(x, ((float) -Math.pow((radiusToPow - Math.abs(Math.pow(x,n))/a)*b,rt)));
        path.close();

        Matrix matrix = new Matrix();
        matrix.postTranslate(left + w, top + h);
        path.transform(matrix);

        return path;
    }
}
