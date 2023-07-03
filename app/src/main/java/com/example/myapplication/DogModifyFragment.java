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
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DogModifyFragment extends Fragment {
    private EditText editTextName;
    private EditText editTextBreed;
    private EditText editTextAge;
    private RadioGroup radioGroupGender;
    private CheckBox checkBoxNeutered;
    private Button buttonRegister;
    private Button buttonUpdate;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference dogReference;
    private FirebaseUser currentUser;

    private CheckBox checkBoxVaccinated;

    private boolean isVaccinated;

    public DogModifyFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_dogmodify, container, false);

        editTextName = view.findViewById(R.id.editTextName);
        editTextBreed = view.findViewById(R.id.editTextBreed);
        editTextAge = view.findViewById(R.id.editTextAge);
        radioGroupGender = view.findViewById(R.id.radioGroupGender);
        checkBoxNeutered = view.findViewById(R.id.checkBoxNeutered);
        checkBoxVaccinated = view.findViewById(R.id.checkBoxVaccination);
        buttonRegister = view.findViewById(R.id.buttonModifydog);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseAuth = FirebaseAuth.getInstance();

        // 기존 정보 불러오기
        if (currentUser != null) {
            String currentUserUid = currentUser.getUid();
            dogReference = FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUserUid)
                    .child("dogs");

            // 기존 정보 가져와서 설정하기
            dogReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot dogSnapshot : dataSnapshot.getChildren()) {
                            Dog dog = dogSnapshot.getValue(Dog.class);
                            if (dog != null) {
                                // 기존 정보를 EditText와 CheckBox에 설정
                                editTextName.setText(dog.getName());
                                editTextBreed.setText(dog.getBreed());
                                editTextAge.setText(dog.getAge());

                                // 성별 설정
                                if (dog.getGender().equals("남")) {
                                    ((RadioButton) view.findViewById(R.id.radioButtonMale)).setChecked(true);
                                } else {
                                    ((RadioButton) view.findViewById(R.id.radioButtonFemale)).setChecked(true);
                                }
                                // 중성화 여부 설정
                                checkBoxNeutered.setChecked(dog.isNeutered());

                                // 예방 접종 여부 설정
                                checkBoxVaccinated.setChecked(dog.isVaccinated());
                                break;
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // 에러 처리를 합니다
                }
            });
        }

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        // 반려견 정보 받아오기
        String userUid = MainActivity.userUid;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users").child(userUid).child("dogs");


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
        boolean isVaccinated = checkBoxVaccinated.isChecked();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(breed) || TextUtils.isEmpty(age) || TextUtils.isEmpty(gender)) {
            showToast("모두 입력하세요");
        } else {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String currentUserUid = currentUser.getUid();
                dogReference = FirebaseDatabase.getInstance().getReference("users")
                        .child(currentUserUid)
                        .child("dogs");
                String dogId = dogReference.push().getKey();
                Dog dog = new Dog(name, breed, age, gender, isNeutered, isVaccinated);

                if (dogId != null) {
                    dogReference.child(dogId).setValue(dog)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        showToast("반려견 정보 등록 완료");

                                        // 이전 단계로 돌아가기 위해 Fragment를 스택에서 제거합니다.
                                        requireActivity().getSupportFragmentManager().popBackStack();

                                        // 기존 정보를 업데이트합니다
                                        DatabaseReference existingDogRef = FirebaseDatabase.getInstance().getReference("users")
                                                .child(currentUserUid)
                                                .child("dogs")
                                                .child(dogId);
                                        existingDogRef.setValue(dog);
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
