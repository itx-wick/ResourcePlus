package com.mr_w.resourceplus.async;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.mr_w.resourceplus.abstract_models.BackgroundCallbacks;
import com.mr_w.resourceplus.database.Database;
import com.mr_w.resourceplus.model.LocalContacts;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("StaticFieldLeak")
public class SyncContacts extends AsyncTask<String, String, String> {

    private final Context context;
    private final List<LocalContacts> localContactsList = new ArrayList<>();
    private final Database db;
    private final BackgroundCallbacks<List<LocalContacts>> callbacks;

    public SyncContacts(Context context, BackgroundCallbacks<List<LocalContacts>> callbacks) {
        this.context = context;
        this.callbacks = callbacks;
        db = Database.getInstance(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        callbacks.onPreProcessing();
    }

    @SuppressLint("NewApi")
    @Override
    protected String doInBackground(String... f_url) {
        try {
            Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, null);
            while (phones.moveToNext()) {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNo = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String id = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                String lookupKey = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY));
                phoneNo = phoneNo.replaceAll("[^0-9+]", "");
                if (phoneNo.length() >= 11 && phoneNo.length() <= 15) {
                    PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                    try {
                        if (phoneNo.startsWith("00")) {
                            phoneNo = phoneNo.replace(phoneNo.substring(0, 2), "+");
                        } else if (phoneNo.startsWith("0")) {
                            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                            String countryCodeValue = tm.getNetworkCountryIso();
                            phoneNo = phoneUtil.format(phoneUtil.parse(phoneNo, countryCodeValue.toUpperCase()), PhoneNumberUtil.PhoneNumberFormat.E164);
                        }
                    } catch (NumberParseException e) {
                        e.printStackTrace();
                    }
                    LocalContacts localContacts = new LocalContacts();
                    localContacts.setName(name);
                    localContacts.setNumber(phoneNo);
                    localContacts.setContactId(id);
                    localContacts.setLookupKey(lookupKey);
                    localContactsList.add(localContacts);

                    if (!db.isContactExists(phoneNo) && !db.isContactExists(id))
                        db.insertContact(localContacts);
                    else
                        db.updateContact(localContacts);
                }
            }
            return "good";
        } catch (
                Exception e) {
            Log.e("Error: ", e.getMessage());
            callbacks.onFailure();
        }
        return null;
    }

    @Override
    protected void onPostExecute(final String message) {
        if (message == null)
            return;
        callbacks.onCompleted(localContactsList);
    }
}