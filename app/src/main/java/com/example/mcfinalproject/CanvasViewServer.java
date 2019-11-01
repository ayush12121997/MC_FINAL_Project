package com.example.mcfinalproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.core.util.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// MainActivity2.java

public class CanvasViewServer extends View {

    public int width;
    public int height;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mPaint;
    private float mX, mY;
    private static final float TOLERANCE = 5;
    Context context;
    List<Pair<Path, Integer>> colorPath;

    public CanvasViewServer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        colorPath = new ArrayList<>();
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

//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//
//        canvas.drawPath(mPath, mPaint);
//    }

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

    private void StartTouch(float x, float y) {
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void moveTouch(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOLERANCE || dy >= TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    public void clearCanvas() {
        mPath = new Path();
        colorPath = new ArrayList<>();
        invalidate();
    }

    private void upTouch() {
        mPath.lineTo(mX, mY);
    }
    public void update(final String data)
    {
        if (data != null) {
        Log.d("SOCKET", "In updateCanvas");
        Log.d("SOCKET", data);
        String[] points = data.split(";");

        int col = Color.BLACK;

        for (int i = 0; i < points.length; i++) {
            String pt = points[i];
            String[] val = pt.split(",");
            float x = Float.parseFloat(val[0]);
            float y = Float.parseFloat(val[1]);
            int flag = Integer.parseInt(val[2]);
            col = Integer.parseInt(val[3]);

            if(i==0) {
                mPaint.setColor(col);
            }
            else if(col!=mPaint.getColor()) {
                colorPath.add(new Pair<>(mPath, mPaint.getColor()));
                mPaint.setColor(col);
                mPath = new Path();
            }
//                mPath = new Path();
//                mPaint.setColor(col);
            switch (flag) {
                case -1:
                    StartTouch(x, y);
                    invalidate();
                    break;
                case 0:
                    moveTouch(x, y);
                    invalidate();
                    break;
                case 1:
                    upTouch();
                    invalidate();
                    break;
            }
        }
        } else if(data.equals("")) {
//            clearCanvas();
            Log.d("SOCKET", "Empty String : Clearing Canvas.");
        }
    }

    public void updateCanvas(final String data1, final String data2) {
        if((data1+data2).length()==0)
            clearCanvas();
        else
        {
            mPath = new Path();
            update(data1);
            mPath = new Path();
            update(data2);
        }
    }

}