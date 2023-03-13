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
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import com.app.pharmacy.common.entity.Pharmacy;
import com.app.pharmacy.common.entity.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.ByteArrayOutputStream;

import lombok.NonNull;

public class Signup extends AppCompatActivity {
    private View topAppBar;

    private TextInputEditText name_field, mobile_field, email_field, password_field;

    private CheckBox phar;

    private Boolean cus = true;

    private Button save_button;

    boolean valid = true;

    private FirebaseAuth auth;

    private ImageButton imageButton;

    private FirebaseFirestore db;

    private String image;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Boolean getCus() {
        return cus;
    }

    public void setCus(Boolean cus) {
        this.cus = cus;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getSupportActionBar().hide();
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        topAppBar = (View) findViewById(R.id.topAppBar);
        name_field = findViewById(R.id.name_field);
        mobile_field = findViewById(R.id.mobile_field);
        email_field = findViewById(R.id.email_field);
        password_field = findViewById(R.id.password_field);
        phar = findViewById(R.id.phar);
        save_button = findViewById(R.id.save_button);

        imageButton = (ImageButton) findViewById(R.id.imageButton);
        if (ContextCompat.checkSelfPermission(Signup.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Signup.this, new String[]{
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
                startActivity(new Intent(getApplicationContext(), Login.class));
                // startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        phar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    setCus(false);
                }
            }
        });
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = name_field.getText().toString();
                String phone = mobile_field.getText().toString();
                String email = email_field.getText().toString();
                String pass = password_field.getText().toString();
                String images = "";
                if (getImage() != null) {
                    image = getImage();
                }
                boolean isCus = getCus();
                boolean isPhar = phar.isChecked();
                checkField(name_field);
                checkField(mobile_field);
                checkField(email_field);
                checkField(password_field);


                if (valid) {
                    auth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            FirebaseUser user = auth.getCurrentUser();
                            TastyToast.makeText(
                                    getApplicationContext(),
                                    "Your Pharmacy has been Successfully added!",
                                    TastyToast.LENGTH_SHORT,
                                    TastyToast.SUCCESS

                            );
                            addDataToFirestore(name, phone, email, pass, isCus, isPhar, images, user);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            TastyToast.makeText(
                                    getApplicationContext(),
                                    "User  has been Create Not Successfully ",
                                    TastyToast.LENGTH_SHORT,
                                    TastyToast.ERROR

                            );

                        }
                    });


                } else {

                    // 2. Warning message
                    TastyToast.makeText(
                            getApplicationContext(),
                            "required filed !",
                            TastyToast.LENGTH_LONG,
                            TastyToast.WARNING
                    );
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

    public boolean checkField(TextInputEditText textField) {
        if (textField.getText().toString().isEmpty()) {
            textField.setError("required filed");
            valid = false;
        } else {
            valid = true;
        }

        return valid;
    }

    private void addDataToFirestore(String name, String phone, String email, String pass, Boolean isCus, Boolean isPhar, String image, FirebaseUser users) {

        // creating a collection reference
        // for our Firebase Firetore database.
        DocumentReference dbRef = db.collection("Users").document(users.getUid());

        // adding our data to our courses object class.
        User user = new User();
        user.setName(name);
        user.setMobile(Integer.parseInt(phone));
        user.setEmail(email);
        user.setPassword(pass);
        user.setCus(isCus);
        user.setPhar(isPhar);
        user.setImage(getImage());
        // below method is use to add data to Firebase Firestore.
        dbRef.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                startActivity(new Intent(getApplicationContext(), Login.class));
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

}