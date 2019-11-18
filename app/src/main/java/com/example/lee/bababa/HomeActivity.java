package com.example.lee.bababa;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.minew.beacon.BeaconValueIndex;
import com.minew.beacon.BluetoothState;
import com.minew.beacon.MinewBeacon;
import com.minew.beacon.MinewBeaconManager;
import com.minew.beacon.MinewBeaconManagerListener;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

//IndexActivity에서 로그인을 성공하거나 이미 로그인을 완료한 상태에서 앱을 실행할 경우 도달하는 Activity
public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth auth;
    private DatabaseReference database;
    private userDB userDTO;
    private ActionBarDrawerToggle toggle;

    private Button ok;
    private String[] peoples = {"1"};
    private EditText text;
    private ArrayList<NameValuePair> nameValuePairs;
    private String url;
    private HttpClient http;
    private String busdriverId = "oh han jin";
    private String busNumber = "720-2";
    private HashMap<String, Objects> fbDB = new HashMap<>();

    //비콘파트 선언
    private BeaconListAdapter mAdapter;

    private position Position1 = new position(450, 900);
    private position Position2 = new position(0, 0);
    private position Position3 = new position(900, 0);
    private position Position4 = new position(0, 0);

    //private KalmanFilter mKalmanAccX = new KalmanFilter(0.0f);
    //private KalmanFilter mKalmanAccY  = new KalmanFilter(0.0f);

    private KalmanFilter mKalmanAcc1 = new KalmanFilter(0.0f);
    private KalmanFilter mKalmanAcc2 = new KalmanFilter(0.0f);
    private KalmanFilter mKalmanAcc3 = new KalmanFilter(0.0f);

    private KalmanFilter mKalmanAcc11 = new KalmanFilter(0.0f);
    private KalmanFilter mKalmanAcc22 = new KalmanFilter(0.0f);
    private KalmanFilter mKalmanAcc33 = new KalmanFilter(0.0f);

    private MinewBeaconManager mMinewBeaconManager;
    private static final int REQUEST_ENABLE_BT = 2;
    private boolean isScanning;

    UserRssi comp = new UserRssi();
    private boolean mIsRefreshing;
    private int state;

    private double a = Position3.x;
    private double b = Position1.y;

    public boolean Case(double x, double y) {
        if (y - 2 * b / a * x < 0 && y + 2 * b / a * x - 2 * b < 0 && y > 0)
            return true;
        else
            return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //ActionBar의 기본으로 설정되었던 이름인 HomeActivity를 변경하기 위한 부분
        //NullPointerException이 발생하여 manifests의 label을 변경하는 것으로 방향을 틀었다
        //android.support.v7.app.ActionBar ab = getSupportActionBar();
        //ab.setTitle("홈");

        database = FirebaseDatabase.getInstance().getReference();

        url = "http://52.79.223.4:8081/MainController";
        http = new DefaultHttpClient();

        //비콘 사용을 위한 선언
        mMinewBeaconManager = MinewBeaconManager.getInstance(this);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        userDTO = new userDB();

        mAdapter = new BeaconListAdapter();
        initListener();

        nameValuePairs = new ArrayList<>();

        auth = FirebaseAuth.getInstance();

        //툴바에 메터리얼 디자인 적용
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //홈 드로어 뷰를 만드는 부분
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        TextView nameTextView = findViewById(R.id.header_name_textView);
        TextView emailTextView = findViewById(R.id.header_email_textView);

        //nameTextView에 자꾸 NullPointerException이 발생하여 이를 방지하기위해 Objects.requireNonNull을 넣어주었다.
        nameTextView.setText(Objects.requireNonNull(auth.getCurrentUser()).getDisplayName());
        emailTextView.setText(auth.getCurrentUser().getEmail());
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onStart() {
        super.onStart();

        final String[] check = new String[1];

        database.child("userDB").child(Objects.requireNonNull(auth.getCurrentUser()).getUid())
                .child("basicInformation").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                fbDB.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    fbDB = (HashMap<String, Objects>) dataSnapshot.getValue();
                }

                check[0] = String.valueOf(fbDB.get("gender"));

                Log.d("check 확인", check[0]);

                if (check[0].equals("null")) {
                    changeIntent(R.id.nav_change_setting);
                } else {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            auth.signOut();
            LoginManager.getInstance().logOut(); //Facebook을 이용하여 로그인을 했을 경우 로그아웃 시 호출
            finish();
            changeIntent(R.id.nav_logout);
        } else if (id == R.id.nav_my_information) {
            changeIntent(R.id.nav_my_information);
        } else if (id == R.id.nav_create_card) {
            changeIntent(R.id.nav_create_card);
        } else if (id == R.id.nav_boarding) {
            changeIntent(R.id.nav_boarding);
        } else if (id == R.id.nav_change_setting) {
            changeIntent(R.id.nav_change_setting);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void changeIntent(int id) {
        Intent intent;
        if (id == R.id.nav_logout) {
            intent = new Intent(HomeActivity.this, IndexActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_create_card) {
            intent = new Intent(HomeActivity.this, CardRegisterActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_boarding) {
            intent = new Intent(HomeActivity.this, TotalInformationActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_change_setting) {
            intent = new Intent(HomeActivity.this, SettingActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_my_information) {
            intent = new Intent(HomeActivity.this, ViewInformationActivity.class);
            startActivity(intent);
        }
    }

    private void initListener() {
        ok = findViewById(R.id.friend);
        text = findViewById(R.id.setFriend);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                peoples[0] = String.valueOf(text.getText());

                if (mMinewBeaconManager != null) {
                    BluetoothState bluetoothState = mMinewBeaconManager.checkBluetoothState();
                    switch (bluetoothState) {
                        case BluetoothStateNotSupported:
                            Toast.makeText(HomeActivity.this, "Not Support BLE", Toast.LENGTH_SHORT).show();
                            finish();
                            break;
                        case BluetoothStatePowerOff:
                            showBLEDialog();
                            return;
                        case BluetoothStatePowerOn:
                            break;
                    }
                }
                if (isScanning) {
                    isScanning = false;
                    Toast.makeText(HomeActivity.this, "자동 결제 시스템이 종료되었습니다", Toast.LENGTH_SHORT).show();
                    ok.setText("설정 및 작동");
                    if (mMinewBeaconManager != null) {
                        mMinewBeaconManager.stopScan();
                    }
                } else {
                    isScanning = true;
                    ok.setText("동작 정지");
                    try {
                        assert mMinewBeaconManager != null;
                        mMinewBeaconManager.startScan();
                        Toast.makeText(HomeActivity.this, "자동 결제 시스템이 시작되었습니다", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mMinewBeaconManager.setDeviceManagerDelegateListener(new MinewBeaconManagerListener() {
            /**
             *   if the manager find some new beacon, it will call back this method.
             *
             *  @param minewBeacons  new beacons the manager scanned
             */
            @Override
            public void onAppearBeacons(List<MinewBeacon> minewBeacons) {

            }

            /**
             *  if a beacon didn't update data in 10 seconds, we think this beacon is out of rang, the manager will call back this method.
             *
             *  @param minewBeacons beacons out of range
             */
            @Override
            public void onDisappearBeacons(List<MinewBeacon> minewBeacons) {
                /*for (MinewBeacon minewBeacon : minewBeacons) {
                    String deviceName = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Name).getStringValue();
                    Toast.makeText(getApplicationContext(), deviceName + "  out range", Toast.LENGTH_SHORT).show();
                }*/
            }


            private int count = 0;
            private boolean boarding = false;

            /**
             *  the manager calls back this method every 1 seconds, you can get all scanned beacons.
             *
             *  @param minewBeacons all scanned beacons
             */
            @Override
            public void onRangeBeacons(final List<MinewBeacon> minewBeacons) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<MinewBeacon> minewBeacons2 = new ArrayList<>();
                        for (int i = 0; i < minewBeacons.size(); i++) {
                            Log.e("인식 확인", String.valueOf(minewBeacons.size()));
                            if (minewBeacons.get(i).getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Name).getStringValue().equals("MiniBeacon_15043")) {
                                //double power = calculateAccuracy(minewBeacons.get(i).getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_TxPower).getIntValue()
                                //,minewBeacons.get(i).getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI).getFloatValue());
                                Position1.distance = minewBeacons.get(i).getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI).getFloatValue();
                                minewBeacons2.add(minewBeacons.get(i));
                            } else if (minewBeacons.get(i).getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Name).getStringValue().equals("MiniBeacon_15048")) {
                                //double power = calculateAccuracy(minewBeacons.get(i).getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_TxPower).getIntValue()
                                //        ,minewBeacons.get(i).getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI).getFloatValue());
                                Position2.distance = minewBeacons.get(i).getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI).getFloatValue();
                                minewBeacons2.add(minewBeacons.get(i));
                            } else if (minewBeacons.get(i).getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Name).getStringValue().equals("MiniBeacon_15054")) {
                                //double power = calculateAccuracy(minewBeacons.get(i).getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_TxPower).getIntValue()
                                //       ,minewBeacons.get(i).getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI).getFloatValue());
                                Position3.distance = minewBeacons.get(i).getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI).getFloatValue();
                                minewBeacons2.add(minewBeacons.get(i));
                            }
                        }


                        if (Position1.distance != 0 && Position2.distance != 0 && Position3.distance != 0) {
                            //Position4 = trilaterations.trilaterationFunction(Position1,Position2,Position3);
                            //float filteredX = (float) mKalmanAccX.update(Position4.x);
                            //float filteredY = (float) mKalmanAccY.update(Position4.y);
                            //Position4.x = filteredX;
                            //Position4.y = filteredY;
                            /*
                            double rkwnd = 4.0;
                            if(Position1.distance< Position2.distance)
                                if(Position1.distance<Position3.distance)
                                    Position1.distance = Position1.distance + rkwnd;
                                else
                                    Position3.distance = Position3.distance + rkwnd;
                            else
                                if(Position2.distance < Position3.distance)
                                    Position2.distance = Position2.distance + rkwnd;
                                else
                                    Position3.distance = Position3.distance + rkwnd;
*/
                            double filtered1 = mKalmanAcc1.update(Position1.distance);
                            double filtered2 = mKalmanAcc2.update(Position2.distance + 10.0);
                            double filtered3 = mKalmanAcc3.update(Position3.distance + 4.0);


                            //filtered RSSI

                            double D1 = Math.pow(10, (-filtered1) / 20);
                            double D2 = Math.pow(10, (-filtered2) / 20);
                            double D3 = Math.pow(10, (-filtered3) / 20);
                            double filterdd1 = mKalmanAcc11.update(D1);
                            double filterdd2 = mKalmanAcc22.update(D2);
                            double filterdd3 = mKalmanAcc33.update(D3);
                            //filtered Distance


                            //Toast.makeText(getApplicationContext(), Position4.x+","+Position4.y,Toast.LENGTH_SHORT).show();
                            Log.d("ghkrdls", D1 + "," + D2 + "," + D3 + " : " + filterdd1 + "," + filterdd2 + "," + filterdd3);
                            //Log.d("ghkrdls",filterdd1 +"," + filterdd2+"," + filterdd3);

                            count++;
                            if (count == 20) {
                                //double D1 = Math.pow(10,(-filtered1)/20);
                                //double D2 = Math.pow(10,(-filtered2)/20);
                                //double D3 = Math.pow(10,(-filtered3)/20);

                                Position1.distance = filterdd1;
                                Position2.distance = filterdd2;
                                Position3.distance = filterdd3;

                                //Log.d("ghkrdls",Position1.distance +"," + Position2.distance+"," + Position3.distance);
                                //Log.d("ghkrdls",filterdd1 +"," + filterdd2+"," + filterdd3);

                                mKalmanAcc1 = new KalmanFilter(0.0f);
                                mKalmanAcc2 = new KalmanFilter(0.0f);
                                mKalmanAcc3 = new KalmanFilter(0.0f);
                                mKalmanAcc11 = new KalmanFilter(0.0f);
                                mKalmanAcc22 = new KalmanFilter(0.0f);
                                mKalmanAcc33 = new KalmanFilter(0.0f);

                                //탑승, 하차에 관련된 action 수행
                                Position4 = Trilateration.trilaterationFunction(Position1, Position2, Position3);
                                if (Case(Position4.x, Position4.y) && !boarding) {
                                    Toast.makeText(getApplicationContext(), "탑승완료", Toast.LENGTH_SHORT).show();
                                    boarding = true;

                                    //버스 승차시
                                    if (boarding) {
                                        try {
                                            nameValuePairs.add(new BasicNameValuePair("action", "ride"));
                                            nameValuePairs.add(new BasicNameValuePair("userid", userDTO.userId));
                                            nameValuePairs.add(new BasicNameValuePair("busdriverid", busdriverId)); //버스기사 id를 보냄
                                            nameValuePairs.add(new BasicNameValuePair("busnumber", busNumber)); //버스 번호 보냄
                                            nameValuePairs.add(new BasicNameValuePair("peoples", peoples[0]));

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

                                            //서버로부터 true라는 대답이 돌아왔다는 것은 정상적으로 정보가 저장이 완료되었다는 뜻임
                                            if (noHTMLString.equals("true")) {
                                                Toast.makeText(HomeActivity.this, "탑승이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } else if (!Case(Position4.x, Position4.y) && boarding) {
                                    Toast.makeText(getApplicationContext(), "하차처리", Toast.LENGTH_SHORT).show();
                                    boarding = false;

                                    //하차 시
                                    if (!boarding) {
                                        try {

                                            nameValuePairs.add(new BasicNameValuePair("action", "quit"));
                                            nameValuePairs.add(new BasicNameValuePair("userid", userDTO.userId));

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

                                            //서버로부터 true라는 대답이 돌아왔다는 것은 정상적으로 정보가 저장이 완료되었다는 뜻임
                                            if (noHTMLString.equals("true")) {
                                                Toast.makeText(HomeActivity.this, "하차가 완료되었습니다.", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                Log.d("ghkrdls", Position4.x + "," + Position4.y);
                                Log.d("ghkrdls", " " + Case(Position4.x, Position4.y));
                                count = 0;
                            }

                            //if(Case(Position4.x, Position4.y))
                            //Toast.makeText(getApplicationContext(),  Position1.distance +"," + Position2.distance+"," + Position3.distance, Toast.LENGTH_SHORT).show();
                            //Toast.makeText(getApplicationContext(),  Position4.x +"," +Position4.y, Toast.LENGTH_SHORT).show();
                            //Toast.makeText(getApplicationContext(),  Position4.x +"," +Position4.y, Toast.LENGTH_SHORT).show();
                            //Log.d("ghkrdls", Position1.distance +"," + Position2.distance+"," + Position3.distance+ ","+filtered1+","+filtered2+","+filtered3);
                            //Log.i(this.getClass().getName(), "Position4.x");
                            //Log.i(this.getClass().getName(), "Position4.y");
                        }

                        Collections.sort(minewBeacons2, comp);
                        Log.e("tag", state + "");
                        if (state == 1 || state == 2) {
                        } else {
                            mAdapter.setItems(minewBeacons2);
                        }

                    }
                });
            }

            /**
             *  the manager calls back this method when BluetoothStateChanged.
             *
             *  @param state BluetothState
             */
            @Override
            public void onUpdateState(BluetoothState state) {
                switch (state) {
                    case BluetoothStatePowerOn:
                        Toast.makeText(getApplicationContext(), "BluetoothStatePowerOn", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothStatePowerOff:
                        Toast.makeText(getApplicationContext(), "BluetoothStatePowerOff", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    private void showBLEDialog() {
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_home_drawer, menu);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
