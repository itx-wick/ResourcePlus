package com.mr_w.resourceplus.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.activities.call_info.CallInfoActivity;
import com.mr_w.resourceplus.model.CallLogs;
import com.mr_w.resourceplus.model.users.Users;

import java.util.ArrayList;
import java.util.List;

public class CallListAdapter extends RecyclerView.Adapter<CallListAdapter.Holder> implements Filterable {

    private Context context;
    private List<CallLogs> callLogs;
    private List<CallLogs> callLogsFull;
    private Users me;

    public CallListAdapter(Context context, List<CallLogs> callLogs) {
        this.context = context;
        this.callLogs = callLogs;
        callLogsFull = new ArrayList<>(callLogs);
    }

    public void setMe(Users me) {
        this.me = me;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.call_row_item, parent, false);
        return new Holder(view);
    }

    @SuppressLint("NewApi")
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        final CallLogs item = callLogs.get(position);

        if (me.getPhoneId().equals(item.getSender().getPhoneId())) {

            if (item.getReceiver().getPhoto().equals("")) {
                holder.profile.setImageResource(R.drawable.icon_male_ph);  // set  default image when profile user is null
            } else {
                Glide.with(context).load(item.getReceiver().getPhoto()).into(holder.profile);
            }

            holder.tvName.setText(item.getReceiver().getName());
            holder.tvDate.setText(item.getDateTime());
            if (item.getCallCategory().equals("audio"))
                holder.call_category.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_call));
            else
                holder.call_category.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_videocall));
            if (item.isMissed()) {
                holder.call_mode.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_incomming_call));
                holder.call_mode.getDrawable().setTint(ContextCompat.getColor(context, android.R.color.holo_red_dark));
            } else {
                holder.call_mode.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_outgoing_call));
                holder.call_mode.getDrawable().setTint(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            }
        } else {

            if (item.getSender().getPhoto().equals("")) {
                holder.profile.setImageResource(R.drawable.icon_male_ph);  // set  default image when profile user is null
            } else {
                Glide.with(context).load(item.getSender().getPhoto()).into(holder.profile);
            }

            holder.tvName.setText(item.getSender().getName());
            holder.tvDate.setText(item.getDateTime());
            if (item.getCallCategory().equals("audio"))
                holder.call_category.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_call));
            else
                holder.call_category.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_videocall));
            if (item.isMissed()) {
                holder.call_mode.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_incomming_call));
                holder.call_mode.getDrawable().setTint(ContextCompat.getColor(context, android.R.color.holo_red_dark));
            } else {
                holder.call_mode.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_incomming_call));
                holder.call_mode.getDrawable().setTint(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            }
        }
        holder.itemView.setOnClickListener(v -> context.startActivity(new Intent(context, CallInfoActivity.class)));
    }

    @Override
    public int getItemCount() {
        return callLogs.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        private TextView tvName, tvDate;
        private ImageView profile;
        private ImageView call_mode, call_category;

        public Holder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvName = itemView.findViewById(R.id.tv_name);
            profile = itemView.findViewById(R.id.image_profile);
            call_mode = itemView.findViewById(R.id.call_mode);
            call_category = itemView.findViewById(R.id.call_category);
        }
    }

    @Override
    public Filter getFilter() {
        return callFilter;
    }

    private final Filter callFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<CallLogs> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(callLogsFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (CallLogs item : callLogsFull) {
                    if (item.getSender().getName().toLowerCase().contains(filterPattern) || item.getReceiver().getName().toLowerCase().contains(filterPattern)) {
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
            callLogs.clear();
            callLogs.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };


}
