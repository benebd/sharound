package com.example.ben.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ben.myapplication.model.Item;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
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
    double lo ;
    double la ;
    public String uploadUri;
    public String takeUri;
    public String selectUri;
    private FirebaseFirestore mFirestore;
    private DocumentReference mItemRef;
    Item item = new Item();
    int PLACE_PICKER_REQUEST = 1;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mFirestore = FirebaseFirestore.getInstance();
        mItemRef = mFirestore.collection("items").document();



        Intent intent = getIntent();
        uploadUri = intent.getStringExtra("uri");
        takeUri = intent.getStringExtra("takeuri");
        selectUri = intent.getStringExtra("selecturi");
        Log.d(TAG, "Main3Activitys" + selectUri);
        Log.d(TAG, "Main3Activityt" + takeUri);

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

    public void goPlacePicker(View view){
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try{
            startActivityForResult(builder.build(AddItemDetailActivity.this),PLACE_PICKER_REQUEST);
        }catch(GooglePlayServicesRepairableException e){
            e.printStackTrace();
        }catch (GooglePlayServicesNotAvailableException e){
            e.printStackTrace();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == PLACE_PICKER_REQUEST){
            Place place = PlacePicker.getPlace(AddItemDetailActivity.this, data);
            LatLng latlng = place.getLatLng();
            la = latlng.latitude;
             lo = latlng.longitude;
        }
    }
    @OnClick(R.id.add)
    public void onViewClicked() {
        addItem();
    }

    public void addItem() {
        Double dLongitude =null;
        Double dLatitude=null;
        Log.d(TAG, "M3additem" + uploadUri);
        String selectedCat = (String) spinner.getSelectedItem();
        String selectedCity = (String) spinner2.getSelectedItem();
        mItemRef = mFirestore.collection("items").document();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userName = user.getDisplayName();
        String userid = user.getUid();

//        if(sLatitude != null || sLongitude != null){
//            dLongitude = Double.parseDouble(sLongitude);
//         dLatitude = Double.parseDouble(sLatitude);}
        item.setName(inname.getText().toString());
        item.setLocation(selectedCity);
        item.setCategory(selectedCat);
        item.setPhoto(uploadUri);
        item.setUsername(userName);
        item.setUserid(userid);
        item.setLongitude(lo);
        item.setLatitude(la);
        mItemRef.set(item);

        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

        finish();

    }

}
