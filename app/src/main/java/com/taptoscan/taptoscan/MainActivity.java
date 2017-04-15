package com.taptoscan.taptoscan;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Category {
    String icon_res;
    String name;
    int id;

    Category(String icon_res, String name, int id) {
        this.icon_res = icon_res;
        this.name = name;
        this.id = id;
    }
}


class Product {
    int id, categoryID;
    String name, code, symbol, price;

    Product(int id, int categoryID, String name, String code, String symbol, String price) {
        this.id = id;
        this.categoryID = categoryID;
        this.name = name;
        this.code = code;
        this.symbol = symbol;
        this.price = price;
    }
}

public class MainActivity extends AppCompatActivity {

    public final String GET_USER_DATA = "https://taptoscan.com/api/v1/getTag";
    public static final String ADD_ORDER = "https://taptoscan.com/api/v1/addOrder";

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    int NAVIGATION_LEVEL = 0;

    private List<Category> categoriesList = new ArrayList<Category>();
    private List<Product> productsList = new ArrayList<Product>();
    private List<Product> productsListCategory = new ArrayList<Product>();
    public static List<Order> productsOrder = new ArrayList<Order>();

    public static String tableID;
    public static String userID;
    String tableName;
    static String businessName;
    static String businessAddress;
    static String businessAddressCityCountry;
    int tagActive, tagDeleted;

    NfcAdapter mAdapterNFC;
    PendingIntent mPendingIntent;

    public static Activity ma;

    MenuItem cart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        mAdapter = new CategoryAdapter(categoriesList);
        recyclerView.setAdapter(mAdapter);

        initializeProductOrders();

        mAdapterNFC = NfcAdapter.getDefaultAdapter(this);
        if (mAdapterNFC == null) {
            //nfc not support your device.
            Toast.makeText(getApplicationContext(), R.string.nfc_no_support, Toast.LENGTH_LONG).show();
            return;
        } else {
            if (!mAdapterNFC.isEnabled()) {
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_yes_no);

                final TextView title = (TextView) dialog.findViewById(R.id.titleText);
                title.setText(R.string.nfc_not_active_title);

                final TextView message = (TextView) dialog.findViewById(R.id.bodyText);
                message.setText(R.string.nfc_open_settings);

                //message.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));

                Button btnYes = (Button) dialog.findViewById(R.id.btnYes);

                btnYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                        Toast.makeText(getApplicationContext(), R.string.activate_nfc, Toast.LENGTH_LONG).show();
                    }
                });

                Button btnNo = (Button) dialog.findViewById(R.id.btnNo);

                btnNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();

            }
        }
        Intent intent = getIntent();


        String action = intent.getAction();

        Log.d("intent", intent.toString());
        Log.d("action", action);

//        Log.d("type", type);
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        if (action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            onNewIntent(intent);
        }
        ma = this;

    }

    @Override
    protected void onNewIntent(Intent intent) {
        //setIntent(intent);
        getTagInfo(intent);
    }

    private void getTagInfo(Intent intent) {

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Ndef ndef = Ndef.get(tag);

        try {
            ndef.connect();

            Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if (messages != null) {
                NdefMessage[] ndefMessages = new NdefMessage[messages.length];
                for (int i = 0; i < messages.length; i++) {
                    ndefMessages[i] = (NdefMessage) messages[i];
                }

                //id
                NdefRecord recordID = ndefMessages[0].getRecords()[0];
                String recordIDString = new String(recordID.getPayload());

                // app
                NdefRecord recordApp = ndefMessages[0].getRecords()[1];
                String recordAppString = new String(recordApp.getPayload());


                if (recordAppString.equals("com.taptoscan.taptoscan")) {
                    recordIDString = recordIDString.substring(recordIDString.length() - 8);
                    getUserProductData(recordIDString);
                } else {
                    Toast.makeText(getApplicationContext(), "Not a TapToScan tag.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                ndef.close();

            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "NFC Tag is not in range.", Toast.LENGTH_LONG).show();
            finish();
        }

        //   Log.d("tag", tag.getTechList().toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapterNFC.enableForegroundDispatch(this, mPendingIntent, null, null);
        if (cart != null) {
            setCartCount(productsOrder.size());
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdapterNFC != null) {
            mAdapterNFC.disableForegroundDispatch(this);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        cart = menu.findItem(R.id.action_cart);

        setCartCount(productsOrder.size());

        cart.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(cart);
            }
        });

        cart.getActionView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final int[] screenPos = new int[2];
                final Rect displayFrame = new Rect();
                v.getLocationOnScreen(screenPos);
                v.getWindowVisibleDisplayFrame(displayFrame);

                final Context context = v.getContext();
                final int width = v.getWidth();
                final int height = v.getHeight();
                final int midy = screenPos[1] + height / 2;
                int referenceX = screenPos[0] + width / 2;
                if (ViewCompat.getLayoutDirection(v) == ViewCompat.LAYOUT_DIRECTION_LTR) {
                    final int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
                    referenceX = screenWidth - referenceX; // mirror
                }
                Toast cheatSheet = Toast.makeText(context, cart.getTitle(), Toast.LENGTH_SHORT);
                if (midy < displayFrame.height()) {
                    // Show along the top; follow action buttons
                    cheatSheet.setGravity(Gravity.TOP | GravityCompat.END, referenceX, height);
                } else {
                    // Show along the bottom center
                    cheatSheet.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, height);
                }
                cheatSheet.show();
                return false;
            }
        });
        return true;
    }

    public void initializeProductOrders() {
        productsOrder.clear();
        productsOrder.add(null);
    }

    public void setCartCount(int count) {

        count = count - 1;
        String c = String.valueOf(count);
        if (cart != null) {
            cart.setTitle("Cart items: " + c);
            TextView cTitle = (TextView) cart.getActionView().findViewById(R.id.icon_title);
            cTitle.setText("(" + c + ")");
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // todo: goto back activity from here
                onBackPressed();
                return true;
            case R.id.action_about:

                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_about);

                final TextView title = (TextView) dialog.findViewById(R.id.titleText);
                title.setText(R.string.action_about);

                final TextView message = (TextView) dialog.findViewById(R.id.bodyText);
                try {
                    message.setText(getResources().getString(R.string.version, getPackageManager().getPackageInfo(getPackageName(), 0).versionName));
                } catch (PackageManager.NameNotFoundException e) {
                    message.setText(getResources().getString(R.string.version, getString(R.string.not_available)));
                    e.printStackTrace();
                }

                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);

                final TextView copyright = (TextView) dialog.findViewById(R.id.copyrightText);
                copyright.setText(getResources().getString(R.string.copyright, year));

                final TextView web = (TextView) dialog.findViewById(R.id.webAddress);
                web.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.taptoscan.com"));
                        startActivity(browserIntent);
                    }
                });

                //message.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));

                Button btnYes = (Button) dialog.findViewById(R.id.btnYes);

                btnYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

                return true;
            case R.id.action_cart:
                Intent intent = new Intent(this, CheckoutActivity.class);
                startActivity(intent);
                return true;
            default:
                Log.d("Default called?", "yes");
                return super.onOptionsItemSelected(item);
        }

    }

    public void getUserProductData(final String tagID) {

        RequestQueue queue;
        StringRequest sr;
        categoriesList.clear();
        productsList.clear();
        queue = Volley.newRequestQueue(this);
        sr = new StringRequest(Request.Method.POST, GET_USER_DATA, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Response", response);
                //Parse JSON
                try {
                    JSONObject jsonResponse;
                    jsonResponse = new JSONObject(response);

                    Log.d("status", jsonResponse.getString("status"));

                    if(jsonResponse != null && jsonResponse.getString("status").equals("ok")) {
                        Log.d("passed???", "yes");
                        JSONObject mainObject = new JSONObject(response);
                        String status = mainObject.getString("status");
                        String message = mainObject.getString("message");
                        if (status.equalsIgnoreCase("ok") && message.equalsIgnoreCase("success")) {
                            //get tag JSON object
                            JSONObject tag = mainObject.getJSONObject("tag");
                            tableID = tag.getString("id");
                            userID = tag.getString("user");
                            tableName = tag.getString("name");
                            String nfcSerial = tag.getString("nfc_tag_serial");
                            tagActive = tag.getInt("active");

                            if(tagActive == 0) {
                                Toast.makeText(MainActivity.this, "This TapToScan tag is not active.", Toast.LENGTH_LONG).show();
                                ma.finish();
                                return;
                            }
                            tagDeleted = tag.getInt("deleted");

                            // Handle in case the tag is inactive or deleted
                            if (tagActive == 0 || tagDeleted == 1) {
                                return;
                            }

                            //get user JSON object
                            JSONObject userData = mainObject.getJSONObject("user");
                            businessName = userData.getString("business_name");
                            String address = userData.getString("address");
                            String city = userData.getString("city");
                            String country = userData.getString("country");
                            String zipCode = userData.getString("zip");
                            businessAddress = address;
                            businessAddressCityCountry = zipCode + ", " + city + ", " + country;

                            JSONArray categories = mainObject.getJSONArray("categories");


                            // Add header row
                            categoriesList.add(null);

                            // rest of the rows go here
                            for (int i = 0; i < categories.length(); i++) {
                                // get products in a category
                                JSONObject category = categories.getJSONObject(i);

                                Category c = new Category(category.getString("icon_res"), category.getString("name"), category.getInt("id"));
                                categoriesList.add(c);

                                JSONArray products = category.getJSONArray("products");

                                for (int j = 0; j < products.length(); j++) {
                                    JSONObject product = products.getJSONObject(j);
                                    int productID = product.getInt("id");
                                    int categoryID = product.getInt("category_id");
                                    String productName = product.getString("name");
                                    String price = product.getString("price");

                                    JSONObject symbol = product.getJSONObject("symbol");
                                    String priceCode = symbol.getString("code");
                                    String priceSymbol = symbol.getString("symbol");
                                    Log.d("products", products.toString());

                                    Product p = new Product(productID, categoryID, productName, priceCode, priceSymbol, price);
                                    productsList.add(p);
                                }

                            }
                            if (LauncherActivity.launcherAct != null) {
                                LauncherActivity.launcherAct.finish();
                            }
                        }
                    } else {
                        Toast.makeText(MainActivity.this, R.string.tag_not_assigned, Toast.LENGTH_LONG).show();
                        finish();
                    }

                } catch (JSONException e) {
                    Log.d("jsonexception", e.toString());
                    Toast.makeText(MainActivity.this, "No data found for this tag.", Toast.LENGTH_SHORT).show();
                }

                // done
                mAdapter = new CategoryAdapter(categoriesList);
                recyclerView.setAdapter(mAdapter);
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in);
                recyclerView.startAnimation(animation);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                finish();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // add parameters to the request
                Map<String, String> params = new HashMap<>();
                params.put("tagID", String.valueOf(tagID));

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //params.put("Content-Type", "application/x-www-form-urlencoded");
                return new HashMap<>();
            }
        };
        // execute the request
        queue.add(sr);
    }

    @Override
    public void onBackPressed() {
        switch (NAVIGATION_LEVEL) {
            case 1:
                loadCategoriesCache();
                NAVIGATION_LEVEL = 0;
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setDisplayShowHomeEnabled(false);
                getSupportActionBar().setTitle(getTitle());
                break;
            default:
                super.onBackPressed();
                break;
        }
    }

    public void loadProducts(int categoryID, String catName) {
        productsListCategory.clear();
        for (Product p : productsList) {
            Log.d("p name", p.name);
            if (p.categoryID == categoryID) {
                productsListCategory.add(p);
            }
        }
        mAdapter = new ProductAdapter(productsListCategory);
        Log.d("productsListCategory", productsListCategory.toString());
        recyclerView.setAdapter(mAdapter);

        Animation animation = AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in);
        recyclerView.startAnimation(animation);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(catName);
        NAVIGATION_LEVEL = 1;
    }

    public void loadCategoriesCache() {
        mAdapter = new CategoryAdapter(categoriesList);
        recyclerView.setAdapter(mAdapter);

        Animation animation = AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in);
        recyclerView.startAnimation(animation);

        NAVIGATION_LEVEL = 0;
    }
}
