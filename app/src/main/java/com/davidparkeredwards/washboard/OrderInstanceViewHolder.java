package com.davidparkeredwards.washboard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class OrderInstanceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    //Try creating base class for WViewHolder that takes a view AND an object reference and whatever happens in
    //the view changes the object
    //Try to create just one ViewHolder that with minimal processing can take any object, create
    //a view and manipulate the object for saving and persistence.
    //Try to create just one WFormField which defines which ViewHolder elements to make visible


    private View v;
    private RecyclerAdapter adapter;

    /*
    Layout:
    Name
    Address - Nav Button
    Pickup notes
    Window
    Status update button - shows pickup then shows return
     */


    private OpenOrdersActivity activity;
    private int currentStatus;
    private String instanceId;

    //Put all the widgets in here, default "GONE" and set visible if needed.
    private TextView textView;
    private Button updateOrderButton;

    public OrderInstanceViewHolder(View v, RecyclerAdapter adapter, OpenOrdersActivity activity, int currentStatus, String instanceId) {
        super(v);
        this.v = v;
        this.adapter = adapter;
        this.activity = activity;
        this.currentStatus = currentStatus;
        this.instanceId = instanceId;

        configureViewHolder();
    }

    private void configureViewHolder() {

        textView = (TextView) v.findViewById(R.id.viewholder_text);
        textView.setVisibility(View.VISIBLE);
        textView.setText("New Text");
        textView.setOnClickListener(this);

        updateOrderButton = (Button) v.findViewById(R.id.update_button);
        updateOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateOrderInstance();
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

    public void updateOrderInstance() {

        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        final View updateStatusView = activity.getLayoutInflater().inflate(R.layout.status_update_dialog, null);
        alert.setView(updateStatusView);
        alert.setCancelable(true);
        AlertDialog a = alert.create();
        a.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        a.show();

        String string = "";
        switch (currentStatus) {
            case 0:
                string = activity.getString(R.string.pickup);
                break;
            case 1:
                string = activity.getString(R.string.dropoff);
                break;
            default:
                string = string;
        }

        Button updateButton = (Button) updateStatusView.findViewById(R.id.update_button);
        (updateButton).setText(string);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText bagsText = (EditText) updateStatusView.findViewById(R.id.bag_number);
                if (bagsText != null) {
                    if (bagsText.getText() != null) {
                        Integer bagNumber = Integer.valueOf(bagsText.getText().toString());
                        if (bagNumber != null) {
                            activity.updateOrderInstance(instanceId, bagNumber, currentStatus + 1);
                        }

                    }
                }
            }
        });

        Button noShowButton = (Button) updateStatusView.findViewById(R.id.no_show_button);
        if (currentStatus != 0) {
            noShowButton.setVisibility(View.GONE);
        } else {
            noShowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alert1 = new AlertDialog.Builder(activity);
                    alert1.setTitle(R.string.is_customer_no_show);
                    alert1.setMessage(R.string.have_you_checked);
                    alert1.setPositiveButton(R.string.yes, new AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.updateOrderInstance(instanceId, 0, 4);
                        }
                    });
                    alert1.setNegativeButton(R.string.cancel, new AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    AlertDialog a = alert1.create();
                    a.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    a.show();

                }
            });
        }
    }
}