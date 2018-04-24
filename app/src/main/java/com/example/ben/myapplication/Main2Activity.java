package com.example.ben.myapplication;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ben.myapplication.model.Item;
import com.example.ben.myapplication.viewmodel.MainActivityViewModel;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Main2Activity extends AppCompatActivity implements ChangePhotoDialog.OnPhotoReceivedListener{
    private static final String TAG = "Main2Activity";
    private FirebaseFirestore mFirestore;
    private DocumentReference mRestaurantRef;
    private RatingDialogFragment mRatingDialog;
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 9001;
    private Uri mFileUri;
    private MainActivityViewModel mViewModel;
    private boolean mStoragePermissions;
    public Uri mSelectedImageUri;
    private Uri mTakeImageUri;
    private Bitmap mSelectedImageBitmap;
    private byte[] mBytes;
    private double progress;
    private BroadcastReceiver mBroadcastReceiver;
    private Uri mDownloadUrl;
    private static final String KEY_FILE_URI = "key_file_uri";
    private static final String KEY_DOWNLOAD_URL = "key_download_url";
    private StorageReference mStorageRef;
    @Override
    public void getImagePath(Uri imagePath) {
        if( !imagePath.toString().equals("")){
            mSelectedImageBitmap = null;
            mSelectedImageUri = imagePath;
            Log.d(TAG, "getImagePath: got the image uri: " + mSelectedImageUri);
           // ImageLoader.getInstance().displayImage(imagePath.toString(), profileImage);
            Picasso.get()
                    .load(mSelectedImageUri)
                    .resize(100, 100)
                    .centerCrop()
                    .into(profileImage);
            uploadFromUrit(mSelectedImageUri);

        }

    }

    @Override
    public void getImageBitmap(Uri imagePath) {
        if( !imagePath.toString().equals("")){
            mSelectedImageBitmap = null;
            mTakeImageUri = imagePath;
            Log.d(TAG, "getImagePath: got the image uri: " + mSelectedImageUri);
            // ImageLoader.getInstance().displayImage(imagePath.toString(), profileImage);
            Picasso.get()
                    .load(imagePath)
                    .resize(100, 100)
                    .centerCrop()
                    .into(profileImage);
            uploadFromUrit(mTakeImageUri);
        }

    }
  /*  @Override
    public void getImageBitmap(Bitmap bitmap) {
        if(bitmap != null){
            mSelectedImageUri = null;
            mSelectedImageBitmap = bitmap;
            Log.d(TAG, "getImageBitmap: got the image bitmap: " + mSelectedImageBitmap);
            profileImage.setImageBitmap(bitmap);
        }
    }*/
    @BindView(R.id.inname)
    TextView mname;
    @BindView(R.id.incity)
    TextView mcity;
    @BindView(R.id.incat)
    TextView mcat;

    @BindView(R.id.inprice)
    TextView mprice;
    @BindView(R.id.inrating)
    TextView mrating;
    @BindView(R.id.add)
    Button badd;
    Item item = new Item();
    @BindView(R.id.profile_image)
    ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        setSupportActionBar(toolbar);
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mFirestore = FirebaseFirestore.getInstance();

        // Get reference to the item
        mRestaurantRef = mFirestore.collection("items").document();
        final DocumentReference ratingRef = mRestaurantRef.collection("ratings").document();
        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            mDownloadUrl= null;
        } else {
            mDownloadUrl= (Uri)extras.get("EXTRA_DOWNLOAD_URL");
        }
        if (savedInstanceState!= null) {

            mFileUri = savedInstanceState.getParcelable(KEY_FILE_URI);
            mDownloadUrl = savedInstanceState.getParcelable(KEY_DOWNLOAD_URL);
        }

        onNewIntent(getIntent());
        // Local broadcast receiver
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive:" + intent);


                switch (intent.getAction()) {

                    case MyUploadService.UPLOAD_COMPLETED:
                    case MyUploadService.UPLOAD_ERROR:
                        onUploadResultIntent(intent);
                        break;
                }
            }
        };
    }
    @Override
    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putParcelable(KEY_FILE_URI, mFileUri);
        out.putParcelable(KEY_DOWNLOAD_URL, mDownloadUrl);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Check if this Activity was launched by clicking on an upload notification
        if (intent.hasExtra(MyUploadService.EXTRA_DOWNLOAD_URL)) {
            onUploadResultIntent(intent);
        }

    }

    @OnClick(R.id.add)
    public void clicked(View view) {
        addItem();
    }


    public void addItem() {


        Log.d(TAG, "uploadFromUri:onSuccess"+mDownloadUrl);
        item.setName(mname.getText().toString());
        item.setLocation(mcity.getText().toString());
        item.setCategory(mcat.getText().toString());
        item.setPhoto(mDownloadUrl.toString());
        item.setPrice(1);
        item.setNumRatings(1);
        //item.setUsername( FirebaseAuth.getInstance().getCurrentUser().toString());
        //item.setUserid();
        //item.setPrice(Integer.parseInt(mprice.toString()));
        //item.setNumRatings(Integer.parseInt(mrating.toString()));

        mRestaurantRef.set(item);
        setEditingEnabled(false);
        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

        setEditingEnabled(true);
        finish();


    }

    private void setEditingEnabled(boolean enabled) {
        mname.setEnabled(enabled);

        if (enabled) {
            badd.setVisibility(View.VISIBLE);
        } else {
            badd.setVisibility(View.GONE);
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
            case R.id.menu_add_items:
                // onAddItemsClicked();
                break;
            case R.id.menu_sign_out:
                AuthUI.getInstance().signOut(this);
                startSignIn();
                break;
            case R.id.add:
                Intent intent = new Intent(this, Main2Activity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
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
    }

    @OnClick(R.id.profile_image)
    public void onViewClicked() {
        ChangePhotoDialog dialog = new ChangePhotoDialog();
        dialog.show(getSupportFragmentManager(), getString(R.string.dialog_change_photo));
    }
    private void uploadFromUri(Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // Save the File URI
        //mFileUri = fileUri;

        // Clear the last download, if any
       // updateUI(mAuth.getCurrentUser());
       // mDownloadUrl = null;

        // Start MyUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        startService(new Intent(this, MyUploadService.class)
                .putExtra(MyUploadService.EXTRA_FILE_URI, fileUri)
                .setAction(MyUploadService.ACTION_UPLOAD));

        // Show loading spinner
        //showProgressDialog(getString(R.string.progress_uploading));
    }
    private void onUploadResultIntent(Intent intent) {
        // Got a new intent from MyUploadService with a success or failure
        mDownloadUrl = intent.getParcelableExtra(MyUploadService.EXTRA_DOWNLOAD_URL);
        //mFileUri = intent.getParcelableExtra(MyUploadService.EXTRA_FILE_URI);

        //updateUI(mAuth.getCurrentUser());
    }

    private void uploadFromUrit(final Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // [START_EXCLUDE]


        // [END_EXCLUDE]

        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg
        final StorageReference photoRef = mStorageRef.child("photos")
                .child(fileUri.getLastPathSegment());
        // [END get_child_ref]

        // Upload file to Firebase Storage
        Log.d(TAG, "uploadFromUri:dst:" + photoRef.getPath());
        photoRef.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Upload succeeded
                Log.d(TAG, "uploadFromUri:onSuccess");

                // Get the public download URL
                Uri downloadUri = taskSnapshot.getMetadata().getDownloadUrl();
                Log.d(TAG, "uploadFromUri:onSuccess"+downloadUri);
                // [START_EXCLUDE]

               mDownloadUrl=downloadUri;


                // [END_EXCLUDE]
            }
        });

    }
    }

