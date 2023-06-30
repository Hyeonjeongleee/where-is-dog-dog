package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.Chat;
import com.example.myapplication.ChatListAdapter;
import com.example.myapplication.ChatFragment;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatListAdapter chatListAdapter;
    private List<Chat> chatList;
    private DatabaseReference usersRef;
    private String nick;

    public ChatListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_chatlist, container, false);

        recyclerView = view.findViewById(R.id.chat_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Get the reference to the "users" node in the Firebase Realtime Database
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Get the nickname of the currently logged-in user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Retrieve the nickname from the database based on the user's ID
            usersRef.child(userId).child("nick").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        nick = dataSnapshot.getValue(String.class);

                        // Update the chat list with the retrieved nickname
                        updateChatListWithNickname();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error if needed
                }
            });
        }

        chatList = new ArrayList<>();
        // Add placeholder items to the chat list
        chatList.add(new Chat("닉넴1", "채팅채팅"));
        chatList.add(new Chat("닉넴2", "채팅채팅"));
        chatList.add(new Chat("닉넴3", "채팅채팅"));

        chatListAdapter = new ChatListAdapter(getActivity(), chatList);
        recyclerView.setAdapter(chatListAdapter);

        // Add click listener for chat list items
        chatListAdapter.setOnItemClickListener(new ChatListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Handle click on chat item
                openChatFragment();
            }
        });

        return view;
    }

    private void updateChatListWithNickname() {
        // Update the chat list with the retrieved nickname
        for (Chat chat : chatList) {
            chat.setNickname(nick);  // 상대방의 이메일 주소를 채팅 목록에 설정
        }

        // Notify the adapter that the data has changed
        chatListAdapter.notifyDataSetChanged();
    }



    private void openChatFragment() {
        ChatFragment chatFragment = new ChatFragment();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frame, chatFragment)
                .addToBackStack(null)
                .commit();
    }

    public void onDestroy() {
        super.onDestroy();
        if (chatListAdapter != null) {
            chatListAdapter.setOnItemClickListener(null);
        }
    }
}
