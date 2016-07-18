package com.google.sample.cloudvision;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by Jeric Pauig on 6/11/2016.
 */
public class ReportActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private ProgressBar mProgress;
    private RecyclerView.Adapter mAdapter;
    private ObjectManager mObjectManager;
    private PlayerManager mPlayerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        mPlayerManager = new PlayerManager(getApplicationContext());
        mObjectManager = ObjectManager.getInstance(getApplicationContext());
        updateTextFields();

        FloatingActionButton cameraFab = (FloatingActionButton) findViewById(R.id.camera_fab);
        cameraFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCameraActivity(view, mObjectManager.getCurrentObject().getName());
            }
        });

        FloatingActionButton resetFab = (FloatingActionButton) findViewById(R.id.reset_fab);
        resetFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show dialog to confirm before resetting player data
                new AlertDialog.Builder(view.getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Reset Player Data")
                        .setMessage("Are you sure you want to erase all of your data?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                resetStatistics();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((ReportRecyclerAdapter) mAdapter).setOnItemClickListener(new ReportRecyclerAdapter.MyClickListener() {
              @Override
              public void onItemClick(String name, View v) {
                  startCameraActivity(v, name);
              }
          });
    }

    //Used to start MainActivity
    public void startCameraActivity(View view, String name) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("obj_name", name);
        startActivity(intent);
    }

    //Reset statistics
    private void resetStatistics() {
        mObjectManager.resetObjects();
        mPlayerManager.resetExperience();
        updateTextFields();
    }

    // Update text fields in view
    private void updateTextFields() {
        Object[] myDataset = mObjectManager.getAllItems();
        mRecyclerView = (RecyclerView) findViewById(R.id.report_recycler);
        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new ReportRecyclerAdapter(myDataset);
        mRecyclerView.setAdapter(mAdapter);

        mProgress = (ProgressBar) findViewById(R.id.user_experience);
        mProgress.setProgress(mPlayerManager.percentageOfLevelComplete());

        TextView expTillNext = (TextView) findViewById(R.id.level_progress);
        String experience = "Experience Until Next Level: "+ mPlayerManager.experienceUntilNextLevel();
        expTillNext.setText(experience);

        TextView currentLevel = (TextView) findViewById(R.id.current_level);
        String level = "You Are Level "+ mPlayerManager.getLevel();
        currentLevel.setText(level);
    }
}
