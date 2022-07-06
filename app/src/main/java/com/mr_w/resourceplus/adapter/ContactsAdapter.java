package com.mr_w.resourceplus.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.activities.select_contact.SelectContactActivity;
import com.mr_w.resourceplus.database.Database;
import com.mr_w.resourceplus.model.users.Users;
import com.mr_w.resourceplus.storage.UserPreferences;

import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> implements Filterable {

    Context context;
    List<Users> list;
    List<Users> filteredListFull;
    SendData sendData;
    Click click;
    Activity activity;
    Users me;
    List<Users> selectedUsers = new ArrayList<>();
    private Database db;

    public void setMe(Users me) {
        this.me = me;
    }

    public List<Users> getSelectedUsers() {
        return selectedUsers;
    }

    public void setSelectedUsers(List<Users> selectedUsers) {
        this.selectedUsers = selectedUsers;
    }

    public interface SendData {
        void performClick(Users userObj, int pos);
    }

    public interface Click {
        void unClick(int pos);
    }

    public void setClick(Click click) {
        this.click = click;
    }

    public void setSendData(SendData sendData) {
        this.sendData = sendData;
    }

    public ContactsAdapter(Context context, List<Users> list, Activity activity) {
        this.context = context;
        this.list = list;
        filteredListFull = new ArrayList<>(list);
        this.activity = activity;
        db = Database.getInstance(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_row_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Users user = list.get(position);

        if (selectedUsers != null) {
            if (selectedUsers.size() > 0) {
                for (int i = 0; i < selectedUsers.size(); i++) {
                    if (user.getPhoneId().equals(selectedUsers.get(i).getPhoneId())) {
                        holder.select.setVisibility(View.VISIBLE);
                        break;
                    }
                }
            }
        }

        if (db.isContactExists(user.getPhoneNo()))
            holder.contactName.setText(db.getContact(user.getPhoneNo()).getName());
        else
            holder.contactName.setText(user.getPhoneNo());
        if (user.getUserId() != null && user.getUserId().equals("Already added to group"))
            holder.contactNumber.setText(user.getUserId());
        else
            holder.contactNumber.setText(user.getPhoneNo());

        if (user.getPhoto() != null && !user.getPhoto().equals("") && !user.getPhoto().equals("null")) {
            Glide.with(activity).load(user.getPhoto()).into(holder.contactProfile);
        } else {
            holder.contactProfile.setImageResource(R.drawable.no_pic);
        }

        if (user.getUserId() != null && user.getUserId().equals("Already added to group")) {
            holder.root.setBackgroundColor(Color.parseColor("#eeeeee"));
        } else {

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!me.getPhoneId().equals(user.getPhoneId())) {
                        if (activity instanceof SelectContactActivity) {
                            if (holder.select.getVisibility() == View.GONE) {
                                holder.select.setVisibility(View.VISIBLE);
                                sendData.performClick(user, position);
                            } else {
                                holder.select.setVisibility(View.GONE);
                                click.unClick(position);
                            }
                        } else {
                            sendData.performClick(user, position);
                        }
                    } else
                        Toast.makeText(context, "Can not add you", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setList(List<Users> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return contactListFilter;
    }

    private final Filter contactListFilter = new Filter() {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Users> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(filteredListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Users item : filteredListFull) {
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            list.clear();
            list.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView contactProfile;
        private TextView contactName, contactNumber;
        private LinearLayout root;
        private CardView select;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            contactProfile = itemView.findViewById(R.id.contact_profile_img);
            contactName = itemView.findViewById(R.id.tv_contact_name);
            contactNumber = itemView.findViewById(R.id.tv_contact_number);

            root = itemView.findViewById(R.id.root);
            select = itemView.findViewById(R.id.select);
        }
    }
}

