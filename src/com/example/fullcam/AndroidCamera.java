package com.example.fullcam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
//import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.TextView;

public class AndroidCamera extends Activity implements SurfaceHolder.Callback, OnClickListener{
	
	Camera mCamera;
	SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	boolean previewing = false;
	ImageButton ibCapture;
	LayoutInflater controlInflater = null;
	int stillCount = 0;
	private static final String TAG = "AndroidCamera";
	private static final int MEDIA_TYPE_IMAGE = 1;
	
	TextView tvAccel, tvGyro, tvSaved, tvCount;
	SensorManager mSensorManager, mAccelSensor, mGyroSensor;
	SensorEventListener mSensorListener;
	Sensor mAccel, mGyro;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //Set orientation to portrait
		initalizeSurfaceView();
		initializeSensorView();
		tvSaved = (TextView) findViewById(R.id.tvSaved);
		tvCount = (TextView) findViewById(R.id.tvCount);
		ibCapture = (ImageButton) findViewById(R.id.ibCapture);
		ibCapture.setOnClickListener(this);
		
		
		//Save picture to gallery
		/*Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		File fileLocation = new File("/storage/sdcard0/DCIM/Camera/.jpg");
		Uri contentUri = Uri.fromFile(fileLocation);
		mediaScanIntent.setData(contentUri);
		this.sendBroadcast(mediaScanIntent);*/
	}

	private void initializeSensorView() {
		tvAccel = (TextView) findViewById(R.id.tvAccel);
		tvGyro = (TextView) findViewById(R.id.tvGyro);
		
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensorListener = new SensorEventListener() {

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSensorChanged(SensorEvent event) {
				// TODO Auto-generated method stub
				Sensor sensor = event.sensor;
				if(sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
					//Accelerometer sensor has 3 values, one for each axis
					float x_accel = event.values[0];
					float y_accel = event.values[1];
					float z_accel = event.values[2];
					double force = Math.sqrt(x_accel*x_accel +y_accel*y_accel+z_accel*z_accel) - 9.81; //Linear acceleration index
					tvAccel.setText("Accelerometer: " + 
									"\n" +"x: " + x_accel + "\t m/s2" + 
									"\n" +"y: " + y_accel + "\t m/s2" + 
									"\n" +"z: " + z_accel + "\t m/s2" +
									"\n" +"Force: " + force);
					tvAccel.setTextColor(Color.WHITE);
				} else if(sensor.getType() == Sensor.TYPE_GYROSCOPE) {
					//Gyroscope sensor has 3 values, one for each axis
					float x_gyro = event.values[0];
					float y_gyro = event.values[1];
					float z_gyro = event.values[2];
					tvGyro.setText("Gyroscope: " +
									"\n"+"x: " + x_gyro + "\trad/s" +
									"\n"+"y: " + y_gyro + "\trad/s" +
									"\n"+"y: " + z_gyro + "\trad/s");
					tvGyro.setTextColor(Color.WHITE);
				}
			}
		};
		
		//List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);//List all available sensors on the list deviceSensors
		//listSensors(deviceSensors, tvAccel);
		
		mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
		ibCapture.setEnabled(false);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(mCamera != null) {
			mCamera.release();
			
		}
	}
	/* CAUSES ERROR HERE
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Camera.open();
	}
	*/
	

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub
		if(previewing) {
			mCamera.stopPreview();
			previewing = false;
		}
		
		if(mCamera != null) {
			try {
				mCamera.setPreviewDisplay(surfaceHolder);
				mCamera.startPreview();
				
				//Use the Auto focus camera feature
				Camera.Parameters params = mCamera.getParameters();//get Camera parameters
				params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//set the focus mode
				mCamera.setParameters(params);
				previewing = true;
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		mCamera = Camera.open();
		
		//Set the portrait orientation of the camera pictures as well as the display
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setRotation(90);
		mCamera.setParameters(parameters);
		mCamera.setDisplayOrientation(90);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub

	}

	//PRIVATE METHODS START HERE
	
	private void initalizeSurfaceView() {
		// TODO Auto-generated method stub
		getWindow().setFormat(PixelFormat.UNKNOWN);
		surfaceView = (SurfaceView) findViewById(R.id.camerapreview);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		
		controlInflater = LayoutInflater.from(getBaseContext());
		View viewControl = controlInflater.inflate(R.layout.control, null);
		
		LayoutParams layoutParamControl = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		this.addContentView(viewControl, layoutParamControl);
	}
	
	/*private void listSensors(List<Sensor> deviceSensors, TextView tvDisplay) {
		for(int i = 1; i < deviceSensors.size(); i++) {
			tvDisplay.append("\n" + deviceSensors.get(i).getName());
		}
	}*/
	
	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			Log.d(TAG, "onShutter'd");
		}
	};
	
	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d(TAG, "onPictureTaken - raw with data = " + ((data != null) ? data.length : " NULL"));
		}
	};
	
	
	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			/*FileOutputStream outStream = null;
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
			try {
				// write to local sandbox file system
				// Or write to sdcard
				outStream = new FileOutputStream(String.format("/storage/sdcard0/DCIM/Camera/IMG_" + timeStamp + "_" + stillCount + ".jpg",
						System.currentTimeMillis()));
				outStream.write(data);
				outStream.close();
				Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);	
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
			Log.d(TAG, "onPictureTaken - jpeg");*/
			
			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if(pictureFile == null) {
				Log.d(TAG, "Error creating media file, check storage permissions.");
				return;
			}
			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				tvSaved.setText("Image saved: " + pictureFile);
				tvSaved.setTextColor(Color.WHITE);
				fos.close();
			} catch(FileNotFoundException e) {
				Log.d(TAG, "File not found: "+ e.getMessage());
			} catch(IOException e) {
				Log.d(TAG, "Error accessing file: "+ e.getMessage());
			}
			
			try {
				mCamera.startPreview();
				stillCount++;
				
				if(stillCount < 20) {
					mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
					tvCount.setText(stillCount + " pictures saved.");
					tvCount.setTextColor(Color.GREEN);
				} else {
					stillCount = 0;
					Thread.sleep(1000);
					tvCount.setText(" ");
					tvSaved.setText(" ");
					ibCapture.setEnabled(true);
				}
			} catch(Exception e) {
				Log.d(TAG, "Error starting preview: " + e.toString());
			}
		}
	};
	
	private File getOutputMediaFile(int type) {
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "FullCam");
		
		//Create the storage directory if it does not exist already
		if(!mediaStorageDir.exists()) {
			if(!mediaStorageDir.mkdirs()) {
				Log.d("FullCam", "failed to create directory");
				return null;
			}
		}
		// Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } else {
	        return null;
	    }
	    return mediaFile;
	}
		
}

