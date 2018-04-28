package com.example.ben.myapplication;

import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Main2Activity extends AppCompatActivity implements ChangePhotoDialog.OnPhotoReceivedListener{
    private static final String TAG = "Main2Activity";

    private static final int RC_SIGN_IN = 9001;

    private MainActivityViewModel mViewModel;

    public Uri mSelectedImageUri;
    private Uri mTakeImageUri;
    private BroadcastReceiver mBroadcastReceiver;
    public Uri mDownloadUrl ;
     public Uri mDownloadUrl2;

    private StorageReference mStorageRef;
    public String sdownLoadUri ;

    private ProgressDialog mProgressDialog;

    Item item = new Item();
    @Override
    public void getImagePath(Uri imagePath) {
        if( !imagePath.toString().equals("")){

            mSelectedImageUri = imagePath;
            Log.d(TAG, "getImagePath: got the image uri: " + mSelectedImageUri);
           // ImageLoader.getInstance().displayImage(imagePath.toString(), profileImage);
            Picasso.get()
                    .load(mSelectedImageUri)
                    .resize(388, 200)
                    .centerCrop()
                    .into(profileImage);
           // uploadFromUrit(mSelectedImageUri);
            uploadFromUri(mSelectedImageUri);
           // addPage(mDownloadUrl);
        }

    }
    @Override
    public void getImageBitmap(Uri imagePath) {
        if( !imagePath.toString().equals("")){

            mTakeImageUri = imagePath;
            Log.d(TAG, "getImagePath: got the image uri: " + mSelectedImageUri);
            // ImageLoader.getInstance().displayImage(imagePath.toString(), profileImage);
            Picasso.get()
                    .load(imagePath)
                    .resize(388, 200)
                    .centerCrop()
                    .into(profileImage);
            uploadFromUri(mTakeImageUri);
           // addPage(mDownloadUrl);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        //updateUI(mAuth.getCurrentUser());

        // Register receiver for uploads and downloads
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
       // manager.registerReceiver(mBroadcastReceiver, MyDownloadService.getIntentFilter());
       // manager.registerReceiver(mBroadcastReceiver, MyUploadService.getIntentFilter());
        manager.registerReceiver(mBroadcastReceiver, MyUploadService.getIntentFilter());
    }

    @Override
    public void onStop() {
        super.onStop();

        // Unregister download receiver
       LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
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



    @BindView(R.id.add)
    Button badd;
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






        if (savedInstanceState!= null) {
            //mFileUri = savedInstanceState.getParcelable(KEY_FILE_URI);
            //mDownloadUrl = savedInstanceState.getParcelable(KEY_DOWNLOAD_URL);
        }
        onNewIntent(getIntent());
        mBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive1:" + intent);
               hideProgressDialog();

                switch (intent.getAction()) {

                    case MyUploadService.UPLOAD_COMPLETED:
                    case MyUploadService.UPLOAD_ERROR:
                        onUploadResultIntent(intent);
                        mDownloadUrl = intent.getParcelableExtra(MyUploadService.EXTRA_DOWNLOAD_URL);
                        item.setPhoto(mDownloadUrl.toString());


                        break;
                }
            }
        };
    }





    @OnClick(R.id.add)
    public void clicked(View view) {

        addPage();
    }




    private void setEditingEnabled(boolean enabled) {
      //  mname.setEnabled(enabled);

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




        // Start MyUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        startService(new Intent(this, MyUploadService.class)
                .putExtra(MyUploadService.EXTRA_FILE_URI, fileUri)
                .setAction(MyUploadService.ACTION_UPLOAD));

        // Show loading spinner
        showProgressDialog(getString(R.string.progress_uploading));
    }
    private void showProgressDialog(String caption) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
        }


        mProgressDialog.setMessage(caption);
        mProgressDialog.show();
    }
    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
    private void onUploadResultIntent(Intent intent) {
        Log.d(TAG, "onUploadResultIntent:" + mDownloadUrl);
        // Got a new intent from MyUploadService with a success or failure
        mDownloadUrl = intent.getParcelableExtra(MyUploadService.EXTRA_DOWNLOAD_URL);
       // mFileUri = intent.getParcelableExtra(MyUploadService.EXTRA_FILE_URI);
        Log.d(TAG, "onUploadResultIntent:" + mDownloadUrl);
        mDownloadUrl2 =mDownloadUrl;
        sdownLoadUri = mDownloadUrl.toString();
        Log.d(TAG, "onUploadResultIntent:" +  sdownLoadUri);
        //updateUI(mAuth.getCurrentUser());
    }


    public void addPage(){
        Log.d(TAG, "addPage:" + mDownloadUrl);
        Intent intent = new Intent(this,Main3Activity.class);
        intent.putExtra("uri",mDownloadUrl.toString());
        if(mSelectedImageUri != null){
            intent.putExtra("selecturi",mSelectedImageUri.toString());
        }else if(mTakeImageUri != null){
            intent.putExtra("takeuri",mTakeImageUri.toString());
        }
        startActivity(intent);
       finish();



    }
    /*
    public void addItem(Uri uri) {


        Intent intent = new Intent(this,Main3Activity.class);
        startActivity(intent);
        startActivity(new Intent(this,Main3Activity.class)
                .putExtra(Main3Activity.UPLOAD_URI, uri));
        startActivity(new Intent(this,Main3Activity.class)
                .putExtra(Main3Activity.UPLOAD_URI, uri));

        uploadFromUri(mSelectedImageUri);
        if(mTakeImageUri != null){

           uploadFromUri(mTakeImageUri);}
            //uri=mTakeImageUri;
         if(mSelectedImageUri != null){
            //uri=mSelectedImageUri;

            uploadFromUri(mSelectedImageUri);
        }
        Log.d(TAG, "uploadFromUri:onSuccessup4" + sdownLoadUri);
        Log.d(TAG, "uploadFromUri:onSuccessadd"+mSelectedImageUri);
        Log.d(TAG, "uploadFromUri:onSuccess2"+mDownloadUrl );
        Log.d(TAG, "uploadFromUri:onSuccess23"+mDownloadUrl2 );
        Log.d(TAG, "onUploadResultIntent:" +  sdownLoadUri);
        item.setName(mname.getText().toString());
        item.setLocation(mcity.getText().toString());
        item.setCategory(mcat.getText().toString());
        //item.setPhoto(mDownloadUrl.toString());
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

        mRestaurantRef.set(item);
        setEditingEnabled(false);
        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

        setEditingEnabled(true);
       // finish();




    }*/
    private void uploadFromUrit(final Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // [START_EXCLUDE]
        //taskStarted();
       // showProgressNotification(getString(R.string.progress_uploading), 0, 0);
        // [END_EXCLUDE]

        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg
        final StorageReference photoRef = mStorageRef.child("photos")
                .child(fileUri.getLastPathSegment());
        // [END get_child_ref]

        // Upload file to Firebase Storage
        Log.d(TAG, "uploadFromUri:dst:" + photoRef.getPath());
        photoRef.putFile(fileUri).
                addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                       // showProgressNotification(getString(R.string.progress_uploading),
                              //  taskSnapshot.getBytesTransferred(),
                              //  taskSnapshot.getTotalByteCount());
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Upload succeeded
                        Log.d(TAG, "uploadFromUri:onSuccess");

                        // Get the public download URL
                        mDownloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                        Log.d(TAG, "uploadFromUri:onSuccess"+mDownloadUrl);
                        // [START_EXCLUDE]
                        // broadcastUploadFinished(downloadUri, fileUri);
                        //broadcastUploadFinished(downloadUri, downloadUri);
                       // showUploadFinishedNotification(downloadUri, fileUri);
                        //taskCompleted();
                        // [END_EXCLUDE]
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Upload failed
                        Log.w(TAG, "uploadFromUri:onFailure", exception);

                        // [START_EXCLUDE]
                       // broadcastUploadFinished(null, fileUri);
                        //showUploadFinishedNotification(null, fileUri);
                        //taskCompleted();
                        // [END_EXCLUDE]
                    }
                });
    }
    }

