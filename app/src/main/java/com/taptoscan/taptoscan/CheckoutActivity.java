package com.taptoscan.taptoscan;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.taptoscan.taptoscan.MainActivity.ma;
import static com.taptoscan.taptoscan.MainActivity.productsOrder;

public class CheckoutActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    public static boolean orderProcessing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        setTitle("Checkout");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        //registerForContextMenu(recyclerView);

        recyclerView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d("long click?", "seems like a yes");

                return false;
            }
        });

        loadCart();
    }

    public ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        ActionMode am;

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_checkout, menu);
            am = mode;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            deselectAll();
        }

        private void deselectAll() {
            for (int i = 1; i < MainActivity.productsOrder.size(); i++) {
                MainActivity.productsOrder.get(i).setSelected(false);
            }
            mAdapter.notifyDataSetChanged();

        }

        private void deleteSelected() {
            // inverse because on remove indexes get changed
            for (int i = MainActivity.productsOrder.size() - 1; i > 0; i--) {
                if (MainActivity.productsOrder.get(i).isSelected()) {
                    MainActivity.productsOrder.remove(i);
                }
            }
            am.finish();

        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    deleteSelected();
                    return true;
                default:
                    mode.finish();
                    return false;
            }
        }
    };

    public void loadCart() {
        mAdapter = new OrderAdapter(MainActivity.productsOrder);
        recyclerView.setAdapter(mAdapter);
    }

    public String getTotal() {

        Map<String, Double> currencyTotals = new HashMap<String, Double>();

        if (productsOrder.size() > 0) {
            for (int i = 1; i < productsOrder.size(); i++) {
                if (currencyTotals.get(productsOrder.get(i).code) != null) {
                    Double curr = currencyTotals.get(productsOrder.get(i).code).doubleValue();
                    currencyTotals.put(productsOrder.get(i).code, curr + (Double.valueOf(productsOrder.get(i).price) * Double.valueOf(productsOrder.get(i).quantity)));
                } else {
                    currencyTotals.put(productsOrder.get(i).code, Double.valueOf(productsOrder.get(i).price) * Double.valueOf(productsOrder.get(i).quantity));
                }
            }
        }

        String returnString = "";

        if (currencyTotals.size() > 0) {
            for (Map.Entry<String, Double> entry : currencyTotals.entrySet()) {
                NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
                format.setCurrency(Currency.getInstance(entry.getKey()));
                if (returnString.length() > 0) {
                    returnString += "+ " + format.format(entry.getValue()) + "\n";
                } else {
                    returnString += format.format(entry.getValue()) + "\n";
                }
            }
            returnString = returnString.trim();
        } else {
            returnString = "Cart is empty";
        }
        return returnString;
    }

    public boolean sendOrder() {

        if(!orderProcessing) {

            orderProcessing = true;
            JSONObject order = new JSONObject();

            try {
                order.put("userID", MainActivity.userID);
                order.put("tagID", MainActivity.tableID);

                JSONArray productOrder = new JSONArray();


                for (int i = 1; i < MainActivity.productsOrder.size(); i++) {

                    //Log.d("int", String.valueOf(i));
                    JSONObject productInfo = new JSONObject();
                    productInfo.put("id", MainActivity.productsOrder.get(i).id);
                    productInfo.put("quantity", MainActivity.productsOrder.get(i).quantity);
                    productInfo.put("price", MainActivity.productsOrder.get(i).price);
                    productInfo.put("symbol_code", MainActivity.productsOrder.get(i).code);
                    productInfo.put("symbol", MainActivity.productsOrder.get(i).symbol);

                    Log.d("symbol_code", MainActivity.productsOrder.get(i).symbol);
                    if (productInfo != null) {
                        productOrder.put(i, productInfo);
                    }


                    Log.d("prod info", productInfo.toString());

                }
                productOrder.remove(0);
                order.put("productOrders", productOrder);
                Log.d("JSON OBJ", order.toString());
                sendOrderToServer(order);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void sendOrderToServer(final JSONObject order) {

        RequestQueue queue = Volley.newRequestQueue(this);
        Log.d("sendOrderToServer", "called");
        Log.d("json obj", order.toString());


        JsonObjectRequest jsor = new JsonObjectRequest(MainActivity.ADD_ORDER, order, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getString("status").equalsIgnoreCase("200")) {
                        final Dialog dialog = new Dialog(CheckoutActivity.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.dialog_ok);

                        final TextView title = (TextView) dialog.findViewById(R.id.titleText);
                        title.setText(R.string.order_placed);

                        final TextView message = (TextView) dialog.findViewById(R.id.bodyText);
                        message.setText(R.string.order_placed_msg);
                        //message.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));

                        Button btnYes = (Button) dialog.findViewById(R.id.btnYes);


                        btnYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                ma.finish();
                                finish();

                            }
                        });

                        dialog.setOnCancelListener(
                                new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        dialog.dismiss();
                                        ma.finish();
                                        finish();
                                    }
                                }
                        );
                        dialog.show();
                    } else {
                        final Dialog dialog = new Dialog(CheckoutActivity.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.dialog_ok);

                        final TextView title = (TextView) dialog.findViewById(R.id.titleText);
                        title.setText(R.string.order_failed);

                        final TextView message = (TextView) dialog.findViewById(R.id.bodyText);
                        message.setText(R.string.order_failed_msg);
                        //message.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));

                        Button btnYes = (Button) dialog.findViewById(R.id.btnYes);

                        btnYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        dialog.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                orderProcessing = false;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                final Dialog dialog = new Dialog(CheckoutActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_ok);

                final TextView title = (TextView) dialog.findViewById(R.id.titleText);
                title.setText(R.string.order_failed);

                final TextView message = (TextView) dialog.findViewById(R.id.bodyText);
                message.setText(R.string.order_failed_msg + "(" + error.toString() + ")");
                //message.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));

                Button btnYes = (Button) dialog.findViewById(R.id.btnYes);

                btnYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                orderProcessing = false;
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("charset", "utf-8");
                return headers;
            }
        };
        queue.add(jsor);
    }

    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
