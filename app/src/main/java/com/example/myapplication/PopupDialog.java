package com.example.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import android.widget.Toast;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PopupDialog extends DialogFragment {
    private static final String TAG = "popUp";
    private String markerId;
    private int count = 0;
    private String puppy_name;
    private String puppy_age;
    private String puppy_breed;
    private String puppy_gender;
    private String puppy_vaccin;


    public PopupDialog(String markerId) {
        this.markerId = markerId;
    }

    private void showCustomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_dialog_user, null);

        AlertDialog customDialog = builder.setView(dialogView).create();
        ImageView dogPhotoImageView = dialogView.findViewById(R.id.iv_dog_photo);
        TextView dogNameTextView = dialogView.findViewById(R.id.tv_dog_name);
        TextView dogAgeTextView = dialogView.findViewById(R.id.tv_dog_age);
        TextView dogBreedTextView = dialogView.findViewById(R.id.tv_dog_breed);
        TextView dogGenderTextView = dialogView.findViewById(R.id.tv_dog_gender);
        TextView dogVaccinatedTextView = dialogView.findViewById(R.id.tv_dog_vaccinated);


        dogPhotoImageView.setImageResource(R.drawable.dog_image);

        // 반려견 정보 받아오기
        String userUid = markerId;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users").child(userUid).child("dogs");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dogSnapshot : dataSnapshot.getChildren()) {
                    String age = dogSnapshot.child("age").getValue(String.class);
                    String breed = dogSnapshot.child("breed").getValue(String.class);
                    String gender = dogSnapshot.child("gender").getValue(String.class);
                    String name = dogSnapshot.child("name").getValue(String.class);
                    Boolean vaccinated = dogSnapshot.child("vaccinated").getValue(Boolean.class);
                    if (age != null && breed != null && gender != null && name != null && vaccinated != null) {
                        puppy_name = name;
                        puppy_age = "나이: " + age;
                        puppy_breed = "견종: " + breed;
                        puppy_gender = "성별: " + gender;
                        if (vaccinated) puppy_vaccin = "백신 접종 완료";
                        else puppy_vaccin = "백신 미접종";

                        dogNameTextView.setText(puppy_name);
                        dogAgeTextView.setText(puppy_age);
                        dogBreedTextView.setText(puppy_breed);
                        dogGenderTextView.setText(puppy_gender);
                        dogVaccinatedTextView.setText(puppy_vaccin);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 오류 처리
                Log.e(TAG, "Failed to read age value", databaseError.toException());
            }
        });

        builder.setView(dialogView)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 필요한 경우 "확인" 버튼 클릭을 처리하세요.
                    }
                })
                .create()
                .show();
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_dialog, null);

        builder.setView(dialogView)
                .setPositiveButton("콕찌르기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // "콕찌르기" 버튼이 클릭되었을 때 수행할 동작
                        // TODO: 콕찌르기 동작 구현

                        Toast.makeText(requireContext(), "콕 찔렀다멍!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("반려견 정보", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 마커와 연관된 사용자 정보 가져오기
                        showCustomDialog();
                        String markerId = "마커의 ID"; // 실제로 사용되는 마커의 ID를 여기에 지정하십시오.
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("whereIsDog")
                                .child("UserAccount").child(markerId);
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (getContext() != null) {
                                    // 컨텍스트가 null이 아닌 경우에만 실행
                                    if (dataSnapshot.exists()) {
                                        UserAccount userAccount = dataSnapshot.getValue(UserAccount.class);
                                        if (userAccount != null) {
                                            final String userInfo = "사용자 정보:\n" +
                                                    "ID: " + userAccount.getIdToken() + "\n" +
                                                    "Email: " + userAccount.getEmailId() + "\n" +
                                                    "Password: " + userAccount.getPassword();

                                            // 다이얼로그 생성 및 표시
                                            AlertDialog.Builder infoDialogBuilder = new AlertDialog.Builder(getContext());
                                            infoDialogBuilder.setTitle("사용자 정보")
                                                    .setMessage(userInfo)
                                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            // 필요한 경우 "확인" 버튼 클릭을 처리하세요.
                                                        }
                                                    })
                                                    .create()
                                                    .show();
                                        }
                                    } else {
                                        // 해당 마커에 연결된 사용자 정보가 없음을 나타내는 처리
                                        Toast.makeText(getContext(), "해당 마커에 연결된 사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // 데이터베이스 조회 오류 처리
                                Toast.makeText(getContext(), "데이터베이스 조회 오류: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
        return builder.create();
    }

}