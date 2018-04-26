package com.example.ben.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Main3Activity extends AppCompatActivity {
    private static final String TAG = "Main3Activity";
    public static final String URI_MESSAGE = "message.uri";
    public static final String UPLOAD_URI = "URI";
    public String string;
    private FirebaseFirestore mFirestore;
    private DocumentReference mRestaurantRef;
    Item item = new Item();
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.textHeader)
    TextView textHeader;
    @BindView(R.id.profile_image)
    ImageView profileImage;
    @BindView(R.id.inname)
    EditText inname;
    @BindView(R.id.textView3)
    TextView textView3;
    @BindView(R.id.R1)
    RelativeLayout R1;
    @BindView(R.id.spinner2)
    Spinner spinner2;
    @BindView(R.id.incity)
    EditText incity;
    @BindView(R.id.textView5)
    TextView textView5;
    @BindView(R.id.R2)
    RelativeLayout R2;
    @BindView(R.id.spinner)
    Spinner spinner;
    @BindView(R.id.incat)
    EditText incat;
    @BindView(R.id.textView4)
    TextView textView4;
    @BindView(R.id.R3)
    RelativeLayout R3;
    @BindView(R.id.inphoto)
    EditText inphoto;

    @BindView(R.id.R4)
    RelativeLayout R4;
    @BindView(R.id.inprice)
    EditText inprice;

    @BindView(R.id.R5)
    RelativeLayout R5;
    @BindView(R.id.inrating)
    EditText inrating;

    @BindView(R.id.R6)
    RelativeLayout R6;
    @BindView(R.id.add)
    Button add;
    @BindView(R.id.image)
    RelativeLayout image;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mFirestore = FirebaseFirestore.getInstance();
        mRestaurantRef = mFirestore.collection("items").document();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //  Uri fileUri = null;
        // Bundle extras = getIntent().getExtras();
        // fileUri=Uri.parse(extras.getString("uri"));
        Intent intent = getIntent();
        string = intent.getStringExtra("uri");
        //Intent intent = new Intent();
        // Uri fileUri = intent.getParcelableExtra(UPLOAD_URI);
        // intent,getString
        Log.d(TAG, "Main3activity" + string);
    }

    @OnClick(R.id.add)
    public void onViewClicked() {
        addItem();
    }

    public void addItem() {

     Log.d(TAG,"M3additem"+string);

        mRestaurantRef = mFirestore.collection("items").document();


        item.setName(inname.getText().toString());
        item.setLocation(incity.getText().toString());
        item.setCategory(incat.getText().toString());
        item.setPhoto(string);
        item.setPrice(1);
        item.setNumRatings(1);
        //item.setUsername( FirebaseAuth.getInstance().getCurrentUser().toString());
        //item.setUserid();
        //item.setPrice(Integer.parseInt(mprice.toString()));
        //item.setNumRatings(Integer.parseInt(mrating.toString()));


/*
        // [START_EXCLUDE]


        // [END_EXCLUDE]

        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg
        final StorageReference photoRef = mStorageRef.child("photos")
                .child(uri.getLastPathSegment());
        // [END get_child_ref]

        // Upload file to Firebase Storage
        Log.d(TAG, "uploadFromUri:dst:" + photoRef.getPath());
        photoRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Upload succeeded
                Log.d(TAG, "uploadFromUri:onSuccess");

                // Get the public download URL
                Uri downloadUri = taskSnapshot.getMetadata().getDownloadUrl();
                mDownloadUrl2 = downloadUri;
                sdownLoadUri = mDownloadUrl2.toString();
                item.setPhoto(sdownLoadUri);
                Log.d(TAG, "uploadFromUri:onSuccessup" + downloadUri);
                // [START_EXCLUDE]


                Log.d(TAG, "uploadFromUri:onSuccessup2" + mDownloadUrl2);
                sdownLoadUri = mDownloadUrl2.toString();
                Log.d(TAG, "uploadFromUri:onSuccessup3" + sdownLoadUri);
                // [END_EXCLUDE]

            }
        });
        */
        mRestaurantRef.set(item);
       // setEditingEnabled(false);
        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();


        // finish();




    }
}
