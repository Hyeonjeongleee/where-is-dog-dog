package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.Chat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
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
    private String nick;

    private EditText EditText_chat;
    private Button Button_send;
    private Button Button_finish;
    private DatabaseReference myRef;
    private LinearLayout input_bar;
    private TextView txtTitle;
    private String currentUserUid;
    private String receiverUid;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 인수 번들로부터 친구 UID를 가져옵니다

//        RecyclerView recyclerView = findViewById(R.id.recyclerView);
//        MyAdapter adapter = new MyAdapter();
//        recyclerView.setAdapter(adapter);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // set myUid and receiverUid
        currentUserUid = MainActivity.userUid;
        if (getArguments() != null) {
            receiverUid = getArguments().getString("friendUid");
        }
        Log.d("FriendUid", receiverUid);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String[] emailParts = user.getEmail().split("@");
            nick = emailParts[0]; // "@" 기호 이전 부분을 닉네임으로 설정합니다.
            //currentUserUid = user.getUid();
            currentUserUid = MainActivity.userUid;
            receiverUid = receiverUid;
        } else {
            // 사용자가 로그인하지 않은 경우 처리할 내용
        }

    }
//    private void openChatFragment() {
//        String friendUid = "55IfHb9JmBh2f0d878iNIv8O3ST2"; // 상대방의 UID를 설정해주세요
//        ChatFragment chatFragment = new ChatFragment();
//
//        // 인수 번들에 친구 UID를 전달합니다
//        Bundle bundle = new Bundle();
//        bundle.putString("friendUid", friendUid);
//        chatFragment.setArguments(bundle);
//
//        requireActivity().getSupportFragmentManager().beginTransaction()
//                .replace(R.id.main_frame, chatFragment)
//                .addToBackStack(null)
//                .commit();
//    }
    private void printExistingChats() {
        myRef.child(currentUserUid).child("messages").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ChatData chat = dataSnapshot.getValue(ChatData.class);

                        // 메시지가 현재 사용자에 의해 보내졌는지 또는 상대방에 의해 보내졌는지를 확인합니다.
                        if (chat.getNickname().equals(nick)) {
                            chat.setSentByMe(true); // 현재 사용자가 보낸 메시지
                        } else {
                            chat.setSentByMe(false); // 상대방이 보낸 메시지
                        }

                        ((ChatAdapter) mAdapter).addChat(chat);
                        System.out.println("대화내용: " + chat.getMsg()); // 대화 내용 출력
                    }
                    scrollToBottom(); // 채팅의 맨 아래로 스크롤합니다.

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 에러 처리
            }
        });
    }
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.activity_chat, container, false);

            currentUserUid = MainActivity.userUid;
            // 번들로 넘어온 uid를 receiverUid에 대입
            if (getArguments() != null) {
                receiverUid = getArguments().getString("friendUid");
                System.out.println("ChatFragment_FriendUid: {}".format(receiverUid));
            }

            Button_send = view.findViewById(R.id.Button_send);
            EditText_chat = view.findViewById(R.id.EditText_chat);
            txtTitle = view.findViewById(R.id.txt_TItle);
            Button_finish = view.findViewById(R.id.finish_button);

            FirebaseApp.initializeApp(requireContext());

            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                String[] emailParts = user.getEmail().split("@");
                nick = emailParts[0]; // "@" 기호 이전 부분을 닉네임으로 설정합니다.

            } else {
                // 사용자가 로그인하지 않은 경우 처리할 내용
            }

            Button_finish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
                    ad.setIcon(R.drawable.sad_tear);
                    ad.setTitle("잘가라 멍!");
                    ad.setMessage("채팅방이 사라지고, 대화 내용이 삭제됩니다.");

                    ad.setPositiveButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    ad.setNegativeButton("나가기", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FirebaseDatabase database1 = FirebaseDatabase.getInstance();
                            DatabaseReference setMessage1 = database1.getReference("users")
                                    .child(receiverUid)
                                    .child("messages")
                                    .child(currentUserUid);

                        // 내 'messages' Reference에 상대방 uid 생성
                            FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                            DatabaseReference setMessage2 = database2.getReference("users")
                                    .child(currentUserUid)
                                    .child("messages")
                                    .child(receiverUid);

                        // 데이터 삭제
                            setMessage1.removeValue();
                            setMessage2.removeValue();

                        // 이전 Fragment로 돌아가기
                            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                            fragmentManager.popBackStack();
                            dialogInterface.dismiss();
                        }
                    });
                    ad.show();
                }
            });

            Button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = EditText_chat.getText().toString();

                if (!msg.isEmpty()) {
                    ChatData chat = new ChatData();
                    chat.setNickname(nick);
                    chat.setMsg(msg);
                    chat.setSentByMe(true); // 내가 보낸 메시지인 경우 true로 설정
                    myRef.push().setValue(chat);
                    EditText_chat.setText("");

                    // 상대방의 DB에도 메시지 저장
                    ChatData friendChat = new ChatData();
                    friendChat.setNickname(nick);
                    friendChat.setMsg(msg);
                    friendChat.setSentByMe(false); // 상대방이 보낸 메시지인 경우 false로 설정
                    //String receiverUid = receiverUid; // 상대 사용자의 uid를 지정해야 합니다.
                    DatabaseReference friendRef = FirebaseDatabase.getInstance().getReference("users")
                            .child(receiverUid)
                            .child("messages")
                            .child(currentUserUid);
                    friendRef.push().setValue(friendChat);
                    printExistingChats();
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
        //FirebaseDatabase database = FirebaseDatabase.getInstance();
        //myRef = database.getReference("message");
        //FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //String receiverUid = "hE6anJk9oINffKoKMvjGxbAAuRw2";
        //String currentUserUid = currentUser.getUid();
        myRef = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUserUid)
                .child("messages")
                .child(receiverUid);;

        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("CHATCHAT", dataSnapshot.getValue().toString());
                ChatData chat = dataSnapshot.getValue(ChatData.class);

                ((ChatAdapter) mAdapter).addChat(chat);
                scrollToBottom(); // 가장 하단으로 스크롤
                printExistingChats();

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