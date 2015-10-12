package com.scottlogic.dmiley.battleship.activities.oneplayersolo;

import com.scottlogic.dmiley.battleship.R;
import com.scottlogic.dmiley.battleship.activities.setting.SettingsActivity;
import com.scottlogic.dmiley.battleship.logic.event.FleetSankEvent;
import com.scottlogic.dmiley.battleship.logic.event.FleetSankListener;
import com.scottlogic.dmiley.battleship.gridview.GameGridView;
import com.scottlogic.dmiley.battleship.gridview.event.SelectionChangedEvent;
import com.scottlogic.dmiley.battleship.gridview.event.SelectionChangedListener;
import com.scottlogic.dmiley.battleship.logic.OceanModel;
import com.scottlogic.dmiley.battleship.logic.RadarModel;
import com.scottlogic.dmiley.battleship.logic.SearchedGridModel;
import com.scottlogic.dmiley.battleship.logic.event.ShipHitEvent;
import com.scottlogic.dmiley.battleship.logic.event.ShipHitListener;
import com.scottlogic.dmiley.battleship.logic.event.ShipSankEvent;
import com.scottlogic.dmiley.battleship.logic.event.ShipSankListener;

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
import android.widget.Toast;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;

// OnePlayerGame activity, used to play a one player game.
public class OnePlayerSoloGameActivity extends ActionBarActivity implements SelectionChangedListener, ShipHitListener, ShipSankListener, FleetSankListener {

  private GameGridView gameGrid;
  private Button fireButton;
  private Button finishGameButton;
  private Toast toast;

  private int radarColorSchemeInteger;
  private int startingRadarColorSchemeInteger;

  private String playerName;
  private RadarModel onePlayerRadar;

  private final static int MAX_NAME_LENGTH = 30;

  private final static int BLUE_WHITE_COLOR_SCHEME = 0;
  private final static int GREEN_BLACK_COLOR_SCHEME = 1;

  // Initialise activity
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Initialise the layout
    setContentView(R.layout.activity_one_player_game);
    toast = Toast.makeText(getApplicationContext(), null, Toast.LENGTH_SHORT);
    // Get views from Layout
    gameGrid = (GameGridView) findViewById(R.id.onePlayerRadarGrid);
    fireButton = (Button) findViewById(R.id.one_player_fire_button);
    finishGameButton = (Button) findViewById(R.id.one_player_finish_game_button);
    // Initialise game logic and player name, load unfinished game if present
    Intent intent = getIntent();
    if (intent.getBooleanExtra("ResumeOnePlayerSoloGame", false)) {
        // Load Player name
        loadPlayerName();
        // Load Game state
        loadGameState();
    } else {
        playerName = intent.getStringExtra("PlayerName");
        OceanModel randomFleetPlacement = new OceanModel();
        SearchedGridModel searchedGrid = new SearchedGridModel();
        onePlayerRadar = new RadarModel(randomFleetPlacement, searchedGrid);
    }
    gameGrid.setShotDataDisplay(onePlayerRadar.display());
    // Hook up touch events
    gameGrid.addSelectionChangedListener(this);

    startingRadarColorSchemeInteger = BLUE_WHITE_COLOR_SCHEME;
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

  private void loadPlayerName() {
    try {
      FileInputStream fileInputStream = openFileInput("oneplayername");
      InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);

      char[] stringBuffer = new char[MAX_NAME_LENGTH];
      int readCharacters = inputStreamReader.read(stringBuffer);
      playerName = String.copyValueOf(stringBuffer, 0, readCharacters);

      inputStreamReader.close();
      fileInputStream.close();
    } catch (Exception e) {
      playerName = getString(R.string.player_one_default_name);
    }
  }

  private void loadGameState() {
    try {
      FileInputStream fileInputStream = openFileInput("oneplayerradar");
      ObjectInputStream objectInputStream = new ObjectInputStream (fileInputStream);
      Object onePlayerRadarReadObject = objectInputStream.readObject();
      objectInputStream.close();
      fileInputStream.close();
      if (onePlayerRadarReadObject instanceof RadarModel) {
        onePlayerRadar = (RadarModel) onePlayerRadarReadObject;
      }
    } catch (Exception e) {
      OceanModel randomFleetPlacement = new OceanModel();
      SearchedGridModel searchedGrid = new SearchedGridModel();
      onePlayerRadar = new RadarModel(randomFleetPlacement, searchedGrid);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    //Set options
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    String radarColorSchemeString = sharedPreferences.getString("onePlayerRadarColorScheme", "0");
    radarColorSchemeInteger = Integer.parseInt(radarColorSchemeString);
    // Set color scheme to radar view
    if (radarColorSchemeInteger != startingRadarColorSchemeInteger) {
        gameGrid.swapColorScheme();
        startingRadarColorSchemeInteger = radarColorSchemeInteger;
    }
    // Hook up model events
    onePlayerRadar.addShipHitListener(this);
    onePlayerRadar.addShipSunkListener(this);
    onePlayerRadar.addFleetSunkListener(this);

    gameGrid.invalidate();
    // Calls fleet sunk listener if needed.
    onePlayerRadar.checkShipsLeft();
  }

  @Override
  protected void onPause() {
    super.onPause();

    // Unhook model events
    onePlayerRadar.removeShipHitListener(this);
    onePlayerRadar.removeShipSunkListener(this);
    onePlayerRadar.removeFleetSunkListener(this);

    // Save player name
    savePlayerName();
    // Save onePlayerRadar
    saveGameState();
    saveOnePlayerGameMarker();
  }

  private void savePlayerName() {
    try {
      FileOutputStream fileOutputStream = openFileOutput("oneplayername", Context.MODE_PRIVATE);
      OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
      outputStreamWriter.write(playerName);
      outputStreamWriter.close();
      fileOutputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void saveGameState() {
    try {
      FileOutputStream fileOutputStream = openFileOutput("oneplayerradar", Context.MODE_PRIVATE);
      ObjectOutputStream objectOutputStream = new ObjectOutputStream (fileOutputStream);
      objectOutputStream.writeObject(onePlayerRadar);
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
      objectOutputStream.writeBoolean(true);
      objectOutputStream.close();
      fileOutputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Handles cell touch on RadarGrid
  public void onSelectionChanged(SelectionChangedEvent selectionChangedEvent) {
    boolean validCellSelected = onePlayerRadar.validCellSelected(gameGrid.getSelectedCell());
    // When a cell is touched, consider whether fireButton should be enabled because cell selection has changed
    invalidateFireButtonState(validCellSelected);
    gameGrid.updateCursorDisplay(validCellSelected);
  }

  @Override
  public void onShipHit(ShipHitEvent shipHitEvent) {
    shipHit();
  }

  // Called when shot is taken and ship not sunk
  private void shipHit() {
    boolean hit = onePlayerRadar.getCurrentGuessCellType().getIsShip();
    String toastText =  onePlayerRadar.getCurrentGuessCellType().getIsShip() ?
      getString(R.string.hit) : getString(R.string.miss);
    toast.setText(toastText);
    toast.show();
    updateUI();
  }

  // Handles shipSank event
  public void onShipSank(ShipSankEvent shipSankEvent) {
    shipSank();
  }

  // Called when ship sunk but fleet not sunk
  private void shipSank() {
    String cellTypeSunk = onePlayerRadar.getCurrentGuessCellType().getName();
    String reportShipSunkTextMessage = "Hit. " + cellTypeSunk + " Sunk.";
    // Displays text popup informing user of ship sinking.
    toast.setText(reportShipSunkTextMessage);
    toast.show();
    updateUI();
  }

  // Handles fleetSunk event
  public void onFleetSank(FleetSankEvent fleetSankEvent) {
    fleetSank();
  }

  // Called when fleet sunk, deactivates fire button and grid
  private void fleetSank() {
    String cellTypeSunk = onePlayerRadar.getCurrentGuessCellType().getName();
    String reportFleetSunkTextMessage = "Hit. " + cellTypeSunk +
      " Sunk.\n" + "You sunk the fleet!";
    // Displays text popup informing user of fleet sinking.
    toast.setText(reportFleetSunkTextMessage);
    toast.show();

    updateUI();

    gameGrid.deactivateUI();
    fireButton.setEnabled(false);
    finishGameButton.setVisibility(View.VISIBLE);
    finishGameButton.setEnabled(true);
  }

  // Reevaluate fireButton state
  private void invalidateFireButtonState(boolean validCellSelected) {
    fireButton.setEnabled(validCellSelected);
  }

  // Updates the fire button and grid state
  private void updateUI() {
    boolean validCellSelected = onePlayerRadar.validCellSelected(gameGrid.getSelectedCell());
    invalidateFireButtonState(validCellSelected);
    gameGrid.updateCursorDisplay(validCellSelected);
    gameGrid.setShotDataDisplay(onePlayerRadar.display());
  }

  public void onFireButtonClicked(View view) {
    fire();
  }

  // Fires a shot at the selected cell
  private void fire() {
    //Simulate the shot, Update the UI
    onePlayerRadar.takeShot(gameGrid.getSelectedCell());
  }

  // Finish game click handler
  public void onFinishGameButtonClicked(View view) {
    // Start OnePlayerPostGame activity
    Intent intent = new Intent(getApplicationContext(), OnePlayerSoloPostGameActivity.class);
    // Get player name & send to next activity
    intent.putExtra("PlayerName", playerName);
    startActivity(intent);
  }
}
