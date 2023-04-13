package com.template;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.TimeZone;
import java.util.UUID;

public class LoadingActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String link;
    private static String finalLink = null;
    private SharedPreferences preferences;
    private Intent mainIntent;
    private Intent webIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        mainIntent = new Intent(this, MainActivity.class);
        webIntent = new Intent(this, WebActivity.class);
        preferences = getSharedPreferences("pref", Context.MODE_PRIVATE);

        if (!checkInternetConnection()) {
            startActivity(mainIntent);
        } else {
            if (preferences.contains("EMPTY")) {
                startActivity(mainIntent);
            } else {
                if (preferences.contains("RESPONSE")) {
                    if (preferences.getString("RESPONSE", "").startsWith("http")) {
                        startActivity(webIntent);
                    } else {
                        startActivity(mainIntent);
                    }
                } else {
                    getLink();
                }
            }
        }
    }


    private void getLink() {

        DocumentReference docRef = db.collection("database").document("check");

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists() && document.get("link") != null) {
                    link = (String) document.get("link");
                    goToLink();
                } else {
                    preferences.edit().putString("EMPTY", "empty").apply();
                    startActivity(mainIntent);
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
                startActivity(mainIntent);
            }
        });
    }

    private void goToLink() {
        StringBuilder createdLink = new StringBuilder();
        createdLink.append(link)
                .append("/?packageid=")
                .append(getApplicationContext().getPackageName())
                .append("&usserid=")
                .append(UUID.randomUUID())
                .append("&getz=")
                .append(TimeZone.getDefault().getID())
                .append("&getr=utm_source=google-play&utm_medium=organic");
        finalLink = String.valueOf(createdLink);
        startActivity(webIntent);
    }

    private boolean checkInternetConnection() {
        try {
            String command = "ping -c 1 google.com";
            return Runtime.getRuntime().exec(command).waitFor() == 0;
        } catch (IOException | InterruptedException IOex) {
            IOex.printStackTrace();
        }
        return false;
    }

    public static String getFinalLink() {
        return finalLink;
    }
}