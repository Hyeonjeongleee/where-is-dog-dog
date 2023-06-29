package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<ChatData> chatList;
    private String nick = "nick1";

    private EditText EditText_chat;
    private Button Button_send;
    private DatabaseReference myRef;
    private LinearLayout input_bar;
    private TextView txtTitle;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_chat, container, false);

        Button_send = view.findViewById(R.id.Button_send);
        EditText_chat = view.findViewById(R.id.EditText_chat);
        txtTitle = view.findViewById(R.id.txt_TItle);

        FirebaseApp.initializeApp(requireContext());

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String[] emailParts = user.getEmail().split("@");
            nick = emailParts[0]; // "@" 기호 이전 부분을 닉네임으로 설정합니다.

        } else {
            // 사용자가 로그인하지 않은 경우 처리할 내용
        }

        Button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = EditText_chat.getText().toString();

                if (msg != null) {
                    ChatData chat = new ChatData();
                    chat.setNickname(nick);
                    chat.setMsg(msg);
                    chat.setSentByMe(true); // 보낸 메시지인 경우 true로 설정
                    myRef.push().setValue(chat);
                    EditText_chat.setText("");
                }
            }
        });


        mRecyclerView = view.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(requireContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        chatList = new ArrayList<>();
        mAdapter = new ChatAdapter(chatList, requireContext(), nick, txtTitle);
        mRecyclerView.setAdapter(mAdapter);

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("message");


        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("CHATCHAT", dataSnapshot.getValue().toString());
                ChatData chat = dataSnapshot.getValue(ChatData.class);
                ((ChatAdapter) mAdapter).addChat(chat);
                scrollToBottom(); // 가장 하단으로 스크롤

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return view;
    }

    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        private int previousHeight = 0;
        private boolean isKeyboardShowing = true;

        @Override
        public void onGlobalLayout() {
            Rect rect = new Rect();
            mRecyclerView.getWindowVisibleDisplayFrame(rect);
            int screenHeight = mRecyclerView.getRootView().getHeight();
            int keyboardHeight = Math.abs(rect.bottom - rect.top); // 키보드의 높이

            if (keyboardHeight > 0) { // 키보드가 올라온 경우
                if (!isKeyboardShowing) {
                    isKeyboardShowing = true;
                    inputBarAnimate(true); // input_bar를 위로 올리는 애니메이션 시작
                }
            } else {
                if (isKeyboardShowing) {
                    isKeyboardShowing = false;
                    // 키보드가 내려가면 하단바 보이기
                    inputBarAnimate(false); // input_bar를 원래 위치로 돌리는 애니메이션 시작
                }
            }

            previousHeight = keyboardHeight;
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        input_bar = view.findViewById(R.id.input_bar);
        EditText_chat.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    inputBarAnimate(true); // input_bar를 위로 올리는 애니메이션 시작
                } else {
                    // 키보드가 내려가면 하단바 보이기
                    inputBarAnimate(false); // input_bar를 원래 위치로 돌리는 애니메이션 시작
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            EditText_chat.getViewTreeObserver().removeOnGlobalLayoutListener(keyboardLayoutListener);
        }
    }

    private void inputBarAnimate(boolean up) {
        float translationY = up ? -inputBarHeight() : 0; // up이 true일 때는 위로 올리고, false일 때는 원래 위치로 돌립니다.
        ObjectAnimator animator = ObjectAnimator.ofFloat(input_bar, "translationY", translationY);
        animator.setDuration(100); // 애니메이션의 지속 시간을 설정합니다.

        // 키보드가 사라지고 애니메이션이 완료된 후에 원래 위치로 돌려줍니다.
        if (!up) {
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    input_bar.setTranslationY(0);
                }
            });
        }

        animator.start();
    }

    private int inputBarHeight() {
        return input_bar.getHeight();
    }

    private void scrollToBottom() {
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
            }
        }, 200);
    }

    public void onDestroy() {
        super.onDestroy();
        txtTitle.setVisibility(View.GONE);
        // 또는 txtTitle.setText("");
    }

}