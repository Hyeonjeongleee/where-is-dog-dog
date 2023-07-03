package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatListAdapter chatListAdapter;
    private List<Chat> chatList;
    private DatabaseReference usersRef;
    private String nick;
    private String currUserUid;
    private String friendUid;
    private String dogName;
    private DatabaseReference friendReference;
    private DatabaseReference friendDogRef;


    public ChatListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_chatlist, container, false);

        recyclerView = view.findViewById(R.id.chat_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        chatList = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(getActivity(), chatList);
        recyclerView.setAdapter(chatListAdapter);

        // Get the reference to the "users" node in the Firebase Realtime Database
        currUserUid = MainActivity.userUid;
        if (currUserUid != null) {
            friendReference = FirebaseDatabase.getInstance().getReference("users")
                    .child(currUserUid)
                    .child("messages");

            friendReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                        String friendUid = messageSnapshot.getKey();
                        System.out.println("FriendUid: " + friendUid);
                        chatList.add(new Chat(friendUid, "..."));
                    }
                    chatListAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error if needed
                }
            });
        }

        // Get the nickname of the currently logged-in user
        if (currUserUid != null) {
            String userId = currUserUid;

            // Retrieve the nickname from the database based on the user's ID
            usersRef = FirebaseDatabase.getInstance().getReference("users").child(currUserUid);
            usersRef.child("dog").child("nick").addListenerForSingleValueEvent(new ValueEventListener() {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (chatListAdapter != null) {
            chatListAdapter.setOnItemClickListener(null);
        }
    }

}
