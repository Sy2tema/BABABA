package com.example.lee.bababa;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;


//HomeActivity에서 정보 조회를 눌렀을 때 나타나는 Activity
public class TotalInformationActivity extends AppCompatActivity {
    private userDB userDTO;
    TextView searchDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_information);

        userDTO = new userDB();
        searchDay = findViewById(R.id.searchDay);

        searchDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeIntent(searchDay);
            }
        });

        Button search = findViewById(R.id.search);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://52.79.223.4:8081/MainController";
                HttpClient http = new DefaultHttpClient();
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();

                try {
                    nameValuePairs.add(new BasicNameValuePair("action", "lookup"));
                    nameValuePairs.add(new BasicNameValuePair("userid", "ohhanjin0330")); //원래는 userDTO.userId넣으면 됨
                    nameValuePairs.add(new BasicNameValuePair("date", String.valueOf(searchDay)));

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
                        Toast.makeText(TotalInformationActivity.this, "정보 조회가 성공하였습니다.", Toast.LENGTH_SHORT).show();

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
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //TableRow tableRow = new TableRow.LayoutParams(layoutFull, layoutMatch);
        //tableRow.setId(5);
        //tableRow.setLayoutParams(new LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TabLayout.LayoutParams.WRAP_CONTENT));

    }

    public TextView makeTableRowWithText(String text, int widthInPercentOfScreenWidth, int fixedHeightInPixels) {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        TextView recyclableTextView = new TextView(this);
        recyclableTextView.setText(text);
        recyclableTextView.setTextColor(Color.BLACK);
        recyclableTextView.setTextSize(15);
        recyclableTextView.setWidth(widthInPercentOfScreenWidth * screenWidth / 100);
        recyclableTextView.setHeight(fixedHeightInPixels);
        return recyclableTextView;
    }

    public void changeIntent(View v) {
        int id = v.getId();
        Intent intent;
        if (id == R.id.searchDay) {
            intent = new Intent(getApplicationContext(), SetDateActivity.class);
            startActivityForResult(intent, 1001);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String birthdayYear = data.getStringExtra("yearOk");
        String birthdayMonth = data.getStringExtra("monthOk");
        String birthdayDay = data.getStringExtra("dayOk");

        searchDay.setText(String.format("%s-%s-%s", birthdayYear, birthdayMonth, birthdayDay));
    }

    private void makeTable(String[][] result) {
        //테이블 생성
        TableRow.LayoutParams wrapWrapTableRowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        int[] scrollableColumnWidths = new int[]{23, 18, 23, 21, 10};
        int fixedRowHeight = 80;

        TableRow row = new TableRow(this);
        row.setLayoutParams(wrapWrapTableRowParams);

        TableLayout scrollablePart = findViewById(R.id.scrollable_part);

        row = new TableRow(this);
        row.setLayoutParams(wrapWrapTableRowParams);
        row.setGravity(Gravity.CENTER);

        for (int i = 0; i < result.length; i++) {
            row.addView(makeTableRowWithText(String.valueOf(result[i][0]), scrollableColumnWidths[0], fixedRowHeight));
            row.addView(makeTableRowWithText(String.valueOf(result[i][1]), scrollableColumnWidths[1], fixedRowHeight));
            row.addView(makeTableRowWithText(String.valueOf(result[i][2]), scrollableColumnWidths[2], fixedRowHeight));
            row.addView(makeTableRowWithText(String.valueOf(result[i][3]), scrollableColumnWidths[3], fixedRowHeight));
            row.addView(makeTableRowWithText(String.valueOf(result[i][4]), scrollableColumnWidths[4], fixedRowHeight));

            Log.d("확인", String.valueOf(row));
            scrollablePart.addView(row);
        }
    }
}