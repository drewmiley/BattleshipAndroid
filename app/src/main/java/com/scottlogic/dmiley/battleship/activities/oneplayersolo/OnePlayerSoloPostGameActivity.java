package com.scottlogic.dmiley.battleship.activities.oneplayersolo;

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

public class OnePlayerSoloPostGameActivity extends ActionBarActivity {

  private String playerName;

  @Override
  public void onBackPressed() {
    Intent intent = new Intent(getApplicationContext(), OnePlayerSoloEntryActivity.class);
    startActivity(intent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_one_player_post_game);

    // Get player name
    Intent intent = getIntent();
    playerName = intent.getStringExtra("PlayerName");

    // Set Personalised congratulations
    TextView textView = (TextView) findViewById(R.id.player_congratulations);
    String congratulations = (String) textView.getText();
    String personalisedCongratulations = playerName + ", " + congratulations;
    textView.setText(personalisedCongratulations);

    // Reset onePlayerRadar save
    resetSavedGameState();
    saveOnePlayerGameMarker();
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
      FileOutputStream fileOutputStream = openFileOutput("oneplayerradar", Context.MODE_PRIVATE);
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
      objectOutputStream.writeObject(null);
      objectOutputStream.close();
      fileOutputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void saveOnePlayerGameMarker() {
    // Save identifier that a saved game exists
    try {
      FileOutputStream fileOutputStream = openFileOutput("oneplayersavedgame", Context.MODE_PRIVATE);
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
    // Start One Player Game activity
    Intent intent = new Intent(getApplicationContext(), OnePlayerSoloGameActivity.class);
    // Get player name
    intent.putExtra("PlayerName", playerName);
    startActivity(intent);
  }

  // Return to menu button click handler
  public void onReturnToMenuButtonClicked(View view) {
    // Start Main menu activity
    Intent intent = new Intent(getApplicationContext(), MainMenu.class);
    startActivity(intent);
  }
}
