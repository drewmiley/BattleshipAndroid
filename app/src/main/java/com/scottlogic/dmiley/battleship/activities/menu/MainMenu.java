package com.scottlogic.dmiley.battleship.activities.menu;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import com.scottlogic.dmiley.battleship.R;
import com.scottlogic.dmiley.battleship.activities.AITestActivity;
import com.scottlogic.dmiley.battleship.activities.setting.SettingsActivity;
import com.scottlogic.dmiley.battleship.activities.oneplayersolo.OnePlayerSoloEntryActivity;
import com.scottlogic.dmiley.battleship.activities.oneplayersolo.OnePlayerSoloGameActivity;
import com.scottlogic.dmiley.battleship.activities.oneplayerversus.OnePlayerVersusEntryActivity;
import com.scottlogic.dmiley.battleship.activities.oneplayerversus.OnePlayerVersusGameActivity;
import com.scottlogic.dmiley.battleship.activities.twoplayer.TwoPlayerEntryActivity;
import com.scottlogic.dmiley.battleship.activities.twoplayer.TwoPlayerGameActivity;

// Main Menu Activity, application launcher.
public class MainMenu extends ActionBarActivity {

    // Initialise activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load layout
        setContentView(R.layout.activity_main_menu);

        boolean onePlayerSavedGame = loadOnePlayerGameMarker();
        if (!onePlayerSavedGame) {
            Button onePlayerResumeButton = (Button) findViewById(R.id.resume_one_player_solo_game_button);
            onePlayerResumeButton.setEnabled(false);
        }

        boolean onePlayerVersusSavedGame = loadOnePlayerVersusGameMarker();
        if (!onePlayerVersusSavedGame) {
            Button onePlayerVersusResumeButton = (Button) findViewById(R.id.resume_one_player_versus_game_button);
            onePlayerVersusResumeButton.setEnabled(false);
        }

        boolean twoPlayerSavedGame = loadTwoPlayerGameMarker();
        if (!twoPlayerSavedGame) {
            Button twoPlayerResumeButton = (Button) findViewById(R.id.resume_two_player_game_button);
            twoPlayerResumeButton.setEnabled(false);
        }
    }

    private boolean loadOnePlayerGameMarker() {
        try {
            FileInputStream fileInputStream = openFileInput("oneplayersavedgame");
            ObjectInputStream objectInputStream = new ObjectInputStream (fileInputStream);
            boolean oneplayersavedgame = objectInputStream.readBoolean();
            objectInputStream.close();
            fileInputStream.close();
            return oneplayersavedgame;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean loadOnePlayerVersusGameMarker() {
        try {
            FileInputStream fileInputStream = openFileInput("oneplayerversussavedgame");
            ObjectInputStream objectInputStream = new ObjectInputStream (fileInputStream);
            boolean oneplayerversussavedgame = objectInputStream.readBoolean();
            objectInputStream.close();
            fileInputStream.close();
            return oneplayerversussavedgame;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean loadTwoPlayerGameMarker() {
        try {
            FileInputStream fileInputStream = openFileInput("twoplayersavedgame");
            ObjectInputStream objectInputStream = new ObjectInputStream (fileInputStream);
            boolean twoplayersavedgame = objectInputStream.readBoolean();
            objectInputStream.close();
            fileInputStream.close();
            return twoplayersavedgame;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // One Player Game button click handler
    public void onNewOnePlayerSoloGameButtonClicked(View view) {
        // Start OnePlayerPreGame activity
        Intent intent = new Intent(getApplicationContext(), OnePlayerSoloEntryActivity.class);
        startActivity(intent);
    }

    // Resume Game button click handler
    public void onResumeOnePlayerSoloGameButtonClicked(View view) {
        // Start OnePlayerGame activity
        Intent intent = new Intent(getApplicationContext(), OnePlayerSoloGameActivity.class);
        // Boolean to notify of whether we want to resume a previous game
        intent.putExtra("ResumeOnePlayerSoloGame", true);
        startActivity(intent);
    }

    public void onNewOnePlayerVersusGameButtonClicked(View view) {
        // Start OnePlayerPreGame activity
        Intent intent = new Intent(getApplicationContext(), OnePlayerVersusEntryActivity.class);
        startActivity(intent);
    }

    public void onResumeOnePlayerVersusGameButtonClicked(View view) {
        // Start OnePlayerGame activity
        Intent intent = new Intent(getApplicationContext(), OnePlayerVersusGameActivity.class);
        // Boolean to notify of whether we want to resume a previous game
        intent.putExtra("ResumeOnePlayerVersusGame", true);
        startActivity(intent);
    }

    // New Two Player game button click handler
    public void onNewTwoPlayerGameButtonClicked(View view) {
        // Start TwoPlayerPreGame activity
        Intent intent = new Intent(getApplicationContext(), TwoPlayerEntryActivity.class);
        startActivity(intent);
    }

    // Resume Two Player Game button click handler
    public void onResumeTwoPlayerGameButtonClicked(View view) {
        // Start TwoPlayerGame activity
        Intent intent = new Intent(getApplicationContext(), TwoPlayerGameActivity.class);
        // Boolean to notify of whether we want to resume a previous game
        intent.putExtra("ResumeTwoPlayerGame", true);
        startActivity(intent);
    }

    public void onSettingsButtonClicked(View view) {
        // Start Options activity
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }

    public void onAITestButtonClicked(View view) {
        // Start AITest activity
        Intent intent = new Intent(getApplicationContext(), AITestActivity.class);
        startActivity(intent);
    }
}
