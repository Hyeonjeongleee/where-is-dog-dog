package com.example.myapplication;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.myapplication.databinding.FragmentMyinfoAfterLoginBinding;
import com.example.myapplication.databinding.FragmentMyinfoBinding;

public class MyinfoAfterLoginFragment extends Fragment {

    private FragmentMyinfoAfterLoginBinding binding;
    private DogRegistrationFragment dogRegistrationFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMyinfoAfterLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btn_logout = view.findViewById(R.id.button_logout);

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.userUid = null;

                // 다른 프래그먼트로 이동
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main_frame, new MyinfoFragment());
                transaction.addToBackStack(null); // 이전 프래그먼트로 돌아가기 위해 back stack에 추가
                transaction.commit();
            }
        });

        Button btn_registerDog = view.findViewById(R.id.button_register_dog);
        btn_registerDog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dogRegistrationFragment = new DogRegistrationFragment();

                // 반려견 정보 등록 프래그먼트로 이동
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main_frame, dogRegistrationFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}