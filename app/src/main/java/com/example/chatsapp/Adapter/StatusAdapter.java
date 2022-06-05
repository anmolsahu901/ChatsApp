package com.example.chatsapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatsapp.MainActivity;
import com.example.chatsapp.Modals.StatusModal;
import com.example.chatsapp.Modals.UserStatusModal;
import com.example.chatsapp.R;
import com.example.chatsapp.databinding.StatusRvItemBinding;

import java.util.ArrayList;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.ViewHolder> {

    Context context;
    ArrayList<UserStatusModal> userStatues;


    public StatusAdapter(Context context, ArrayList<UserStatusModal> userStatues) {
        this.context = context;
        this.userStatues = userStatues;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.status_rv_item,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        UserStatusModal userStatus = userStatues.get(position);

        StatusModal laststatus = userStatus.getStatuses().get(userStatus.getStatuses().size()-1);
        Glide.with(context)
                .load(laststatus.getImgUrl())
                .into(holder.binding.image);

        holder.binding.circularStatusView.setPortionsCount(userStatus.getStatuses().size());

        holder.binding.circularStatusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<MyStory> myStories = new ArrayList<>();

                for (StatusModal status: userStatus.getStatuses()) {
                    myStories.add(new MyStory(status.getImgUrl()));
                }

                new StoryView.Builder(((MainActivity)context).getSupportFragmentManager())
                        .setStoriesList(myStories) // Required
                        .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                        .setTitleText(userStatus.getName()) // Default is Hidden
                        .setSubtitleText("") // Default is Hidden
                        .setTitleLogoUrl(userStatus.getProfileImg()) // Default is Hidden
                        .setStoryClickListeners(new StoryClickListeners() {
                            @Override
                            public void onDescriptionClickListener(int position) {
                                //your action
                            }

                            @Override
                            public void onTitleIconClickListener(int position) {
                                //your action
                            }
                        }) // Optional Listeners
                        .build() // Must be called before calling show method
                        .show();


            }
        });

    }

    @Override
    public int getItemCount() {
        return userStatues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        StatusRvItemBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = StatusRvItemBinding.bind(itemView);
        }
    }

}
