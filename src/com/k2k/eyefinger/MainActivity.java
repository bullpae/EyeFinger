package com.k2k.eyefinger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

//ActionBarActivity AppCompatActivity

public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

//	private static final String TAG = "Sample::Activity";
//	private FdView mView;
//	private int mDetectorType = 0;
//	private TextView matching_method;
//	public static int method = 1;
//	private String[] mDetectorName;
//	private CameraBridgeViewBase mOpenCvCameraView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(
				R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

		// mOpenCvCameraView = (CameraBridgeViewBase)
		// findViewById(R.id.eye_intention_surface_view);
		// mOpenCvCameraView.setCvCameraViewListener(this);

		// Log.i(TAG, "onCreate");
		// super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Log.i(TAG, "Trying to load OpenCV library");
		// if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this,
		// mOpenCVCallBack)) {
		// Log.e(TAG, "Cannot connect to OpenCV Manager");
		// }
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.container, PlaceholderFragment.newInstance(position + 1)).commit();
	}

	public void onSectionAttached(int number) {
		switch (number) {
		case 1:
			mTitle = getString(R.string.title_section1);
			break;
		case 2:
			mTitle = getString(R.string.title_section2);
			break;
		case 3:
			mTitle = getString(R.string.title_section3);
			break;
		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment implements CvCameraViewListener2 {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		private static final String TAG = "CameraViewFragment";
		private CameraBridgeViewBase mOpenCvCameraView;

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			if (mOpenCvCameraView != null) {
				mOpenCvCameraView.disableView();
			}
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);

			Log.i(TAG, "called onCreate");
			super.onCreate(savedInstanceState);
			getActivity().getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);

			getActivity().setContentView(R.layout.fragment_main);

			mOpenCvCameraView = (CameraBridgeViewBase) getActivity().findViewById(R.id.javaCameraView1);
			mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
			mOpenCvCameraView.setCvCameraViewListener(this);

			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
		}

		@Override
		public void onPause() {
			super.onPause();
			if (mOpenCvCameraView != null)
				mOpenCvCameraView.disableView();
		}

		@Override
		public void onResume() {
			super.onResume();
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this.getActivity(), mOpenCVCallBack);
		}

		@Override
		public void onCameraViewStarted(int width, int height) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onCameraViewStopped() {
			// TODO Auto-generated method stub

		}

		@Override
		public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
			// TODO Auto-generated method stub
			return inputFrame.rgba();
		}
		
		//private static final String TAG = "PlaceholderFragment";
		private FdView mView;
		private int mDetectorType = 0;
		private TextView matching_method;
		public static int method = 1;
		private String[] mDetectorName;
		
		private File mCascadeFile;
		private CascadeClassifier mFaceDetector;
		private CascadeClassifier mEyeDetector;
		
		private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this.getActivity()) {
			@Override
			public void onManagerConnected(int status) {
				switch (status) {
				case LoaderCallbackInterface.SUCCESS: {
					Log.i(TAG, "OpenCV loaded Successfully");
					mOpenCvCameraView.enableView();
				}
					break;
				default: {
					super.onManagerConnected(status);
				}
					break;
				}
				/*
				switch (status) {
				case LoaderCallbackInterface.SUCCESS: {
					
					try {
						InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
						File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
						mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
						FileOutputStream os = new FileOutputStream(mCascadeFile);

						byte[] buffer = new byte[4096];
						int bytesRead;
						while ((bytesRead = is.read(buffer)) != -1) {
							os.write(buffer, 0, bytesRead);
						}
						is.close();
						os.close();

						InputStream iser = getResources().openRawResource(R.raw.haarcascade_lefteye_2splits);
						File RightEyecascadeDir = getDir("RightEyecascade", Context.MODE_PRIVATE);
						File right_eye_cascadeFile = new File(RightEyecascadeDir, "haarcascade_eye_right.xml");
						FileOutputStream oser = new FileOutputStream(right_eye_cascadeFile);

						byte[] bufferER = new byte[4096];
						int bytesReadER;
						while ((bytesReadER = iser.read(bufferER)) != -1) {
							oser.write(bufferER, 0, bytesReadER);
						}
						iser.close();
						oser.close();

						mFaceDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());

						mEyeDetector = new CascadeClassifier(right_eye_cascadeFile.getAbsolutePath());
						cascadeDir.delete();
						RightEyecascadeDir.delete();

					} catch (IOException e) {
						e.printStackTrace();
					}
					mOpenCvCameraView.setCameraIndex(1);
					// mOpenCvCameraView.enableFpsMeter();
					mOpenCvCameraView.enableView();

				}
					break;
				default: {
					super.onManagerConnected(status);
				}
					break;
				}
				*/
			}
		};
	

/*
			@Override
			public void onManagerConnected(int status) {
				switch (status) {
				case LoaderCallbackInterface.SUCCESS: {
					
					Log.i(TAG, "OpenCV loaded successfully");

					// Load native libs after OpenCV initialization
					// System.loadLibrary("detection_based_tracker");

					// Create and set View
					mView = new FdView(mAppContext);
					mView.setDetectorType(mDetectorType);
					mView.setMinFaceSize(0.2f);

//					VerticalSeekBar VerticalseekBar = new VerticalSeekBar(getApplicationContext());
//					VerticalseekBar.setMax(5);
//					VerticalseekBar.setPadding(20, 20, 20, 20);
//					RelativeLayout.LayoutParams vsek = new RelativeLayout.LayoutParams(
//							RelativeLayout.LayoutParams.WRAP_CONTENT, 400);
//					vsek.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//					VerticalseekBar.setId(1);
//					VerticalseekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
//
//						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//
//							method = progress;
//							switch (method) {
//							case 0:
//								matching_method.setText("TM_SQDIFF");
//								break;
//							case 1:
//								matching_method.setText("TM_SQDIFF_NORMED");
//								break;
//							case 2:
//								matching_method.setText("TM_CCOEFF");
//								break;
//							case 3:
//								matching_method.setText("TM_CCOEFF_NORMED");
//								break;
//							case 4:
//								matching_method.setText("TM_CCORR");
//								break;
//							case 5:
//								matching_method.setText("TM_CCORR_NORMED");
//								break;
//							}
//
//						}
//
//						public void onStartTrackingTouch(SeekBar seekBar) {
//						}
//
//						public void onStopTrackingTouch(SeekBar seekBar) {
//						}
//					});

//					matching_method = new TextView(getApplicationContext());
//					matching_method.setText("TM_SQDIFF");
//					matching_method.setTextColor(Color.YELLOW);
//					RelativeLayout.LayoutParams matching_method_param = new RelativeLayout.LayoutParams(
//							RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//					matching_method_param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//					matching_method_param.addRule(RelativeLayout.BELOW, VerticalseekBar.getId());
//
//					Button btn = new Button(getApplicationContext());
//					btn.setText("Create template");
//					RelativeLayout.LayoutParams btnp = new RelativeLayout.LayoutParams(
//							RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//					btnp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//					btn.setId(2);
//
//					btn.setOnClickListener(new OnClickListener() {
//						public void onClick(View v) {
//							mView.resetLearFramesCount();
//						}
//					});
//
//					RelativeLayout frameLayout = new RelativeLayout(getApplicationContext());
//					frameLayout.addView(mView, 0);
//					frameLayout.addView(btn, btnp);
//
//					frameLayout.addView(VerticalseekBar, vsek);
//					frameLayout.addView(matching_method, matching_method_param);
//
//					setContentView(frameLayout);

					// Check native OpenCV camera
					if (!mView.openCamera()) {
//						AlertDialog ad = new AlertDialog.Builder(mAppContext).create();
//						ad.setCancelable(false); // This blocks the 'BACK' button
//						ad.setMessage("Fatal error: can't open camera!");
//						ad.setButton("OK", new DialogInterface.OnClickListener() {
//							public void onClick(DialogInterface dialog, int which) {
//								dialog.dismiss();
//								finish();
//							}
//						});
//						ad.show();
					}
				}
					break;
				default: {
					super.onManagerConnected(status);
				}
					break;
				}
				
			}

		};
*/
	}

//	private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
//		@Override
//		public void onManagerConnected(int status) {
//			switch (status) {
//			case LoaderCallbackInterface.SUCCESS: {
//				Log.i(TAG, "OpenCV loaded successfully");
//
//				// Load native libs after OpenCV initialization
//				// System.loadLibrary("detection_based_tracker");
//
//				// Create and set View
//				mView = new FdView(mAppContext);
//				mView.setDetectorType(mDetectorType);
//				mView.setMinFaceSize(0.2f);
//
//				VerticalSeekBar VerticalseekBar = new VerticalSeekBar(getApplicationContext());
//				VerticalseekBar.setMax(5);
//				VerticalseekBar.setPadding(20, 20, 20, 20);
//				RelativeLayout.LayoutParams vsek = new RelativeLayout.LayoutParams(
//						RelativeLayout.LayoutParams.WRAP_CONTENT, 400);
//				vsek.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//				VerticalseekBar.setId(1);
//				VerticalseekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
//
//					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//
//						method = progress;
//						switch (method) {
//						case 0:
//							matching_method.setText("TM_SQDIFF");
//							break;
//						case 1:
//							matching_method.setText("TM_SQDIFF_NORMED");
//							break;
//						case 2:
//							matching_method.setText("TM_CCOEFF");
//							break;
//						case 3:
//							matching_method.setText("TM_CCOEFF_NORMED");
//							break;
//						case 4:
//							matching_method.setText("TM_CCORR");
//							break;
//						case 5:
//							matching_method.setText("TM_CCORR_NORMED");
//							break;
//						}
//
//					}
//
//					public void onStartTrackingTouch(SeekBar seekBar) {
//					}
//
//					public void onStopTrackingTouch(SeekBar seekBar) {
//					}
//				});
//
//				matching_method = new TextView(getApplicationContext());
//				matching_method.setText("TM_SQDIFF");
//				matching_method.setTextColor(Color.YELLOW);
//				RelativeLayout.LayoutParams matching_method_param = new RelativeLayout.LayoutParams(
//						RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//				matching_method_param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//				matching_method_param.addRule(RelativeLayout.BELOW, VerticalseekBar.getId());
//
//				Button btn = new Button(getApplicationContext());
//				btn.setText("Create template");
//				RelativeLayout.LayoutParams btnp = new RelativeLayout.LayoutParams(
//						RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//				btnp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//				btn.setId(2);
//
//				btn.setOnClickListener(new OnClickListener() {
//					public void onClick(View v) {
//						mView.resetLearFramesCount();
//					}
//				});
//
//				RelativeLayout frameLayout = new RelativeLayout(getApplicationContext());
//				frameLayout.addView(mView, 0);
//				frameLayout.addView(btn, btnp);
//
//				frameLayout.addView(VerticalseekBar, vsek);
//				frameLayout.addView(matching_method, matching_method_param);
//
//				setContentView(frameLayout);
//
//				// Check native OpenCV camera
//				if (!mView.openCamera()) {
//					AlertDialog ad = new AlertDialog.Builder(mAppContext).create();
//					ad.setCancelable(false); // This blocks the 'BACK' button
//					ad.setMessage("Fatal error: can't open camera!");
//					ad.setButton("OK", new DialogInterface.OnClickListener() {
//						public void onClick(DialogInterface dialog, int which) {
//							dialog.dismiss();
//							finish();
//						}
//					});
//					ad.show();
//				}
//			}
//				break;
//			default: {
//				super.onManagerConnected(status);
//			}
//				break;
//			}
//		}
//	};
}
