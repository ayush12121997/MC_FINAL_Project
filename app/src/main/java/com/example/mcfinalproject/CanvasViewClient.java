package com.example.mcfinalproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

// MainActivity.java

public class CanvasViewClient extends View {

    public int width;
    public int height;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mPaint;
    private float mX, mY;
    private static final float TOLERANCE = 5;
    Context context;
    private static CanvasObject client_data;
    String pl = "";

    public CanvasViewClient(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        mPath = new Path();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(4f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPath(mPath, mPaint);
    }

    private void StartTouch(float x, float y){
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void moveTouch(float x, float y){
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if(dx >= TOLERANCE || dy >= TOLERANCE){
            mPath.quadTo(mX, mY, (x+mX) / 2, (y+mY) / 2);
            mX = x;
            mY = y;
        }
    }

    public void clearCanvas(){
        mPath.reset();
        invalidate();
    }

    private void upTouch(){
        mPath.lineTo(mX, mY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                client_data = new CanvasObject(x, y, -1);
                pl = pl + x + "," + y +"," + "-1" + ";";
                Log.d("SOCKET", client_data.x + " " + client_data.y + " " + client_data.flag);
                StartTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                client_data = new CanvasObject(x, y, 0);
                pl = pl + x + "," + y +"," + "0" + ";";
                Log.d("SOCKET", client_data.x + " " + client_data.y + " " + client_data.flag);
                moveTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                client_data = new CanvasObject(x, y, 1);
                pl = pl + x + "," + y +"," + "1" + ";";
                Log.d("SOCKET", client_data.x + " " + client_data.y + " " + client_data.flag);
                upTouch();
                invalidate();
                break;
        }
        return true;
    }
}