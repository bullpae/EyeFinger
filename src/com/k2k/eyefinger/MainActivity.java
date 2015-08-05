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
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

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
		fragmentManager.beginTransaction().replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
				.commit();
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

		private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
		public static final int JAVA_DETECTOR = 0;
		private static final int TM_SQDIFF = 0;
		private static final int TM_SQDIFF_NORMED = 1;
		private static final int TM_CCOEFF = 2;
		private static final int TM_CCOEFF_NORMED = 3;
		private static final int TM_CCORR = 4;
		private static final int TM_CCORR_NORMED = 5;

		private CameraBridgeViewBase mOpenCvCameraView;
		private int mDetectorType = JAVA_DETECTOR;
		private String[] mDetectorName;

		private float mRelativeFaceSize = 0.2f;
		private int mAbsoluteFaceSize = 0;
		private Mat mRgba;
		private Mat mGray;
		private Mat mRgbaT;
		private Mat mGrayT;
		// matrix for zooming
		private Mat mZoomWindow;
		private Mat mZoomWindow2;

		double xCenter = -1;
		double yCenter = -1;
		private Mat teplateR;
		private Mat teplateL;
		private int learn_frames = 0;
		int method = 0;

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

			mMainContext = activity;
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
			// return inputFrame.rgba();


			mRgba = inputFrame.rgba();			
			mGray = inputFrame.gray();		
			Core.flip(mRgba, mRgba, 1);
			Core.flip(mGray, mGray, 1);
			
//            int height = mGray.rows();
//            int faceSize = Math.round(height * 0.5F);
//
//            Mat temp = mGray.clone();
//            Core.transpose(mGray, temp);
//            Core.flip(temp, temp, -1);
//
//            MatOfRect rectFaces = new MatOfRect();
//
//            // java detector fast
//            if (mJavaDetector != null) {
//            	mJavaDetector.detectMultiScale(temp, rectFaces, 1.1, 1, 0, new Size(faceSize, faceSize), new Size());
//            }
            
			if (mAbsoluteFaceSize == 0) {
				int height = mGray.rows();
				if (Math.round(height * mRelativeFaceSize) > 0) {
					mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
				}
			}

			if (mZoomWindow == null || mZoomWindow2 == null) {
				CreateAuxiliaryMats();
			}

			MatOfRect faces = new MatOfRect();

			if (mJavaDetector != null) {
				Mat temp = mGray.clone();
	            Core.transpose(mGray, temp);
	            Core.flip(temp, temp, -1);
	            
	            mJavaDetector.detectMultiScale(temp, faces, 1.1, 1, 0, new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
	            
				//mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
			}

			Rect[] facesArray = faces.toArray();
			for (int i = 0; i < facesArray.length; i++) {
				Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
				xCenter = (facesArray[i].x + facesArray[i].width + facesArray[i].x) / 2;
				yCenter = (facesArray[i].y + facesArray[i].y + facesArray[i].height) / 2;
				Point center = new Point(xCenter, yCenter);

				Core.circle(mRgba, center, 10, new Scalar(255, 0, 0, 255), 3);

				Core.putText(mRgba, "[" + center.x + "," + center.y + "]", new Point(center.x + 20, center.y + 20),
						Core.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(255, 255, 255, 255));

				Rect r = facesArray[i];
				// compute the eye area
				Rect eyearea = new Rect(r.x + r.width / 8, (int) (r.y + (r.height / 4.5)), r.width - 2 * r.width / 8,
						(int) (r.height / 3.0));
				// split it
				Rect eyearea_right = new Rect(r.x + r.width / 16, (int) (r.y + (r.height / 4.5)),
						(r.width - 2 * r.width / 16) / 2, (int) (r.height / 3.0));
				Rect eyearea_left = new Rect(r.x + r.width / 16 + (r.width - 2 * r.width / 16) / 2,
						(int) (r.y + (r.height / 4.5)), (r.width - 2 * r.width / 16) / 2, (int) (r.height / 3.0));

				Core.rectangle(mRgba, eyearea_left.tl(), eyearea_left.br(), new Scalar(255, 0, 0, 255), 2);
				Core.rectangle(mRgba, eyearea_right.tl(), eyearea_right.br(), new Scalar(255, 0, 0, 255), 2);

				if (learn_frames < 5) {
					teplateR = get_template(mJavaDetectorEye, eyearea_right, 24);
					teplateL = get_template(mJavaDetectorEye, eyearea_left, 24);
					learn_frames++;
				} else {
					match_eye(eyearea_right, teplateR, method);
					match_eye(eyearea_left, teplateL, method);

				}

				Imgproc.resize(mRgba.submat(eyearea_left), mZoomWindow2, mZoomWindow2.size());
				Imgproc.resize(mRgba.submat(eyearea_right), mZoomWindow, mZoomWindow.size());

			}

			return mRgba;
		}

		private Context mMainContext;

		private File mCascadeFile;
		private CascadeClassifier mJavaDetector;
		private CascadeClassifier mJavaDetectorEye;

		private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(mMainContext) {

			@Override
			public void onManagerConnected(int status) {
				switch (status) {
				case LoaderCallbackInterface.SUCCESS: {
					Log.i(TAG, "OpenCV loaded successfully");

					try {
						// load cascade file from application resource						
						InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
						File cascadeDir = mMainContext.getDir("cascade", Context.MODE_PRIVATE);
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
						File cascadeDirER = mMainContext.getDir("cascadeER", Context.MODE_PRIVATE);
						File cascadeFileER = new File(cascadeDirER, "haarcascade_eye_right.xml");
						FileOutputStream oser = new FileOutputStream(cascadeFileER);

						byte[] bufferER = new byte[4096];
						int bytesReadER;
						while ((bytesReadER = iser.read(bufferER)) != -1) {
							oser.write(bufferER, 0, bytesReadER);
						}
						iser.close();
						oser.close();

						mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
						if (mJavaDetector.empty()) {
							Log.e(TAG, "Failed to load cascade classifier");
							mJavaDetector = null;
						} else
							Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

						mJavaDetectorEye = new CascadeClassifier(cascadeFileER.getAbsolutePath());
						if (mJavaDetectorEye.empty()) {
							Log.e(TAG, "Failed to load cascade classifier");
							mJavaDetectorEye = null;
						} else
							Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

						cascadeDir.delete();

					} catch (IOException e) {
						e.printStackTrace();
						Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
					}
					mOpenCvCameraView.setCameraIndex(1);
					// mOpenCvCameraView.setRotation((float) 90.0);
					mOpenCvCameraView.enableFpsMeter();
					mOpenCvCameraView.enableView();

				}
					break;
				default: {
					super.onManagerConnected(status);
				}
					break;
				}
			}
		};

		private Mat get_template(CascadeClassifier clasificator, Rect area, int size) {
			Mat template = new Mat();
			Mat mROI = mGray.submat(area);
			MatOfRect eyes = new MatOfRect();
			Point iris = new Point();
			Rect eye_template = new Rect();
			clasificator.detectMultiScale(mROI, eyes, 1.15, 2, Objdetect.CASCADE_FIND_BIGGEST_OBJECT
					| Objdetect.CASCADE_SCALE_IMAGE, new Size(30, 30), new Size());

			Rect[] eyesArray = eyes.toArray();
			for (int i = 0; i < eyesArray.length;) {
				Rect e = eyesArray[i];
				e.x = area.x + e.x;
				e.y = area.y + e.y;
				Rect eye_only_rectangle = new Rect((int) e.tl().x, (int) (e.tl().y + e.height * 0.4), (int) e.width,
						(int) (e.height * 0.6));
				mROI = mGray.submat(eye_only_rectangle);
				Mat vyrez = mRgba.submat(eye_only_rectangle);

				Core.MinMaxLocResult mmG = Core.minMaxLoc(mROI);

				Core.circle(vyrez, mmG.minLoc, 2, new Scalar(255, 255, 255, 255), 2);
				iris.x = mmG.minLoc.x + eye_only_rectangle.x;
				iris.y = mmG.minLoc.y + eye_only_rectangle.y;
				eye_template = new Rect((int) iris.x - size / 2, (int) iris.y - size / 2, size, size);
				Core.rectangle(mRgba, eye_template.tl(), eye_template.br(), new Scalar(255, 0, 0, 255), 2);
				template = (mGray.submat(eye_template)).clone();
				return template;
			}
			return template;
		}

		private void CreateAuxiliaryMats() {
			if (mGray.empty())
				return;

			int rows = mGray.rows();
			int cols = mGray.cols();

			if (mZoomWindow == null) {
				mZoomWindow = mRgba.submat(rows / 2 + rows / 10, rows, cols / 2 + cols / 10, cols);
				mZoomWindow2 = mRgba.submat(0, rows / 2 - rows / 10, cols / 2 + cols / 10, cols);
			}

		}

		private void match_eye(Rect area, Mat mTemplate, int type) {
			Point matchLoc;
			Mat mROI = mGray.submat(area);
			int result_cols = mROI.cols() - mTemplate.cols() + 1;
			int result_rows = mROI.rows() - mTemplate.rows() + 1;

			if (mTemplate.cols() == 0 || mTemplate.rows() == 0) {
				return;
			}
			Mat mResult = new Mat(result_cols, result_rows, CvType.CV_8U);

			switch (type) {
			case TM_SQDIFF:
				Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_SQDIFF);
				break;
			case TM_SQDIFF_NORMED:
				Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_SQDIFF_NORMED);
				break;
			case TM_CCOEFF:
				Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCOEFF);
				break;
			case TM_CCOEFF_NORMED:
				Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCOEFF_NORMED);
				break;
			case TM_CCORR:
				Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCORR);
				break;
			case TM_CCORR_NORMED:
				Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCORR_NORMED);
				break;
			}

			Core.MinMaxLocResult mmres = Core.minMaxLoc(mResult);
			if (type == TM_SQDIFF || type == TM_SQDIFF_NORMED) {
				matchLoc = mmres.minLoc;
			} else {
				matchLoc = mmres.maxLoc;
			}

			Point matchLoc_tx = new Point(matchLoc.x + area.x, matchLoc.y + area.y);
			Point matchLoc_ty = new Point(matchLoc.x + mTemplate.cols() + area.x, matchLoc.y + mTemplate.rows()
					+ area.y);

			Core.rectangle(mRgba, matchLoc_tx, matchLoc_ty, new Scalar(255, 255, 0, 255));
			Rect rec = new Rect(matchLoc_tx, matchLoc_ty);

		}

	}

}
