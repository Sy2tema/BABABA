package com.example.lee.bababa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Calendar;
import java.util.GregorianCalendar;

//SettingActivity에서 날짜를 누르게 될 경우 팝업창 형식으로 나타나게 되는 Activity
public class SetBirthdayActivity extends Activity {
    GregorianCalendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_birthday);

        calendar = new GregorianCalendar();
        final EditText year = findViewById(R.id.year);
        final EditText month = findViewById(R.id.month);
        final EditText day = findViewById(R.id.day);
        Button setButton = findViewById(R.id.btnSet);

        //설정 버튼을 누르면 각 값들을 변수에 담아 putExtra에 넣어 setResult로 보낸다.
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String yearOk = year.getText().toString();
                String monthOk = month.getText().toString();
                String dayOk = day.getText().toString();

                //나이 계산
                int currentYear = calendar.get(Calendar.YEAR);
                int birth = Integer.parseInt(yearOk);
                int result = currentYear - birth;
                String ageOk = String.valueOf(result);

                Intent intent = new Intent();
                intent.putExtra("yearOk", yearOk);
                intent.putExtra("monthOk", monthOk);
                intent.putExtra("dayOk", dayOk);
                intent.putExtra("ageOk", ageOk);
                setResult(0, intent);
                finish();
            }
        });
    }
}
