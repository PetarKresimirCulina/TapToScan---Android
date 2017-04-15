package com.taptoscan.taptoscan;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * Created by Kreso on 23.3.2017..
 */

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {


    private List<Product> mDataset;
    private Context context;


    public ProductAdapter(List<Product> productsListCategory) {
        mDataset = productsListCategory;
    }


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView productName, txtPrice;
        public ImageView addIVBtn;
        public ConstraintLayout holderLayout;

        public ViewHolder(View v) {
            super(v);
            productName = (TextView) v.findViewById(R.id.productName);
            txtPrice = (TextView) v.findViewById(R.id.priceLine);
            addIVBtn = (ImageView) v.findViewById(R.id.addImageViewBtn);
            holderLayout = (ConstraintLayout) v.findViewById(R.id.itemHolder);
            context = v.getContext();
        }
    }

    /*public void add(int position, String item) {
        mDataset.add(position, item);
        notifyItemInserted(position);
    }*/

    public void remove(int position) {
        try {
            mDataset.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mDataset.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
   /* public CategoryAdapter(ArrayList<String> myDataset) {
        mDataset = myDataset;
    }*/

    // Create new views (invoked by the layout manager)
    @Override
    public ProductAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_product_item_row, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        context = parent.getContext();
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        holder.productName.setText(mDataset.get(position).name);
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
        format.setCurrency(Currency.getInstance(mDataset.get(position).code));
        final String result = format.format(Double.valueOf(mDataset.get(position).price));
        holder.txtPrice.setText(result);

        holder.holderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.add_item_to_order);

                final EditText qEditText = (EditText) dialog.findViewById(R.id.quantityEditText);
                qEditText.setHint("Quantity");
                qEditText.setText("1");

                TextView name = (TextView) dialog.findViewById(R.id.bodyText);
                String boldText = context.getResources().getString(R.string.name) + ":";
                String normalText = mDataset.get(position).name;
                SpannableString str = new SpannableString(boldText + " " + normalText);
                str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                name.setText(str);

                TextView price = (TextView) dialog.findViewById(R.id.priceText);
                boldText = context.getResources().getString(R.string.price) + ":";
                normalText = result;
                str = new SpannableString(boldText + " " + normalText);
                str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                price.setText(str);

                Button btnCancel = (Button) dialog.findViewById(R.id.btnNo);

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                Button addToCart = (Button) dialog.findViewById(R.id.btnYes);

                addToCart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int id, categoryID, quantity;
                        String name, code, symbol, price;

                        if (qEditText.getText().length() > 0 && Integer.valueOf(qEditText.getText().toString()) > 0 && Integer.valueOf(qEditText.getText().toString()) <= 10) {
                            id = mDataset.get(position).id;
                            categoryID = mDataset.get(position).categoryID;
                            quantity = Integer.valueOf(qEditText.getText().toString());


                            name = mDataset.get(position).name;
                            symbol = mDataset.get(position).symbol;
                            price = mDataset.get(position).price;
                            code = mDataset.get(position).code;


                            Log.d("DATA:", name + " " + symbol + " " + price + " " +code);
                            Order cOrder = new Order(id, categoryID, quantity, name, code, symbol, price, false);
                            MainActivity.productsOrder.add(cOrder);
                            ((MainActivity) context).setCartCount(MainActivity.productsOrder.size());
                            dialog.dismiss();

                            final SharedPreferences pref;

                            pref = context.getSharedPreferences("tts-pref-1", 0);
                            if(!pref.getBoolean("do_not_show_cart_msg", false)) {
                                //Show confirmation dialog
                                final Dialog dialogConfirmation = new Dialog(context);
                                dialogConfirmation.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialogConfirmation.setContentView(R.layout.dialog_ok_checkbox);

                                final TextView title = (TextView) dialogConfirmation.findViewById(R.id.titleText);
                                title.setText(R.string.added_to_cart);

                                final TextView message = (TextView) dialogConfirmation.findViewById(R.id.bodyText);
                                message.setText(R.string.added_to_cart_msg);

                                Button btnYes = (Button) dialogConfirmation.findViewById(R.id.btnYes);
                                final CheckBox doNotShow = (CheckBox) dialogConfirmation.findViewById(R.id.doNotShow);

                                btnYes.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(doNotShow.isChecked()) {
                                            SharedPreferences.Editor editor = pref.edit();
                                            editor.putBoolean("do_not_show_cart_msg", true);
                                            editor.commit();
                                        }
                                        dialogConfirmation.dismiss();
                                    }
                                });
                                dialogConfirmation.show();
                            }


                        } else {
                            Toast.makeText(context, R.string.invalid_ammount, Toast.LENGTH_LONG).show();
                        }

                    }
                });

                //dialog.setTitle("Custom Alert Dialog");

               /* Button btnSave          = (Button) dialog.findViewById(R.id.save);
                Button btnCancel        = (Button) dialog.findViewById(R.id.cancel);
               */
                dialog.show();

            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
