package com.app.pharmacy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.app.pharmacy.common.entity.Pharmacy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import lombok.NonNull;

import com.google.firebase.database.core.Constants;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class AddPharmacy extends AppCompatActivity {

    private View topAppBar;

    private TextInputEditText name_field, mobile_field, email_field, location_field, des_field;

    private Button save_button;

    private ImageButton imageButton;

    private ImageView imageView;

    private FirebaseFirestore db;

    private String image;

    private StorageReference storageReference;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    /**
     * @{inherit}
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //  getSupportActionBar().hide();
        setContentView(R.layout.activity_add_pharmacy);
        db = FirebaseFirestore.getInstance();
        topAppBar = (View) findViewById(R.id.topAppBar);
        name_field = findViewById(R.id.name_field);
        mobile_field = findViewById(R.id.mobile_field);
        email_field = findViewById(R.id.email_field);
        location_field = findViewById(R.id.location_field);
        des_field = findViewById(R.id.des_field);
        save_button = findViewById(R.id.save_button);
        imageButton = (ImageButton) findViewById(R.id.imageButton);
        if (ContextCompat.checkSelfPermission(AddPharmacy.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddPharmacy.this, new String[]{
                    Manifest.permission.CAMERA
            }, 100);
        }

        imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 100);
            }
        });

        topAppBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Owner.class));
            }
        });

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = name_field.getText().toString();
                String phone = mobile_field.getText().toString();
                String email = email_field.getText().toString();
                String location = location_field.getText().toString();
                String des = des_field.getText().toString();
                String image = "";
                if (getImage() != null) {
                    image = getImage();
                }

                if (TextUtils.isEmpty(name) && TextUtils.isEmpty(phone) && TextUtils.isEmpty(email)) {

                    // 2. Warning message
                    TastyToast.makeText(
                            getApplicationContext(),
                            "required filed !",
                            TastyToast.LENGTH_LONG,
                            TastyToast.WARNING
                    );
                } else {

                    addDataToFirestore(name, phone, email, location, des, image);
                }
            }
        });
    }

    /**
     * for  event camera activity result.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            // imageView.setImageBitmap(bitmap);
            setImage(encodeBitmapAndSaveToFirebase(bitmap));
            if (getImage() != null) {
                imageButton.setImageIcon(Icon.createWithBitmap(decodeBitmapAndSaveToFirebase(getImage())));
            }
        }
    }

    /**
     * decode byte image.
     *
     * @param image
     * @return
     */
    public Bitmap decodeBitmapAndSaveToFirebase(String image) {
        //decode base64 string to image
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] imageBytes = baos.toByteArray();
        imageBytes = Base64.decode(image, Base64.DEFAULT);
        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        return decodedImage;
    }

    /**
     *  encode image to base64
     *
     * @param bitmap
     * @return
     */
    public String encodeBitmapAndSaveToFirebase(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        return imageEncoded;
    }

    /**
     *  add to pharmacy details db
     *
     * @param name
     * @param phone
     * @param email
     * @param location
     * @param des
     * @param image
     */
    private void addDataToFirestore(String name, String phone, String email, String location, String des, String image) {

        // creating a collection reference
        // for our Firebase Firetore database.
        CollectionReference dbCourses = db.collection("pharmacy");
        // adding our data to our courses object class.
        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setName(name);
        pharmacy.setMobile(phone);
        pharmacy.setEmail(email);
        pharmacy.setLocation(location);
        pharmacy.setDescription(des);
        pharmacy.setImage(image);
        pharmacy.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        // below method is use to add data to Firebase Firestore.
        dbCourses.add(pharmacy).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                // after the data addition is successful
                // we are displaying a success toast message.
                // 1. Success message
                TastyToast.makeText(
                        getApplicationContext(),
                        "Your Pharmacy has been Successfully added!",
                        TastyToast.LENGTH_LONG,
                        TastyToast.SUCCESS
                );
                startActivity(new Intent(getApplicationContext(), Owner.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // this method is called when the data addition process is failed.
                // displaying a toast message when data addition is failed.
                TastyToast.makeText(
                        getApplicationContext(),
                        "Fail to add Pharmacy" + e,
                        TastyToast.LENGTH_LONG,
                        TastyToast.ERROR
                );
            }
        });
    }

}


