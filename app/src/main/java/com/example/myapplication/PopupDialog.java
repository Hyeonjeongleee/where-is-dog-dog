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


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
        MainActivity.kokUserUid = markerId;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_dialog_user, null);

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
                .setPositiveButton("콕찌르기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // "콕찌르기" 버튼이 클릭되었을 때 수행할 동작
                        // TODO: 콕찌르기 동작 구현
                        String markerId = MainActivity.kokUserUid;

                        // 상대방 userUid(콕 찔린 사람) - alarm에 나의 userUid(콕 찌른 사람) 남기기.
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference("users")
                                        .child(markerId)
                                        .child("alarm");

                        String myId = MainActivity.userUid;
                        myRef.child(myId).setValue(myId);

                        // 상대방의 userUid 아래 kok 키 값을 true로 바꿔주기. (상대방 인식키)
                        DatabaseReference kokReceiver = FirebaseDatabase.getInstance().getReference("users")
                                .child(markerId);
                        kokReceiver.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild("kok")) {
                                    // "kok" 키가 이미 존재함
                                    Log.d("TAG", "Key exists");
                                    // "kok" 키에 값을 설정하거나 업데이트
                                    kokReceiver.child("kok").setValue(true);
                                } else {
                                    // "kok" 키가 존재하지 않음
                                    Log.d("TAG", "Key does not exist");
                                    kokReceiver.child("kok").setValue(true);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // 에러 처리
                                Log.e("TAG", "Error: " + databaseError.getMessage());
                            }
                        });


                        Toast.makeText(requireContext(), "콕 찔렀다멍!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 필요한 경우 "확인" 버튼 클릭을 처리하세요.
                    }
                });
        return builder.create();
    }
}