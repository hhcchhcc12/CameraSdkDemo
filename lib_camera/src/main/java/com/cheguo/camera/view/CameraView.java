package com.cheguo.camera.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class CameraView extends SurfaceView implements SurfaceHolder.Callback,
		Camera.PictureCallback {

	private String TAG = "CameraView";
	private SurfaceHolder holder;
	private Camera camera;

	private Camera.Parameters parameters = null;
	Context context;
	//Bundle bundle = null; // 声明一个Bundle对象，用来存储数据
	//private String cameraPath;
	int degree;
	private int cameraPosition = 0;//0代表前置摄像头，1代表后置摄像头

	private SensorManager mSensorManager;
	private Sensor mSensor;
	private int mX, mY, mZ;
	MediaPlayer shootMP;

	ICamera icamera;
	List<Size> piclist ;
	List<Size> prelist ;
	Size size ;
	Size size_pre ;

	public CameraView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		initSensor();
		holder = getHolder();// 生成Surface Holder
		holder.addCallback(this);
		holder.setFixedSize(getScreenWidth(context), getScreenHeight(context)); // 设置Surface分辨率
		holder.setKeepScreenOn(true);// 屏幕常亮
		// holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// 指定PushBuffer

	}

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initSensor();
		holder = getHolder();// 生成Surface Holder
		holder.addCallback(this);
		holder.setFixedSize(getScreenWidth(context), getScreenHeight(context)); // 设置Surface分辨率
		holder.setKeepScreenOn(true);// 屏幕常亮
		//holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// 指定PushBuffer

	}

	public void setICamera(ICamera icamera){
		this.icamera = icamera;
	}

	private void initSensor() {
		mSensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);// TYPE_GRAVITY
		if (null == mSensorManager) {
			Log.d(TAG, "deveice not support SensorManager");
		}
		// 参数三，检测的精准度
		mSensorManager.registerListener(sensorLis, mSensor,
				SensorManager.SENSOR_DELAY_NORMAL);// SENSOR_DELAY_GAME
	}

	float tMax = 1.0f;
	private SensorEventListener sensorLis = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor == null) {
				return;
			}

			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				int x = (int) event.values[0];
				int y = (int) event.values[1];
				int z = (int) event.values[2];

				int px = Math.abs(mX - x);
				int py = Math.abs(mY - y);
				int pz = Math.abs(mZ - z);
				int maxvalue = getMaxValue(px, py, pz);
				if (maxvalue > 2) {
					if(camera != null){
						try {
							camera.autoFocus(autoFocusCallback);

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					//Log.e(TAG, " sensor isMoveorchanged....");
				}

				mX = x;
				mY = y;
				mZ = z;

			}
		}
	};

	/**
	 * 获取一个最大值
	 *
	 * @param px
	 * @param py
	 * @param pz
	 * @return
	 */
	public int getMaxValue(int px, int py, int pz) {
		int max = 0;
		if (px > py && px > pz) {
			max = px;
		} else if (py > px && py > pz) {
			max = py;
		} else if (pz > px && pz > py) {
			max = pz;
		}

		return max;
	}

	public void registerSensor(){
		// 参数三，检测的精准度
		mSensorManager.registerListener(sensorLis, mSensor,
				SensorManager.SENSOR_DELAY_NORMAL);// SENSOR_DELAY_GAME
	}

	public void unRegisterSensor(){
		mSensorManager.unregisterListener(sensorLis);
	}

	// 开始拍照时调用该方法
	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		startCamera(false);

	}

	private void initCamera(){
		try {
			camera = Camera.open(cameraPosition); // 打开摄像头
			if(camera != null){
				// 设置holder主要是用于surfaceView的图片的实时预览，以及获取图片等功能
				camera.setPreviewDisplay(holder);
				//parameters = camera.getParameters();
				initSize();
				initCameraParams();
				camera.stopPreview();
				camera.startPreview(); // 开始预览
				camera.autoFocus(autoFocusCallback);
			}
		} catch (Exception e) {
			e.printStackTrace();
			//icamera.cameraError(e);
		}
	}

	public void startCamera(boolean isCancelCamera){
		try {
			if(isCancelCamera && camera != null){
				camera.stopPreview();//停掉原来摄像头的预览
				camera.release();//释放资源
				camera = null;//取消原来摄像头
			}
			camera = Camera.open(cameraPosition); // 打开摄像头
			if(camera != null){
				initSize();
				initCameraParams();
				camera.startPreview(); // 开始预览
				// 设置holder主要是用于surfaceView的图片的实时预览，以及获取图片等功能
				camera.setPreviewDisplay(holder);
				camera.autoFocus(autoFocusCallback);
			}
		} catch (Exception e) {
			e.printStackTrace();
			icamera.cameraError(e);
		}
	}

	private void initSize(){
		parameters = camera.getParameters();
		piclist = parameters.getSupportedPictureSizes();
		prelist = parameters.getSupportedPreviewSizes();
		boolean isScreenHorizontal = isScreenHorizontal();
		int width = 2160;
//		if(isScreenHorizontal){
//			width = 2560;
//		}
//		boolean suppSize = false;
		size_pre = MyCamPara.getInstance(context).getPreviewSize(prelist, width,isScreenHorizontal);
//		for(Size s : piclist){
//			if(s.width == size_pre.width && s.height == size_pre.height){
//				suppSize = true;
//				break;
//			}
//		}
		size = MyCamPara.getInstance(context).getPictureSize(piclist, width,isScreenHorizontal);
//		if(!suppSize){
//		}
//		else{
//			size = size_pre;
//		}
	}

	/** 预览界面分辨率 */
	public void setPreviewSize(Camera.Parameters parametes) {
		List<Camera.Size> localSizes = parametes.getSupportedPreviewSizes();
		Camera.Size biggestSize = null;
		Camera.Size fitSize = null;// 优先选屏幕分辨率
		Camera.Size targetSize = null;// 没有屏幕分辨率就取跟屏幕分辨率相近(大)的size
		Camera.Size targetSiz2 = null;// 没有屏幕分辨率就取跟屏幕分辨率相近(小)的size
		if(localSizes != null) {
			int cameraSizeLength = localSizes.size();
			for(int n = 0; n < cameraSizeLength; n++) {
				Camera.Size size = localSizes.get(n);
				if(biggestSize == null||
						(size.width >= biggestSize.width && size.height >= biggestSize.height)) {
					biggestSize = size;
				}

				int height = getScreenHeight(context);
				int width = getScreenWidth(context);

				if(size.width == height
						&& size.height == width) {
					fitSize = size;
				} else if(size.width == height
						|| size.height == width) {
					if(targetSize == null) {
						targetSize = size;
					} else if(size.width < height
							|| size.height < height) {
						targetSiz2 = size;
					}
				}
			}

			if(fitSize == null) {
				fitSize = targetSize;
			}

			if(fitSize == null) {
				fitSize = targetSiz2;
			}

			if(fitSize == null) {
				fitSize = biggestSize;
			}
			parametes.setPreviewSize(fitSize.width, fitSize.height);
		}

	}

	/** 输出的照片为最高像素 */
	public void setPictureSize(Camera.Parameters parametes) {
		List<Camera.Size> localSizes = parametes.getSupportedPictureSizes();
		Camera.Size biggestSize = null;
		Camera.Size fitSize = null;// 优先选预览界面的尺寸
		Camera.Size previewSize = parametes.getPreviewSize();
		float previewSizeScale = 0;
		if(previewSize != null) {
			previewSizeScale = previewSize.width / (float) previewSize.height;
		}

		if(localSizes != null) {
			int cameraSizeLength = localSizes.size();
			for(int n = 0; n < cameraSizeLength; n++) {
				Camera.Size size = localSizes.get(n);
				if(biggestSize == null) {
					biggestSize = size;
				} else if(size.width >= biggestSize.width && size.height >= biggestSize.height) {
					biggestSize = size;
				}

				// 选出与预览界面等比的最高分辨率
				if(previewSizeScale > 0
						&& size.width >= previewSize.width && size.height >= previewSize.height) {
					float sizeScale = size.width / (float) size.height;
					if(sizeScale == previewSizeScale) {
						if(fitSize == null) {
							fitSize = size;
						} else if(size.width >= fitSize.width && size.height >= fitSize.height) {
							fitSize = size;
						}
					}
				}
			}

			// 如果没有选出fitSize, 那么最大的Size就是FitSize
			if(fitSize == null) {
				fitSize = biggestSize;
			}

			//parametes.setPictureSize(fitSize.width, fitSize.height);
			parametes.setPictureSize(biggestSize.width, biggestSize.height);
		}
	}

	// 拍照状态变化时调用该方法
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
							   int height) {
		try {
			initCameraParams();

		} catch (Exception e) {
			e.printStackTrace();
			icamera.cameraError(e);
		}
	}

	private void initCameraParams(){
		if(camera != null){
			//设置camera预览的角度
			int degreeTemp = CameraUtil.getDisplayRotation((Activity)context);
			int mDisplayOrientation = CameraUtil.getDisplayOrientation(degreeTemp, cameraPosition);
			degree = mDisplayOrientation;
			camera.setDisplayOrientation(mDisplayOrientation);
			//camera.setDisplayOrientation(getPreviewDegree((Activity)context));
			parameters = camera.getParameters(); // 获取各项参数  
			//设置zoombar
			if(icamera != null){
				//icamera.setSeekBar(parameters.getMaxZoom());
				icamera.setSeekBar(parameters.getMinExposureCompensation(),
						parameters.getMaxExposureCompensation(),parameters.getExposureCompensation());
			}

			////设置自动对焦
			//if(cameraPosition == 0) {
			//	parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
			//}
			//parameters.setPreviewFrameRate(5);  //设置每秒显示4帧  
			//// 设置保存的图片尺寸  
			//List<Size> piclist = parameters.getSupportedPictureSizes();
			//List<Size> prelist = parameters.getSupportedPreviewSizes();
			//Size s = MyCamPara.getInstance().getPictureSize(piclist, 720);
			//Size s_pre = MyCamPara.getInstance().getPreviewSize(prelist, getScreenWidth(context));

			parameters.setPictureFormat(ImageFormat.JPEG); // 设置图片格式  
			parameters.setJpegQuality(100); // 设置照片质量
//			parameters.setPictureSize(size.width,size.height);
//			parameters.setPreviewSize(size_pre.width, size_pre.height); // 设置预览大小
			setPreviewSize(parameters);
			setPictureSize(parameters);

//			if(degree == 0 || degree == 180){
//				//parameters.setPictureSize(size.height,size.width);
//				//parameters.setPreviewSize(size_pre.height,size_pre.width); // 设置预览大小  
//			}
//			else if(degree == 90 || degree == 270){
//				//parameters.setPictureSize(size.width,size.height);
//				//parameters.setPreviewSize(size_pre.width,size_pre.height); // 设置预览大小  
//			}

			camera.setParameters(parameters);
		}

	}

	private int getCustomExposureCompensation(int value){
		int maxExposure = parameters.getMaxExposureCompensation();
		int minExposure = parameters.getMinExposureCompensation();
//		int currExposure = parameters.getExposureCompensation();
//		Log.e(TAG, "maxExposure : " + maxExposure);
//		Log.e(TAG, "minExposure : " + minExposure);
//		Log.e(TAG, "currExposure : " + currExposure);

		if(value < minExposure  || value > maxExposure){
			return 0;
		}

		return value;
	}

	public void setCustomExposureCompensation(int value){
		//camera.setParameters(parameters);

		if(camera != null){
			try {
				parameters = camera.getParameters(); // 获取各项参数
				parameters.setExposureCompensation(getCustomExposureCompensation(value));
				camera.setParameters(parameters);

			} catch (Exception e) {
				e.printStackTrace();
				icamera.cameraError(e);
			}
		}

	}

	public int getCustomMaxZoom(){
		return parameters.getMaxZoom();
	}

	/**
	 *
	 * @method 获取屏幕宽度
	 * @param
	 * @return
	 * @throws
	 * @since v1.0
	 */
	private int getScreenWidth(Context context) {
		WindowManager manager = ((Activity) context).getWindowManager();
		int width = 0;
		width = manager.getDefaultDisplay().getWidth();

		return width;
	}

	/**
	 *
	 * @method 获取屏幕高度
	 * @param
	 * @return
	 * @throws
	 * @since v1.0
	 */
	public static int getScreenHeight(Context context) {
		WindowManager manager = ((Activity) context).getWindowManager();
		int height = 0;
		height = manager.getDefaultDisplay().getHeight();

		return height;
	}

	// 停止拍照时调用该方法
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (camera != null) {
			try {
				camera.setPreviewCallback(null);
				camera.stopPreview();
				camera.release(); // 释放照相机
				camera = null;

			} catch (Exception e) {
				e.printStackTrace();
				icamera.cameraError(e);
			}
		}
	}

//	protected void setDisplayOrientation(Camera camera, int angle) {
//		Method downPolymorphic;
//		try {
//			downPolymorphic = camera.getClass().getMethod(
//					"setDisplayOrientation", new Class[] { int.class });
//			if (downPolymorphic != null)
//				downPolymorphic.invoke(camera, new Object[] { angle });
//		} catch (Exception e1) {
//		}
//	}

//	// 提供一个静态方法，用于根据手机方向获得相机预览画面旋转的角度
//	public int getPreviewDegree(Activity activity) {
//		// 获得手机的方向
//		int rotation = activity.getWindowManager().getDefaultDisplay()
//				.getRotation();
//		degree = 0;
//		// 根据手机的方向计算相机预览画面应该选择的角度
//		switch (rotation) {
//		case Surface.ROTATION_0:
//			degree = 90;
//			break;
//		case Surface.ROTATION_90:
//			degree = 0;
//			break;
//		case Surface.ROTATION_180:
//			degree = 270;
//			break;
//		case Surface.ROTATION_270:
//			degree = 180;
//			break;
//		}
//		return degree;
//	}

	public void adjustZoom(int zoom){
		if(camera != null){
			try {
				parameters = camera.getParameters(); // 获取各项参数  
				parameters.setZoom(zoom);
				camera.setParameters(parameters);

			} catch (Exception e) {
				e.printStackTrace();
				icamera.cameraError(e);
			}
		}
	}

	public int getCustomZoom(){
		parameters = camera.getParameters(); // 获取各项参数
		return parameters.getZoom();
	}

	/**
	 * 对焦回调
	 */
	AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			if(success){
				//shootSound("file:///system/media/audio/ui/camera_focus.ogg");
				Log.e(TAG, "autoFocusCallback");
			}
		}
	};

//	@Override
//	public boolean onTouchEvent(MotionEvent event) {// 屏幕触摸事件
//		if(camera != null){
//			if (event.getAction() == MotionEvent.ACTION_DOWN) {// 按下时自动对焦
//				camera.autoFocus(autoFocusCallback);
//			}
//			if (event.getAction() == MotionEvent.ACTION_UP) {// 放开后拍照
//				//camera.takePicture(null, null, this);
//			}
//		}
//		return true;
//	}

	/**
	 * 拍照
	 */
	public void camera() {
		if(camera != null){
			try {
				camera.takePicture(sh, null,null,this);
			} catch (Exception e) {
				//e.printStackTrace();
				icamera.cameraError(e);
			}
		}

	}

	ShutterCallback sh = new ShutterCallback() {

		@Override
		public void onShutter() {
			shootSound("file:///system/media/audio/ui/camera_click.ogg");
		}
	};

	/**
	 *   播放系统拍照声音 
	 */
	public void shootSound(String uri)
	{
		if(!TextUtils.isEmpty(uri)){
			//AudioManager meng = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);  
			//int volume = meng.getStreamVolume( AudioManager.STREAM_NOTIFICATION);  
			shootMP = MediaPlayer.create(getContext(), Uri.parse(uri));
			if(shootMP != null){
				try {
					shootMP.prepareAsync();
					shootMP.start();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
			}

		}
	}

	/**
	 * 对焦
	 */
	public void focus(AutoFocusCallback autoFocusCallback){
		if(camera != null){
			try {

				camera.autoFocus(autoFocusCallback);
			} catch (Exception e) {
				e.printStackTrace();
				//icamera.cameraError(e);
			}
		}
	}

	/**
	 * 手动聚焦 
	 *  @param point 触屏坐标
	 */
	public void onFocus(Point point,AutoFocusCallback callback){
		if(camera == null){
			return;
		}
		Camera.Parameters parameters=camera.getParameters();
		//不支持设置自定义聚焦，则使用自动聚焦，返回
		if (parameters.getMaxNumFocusAreas()<=0) {
			try {
				camera.autoFocus(callback);
			} catch (Exception e) {
				e.printStackTrace();
				//Toast.makeText(getContext(), "打开相机失败,请开启相机权限", Toast.LENGTH_SHORT).show();
			}
			return;
		}
		List<Area> areas=new ArrayList<Camera.Area>();
		int left=point.x-300;
		int top=point.y-300;
		int right=point.x+300;
		int bottom=point.y+300;
		left=left<-1000?-1000:left;
		top=top<-1000?-1000:top;
		right=right>1000?1000:right;
		bottom=bottom>1000?1000:bottom;
		areas.add(new Area(new Rect(left,top,right,bottom), 100));
		try {
			parameters.setFocusAreas(areas);
			//使用小米手机在设置聚焦区域的时候经常会出异常，看日志发现是框架层的字符串转int的时候出错了，
			//目测是小米修改了框架层代码导致，在此try掉，对实际聚焦效果没影响
			camera.setParameters(parameters);
			camera.autoFocus(callback);
		} catch (Exception e) {
			e.printStackTrace();
			//Toast.makeText(getContext(), "打开相机失败,请开启相机权限", Toast.LENGTH_SHORT).show();
		}
	}

	public void flash(int status){
		parameters = camera.getParameters();
		switch (status) {
			case 0://关闭
				parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
				break;
			case 1://打开
				parameters.setFlashMode(Parameters.FLASH_MODE_ON);
				break;
			case 2://自动
				parameters.setFlashMode(Parameters.FLASH_MODE_AUTO);
				break;

			default:
				break;
		}
		camera.setParameters(parameters);
		camera.startPreview();//开始预览
	}

	public void changeCamera(){
		//切换前后摄像头
		int cameraCount = 0;
		CameraInfo cameraInfo = new CameraInfo();
		cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数

		for(int i = 0; i < cameraCount; i++   ) {
			Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
			if(cameraPosition == 0) {
				//现在是后置，变更为前置
				if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
					cameraPosition = 1;
					camera.stopPreview();//停掉原来摄像头的预览
					camera.release();//释放资源
					camera = null;//取消原来摄像头
					camera = Camera.open(i);//打开当前选中的摄像头
					try {
						camera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
					} catch (IOException e) {
						e.printStackTrace();
						icamera.cameraError(e);
					}
					initSize();
					initCameraParams();
					camera.startPreview();//开始预览
					camera.autoFocus(autoFocusCallback);
					break;
				}
			} else if(cameraPosition == 1){
				//现在是前置， 变更为后置
				if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
					cameraPosition = 0;
					camera.stopPreview();//停掉原来摄像头的预览
					camera.release();//释放资源
					camera = null;//取消原来摄像头
					camera = Camera.open(i);//打开当前选中的摄像头
					try {
						camera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
					} catch (IOException e) {
						e.printStackTrace();
						icamera.cameraError(e);
					}
					initSize();
					initCameraParams();
					camera.startPreview();//开始预览
					camera.autoFocus(autoFocusCallback);
					break;
				}
			}

		}
	}


	@Override
	public void onPictureTaken(byte[] data, Camera camera) {// 拍摄完成后保存照片
		try {
			// String path = Environment.getExternalStorageDirectory()
			// + "/test.jpg";
			// data2file(data, path);
			camera.startPreview();// 重新开始预览
			// 保存图片到sd卡中
			//saveToSDCard(data); 
			if(icamera != null){
				icamera.saveToSDCard(data,degree,cameraPosition);
			}
		} catch (Exception e) {

		}
	}

	/**
	 * 判断是否横屏
	 *
	 * @return
	 */
	public boolean isScreenHorizontal() {

		Configuration mConfiguration = this.getResources().getConfiguration(); // 获取设置的配置信息
		int ori = mConfiguration.orientation; // 获取屏幕方向
		if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
			// 横屏
			return true;
		} else if (ori == mConfiguration.ORIENTATION_PORTRAIT) {
			// 竖屏
			return false;
		}
		return false;
	}

	// private void data2file(byte[] w, String fileName) throws Exception {//
	// 将二进制数据转换为文件的函数
	// FileOutputStream out = null;
	// try {
	// out = new FileOutputStream(fileName);
	// out.write(w);
	// out.close();
	// } catch (Exception e) {
	// if (out != null)
	// out.close();
	// throw e;
	// }
	// }

}