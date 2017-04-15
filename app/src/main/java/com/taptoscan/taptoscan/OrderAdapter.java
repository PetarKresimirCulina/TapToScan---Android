package com.taptoscan.taptoscan;

import android.app.Dialog;
import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.MalformedInputException;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * Created by Kreso on 23.3.2017..
 */

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {


    private List<Order> mDataset;
    private Context context;
    boolean actionMode = false;

    private static final int HEADER_VIEW = 1;


    private int position;

    ActionMode am;


    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }


    public OrderAdapter(List<Order> productsOrder) {
        mDataset = productsOrder;
        Log.d("dataset", String.valueOf(mDataset.toArray()));
    }


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView productName, txtPrice, txtQuantity;
        public ConstraintLayout holderLayout;

        public ViewHolder(View v) {
            super(v);
            productName = (TextView) v.findViewById(R.id.productName);
            txtPrice = (TextView) v.findViewById(R.id.priceLine);
            holderLayout = (ConstraintLayout) v.findViewById(R.id.itemHolder);
            holderLayout.setLongClickable(true);
            txtQuantity = (TextView) v.findViewById(R.id.quantity);
            context = v.getContext();

        }
    }

    public class HeaderViewHolder extends OrderAdapter.ViewHolder {
        public TextView total;
        public Button btnOrder;
        public ConstraintLayout holderLayout;

        public HeaderViewHolder(View v) {
            super(v);
            total = (TextView) v.findViewById(R.id.categoryName);
            btnOrder = (Button) v.findViewById(R.id.btnOrder);
            holderLayout = (ConstraintLayout) v.findViewById(R.id.itemHolder);
            context = v.getContext();
        }
    }


    /*public void add(int position, String item) {
        mDataset.add(position, item);
        notifyItemInserted(position);
    }*/
    // Provide a suitable constructor (depends on the kind of dataset)
   /* public CategoryAdapter(ArrayList<String> myDataset) {
        mDataset = myDataset;
    }*/

    // Create new views (invoked by the layout manager)
    @Override
    public OrderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        // create a new view

        View v;

        if (viewType == HEADER_VIEW) {
            Log.d("THIS IS CALLED", "YES!");
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_checkout_header_row, parent, false);

            return new HeaderViewHolder(v);
        }

        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_checkout_item_row, parent, false);
        // set the view's size, margins, paddings and layout parameters
        //v.setOnCreateContextMenuListener((View.OnCreateContextMenuListener) parent.getContext());
        ViewHolder vh = new ViewHolder(v);
        context = parent.getContext();
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        try {
            if (holder instanceof OrderAdapter.HeaderViewHolder) {
                OrderAdapter.HeaderViewHolder vh = (OrderAdapter.HeaderViewHolder) holder;

                vh.total.setText(((CheckoutActivity) context).getTotal());

                vh.btnOrder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (MainActivity.productsOrder.size() > 1) {
                            final Dialog dialog = new Dialog(context);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView(R.layout.dialog_yes_no);

                            final TextView title = (TextView) dialog.findViewById(R.id.titleText);
                            title.setText(R.string.place_order);

                            final TextView message = (TextView) dialog.findViewById(R.id.bodyText);
                            message.setText(R.string.place_order_msg);
                            //message.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));

                            Button btnYes = (Button) dialog.findViewById(R.id.btnYes);
                            Button btnNo = (Button) dialog.findViewById(R.id.btnNo);

                            btnNo.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });

                            btnYes.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                    if(!CheckoutActivity.orderProcessing) {
                                        ((CheckoutActivity) context).sendOrder();
                                    }
                                }
                            });
                            dialog.show();
                        } else {
                            Toast.makeText(context, "No orders on the record", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            } else {

                holder.productName.setText(mDataset.get(position).name);

                NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
                format.setCurrency(Currency.getInstance(mDataset.get(position).code));
                final String result = format.format(Double.valueOf(mDataset.get(position).price));
                holder.txtPrice.setText(result);

                holder.txtQuantity.setText(context.getString(R.string.items_x_sign) + String.valueOf(mDataset.get(position).quantity));

                if (mDataset.get(position).isSelected()) {
                    holder.holderLayout.setBackgroundColor((ResourcesCompat.getColor(context.getResources(), R.color.selected, context.getTheme())));
                } else {
                    holder.holderLayout.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.recycler_item_bg, context.getTheme()));
                }

                holder.holderLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (actionMode) {
                            final Order model = mDataset.get(position);

                            int currSelected = getAllSelected();

                            if (!model.isSelected()) {
                                holder.holderLayout.setBackgroundColor((ResourcesCompat.getColor(context.getResources(), R.color.selected, context.getTheme())));
                                model.setSelected(true);
                                am.setTitle("Selected: " + String.valueOf(currSelected + 1));
                            } else {
                                holder.holderLayout.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.recycler_item_bg, context.getTheme()));
                                model.setSelected(false);
                                am.setTitle("Selected: " + String.valueOf(currSelected - 1));
                                if (currSelected == 1 && am != null) {
                                    am.finish();
                                    actionMode = false;
                                }
                            }

                        }
                    }
                });

                holder.holderLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        final Order model = mDataset.get(position);

                        int currSelected = getAllSelected();

                        if (currSelected == 0) {
                            am = ((CheckoutActivity) context).getSupportActionBar().startActionMode(((CheckoutActivity) context).mActionModeCallback);
                            actionMode = true;
                        }
                        if (!model.isSelected()) {
                            holder.holderLayout.setBackgroundColor((ResourcesCompat.getColor(context.getResources(), R.color.selected, context.getTheme())));
                            model.setSelected(true);
                            am.setTitle("Selected: " + String.valueOf(currSelected + 1));
                        } else {
                            holder.holderLayout.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.recycler_item_bg, context.getTheme()));
                            model.setSelected(false);
                            am.setTitle("Selected: " + String.valueOf(currSelected - 1));
                            if (currSelected == 1 && am != null) {
                                am.finish();
                                actionMode = false;
                            }
                        }
                        return true;
                    }
                });


            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int getAllSelected() {
        int res = 0;
        for (int i = 1; i < mDataset.size(); i++) {
            if (mDataset.get(i).isSelected()) {
                res += 1;
            }
        }
        return res;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            // This is where we'll add footer.
            return HEADER_VIEW;
        }

        return super.getItemViewType(position);
    }

}
