package com.davidparkeredwards.washboard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

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
    private OrderInstance oldOrderInstance;

    //Put all the widgets in here, default "GONE" and set visible if needed.
    private TextView textView;
    private Button updateOrderButton;

    private ArrayList<String> newBagList;

    public OrderInstanceViewHolder(View v, RecyclerAdapter adapter, OpenOrdersActivity activity) {
        super(v);
        this.v = v;
        this.adapter = adapter;
        this.activity = activity;


        //configureViewHolder();
    }

    public void configureViewHolder(OrderInstance orderInstance) {

        this.oldOrderInstance = orderInstance;

        textView = (TextView) v.findViewById(R.id.viewholder_text);
        textView.setVisibility(View.VISIBLE);
        String textViewString = (orderInstance.getCustomerName() + "\n" +
                orderInstance.getOrder().getAddress() +
                "\n" + orderInstance.getOrder().getZip() + "\n" +
                orderInstance.getCustomerPhone() + "\n" +
                orderInstance.getOrder().getPickupNotes() + "\n");
        if(oldOrderInstance.getStatus() == 1) {
            String bagString = "";
            for(String bag : oldOrderInstance.getBags()) {
                bagString = bagString + bag + "\n";
            }
            if(bagString == "") bagString = "0";
            textViewString = textViewString + activity.getString(R.string.bag_number) + ": " + bagString;
        }
        textView.setText(textViewString);

        textView.setOnClickListener(this);



        updateOrderButton = (Button) v.findViewById(R.id.status_change_button);
        if(oldOrderInstance.getStatus() == 0) {
            updateOrderButton.setText(R.string.pickup);
        } else if(oldOrderInstance.getStatus() == 1) {
            updateOrderButton.setText(R.string.dropoff);
        } else {
            updateOrderButton.setText(R.string.error_field_required);
        }

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
        final AlertDialog a = alert.show();
        a.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);


        String string = "";
        switch (oldOrderInstance.getStatus()) {
            case 0:
                string = activity.getString(R.string.pickup);
                break;
            case 1:
                string = activity.getString(R.string.dropoff);
                break;
            default:
                string = string;
        }
        newBagList = new ArrayList<String>();
        final EditText bagsText = (EditText) updateStatusView.findViewById(R.id.bag_number);

        final Button addBagButton = (Button) updateStatusView.findViewById(R.id.save_bag_button);
        final Button updateButton = (Button) updateStatusView.findViewById(R.id.update_button);
        final Button noShowButton = (Button) updateStatusView.findViewById(R.id.no_show_button);
        final Button cancelButton = (Button) updateStatusView.findViewById(R.id.cancel_status_change);
        final Button clearButton = (Button) updateStatusView.findViewById(R.id.clear_bags_buttons);
        final TextView bagNumberText = (TextView) updateStatusView.findViewById(R.id.bag_number_text);
        final ScrollView bagScroll = (ScrollView) updateStatusView.findViewById(R.id.bag_scroll);

        String bagString = "";
        for(String bag : oldOrderInstance.getBags()) {
            bagString = bagString + bag + "\n";
        }

        bagNumberText.setText(activity.getString(R.string.bags_entered) + ": " + bagString);

        clearButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                newBagList = new ArrayList<String>();
                String bagString = "";
                for(String bag : newBagList) {
                    bagString = bagString + bag + "\n";
                }

                bagNumberText.setText(activity.getString(R.string.bags_entered) + ": " + bagString);
            }
        });

        updateButton.setEnabled(false);
        addBagButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (bagsText != null) {
                    if (bagsText.getText() != null) {
                        String bagId = bagsText.getText().toString();
                        if (bagId != null && isValidId(bagId)) {
                            newBagList.add(bagId);
                            String bagString = "";
                            for(String bag : newBagList) {
                                bagString = bagString + bag + "\n";
                            }
                            bagNumberText.setText(activity.getString(R.string.bags_entered) + ": " + bagString);
                            bagsText.setText("");
                            bagScroll.post(new Runnable() {

                                @Override
                                public void run() {
                                    bagScroll.fullScroll(ScrollView.FOCUS_DOWN);
                                }
                            });
                            if(oldOrderInstance.getStatus() == 1) {
                                if (newBagList.equals(oldOrderInstance.getBags())) {
                                    updateButton.setEnabled(true);
                                }
                            } else {

                                updateButton.setEnabled(true);
                            }
                        } else {
                            Toast.makeText(activity, R.string.valid_bag_id_not_found, Toast.LENGTH_LONG).show();
                        }

                    }
                }
        }});


        updateButton.setText(string);
        updateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(oldOrderInstance.getStatus() == 0) {
                    activity.updateOrderInstance(oldOrderInstance.getId(), newBagList, oldOrderInstance.getStatus() + 1);
                }
            }
        });


        if (oldOrderInstance.getStatus() != 0) {
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
                            activity.updateOrderInstance(oldOrderInstance.getId(), new ArrayList<String>(), 4);
                        }
                    });
                    alert1.setNegativeButton(R.string.no, new AlertDialog.OnClickListener() {
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


            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("ViewHolder", "Dismiss view");
                    a.dismiss();
                }
            });

        }
    }

    private boolean isValidId(String string) {

        if(!string.contains("WB") || !(string.length() == 9)) return false;
        if(newBagList.contains(string)) return false;

        if(oldOrderInstance.getStatus() == 1) {
            if (oldOrderInstance.getBags().contains(string)){
                return true;
            } else {
                return false;
            }

        }

        return true;
    }


}