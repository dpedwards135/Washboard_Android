package com.davidparkeredwards.washboard;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    /* Don't mess with inserting into RecyclerView, etc, just modify the ArrayList with the
    data and rebuild the RecyclerView. That means any edits need to be saved to the constructorObjects
    and that is always up to date. */

    private static final String TAG = RecyclerAdapter.class.getSimpleName();

    //Rewrite to make bundle the foundation of the adapter and everything gets saved to it.

    private ArrayList<OrderInstance> orderList;

    private Activity activity;

    public RecyclerAdapter(Activity activity, ArrayList<OrderInstance> list) {
        this.activity = activity;
        this.orderList = list;

    }

    //Going to use this for friends, trips, address - Use the type of activity to determine the viewholder

    @Override
    public int getItemViewType(int position) {
        return 1;
        /*
        if(activity == MyTripsActivity.class) {
            return 1;
        } else if(activity == FriendsActivity.class) {
            return 2;
        } else if(activity == AddressesActivity.class) {
            return 3;
        } else {
            return 0;
        }
        */

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        int layoutResource = 0;
        View inflatedView;

        switch (viewType) {
            default:
                //case 1:
                layoutResource = R.layout.order_viewholder;
                inflatedView = LayoutInflater.from(parent.getContext()).inflate(layoutResource,
                        parent,false);
                RecyclerView.LayoutParams rlp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                inflatedView.setLayoutParams(rlp);
                viewHolder = new OrderInstanceViewHolder(inflatedView, this, (OpenOrdersActivity) activity);
                Log.i(TAG, "onCreateViewHolder: ");

                break;

        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        OrderInstance item = orderList.get(position);
        OrderInstanceViewHolder viewHolder = (OrderInstanceViewHolder) holder;
        viewHolder.setTripName(item.getId());

    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        //Save and persist dataset
        super.onViewRecycled(holder);
    }

    public void onClick() {

    }


}