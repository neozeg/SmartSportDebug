package com.mupro.smartsportdebug.Views;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLSurfView extends GLSurfaceView implements GLSurfaceView.Renderer{
	private final static String TAG = "MyGLSurfView";


	private final static float RATIO_X = 4f;
	private final static float RATIO_Y = 1f;
	private final static float RATIO_Z = 1f;
	private final static float CONST_SCALE = 300f;
	private final float RATIO_SUM = RATIO_X + RATIO_Y +RATIO_Z;

	//private final int NUMBER_OF_POINT = 256;
	private final int CHANNEL_NUMBER_MAX = 6;
	private final static float TWOPI = (float) Math.PI;

	private final static int BUFFER_LENTH_MAX = 1024;

	private int ChannelNumber = 0;
	private boolean isGraphFlat = false;

	private final static float[] COLOR_ARRAY = {
			0.0f,1.0f,0.0f,1.0f,//Green
			0.0f,0.0f,1.0f,1.0f,//Blue
			1.0f,1.0f,1.0f,1.0f,//White
			1.0f,1.0f,0.0f,1.0f,//yellow
			1.0f,0.0f,0.0f,1.0f,//Red
			1.0f,0.5f,0.0f,1.0f,//orange
	};



	private GestureDetector mGestureDetector;

	private float Width,Height,Depth;
	private float adjDepth;
	//private float mDepth;
	
	public MyGLSurfView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		setRenderer(this);
		setOnTouchListener(mOnTouchListener);
		mGestureDetector = new GestureDetector(context,mOGL);
		ChannelNumber = CHANNEL_NUMBER_MAX;
		for(int i=0;i<CHANNEL_NUMBER_MAX;i++){
			waveDataBuf[i] = new short[BUFFER_LENTH_MAX];
			for(int j=0;j<BUFFER_LENTH_MAX;j++)
				waveDataBuf[i][j] = 0;
		}
		createWaves();
	}
	
	private FloatBuffer[] mVertBuf,mVertColorBuf;
	private ShortBuffer[] mIndexBuf;

	//private float[] mCubeVert;
	//private float[] mCubeVertColor ;
	//private short[] mCubeVertIndex;
	private short [][] waveDataBuf = new short[CHANNEL_NUMBER_MAX][];
	private short []  waveDataMax = new short[CHANNEL_NUMBER_MAX];
	private short []  waveDataMin = new short[CHANNEL_NUMBER_MAX];
	private boolean[] waveEnabled = new boolean[CHANNEL_NUMBER_MAX];


	private float[][] mWaveVert = new float[CHANNEL_NUMBER_MAX][];
	private float[][] mWaveVertColor = new float[CHANNEL_NUMBER_MAX][];
	private short[][] mWaveVertIndex = new short[CHANNEL_NUMBER_MAX][];
	
	private float backColorR = 0.1f,
			backColorG = 0.1f,
			backColorB = 0.1f,
			backColorA = 0.0f;
	
	private float mYaw = 0f, mPitch=0f, mRoll=0f;
	private float mdX = 0f, mdY=0f, mdZ=0f;
	
	private synchronized void setup(){
		for(int i=0;i<CHANNEL_NUMBER_MAX;i++)
			waveEnabled[i]=true;
		createWaves();
	}

	private synchronized void updateWavesDepth(){

		for(int chNo=0;chNo<ChannelNumber;chNo++){
			int length = waveDataBuf[chNo].length;
			float stepZ = adjDepth/ChannelNumber;
			for(int i=0;i<length;i++){
				mWaveVert[chNo][i*3 + 2] = ((float)chNo*stepZ - adjDepth/2)/(CONST_SCALE*RATIO_Z/RATIO_SUM);
			}
			ByteBuffer vertBuf = ByteBuffer.allocateDirect(4 * mWaveVert[chNo].length);
			vertBuf.order(ByteOrder.nativeOrder());
			mVertBuf[chNo] = vertBuf.asFloatBuffer();

			mVertBuf[chNo].put(mWaveVert[chNo]);

			mVertBuf[chNo].position(0);
		}
	}
	private synchronized void createWaves(){

		short[] minValues = new short[CHANNEL_NUMBER_MAX];
		short[] maxValues = new short[CHANNEL_NUMBER_MAX];

		mVertBuf = new FloatBuffer[ChannelNumber];
		mVertColorBuf = new FloatBuffer[ChannelNumber];
		mIndexBuf = new ShortBuffer[ChannelNumber];
		for(int chNo=0;chNo<ChannelNumber;chNo++){
			int length = waveDataBuf[chNo].length;
			mWaveVert[chNo] = new float[length *3];
			mWaveVertColor[chNo] = new float[length * 4];
			mWaveVertIndex[chNo] = new short[length];

			float stepX = Width/(length-1);
			float stepZ = adjDepth/ChannelNumber;
			minValues[chNo] = waveDataBuf[chNo][0];
			maxValues[chNo] = waveDataBuf[chNo][0];
			for(int i=0;i<length;i++){
				if(waveDataBuf[chNo][i]<minValues[chNo]){
					minValues[chNo] = waveDataBuf[chNo][i];
				}
				if(waveDataBuf[chNo][i]>maxValues[chNo]){
					maxValues[chNo] = waveDataBuf[chNo][i];
				}

				if(minValues[chNo] > waveDataMin[chNo])
					minValues[chNo] = waveDataMin[chNo];
				else
					waveDataMin[chNo] = minValues[chNo];

				if(maxValues[chNo] < waveDataMax[chNo])
					maxValues[chNo] = waveDataMax[chNo];
				else
					waveDataMax[chNo] = maxValues[chNo];


			}

			for(int j=0;j<length;j++){
				mWaveVert[chNo][j*3 + 0] = (j*stepX - Width/2)/(CONST_SCALE*RATIO_X/RATIO_SUM);//(float)(j-length/2)/100;
				if(maxValues[chNo]==minValues[chNo]){
					mWaveVert[chNo][j*3 + 1] = 0;
				}else{
					mWaveVert[chNo][j*3 + 1] = ((float)waveDataBuf[chNo][j]/(maxValues[chNo]-minValues[chNo]));
				}

				mWaveVert[chNo][j*3 + 2] = ((float)chNo*stepZ - adjDepth/2)/(CONST_SCALE*RATIO_Z/RATIO_SUM);//(float)(chNo-ChannelNumber/2)/5;//(RATIO_Z/RATIO_SUM/CONST_SCALE);

				mWaveVertColor[chNo][j*4 + 0] = COLOR_ARRAY[chNo*4 + 0];
				mWaveVertColor[chNo][j*4 + 1] = COLOR_ARRAY[chNo*4 + 1];
				mWaveVertColor[chNo][j*4 + 2] = COLOR_ARRAY[chNo*4 + 2];
				mWaveVertColor[chNo][j*4 + 3] = COLOR_ARRAY[chNo*4 + 3];

				mWaveVertIndex[chNo][j] = (short)j;
			}

			ByteBuffer vertBuf = ByteBuffer.allocateDirect(4 * mWaveVert[chNo].length);
			vertBuf.order(ByteOrder.nativeOrder());
			mVertBuf[chNo] = vertBuf.asFloatBuffer();

			ByteBuffer vertColorBuf = ByteBuffer.allocateDirect(4 * mWaveVertColor[chNo].length);
			vertColorBuf.order(ByteOrder.nativeOrder());
			mVertColorBuf[chNo] = vertColorBuf.asFloatBuffer();

			ByteBuffer indexBuf = ByteBuffer.allocateDirect(2 * mWaveVertIndex[chNo].length);
			indexBuf.order(ByteOrder.nativeOrder());
			mIndexBuf[chNo] = indexBuf.asShortBuffer();

			mVertBuf[chNo].put(mWaveVert[chNo]);
			mVertColorBuf[chNo].put(mWaveVertColor[chNo]);
			mIndexBuf[chNo].put(mWaveVertIndex[chNo]);

			mVertBuf[chNo].position(0);
			mVertColorBuf[chNo].position(0);
			mIndexBuf[chNo].position(0);
		}

	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		Width = getMeasuredWidth();
		Height = Width *9/16;//getMeasuredHeight();
		Depth  = Width/8;
		adjDepth = Depth;
		Log.v(TAG,"Width= "+ Width + " ,Height= " + Height);
		setMeasuredDimension((int)Width,(int)Height);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// TODO Auto-generated method stub
		gl.glClearColor(backColorR, backColorG, backColorB, backColorA);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glFrontFace(GL10.GL_CCW);
		gl.glCullFace(GL10.GL_BACK);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		gl.glLineWidth(5);

		
		setup();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// TODO Auto-generated method stub
		final float fNEAREST = .01f,
				fFAREST	= 100f,
				fVIEW_ANGLE = 45f;
		gl.glMatrixMode(GL10.GL_PROJECTION);
		float fViewWidth = fNEAREST * (float) Math.tan(Math.toRadians(fVIEW_ANGLE)/2);
		float aspectRatio = (float) width/ (float) height;
		gl.glFrustumf(-fViewWidth, fViewWidth, -fViewWidth/aspectRatio, fViewWidth/aspectRatio, fNEAREST, fFAREST);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		Log.v(TAG,"Width= "+ width + " ,Height= " + height);
		gl.glViewport(0, 0, width, height);

		setup();
	}

	@Override
	public synchronized void onDrawFrame(GL10 gl) {
		// TODO Auto-generated method stub

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		for(int i = 0 ;i<ChannelNumber;i++){

			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertBuf[i]);
			gl.glColorPointer(4, GL10.GL_FLOAT, 0, mVertColorBuf[i]);
			gl.glLoadIdentity();
			gl.glTranslatef(mdX, mdY, mdZ-6);
			float x = (float) Math.cos(Math.toRadians(-mYaw))*1.0f;
			float y = (float) Math.sin(Math.toRadians(-mYaw))*1.0f;
			float z = (float) Math.sin(Math.toRadians(-mPitch))*1.0f;
			gl.glRotatef(mYaw,0,0,1f);
			gl.glRotatef(mRoll, 0f, 1.0f, 0f);
			gl.glRotatef(mPitch, 1.0f, 0f, 0f);
			if(waveEnabled[i])
				gl.glDrawElements(GL10.GL_LINE_STRIP, mWaveVertIndex[i].length, GL10.GL_UNSIGNED_SHORT, mIndexBuf[i]);
		}
		
	}
	
	public void setOrientation(float yaw, float pitch, float roll){
		mYaw = yaw;
		mPitch = pitch;
		mRoll = roll;
	}
	
	public void setTranslate(float ax, float ay, float az){
		mdX = ax;
		mdY = ay;
		mdZ = az;
	}

	public void setDataBuffer(short[] buf){
		//etDataBuffer1Ch");
		ChannelNumber = 1;
		waveDataBuf[0] = new short[buf.length> BUFFER_LENTH_MAX ? BUFFER_LENTH_MAX : buf.length];
		System.arraycopy(buf,0,waveDataBuf[0],0,buf.length);

		createWaves();
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
	}

	public void setDataBuffer6Ch(short[] bufCh1,short[] bufCh2, short[] bufCh3,short[] bufCh4,short[] bufCh5, short[] bufCh6,int buflength){

		//Log.v(TAG,"setDataBuffer6Ch");
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
	}

	public void clearDataRange(){
		for(int i=0;i<CHANNEL_NUMBER_MAX;i++){
			waveDataMin[i]=waveDataMax[i]=0;
		}
	}
	public int getWaveColor(int chNo){
		if(chNo < CHANNEL_NUMBER_MAX){
			float a = COLOR_ARRAY[chNo*4+3];
			float r = COLOR_ARRAY[chNo*4+0];
			float g = COLOR_ARRAY[chNo*4+1];
			float b = COLOR_ARRAY[chNo*4+2];
			int aa = (int)(255 * a);
			int rr = (int)(255 * r);
			int gg = (int)(255 * g);
			int bb = (int)(255 * b);
			int color = Color.argb(aa,rr,gg,bb);
			return color;
		}
		return 0;
	}

	public boolean isWaveEnabled(int chNo){
		if(chNo < CHANNEL_NUMBER_MAX)
			return waveEnabled[chNo];
		else
			return false;
	}
	public void setWaveEnable(int chNo,boolean enable){
		if(chNo < CHANNEL_NUMBER_MAX){
			waveEnabled[chNo] = enable;
		}
	}
	private float posX,posY;
	private OnTouchListener mOnTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			//Log.v(TAG,"Event="+event.getAction());
			switch (event.getAction()){
				case MotionEvent.ACTION_DOWN:
					posX = event.getX();
					posY = event.getY();
					getParent().requestDisallowInterceptTouchEvent(true);
					break;
				case MotionEvent.ACTION_MOVE:
					float distantX = Math.abs(event.getX() - posX);
					float distantY = Math.abs(event.getY() - posY);
					if(distantX > 2 *distantY){
						mRoll += 360 * (event.getX() - posX)/Width;
					}
					if(distantY > 2 * distantX){
						//mPitch += 10 * (event.getY() - posY)/Height;
						adjDepth += 30* (event.getY() - posY)/Height;
						if(adjDepth < 0 )adjDepth = 0;
						//createWaves();
						updateWavesDepth();
					}

					posX = event.getX();
					posY = event.getY();
					break;
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					getParent().requestDisallowInterceptTouchEvent(false);
					break;
			}
			mGestureDetector.onTouchEvent(event);
			return true;
		}
	};

	private GestureDetector.OnGestureListener mOGL = new GestureDetector.OnGestureListener() {
		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {

		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			mRoll = 0;
			mPitch = 0;
			mYaw = 0;
			//isGraphFlat = !isGraphFlat;
			adjDepth = Depth;
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			//Log.v(TAG,"MotionEvent = "+ e1.getX() + " , " + e2.getX());
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {

		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			//Log.v(TAG,"MotionEvent = "+ e1.getX() + " , " + e2.getX());
			return false;
		}
	};
	
}
