package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DogRegistrationFragment extends Fragment {
    private EditText editTextName;
    private EditText editTextBreed;
    private EditText editTextAge;
    private RadioGroup radioGroupGender;
    private RadioButton radioButtonMale;
    private RadioButton radioButtonFemale;
    private CheckBox checkBoxNeutered;
    private Button buttonRegister;
    private FirebaseAuth mFirebaseAuth;      // 파이어베이스 인증 처리
    private DatabaseReference mDatabaseRef;  // 실시간 데이터베이스 - 서버에 연동시킬 수 있는 객체
    private DatabaseReference dogReference;
    private DatabaseReference userReference;
    private FirebaseUser currentUser;
    private FirebaseUser mFirebaseUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dogregistration, container, false);

        editTextName = view.findViewById(R.id.editTextName);
        editTextBreed = view.findViewById(R.id.editTextBreed);
        editTextAge = view.findViewById(R.id.editTextAge);
        radioGroupGender = view.findViewById(R.id.radioGroupGender);
        radioButtonMale = view.findViewById(R.id.radioButtonMale);
        radioButtonFemale = view.findViewById(R.id.radioButtonFemale);
        checkBoxNeutered = view.findViewById(R.id.checkBoxNeutered);
        buttonRegister = view.findViewById(R.id.buttonRegisterdog);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                //String uid = firebaseUser.getUid();
                String uid = MainActivity.userUid;
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    registerDog();
                } else {
                    Toast.makeText(requireContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void registerDog() {
        String name = editTextName.getText().toString();
        String breed = editTextBreed.getText().toString();
        String age = editTextAge.getText().toString();

        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        RadioButton selectedGenderRadioButton = getView().findViewById(selectedGenderId);
        String gender = "";

        if (selectedGenderRadioButton != null) {
            gender = selectedGenderRadioButton.getText().toString();
        }

        boolean isNeutered = checkBoxNeutered.isChecked();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(breed) || TextUtils.isEmpty(age) || TextUtils.isEmpty(gender)) {
            showToast("모두 입력하세요");
        } else {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String currentUserUid = currentUser.getUid();
                dogReference = FirebaseDatabase.getInstance().getReference("users")
                        .child(currentUserUid)
                        .child("dogs");
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                String dogId = dogReference.push().getKey();
                Dog dog = new Dog(name, breed, age, gender, isNeutered);

                if (dogId != null) {
                    dogReference.child(dogId).setValue(dog)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        showToast("반려견 정보 등록 완료");
                                    } else {
                                        showToast("반려견 정보 등록 실패");
                                    }
                                }
                            });
                }
            } else {
                showToast("로그인이 필요합니다");
            }
        }
    }
}