package com.example.lee.bababa;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SetDateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_date);

        final EditText year = findViewById(R.id.year123);
        final EditText month = findViewById(R.id.month123);
        final EditText day = findViewById(R.id.day123);
        Button setButton = findViewById(R.id.setDay);

        //설정 버튼을 누르면 각 값들을 변수에 담아 putExtra에 넣어 setResult로 보낸다.
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String yearOk = year.getText().toString();
                String monthOk = month.getText().toString();
                String dayOk = day.getText().toString();

                Intent intent = new Intent();
                intent.putExtra("yearOk", yearOk);
                intent.putExtra("monthOk", monthOk);
                intent.putExtra("dayOk", dayOk);
                setResult(0, intent);
                finish();
            }
        });
    }
}
