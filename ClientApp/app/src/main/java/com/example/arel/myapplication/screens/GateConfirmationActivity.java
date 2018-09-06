package com.example.arel.myapplication.screens;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.arel.myapplication.BaseActivity;
import com.example.arel.myapplication.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.nfc.NdefRecord.createMime;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class GateConfirmationActivity extends BaseActivity implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {

    NfcAdapter mNfcAdapter;
    private Uri[] mFileUris = new Uri[10];

    private FileUriCallback mFileUriCallback;
    private static String TAG = "GateConfirmationActivity";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate_confirmation);


        // Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // Register callback
        mNfcAdapter.setNdefPushMessageCallback(this, this);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        /*
         * Instantiate a new FileUriCallback to handle requests for
         * URIs
         */
        mFileUriCallback = new FileUriCallback();
        // Set the dynamic callback for URI requests.
        mNfcAdapter.setBeamPushUrisCallback(mFileUriCallback,this);
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
        DocumentReference ref = db.collection("account_history").document();
        String myId = ref.getId();

        String text = (user.getUid() + ":" + myId);
        Log.d("arelguatno",myId);
        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { createMime(
                        "application/vnd.com.example.android.beam", text.getBytes())
                        /**
                         * The Android Application Record (AAR) is commented out. When a device
                         * receives a push with an AAR in it, the application specified in the AAR
                         * is guaranteed to run. The AAR overrides the tag dispatch system.
                         * You can add it back in to guarantee that this
                         * activity starts when receiving a beamed message. For now, this code
                         * uses the tag dispatch system.
                        */
                        ,NdefRecord.createApplicationRecord("com.example.appdev.beamreceiver")
                });
        // Save data to firestore
        saveEntryPointToFirestore();

        return msg;
    }

    private void saveEntryPointToFirestore() {
        Map<String, Object> city = new HashMap<>();
        city.put("amount", 25);
        city.put("date", System.currentTimeMillis());
        city.put("description", "Ride");
        city.put("description", "-");
        city.put("ride_from", "North Avenue");
        city.put("ride_from", "North Avenue");
        city.put("type", "PENDING");

        db.collection("account_history").document(user.getUid()).collection("data").document()
                .set(city)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }


    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    void processIntent(Intent intent) {
//        textView = (TextView) findViewById(R.id.textView);
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
//        textView.setText(new String(msg.getRecords()[0].getPayload()));
    }

    @Override
    public void onNdefPushComplete(NfcEvent nfcEvent) {
        Log.d("arelguatno", "Yehey");
    }

    private class FileUriCallback implements
            NfcAdapter.CreateBeamUrisCallback {
        public FileUriCallback() {

        }
        /**
         * Create content URIs as needed to share with another device
         */
        @Override
        public Uri[] createBeamUris(NfcEvent event) {
            return mFileUris;
        }
    }

}
