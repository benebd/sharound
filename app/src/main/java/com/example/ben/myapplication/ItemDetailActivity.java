package com.example.ben.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ben.myapplication.adapter.RatingAdapter;
import com.example.ben.myapplication.model.Item;
import com.example.ben.myapplication.model.Rating;
import com.example.ben.myapplication.util.ItemUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class ItemDetailActivity extends AppCompatActivity
        implements EventListener<DocumentSnapshot>, RatingDialogFragment.RatingListener {

    private static final String TAG = "ItemDetail";

    public static final String KEY_ITEM_ID = "key_item_id";
    public static final String KEY_ITEM_UID = "key_item_uid";
    public static final String KEY_ITEM = "key_item";
    @BindView(R.id.item_image)
    ImageView mImageView;

    @BindView(R.id.item_name)
    TextView mNameView;

    @BindView(R.id.item_rating)
    MaterialRatingBar mRatingIndicator;

    @BindView(R.id.item_num_ratings)
    TextView mNumRatingsView;

    @BindView(R.id.item_city)
    TextView mCityView;

    @BindView(R.id.item_category)
    TextView mCategoryView;

    @BindView(R.id.item_price)
    TextView mPriceView;

    @BindView(R.id.view_empty_ratings)
    ViewGroup mEmptyView;

    @BindView(R.id.recycler_ratings)
    RecyclerView mRatingsRecycler;

    @BindView(R.id.fab_show_delete)
    FloatingActionButton fabShowDelete;

    private RatingDialogFragment mRatingDialog;

    private FirebaseFirestore mFirestore;
    private DocumentReference mItemRef;
    private DocumentReference mLikeRef;
    private ListenerRegistration mRestaurantRegistration;

    private RatingAdapter mRatingAdapter;
    String itemid;
    Item item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        ButterKnife.bind(this);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Get item ID from extras

        itemid = getIntent().getExtras().getString(KEY_ITEM_ID);
        String itemUid = getIntent().getExtras().getString(KEY_ITEM_UID);
        String mItemid = getIntent().getExtras().getString(KEY_ITEM);

        Log.d(TAG, "onCreateitemid" + itemid);
        Log.d(TAG, "onCreateitemUid" + itemUid);
        if (itemid == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_ITEM_ID);
        }
        String userid = user.getUid();
        if (userid.equals(itemUid)) {
            fabShowDelete.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "onCreateitemUid2" + userid);

        mFirestore = FirebaseFirestore.getInstance();

        // Get reference to the item
        if(mItemid != null)mItemRef = mFirestore.collection("items").document(mItemid);
        if(itemid !=null)mItemRef = mFirestore.collection("items").document(itemid);

        if(itemid !=null)mLikeRef = mFirestore.collection("likeitem").document(userid).collection("items").document(itemid);
        // Get ratings
        Query ratingsQuery = mItemRef
                .collection("ratings")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50);

        // RecyclerView
        mRatingAdapter = new RatingAdapter(ratingsQuery) {
            @Override
            protected void onDataChanged() {
                if (getItemCount() == 0) {
                    mRatingsRecycler.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mRatingsRecycler.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
            }
        };
        mRatingsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRatingsRecycler.setAdapter(mRatingAdapter);

        mRatingDialog = new RatingDialogFragment();
    }

    @Override
    public void onStart() {
        super.onStart();

        mRatingAdapter.startListening();
        mRestaurantRegistration = mItemRef.addSnapshotListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        mRatingAdapter.stopListening();

        if (mRestaurantRegistration != null) {
            mRestaurantRegistration.remove();
            mRestaurantRegistration = null;
        }
        Toast.makeText(this,"hi",Toast.LENGTH_LONG);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    /**
     * Listener for the Item document ({@link #mItemRef}).
     */
    @Override
    public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "item:onEvent", e);
            return;
        }
        item =snapshot.toObject(Item.class);
        onRestaurantLoaded(snapshot.toObject(Item.class));
    }

    private Item returnItem(Item item){
        return  item;
    }
    private void onRestaurantLoaded(Item item) {
        mNameView.setText(item.getName());
        mRatingIndicator.setRating((float) item.getAvgRating());
        mNumRatingsView.setText(getString(R.string.fmt_num_ratings, item.getNumRatings()));
        mCityView.setText(item.getLocation());
        mCategoryView.setText(item.getCategory());
        mPriceView.setText(ItemUtil.getPriceString(item));

        // Background image
        Glide.with(mImageView.getContext())
                .load(item.getPhoto())
                .into(mImageView);
    }

    private void addLikeItem(Item item){

        mLikeRef.set(item);
    }

    @OnClick(R.id.item_button_back)
    public void onBackArrowClicked(View view) {
        onBackPressed();
    }

    @OnClick(R.id.fab_show_rating_dialog)
    public void onAddRatingClicked(View view) {
        mRatingDialog.show(getSupportFragmentManager(), RatingDialogFragment.TAG);
    }

    @OnClick(R.id.fab_show_delete)
    public void onViewClicked() {
        Log.d(TAG, "delete" + itemid);

        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(this);

        builder.setTitle("Delete item")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete

//                        Intent broadcast = new Intent(Intent.ACTION_DELETE)
//                                .putExtra("itemid", itemid);
//
//                       sendBroadcast(broadcast);
//                        Intent intent = new Intent(ItemDetailActivity.this,MainActivity.class);
//                        intent.putExtra("itemid",itemid);
//                        startActivity(intent);
                        mFirestore.collection("items").document(itemid).delete();
                       // finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        //mFirestore.collection("items").document(itemid).delete();
       // Toast.makeText(this, "successful delete", Toast.LENGTH_LONG);
        //  mItemRef.document(itemid).delete();
       // DeleteDialog dialog = new DeleteDialog();
       // dialog.show(getSupportFragmentManager(), getString(R.string.dialog_change_photo));
    }

    @OnClick(R.id.fab_show_like)
    public void onLikeViewClicked() {
        addLikeItem(item);
        Toast.makeText(this,"Liked",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRating(Rating rating) {
        // In a transaction, add the new rating and update the aggregate totals
        addRating(mItemRef, rating)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Rating added");

                        // Hide keyboard and scroll to top
                        hideKeyboard();
                        mRatingsRecycler.smoothScrollToPosition(0);
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Add rating failed", e);

                        // Show failure message and hide keyboard
                        hideKeyboard();
                        Snackbar.make(findViewById(android.R.id.content), "Failed to add rating",
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private Task<Void> addRating(final DocumentReference itemRef, final Rating rating) {
        // Create reference for new rating, for use inside the transaction
        final DocumentReference ratingRef = itemRef.collection("ratings").document();

        // In a transaction, add the new rating and update the aggregate totals
        return mFirestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                Item item = transaction.get(itemRef).toObject(Item.class);

                // Compute new number of ratings
                int newNumRatings = item.getNumRatings() + 1;

                // Compute new average rating
                double oldRatingTotal = item.getAvgRating() * item.getNumRatings();
                double newAvgRating = (oldRatingTotal + rating.getRating()) / newNumRatings;

                // Set new item info
                item.setNumRatings(newNumRatings);
                item.setAvgRating(newAvgRating);

                // Commit to Firestore
                transaction.set(itemRef, item);
                transaction.set(ratingRef, rating);

                return null;
            }
        });
    }


    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }



}
