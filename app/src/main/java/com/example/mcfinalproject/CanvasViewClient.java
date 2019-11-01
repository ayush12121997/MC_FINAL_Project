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

import androidx.core.util.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

// MainActivity.java

public class CanvasViewClient extends View {
    //    file change
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
    List<Pair<Path, Integer>> colorPath;

    public CanvasViewClient(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        colorPath = new ArrayList<Pair<Path,Integer>>();
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
        int col = mPaint.getColor();
        for(int i=0;i<colorPath.size();i++)
        {
            Pair<Path,Integer> p = colorPath.get(i);
            mPaint.setColor(p.second);
            canvas.drawPath(p.first,mPaint);
        }
        mPaint.setColor(col);
        canvas.drawPath(mPath, mPaint);
//        mPath = new Path();
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
    private void upTouch(){
        mPath.lineTo(mX, mY);
    }

    public void clearCanvas(){
        mPath = new Path();
        colorPath = new ArrayList<>();
        invalidate();
    }
    public void setColor(int color)
    {
        colorPath.add(new Pair(mPath,mPaint.getColor()));
        mPaint.setColor(color);
        mPath = new Path();
        Log.d("fragment","here");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                client_data = new CanvasObject(x, y, -1);
                pl = pl + x + "," + y +"," + "-1" +","+mPaint.getColor()+ ";";
                Log.d("SOCKET", client_data.x + " " + client_data.y + " " + client_data.flag);
                StartTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                client_data = new CanvasObject(x, y, 0);
                pl = pl + x + "," + y +"," + "0" +","+mPaint.getColor()+ ";";
                Log.d("SOCKET", client_data.x + " " + client_data.y + " " + client_data.flag);
                moveTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                client_data = new CanvasObject(x, y, 1);
                pl = pl + x + "," + y +"," + "1" +","+mPaint.getColor()+ ";";
                Log.d("SOCKET", client_data.x + " " + client_data.y + " " + client_data.flag);
                upTouch();
                invalidate();
                break;
        }
        return true;
    }
}