package com.mr_w.resourceplus.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.database.Database;
import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.model.MessageData;
import com.mr_w.resourceplus.model.UnreadBadge;
import com.mr_w.resourceplus.model.users.Users;
import com.mr_w.resourceplus.storage.UserPreferences;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.Holder> implements Filterable {

    private Context context;
    SendChatData sendChatData;
    RemoveItem removeItem;
    Selection selection;
    ViewUser viewUser;
    List<UnreadBadge> unReads = new ArrayList<>();
    private List<Conversation> list;
    private List<Conversation> chatListFull;
    private List<Conversation> selections = new ArrayList<>();
    String typing;
    String phoneId;
    private Database db;
    private DataManager userPrefs;


    public void setPhoneId(String phoneId) {
        this.phoneId = phoneId;
    }

    public List<UnreadBadge> getUnReads() {
        return unReads;
    }

    public void setUnReads(List<UnreadBadge> unReads) {
        this.unReads = unReads;
    }

    public List<Conversation> getSelections() {
        return selections;
    }

    public void setSelections(List<Conversation> selections) {
        this.selections = selections;
    }

    public String getTyping() {
        return typing;
    }

    public void setTyping(String typing) {
        this.typing = typing;
    }

    public ViewUser getViewUser() {
        return viewUser;
    }

    public void setViewUser(ViewUser viewUser) {
        this.viewUser = viewUser;
    }

    public RemoveItem getRemoveItem() {
        return removeItem;
    }

    public void setRemoveItem(RemoveItem removeItem) {
        this.removeItem = removeItem;
    }

    public List<Conversation> getList() {
        return list;
    }

    public void setList(List<Conversation> list) {
        this.list = list;
    }

    public void setSelection(Selection selection) {
        this.selection = selection;
    }

    public interface SendChatData {
        void performClick(Conversation conversation);

        void performLongClick(Conversation conversation);
    }

    public interface RemoveItem {
        void updateView();
    }

    public void setUserPrefs(DataManager userPrefs) {
        this.userPrefs = userPrefs;
    }

    public void setSendChatData(SendChatData sendChatData) {
        this.sendChatData = sendChatData;
    }

    public ChatListAdapter(Context context, List<Conversation> list) {
        this.context = context;
        this.list = list;
        chatListFull = new ArrayList<>(list);
        db = Database.getInstance(context);
    }


    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_row_item, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        if (list.size() == 0) {
            removeItem.updateView();
            return;
        }
        final Conversation conversation = list.get(position);

        if (selections.size() > 0) {
            if (selections.get(position).get_id().equals(conversation.get_id())) {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSelection));
                holder.select.setVisibility(View.VISIBLE);
            }
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            holder.select.setVisibility(View.GONE);
        }

        if (unReads.size() > 0) {
            for (UnreadBadge badge : unReads) {
                if (badge.getPosition() == position && badge.getCount() > 0) {
                    holder.unreadCount.setVisibility(View.VISIBLE);
                    holder.unreadCount.setText(String.valueOf(badge.getCount()));
                    break;
                } else
                    holder.unreadCount.setVisibility(View.GONE);
            }
        } else holder.unreadCount.setVisibility(View.GONE);

        if (!selections.contains(conversation)) {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            holder.select.setVisibility(View.GONE);
        }

        holder.tvDesc.setTextColor(Color.parseColor("#2E2E2E"));
        holder.type.setVisibility(View.VISIBLE);
        if (conversation.getType().equals("one-to-one")) {
            for (Users member : conversation.getMembers()) {
                if (!member.getPhoneId().equals(userPrefs.getUserDetails().getPhoneId())) {

                    if (db.isContactExists(member.getPhoneNo()))
                        holder.tvName.setText(db.getContact(member.getPhoneNo()).getName());
                    else
                        holder.tvName.setText(member.getPhoneNo());

                    if (member.getPhoto() != null && !member.getPhoto().equals("") && !member.getPhoto().equals("null")) {
                        Glide.with(context).load(member.getPhoto()).placeholder(R.drawable.no_pic).into(holder.profile);
                    } else
                        holder.profile.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.no_pic));

                    break;
                }
            }
        } else {
            holder.tvName.setText(conversation.getTitle());

            if (conversation.getImage() != null && !conversation.getImage().equals("") && !conversation.getImage().equals("null")) {
                Glide.with(context).load(conversation.getImage()).into(holder.profile);
            } else
                holder.profile.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.no_pic));

        }
        if (conversation.getMessage() != null) {

            String senderPhoneId = conversation.getMessage().getSenderPhoneId();
            if (conversation.getMessage().getType().equals(MessageData.TYPE.VIDEO)) {
                holder.tvDesc.setText("Video");
                holder.type.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_video));

                if (!phoneId.equals(senderPhoneId))
                    holder.status.setVisibility(View.GONE);
            } else if (conversation.getMessage().getType().equals(MessageData.TYPE.GIF)) {
                holder.tvDesc.setText("GIF");
                holder.type.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.gif));

                if (!phoneId.equals(senderPhoneId))
                    holder.status.setVisibility(View.GONE);
            } else if (conversation.getMessage().getType().equals(MessageData.TYPE.AUDIO)) {
                holder.tvDesc.setText("Audio");
                holder.type.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_audio));

                if (!phoneId.equals(senderPhoneId))
                    holder.status.setVisibility(View.GONE);
            } else if (conversation.getMessage().getType().equals(MessageData.TYPE.PICTURE)) {
                holder.tvDesc.setText("Photo");
                holder.type.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_image));

                if (!phoneId.equals(senderPhoneId))
                    holder.status.setVisibility(View.GONE);
            } else if (conversation.getMessage().getType().equals(MessageData.TYPE.DOCUMENTS)) {
                holder.tvDesc.setText("Document");
                holder.type.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_document));

                if (!phoneId.equals(senderPhoneId))
                    holder.status.setVisibility(View.GONE);
            } else if (conversation.getMessage().getType().equals(MessageData.TYPE.INFO)) {
                holder.tvDesc.setText("Info");
                holder.type.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_info));
//                if (userPreferences.getUserDetails().getPhoneId().equals(conversation.getMessage().getSenderPhoneId()))
//                    holder.status.setVisibility(View.VISIBLE);
            } else if (conversation.getMessage().getType().equals(MessageData.TYPE.CONTACT)) {
                holder.tvDesc.setText("Contact");
                holder.type.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_contact));
//                if (userPreferences.getUserDetails().getPhoneId().equals(conversation.getMessage().getSenderPhoneId()))
//                    holder.status.setVisibility(View.VISIBLE);
            } else {
                holder.type.setVisibility(View.GONE);
                holder.tvDesc.setText(conversation.getMessage().getText());
            }

            if (!phoneId.equals(senderPhoneId))
                holder.status.setVisibility(View.GONE);
            else if (conversation.getMessage().getStatus().equals("sending") || conversation.getMessage().getStatus().equals("cancelled")) {
                holder.status.setVisibility(View.VISIBLE);
                holder.status.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_error));
                holder.status.setColorFilter(ContextCompat.getColor(context, R.color.black), PorterDuff.Mode.SRC_IN);
            }

            DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = null;
            try {
                date = utcFormat.parse(conversation.getMessage().getDateTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            DateFormat pstFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            pstFormat.setTimeZone(TimeZone.getDefault());
            DateFormat format = new SimpleDateFormat("dd-MM-yyyy");
            pstFormat.setTimeZone(TimeZone.getDefault());

            if (isToday(pstFormat.format(date)))
                holder.tvDate.setText(getTime(pstFormat.format(date)));
            else
                holder.tvDate.setText(format.format(date));

            switch (conversation.getMessage().getStatus()) {
                case "send":
                    holder.status.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_single_check));
                    break;
                case "delivered":
                    holder.status.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_double_check));
                    break;
                case "seen":
                    holder.status.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_double_check));
                    holder.status.setColorFilter(ContextCompat.getColor(context, R.color.primary), PorterDuff.Mode.SRC_IN);
                    break;
            }

        } else {

            holder.status.setVisibility(View.GONE);
            holder.type.setVisibility(View.GONE);

            DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = null;
            try {
                date = utcFormat.parse(conversation.getUpdatedAt());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            DateFormat pstFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            pstFormat.setTimeZone(TimeZone.getDefault());
            DateFormat format = new SimpleDateFormat("dd-MM-yyyy");
            pstFormat.setTimeZone(TimeZone.getDefault());

            if (isToday(pstFormat.format(date)))
                holder.tvDate.setText(getTime(pstFormat.format(date)));
            else
                holder.tvDate.setText(format.format(date));
        }

        if (typing != null) {
            holder.tvDesc.setText(typing);
            holder.tvDesc.setTextColor(ContextCompat.getColor(context, R.color.primary));
            holder.type.setVisibility(View.GONE);
            holder.status.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selections.contains(conversation)) {
                    holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                    selections.remove(conversation);
                    holder.select.setVisibility(View.GONE);
                    selection.onRemove();
                } else if (selections.size() > 0) {
                    holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSelection));
                    selections.add(conversation);
                    holder.select.setVisibility(View.VISIBLE);
                    selection.onSelect();
                } else
                    sendChatData.performClick(conversation);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!selections.contains(conversation)) {
                    holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSelection));
                    selections.add(conversation);
                    sendChatData.performLongClick(conversation);
                    holder.select.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });

        holder.profile.setOnClickListener(v -> {
            if (selections.size() == 0)
                viewUser.view(position);
            else if (!selections.contains(conversation)) {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSelection));
                selections.add(conversation);
                holder.select.setVisibility(View.VISIBLE);
                selection.onSelect();
            } else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                selections.remove(conversation);
                holder.select.setVisibility(View.GONE);
                selection.onRemove();
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public static class Holder extends RecyclerView.ViewHolder {
        private TextView tvName, tvDesc, tvDate, unreadCount;
        private ImageView status, type;
        private ImageView profile;
        private CardView select;
        private FrameLayout frameLayout;

        public Holder(@NonNull View itemView) {
            super(itemView);

            frameLayout = itemView.findViewById(R.id.frame);
            select = itemView.findViewById(R.id.select);
            status = itemView.findViewById(R.id.status);
            type = itemView.findViewById(R.id.msgType);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvDesc = itemView.findViewById(R.id.tv_desc);
            tvName = itemView.findViewById(R.id.tv_name);
            profile = itemView.findViewById(R.id.image_profile);
            unreadCount = itemView.findViewById(R.id.unread_count);
        }
    }

    @Override
    public Filter getFilter() {
        return chatListFilter;
    }

    private final Filter chatListFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Conversation> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(chatListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Conversation item : chatListFull) {
                    if (item.getTitle().toLowerCase().contains(filterPattern)) {
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

    @SuppressLint("SimpleDateFormat")
    public static boolean isToday(String date) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date date1 = null;
        try {
            date1 = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date1);
            return DateUtils.isToday(cal.getTimeInMillis());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getTime(String createdAt) {
        SimpleDateFormat pstFormat = new SimpleDateFormat("h:mm aa");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date date = null;
        try {
            date = format.parse(createdAt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return pstFormat.format(date);
    }

    public interface ViewUser {
        void view(int pos);
    }

    public interface Selection {
        void onRemove();

        void onSelect();
    }

}
