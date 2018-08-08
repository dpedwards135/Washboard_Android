package com.davidparkeredwards.washboard;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class OrderInstanceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    //Try creating base class for WViewHolder that takes a view AND an object reference and whatever happens in
    //the view changes the object
    //Try to create just one ViewHolder that with minimal processing can take any object, create
    //a view and manipulate the object for saving and persistence.
    //Try to create just one WFormField which defines which ViewHolder elements to make visible


    private View v;
    private RecyclerAdapter adapter;



    private OpenOrdersActivity activity;

    //Put all the widgets in here, default "GONE" and set visible if needed.
    private TextView textView;
    private Button shareTripButton;
    private Button deleteButton;

    public OrderInstanceViewHolder(View v, RecyclerAdapter adapter, OpenOrdersActivity activity) {
        super(v);
        this.v = v;
        this.adapter = adapter;
        this.activity = activity;
        configureViewHolder();
    }

    private void configureViewHolder() {

        textView = (TextView) v.findViewById(R.id.viewholder_text);
        textView.setVisibility(View.VISIBLE);
        textView.setText("New Text");
        textView.setOnClickListener(this);

        shareTripButton = (Button) v.findViewById(R.id.Share);
        shareTripButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.i("ViewHolder", "ShareTrip Click");
                
            }
        });



        deleteButton = (Button) v.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
            }
        });





        //v.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        Log.d("RecyclerView", "CLICK!" + textView.getText());
        adapter.onClick();

    }

    public void setTripName(String name) {
        textView.setText(name);
    }

    public void setActivity(OpenOrdersActivity activity) {
        this.activity = activity;
    }

    public void hideButtons(Boolean hide) {
        if(hide) {
            shareTripButton.setVisibility(View.INVISIBLE);
            deleteButton.setVisibility(View.INVISIBLE);

        } else {
            shareTripButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
        }
    }
}