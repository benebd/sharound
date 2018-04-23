package com.example.ben.myapplication.adapter;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.ben.myapplication.R;
import com.example.ben.myapplication.model.Item;
import com.example.ben.myapplication.util.ItemUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

/**
 * RecyclerView adapter for a list of Items. {@link Item}.
 */
public class ItemAdapter extends FirestoreAdapter<ItemAdapter.ViewHolder> {




    public interface OnItemSelectedListener {

        void onItemSelected(DocumentSnapshot item);

    }

    private OnItemSelectedListener mListener;

    public ItemAdapter(Query query, OnItemSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_restaurant, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private static final SimpleDateFormat FORMAT  = new SimpleDateFormat(
                "MM/dd/yyyy", Locale.US);

        @BindView(R.id.restaurant_item_image)
        ImageView imageView;

        @BindView(R.id.restaurant_item_name)
        TextView nameView;

        @BindView(R.id.restaurant_item_rating)
        MaterialRatingBar ratingBar;

        @BindView(R.id.restaurant_item_num_ratings)
        TextView numRatingsView;

        @BindView(R.id.restaurant_item_price)
        TextView priceView;

        @BindView(R.id.restaurant_item_category)
        TextView categoryView;

        @BindView(R.id.restaurant_item_city)
        TextView cityView;

        @BindView(R.id.username)
        TextView usernameView;

        @BindView(R.id.item_time)
        TextView itemTimeView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnItemSelectedListener listener) {

            Item item = snapshot.toObject(Item.class);
            Resources resources = itemView.getResources();

            // Load image
            Glide.with(imageView.getContext())
                    .load(item.getPhoto())
                    .into(imageView);

            nameView.setText(item.getName());
            ratingBar.setRating((float) item.getAvgRating());
            cityView.setText(item.getLocation());
            categoryView.setText(item.getCategory());
            numRatingsView.setText(resources.getString(R.string.fmt_num_ratings,
                    item.getNumRatings()));
            priceView.setText(ItemUtil.getPriceString(item));
            usernameView.setText(item.getUsername());

            if (item.getTimestamp() != null) {
                itemTimeView.setText(FORMAT.format(item.getTimestamp()));
            }

            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onItemSelected(snapshot);
                    }
                }
            });
        }

    }
}
