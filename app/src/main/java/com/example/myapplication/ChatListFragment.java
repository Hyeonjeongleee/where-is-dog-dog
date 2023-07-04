package com.example.myapplication;
import com.example.myapplication.ChatFragment; // ChatFragment 클래스의 import 문 추가
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
import android.os.Bundle;
import android.util.Log;
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
    private DatabaseReference friendReference;
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
                    //chatList.clear();
                    chatListAdapter.clearChatList();
                    for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                        String friendUid = messageSnapshot.getKey();
                        System.out.println("FriendUid: " + friendUid);

                        String friendDogName = getFriendDogName(friendUid);

                        //chatList.add(new Chat(friendUid, "..."));
                        chatListAdapter.addChat(new Chat(friendDogName,"test nickname", friendUid));
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
                String chosenFriendUid = chatListAdapter.getFriendUid(position);
                Log.d("ChatListFragment_ItemListener",
                        String.format("POSITION: %d / FRIEND UID: %s", position, chosenFriendUid));
                openChatFragment(chosenFriendUid);
            }
        });

        return view;
    }
    public String getFriendDogName(String friendUid){
        DatabaseReference friendDogRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(friendUid)
                .child("dogs");

        final String[] dogName = {"김멍멍"};

        friendDogRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // iterate the number of dogs
                for(DataSnapshot dogSnapshot: snapshot.getChildren()){
                    String dogKey = dogSnapshot.getKey();
                    System.out.println(String.format("Dog Key of %s: %s", friendUid, dogKey));
                    // 멍멍이 이름 당겨오는 데에서 문제
                    String friendDogName = dogSnapshot.child(dogKey).child("name").getValue(String.class);
                    System.out.println(friendDogName);
                    //dogName[0] = friendDogName;
                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return dogName[0];
    }


    private void updateChatListWithNickname() {
        // Update the chat list with the retrieved nickname
        for (Chat chat : chatList) {
            chat.setNickname(nick);  // 상대방의 이메일 주소를 채팅 목록에 설정
        }

        // Notify the adapter that the data has changed
        chatListAdapter.notifyDataSetChanged();
    }



    private void openChatFragment(String chosenFriendUid) {
        ChatFragment chatFragment = new ChatFragment();
        // Fragment에 번들로 넘기는 코드 짜기
        // 인수 번들에 친구 UID를 전달합니다
        Bundle bundle = new Bundle();
        bundle.putString("friendUid", chosenFriendUid);
        chatFragment.setArguments(bundle);

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
