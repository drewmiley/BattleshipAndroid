package com.scottlogic.dmiley.battleship.activities.oneplayersolo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.scottlogic.dmiley.battleship.R;
import com.scottlogic.dmiley.battleship.activities.menu.MainMenu;
import com.scottlogic.dmiley.battleship.activities.setting.SettingsActivity;

public class OnePlayerSoloEntryActivity extends ActionBarActivity {

    private EditText editText;

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainMenu.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_player_entry);

        editText = (EditText) findViewById(R.id.edit_player_name);
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

        if (!(playerOneDefaultName.equals(""))) {
            editText.setHint(playerOneDefaultName);
        } else {
            editText.setHint(getString(R.string.player_one_default_name));
        }
    }

    // Start Game button click handler
    public void onStartGameButtonClicked(View view) {

        // Start OnePlayerGame activity
        Intent intent = new Intent(getApplicationContext(), OnePlayerSoloGameActivity.class);

        // Get player name & send to next activity
        String playerName = editText.getText().toString();
        if (playerName.equals("")) {
            playerName = (String) editText.getHint();
        }
        intent.putExtra("PlayerName", playerName);

        // Boolean to notify of whether we want to resume a previous game (in this case not)
        intent.putExtra("ResumeOnePlayerSoloGame", false);

        startActivity(intent);
    }
}
