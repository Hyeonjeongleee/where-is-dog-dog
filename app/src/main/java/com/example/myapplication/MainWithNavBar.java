package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainWithNavBar extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private ProfileFragment profileFragment;
    private BlankFragment blankFragment;
    private MapFragment mapFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_with_nav_bar);

        bottomNavigationView = findViewById(R.id.bottomNavi);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_myinfo:
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

        profileFragment = new ProfileFragment();
        blankFragment = new BlankFragment();
        mapFragment = new MapFragment();
        setFrag(0);  // 첫 프래그먼트 화면 지정
    }


    // 프래그먼트 교체가 일어나는 실행문
    private void setFrag(int n) {
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        switch (n) {
            case 0:
                ft.replace(R.id.main_frame, profileFragment);
                ft.commit();  // 저장을 의미
                break;
            case 1:
                ft.replace(R.id.main_frame, mapFragment);
                ft.commit();
                break;
            case 2:
                ft.replace(R.id.main_frame, blankFragment);
                ft.commit();
                break;

        }
    }

}