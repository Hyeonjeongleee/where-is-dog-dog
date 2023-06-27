package com.example.myapplication;

import android.content.Context;
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

import android.content.Intent;
import android.widget.TextView;

import static android.app.Activity.RESULT_OK;

import com.example.myapplication.databinding.FragmentMyinfoBinding;

public class MyinfoFragment extends Fragment {

    private FragmentMyinfoBinding binding;
    TextView infoText;

    @Override
    public android.view.View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentMyinfoBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //test용 코드 추가
        infoText = view.findViewById(R.id.infoText);

        Button btn_login = view.findViewById(R.id.button_login); // 버튼을 findViewById로 찾습니다.

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                getLoginInfo.launch(intent);  //startActivityForResult와 동일한 기능 수행
            }
        });

        Button btn_register = view.findViewById(R.id.button_register);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    //RegisterForActivityResult를 위한 추가 변수
    private final ActivityResultLauncher<Intent> getLoginInfo = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                //Login Activity를 수행하고 돌아왔을때 결과 값을 받아 올 수 있는 통로
                if (result.getResultCode() == RESULT_OK) {
                    // 로그인 성공 시 유저 정보를 전달받음.
                    infoText.setText(result.getData().getStringExtra("value"));

                    // 다른 프래그먼트로 이동
                    FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.main_frame, new MyinfoAfterLoginFragment());
                    transaction.addToBackStack(null); // 이전 프래그먼트로 돌아가기 위해 back stack에 추가
                    transaction.commit();
                    }
                }
    );

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}