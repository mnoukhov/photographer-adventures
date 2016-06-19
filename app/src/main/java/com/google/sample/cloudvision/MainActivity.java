/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sample.cloudvision;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final String API_KEY = BuildConfig.API_KEY;
    public static final String FILE_NAME = "temp.jpg";

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    private TextView mImageDetails;
    private TextView mSearchWord;
//    private ImageView mMainImage;

    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera.PictureCallback rawCallback;
    private Camera.ShutterCallback shutterCallback;
    private Camera.PictureCallback jpegCallback;

    private ObjectManager mObjectManager;
    private Object mCurrentObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton reportFab = (FloatingActionButton) findViewById(R.id.report_fab);
        reportFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startReportActivity(view);
            }
        });

        FloatingActionButton skipFab = (FloatingActionButton) findViewById(R.id.skip_fab);
        skipFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                skipObject();
            }
        });

        FloatingActionButton pictureFab = (FloatingActionButton) findViewById(R.id.image_fab);
        pictureFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    captureImage();
                    mImageDetails.setText("Checking your image");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        surfaceHolder = surfaceView.getHolder();

        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        final CloudVisionUtils.CloudVisionTask.CVTaskListener listener = new CloudVisionUtils.CloudVisionTask.CVTaskListener() {
            @Override
            public void onFinished(String result) {
                // Update object statistics and reinsert into object manager!
                String output;
                if (result.contains(mCurrentObject.getName().toLowerCase())) {
                    mCurrentObject.setState(Object.State.CORRECT);
                    output = String.format("Congratulations, you found the %s!", mCurrentObject.getName());
                    skipObject();
                } else {
                    mCurrentObject.setState(Object.State.SKIPPED);
                    output = "Oops! Try again." + result;
                }
                mCurrentObject.setAttempts(1 + mCurrentObject.getAttempts());
                mObjectManager.updateObject(mCurrentObject);

                // Output match results and refresh the camera
                mImageDetails.setText(output);
                refreshCamera();
            }
        };

        jpegCallback = new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                CloudVisionUtils.uploadImage(data, mCurrentObject, listener);
            }
        };

        mObjectManager = ObjectManager.getInstance(getApplicationContext());
        mImageDetails = (TextView) findViewById(R.id.image_details);
        mSearchWord = (TextView) findViewById(R.id.search_word);
        skipObject();
    }

    //Used to start ReportActivity
    public void startReportActivity(View view){
        Intent intent = new Intent(this, ReportActivity.class);
        startActivity(intent);
    }

    public void skipObject(){
        mCurrentObject = mObjectManager.getNextObject(mCurrentObject);
        mSearchWord.setText(String.format("Current Search Word: %s.",mCurrentObject.getName()));
    }

    public void captureImage() throws IOException {
        camera.takePicture(null, null, jpegCallback);
    }

    public void refreshCamera() {
        if (surfaceHolder.getSurface() == null) {
            return;
        }

        try {
            camera.stopPreview();
        } catch (Exception e) {
        }

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = Camera.open();
        }

        catch (RuntimeException e) {
            System.err.println(e);
            System.out.println("Could not open camera");
            return;
        }
        Camera.Parameters param;
        param = camera.getParameters();
        param.setPreviewSize(352, 288);

        camera.setDisplayOrientation(90);

        camera.setParameters(param);

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        }

        catch (Exception e) {
            System.err.println(e);
            return;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_photo:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_report:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void startGalleryChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                GALLERY_IMAGE_REQUEST);
    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getCameraFile()));
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public File getCameraFile() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
//            CloudVisionUtils.uploadImage(data.getData(), mMainImage, mImageDetails, this);
//        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
//            CloudVisionUtils.uploadImage(Uri.fromFile(getCameraFile()), mMainImage, mImageDetails, this);
//        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtils.permissionGranted(
                requestCode,
                CAMERA_PERMISSIONS_REQUEST,
                grantResults)) {
            startCamera();
        }
    }
}
