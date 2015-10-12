package com.scottlogic.dmiley.battleship.activities.twoplayer;

import com.scottlogic.dmiley.battleship.R;
import com.scottlogic.dmiley.battleship.activities.menu.MainMenu;
import com.scottlogic.dmiley.battleship.activities.setting.SettingsActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class TwoPlayerEntryActivity extends ActionBarActivity {

  private boolean playerOnePlaceFleet;
  private boolean playerTwoPlaceFleet;

  private EditText editTextOne;
  private EditText editTextTwo;
  private Button startGameButton;

  @Override
  public void onBackPressed() {
    Intent intent = new Intent(getApplicationContext(), MainMenu.class);
    startActivity(intent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_two_player_entry);

    editTextOne = (EditText) findViewById(R.id.edit_player_one_name);
    editTextTwo = (EditText) findViewById(R.id.edit_player_two_name);

    resetFleetPlacementSave();

    startGameButton = (Button) findViewById(R.id.start_game_button);
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
    String playerOneFleetPreferenceBooleanString = sharedPreferences.getString("playerOneDefaultFleetPlacement", "1");
    int playerOneFleetPreferenceBooleanInteger = Integer.parseInt(playerOneFleetPreferenceBooleanString);
    if (playerOneFleetPreferenceBooleanInteger == 0) {
        RadioButton click = (RadioButton) findViewById(R.id.player_one_fleet_placement_preference_false);
        click.performClick();
    } else {
        RadioButton click = (RadioButton) findViewById(R.id.player_one_fleet_placement_preference_true);
        click.performClick();
    }
    String playerTwoDefaultName = sharedPreferences.getString("playerTwoDefaultName", "");
    String playerTwoFleetPreferenceBooleanString = sharedPreferences.getString("playerTwoDefaultFleetPlacement", "1");
    int playerTwoFleetPreferenceBooleanInteger = Integer.parseInt(playerTwoFleetPreferenceBooleanString);
    if (playerTwoFleetPreferenceBooleanInteger == 0) {
        RadioButton click = (RadioButton) findViewById(R.id.player_two_fleet_placement_preference_false);
        click.performClick();
    } else {
        RadioButton click = (RadioButton) findViewById(R.id.player_two_fleet_placement_preference_true);
        click.performClick();
    }

    if (!playerOneDefaultName.equals("")) {
        editTextOne.setHint(playerOneDefaultName);
    } else {
        editTextOne.setHint(getString(R.string.player_one_default_name));
    }

    if (!playerTwoDefaultName.equals("")) {
        editTextTwo.setHint(playerTwoDefaultName);
    } else {
        editTextTwo.setHint(getString(R.string.player_two_default_name));
    }
  }

  private void resetFleetPlacementSave() {
      try {
        FileOutputStream fileOutputStream = openFileOutput("playeronefleetplacement", Context.MODE_PRIVATE);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream (fileOutputStream);
        objectOutputStream.writeObject(null);
        objectOutputStream.close();
        fileOutputStream.close();
      } catch (Exception e) {
        e.printStackTrace();
      }

      try {
        FileOutputStream fileOutputStream = openFileOutput("playertwofleetplacement", Context.MODE_PRIVATE);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream (fileOutputStream);
        objectOutputStream.writeObject(null);
        objectOutputStream.close();
        fileOutputStream.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
  }

  public void onPlayerOneWantToPlaceFleetButtonClicked(View view) {
    playerOnePlaceFleet = true;
  }

  public void onPlayerOneDontWantToPlaceFleetButtonClicked(View view) {
    playerOnePlaceFleet = false;
  }

  public void onPlayerTwoWantToPlaceFleetButtonClicked(View view) {
    playerTwoPlaceFleet = true;
  }

  public void onPlayerTwoDontWantToPlaceFleetButtonClicked(View view) {
    playerTwoPlaceFleet = false;
  }

  // Start Game button click handler
  public void onStartGameButtonClicked(View view) {
    Intent intent;
    if (playerOnePlaceFleet || playerTwoPlaceFleet) {
        // Start FleetPlacement activity
        intent = new Intent(getApplicationContext(), TwoPlayerFleetPlacementActivity.class);
    } else {
        // Start TwoPlayerGame activity
        intent = new Intent(getApplicationContext(), TwoPlayerGameActivity.class);
    }

    // Get player names & send to next activity
    String playerOneName = editTextOne.getText().toString();
    if (playerOneName.equals("")) {
        playerOneName = (String) editTextOne.getHint();
    }
    intent.putExtra("PlayerOneName", playerOneName);
    intent.putExtra("PlayerOnePlaceFleet", playerOnePlaceFleet);

    String playerTwoName = editTextTwo.getText().toString();
    if (playerTwoName.equals("")) {
        playerTwoName = (String) editTextTwo.getHint();
    }
    intent.putExtra("PlayerTwoName", playerTwoName);
    intent.putExtra("PlayerTwoPlaceFleet", playerTwoPlaceFleet);

    // Boolean to notify of whether we want to resume a previous game (in this case not)
    intent.putExtra("ResumeTwoPlayerGame", false);
    int firstTurnIdentifier = (int) Math.ceil(2 * Math.random());
    intent.putExtra("FirstTurnIdentifier", firstTurnIdentifier);

    startActivity(intent);
  }
}
