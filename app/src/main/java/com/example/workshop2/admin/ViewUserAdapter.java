package com.example.workshop2.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop2.R;
import com.example.workshop2.model.User;

import java.util.ArrayList;

public class ViewUserAdapter extends RecyclerView.Adapter<ViewUserAdapter.ViewUserViewHolder> {

    private Context context;
    private ArrayList<User> userList;

    public ViewUserAdapter(Context context, ArrayList<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.users_item, parent, false);
        return new ViewUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewUserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.userNameTextView.setText("Name: " + user.getFullName());
        holder.userEmailTextView.setText("Email: " + user.getEmail());
        holder.userTypeTextView.setText("Role: " + user.getUserType());
        holder.orgNameTextView.setText("Organization: " + user.getOrgName());
        holder.positionTextView.setText("Position: " + user.getPosition());
        holder.phoneTextView.setText("Phone: " + user.getPhoneNumber());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewUserViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView, userEmailTextView, userTypeTextView, orgNameTextView, positionTextView, phoneTextView;

        public ViewUserViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            userEmailTextView = itemView.findViewById(R.id.userEmailTextView);
            userTypeTextView = itemView.findViewById(R.id.userTypeTextView);
            orgNameTextView = itemView.findViewById(R.id.orgNameTextView);
            positionTextView = itemView.findViewById(R.id.positionTextView);
            phoneTextView = itemView.findViewById(R.id.phoneTextView);
        }
    }
}
