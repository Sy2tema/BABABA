package com.example.lee.bababa;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class DriverHomeActivity extends AppCompatActivity {
    private HashMap<String, Objects> userDB = new HashMap<>();
    private HashMap<String, Objects> driverDTO = new HashMap<>();
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private final String[] give = new String[1];

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        TextView changesetting = findViewById(R.id.changesetting);
        TextView logout = findViewById(R.id.logout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                LoginManager.getInstance().logOut(); //Facebook을 이용하여 로그인을 했을 경우 로그아웃 시 호출
                changeIntent(R.id.logout);
            }
        });

        changesetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeIntent(R.id.changesetting);
            }
        });

        String url = "http://52.79.223.4:8081/MainController";
        HttpClient http = new DefaultHttpClient();
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();

        final TextView name = findViewById(R.id.driverName);

        name.setText(String.valueOf(Objects.requireNonNull(auth.getCurrentUser()).getDisplayName()));

        database.getReference().child("userDB").child(Objects.requireNonNull(auth.getCurrentUser()).getUid())
                .child("basicInformation").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userDB.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    userDB = (HashMap<String, Objects>) dataSnapshot.getValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        try {
            nameValuePairs.add(new BasicNameValuePair("action", "lookup"));
            nameValuePairs.add(new BasicNameValuePair("userid", give[0]));

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

            //noHTMLString이 null값이 아니라는 것은 정상적으로 정보가 들어왔다는 의미
            if (noHTMLString != null) {
                Log.d("확인 noHTMLString", noHTMLString);
                Toast.makeText(DriverHomeActivity.this, "정보 조회가 성공하였습니다.", Toast.LENGTH_SHORT).show();

                //들어온 정보 나누어 출력
                //받아서 2차원배열에 저장하는 구간
                String[] result_1;
                result_1 = noHTMLString.split("@");
                String[][] result = new String[result_1.length][5];
                for (int i = 0; i < result_1.length; i++)
                    result[i] = result_1[i].split(",");
                Log.d("확인", String.valueOf(result));
                //구간 끝 result에 저장되어있음.

                makeTable(result); //테이블 생성
            } else {Toast.makeText(DriverHomeActivity.this, "정보 조회에 실패했습니다", Toast.LENGTH_SHORT).show();}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //시작 시 Firebase DB에서 정보를 받아와 입력된 정보가 없을 경우 SettingActivity로 이동하게 하는 메소드
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onStart() {
        super.onStart();

        final String[] check = new String[1];

        database.getReference().child("busdriverDB").child(Objects.requireNonNull(auth.getCurrentUser()).getUid())
                .child("basicInformation").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                driverDTO.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    driverDTO = (HashMap<String, Objects>) dataSnapshot.getValue();
                }

                check[0] = String.valueOf(driverDTO.get("driverName"));
                give[0] = String.valueOf(driverDTO.get("driverName"));
                Log.d("아이디 확인", auth.getCurrentUser().getEmail());
                Log.d("check 확인", check[0]);

                if (check[0].equals("null")) {
                } else {
                    changeIntent(R.id.changesetting);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void makeTable(String[][] result) {
        //테이블 생성
        TableRow.LayoutParams wrapWrapTableRowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        int scrollableColumnWidths =20;
        int fixedRowHeight = 80;

        TableRow row = new TableRow(this);
        row.setLayoutParams(wrapWrapTableRowParams);

        TableLayout scrollablePart = findViewById(R.id.scrollable_part);

        row = new TableRow(this);
        row.setLayoutParams(wrapWrapTableRowParams);
        row.setGravity(Gravity.CENTER);

        for (int i = 0; i < result.length; i++) {
            row.addView(makeTableRowWithText(String.valueOf(result[i]), scrollableColumnWidths, fixedRowHeight));

            Log.d("확인", String.valueOf(row));
            scrollablePart.addView(row);
        }
    }

    public TextView makeTableRowWithText(String text, int widthInPercentOfScreenWidth, int fixedHeightInPixels) {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        TextView recyclableTextView = new TextView(this);
        recyclableTextView.setText(text);
        recyclableTextView.setTextColor(Color.BLACK);
        recyclableTextView.setTextSize(20);
        recyclableTextView.setWidth(widthInPercentOfScreenWidth * screenWidth / 100);
        recyclableTextView.setHeight(fixedHeightInPixels);
        return recyclableTextView;
    }

    public void changeIntent(int id) {
        Intent intent;
        if (id == R.id.logout) {
            intent = new Intent(DriverHomeActivity.this, DriverIndexActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.changesetting) {
            intent = new Intent(DriverHomeActivity.this, DriverSettingActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public  void onBackPressed() {

    }
}
