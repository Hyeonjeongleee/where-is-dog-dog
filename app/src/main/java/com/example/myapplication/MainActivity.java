package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    public static String userUid;  // 메인에서 가지고 다닐 내 uid
    public static String kokUserUid;  // 내가 콕 찌른사람

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private MapFragment mapFragment;
    private ChatFragment chatFragment;
    private MyinfoAfterLoginFragment myinfoAfterLoginFragment;
    private RequestLoginFragment requestLoginFragment;
    private ChatListFragment chatListFragment;

    // 실시간 위치용 코드
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private ActivityMainBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //실시간 위치용 코드
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // 위치 권한 확인 및 요청
        if (checkLocationPermission()) {
            startLocationUpdates();
        }

        //nav-bar
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavi);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        setFrag(0);
                        break;
                    case R.id.action_map:
                        setFrag(1);
                        break;
                    case R.id.action_chat:
                        setFrag(2);
                        break;
                }
                return true;
            }
        });

        mapFragment = new MapFragment();
        chatFragment = new ChatFragment();
        myinfoAfterLoginFragment = new MyinfoAfterLoginFragment();
        requestLoginFragment = new RequestLoginFragment();
        chatListFragment = new ChatListFragment();
        setFrag(0);  // 첫 프래그먼트 화면 지정

        //kok 데이터를 실시간 받아오기 위한 ReceiveKok() 실행
        ReceiveKok kokReceiver = new ReceiveKok();

        //kok 보낸거 수락했는지 데이터 실시간 받아오기 위한
        CheckMatched checkMatched = new CheckMatched();
    }


    // 프래그먼트 교체가 일어나는 실행문
    private void setFrag(int n) {
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        switch (n) {
            case 0:
                ft.replace(R.id.main_frame, myinfoAfterLoginFragment);
                ft.commit();  // 저장을 의미
                break;
            case 1:
                ft.replace(R.id.main_frame, mapFragment);
                ft.commit();
                break;
            case 2:
                ft.replace(R.id.main_frame, chatListFragment);
                ft.commit();
                break;
        }
    }


    //실시간 위치용 코드
    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private void startLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    // 위치 정보를 이용하여 원하는 작업 수행
                    // 예: 실시간 위치 공유, 지도 업데이트 등
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } catch (SecurityException e) {
            e.printStackTrace();
            // 보안 예외 처리
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                // 위치 권한이 거부된 경우 처리
                Toast.makeText(this, "위치 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null) {
        }
    }

    public class ReceiveKok {
        public String myUid = userUid;
        public String urUid;
        DataSnapshot previousSnapshot;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mDataBase = database.getReference("users").child(myUid);

        public ReceiveKok() {
            mDataBase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean wasActive = false;
                    if (previousSnapshot != null && previousSnapshot.child("kok").exists()) {
                        boolean isActive = dataSnapshot.child("kok").getValue(Boolean.class);
                        if (isActive != wasActive) {
                            // 알림 생성 로직 실행
                            AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
                            ad.setIcon(R.drawable.dog_icon);
                            ad.setTitle("콕 찌르기 도착");
                            ad.setMessage("친구야, 같이 놀자멍!");

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference mDataBase = database.getReference("users").child(myUid);
                            mDataBase.child("kok").setValue(false);

                            // 상대방 userUid 가져오기
                            DatabaseReference alarmRef = FirebaseDatabase.getInstance().getReference("users").child(myUid).child("alarm");
                            alarmRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    // 데이터 스냅샷에서 하위 레퍼런스의 값을 가져오기
                                    if (dataSnapshot.exists()) {
                                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                            urUid = childSnapshot.getValue(String.class);
                                        }
                                    } else {
                                        // "alarm" 레퍼런스가 존재하지 않는 경우 처리
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // 에러 처리
                                }
                            });
                            System.out.println("test Kok's urUid : " + urUid);

                            ad.setPositiveButton("다음에..", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(MainActivity.this, "다음에 만나요, 멍!", Toast.LENGTH_SHORT).show();

                                    // 내 'CheckMatched' Reference 키 값 변경 > 콕 찌른 사람에게 거절 알람.
                                    FirebaseDatabase database3 = FirebaseDatabase.getInstance();
                                    DatabaseReference setMessage3 = database3.getReference("users")
                                            .child(myUid)
                                            .child("CheckMatched"); // "0" : default, "1" : 수락, "2" : 거절 (PopupDialog에서 콕 찌를때 상대유저 0으로 설정)
                                    setMessage3.setValue("2");

                                    dataSnapshot.child("alarm").getRef().removeValue();
                                    dialogInterface.dismiss();
                                }
                            });

                            ad.setNegativeButton("놀자!", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(MainActivity.this, "상대방과 채팅이 생성되었습니다! 만나요, 멍!", Toast.LENGTH_SHORT).show();

                                    // 상대방 'messages' Reference에 내 uid 생성
                                    FirebaseDatabase database1 = FirebaseDatabase.getInstance();
                                    DatabaseReference setMessage1 = database1.getReference("users")
                                            .child(urUid)
                                            .child("messages")
                                            .child(myUid);
                                    setMessage1.setValue(myUid);

                                    // 내 'messages' Reference에 상대방 uid 생성
                                    FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                                    DatabaseReference setMessage2 = database2.getReference("users")
                                            .child(myUid)
                                            .child("messages")
                                            .child(urUid);
                                    setMessage2.setValue(urUid);

                                    // 내 'CheckMatched' Reference 키 값 변경 > 콕 찌른 사람에게 수락 알람.
                                    FirebaseDatabase database3 = FirebaseDatabase.getInstance();
                                    DatabaseReference setMessage3 = database3.getReference("users")
                                            .child(myUid)
                                            .child("CheckMatched"); // "0" : default, "1" : 수락, "2" : 거절 (PopupDialog에서 콕 찌를때 상대유저 0으로 설정)
                                    setMessage3.setValue("1");

                                    // 사용한 alarm 삭제 및 종료
                                    dataSnapshot.child("alarm").getRef().removeValue();
                                    dialogInterface.dismiss();
                                }
                            });
                            ad.show();
                        }
                    }
                    previousSnapshot = dataSnapshot;
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.e("ReceiveKok", "loadPost:onCancelled", databaseError.toException());
                }
            });
        }
    }

    // Check Matched - Koker
    public class CheckMatched {
        public String myUid = userUid;
        public String urUid = kokUserUid;
        DataSnapshot previousSnapshot;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mDataBase = database.getReference("users");

        public CheckMatched() {
            mDataBase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean wasActive = false;
                    if (urUid != null && previousSnapshot.child(urUid) != null && previousSnapshot.child(urUid).child("CheckMatched").exists()) {
                        boolean isActive = !dataSnapshot.child("CheckMatched").getValue(String.class).equals("0");
                        if (isActive != wasActive) {
                            // 알림 생성 로직 실행
                            if (dataSnapshot.child("CheckMatched").getValue(String.class).equals("1")) {
                                AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
                                ad.setIcon(R.drawable.dog_icon);
                                ad.setTitle("콕 찌르기 수락");
                                ad.setMessage("콕 찌른 상대와 채팅이 생성 되었습니다! 멍!");

                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference mDataBase = database.getReference("users").child(urUid);
                                mDataBase.child("CheckMatched").setValue("0");

                                ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                                ad.show();
                            } else {
                                AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
                                ad.setIcon(R.drawable.sad_tear);
                                ad.setTitle("콕 찌르기 거절");
                                ad.setMessage("다음에 만나요, 멍!");

                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference mDataBase = database.getReference("users").child(urUid);
                                mDataBase.child("CheckMatched").setValue("0");

                                ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                                ad.show();
                            }
                        }
                    }
                    previousSnapshot = dataSnapshot;
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.e("CheckMatched", "loadPost:onCancelled", databaseError.toException());
                }
            });
        }
    }

}