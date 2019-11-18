package com.example.lee.bababa;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

//HomeActivity에서 내 정보를 터치했을 때 나타나는 Activity
public class ViewInformationActivity extends AppCompatActivity {
    private HashMap<String, Objects> fbDB = new HashMap<>();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_information);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        final TextView name = findViewById(R.id.nametext);
        final TextView userid = findViewById(R.id.idtext);
        final TextView age = findViewById(R.id.agetext);
        final TextView gender = findViewById(R.id.gendertext);
        final TextView birth = findViewById(R.id.birthtext);

        database.getReference().child("userDB").child(Objects.requireNonNull(auth.getCurrentUser()).getUid())
                .child("basicInformation").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                fbDB.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    fbDB = (HashMap<String, Objects>) dataSnapshot.getValue();
                }

                gender.setText(String.valueOf(fbDB.get("gender")));
                name.setText(String.valueOf(fbDB.get("userName")));
                userid.setText(String.valueOf(fbDB.get("userId")));
                age.setText(String.valueOf(fbDB.get("userAge")));
                birth.setText(String.valueOf(fbDB.get("userBirthday")));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
