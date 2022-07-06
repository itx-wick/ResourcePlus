package com.mr_w.resourceplus.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mr_w.resourceplus.ResourcePlusApplication;
import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.model.Link;
import com.mr_w.resourceplus.model.LocalContacts;
import com.mr_w.resourceplus.model.MessageData;
import com.mr_w.resourceplus.model.MessageStatesRecord;
import com.mr_w.resourceplus.model.users.Users;

import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteOpenHelper {
    public static final String DBNAME = "reSource+.db";

    public static final String TABLE_CONVERSATIONS = "conversations";
    public static final String CONVERSATION_ID = "_id";
    public static final String CONVERSATION_TITLE = "title";
    public static final String CONVERSATION_TYPE = "type";
    public static final String CONVERSATION_IMAGE = "image";
    public static final String CONVERSATION_DESCRIPTION = "description";
    public static final String CONVERSATION_ADMINS = "admins";
    public static final String CONVERSATION_MEMBERS = "members";
    public static final String CONVERSATION_LAST_MESSAGE_ID = "last_message";
    public static final String CONVERSATION_CREATED_AT = "created_at";
    public static final String CONVERSATION_UPDATED_AT = "updated_at";
    public static final String CONVERSATION_VERSION = "__v";
    public static final String CONVERSATION_CREATOR = "creator";
    public static final String CONVERSATION_REMOVED_MEMBERS = "removed_members";

    public static final String TABLE_MESSAGES = "messages";
    public static final String MESSAGES_ID = "_id";
    public static final String MESSAGES_TYPE = "type";
    public static final String MESSAGES_URL = "url";
    public static final String MESSAGES_STATUS = "status";
    public static final String MESSAGES_TEXT = "text";
    public static final String MESSAGES_DATE_TIME = "date_time";
    public static final String MESSAGES_SENDER_ID = "sender_id";
    public static final String MESSAGES_CONVERSATION_ID = "conversation_id";
    public static final String MESSAGES_FILE_SIZE = "file_size";
    public static final String MESSAGES_SEEN_COUNT = "seen_count";

    public static final String TABLE_USERS = "users";
    public static final String USERS_ID = "_id";
    public static final String USERS_USERNAME = "username";
    public static final String USERS_PHONE_NUMBER = "phone_number";
    public static final String USERS_IMAGE = "image";
    public static final String USERS_PHONE_ID = "phone_id";
    public static final String USERS_ABOUT = "about";
    public static final String USERS_COMMUNICATIONS = "communications";

    public static final String TABLE_CONTACTS = "contacts";
    public static final String CONTACTS_ID = "_id";
    public static final String CONTACTS_NUMBER = "number";
    public static final String CONTACTS_NAME = "name";
    public static final String CONTACTS_LOOKUP_KEY = "lookup_key";

    public static final String TABLE_LINKS = "links";
    public static final String LINKS_URL = "url";
    public static final String LINKS_TITLE = "title";

    private SQLiteDatabase mDatabase;
    private static Database single_instance = null;

    private Database(Context context) {
        super(context, DBNAME, null, 1);
    }

    public static Database getInstance(Context context) {
//        if (single_instance == null)
        single_instance = new Database(context);

        return single_instance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("create table if not exists " + TABLE_CONVERSATIONS + " (" +
                "" + CONVERSATION_ID + " text primary key ," +
                "" + CONVERSATION_TITLE + " text," +
                "" + CONVERSATION_TYPE + " text," +
                "" + CONVERSATION_IMAGE + " text," +
                "" + CONVERSATION_CREATED_AT + " text," +
                "" + CONVERSATION_DESCRIPTION + " text," +
                "" + CONVERSATION_UPDATED_AT + " text," +
                "" + CONVERSATION_VERSION + " text," +
                "" + CONVERSATION_ADMINS + " text," +
                "" + CONVERSATION_MEMBERS + " text," +
                "" + CONVERSATION_LAST_MESSAGE_ID + " text," +
                "" + CONVERSATION_CREATOR + " text," +
                "" + CONVERSATION_REMOVED_MEMBERS + " text)");

        sqLiteDatabase.execSQL("create table if not exists " + TABLE_MESSAGES + " (" +
                "" + MESSAGES_ID + " text primary key," +
                "" + MESSAGES_TYPE + " text," +
                "" + MESSAGES_URL + " text," +
                "" + MESSAGES_STATUS + " text," +
                "" + MESSAGES_TEXT + " text," +
                "" + MESSAGES_DATE_TIME + " text," +
                "" + MESSAGES_SENDER_ID + " text," +
                "" + MESSAGES_CONVERSATION_ID + " text," +
                "" + MESSAGES_FILE_SIZE + " text," +
                "" + MESSAGES_SEEN_COUNT + " Integer)");

        sqLiteDatabase.execSQL("create table if not exists " + TABLE_USERS + "  (" +
                "" + USERS_ID + " text PRIMARY KEY," +
                "" + USERS_PHONE_ID + " text," +
                "" + USERS_USERNAME + " text  ," +
                "" + USERS_PHONE_NUMBER + " text," +
                "" + USERS_IMAGE + " text," +
                "" + USERS_ABOUT + " text," +
                "" + USERS_COMMUNICATIONS + " text)");

        sqLiteDatabase.execSQL("create table if not exists " + TABLE_CONTACTS + " (" +
                "" + CONTACTS_ID + " text PRIMARY KEY," +
                "" + CONTACTS_NUMBER + " text," +
                "" + CONTACTS_NAME + " text," +
                "" + CONTACTS_LOOKUP_KEY + " text)");

        sqLiteDatabase.execSQL("create table if not exists " + TABLE_LINKS + " (" +
                "" + LINKS_URL + " text PRIMARY KEY," +
                "" + LINKS_TITLE + " text)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

    public void insertConversation(Conversation conversation) {

        try {
            mDatabase = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(CONVERSATION_ID, conversation.get_id());
            values.put(CONVERSATION_TITLE, conversation.getTitle());
            values.put(CONVERSATION_TYPE, conversation.getType());
            values.put(CONVERSATION_IMAGE, conversation.getImage());
            values.put(CONVERSATION_DESCRIPTION, conversation.getDescription());
            values.put(CONVERSATION_ADMINS, new Gson().toJson(conversation.getAdmin()));
            values.put(CONVERSATION_MEMBERS, new Gson().toJson(conversation.getMembers()));
            values.put(CONVERSATION_LAST_MESSAGE_ID, conversation.getMessage().getId());
            values.put(CONVERSATION_CREATED_AT, conversation.getCreatedAt());
            values.put(CONVERSATION_UPDATED_AT, conversation.getUpdatedAt());
            values.put(CONVERSATION_VERSION, conversation.get__v());
            values.put(CONVERSATION_CREATOR, conversation.getCreator());
            values.put(CONVERSATION_REMOVED_MEMBERS, new Gson().toJson(conversation.getRemovedMembers()));
            mDatabase.insert(TABLE_CONVERSATIONS, null, values);
            mDatabase.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateConversation(Conversation conversation) {
        try {
            mDatabase = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(CONVERSATION_TITLE, conversation.getTitle());
            values.put(CONVERSATION_TYPE, conversation.getType());
            values.put(CONVERSATION_IMAGE, conversation.getImage());
            values.put(CONVERSATION_DESCRIPTION, conversation.getDescription());
            values.put(CONVERSATION_ADMINS, new Gson().toJson(conversation.getAdmin()));
            values.put(CONVERSATION_MEMBERS, new Gson().toJson(conversation.getMembers()));
            values.put(CONVERSATION_LAST_MESSAGE_ID, conversation.getMessage().getId());
            values.put(CONVERSATION_CREATED_AT, conversation.getCreatedAt());
            values.put(CONVERSATION_UPDATED_AT, conversation.getUpdatedAt());
            values.put(CONVERSATION_VERSION, conversation.get__v());
            values.put(CONVERSATION_CREATOR, conversation.getCreator());
            values.put(CONVERSATION_REMOVED_MEMBERS, new Gson().toJson(conversation.getRemovedMembers()));
            mDatabase.update(TABLE_CONVERSATIONS, values, CONVERSATION_ID + " = ?", new String[]{conversation.get_id()});
            mDatabase.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isConversationExists(String conversationId) {
        mDatabase = this.getReadableDatabase();
        boolean isPresent = false;
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_CONVERSATIONS + " WHERE " + CONVERSATION_ID + " = ?", new String[]{conversationId});
        if (cursor.moveToNext())
            isPresent = true;
        cursor.close();
        mDatabase.close();
        return isPresent;
    }

    public Conversation isConversationExists(String me, String other) {
        mDatabase = this.getReadableDatabase();
        Conversation conversation = null; // AND (" + CONVERSATION_ADMINS + " LIKE ? || '%' OR " + CONVERSATION_ADMINS + " LIKE ? || '%')
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_CONVERSATIONS + " WHERE " + CONVERSATION_TYPE + " = 'one-to-one' AND (" + CONVERSATION_ADMINS + " LIKE '%" + me + "%' OR " + CONVERSATION_ADMINS + " LIKE '%" + other + "%')", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            MessageData messageData = getMessage(cursor.getString(10));

            conversation = new Conversation(cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    new Gson().fromJson(cursor.getString(8), new TypeToken<List<String>>() {
                    }.getType()),
                    new Gson().fromJson(cursor.getString(9), new TypeToken<List<Users>>() {
                    }.getType()),
                    messageData
            );

            int count = 0;
            for (Users user : conversation.getMembers()) {
                if (user.getPhoneId().equals(me) || user.getPhoneId().equals(other))
                    count++;
            }
            if (count == 2)
                break;
            else conversation = null;
            cursor.moveToNext();

        }
        cursor.close();
        mDatabase.close();
        return conversation;
    }

    public Conversation getConversation(String conversationId) {
        mDatabase = this.getReadableDatabase();
        Conversation conversation = null;
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_CONVERSATIONS + " WHERE " + CONVERSATION_ID + " = ?", new String[]{conversationId});
        if (cursor.moveToNext()) {
            MessageData messageData = getMessage(cursor.getString(10));

            conversation = new Conversation(cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    new Gson().fromJson(cursor.getString(8), new TypeToken<List<String>>() {
                    }.getType()),
                    new Gson().fromJson(cursor.getString(9), new TypeToken<List<Users>>() {
                    }.getType()),
                    messageData
            );
            conversation.setCreator(conversation.getType().equals("group") ? cursor.getString(11) : null);
            conversation.setRemovedMembers(new Gson().fromJson(cursor.getString(12), new TypeToken<List<String>>() {
            }.getType()));
        }
        cursor.close();
        mDatabase.close();
        return conversation;
    }

    public List<Conversation> getAllConversations() {
        mDatabase = this.getReadableDatabase();
        List<Conversation> conversations = new ArrayList<>();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_CONVERSATIONS + "", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            MessageData messageData = getMessage(cursor.getString(10));

            Conversation conversation = new Conversation(cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    new Gson().fromJson(cursor.getString(8), new TypeToken<List<String>>() {
                    }.getType()),
                    new Gson().fromJson(cursor.getString(9), new TypeToken<List<Users>>() {
                    }.getType()),
                    messageData
            );
            conversation.setCreator(cursor.getString(11));
            conversation.setRemovedMembers(new Gson().fromJson(cursor.getString(12), new TypeToken<List<String>>() {
            }.getType()));
            conversations.add(conversation);
            cursor.moveToNext();
        }
        cursor.close();
        mDatabase.close();
        return conversations;
    }

    public List<Conversation> getAllOneToOneConversations(String me) {
        mDatabase = this.getReadableDatabase();
        List<Conversation> conversations = new ArrayList<>();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_CONVERSATIONS + " WHERE " + CONVERSATION_TYPE + " = 'one-to-one' AND " + CONVERSATION_ADMINS + " LIKE ? || '%'", new String[]{me});
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            MessageData messageData = getMessage(cursor.getString(10));

            Conversation conversation = new Conversation(cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    new Gson().fromJson(cursor.getString(8), new TypeToken<List<String>>() {
                    }.getType()),
                    new Gson().fromJson(cursor.getString(9), new TypeToken<List<Users>>() {
                    }.getType()),
                    messageData
            );
            conversation.setCreator(cursor.getString(11));
            conversation.setRemovedMembers(new Gson().fromJson(cursor.getString(12), new TypeToken<List<String>>() {
            }.getType()));
            conversations.add(conversation);
            cursor.moveToNext();
        }
        cursor.close();
        mDatabase.close();
        return conversations;
    }

    public List<Conversation> getAllGroupConversations() {
        mDatabase = this.getReadableDatabase();
        List<Conversation> conversations = new ArrayList<>();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_CONVERSATIONS + " WHERE " + CONVERSATION_TYPE + " = 'group'", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            MessageData messageData = getMessage(cursor.getString(10));

            Conversation conversation = new Conversation(cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    new Gson().fromJson(cursor.getString(8), new TypeToken<List<String>>() {
                    }.getType()),
                    new Gson().fromJson(cursor.getString(9), new TypeToken<List<Users>>() {
                    }.getType()),
                    messageData
            );
            conversation.setCreator(cursor.getString(11));
            conversation.setRemovedMembers(new Gson().fromJson(cursor.getString(12), new TypeToken<List<String>>() {
            }.getType()));
            conversations.add(conversation);
            cursor.moveToNext();
        }
        cursor.close();
        mDatabase.close();
        return conversations;
    }

    public void deleteConversation(String conversationId) {
        mDatabase = this.getReadableDatabase();
        mDatabase.delete(TABLE_CONVERSATIONS, "" + CONVERSATION_ID + " = ?", new String[]{conversationId});
        mDatabase.close();
    }

    public void deleteAllConversations() {
        mDatabase = this.getReadableDatabase();
        mDatabase.execSQL("DELETE FROM " + TABLE_CONVERSATIONS + "");
        mDatabase.close();
    }

    public void insertMessage(MessageData messageData) {

        try {
            mDatabase = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(MESSAGES_ID, messageData.getId());
            values.put(MESSAGES_TYPE, messageData.getType().toString());
            values.put(MESSAGES_URL, messageData.getUrl());
            values.put(MESSAGES_STATUS, messageData.getStatus());
            values.put(MESSAGES_TEXT, messageData.getText());
            values.put(MESSAGES_DATE_TIME, messageData.getDateTime());
            values.put(MESSAGES_SENDER_ID, messageData.getSenderPhoneId());
            values.put(MESSAGES_CONVERSATION_ID, messageData.getConversationId());
            values.put(MESSAGES_FILE_SIZE, messageData.getFileSize());
            values.put(MESSAGES_SEEN_COUNT, new Gson().toJson(messageData.getStatesRecords()));
            mDatabase.insert(TABLE_MESSAGES, null, values);
            mDatabase.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateMessage(MessageData messageData) {
        try {
            mDatabase = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(MESSAGES_TYPE, messageData.getType().toString());
            values.put(MESSAGES_URL, messageData.getUrl());
            values.put(MESSAGES_STATUS, messageData.getStatus());
            values.put(MESSAGES_TEXT, messageData.getText());
            values.put(MESSAGES_DATE_TIME, messageData.getDateTime());
            values.put(MESSAGES_SENDER_ID, messageData.getSenderPhoneId());
            values.put(MESSAGES_CONVERSATION_ID, messageData.getConversationId());
            values.put(MESSAGES_FILE_SIZE, messageData.getFileSize());
            values.put(MESSAGES_SEEN_COUNT, new Gson().toJson(messageData.getStatesRecords()));
            mDatabase.update(TABLE_MESSAGES, values, MESSAGES_ID + " = ?", new String[]{messageData.getId()});
            mDatabase.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isMessageExists(String messageId) {
        mDatabase = this.getReadableDatabase();
        boolean isPresent = false;
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_MESSAGES + " WHERE " + MESSAGES_ID + " = ?", new String[]{messageId});
        if (cursor.moveToNext())
            isPresent = true;
        cursor.close();
        mDatabase.close();
        return isPresent;
    }

    public List<MessageData> getAllMessages(String conversationId) {
        mDatabase = this.getReadableDatabase();
        List<MessageData> messages = new ArrayList<>();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_MESSAGES + " WHERE " + MESSAGES_CONVERSATION_ID + " = ?", new String[]{conversationId});
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            MessageData message = new MessageData(cursor.getString(0),
                    MessageData.getType(cursor.getString(1)),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8)
            );
            message.setStatesRecords(new Gson().fromJson(cursor.getString(9), new TypeToken<List<MessageStatesRecord>>() {
            }.getType()));
            messages.add(message);
            cursor.moveToNext();
        }
        cursor.close();
        mDatabase.close();
        return messages;
    }

    public List<MessageData> getAllUnsentMessages(String conversationId) {
        mDatabase = this.getReadableDatabase();
        List<MessageData> messages = new ArrayList<>();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_MESSAGES + " WHERE " + MESSAGES_CONVERSATION_ID + " = ? AND " + MESSAGES_STATUS + "='sending'", new String[]{conversationId});
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            MessageData message = new MessageData(cursor.getString(0),
                    MessageData.getType(cursor.getString(1)),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8)
            );
            message.setStatesRecords(new Gson().fromJson(cursor.getString(9), new TypeToken<List<MessageStatesRecord>>() {
            }.getType()));
            messages.add(message);
            cursor.moveToNext();
        }
        cursor.close();
        mDatabase.close();
        return messages;
    }

    public List<MessageData> getUnreadMessages(String conversationId, String senderId) {
        mDatabase = this.getReadableDatabase();
        List<MessageData> messages = new ArrayList<>();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_MESSAGES + " WHERE " +
                "" + MESSAGES_CONVERSATION_ID + " = ?" +
                " AND " +
                "" + MESSAGES_SENDER_ID + " != ?" +
                " AND " +
                "(" + MESSAGES_STATUS + " = 'send' OR " + MESSAGES_STATUS + " = 'delivered')", new String[]{conversationId, senderId});
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            MessageData message = new MessageData(cursor.getString(0),
                    MessageData.getType(cursor.getString(1)),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8)
            );
            message.setStatesRecords(new Gson().fromJson(cursor.getString(9), new TypeToken<List<MessageStatesRecord>>() {
            }.getType()));
            messages.add(message);
            cursor.moveToNext();
        }
        cursor.close();
        mDatabase.close();
        return messages;
    }

    public int getOneToOneUnreadCount(String conversationId, String senderId) {
        mDatabase = this.getReadableDatabase();
        int count = 0;
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_MESSAGES + " WHERE " +
                "" + MESSAGES_CONVERSATION_ID + " = ?" +
                " AND " +
                "" + MESSAGES_SENDER_ID + " != ?" +
                " AND " +
                "(" + MESSAGES_STATUS + " = 'send' OR " + MESSAGES_STATUS + " = 'delivered') AND " + CONVERSATION_TYPE + "='one-to-one'", new String[]{conversationId, senderId});
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            count++;
            cursor.moveToNext();
        }
        cursor.close();
        mDatabase.close();
        return count;
    }

    public int getUnreadCount(String conversationId, String senderId) {
        mDatabase = this.getReadableDatabase();
        int count = 0;
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_MESSAGES + " WHERE " +
                "" + MESSAGES_CONVERSATION_ID + " = ?" +
                " AND " +
                "" + MESSAGES_SENDER_ID + " != ?" +
                " AND " +
                "(" + MESSAGES_STATUS + " = 'send' OR " + MESSAGES_STATUS + " = 'delivered')", new String[]{conversationId, senderId});
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            count++;
            cursor.moveToNext();
        }
        cursor.close();
        mDatabase.close();
        return count;
    }

    public MessageData getMessage(String messageId) {
        mDatabase = this.getReadableDatabase();
        MessageData message = null;
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_MESSAGES + " WHERE " + MESSAGES_ID + " = ?", new String[]{messageId});
        if (cursor.moveToNext()) {

            message = new MessageData(cursor.getString(0),
                    MessageData.getType(cursor.getString(1)),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8)
            );
            message.setStatesRecords(new Gson().fromJson(cursor.getString(9), new TypeToken<List<MessageStatesRecord>>() {
            }.getType()));
        }
        cursor.close();
        mDatabase.close();
        return message;
    }

    public List<MessageData> getAllMediaMessages(String conversationId) {
        mDatabase = this.getReadableDatabase();
        List<MessageData> messages = new ArrayList<>();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_MESSAGES + " WHERE " + MESSAGES_CONVERSATION_ID + " = ? AND " +
                "(" + MESSAGES_TYPE + " = 'PICTURE' OR " +
                "" + MESSAGES_TYPE + " = 'VIDEO' OR " +
                "" + MESSAGES_TYPE + " = 'DOCUMENTS' OR " +
                "" + MESSAGES_TYPE + " = 'AUDIO' OR " +
                "" + MESSAGES_TYPE + " = 'GIF' OR " +
                "" + MESSAGES_TYPE + " = 'LINK')", new String[]{conversationId});
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            MessageData message = new MessageData(cursor.getString(0),
                    MessageData.getType(cursor.getString(1)),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8)
            );
            message.setStatesRecords(new Gson().fromJson(cursor.getString(9), new TypeToken<List<MessageStatesRecord>>() {
            }.getType()));
            messages.add(message);
            cursor.moveToNext();
        }
        cursor.close();
        mDatabase.close();
        return messages;
    }

    public List<MessageData> getAllDocumentsMessages(String conversationId) {
        mDatabase = this.getReadableDatabase();
        List<MessageData> messages = new ArrayList<>();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_MESSAGES + " WHERE " + MESSAGES_CONVERSATION_ID + " = ? AND " +
                "(" + MESSAGES_TYPE + " = 'DOCUMENTS')", new String[]{conversationId});
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            MessageData message = new MessageData(cursor.getString(0),
                    MessageData.getType(cursor.getString(1)),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8)
            );
            message.setStatesRecords(new Gson().fromJson(cursor.getString(9), new TypeToken<List<MessageStatesRecord>>() {
            }.getType()));
            messages.add(message);
            cursor.moveToNext();
        }
        cursor.close();
        mDatabase.close();
        return messages;
    }

    public List<MessageData> getAllLinksMessages(String conversationId) {
        mDatabase = this.getReadableDatabase();
        List<MessageData> messages = new ArrayList<>();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_MESSAGES + " WHERE " + MESSAGES_CONVERSATION_ID + " = ? AND " +
                "(" + MESSAGES_TYPE + " = 'LINK')", new String[]{conversationId});
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            MessageData message = new MessageData(cursor.getString(0),
                    MessageData.getType(cursor.getString(1)),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8)
            );
            message.setStatesRecords(new Gson().fromJson(cursor.getString(9), new TypeToken<List<MessageStatesRecord>>() {
            }.getType()));
            messages.add(message);
            cursor.moveToNext();
        }
        cursor.close();
        mDatabase.close();
        return messages;
    }

    public List<MessageData> getAllMultiMediaMessages(String conversationId) {
        mDatabase = this.getReadableDatabase();
        List<MessageData> messages = new ArrayList<>();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_MESSAGES + " WHERE " + MESSAGES_CONVERSATION_ID + " = ? AND " +
                "(" + MESSAGES_TYPE + " = 'PICTURE' OR " +
                "" + MESSAGES_TYPE + " = 'VIDEO' OR " +
                "" + MESSAGES_TYPE + " = 'AUDIO' OR " +
                "" + MESSAGES_TYPE + " = 'GIF')", new String[]{conversationId});
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            MessageData message = new MessageData(cursor.getString(0),
                    MessageData.getType(cursor.getString(1)),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8)
            );
            message.setStatesRecords(new Gson().fromJson(cursor.getString(9), new TypeToken<List<MessageStatesRecord>>() {
            }.getType()));
            messages.add(message);
            cursor.moveToNext();
        }
        cursor.close();
        mDatabase.close();
        return messages;
    }

    public void deleteMessage(String messageId) {
        mDatabase = this.getReadableDatabase();
        mDatabase.delete(TABLE_MESSAGES, "" + MESSAGES_ID + " = ?", new String[]{messageId});
        mDatabase.close();
    }

    public void deleteAllMessages(String conversationId) {
        mDatabase = this.getReadableDatabase();
        mDatabase.delete(TABLE_MESSAGES, "" + MESSAGES_CONVERSATION_ID + " = ?", new String[]{conversationId});
        mDatabase.close();
    }

    public void deleteAllMessages() {
        mDatabase = this.getReadableDatabase();
        mDatabase.execSQL("DELETE FROM " + TABLE_MESSAGES + "");
        mDatabase.close();
    }

    public void insertUser(Users user) {

        try {
            mDatabase = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(USERS_ID, user.getUserId());
            values.put(USERS_PHONE_ID, user.getPhoneId());
            values.put(USERS_USERNAME, user.getName());
            values.put(USERS_PHONE_NUMBER, user.getPhoneNo());
            values.put(USERS_IMAGE, user.getPhoto());
            values.put(USERS_ABOUT, user.getAbout());
            values.put(USERS_COMMUNICATIONS, new Gson().toJson(user.getCommunications()));
            mDatabase.insert(TABLE_USERS, null, values);
            mDatabase.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateUser(Users user) {
        try {
            mDatabase = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(USERS_PHONE_ID, user.getPhoneId());
            values.put(USERS_USERNAME, user.getName());
            values.put(USERS_PHONE_NUMBER, user.getPhoneNo());
            values.put(USERS_IMAGE, user.getPhoto());
            values.put(USERS_ABOUT, user.getAbout());
            values.put(USERS_COMMUNICATIONS, new Gson().toJson(user.getCommunications()));
            mDatabase.update(TABLE_USERS, values, USERS_PHONE_ID + " = ?", new String[]{user.getPhoneId()});
            mDatabase.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isUserExists(String phoneId) {
        mDatabase = this.getReadableDatabase();
        boolean isPresent = false;
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + USERS_PHONE_ID + " = ?", new String[]{phoneId});
        if (cursor.moveToNext())
            isPresent = true;
        cursor.close();
        mDatabase.close();
        return isPresent;
    }

    public List<Users> getAllUsersExceptMe(String phoneId) {
        mDatabase = this.getReadableDatabase();
        List<Users> users = new ArrayList<>();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + USERS_PHONE_ID + " != ?", new String[]{phoneId});
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Users user = new Users(cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    new Gson().fromJson(cursor.getString(6), new TypeToken<List<String>>() {
                    }.getType())
            );
            users.add(user);
            cursor.moveToNext();
        }
        cursor.close();
        mDatabase.close();
        return users;
    }

    public List<Users> getAllUsers() {
        mDatabase = this.getReadableDatabase();
        List<Users> users = new ArrayList<>();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_USERS + "", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Users user = new Users(cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    new Gson().fromJson(cursor.getString(6), new TypeToken<List<String>>() {
                    }.getType())
            );
            users.add(user);
            cursor.moveToNext();
        }
        cursor.close();
        mDatabase.close();
        return users;
    }

    public Users getUser(String phoneId) {
        mDatabase = this.getReadableDatabase();
        Users user = null;
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + USERS_PHONE_ID + " = ?", new String[]{phoneId});
        if (cursor.moveToNext()) {
            user = new Users(cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    new Gson().fromJson(cursor.getString(6), new TypeToken<List<String>>() {
                    }.getType())
            );
        }
        cursor.close();
        mDatabase.close();
        return user;
    }

    public void deleteUser(String phoneId) {
        mDatabase = this.getReadableDatabase();
        mDatabase.delete(TABLE_USERS, "" + USERS_PHONE_ID + " = ?", new String[]{phoneId});
        mDatabase.close();
    }

    public void deleteAllUsers() {
        mDatabase = this.getReadableDatabase();
        mDatabase.execSQL("DELETE FROM " + TABLE_USERS + "");
        mDatabase.close();
    }

    public void insertContact(LocalContacts contact) {

        try {
            mDatabase = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(CONTACTS_ID, contact.getContactId());
            values.put(CONTACTS_NUMBER, contact.getNumber());
            values.put(CONTACTS_NAME, contact.getName());
            values.put(CONTACTS_LOOKUP_KEY, contact.getLookupKey());
            mDatabase.insert(TABLE_CONTACTS, null, values);
            mDatabase.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateContact(LocalContacts contact) {
        try {
            mDatabase = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(CONTACTS_NUMBER, contact.getNumber());
            values.put(CONTACTS_NAME, contact.getName());
            values.put(CONTACTS_LOOKUP_KEY, contact.getLookupKey());
            mDatabase.update(TABLE_CONTACTS, values, CONTACTS_ID + " = ?", new String[]{contact.getContactId()});
            mDatabase.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isContactExists(String phoneNumber) {
        mDatabase = this.getReadableDatabase();
        boolean isPresent = false;
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_CONTACTS + " WHERE " + CONTACTS_NUMBER + " LIKE ? || '%' OR " + CONTACTS_ID + " = ?", new String[]{phoneNumber, phoneNumber});
        if (cursor.moveToNext())
            isPresent = true;
        cursor.close();
        mDatabase.close();
        return isPresent;
    }

    public List<LocalContacts> getAllContacts() {
        mDatabase = this.getReadableDatabase();
        List<LocalContacts> contacts = new ArrayList<>();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_CONTACTS + "", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            LocalContacts contact = new LocalContacts(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
            );
            contacts.add(contact);
            cursor.moveToNext();
        }
        cursor.close();
        mDatabase.close();
        return contacts;
    }

    public LocalContacts getContact(String query) {
        mDatabase = this.getReadableDatabase();
        LocalContacts contact = null;
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_CONTACTS + " WHERE " + CONTACTS_ID + " = ? OR " + CONTACTS_NUMBER + " = ?", new String[]{query, query});
        if (cursor.moveToNext()) {
            contact = new LocalContacts(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
            );
        }
        cursor.close();
        mDatabase.close();
        return contact;
    }

    public void deleteContact(String query) {
        mDatabase = this.getReadableDatabase();
        mDatabase.delete(TABLE_CONTACTS, "" + CONTACTS_ID + " = ? OR " + CONTACTS_NUMBER + " = ?", new String[]{query, query});
        mDatabase.close();
    }

    public void deleteAllContacts() {
        mDatabase = this.getReadableDatabase();
        mDatabase.execSQL("DELETE FROM " + TABLE_CONTACTS + "");
        mDatabase.close();
    }

    public void insertLink(Link link) {

        try {
            mDatabase = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(LINKS_URL, link.getUrl());
            values.put(LINKS_TITLE, link.getTitle());
            mDatabase.insert(TABLE_LINKS, null, values);
            mDatabase.close();
        } catch (Exception e) {
            Log.d(ResourcePlusApplication.TAG, "Insert Link Error: " + e.getMessage());
        }
    }

    public Link getLink(String query) {
        mDatabase = this.getReadableDatabase();
        Link link = null;
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + TABLE_LINKS + " WHERE " + LINKS_URL + " = ?", new String[]{query});
        if (cursor.moveToNext()) {
            link = new Link(
                    cursor.getString(0),
                    cursor.getString(1)
            );
        }
        cursor.close();
        mDatabase.close();
        return link;
    }

    public void deleteLink(String query) {
        mDatabase = this.getReadableDatabase();
        mDatabase.delete(TABLE_LINKS, "" + LINKS_URL + " = ?", new String[]{query});
        mDatabase.close();
    }

}
