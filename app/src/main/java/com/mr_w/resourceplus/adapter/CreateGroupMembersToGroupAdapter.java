package com.mr_w.resourceplus.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.database.Database;
import com.mr_w.resourceplus.model.groups.GroupMembers;
import com.mr_w.resourceplus.model.users.Users;

import java.util.List;

public class CreateGroupMembersToGroupAdapter extends RecyclerView.Adapter<CreateGroupMembersToGroupAdapter.ViewHolder> {

    private List<Users> users;
    Context context;
    RemoveItem removeItem;
    private Database db;

    public RemoveItem getRemoveItem() {
        return removeItem;
    }

    public void setRemoveItem(RemoveItem removeItem) {
        this.removeItem = removeItem;
    }

    public interface RemoveItem {
        void updateView(int position);
    }

    public CreateGroupMembersToGroupAdapter(List<Users> members, Context context) {
        this.users = members;
        this.context = context;
        db = Database.getInstance(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_create_group_members, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Users user = users.get(position);
        if (user.getPhoto() != null && !user.getPhoto().equals("") && !user.getPhoto().equals("null")) {
            Glide.with(context).load(user.getPhoto()).into(holder.memberImage);// set  default image when profile user is null
        } else {
            holder.memberImage.setImageResource(R.drawable.no_pic);
        }
        if (db.isContactExists(user.getPhoneNo()))
            holder.memberName.setText(db.getContact(user.getPhoneNo()).getName());
        else
            holder.memberName.setText(user.getName());

        holder.removeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItem.updateView(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView memberImage;
        private TextView memberName;
        private CardView removeIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            memberImage = itemView.findViewById(R.id.memberImage);
            memberName = itemView.findViewById(R.id.memberName);
            removeIcon = itemView.findViewById(R.id.remove_item);
        }
    }

}
