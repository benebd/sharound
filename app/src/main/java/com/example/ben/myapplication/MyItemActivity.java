package com.example.ben.myapplication;

import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.example.ben.myapplication.adapter.*;
import com.example.ben.myapplication.model.Rating;
import com.example.ben.myapplication.model.Item;
import com.example.ben.myapplication.util.RatingUtil;
import com.example.ben.myapplication.util.ItemUtil;
import com.example.ben.myapplication.viewmodel.MainActivityViewModel;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MyItemActivity extends AppCompatActivity implements

        ItemAdapter.OnItemSelectedListener {

    private static final String TAG = "MyItemActivity";

    private static final int RC_SIGN_IN = 9001;

    private static final int LIMIT = 50;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.recycler_restaurants)
    RecyclerView mItemsRecycler;

    @BindView(R.id.view_empty)
    ViewGroup mEmptyView;

    private FirebaseFirestore mFirestore;
    private Query mQuery;

    private FilterDialogFragment mFilterDialog;
    private ItemAdapter mAdapter;

    private MainActivityViewModel mViewModel;

   // FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyItemActivity.this,AddPhotoActivity.class);
                startActivity(intent);
            }
        });
        // View model
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);

        // Firestore
        mFirestore = FirebaseFirestore.getInstance();
       //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Get ${LIMIT} restaurants
        //user.getUid()..whereEqualTo("userid", user.getUid())
        //if(user != null) {
      //
     //  Log.d(TAG, "Write batch succeeded.");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String sUser =user.getUid();
        Log.d(TAG,"userid1"+sUser);
            mQuery = mFirestore.collection("items").whereEqualTo("userid", user.getUid())
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(LIMIT);
       // }
        // RecyclerView
        mAdapter = new ItemAdapter(mQuery, this) {
            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    mItemsRecycler.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mItemsRecycler.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(findViewById(android.R.id.content),
                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show();
            }
        };
        mItemsRecycler.setLayoutManager(new LinearLayoutManager(this));
        // mItemsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mItemsRecycler.setAdapter(mAdapter);

        // Filter Dialog
        mFilterDialog = new FilterDialogFragment();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Start sign in if necessary
        if (shouldStartSignIn()) {
            startSignIn();
            return;
        }

        // Apply filters
        //onFilter(mViewModel.getFilters());

        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_want_items:
                // onAddItemsClicked();
                Intent intent1 = new Intent(this,LikeItemActivity.class);
                startActivity(intent1);
                break;
            case R.id.menu_sign_out:
                AuthUI.getInstance().signOut(this);
                startSignIn();
                break;
            case R.id.add:
                Intent intent = new Intent(this,AddPhotoActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_map:
                Intent intent4 =new Intent(this,navAct.class);
                startActivity(intent4);
                break;
            case R.id.menu_myitem:
                Intent intent2 = new Intent(this,MyItemActivity.class);
                startActivity(intent2);
            case R.id.menu_all_item:
                Intent intent3 =new Intent(this,MainActivity.class);
                startActivity(intent3);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            mViewModel.setIsSigningIn(false);

            if (resultCode != RESULT_OK) {
                if (response == null) {
                    // User pressed the back button.
                    finish();
                } else if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSignInErrorDialog(R.string.message_no_network);
                } else {
                    showSignInErrorDialog(R.string.message_unknown);
                }
            }
        }
    }

    @OnClick(R.id.filter_bar)
    public void onFilterClicked() {
        // Show the dialog containing filter options
        mFilterDialog.show(getSupportFragmentManager(), FilterDialogFragment.TAG);
    }

    /*@OnClick(R.id.button_clear_filter)
    public void onClearFilterClicked() {
        mFilterDialog.resetFilters();

        onFilter(Filters.getDefault());
    }*/

    @Override
    public void onItemSelected(DocumentSnapshot restaurant) {
        // Go to the details page for the selected restaurant
        Intent intent = new Intent(this, ItemDetailActivity.class);
        intent.putExtra(ItemDetailActivity.KEY_ITEM_ID, restaurant.getId());
        intent.putExtra(ItemDetailActivity.KEY_ITEM_UID,restaurant.getString("userid"));
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    }

   /* @Override
    public void onFilter(Filters filters) {
        // Construct query basic query
        Query query = mFirestore.collection("items");

        // Category (equality filter)
        if (filters.hasCategory()) {
            query = query.whereEqualTo(Item.FIELD_CATEGORY, filters.getCategory());
        }

        // City (equality filter)
        if (filters.hasCity()) {
            query = query.whereEqualTo(Item.FIELD_LOCATION, filters.getCity());
        }

        // Price (equality filter)
        if (filters.hasPrice()) {
            query = query.whereEqualTo(Item.FIELD_PRICE, filters.getPrice());
        }

        // Sort by (orderBy with direction)
        if (filters.hasSortBy()) {
            query = query.orderBy(filters.getSortBy(), filters.getSortDirection());
        }

        // Limit items
        query = query.limit(LIMIT);

        // Update the query
        mAdapter.setQuery(query);

        // Set header
        mCurrentSearchView.setText(Html.fromHtml(filters.getSearchDescription(this)));
        mCurrentSortByView.setText(filters.getOrderDescription(this));

        // Save filters
        mViewModel.setFilters(filters);
    }*/

    private boolean shouldStartSignIn() {
        return (!mViewModel.getIsSigningIn() && FirebaseAuth.getInstance().getCurrentUser() == null);
    }

    private void startSignIn() {
        // Sign in with FirebaseUI
        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(Collections.singletonList(
                        new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()))
                .setIsSmartLockEnabled(false)
                .build();

        startActivityForResult(intent, RC_SIGN_IN);
        mViewModel.setIsSigningIn(true);
        //user =FirebaseAuth.getInstance().getCurrentUser();
       // String sUser =user.getUid();
      //  Log.d(TAG,"userid2"+sUser);
    }

    private void onAddItemsClicked() {
        // Add a bunch of random restaurants
        WriteBatch batch = mFirestore.batch();
        for (int i = 0; i < 10; i++) {
            DocumentReference restRef = mFirestore.collection("items").document();

            // Create random restaurant / ratings
            Item randomItem = ItemUtil.getRandom(this);
            List<Rating> randomRatings = RatingUtil.getRandomList(randomItem.getNumRatings());
            randomItem.setAvgRating(RatingUtil.getAverageRating(randomRatings));

            // Add restaurant
            batch.set(restRef, randomItem);

            // Add ratings to subcollection
            for (Rating rating : randomRatings) {
                batch.set(restRef.collection("ratings").document(), rating);
            }
        }

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Write batch succeeded.");
                } else {
                    Log.w(TAG, "write batch failed.", task.getException());
                }
            }
        });
    }

    private void showSignInErrorDialog(@StringRes int message) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.title_sign_in_error)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.option_retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startSignIn();
                    }
                })
                .setNegativeButton(R.string.option_exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).create();

        dialog.show();
    }
}
