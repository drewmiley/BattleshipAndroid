package com.scottlogic.dmiley.battleship.activities.oneplayerversus;

import com.scottlogic.dmiley.battleship.R;
import com.scottlogic.dmiley.battleship.activities.menu.MainMenu;
import com.scottlogic.dmiley.battleship.activities.setting.SettingsActivity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class OnePlayerVersusPostGameActivity extends ActionBarActivity {

  private String playerName;
  private boolean playerPlaceFleet;

  @Override
  public void onBackPressed() {
    Intent intent = new Intent(getApplicationContext(), OnePlayerVersusEntryActivity.class);
    startActivity(intent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_one_player_versus_post_game);

    // Get player name
    Intent intent = getIntent();
    playerName = intent.getStringExtra("PlayerName");
    playerPlaceFleet = intent.getBooleanExtra("PlayerPlaceFleet", false);

    // Set Personalised congratulations
    TextView textView = (TextView) findViewById(R.id.versus_player_textview);
    if (intent.getBooleanExtra("PlayerWon", true)) {
        String personalisedCongratulations = playerName + ", " + getString(R.string.congratulations);
        textView.setText(personalisedCongratulations);
        setTitle(R.string.congratulations_title);
    } else {
        String personalisedCommiserations = playerName + ", " + getString(R.string.commiserations);
        textView.setText(personalisedCommiserations);
        setTitle(R.string.commiserations_title);
    }

    // Reset onePlayerRadar save
    resetSavedGameState();
    saveOnePlayerVersusGameMarker();
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

  private void resetSavedGameState() {
    try {
      FileOutputStream fileOutputStream = openFileOutput("playerversusradar", Context.MODE_PRIVATE);
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
      objectOutputStream.writeObject(null);
      objectOutputStream.close();
      fileOutputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      FileOutputStream fileOutputStream = openFileOutput("computerversusradar", Context.MODE_PRIVATE);
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
      objectOutputStream.writeObject(null);
      objectOutputStream.close();
      fileOutputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void saveOnePlayerVersusGameMarker() {
    // Save identifier that a saved game exists
    try {
      FileOutputStream fileOutputStream = openFileOutput("oneplayerversussavedgame", Context.MODE_PRIVATE);
      ObjectOutputStream objectOutputStream = new ObjectOutputStream (fileOutputStream);
      objectOutputStream.writeBoolean(false);
      objectOutputStream.close();
      fileOutputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Play again click handler
  public void onPlayAgainButtonClicked(View view) {
    Intent intent;
    if (playerPlaceFleet) {
      // Start FleetPlacement activity
      intent = new Intent(getApplicationContext(), OnePlayerVersusFleetPlacementActivity.class);
    } else {
      // Start TwoPlayerGame activity
      intent = new Intent(getApplicationContext(), OnePlayerVersusGameActivity.class);
    }

    // Get player name and fleet placement preference
    intent.putExtra("PlayerName", playerName);
    intent.putExtra("PlayerPlaceFleet", playerPlaceFleet);

    startActivity(intent);
  }

  // Return to menu button click handler
  public void onReturnToMenuButtonClicked(View view) {
    // Start Main menu activity
    Intent intent = new Intent(getApplicationContext(), MainMenu.class);
    startActivity(intent);
  }
}
