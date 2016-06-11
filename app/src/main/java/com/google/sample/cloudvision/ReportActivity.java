package com.google.sample.cloudvision;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Jeric Pauig on 6/11/2016.
 */
public class ReportActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private ObjectManager objects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        objects = ObjectManager.getInstance(getApplicationContext());
        String[] myDataset = objects.allItems();

        mRecyclerView = (RecyclerView) findViewById(R.id.report_recycler);

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new ReportRecyclerAdapter(myDataset);
        mRecyclerView.setAdapter(mAdapter);

    }

    //Used to start MainActivity
    public void startReportActivity(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
