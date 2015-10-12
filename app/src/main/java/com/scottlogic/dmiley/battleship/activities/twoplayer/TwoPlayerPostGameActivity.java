package com.scottlogic.dmiley.battleship.activities.twoplayer;

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

public class TwoPlayerPostGameActivity extends ActionBarActivity {

  private String playerTwoName;
  private String playerOneName;
  private boolean playerOnePlaceFleet;
  private boolean playerTwoPlaceFleet;
  private int firstTurnIdentifier;

  private final static int PLAYER_ONE_TURN_IDENTIFIER = 1;
  private final static int PLAYER_TWO_TURN_IDENTIFIER = 2;

  @Override
  public void onBackPressed() {
    Intent intent = new Intent(getApplicationContext(), TwoPlayerEntryActivity.class);
    startActivity(intent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_two_player_post_game);

    // Get player name
    Intent intent = getIntent();
    playerOneName = intent.getStringExtra("PlayerOneName");
    playerTwoName = intent.getStringExtra("PlayerTwoName");
    playerOnePlaceFleet = intent.getBooleanExtra("PlayerOnePlaceFleet", false);
    playerTwoPlaceFleet = intent.getBooleanExtra("PlayerTwoPlaceFleet", false);

    firstTurnIdentifier = intent.getIntExtra("FirstTurnIdentifier", (int) Math.ceil(2 * Math.random()));
    alternateFirstTurn();
    int winningIdentifier = intent.getIntExtra("WinningIdentifier", 0);

    // Set Personalised congratulations
    TextView textView = (TextView) findViewById(R.id.two_player_congratulations);
    String congratulationsPartOne = getString(R.string.two_player_congratulations_part_one) + " ";
    String congratulationsPartTwo = getString(R.string.two_player_congratulations_part_two);
    if (winningIdentifier == PLAYER_ONE_TURN_IDENTIFIER) {
      String personalisedCongratulations = playerOneName + congratulationsPartOne + playerTwoName + congratulationsPartTwo;
      textView.setText(personalisedCongratulations);
    } else if (winningIdentifier == PLAYER_TWO_TURN_IDENTIFIER) {
      String personalisedCongratulations = playerTwoName + congratulationsPartOne + playerOneName + congratulationsPartTwo;
      textView.setText(personalisedCongratulations);
    }

    // Reset finished game state
    resetSavedGameState();
    saveTwoPlayerGameMarker();
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
        FileOutputStream fileOutputStream = openFileOutput("playeroneradar", Context.MODE_PRIVATE);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(null);
        objectOutputStream.close();
        fileOutputStream.close();
      } catch (Exception e) {
        e.printStackTrace();
      }

      try {
        FileOutputStream fileOutputStream = openFileOutput("playertworadar", Context.MODE_PRIVATE);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(null);
        objectOutputStream.close();
        fileOutputStream.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
  }

  private void saveTwoPlayerGameMarker() {
      // Save identifier that a saved game exists
      try {
        FileOutputStream fileOutputStream = openFileOutput("twoplayersavedgame", Context.MODE_PRIVATE);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream (fileOutputStream);
        objectOutputStream.writeBoolean(false);
        objectOutputStream.close();
        fileOutputStream.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
  }

  private void alternateFirstTurn() {
    if (firstTurnIdentifier == PLAYER_ONE_TURN_IDENTIFIER) {
      firstTurnIdentifier = PLAYER_TWO_TURN_IDENTIFIER;
    } else {
      firstTurnIdentifier = PLAYER_ONE_TURN_IDENTIFIER;
    }
  }

  // Play again click handler
  public void onPlayAgainButtonClicked(View view) {
    Intent intent;
    if (playerOnePlaceFleet || playerTwoPlaceFleet) {
      // Start FleetPlacement activity
      intent = new Intent(getApplicationContext(), TwoPlayerFleetPlacementActivity.class);
    } else {
      // Start TwoPlayerGame activity
      intent = new Intent(getApplicationContext(), TwoPlayerGameActivity.class);
    }

    // Get player name and fleet placement preference
    intent.putExtra("PlayerOneName", playerOneName);
    intent.putExtra("PlayerTwoName", playerTwoName);
    intent.putExtra("PlayerOnePlaceFleet", playerOnePlaceFleet);
    intent.putExtra("PlayerTwoPlaceFleet", playerTwoPlaceFleet);
    intent.putExtra("FirstTurnIdentifier", firstTurnIdentifier);
    startActivity(intent);
  }

  // Return to menu button click handler
  public void onReturnToMenuButtonClicked(View view) {
    // Start Main menu activity
    Intent intent = new Intent(getApplicationContext(), MainMenu.class);
    startActivity(intent);
  }
}
