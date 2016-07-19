package com.google.sample.cloudvision;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ReportRecyclerAdapter extends RecyclerView.Adapter<ReportRecyclerAdapter.ViewHolder> {
    private Object[] mDataset;
    private static MyClickListener myClickListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView mObjectName;
        public TextView mObjectStatus;
        public TextView mObjectAttempts;
        public ImageView mImageView;
        public ViewHolder(View v) {
            super(v);
            mObjectName = (TextView) itemView.findViewById(R.id.objectName);
            mObjectStatus = (TextView) itemView.findViewById(R.id.objectStatus);
            mObjectAttempts = (TextView) itemView.findViewById(R.id.objectAttempts);
            mImageView = (ImageView) itemView.findViewById(R.id.objectClickIcon);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(mObjectName.getText().toString(), v);
        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ReportRecyclerAdapter(Object[] myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ReportRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.report_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        Object o = mDataset[position];
        String stateString = "";
        Object.State state = o.getState();
        if (state == Object.State.SKIPPED && o.getAttempts() > 0){
            stateString = "Incorrect";
            holder.mObjectStatus.setTextColor(0x96ff4043);
        } else if (state == Object.State.CORRECT) {
            stateString = "Correct";
            holder.mObjectStatus.setTextColor(0x9650ff40);
        } else if (state == Object.State.NOT_TESTED) {
            stateString = "Not tested";
            holder.mObjectStatus.setTextColor(0xff000000);
        }
        holder.mObjectName.setText(o.getName());
        holder.mObjectStatus.setText(stateString);
        holder.mObjectAttempts.setText(Integer.toString(o.getAttempts()));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.length;
    }

    public interface MyClickListener {
        public void onItemClick(String name, View v);
    }
}