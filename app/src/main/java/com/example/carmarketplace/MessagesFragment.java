
package com.example.carmarketplace;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MessagesFragment extends Fragment {
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        LinearLayout messagesContainer = view.findViewById(R.id.messagesContainer);

        addMessage(inflater, messagesContainer, "Marcus Velocity", "Is the Toyota RAV4 still available?", "2m");
        addMessage(inflater, messagesContainer, "Velocity Support", "Welcome to your marketplace inbox.", "1h");
        addMessage(inflater, messagesContainer, "Sarah", "Can we arrange a viewing?", "3h");

        return view;
    }

    private void addMessage(LayoutInflater inflater, LinearLayout container, String name, String msg, String time) {
        View row = inflater.inflate(R.layout.item_message, container, false);

        TextView senderName = row.findViewById(R.id.senderName);
        TextView messageText = row.findViewById(R.id.messageText);
        TextView messageTime = row.findViewById(R.id.messageTime);

        senderName.setText(name);
        messageText.setText(msg);
        messageTime.setText(time);

        container.addView(row);
    }
}
