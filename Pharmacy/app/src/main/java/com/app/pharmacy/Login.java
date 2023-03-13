package com.app.pharmacy;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sdsmdg.tastytoast.TastyToast;

public class Login extends AppCompatActivity {
    private Button sign_in_button, sign_up_button;
    private boolean valid = true;
    TextInputEditText password_field, username_field;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        sign_in_button = findViewById(R.id.sign_in_button);
        sign_up_button = findViewById(R.id.sign_up_button);
        username_field = findViewById(R.id.username_field);
        password_field = findViewById(R.id.password_field);
        sign_up_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Signup.class));

            }
        });

        sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkField(username_field);
                checkField(password_field);
                if (valid) {
                    String email = username_field.getText().toString();
                    String pass = password_field.getText().toString();

                    auth.signInWithEmailAndPassword(email, pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            TastyToast.makeText(
                                    getApplicationContext(),
                                    "Successfully login!",
                                    TastyToast.LENGTH_SHORT,
                                    TastyToast.SUCCESS

                            );
                            checkUserAccessLevel(authResult.getUser().getUid());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            TastyToast.makeText(
                                    getApplicationContext(),
                                    "Login Faller!",
                                    TastyToast.LENGTH_LONG,
                                    TastyToast.ERROR

                            );
                        }
                    });
                } else {
                    // 2. Warning message
                    TastyToast.makeText(
                            getApplicationContext(),
                            "required filed ",
                            TastyToast.LENGTH_LONG,
                            TastyToast.WARNING
                    );
                }
            }
        });

    }

    public boolean checkField(TextInputEditText textField) {
        if (textField.getText().toString().isEmpty()) {
            textField.setError("required filed ");
            valid = false;
        } else {
            valid = true;
        }

        return valid;
    }

    public void checkUserAccessLevel(String uid) {
        DocumentReference documentReference = db.collection("Users").document(uid);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d(TAG, "onSuccess: " + documentSnapshot.getBoolean("isPhar"));
                Log.d(TAG, "onSuccess isCus: " + documentSnapshot.getBoolean("isCus"));
                if (documentSnapshot.getBoolean("isPhar") == true) {

                    startActivity(new Intent(getApplicationContext(), Owner.class));
                    finish();
                }
                if (documentSnapshot.getBoolean("isCus") == true) {
                    startActivity(new Intent(getApplicationContext(), Customer.class));
                    finish();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.getBoolean("isPhar") == true) {

                        startActivity(new Intent(getApplicationContext(), Owner.class));
                        finish();
                    }
                    if (documentSnapshot.getBoolean("isCus") == true) {
                        startActivity(new Intent(getApplicationContext(), Customer.class));
                        finish();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
            });
        }
    }
}