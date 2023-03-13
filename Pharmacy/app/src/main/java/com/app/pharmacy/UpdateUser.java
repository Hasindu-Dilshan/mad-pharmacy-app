package com.app.pharmacy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;

import com.app.pharmacy.common.entity.Pharmacy;
import com.app.pharmacy.common.entity.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class UpdateUser extends AppCompatActivity {
    private View topAppBar;

    private TextInputEditText name_field, mobile_field, email_field, password_field;

    private CheckBox phar;

    private Button update_button;

    private ImageButton imageButton;

    private String image;

    private FirebaseFirestore db;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);

        db = FirebaseFirestore.getInstance();
        topAppBar = (View) findViewById(R.id.topAppBar);
        name_field = findViewById(R.id.name_field);
        mobile_field = findViewById(R.id.mobile_field);
        phar = findViewById(R.id.phar);
        password_field = findViewById(R.id.password_field);
        email_field = findViewById(R.id.email_field);
        update_button = findViewById(R.id.update_button);
        imageButton = (ImageButton) findViewById(R.id.imageButton);
        if (ContextCompat.checkSelfPermission(UpdateUser.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UpdateUser.this, new String[]{
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

        getUserDetail();


        topAppBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Owner.class));
            }
        });
        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String name = name_field.getText().toString();
                String phone = mobile_field.getText().toString();
                String email = email_field.getText().toString();
                String pass = password_field.getText().toString();
                Boolean pharm = phar.isChecked();
                String image = getImage();


                if (TextUtils.isEmpty(name) && TextUtils.isEmpty(phone) && TextUtils.isEmpty(email) && TextUtils.isEmpty(pass)) {

                    // 2. Warning message
                    TastyToast.makeText(
                            getApplicationContext(),
                            "required filed !",
                            TastyToast.LENGTH_LONG,
                            TastyToast.WARNING
                    );
                } else {

                    update(name, phone, email, pass, pharm, image);
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
     * encode image to base64
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
     * update user
     *
     * @param name
     * @param phone
     * @param email
     * @param pass
     * @param ph
     * @param image
     */
    private void update(String name, String phone, String email, String pass, Boolean ph, String image) {
        User users = new User();
        users.setName(name);
        users.setMobile(Integer.parseInt(phone));
        users.setEmail(email);
        users.setPassword(pass);
        users.setPhar(ph);
        users.setImage(image);
        db.collection("Users").document(FirebaseAuth.getInstance().getUid()).set(users).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // on successful completion of this process
                // we are displaying the toast message.
                TastyToast.makeText(
                        getApplicationContext(),
                        "Successfully update!",
                        TastyToast.LENGTH_LONG,
                        TastyToast.SUCCESS

                );
                startActivity(new Intent(getApplicationContext(), Owner.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            // inside on failure method we are
            // displaying a failure message.
            @Override
            public void onFailure(Exception e) {
                TastyToast.makeText(
                        getApplicationContext(),
                        "Fail to update" + e,
                        TastyToast.LENGTH_LONG,
                        TastyToast.ERROR
                );
            }
        });
    }

    private void getUserDetail() {
        String userId = (FirebaseAuth.getInstance().getCurrentUser().getUid());
        DocumentReference documentReference = db.collection("Users").document(userId);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @SuppressLint("ResourceType")
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                name_field.setText(documentSnapshot.getString("name"));
                mobile_field.setText(documentSnapshot.get("mobile").toString());
                email_field.setText(documentSnapshot.getString("email"));
                password_field.setText(documentSnapshot.getString("password"));
                phar.setChecked(documentSnapshot.getBoolean("isPhar"));
                if (documentSnapshot.getString("image") != null) {
                    imageButton.setImageIcon(Icon.createWithBitmap(decodeBitmapAndSaveToFirebase(documentSnapshot.getString("image"))));
                    setImage(documentSnapshot.getString("image"));
                }

            }

        });
    }
}