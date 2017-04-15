package com.taptoscan.taptoscan;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Kreso on 23.3.2017..
 */

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {


    private List<Category> mDataset;
    private Context context;

    public CategoryAdapter(List<Category> categoriesList) {
        mDataset = categoriesList;
    }

    private static final int HEADER_VIEW = 1;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView catName;
        public ConstraintLayout holderLayout;
        public ImageView icon;

        public ViewHolder(View v) {
            super(v);
            catName = (TextView) v.findViewById(R.id.categoryName);
            holderLayout = (ConstraintLayout) v.findViewById(R.id.itemHolder);
            icon = (ImageView) v.findViewById(R.id.icon);
            context = v.getContext();
        }
    }

    public class HeaderViewHolder extends ViewHolder {
        public TextView businessName, businessAddress, businessAddressCityCountry;
        public ConstraintLayout holderLayout;

        public HeaderViewHolder(View v) {
            super(v);
            businessName = (TextView) v.findViewById(R.id.categoryName);
            businessAddress = (TextView) v.findViewById(R.id.businessAddress);
            businessAddressCityCountry = (TextView) v.findViewById(R.id.businessAddressCityCountry);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
   /* public CategoryAdapter(ArrayList<String> myDataset) {
        mDataset = myDataset;
    }*/

    // Create new views (invoked by the layout manager)
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {

        View v;

        if (viewType == HEADER_VIEW) {
            Log.d("THIS IS CALLED", "YES!");
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_header_row, parent, false);

            return new HeaderViewHolder(v);
        }

        // create a new view
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_row, parent, false);
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

        try {
            if (holder instanceof HeaderViewHolder) {
                HeaderViewHolder vh = (HeaderViewHolder) holder;

                vh.businessName.setText(MainActivity.businessName);
                vh.businessAddress.setText(MainActivity.businessAddress);
                vh.businessAddressCityCountry.setText(MainActivity.businessAddressCityCountry);

            } else {

                holder.catName.setText(mDataset.get(position).name);
                // Remove file extension
                String str = mDataset.get(position).icon_res.substring(0, mDataset.get(position).icon_res.lastIndexOf('.'));
                // Replace - with underscore _
                str = str.replace("-", "_");

                // Find resource in drawable folder
                int res = context.getResources().getIdentifier("ic_" + str, "drawable", context.getPackageName());
                // Set icon resource
                if (res == 0) {
                    holder.icon.setImageResource(R.drawable.ic_coffee);
                } else {
                    holder.icon.setImageResource(res);
                }
                holder.holderLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //remove(position);
                        ((MainActivity) context).loadProducts(mDataset.get(position).id, mDataset.get(position ).name);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
