package com.app.pharmacy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.app.pharmacy.common.PharmacyAdapter;
import com.app.pharmacy.common.entity.Pharmacy;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AddStock extends AppCompatActivity {
    private View topAppBar;
    private FloatingActionButton float_button;
    private RecyclerView view;
    private ArrayList<Pharmacy> pharmacyArrayList;
    private PharmacyAdapter adapter;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  getSupportActionBar().hide();
        setContentView(R.layout.activity_add_stock);
        view = findViewById(R.id.rcView);
        view.setHasFixedSize(true);
        view.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        pharmacyArrayList = new ArrayList<Pharmacy>();
        adapter = new PharmacyAdapter(AddStock.this, pharmacyArrayList);

        topAppBar = findViewById(R.id.topAppBar);
        float_button = findViewById(R.id.float_button);

        float_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AddPharmacy.class));
            }
        });
        topAppBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Owner.class));
                finish();
            }
        });
        view.setAdapter(adapter);
    }
}