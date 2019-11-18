package com.example.lee.bababa;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Objects;

//HomeActivity에서 DrawerNaviagtion를 이용하여 정보 수정부분을 클릭하게 될 경우 나타나는 Activity
//또한 IndexActivity를 통해 로그인을 할 경우 나타나는 Activity
public class SettingActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {
    DatabaseReference database;
    private userDB userDTO;
    TextView birthday;
    TextView userAge;
    GregorianCalendar calendar;
    RadioGroup gender;
    RadioButton gMan, gWomen;
    String checkGender = "남자";
    FirebaseAuth auth;

    String url = "http://52.79.223.4:8081/MainController";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        gender = findViewById(R.id.genderGroup);
        birthday = findViewById(R.id.birthday);
        userAge = findViewById(R.id.user);
        gMan = findViewById(R.id.genderMan);
        gWomen = findViewById(R.id.genderWoman);

        //유저 DB와 Firebase DB를 불러옴
        userDTO = new userDB();
        database = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        calendar();
        gender.setOnCheckedChangeListener(this);

        final Button settingButton = findViewById(R.id.settingButton);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                String url = "http://52.79.223.4:8081/MainController";
                HttpClient http = new DefaultHttpClient();

                userDTO.gender = checkGender;
                userDTO.userAge = userAge.getText().
                        toString();
                String serverAge = userDTO.userAge;

                userDTO.userBirthday = birthday.getText().
                        toString();

                userDTO.userId = Objects.requireNonNull(auth.getCurrentUser()).
                        getEmail();
                String serverId = userDTO.userId;

                userDTO.userName = Objects.requireNonNull(auth.getCurrentUser()).
                        getDisplayName();

                //Firebase DB저장 부분
                writeNewUser(userDTO.userId, userDTO.userName, userDTO.gender, userDTO.userAge, userDTO.userBirthday);

                //서버로 POST하는 부분
                //String regex1 = "\\<.*?\\>";
                //String regex2 = "<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>";
                try {
                    ArrayList<NameValuePair> nameValuePairs =
                            new ArrayList<>();
                    nameValuePairs.add(new BasicNameValuePair("action", "usersave"));
                    nameValuePairs.add(new BasicNameValuePair("userid", serverId));
                    nameValuePairs.add(new BasicNameValuePair("age", serverAge));

                    HttpParams params = http.getParams();
                    HttpConnectionParams.setConnectionTimeout(params, 5000);
                    HttpConnectionParams.setSoTimeout(params, 5000);

                    HttpPost httpPost = new HttpPost(url);
                    UrlEncodedFormEntity entityRequest =
                            new UrlEncodedFormEntity(nameValuePairs, "EUC-KR");

                    httpPost.setEntity(entityRequest);

                    HttpResponse responsePost = http.execute(httpPost);
                    HttpEntity resEntity = responsePost.getEntity();

                    String noHTMLString = EntityUtils.toString(resEntity);
                    noHTMLString = noHTMLString.replaceAll("\\<.*?\\>", "");// Remove Carriage return from java String
                    noHTMLString = noHTMLString.replaceAll("\r", "");
                    noHTMLString = noHTMLString.replaceAll("<([bip])>.*?</\1>", "");// Remove New line from java string and replace html break
                    noHTMLString = noHTMLString.replaceAll("\n", "");
                    noHTMLString = noHTMLString.replaceAll("\"", "");
                    noHTMLString = noHTMLString.replaceAll("<(.*?)\\>","");//Removes all items in brackets
                    noHTMLString = noHTMLString.replaceAll("<(.*?)\\\n","");//Must be undeneath
                    noHTMLString = noHTMLString.replaceFirst("(.*?)\\>", "");//noHTMLString = noHTMLString.replaceAll("&nbsp;","");
                    noHTMLString = noHTMLString.replaceAll("&amp;","");
                    noHTMLString = noHTMLString.replaceAll("<[^>]*>", "");//noHTMLString = noHTMLString.replaceAll("", "");
                    noHTMLString = noHTMLString.replaceAll("\\p{Z}", "");//noHTMLString.trim();
                    //html = EntityUtils.toString(resEntity).replaceAll(regex2,"");

                    //서버로부터 true라는 대답이 돌아왔다는 것은 정상적으로 유저 정보가 저장이 완료되었다는 뜻임
                    if (noHTMLString.equals("true")) {
                        Log.d("확인", noHTMLString);
                        Toast.makeText(SettingActivity.this, "DB로 저장 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    } else if (noHTMLString.equals("false")) {
                        Toast.makeText(SettingActivity.this, "DB로의 저장이 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                }catch(Exception e){e.printStackTrace();}

                changeIntent(settingButton);
            }
        });
    }

    //뒤로가기 터치가 작동하지 않도록 눌렸을 경우의 액션을 비워둔다.
    @Override
    public void onBackPressed() {
    }

    //입력된 정보를 DB에 업로드하고 Activity 종료
    public void changeIntent(View v) {
        int id = v.getId();
        Intent intent;
        if (id == R.id.settingButton) {
            finish();
        } else if (id == R.id.birthday) {
            intent = new Intent(getApplicationContext(), SetBirthdayActivity.class);
            startActivityForResult(intent, 1001);
        }
    }


    private void calendar() {
        calendar = new GregorianCalendar();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        birthday.setText(year + "년" + (month + 1) + "월" + day + "일");

        birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeIntent(birthday);
            }
        });
    }


    //생일을 설정하면 xml에 바로바로 업데이트 시켜준다.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String birthdayYear = data.getStringExtra("yearOk");
        String birthdayMonth = data.getStringExtra("monthOk");
        String birthdayDay = data.getStringExtra("dayOk");
        String age = data.getStringExtra("ageOk");


        birthday.setText(String.format("%s년%s월%s일", birthdayYear, birthdayMonth, birthdayDay));
        userAge.setText(age);
    }

    //성별 설정 Listener
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.genderMan:
                checkGender = "남자";
                break;
            case R.id.genderWoman:
                checkGender = "여자";
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void writeNewUser(String userId, String userName, String gender, String userAge, String userBirthday) {
        userDB userDB = new userDB(userId, userName, gender, userAge, userBirthday);

        //auth.getUid()부분을 이용하여 각 어플 사용자들의 id별로 정보를 나눠서 저장할 수 있도록 DB설정을 하게 된다.
        database.child("userDB").child(Objects.requireNonNull(auth.getCurrentUser()).getUid()).child("basicInformation").setValue(userDB);
    }

}