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

import java.util.ArrayList;
import java.util.List;


public class ChatListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatListAdapter chatListAdapter;
    private List<String> chatList;

    public ChatListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_chatlist, container, false);

        recyclerView = view.findViewById(R.id.chat_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        chatList = new ArrayList<>();
        chatList.add("Chat 1");
        chatList.add("Chat 2");
        chatList.add("Chat 3");

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
