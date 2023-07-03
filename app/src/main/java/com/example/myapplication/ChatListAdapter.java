package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.myapplication.Chat;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    private Context context;
    private List<Chat> chatList;
    private OnItemClickListener listener;

    public ChatListAdapter(Context context, List<Chat> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chatItem = chatList.get(position);
        holder.bind(chatItem, listener);
    }


    @Override
    public int getItemCount() {
        if (chatList == null) {
            return 0;
        }
        return chatList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView chatNameTextView;
        private TextView chatNicknameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chatNameTextView = itemView.findViewById(R.id.chat_name_text_view);
            chatNicknameTextView = itemView.findViewById(R.id.chat_nickname_text_view);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }

        public void bind(Chat chatItem, OnItemClickListener listener) {
            chatNameTextView.setText(chatItem.getChatName());
            chatNicknameTextView.setText(chatItem.getNickname());
            chatNicknameTextView.setTextColor(context.getColor(android.R.color.black));
        }

    }
}