package com.google.sample.cloudvision;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.MenuItem;
import android.widget.TextView;

import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final String HAS_SHOWN_PARENT_DIALOG = "hasShownParentDialog";
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;

    private TextView mImageDetails;
    private TextView mSearchWord;

    private Camera camera;
    private SurfaceHolder surfaceHolder;
    private Camera.PictureCallback jpegCallback;

    private ObjectManager mObjectManager;
    private PlayerManager mPlayerManager;
    private Object mCurrentObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPermissions();

        if(!isNetworkAvailable()){
            NoInternetDialog noInternet = new NoInternetDialog();
            noInternet.show(getFragmentManager(), "noInternetDialog");
        }

        setContentView(R.layout.activity_main);

        mPlayerManager = new PlayerManager(getApplicationContext());

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

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        final CloudVisionUtils.CloudVisionTask.CVTaskListener listener = new CloudVisionUtils.CloudVisionTask.CVTaskListener() {
            @Override
            public void onFinished(String result) {
                // Update object statistics and reinsert into object manager!
                boolean match = false;
                String output;
                ArrayList<String> searchWords = new ArrayList<>();
                String associatedWords = mCurrentObject.getAssociatedWords();

                // add associated words to the list of search words if they exist
                searchWords.add(mCurrentObject.getName().toLowerCase());
                if ("" != associatedWords) {
                    String [] split = associatedWords.split(",");
                    searchWords.addAll(Arrays.asList(split));
                }

                // iterate over search words and check if they CloudVision results contain that search word
                for (String word : searchWords) {
                    if (result.contains(word)) {
                        match = true;
                        break;
                    }
                }

                if (match) {
                    mCurrentObject.setState(Object.State.CORRECT);
                    mPlayerManager.addExperience(mObjectManager.getExperience(mCurrentObject));
                    output = String.format("Congratulations, you found the %s!", mCurrentObject.getName());

                } else {
                    mCurrentObject.setState(Object.State.SKIPPED);
                    output = "Oops, try again! Looks like your image contains " + result;
                }

                mCurrentObject.setAttempts(1 + mCurrentObject.getAttempts());
                mObjectManager.updateObject(mCurrentObject);

                // Output match results and refresh the camera
                mImageDetails.setText(output);
                refreshCamera();
                if (result.contains(mCurrentObject.getName().toLowerCase())){
                    skipObject();
                }
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

        showParentDialog();
        skipObject();
        String obj_name = "";
        if (getIntent() != null){
            if (getIntent().getExtras() != null) {
                if (getIntent().getExtras().containsKey("obj_name")) {
                    obj_name = getIntent().getExtras().getString("obj_name");
                }
            }
        }

        if (obj_name.isEmpty()) {
            skipObject();
        } else {
            goToObject(obj_name);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        //When the app fully stops, either from memory or explicit stoppage, reset to show the next
        //time the app runs
        setHasShownParentDialog(false);
    }

    private void showParentDialog(){
        if (!hasShownParentDialog()){
            ParentalAdviceDialog dialog = new ParentalAdviceDialog();
            dialog.show(getFragmentManager(), "parentalAdviceDialog");
            setHasShownParentDialog(true);
        }
    }

    private void setHasShownParentDialog(Boolean bool){
        SharedPreferences pref = getSharedPreferences("MainActivity", MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.putBoolean(HAS_SHOWN_PARENT_DIALOG, bool);
        edit.commit();
    }

    private boolean hasShownParentDialog(){
        SharedPreferences pref = getSharedPreferences("MainActivity", MODE_PRIVATE);
        Boolean value = pref.getBoolean(HAS_SHOWN_PARENT_DIALOG, false);
        return value;
    }

    //Used to start ReportActivity
    public void startReportActivity(View view){
        Intent intent = new Intent(this, ReportActivity.class);
        startActivity(intent);
    }

    public void goToObject(String name) {
        mCurrentObject = mObjectManager.setCurrentObject(name);
        mSearchWord.setText(String.format("Current Search Word: %s.", name));
    }

    public void skipObject(){
        mCurrentObject = mObjectManager.getNextObject(mCurrentObject);
        String name = "";
        if (null != mCurrentObject) {
            name = mCurrentObject.getName();
        }
        mSearchWord.setText(String.format("Current Search Word: %s.", name));
    }

    public void captureImage() throws IOException {
        camera.takePicture(null, null, jpegCallback);
    }

    public void refreshCamera() {
        if (surfaceHolder.getSurface() == null) {
            return;
        }

        if (camera == null){
            startCamera();
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
        startCamera();
    }

    private void startCamera(){
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

        List<Camera.Size> sizes = param.getSupportedPreviewSizes();
        Camera.Size cs = sizes.get(0);
        param.setPreviewSize(cs.width, cs.height);

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
        if (null != camera) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_photo:
                return true;

            case R.id.action_report:
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void getPermissions() {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtils.permissionGranted(
                requestCode,
                CAMERA_PERMISSIONS_REQUEST,
                grantResults)) {
            refreshCamera();
        }
    }

    public static class ParentalAdviceDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.parent_advice)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //Do nothing. This is just a warning for little kids
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    public static class NoInternetDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.no_internet)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(getContext(), ReportActivity.class);
                            startActivity(intent);
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }
}
