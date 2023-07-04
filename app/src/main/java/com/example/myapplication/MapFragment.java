package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import android.os.Handler;

public class MapFragment extends Fragment implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap mMap;
    private Marker currentMarker = null;

    private static final String TAG = "googlemap";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000; // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초

    // OnRequestPermissionsResultCallback에서 수신된 결과에서 ActivityCompat.OnRequestPermissionsResultCallback를 사용한 퍼미션 요청을 구별하기 위함
    private static final int PERMISSION_REQUEST_CODE = 100;

    // 앱을 실행하기 위해 필요한 퍼미션 정의
    String[] REQUIRED_PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION };

    Location mCurrentLocation;
    LatLng currentPosition;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest; // 주의
    private Location location;

    //GPT _ Fragment로 전환중 추가한 코드
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return rootView;
    }


//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.map);
//
//        locationRequest = new LocationRequest()
//                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                .setInterval(UPDATE_INTERVAL_MS)
//                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);
//
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
//
//        builder.addLocationRequest(locationRequest);
//
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
//
//    }

    public void onMapReady(@NonNull GoogleMap googleMap) {
        Activity activity = getActivity();
        if (activity == null) {
            return; // 액티비티가 없는 경우, 작업을 중단하고 리턴합니다.
        }
        Log.d(TAG, "onMapReady: 들어옴 ");
        mMap = googleMap;

        // 지도의 초기위치 이동
        setDefaultLocation();

        // 런타임 퍼미션 처리
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 퍼미션을 가지고 있다면
            startLocationUpdates(); // 위치 업데이트 실행
            // 5초마다 위치 업데이트를 실행하기 위해 setInterval 함수 사용
            Handler handler = new Handler();
            int delay = 5000; // 5초
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Activity activity = getActivity();
                    if (activity == null) {
                        return; // 액티비티가 없는 경우, 작업을 중단하고 리턴합니다.
                    }
                    startLocationUpdates();
                    handler.postDelayed(this, delay);
                }
            }, delay);

        } else {
            // 퍼미션 요청하기
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), REQUIRED_PERMISSION[0])) {
                Snackbar.make(requireView(), "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Activity activity = getActivity();
                        if (activity == null) {
                            return; // 액티비티가 없는 경우, 작업을 중단하고 리턴합니다.
                        }
                        ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSION, PERMISSION_REQUEST_CODE);
                    }
                }).show();
            } else {
                ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSION, PERMISSION_REQUEST_CODE);
            }
        }
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                Activity activity = getActivity();
                if (activity == null) {
                    return; // 액티비티가 없는 경우, 작업을 중단하고 리턴합니다.
                }
                Log.d(TAG, "onMapClick: ");
            }
        });


        // Firebase 데이터베이스에서 다른 사용자의 위치 정보를 가져옴
        FirebaseDatabase database1 = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database1.getReference("users");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUserEmail = currentUser != null ? currentUser.getEmail() : "";
        String currentUserUid = currentUser != null ? currentUser.getUid() : "";

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Activity activity = getActivity();
                if (activity == null) {
                    return; // 액티비티가 없는 경우, 작업을 중단하고 리턴합니다.
                }
                // 이전 마커들 제거
                mMap.clear();

                // 데이터베이스에서 위치 정보를 가져와서 처리
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userUid = userSnapshot.getKey();
                    String emailID = userSnapshot.child("emailID").getValue(String.class);
                    Double latitudeObj = userSnapshot.child("latitude").getValue(Double.class);
                    Double longitudeObj = userSnapshot.child("longitude").getValue(Double.class);

                    if (emailID != null && latitudeObj != null && longitudeObj != null) {
                        double latitude = latitudeObj.doubleValue();
                        double longitude = longitudeObj.doubleValue();

                        // 가져온 위치 정보를 마커로 표시
                        LatLng userLatLng = new LatLng(latitude, longitude);
                        MarkerOptions markerOptions;

                        // 6.28추가 cj 마커 다르게
                        if (emailID.equals(currentUserEmail)) {
                            // 현재 사용자의 마커
                            markerOptions = new MarkerOptions()
                                    .position(userLatLng)
                                    .title(userUid)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.dog_icon1));
                        } else {
                            // 다른 사용자의 마커
                            markerOptions = new MarkerOptions()
                                    .position(userLatLng)
                                    .title(userUid)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.dog_icon3));
                        }

                        // 마커 추가
                        currentMarker = mMap.addMarker(markerOptions);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Activity activity = getActivity();
                if (activity == null) {
                    return; // 액티비티가 없는 경우, 작업을 중단하고 리턴합니다.
                }
                // 오류 처리
                Log.e(TAG, "Failed to read user locations", databaseError.toException());
            }
        });
        // 팝업 추가
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                currentMarker = marker; // 클릭한 마커를 currentMarker에 대입합니다.

                if (!marker.getTitle().equals(currentUserUid)) {
                    // marker.equals(currentMarker) : 내 정보도 클릭해서 볼 수 있는 코드
                    showPopupDialog(marker.getTitle()); // 마커가 클릭되었을 때 팝업을 띄웁니다.
                    return true;
                }
                return false;
            }
        });
    }
    private void showPopupDialog(String markerId) {
        PopupDialog popupDialog = new PopupDialog(markerId);
        popupDialog.show(getChildFragmentManager(), "popup_dialog");
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if(locationList.size() > 0){
                location = locationList.get(locationList.size() -1);
                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도 :" + String.valueOf(location.getLatitude()) + "경도 :" +
                        String.valueOf(location.getLongitude());
                // 현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet);
                mCurrentLocation = location;
            }
        }
    };

    private String getCurrentAddress(LatLng currentPosition) {
        // 지오코더 gps를 주소로 변환
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());

        List<Address> addresses;
        try{
            addresses = geocoder.getFromLocation(
                    currentPosition.latitude,
                    currentPosition.longitude,
                    1
            );
        }catch (IOException ioException){
            // 네트워크 문제
            Toast.makeText(requireContext(), "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return  "지오코더 서비스 사용 불가";
        }catch (IllegalArgumentException illegalArgumentException){
            Toast.makeText(requireContext(),"잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if(addresses == null || addresses.size() == 0){
            Toast.makeText(requireContext(),"주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }else{
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }
    }

    private void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
        Activity activity = getActivity();
        if (activity == null) {
            return; // 액티비티가 없는 경우, 작업을 중단하고 리턴합니다.
        }

        if(currentMarker != null)
        {
            currentMarker.remove();
        }

        LatLng currentLatLng =  new LatLng(location.getLatitude(), location.getLongitude());

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String uid = MainActivity.userUid;

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.dog_icon1));

        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mMap.moveCamera(cameraUpdate);

        // 위도와 경도 값을 가져옵니다.
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        // 위도와 경도를 문자열로 변환합니다.
        String latitudeString = String.valueOf(latitude);
        String longitudeString = String.valueOf(longitude);

        // Firebase에 위도와 경도를 저장합니다.
        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
        DatabaseReference locationRef = database2.getReference("users").child(uid);
        locationRef.child("emailID").setValue(firebaseUser.getEmail());
        locationRef.child("latitude").setValue(location.getLatitude());
        locationRef.child("longitude").setValue(location.getLongitude());

    }



    private void startLocationUpdates() {
        Activity activity = getActivity();
        if (activity == null) {
            return; // 액티비티가 없는 경우, 작업을 중단하고 리턴합니다.
        }

        if(!checkLocationServicesStatus()){
            showDiologForLocationServiceSetting();

        }else{
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

            if(hasFineLocationPermission != PackageManager.PERMISSION_GRANTED|| hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED ){

                Log.d(TAG, "startLocationUpdates: 퍼미션 없음");
                return;
            }

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            if(checkPermission()){
                mMap.setMyLocationEnabled(true);
            }
        }

    }

    @Override
    public void onStart() {
        Activity activity = getActivity();
        if (activity == null) {
            return; // 액티비티가 없는 경우, 작업을 중단하고 리턴합니다.
        }

        super.onStart();
        Log.d(TAG, "onStart: ");

        if(checkPermission()){
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            if(mMap!=null){
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    public void onStop() {
        Activity activity = getActivity();
        if (activity == null) {
            return; // 액티비티가 없는 경우, 작업을 중단하고 리턴합니다.
        }

        super.onStop();

        if(mFusedLocationClient != null){
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private boolean checkPermission(){
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

        if(hasFineLocationPermission != PackageManager.PERMISSION_GRANTED|| hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED ){

            Log.d(TAG, "startLocationUpdates: 퍼미션 없음");
            return true;
        }

        return false;

    }

    private void showDiologForLocationServiceSetting() {
        Activity activity = getActivity();
        if (activity == null) {
            return; // 액티비티가 없는 경우, 작업을 중단하고 리턴합니다.
        }

        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // 위치 서비스가 비활성화된 경우
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("위치 서비스 비활성화");
            builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다. 위치설정을 수정하시겠습니까?");
            builder.setCancelable(true);
            builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
                }
            });
            builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            builder.create().show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Activity activity = getActivity();
        if (activity == null) {
            return; // 액티비티가 없는 경우, 작업을 중단하고 리턴합니다.
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 위치 권한이 허용된 경우
                    startLocationUpdates();
                } else {
                    // 위치 권한이 거부된 경우
                    // 권한에 대한 설명이나 앱을 사용할 수 없는 안내 메시지를 표시할 수 있습니다.
                }
                break;
        }
    }


    private boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void setDefaultLocation() {
        Activity activity = getActivity();
        if (activity == null) {
            return; // 액티비티가 없는 경우, 작업을 중단하고 리턴합니다.
        }

        // 기본 위치
        LatLng DEFAULT_LOCATION = new LatLng(100.56, 126.97);
        String markerTitle = "위치 정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 여부를 확인하세요";


        if(currentMarker != null){
            currentMarker.remove();
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.puppy));

        currentMarker = mMap.addMarker(markerOptions);


        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);
    }
}