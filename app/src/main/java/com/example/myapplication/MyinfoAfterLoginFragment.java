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

public class MyinfoAfterLoginFragment extends Fragment {

    private FragmentMyinfoAfterLoginBinding binding;
    private DogRegistrationFragment dogRegistrationFragment;
    private DogModifyFragment dogModifyFragment;

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
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) {
                    activity.userUid = null;
                    Intent intent = new Intent(activity, LoginActivity.class);
                    startActivity(intent);
                }
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

        Button btn_editDog = view.findViewById(R.id.button_edit_dog);
        btn_editDog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dogModifyFragment = new DogModifyFragment();

                // 반려견 정보 등록 프래그먼트로 이동
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main_frame, dogModifyFragment);
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