package com.scottlogic.dmiley.battleship.activities.oneplayerversus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import com.scottlogic.dmiley.battleship.R;
import com.scottlogic.dmiley.battleship.activities.menu.MainMenu;
import com.scottlogic.dmiley.battleship.activities.setting.SettingsActivity;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;


public class OnePlayerVersusEntryActivity extends ActionBarActivity {

    private boolean playerPlaceFleet;

    private EditText editText;

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainMenu.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_player_versus_entry);

        editText = (EditText) findViewById(R.id.edit_player_versus_name);

        resetFleetPlacementSave();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // Start Options activity
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        //Set default name
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String playerOneDefaultName = sharedPreferences.getString("playerOneDefaultName", "");
        String playerVersusFleetPreferenceBooleanString = sharedPreferences.getString("playerOneDefaultFleetPlacement", "1");
        int playerVersusFleetPreferenceBooleanInteger = Integer.parseInt(playerVersusFleetPreferenceBooleanString);
        if (playerVersusFleetPreferenceBooleanInteger == 0) {
            RadioButton click = (RadioButton) findViewById(R.id.player_versus_fleet_placement_preference_false);
            click.performClick();
        } else {
            RadioButton click = (RadioButton) findViewById(R.id.player_versus_fleet_placement_preference_true);
            click.performClick();
        }

        if (!(playerOneDefaultName.equals(""))) {
            editText.setHint(playerOneDefaultName);
        } else {
            editText.setHint(getString(R.string.player_one_default_name));
        }
    }

    private void resetFleetPlacementSave() {
        try {
            FileOutputStream fileOutputStream = openFileOutput("playerversusfleetplacement", Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream (fileOutputStream);
            objectOutputStream.writeObject(null);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void onPlayerWantToPlaceFleetButtonClicked(View view) {
        playerPlaceFleet = true;
    }

    public void onPlayerDontWantToPlaceFleetButtonClicked(View view) {
        playerPlaceFleet = false;
    }

    // Start Game button click handler
    public void onStartGameButtonClicked(View view) {
        Intent intent;
        if (playerPlaceFleet) {
            // Start FleetPlacement activity
            intent = new Intent(getApplicationContext(), OnePlayerVersusFleetPlacementActivity.class);
        } else {
            // Start TwoPlayerGame activity
            intent = new Intent(getApplicationContext(), OnePlayerVersusGameActivity.class);
        }

        // Get player names & send to next activity
        String playerName = editText.getText().toString();
        if (playerName.equals("")) {
            playerName = (String) editText.getHint();
        }
        intent.putExtra("PlayerName", playerName);
        intent.putExtra("PlayerPlaceFleet", playerPlaceFleet);

        // Boolean to notify of whether we want to resume a previous game (in this case not)
        intent.putExtra("ResumeOnePlayerVersusGame", false);

        startActivity(intent);
    }
}