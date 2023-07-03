package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;      // 파이어베이스 인증 처리
    private DatabaseReference mDatabaseRef;  // 실시간 데이터베이스 - 서버에 연동시킬 수 있는 객체
    private EditText mEtEmail, mEtPwd;       // 로그인 입력필드
    private FirebaseUser mFirebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // activity는 하나의 화면, 하나의 xml만 연동이 가능
        //해당 view (activity_login이라는 xml) 기준으로만 아래 findViewById 괄호 안의 변수를 가져와.
        //따라서 해당 xml에서 선언된 변수를 가져와야해!

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("whereIsDog");

        mEtEmail = findViewById(R.id.et_email);
        mEtPwd = findViewById(R.id.et_pwd);


        Button btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //로그인 요청
                String strEmail = mEtEmail.getText().toString();
                String strPwd = mEtPwd.getText().toString();

                mFirebaseAuth.signInWithEmailAndPassword(strEmail, strPwd).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 로그인 성공
                            mFirebaseUser = mFirebaseAuth.getCurrentUser();
                            if (mFirebaseUser != null) {
                                String uid = mFirebaseUser.getUid();
                                // 메인 액티비티로 돌아갈 때 입력 값 돌려줌. 사용자 UID 넘겨주기
                                MainActivity.userUid = uid;

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);

                                finish(); // 로그인 완료하면 현재 login_activity를 쓸 일이 없기 때문에 현재 액티비티 파괴
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "로그인 실패!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });


        Button btn_register = findViewById(R.id.btn_register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 회원가입 화면으로 이동
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class); // 버튼 눌러서 화면 이동하는 코드 Intent (현재화면, 이동할 화면)
                startActivity(intent);
            }
        });
    }
}