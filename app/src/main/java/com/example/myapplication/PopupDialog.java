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
    private int count = 0;

    private void showCustomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_dialog_user, null);

        AlertDialog customDialog = builder.setView(dialogView).create();
        ImageView dogPhotoImageView = dialogView.findViewById(R.id.iv_dog_photo);
        TextView dogNameTextView = dialogView.findViewById(R.id.tv_dog_name);
        TextView dogAgeTextView = dialogView.findViewById(R.id.tv_dog_age);
        TextView dogBreedTextView = dialogView.findViewById(R.id.tv_dog_breed);

        dogPhotoImageView.setImageResource(R.drawable.dog_image);
        dogNameTextView.setText("반려견 이름");
        dogAgeTextView.setText("반려견 나이");
        dogBreedTextView.setText("반려견 종류");


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