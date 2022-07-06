package com.mr_w.resourceplus.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.database.Database;
import com.mr_w.resourceplus.model.Participant;
import com.mr_w.resourceplus.model.users.Users;
import com.mr_w.resourceplus.storage.UserPreferences;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.ViewHolder> implements Filterable {

    private Activity context;
    private List<Participant> participants;
    private ClickListener clickListener;
    private ParticipantsOperations po;
    private Users me;
    private String creator;
    private List<Participant> filteredListFull;
    public static Database db;

    public ParticipantsAdapter(Activity context, List<Participant> participants, ClickListener clickListener) {
        this.context = context;
        this.participants = participants;
        this.clickListener = clickListener;
        filteredListFull = new ArrayList<>(participants);
        db = Database.getInstance(context);
    }

    public void setMe(Users me) {
        this.me = me;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setPo(ParticipantsOperations po) {
        this.po = po;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.participant_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantsAdapter.ViewHolder holder, int position) {
        Participant participant = participants.get(position);

        if (participant.getUser().getPhoto() != null && !participant.getUser().getPhoto().equals("") && !participant.getUser().getPhoto().equals("null")) {
            Glide.with(context).load(participant.getUser().getPhoto()).into(holder.image);
        }

        if (participant.getUser().getPhoneId().equals(me.getPhoneId())) {
            holder.title.setText("You");
        } else {
            if (db.getContact(participant.getUser().getPhoneNo()) != null)
                holder.title.setText(db.getContact(participant.getUser().getPhoneNo()).getName());
            else
                holder.title.setText(participant.getUser().getPhoneNo());
        }
        holder.about.setText(participant.getUser().getAbout());
        if (!participant.isAdmin())
            holder.adminCard.setVisibility(View.GONE);
        else
            holder.adminCard.setVisibility(View.VISIBLE);

        if (!participant.getUser().getPhoneId().equals(creator))
            holder.creatorCard.setVisibility(View.GONE);
        else
            holder.creatorCard.setVisibility(View.VISIBLE);

        holder.root.setOnClickListener(v -> {
            clickListener.onClick(position, participant, v);
        });

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    @Override
    public Filter getFilter() {
        return participantListFilter;
    }

    private final Filter participantListFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Participant> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(filteredListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Participant item : filteredListFull) {
                    if (db.isContactExists(item.getUser().getPhoneNo())) {
                        if (db.getContact(item.getUser().getPhoneNo()).getName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(item);
                        }
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results != null) {
                participants.clear();
                participants.addAll((List) results.values);
                notifyDataSetChanged();
            }
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout root;
        ImageView image;
        TextView title, about;
        TextView adminCard, creatorCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            root = itemView.findViewById(R.id.root);
            image = itemView.findViewById(R.id.image);
            title = itemView.findViewById(R.id.title);
            about = itemView.findViewById(R.id.allMembers);
            adminCard = itemView.findViewById(R.id.adminCard);
            creatorCard = itemView.findViewById(R.id.creatorCard);
        }
    }

    public interface ClickListener {
        void onClick(int pos, Participant participant, View v);
    }

    public interface ParticipantsOperations {
        void message(int pos);

        void view(int pos);

        void add(int pos);
    }

}
