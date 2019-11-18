package com.example.lee.bababa;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.Objects;

public class DriverSettingActivity extends AppCompatActivity {
    DatabaseReference database;
    private driverDTO driverDTO;

    FirebaseAuth auth;

    private HttpClient http;
    String url = "http://52.79.223.4:8081/MainController";
    private ArrayList<NameValuePair> nameValuePairs;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_setting);

        driverDTO = new driverDTO();
        database = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        final EditText driverName = findViewById(R.id.drivername);
        final EditText busNumber = findViewById(R.id.busnumber);
        final EditText busCarNumber = findViewById(R.id.buscarnumber);
        Button setting = findViewById(R.id.settingbutton);

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driverDTO.driverId = Objects.requireNonNull(auth.getCurrentUser()).getEmail();
                driverDTO.driverName = driverName.getText().toString();
                driverDTO.busNumber = busNumber.getText().toString();
                driverDTO.busCarNumber = busCarNumber.getText().toString();

                writeNewUser(driverDTO.driverId, driverDTO.driverName, driverDTO.busNumber, driverDTO.busCarNumber);

                try {
                    nameValuePairs = new ArrayList<>();
                    nameValuePairs.add(new BasicNameValuePair("action", "driversave"));
                    nameValuePairs.add(new BasicNameValuePair("drivername", driverDTO.driverName));
                    nameValuePairs.add(new BasicNameValuePair("busnumber", driverDTO.busNumber));
                    nameValuePairs.add(new BasicNameValuePair("buscarnumber", driverDTO.busCarNumber));

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
                    noHTMLString = noHTMLString.replaceAll("<(.*?)\\>", "");//Removes all items in brackets
                    noHTMLString = noHTMLString.replaceAll("<(.*?)\\\n", "");//Must be undeneath
                    noHTMLString = noHTMLString.replaceFirst("(.*?)\\>", "");//noHTMLString = noHTMLString.replaceAll("&nbsp;","");
                    noHTMLString = noHTMLString.replaceAll("&amp;", "");
                    noHTMLString = noHTMLString.replaceAll("<[^>]*>", "");//noHTMLString = noHTMLString.replaceAll("", "");
                    noHTMLString = noHTMLString.replaceAll("\\p{Z}", "");//noHTMLString.trim();
                    //html = EntityUtils.toString(resEntity).replaceAll(regex2,"");

                    //서버로부터 true라는 대답이 돌아왔다는 것은 정상적으로 유저 정보가 저장이 완료되었다는 뜻임
                    if (noHTMLString.equals("true")) {
                        Log.d("확인", noHTMLString);
                        Toast.makeText(DriverSettingActivity.this, "DB로 저장 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    } else if (noHTMLString.equals("false")) {
                        Toast.makeText(DriverSettingActivity.this, "DB로의 저장이 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finish();
            }
        });
    }

    //뒤로가기 터치가 작동하지 않도록 눌렸을 경우의 액션을 비워둔다.
    @Override
    public void onBackPressed() {
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void writeNewUser(String driverId, String driverName, String busNumber, String busCarNumber) {
        driverDTO driverDTO = new driverDTO(driverId, driverName, busNumber, busCarNumber);

        database.child("busdriverDB").child(Objects.requireNonNull(auth.getCurrentUser()).getUid()).child("basicInformation").setValue(driverDTO);
    }
}
