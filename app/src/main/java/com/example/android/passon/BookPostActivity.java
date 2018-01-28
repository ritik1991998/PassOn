package com.example.android.passon;

import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BookPostActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private static final int CHOOSE_CAMERA_RESULT = 1;
    private static final int GALLERY_RESULT = 2;
    public static File file;
    private FirebaseDatabase mfirebaseDatabase;
    public static DatabaseReference mPostDatabaseReference;
    public static DatabaseReference mUserDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    Uri tempuri;

    private EditText bookName;
    Spinner s1,s2;
    String filter1,filter2;
    private Button postButton, cameraIntent, galleryIntent;
    private Boolean bookNameEnable = false, filter1Enable = false, filter2Enable = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_post);

        bookName = (EditText) findViewById(R.id.edit1);
        s1 = (Spinner) findViewById(R.id.s1);
        s2 = (Spinner) findViewById(R.id.s2);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this,R.array.subject_spinner, android.R.layout.simple_spinner_dropdown_item);
        s1.setAdapter(adapter);
        s1.setOnItemSelectedListener(this);
        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(this,R.array.class_spinner, android.R.layout.simple_spinner_dropdown_item);
        s2.setAdapter(adapter1);
        s2.setOnItemSelectedListener(this);
        postButton = (Button) findViewById(R.id.postButton);
        cameraIntent=(Button)findViewById(R.id.camera_intent);
        galleryIntent=(Button)findViewById(R.id.gallery_intent);

        mfirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mPostDatabaseReference = mfirebaseDatabase.getReference().child("post");
        mUserDatabaseReference = mfirebaseDatabase.getReference().child("user");

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("MainActivity","Permission is granted");
            } else {

                Log.v("MainActivity","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }else { //permission is automatically granted on sdk<23 upon installation
            Log.v("MainActivity", "Permission is granted");
        }


        cameraIntent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "IMG_" + timeStamp + ".jpg");
                tempuri = Uri.fromFile(file);
                i.putExtra(MediaStore.EXTRA_OUTPUT, tempuri);
                i.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,0);
                startActivityForResult(i, CHOOSE_CAMERA_RESULT);
            }
        });

        galleryIntent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/jpeg");
                startActivityForResult(intent, GALLERY_RESULT);
            }
        });

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!(bookName.getText().toString().equals("")||filter1.equals("")||filter2.equals("")||filter1.equals("Select Subject")||filter2.equals("Select Class"))) {
                    //pust post object to database
                    Post post = new Post(1, "a", calculateTime(), bookName.getText().toString(), Main2Activity.mUserId, Main2Activity.mUser, filter1, filter2, true);
//                posts.add(post);
                    BooksFragment.mPostDatabaseReference.push().setValue(post);
                    bookName.setText("");
                    filter1 = "";
                    filter2 = "";
                    Toast.makeText(BookPostActivity.this, "Book added successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(BookPostActivity.this, Main2Activity.class));
                }else{
                    Toast.makeText(BookPostActivity.this,"Please fill all the fields",Toast.LENGTH_SHORT).show();
                }
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (null == imageReturnedIntent) return;
        Uri originalUri = null;
        switch(requestCode) {
            case CHOOSE_CAMERA_RESULT:
                if(resultCode == RESULT_OK){
                    if(file.exists()){
                        Toast.makeText(this,"The image was saved at "+file.getAbsolutePath(),Toast.LENGTH_LONG).show();;
                    }
                    //Uri is at variable tempuri which can be converted to string

                    // code for inserting in database
                }

                break;
            case GALLERY_RESULT:
                if(resultCode == RESULT_OK){
                    originalUri = imageReturnedIntent.getData();
                    final int takeFlags = imageReturnedIntent.getFlags()
                            & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getContentResolver().takePersistableUriPermission(originalUri, takeFlags);
                    // code for inserting in database
                    //Uri is originalUri which can be converted to string
                }
                break;
        }

    }

    public String calculateTime() {
        return android.text.format.DateFormat.format("MMM dd, yyyy hh:mm:ss aaa", new java.util.Date()).toString();

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
        Spinner spin = (Spinner)parent;
        Spinner spin2 = (Spinner)parent;
        if(spin.getId() == R.id.s1)
        {
            TextView selectedTextView = (TextView) view;
            filter1 = selectedTextView.getText().toString();
        }
        if(spin2.getId() == R.id.s2)
        {
            TextView selectedTextView = (TextView) view;
            filter2 = selectedTextView.getText().toString();
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}