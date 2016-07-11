package com.google.sample.cloudvision;

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
    private ObjectManager objects;
    private PlayerManager mPlayerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        mPlayerManager = new PlayerManager(getApplicationContext());
        objects = ObjectManager.getInstance(getApplicationContext());
        Object[] myDataset = objects.getAllItems();
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

        FloatingActionButton reportFab = (FloatingActionButton) findViewById(R.id.camera_fab);
        reportFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCameraActivity(view);
            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        ((ReportRecyclerAdapter) mAdapter).setOnItemClickListener(new ReportRecyclerAdapter.MyClickListener() {
              @Override
              public void onItemClick(int position, View v) {
                  startCameraActivity(v);
              }
          });
    }

    //Used to start MainActivity
    public void startCameraActivity(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
