package com.example.ben.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ben.myapplication.model.Item;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddItemDetailActivity extends AppCompatActivity {
    private static final String TAG = "AddItemDetailActivity";

    public String uploadUri;
    public String takeUri;
    public String selectUri;

    private FirebaseFirestore mFirestore;
    private DocumentReference mRestaurantRef;
    Item item = new Item();


    @BindView(R.id.profile_image)
    ImageView profileImage;
    @BindView(R.id.inname)
    EditText inname;

    @BindView(R.id.spinner2)
    Spinner spinner2;



    @BindView(R.id.spinner)
    Spinner spinner;



    @BindView(R.id.add)
    Button add;
    @BindView(R.id.image)
    RelativeLayout image;

    //Location
    @BindView(R.id.E_latitude)
    EditText ELatitude;
    @BindView(R.id.E_longitude)
    EditText ELongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mFirestore = FirebaseFirestore.getInstance();
        mRestaurantRef = mFirestore.collection("items").document();


        //  Uri fileUri = null;
        // Bundle extras = getIntent().getExtras();
        // fileUri=Uri.parse(extras.getString("uri"));
        Intent intent = getIntent();
        uploadUri = intent.getStringExtra("uri");
        takeUri = intent.getStringExtra("takeuri");
        selectUri = intent.getStringExtra("selecturi");
        Log.d(TAG, "Main3Activitys" + selectUri);
        Log.d(TAG, "Main3Activityt" + takeUri);
        //Intent intent = new Intent();
        // Uri fileUri = intent.getParcelableExtra(UPLOAD_URI);
        // intent,getString
        Log.d(TAG, "Main3activity" + uploadUri);
        if (selectUri != null) {
            // Uri.parse(selectUri);
            Picasso.get()
                    .load(Uri.parse(selectUri))
                    .resize(100, 100)
                    .centerCrop()
                    .into(profileImage);
        } else if (takeUri != null) {
            // Uri.parse(takeUri);
            Picasso.get()
                    .load(Uri.parse(takeUri))
                    .resize(100, 100)
                    .centerCrop()
                    .into(profileImage);
        }
    }

    @OnClick(R.id.add)
    public void onViewClicked() {
        addItem();
    }

    public void addItem() {

        Log.d(TAG, "M3additem" + uploadUri);
        String selectedCat = (String) spinner.getSelectedItem();
        String selectedCity = (String) spinner2.getSelectedItem();
        mRestaurantRef = mFirestore.collection("items").document();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userName = user.getDisplayName();
        String userid = user.getUid();

        item.setName(inname.getText().toString());
        item.setLocation(selectedCity);
        item.setCategory(selectedCat);
        item.setPhoto(uploadUri);
       // item.setPrice(1);
       // item.setNumRatings(1);
        item.setUsername(userName);
        item.setUserid(userid);
        //item.setLatitude(Double.parseDouble(ELatitude.getText().toString()));
        //item.setLongitude(Double.parseDouble(ELongitude.getText().toString()));
        item.setLongitude(1);
        item.setLatitude(1);
        //item.setPrice(Integer.parseInt(mprice.toString()));
        //item.setNumRatings(Integer.parseInt(mrating.toString()));



        mRestaurantRef.set(item);
        // setEditingEnabled(false);
        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();


        finish();


    }

}
