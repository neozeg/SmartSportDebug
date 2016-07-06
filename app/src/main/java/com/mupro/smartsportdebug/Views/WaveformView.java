package com.mupro.smartsportdebug.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by Administrator on 2015/11/18.
 */
public class WaveformView extends SurfaceView implements SurfaceHolder.Callback ,Runnable {
    private final  static String TAG = "WaveformView";
    private final static boolean FRAME_EXIST = true;
    private SurfaceHolder mSurfaceHolder;
    private Surface mSurface;
    private int mHeight,mWidth;
    private Thread drawThread;

    private int ChannelNumber=0;


    private final static int XOFFSET_MAX = 10;
    private final static int CHANNEL_NUMBER_MAX = 6;
    private final static int BUFFER_LENTH_MAX = 1024;
    private final static float CONST_RATIO = 0.9f;

    //private short [] waveCh1DataBuf;
    //private short [] waveCh2DataBuf;
    //private short [] waveCh3DataBuf;
    //private short [] waveCh4DataBuf;
    //private short [] waveCh5DataBuf;
    //private short [] waveCh6DataBuf;

    private short [][] waveDataBuf = new short[CHANNEL_NUMBER_MAX][];
    public WaveformView(Context context) {
        super(context);
        init();
    }

    public WaveformView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        //setZOrderOnTop(true);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        mSurface = new Surface();
        mSurface.density = getResources().getDisplayMetrics().density;
        mSurface.init();

        for(int i=0;i<CHANNEL_NUMBER_MAX;i++){
            mSurface.wavePointsArray[i] = new float[BUFFER_LENTH_MAX*2];
        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        mSurface.width = getMeasuredWidth();
        mSurface.height = getMeasuredHeight();
        mWidth = mSurface.width;
        mHeight = mSurface.height;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        //Log.v(TAG,"onLayout() ");
        if(changed){
            mSurface.init();
        }
        super.onLayout(changed, left, top, right, bottom);
    }
    /*
    @Override
    protected  void onDraw(Canvas canvas) {
        //mSurface.reDraw();
        //canvas.drawPath(mSurface.borderPath, mSurface.borderPaint);
        super.onDraw(canvas);
    }*/

    /*
        @Override
        public void run() {
            Canvas c = null;
            synchronized (mSurfaceHolder){
                c = mSurfaceHolder.lockCanvas();
                if(c!=null){
                    draw(c);
                    mSurfaceHolder.unlockCanvasAndPost(c);
                }
                else
                    Log.TAG,"canvas = null");
            }
        }
        */
        @Override
        public void run() {
            Canvas c = null;
            try{
                c = mSurfaceHolder.lockCanvas();
                synchronized(mSurfaceHolder){
                    if(c!=null){
                        draw(c);
                    }
                }
            }finally{
                try{
                    if(mSurfaceHolder != null){
                        mSurfaceHolder.unlockCanvasAndPost(c);;
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        }
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
       // Log.v(TAG,"draw(c)");
        long start =System.currentTimeMillis();
        mSurface.reDraw();
        canvas.drawPath(mSurface.borderPath, mSurface.borderPaint);
        for(int chNo=0;chNo<ChannelNumber;chNo++){
            if(mSurface.waveEnabled[chNo])
                canvas.drawPath(mSurface.wavePaths[chNo],mSurface.wavePaints[chNo]);
        }
        long end = System.currentTimeMillis();
        //Log.v(TAG,"duration"+(end-start)+"ms");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurface.width = width;
        mSurface.height = height;
        mWidth = mSurface.width;
        mHeight = mSurface.height;
        Log.v(TAG, "surfaceChanged");
        //Log.v(TAG,"width="+width+",height="+height);
        //Log.v(TAG,"mWidth="+mWidth+",mHeigh="+mHeight);
        drawThread = new Thread(this);
        drawThread.run();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
    public synchronized void createWaves(){

        short[] minValues = new short[CHANNEL_NUMBER_MAX];
        short[] maxValues = new short[CHANNEL_NUMBER_MAX];

        float stepWidth = 0;

        int lengthMin = waveDataBuf[0].length;
        for(int i=0;i<ChannelNumber;i++){
            mSurface.wavePointsArray[i] = new float[waveDataBuf[i].length*2];
        }
        stepWidth = (float)mSurface.width/(lengthMin-1);



        for(int chNo=0;chNo<ChannelNumber;chNo++){
            minValues[chNo] = waveDataBuf[chNo][0];
            maxValues[chNo] = waveDataBuf[chNo][0];
            for(int i=0;i<lengthMin;i++){
                    if(waveDataBuf[chNo][i]<minValues[chNo]){
                        minValues[chNo] = waveDataBuf[chNo][i];
                    }
                    if(waveDataBuf[chNo][i]>maxValues[chNo]){
                        maxValues[chNo] = waveDataBuf[chNo][i];
                    }
            }
            //Log.v(TAG,"min["+chNo+"]="+minValues[chNo] + "\tmax["+chNo+"]="+maxValues[chNo]);
        }
        for(int chNum=0;chNum<ChannelNumber;chNum++){
            for(int i=0;i<lengthMin;i++){
                mSurface.wavePointsArray[chNum][i*2+0] = stepWidth * i;
                if(minValues[chNum] != maxValues[chNum])
                    mSurface.wavePointsArray[chNum][i*2+1] = mSurface.height *(1 + CONST_RATIO)/2 -(waveDataBuf[chNum][i] - minValues[chNum]) * mSurface.height * CONST_RATIO / (maxValues[chNum] -minValues[chNum]);
                else
                    mSurface.wavePointsArray[chNum][i*2+1] = mSurface.height *1/2;
            }
        }
    }
    public int getWaveColor(int chNo){
        if(chNo < CHANNEL_NUMBER_MAX){
            return mSurface.waveColors[chNo];
        }
        return 0;
    }

    public boolean isWaveEnabled(int chNo){
        if(chNo < CHANNEL_NUMBER_MAX)
            return mSurface.waveEnabled[chNo];
        else
            return false;
    }
    public void setWaveEnable(int chNo,boolean enable){
        if(chNo < CHANNEL_NUMBER_MAX){
            mSurface.waveEnabled[chNo] = enable;
            drawThread = new Thread(this);
            drawThread.run();
        }
    }

    public void setDataBuffer(short[] buf){
        //etDataBuffer1Ch");
        ChannelNumber = 1;
        waveDataBuf[0] = new short[buf.length> BUFFER_LENTH_MAX ? BUFFER_LENTH_MAX : buf.length];
        System.arraycopy(buf,0,waveDataBuf[0],0,buf.length);

        createWaves();
        drawThread = new Thread(this);
        drawThread.run();
        //invalidate();
    }

    public void setDataBuffer2Ch(short[] bufCh1,short[] bufCh2,int buflength){
        //Log.v(TAG,"setDataBuffer2Ch");

        ChannelNumber = 2;
        for(int i=0;i<ChannelNumber;i++){
            waveDataBuf[i] = new short[buflength> BUFFER_LENTH_MAX ? BUFFER_LENTH_MAX : buflength];
            switch(i%CHANNEL_NUMBER_MAX){
                case 0:
                    System.arraycopy(bufCh1,0,waveDataBuf[i],0,buflength);
                    break;
                case 1:
                    System.arraycopy(bufCh2,0,waveDataBuf[i],0,buflength);
                    break;
            }
        }
        createWaves();
        drawThread = new Thread(this);
        drawThread.run();
        //invalidate();
    }

    public void setDataBuffer3Ch(short[] bufCh1,short[] bufCh2, short[] bufCh3,int buflength){
        //Log.v(TAG,"setDataBuffer3Ch");
        ChannelNumber = 3;
        for(int i=0;i<ChannelNumber;i++){
            waveDataBuf[i] = new short[buflength> BUFFER_LENTH_MAX ? BUFFER_LENTH_MAX : buflength];
            switch(i%CHANNEL_NUMBER_MAX){
                case 0:
                    System.arraycopy(bufCh1,0,waveDataBuf[i],0,buflength);
                    break;
                case 1:
                    System.arraycopy(bufCh2,0,waveDataBuf[i],0,buflength);
                    break;
                case 2:
                    System.arraycopy(bufCh3,0,waveDataBuf[i],0,buflength);
                    break;
            }
        }
        createWaves();
        drawThread = new Thread(this);
        drawThread.run();
        //invalidate();
    }

    public void setDataBuffer6Ch(short[] bufCh1,short[] bufCh2, short[] bufCh3,short[] bufCh4,short[] bufCh5, short[] bufCh6,int buflength){

        Log.v(TAG,"setDataBuffer6Ch");
        ChannelNumber = 6;
        for(int i=0;i<ChannelNumber;i++){
            waveDataBuf[i] = new short[buflength> BUFFER_LENTH_MAX ? BUFFER_LENTH_MAX : buflength];
            switch(i%CHANNEL_NUMBER_MAX){
                case 0:
                    System.arraycopy(bufCh1,0,waveDataBuf[i],0,buflength);
                    break;
                case 1:
                    System.arraycopy(bufCh2,0,waveDataBuf[i],0,buflength);
                    break;
                case 2:
                    System.arraycopy(bufCh3,0,waveDataBuf[i],0,buflength);
                    break;
                case 3:
                    System.arraycopy(bufCh4,0,waveDataBuf[i],0,buflength);
                    break;
                case 4:
                    System.arraycopy(bufCh5,0,waveDataBuf[i],0,buflength);
                    break;
                case 5:
                    System.arraycopy(bufCh6,0,waveDataBuf[i],0,buflength);
                    break;
            }
        }

        createWaves();
        drawThread = new Thread(this);
        drawThread.run();
        //invalidate();
    }


    private class Surface{
        public int width;
        public int height;

        public float density;
        public float borderWidth;
        public float borderCornerLength;

        public Paint borderPaint;
        public Path borderPath;
        //private int borderColor = Color.parseColor("#61c8cd");
        private int borderColor = getResources().getColor(android.R.color.holo_blue_light);


        private int waveCh1Color = getResources().getColor(android.R.color.holo_orange_dark);
        private int waveCh2Color = getResources().getColor(android.R.color.holo_purple);
        private int waveCh3Color = getResources().getColor(android.R.color.holo_red_light);
        private int waveCh4Color = getResources().getColor(android.R.color.holo_blue_light);
        private int waveCh5Color = getResources().getColor(android.R.color.holo_green_light);
        private int waveCh6Color = getResources().getColor(android.R.color.white);

        public Paint[] wavePaints= new Paint[CHANNEL_NUMBER_MAX];
        public Path[] wavePaths = new Path[CHANNEL_NUMBER_MAX];
        public boolean[] waveEnabled = new boolean[CHANNEL_NUMBER_MAX];
        public float[][] wavePointsArray = new float[CHANNEL_NUMBER_MAX][];
        private int[] waveColors = new int[CHANNEL_NUMBER_MAX];


        //private Path path1,path2,path3,path4,path5,path6;


        public void init(){
            width = mWidth;
            height = mHeight;
            for(int chNo=0;chNo<CHANNEL_NUMBER_MAX;chNo++){
                waveEnabled[chNo]=true;
                switch(chNo%CHANNEL_NUMBER_MAX){
                    case 0:
                        waveColors[chNo] = waveCh1Color;
                        break;
                    case 1:
                        waveColors[chNo] = waveCh2Color;
                        break;
                    case 2:
                        waveColors[chNo] = waveCh3Color;
                        break;
                    case 3:
                        waveColors[chNo] = waveCh4Color;
                        break;
                    case 4:
                        waveColors[chNo] = waveCh5Color;
                        break;
                    case 5:
                        waveColors[chNo] = waveCh6Color;
                        break;

                }
            }
        }
        public void reDraw(){
            borderPaint = new Paint();
            borderPaint.setColor(borderColor);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderWidth = (float) (2 * density);
            borderWidth = borderWidth < 1 ? 1 : borderWidth;
            borderPaint.setStrokeWidth(borderWidth/4);
            borderPath = new Path();
            borderCornerLength = height/6;

            if(FRAME_EXIST){
                borderPath.moveTo(borderWidth / 2, borderWidth / 2);
                borderPath.rLineTo(borderCornerLength, 0);
                borderPath.moveTo(borderWidth / 2, borderWidth / 2);
                borderPath.rLineTo(0, borderCornerLength);
                borderPath.moveTo(width - borderWidth / 2, borderWidth / 2);
                borderPath.rLineTo(-borderCornerLength, 0);
                borderPath.moveTo(width - borderWidth / 2, borderWidth / 2);
                borderPath.rLineTo(0, borderCornerLength);
                borderPath.moveTo(borderWidth / 2, height - borderWidth / 2);
                borderPath.rLineTo(borderCornerLength, 0);
                borderPath.moveTo(borderWidth / 2, height - borderWidth / 2);
                borderPath.rLineTo(0, -borderCornerLength);
                borderPath.moveTo(width - borderWidth / 2, height - borderWidth / 2);
                borderPath.rLineTo(-borderCornerLength, 0);
                borderPath.moveTo(width - borderWidth / 2, height - borderWidth / 2);
                borderPath.rLineTo(0, -borderCornerLength);
            }

            for(int i=0;i<CHANNEL_NUMBER_MAX;i++){
                switch(i%CHANNEL_NUMBER_MAX){
                    case 0:
                        waveColors[i] = waveCh1Color;
                        break;
                    case 1:
                        waveColors[i] = waveCh2Color;
                        break;
                    case 2:
                        waveColors[i] = waveCh3Color;
                        break;
                    case 3:
                        waveColors[i] = waveCh4Color;
                        break;
                    case 4:
                        waveColors[i] = waveCh5Color;
                        break;
                    case 5:
                        waveColors[i] = waveCh6Color;
                        break;

                }
                wavePaints[i] = new Paint();
                wavePaints[i].setColor(waveColors[i]);
                wavePaints[i].setStyle(Paint.Style.STROKE);
                wavePaints[i].setStrokeWidth(borderWidth / 4);
                wavePaints[i].setAntiAlias(true);

                wavePaths[i] = new Path();
            }


            int lengthMin = wavePointsArray[0].length;
            for(int chNo=0;chNo<ChannelNumber;chNo++){
                wavePaths[chNo].moveTo(wavePointsArray[chNo][0],wavePointsArray[chNo][1]);
                lengthMin = lengthMin < wavePointsArray[chNo].length ? lengthMin:wavePointsArray[chNo].length;
            }
            for(int i = 1; i<lengthMin/2;i++){
                for(int chNo=0;chNo<ChannelNumber;chNo++)
                    wavePaths[chNo].lineTo(wavePointsArray[chNo][i * 2 + 0],wavePointsArray[chNo][i * 2 + 1]);
            }
        }

    }
}
