package com.cheguo.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.cheguo.camera.helper.PermissionUtils;
import com.cheguo.camera.view.CameraUtil;
import com.cheguo.camera.view.CameraView;
import com.cheguo.camera.view.FocusImageView;
import com.cheguo.camera.view.ICamera;
import com.cheguo.camera.view.ISOSeekBarPopwindow;
import com.cheguo.camera.view.SeekBarParams;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CameraActivity extends Activity implements OnClickListener,AdapterView.OnItemClickListener, ICamera {
	private String TAG = "CameraActivity";
	CameraView cameraView;
	/** 触摸屏幕时显示的聚焦图案  */
	private FocusImageView mFocusImageView;
	private View layout;
	private LinearLayout linearLayout;
	private ScrollView scrollView;
	private ListView photoListview;
	private CameraPhotosAdapter adapter;
	private ArrayList<String> listPath = new ArrayList<>();// 存放路径的list

	private ArrayList<String> listImage = new ArrayList<>();// 存放图片的list
	private Button takepicture;

	private Button flash;
	private Button cancel;
	private Button confirm;
	private Button changeCamera;
	private Button isoBtn;

	ExecutorService fixedThreadPool;

	private final String IMAGE_FOLDER = "/my_camera";//图片文件夹

	SeekBar zoomBar;
	LinearLayout zoomBarLayout;
	private int flashStatus;
	SeekBarParams seekBarParams;
	ISOSeekBarPopwindow isoSeekBarPopwindow;

	byte[] data;
	int degree;
	int cameraNum;
	String imageType;

	int isComposite;//是否打水印 0不打 1打

	public final static String PARAMS_IMAGE_LIST = "PARAMS_IMAGE_LIST";
	public final static String PARAMS_IMAGE_TYPE = "PARAMS_IMAGE_TYPE";
	public final static String PARAMS_IMAGE_WIDTH = "PARAMS_IMAGE_WIDTH";
	public final static String PARAMS_IMAGE_HEIGHT = "PARAMS_IMAGE_HEIGHT";
	public final static String PARAMS_CALLBACK = "PARAMS_CALLBACK";
	public final static String PARAMS_RESULTCODE = "PARAMS_RESULTCODE";

	private final int DEFAULT_IMAGE_WIDTH = 1440;
	private final int DEFAULT_IMAGE_HEIGHT = 2560;

	private int IMAGE_WIDTH = DEFAULT_IMAGE_WIDTH;
	private int IMAGE_HEIGHT = DEFAULT_IMAGE_HEIGHT;

	private int resultCode;
	private ImageCallback imageCallback;

	//使用相机需要的权限
	private final String[] PERMISSIONS = new String[]{Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE
			, Manifest.permission.WRITE_EXTERNAL_STORAGE};

	// 打开相机请求Code，多个权限请求Code
	private final int REQUEST_CODE_PERMISSIONS=1;

	public static void launch(Activity context,int width,int height,int resultCode,ImageCallback imageCallback){
		Intent intent = new Intent(context,CameraActivity.class);
		intent.putExtra(PARAMS_IMAGE_WIDTH,width);
		intent.putExtra(PARAMS_IMAGE_HEIGHT,height);
		intent.putExtra(PARAMS_RESULTCODE,resultCode);
		intent.putExtra(PARAMS_CALLBACK,imageCallback);
		context.startActivityForResult(intent,resultCode);

	}

	public interface ImageCallback extends Serializable {
		void setImagePaths(ArrayList<String> listImage);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_custome_camera);
		initView();
		initData(savedInstanceState);
		//检查权限
		//requestMorePermissions(this);

	}

	// 普通申请多个权限
	private void requestMorePermissions(final Context mContext){
		PermissionUtils.checkAndRequestMorePermissions(mContext, PERMISSIONS, REQUEST_CODE_PERMISSIONS,
				new PermissionUtils.PermissionRequestSuccessCallBack() {
					@Override
					public void onHasPermission() {
						// 权限已被授予
						//initCamera();
						//cameraErrorDone();
					}
				});
	}

	/**
	 * 初始化相机
	 */
	public void initCamera(){
		takepicture.setVisibility(View.VISIBLE);
		flash.setVisibility(View.VISIBLE);
		changeCamera.setVisibility(View.VISIBLE);
		zoomBarLayout.setVisibility(View.VISIBLE);

		cameraView.startCamera(true);
	}


	/**
	 * 权限回调
	 * @param requestCode
	 * @param permissions
	 * @param grantResults
     */
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case REQUEST_CODE_PERMISSIONS:
				PermissionUtils.onRequestMorePermissionsResult(CameraActivity.this, PERMISSIONS, new PermissionUtils.PermissionCheckCallBack() {
					@Override
					public void onHasPermission() {
						// 权限申请成功
						initCamera();
					}

					@Override
					public void onUserHasAlreadyTurnedDown(String... permission) {
						//Toast.makeText(CameraActivity.this, "我们需要"+ Arrays.toString
						// (permission)+"权限", Toast.LENGTH_SHORT).show();
						Toast.makeText(CameraActivity.this,"相机未授权,请设置权限",Toast.LENGTH_SHORT).show();
						cameraErrorDone();
					}

					@Override
					public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
						//Toast.makeText(CameraActivity.this, "我们需要"+ Arrays.toString(permission)+"权限", Toast.LENGTH_SHORT).show();
						CameraUtils.showToAppSettingDialog(CameraActivity.this);
					}
				});

		}
	}

	@Override
	public void onConfigurationChanged(android.content.res.Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private void initView(){
		layout = this.findViewById(R.id.buttonLayout);
		layout.getBackground().setAlpha(100);
		linearLayout = (LinearLayout) findViewById(R.id.linearlayout_images);
		scrollView = (ScrollView) findViewById(R.id.gallery_images);
		changeCamera = (Button) findViewById(R.id.singlepicture);
		takepicture = (Button) findViewById(R.id.takepicture);
		flash = (Button) findViewById(R.id.flash);
		cancel = (Button) findViewById(R.id.cancel);
		confirm = (Button) findViewById(R.id.confirm);
		cancel.setVisibility(View.GONE);
		isoBtn = (Button) findViewById(R.id.iso_btn);

		zoomBar =(SeekBar) findViewById(R.id.seek_bar);
		zoomBarLayout = (LinearLayout) findViewById(R.id.seek_bar_layout);

		mFocusImageView=(FocusImageView) findViewById(R.id.focusImageView);
		cameraView = (CameraView) this.findViewById(R.id.surfaceView);
		cameraView.setICamera(this);
		cameraView.setOnTouchListener(new TouchListener());

		photoListview = (ListView) findViewById(R.id.gallery_images_listview);
		adapter = new CameraPhotosAdapter(this, listImage);
		photoListview.setAdapter(adapter);
		photoListview.setOnItemClickListener(this);

		takepicture.setOnClickListener(this);
		flash.setOnClickListener(this);
		cancel.setOnClickListener(this);
		confirm.setOnClickListener(this);

		isoBtn.setOnClickListener(this);

		bindMenuListener();

	}

	private void initData(Bundle savedInstanceState){
		fixedThreadPool = Executors.newFixedThreadPool(1);
		imageType = getIntent().getStringExtra(PARAMS_IMAGE_TYPE);
		if(getIntent().hasExtra(PARAMS_IMAGE_WIDTH)){
			IMAGE_WIDTH = getIntent().getIntExtra(PARAMS_IMAGE_WIDTH,0) > 0 ?
					getIntent().getIntExtra(PARAMS_IMAGE_WIDTH,0) : DEFAULT_IMAGE_WIDTH;
		}
		if(getIntent().hasExtra(PARAMS_IMAGE_WIDTH)){
			IMAGE_HEIGHT = getIntent().getIntExtra(PARAMS_IMAGE_HEIGHT,0) > 0 ?
					getIntent().getIntExtra(PARAMS_IMAGE_HEIGHT,0) : DEFAULT_IMAGE_HEIGHT;
		}
		resultCode = getIntent().getIntExtra(PARAMS_RESULTCODE,0);
		imageCallback = (ImageCallback) getIntent().getSerializableExtra(PARAMS_CALLBACK);

		if (savedInstanceState != null
				&& savedInstanceState.getStringArrayList("path_list") != null){
			ArrayList<String> temp = savedInstanceState.getStringArrayList("path_list");
			if(temp.size() > 0){
				listPath.clear();
				listImage.clear();
				listPath.addAll(temp);
				adapter.refresh(listImage);
			}
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		ArrayList<String> temp = outState.getStringArrayList("path_list");
		if(temp != null){
			temp.clear();
			removeImageDelete(temp);
		}
		else{
			temp = new ArrayList<>();
			removeImageDelete(temp);
			outState.putStringArrayList("path_list", temp);
		}
	}

	private final class TouchListener implements OnTouchListener {

		/** 记录是拖拉照片模式还是放大缩小照片模式 */

		private static final int MODE_INIT = 0;
		/** 放大缩小照片模式 */
		private static final int MODE_ZOOM = 1;
		private int mode = MODE_INIT;// 初始状态 

		/** 用于记录拖拉图片移动的坐标位置 */

		private float startDis;


		@Override
		public boolean onTouch(View v, MotionEvent event) {
			/** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
				// 手指压下屏幕
				case MotionEvent.ACTION_DOWN:
					mode = MODE_INIT;
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					mode = MODE_ZOOM;
					/** 计算两个手指间的距离 */
					startDis = distance(event);
					break;
				case MotionEvent.ACTION_MOVE:
					if(mode == MODE_ZOOM){
						//只有同时触屏两个点的时候才执行
						if (event.getPointerCount() < 2) return true;
						float endDis = distance(event);// 结束距离
						//每变化10f zoom变1
						int scale = (int) ((endDis - startDis) / 10f);
						if (scale >= 1 || scale <= -1) {
							int zoom = cameraView.getCustomZoom() + scale;
							//zoom不能超出范围
							if (zoom > cameraView.getCustomMaxZoom())
								zoom = cameraView.getCustomMaxZoom();
							if (zoom < 0) zoom = 0;
							cameraView.adjustZoom(zoom);
							//将最后一次的距离设为当前距离
							startDis = endDis;
						}
					}
					break;
				// 手指离开屏幕
				case MotionEvent.ACTION_UP:
					if(mode!=MODE_ZOOM){
						//设置聚焦
						Point point=new Point((int)event.getX(), (int)event.getY());
						cameraView.onFocus(point,autoFocusCallback);
						mFocusImageView.startFocus(point);
					}
					break;
			}
			return true;
		}
		/** 计算两个手指间的距离 */
		private float distance(MotionEvent event) {
			float dx = event.getX(1) - event.getX(0);
			float dy = event.getY(1) - event.getY(0);
			/** 使用勾股定理返回两点之间的距离 */
			return (float) Math.sqrt(dx * dx + dy * dy);
		}

	}


	private final AutoFocusCallback autoFocusCallback=new AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			//聚焦之后根据结果修改图片
			if (success) {
				mFocusImageView.onFocusSuccess();
			}else {
				//聚焦失败显示的图片，由于未找到合适的资源，这里仍显示同一张图片
				mFocusImageView.onFocusFailed();

			}
		}
	};


	public void setSeekBar(int min,int max,int cur){
		seekBarParams = new SeekBarParams(min,max,cur);
		isoSeekBarPopwindow = new ISOSeekBarPopwindow(this, seekBarParams, new ISOSeekBarPopwindow.SeekCallback() {
			@Override
			public void progressChanged(int exposure) {
				cameraView.setCustomExposureCompensation(exposure);
				cameraView.focus(null);
			}
		});

		//initSeekBar(min,max,cur);
	}

	private void initSeekBar(int min,int max,int cur){
		final int abs_min = Math.abs(min);
		zoomBar.setMax(max + abs_min);
		zoomBar.setProgress(cur + abs_min);


		zoomBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {

			}

			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				//cameraView.adjustZoom(arg1);
				int exposure = arg1 - abs_min;
				Log.e(TAG,"exposure : "+ exposure);
				cameraView.setCustomExposureCompensation(exposure);
				cameraView.focus(null);

			}
		});
	}

	public void cameraError(Exception e){
		requestMorePermissions(this);
		cameraErrorDone();
		Toast.makeText(this,"无法使用相机",Toast.LENGTH_SHORT).show();
		Log.e(TAG, "cameraError ------------------" + e.getClass());
		Log.e(TAG, "cameraError ------------------" + e.getMessage());
		//finish();
	}

	private void cameraErrorDone(){
		takepicture.setVisibility(View.GONE);
		flash.setVisibility(View.GONE);
		changeCamera.setVisibility(View.GONE);
		zoomBarLayout.setVisibility(View.GONE);
	}

	@Override
	protected void onResume() {
		// 注册传感器监听
		// 参数三，检测的精准度
		cameraView.registerSensor();
		super.onResume();
	}

	protected void onPause() {
		// 取消传感器监听
		cameraView.unRegisterSensor();
		super.onPause();
	}

	public void bindMenuListener() {
		changeCamera.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				cameraView.changeCamera();
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// 进入浏览页面
//		Intent intent = new Intent(this, PhotosViewPagerActivity.class);
//		intent.setAction(Params.ACTION_IN_CAMERA);
//		intent.putExtra(Params.PARAMS_IMAGE_LIST, listImage);
//		intent.putExtra(Params.PARAMS_POSITION, position);
//		startActivityForResult(intent, Params.REQUESTCODE_PHOTO_BROWSE);
	}

	@Override
	public void onClick(View v) {
		btnOnclick(v);

	}

	/**
	 * 按钮被点击触发的事件
	 *
	 * @param v
	 */
	public void btnOnclick(View v) {
		int i1 = v.getId();
		if (i1 == R.id.takepicture) {
			if (CameraUtils.getSDFreeSize() < 10) {
				Toast.makeText(this,"SD卡空间不足",Toast.LENGTH_SHORT).show();
				return;
			}
			cameraView.camera();
			takepicture.setEnabled(false);

		} else if (i1 == R.id.cancel) {
			cancel();

		} else if (i1 == R.id.confirm) {
			confirm();

		} else if (i1 == R.id.flash) {
			flash();

		}
		else if (i1 == R.id.iso_btn) {
			isoSeekBarPopwindow.showPopupWindow(isoBtn);
		}
	}

	private void flash(){
		flashStatus++;
		if(flashStatus > 2){
			flashStatus = 0;
		}
		switch (flashStatus) {
			case 0://关闭
				flash.setBackgroundResource(R.drawable.icon_flash_off_holo_light);
				break;
			case 1://打开
				flash.setBackgroundResource(R.drawable.icon_flash_on_holo_light);
				break;
			case 2://自动
				flash.setBackgroundResource(R.drawable.icon_flash_auto_holo_light);
				break;
			default:
				break;
		}
		cameraView.flash(flashStatus);
	}

	private void removeImageDelete(ArrayList<String> temp){
		for(String path : listPath){
			if(!path.equals("NOIMAGE")){
				temp.add(path);
			}
		}

	}

	private void confirm(){
//		ArrayList<String> temp = new ArrayList<>();listImage
//		removeImageDelete(temp);
		if(imageCallback != null){
			imageCallback.setImagePaths(listImage);
		}
		Intent data = new Intent();
		data.putExtra(PARAMS_IMAGE_LIST,listImage);
		setResult(resultCode,data);
		finish();
	}

	private void cancel(){
		ArrayList<String> temp = new ArrayList<>();
		removeImageDelete(temp);
		for(String path : temp){
			CameraUtils.deleteFile(path);
		}

		finish();
	}

	/**
	 * 将拍下来的照片存放在SD卡中
	 *
	 * @param data
	 * @throws IOException
	 */
	@Override
	public void saveToSDCard(byte[] data,int degree,int cameraNum) throws IOException {

		if(data != null && data.length > 0){
			this.data = data;
			this.degree = degree;
			this.cameraNum = cameraNum;

			fixedThreadPool.execute(new Runnable() {

				@Override
				public void run() {
					long time = System.currentTimeMillis();
					String filename = CameraUtils.FormatTimeForm(time,"yyyy-MM-dd HH:mm:ss") + ".jpg";
					//+ ".jpg";

					String packageName = CameraUtils.getAppProcessName(CameraActivity.this);

					String pic_root = CameraUtils.getSDCardAbsolutePath() + packageName;
					String pic_dir = pic_root + IMAGE_FOLDER + "/";

					String cameraPath = pic_dir + filename;

					//Bitmap bp = ImageUtils.Bytes2Bimap(CameraActivity.this.data);
					Bitmap bp = CameraUtils.getSmallBitmap(CameraActivity.this.data, IMAGE_WIDTH, IMAGE_HEIGHT);
					Bitmap bp_degre = null;
					if(CameraActivity.this.cameraNum == 1){//前置摄像头
						CameraActivity.this.degree += 180;
						if(CameraActivity.this.degree > 360){
							CameraActivity.this.degree -= 360;
						}
					}

					bp_degre = CameraUtils.scalingImageView(CameraActivity.this.degree, IMAGE_HEIGHT,IMAGE_WIDTH,bp);
					//bp_degre = ImageUtils.rotaingImageView(CameraActivity.this.degree, bp);
					//boolean bo = ImageUtils.saveBitmapToFile(bp_withWord, cameraPath);
					int quality = 95;
					boolean bo = CameraUtils.saveBitmapToFile(bp_degre,pic_dir ,filename,quality);
					if(bo){
						Message msg = UIhandler.obtainMessage();
						msg.obj = cameraPath;
						UIhandler.sendMessage(msg);
					}
					if (bp != null) {
						bp.recycle();
					}
					if (bp_degre != null) {
						bp_degre.recycle();
					}


					BtnHandler.sendEmptyMessage(0);

				}
			});
		}

		//takepicture.setEnabled(true);
	}

	Handler UIhandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			String imagePath = (String) msg.obj;
			//getImageView(path);

			if(!TextUtils.isEmpty(imagePath)){
				listPath.add(imagePath);
				listImage.add(imagePath);

				adapter.refresh(listImage);
				int size = listImage.size();
				if(size > 0){
					photoListview.setSelection(size - 1);
				}
			}
		}
	};

	Handler BtnHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			takepicture.setEnabled(true);
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			//icon_cancel();
			confirm();
		}
		return super.onKeyDown(keyCode, event);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//super.onActivityResult(requestCode, resultCode, data);
//		if(resultCode == Params.REQUESTCODE_PHOTO_BROWSE && data != null){
//			ArrayList<ImageBean> list = (ArrayList<ImageBean>) data.getSerializableExtra(Params.PARAMS_IMAGE_LIST);
//			if(list != null){
//				listImage = list;
//				adapter.refresh(listImage);
//			}
//		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.e("CameraActivity", "onDestroy");
	}


}