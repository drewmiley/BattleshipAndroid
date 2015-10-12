package com.scottlogic.dmiley.battleship.activities.twoplayer;

import com.scottlogic.dmiley.battleship.R;
import com.scottlogic.dmiley.battleship.activities.setting.SettingsActivity;
import com.scottlogic.dmiley.battleship.gridview.fleet.Fleet;
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
import com.scottlogic.dmiley.battleship.util.GridLocation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;

// TwoPlayerGame activity
public class TwoPlayerGameActivity extends ActionBarActivity implements SensorEventListener, SelectionChangedListener, ShipHitListener, ShipSankListener, FleetSankListener {

  private GameGridView gameGrid;
  private Button fireButton;
  private Button toggleViewButton;
  private Button continueGameButton;
  private Button readyButton;
  private TextView passDeviceText;
  private TextView countdown;
  private Toast toast;

  private int radarColorSchemeInteger;
  private int startingRadarColorSchemeInteger;

  private CountDownTimer countdownTimer;
  private SensorManager sensorManager;
  private Sensor accelerometer;

  private String playerOneName;
  private String playerTwoName;
  private boolean playerOnePlaceFleet;
  private boolean playerTwoPlaceFleet;
  private int firstTurnIdentifier;
  private int playerTurnIdentifier;
  private int winningIdentifier;

  private boolean viewingRadar;
  private GridLocation toggleViewRadarSelectedCell;
  private boolean fleetSunkCalled;
  private RadarModel playerOneRadar;
  private RadarModel playerTwoRadar;

  private final static int MAX_NAME_LENGTH = 30;
  private final static int PLAYER_ONE_TURN_IDENTIFIER = 1;
  private final static int PLAYER_TWO_TURN_IDENTIFIER = 2;
  private final static int BLUE_WHITE_COLOR_SCHEME = 0;
  private final static int GREEN_BLACK_COLOR_SCHEME = 1;

  @Override
  public void onBackPressed() {
    Intent intent = new Intent(getApplicationContext(), TwoPlayerEntryActivity.class);
    startActivity(intent);
  }

  // Initialise activity
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Delete complete fleet placement save
    resetFleetPlacementSave();
    // Initialise the layout
    setContentView(R.layout.activity_two_player_game);
    toast = Toast.makeText(getApplicationContext(), null, Toast.LENGTH_SHORT);
    //Add sensor
    sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    sensorManager.registerListener(this, accelerometer , SensorManager.SENSOR_DELAY_NORMAL);
    // Get views from Layout
    gameGrid = (GameGridView) findViewById(R.id.twoPlayerRadarGrid);
    fireButton = (Button) findViewById(R.id.two_player_fire_button);
    toggleViewButton = (Button) findViewById(R.id.two_player_toggle_view_button);
    continueGameButton = (Button) findViewById(R.id.two_player_continue_game_button);
    readyButton = (Button) findViewById(R.id.ready_fire_button);
    passDeviceText = (TextView) findViewById(R.id.ready_text);
    countdown = (TextView) findViewById(R.id.countdownTextView);
    // Initialise game logic and player name, load unfinished game if present
    Intent intent = getIntent();
    firstTurnIdentifier = intent.getIntExtra("FirstTurnIdentifier", PLAYER_ONE_TURN_IDENTIFIER);
    setPlayerTurnIdentifier(firstTurnIdentifier);

    gameGrid.setFleetPlacementShown(false);
    setViewingRadar(true);
    setFleetSunkCalled(false);
    updateStartOfPassUIState();

    if (intent.getBooleanExtra("ResumeTwoPlayerGame", false)) {
      // Load Player name
      loadPlayers();
      // Load Game state
      loadGameState();
    } else {
      playerOneName = intent.getStringExtra("PlayerOneName");
      playerTwoName = intent.getStringExtra("PlayerTwoName");
      playerOnePlaceFleet = intent.getBooleanExtra("PlayerOnePlaceFleet", false);
      playerTwoPlaceFleet = intent.getBooleanExtra("PlayerTwoPlaceFleet", false);
      Fleet playerOneFleet = intent.getParcelableExtra("PlayerOneFleetPlacement");
      Fleet playerTwoFleet = intent.getParcelableExtra("PlayerTwoFleetPlacement");

      OceanModel playerOneFleetPlacement = playerTwoFleet != null ?
        new OceanModel(playerTwoFleet) : new OceanModel();
      SearchedGridModel playerOneSearchedGrid = new SearchedGridModel();
      playerOneRadar = new RadarModel(playerOneFleetPlacement, playerOneSearchedGrid);

      OceanModel playerTwoFleetPlacement = playerOneFleet != null ?
        new OceanModel(playerOneFleet) : new OceanModel();
      SearchedGridModel playerTwoSearchedGrid = new SearchedGridModel();
      playerTwoRadar = new RadarModel(playerTwoFleetPlacement, playerTwoSearchedGrid);
      }
    // Hook up touch events
    gameGrid.addSelectionChangedListener(this);
    // Set loaded UI
    startingRadarColorSchemeInteger = BLUE_WHITE_COLOR_SCHEME;
    startNextTurn();
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
  protected void onResume() {
    super.onResume();

    //Set options
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    String radarColorSchemeString = sharedPreferences.getString("twoPlayerRadarColorScheme", "1");
    radarColorSchemeInteger = Integer.parseInt(radarColorSchemeString);

    updateToggleButtonState();
    // Set color scheme to radar view
    if (radarColorSchemeInteger != startingRadarColorSchemeInteger) {
      gameGrid.swapColorScheme();
      startingRadarColorSchemeInteger = radarColorSchemeInteger;
    }

    // Hook up model events
    sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

    playerOneRadar.addShipHitListener(this);
    playerOneRadar.addShipSunkListener(this);
    playerOneRadar.addFleetSunkListener(this);

    playerTwoRadar.addShipHitListener(this);
    playerTwoRadar.addShipSunkListener(this);
    playerTwoRadar.addFleetSunkListener(this);

    gameGrid.invalidate();

    // Calls fleet sunk listener if needed.
    playerOneRadar.checkShipsLeft();
    playerTwoRadar.checkShipsLeft();
  }

  @Override
  protected void onPause() {
    super.onPause();

    // Unhook model events
    sensorManager.unregisterListener(this);

    playerOneRadar.removeShipHitListener(this);
    playerOneRadar.removeShipSunkListener(this);
    playerOneRadar.removeFleetSunkListener(this);

    playerTwoRadar.removeShipHitListener(this);
    playerTwoRadar.removeShipSunkListener(this);
    playerTwoRadar.removeFleetSunkListener(this);

    // Save player name
    savePlayers();
    // Save playerOneRadar & playerTwoRadar
    saveGameState();
    saveTwoPlayerGameMarker();
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

  private int getPlayerTurnIdentifier() {
    return playerTurnIdentifier;
  }

  private void setPlayerTurnIdentifier(int playerTurnIdentifier) {
    this.playerTurnIdentifier = playerTurnIdentifier;
  }

  public boolean isFleetSunkCalled() {
    return fleetSunkCalled;
  }

  public void setFleetSunkCalled(boolean fleetSunkCalled) {
    this.fleetSunkCalled = fleetSunkCalled;
  }

  public boolean isViewingRadar() {
    return viewingRadar;
  }

  public void setViewingRadar(boolean viewingRadar) {
    this.viewingRadar = viewingRadar;
  }

  private void loadPlayers() {
    try {
      FileInputStream fileInputStream = openFileInput("playeronename");
      InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);

      char[] stringBuffer = new char[MAX_NAME_LENGTH];
      int readCharacters = inputStreamReader.read(stringBuffer);
      playerOneName = String.copyValueOf(stringBuffer, 0, readCharacters);

      inputStreamReader.close();
      fileInputStream.close();
    } catch (Exception e) {
      playerOneName = getString(R.string.player_one_default_name);
    }

    try {
      FileInputStream fileInputStream = openFileInput("playeroneplacefleetpreference");
      ObjectInputStream objectInputStream = new ObjectInputStream (fileInputStream);
      playerOnePlaceFleet = objectInputStream.readBoolean();
      objectInputStream.close();
      fileInputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      FileInputStream fileInputStream = openFileInput("playertwoname");
      InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);

      char[] stringBuffer = new char[MAX_NAME_LENGTH];
      int readCharacters = inputStreamReader.read(stringBuffer);
      playerTwoName = String.copyValueOf(stringBuffer, 0, readCharacters);

      inputStreamReader.close();
      fileInputStream.close();
    } catch (Exception e) {
      playerTwoName = getString(R.string.player_two_default_name);
    }

    try {
      FileInputStream fileInputStream = openFileInput("playertwoplacefleetpreference");
      ObjectInputStream objectInputStream = new ObjectInputStream (fileInputStream);
      playerTwoPlaceFleet = objectInputStream.readBoolean();
      objectInputStream.close();
      fileInputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void loadGameState() {
    try {
      FileInputStream fileInputStream = openFileInput("playeroneradar");
      ObjectInputStream objectInputStream = new ObjectInputStream (fileInputStream);
      Object playerOneRadarReadObject = objectInputStream.readObject();
      objectInputStream.close();
      fileInputStream.close();
      if (playerOneRadarReadObject instanceof RadarModel) {
        playerOneRadar = (RadarModel) playerOneRadarReadObject;
      }
    } catch (Exception e) {
      OceanModel playerOneRandomFleetPlacement = new OceanModel();
      SearchedGridModel playerOneSearchedGrid = new SearchedGridModel();
      playerOneRadar = new RadarModel(playerOneRandomFleetPlacement, playerOneSearchedGrid);
    }

    try {
      FileInputStream fileInputStream = openFileInput("playertworadar");
      ObjectInputStream objectInputStream = new ObjectInputStream (fileInputStream);
      Object playerTwoRadarReadObject = objectInputStream.readObject();
      objectInputStream.close();
      fileInputStream.close();
      if (playerTwoRadarReadObject instanceof RadarModel) {
        playerTwoRadar = (RadarModel) playerTwoRadarReadObject;
      }
    } catch (Exception e) {
      OceanModel playerTwoRandomFleetPlacement = new OceanModel();
      SearchedGridModel playerTwoSearchedGrid = new SearchedGridModel();
      playerTwoRadar = new RadarModel(playerTwoRandomFleetPlacement, playerTwoSearchedGrid);
    }

    // Setup whose turn it is currently
    try {
      FileInputStream fileInputStream = openFileInput("turnidentifier");
      InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);

      char[] stringBuffer = new char[2];
      int readCharacters = inputStreamReader.read(stringBuffer);

      String turnIdentifier = String.copyValueOf(stringBuffer, 0, readCharacters);
      setPlayerTurnIdentifier(Integer.parseInt(turnIdentifier.substring(1)));

      inputStreamReader.close();
      fileInputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void savePlayers() {
    try {
      FileOutputStream fileOutputStream = openFileOutput("playeronename", Context.MODE_PRIVATE);
      OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
      outputStreamWriter.write(playerOneName);
      outputStreamWriter.close();
      fileOutputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      FileOutputStream fileOutputStream = openFileOutput("playeroneplacefleetpreference", Context.MODE_PRIVATE);
      ObjectOutputStream objectOutputStream = new ObjectOutputStream (fileOutputStream);
      objectOutputStream.writeBoolean(playerOnePlaceFleet);
      objectOutputStream.close();
      fileOutputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      FileOutputStream fileOutputStream = openFileOutput("playertwoname", Context.MODE_PRIVATE);
      OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
      outputStreamWriter.write(playerTwoName);
      outputStreamWriter.close();
      fileOutputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      FileOutputStream fileOutputStream = openFileOutput("playertwoplacefleetpreference", Context.MODE_PRIVATE);
      ObjectOutputStream objectOutputStream = new ObjectOutputStream (fileOutputStream);
      objectOutputStream.writeBoolean(playerTwoPlaceFleet);
      objectOutputStream.close();
      fileOutputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void saveGameState() {
    try {
      FileOutputStream fileOutputStream = openFileOutput("playeroneradar", Context.MODE_PRIVATE);
      ObjectOutputStream objectOutputStream = new ObjectOutputStream (fileOutputStream);
      objectOutputStream.writeObject(playerOneRadar);
      objectOutputStream.close();
      fileOutputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      FileOutputStream fileOutputStream = openFileOutput("playertworadar", Context.MODE_PRIVATE);
      ObjectOutputStream objectOutputStream = new ObjectOutputStream (fileOutputStream);
      objectOutputStream.writeObject(playerTwoRadar);
      objectOutputStream.close();
      fileOutputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Save identifier of whose turn it is
    try {
      String turnIdentifier = "P" + getPlayerTurnIdentifier();
      FileOutputStream fileOutputStream = openFileOutput("turnidentifier", Context.MODE_PRIVATE);
      OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
      outputStreamWriter.write(turnIdentifier);
      outputStreamWriter.close();
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
      objectOutputStream.writeBoolean(true);
      objectOutputStream.close();
      fileOutputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    Sensor sensor = event.sensor;
    if (sensor.getType() == Sensor.TYPE_ACCELEROMETER &&
      passDeviceText.getVisibility() == View.VISIBLE) {
      float xAcceleration = event.values[0];
      float yAcceleration = event.values[1];
      float zAcceleration = event.values[2];

      double acceleration = Math.sqrt(xAcceleration * xAcceleration +
        yAcceleration * yAcceleration + zAcceleration * zAcceleration);
      double GRAVITY = 9.80665;
      if (Math.abs(acceleration - GRAVITY) > 7) {
        countdownTimer.onFinish();
        countdownTimer.cancel();
      }
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) { }

  // Handles cell touch on RadarGrid
  public void onSelectionChanged(SelectionChangedEvent selectionChangedEvent) {
    boolean validCellSelected;
    if (getPlayerTurnIdentifier() == PLAYER_ONE_TURN_IDENTIFIER) {
      validCellSelected = playerOneRadar.validCellSelected(gameGrid.getSelectedCell());
    } else {
      validCellSelected = playerTwoRadar.validCellSelected(gameGrid.getSelectedCell());
    }

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
    boolean hit;
    if (getPlayerTurnIdentifier() == PLAYER_ONE_TURN_IDENTIFIER) {
      hit = playerOneRadar.getCurrentGuessCellType().getIsShip();
    } else {
      hit = playerTwoRadar.getCurrentGuessCellType().getIsShip();
    }
    String toastText = hit ? getString(R.string.hit) : getString(R.string.miss);
    toastText.setText(toastText);
    toast.show();
    updateUI();
  }

  // Handles shipSank event
  public void onShipSank(ShipSankEvent shipSankEvent) {
    shipSank();
  }

  // Called when ship sunk but fleet not sunk
  private void shipSank() {
    String cellTypeSunk;
    if (getPlayerTurnIdentifier() == PLAYER_ONE_TURN_IDENTIFIER) {
      cellTypeSunk = playerOneRadar.getCurrentGuessCellType().getName();
    } else {
      cellTypeSunk = playerTwoRadar.getCurrentGuessCellType().getName();
    }
    String reportShipSunkTextMessage = getString(R.string.hit) + ". " +
      cellTypeSunk + " " + getString(R.string.sunk);
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
    String cellTypeSunk;
    if (getPlayerTurnIdentifier() == PLAYER_ONE_TURN_IDENTIFIER) {
      cellTypeSunk = playerOneRadar.getCurrentGuessCellType().getName();
    } else {
      cellTypeSunk = playerTwoRadar.getCurrentGuessCellType().getName();
    }
    String reportFleetSunkTextMessage = getString(R.string.hit) + ". " +
      cellTypeSunk + " " + getString(R.string.sunk) + "\n";
    reportFleetSunkTextMessage += getString(R.string.fleet_sunk);
    // Displays text popup informing user of fleet sinking.
    toast.setText(reportFleetSunkTextMessage);
    toast.show();
    winningIdentifier = getPlayerTurnIdentifier();
    setFleetSunkCalled(true);
    updateUI();

    if (winningIdentifier == PLAYER_ONE_TURN_IDENTIFIER) {
      setTitle(playerOneName + " " + getString(R.string.fleet_sunk_title_suffix));
    } else {
      setTitle(playerTwoName + " " + getString(R.string.fleet_sunk_title_suffix));
    }
    fireButton.setEnabled(false);
    continueGameButton.setText(getString(R.string.finish_game_button));
  }

  // Reevaluate fireButton state
  private void invalidateFireButtonState(boolean validCellSelected) {
    fireButton.setEnabled(validCellSelected);
  }

  // Updates the fire button and grid state
  private void updateUI() {
    boolean validCellSelected;
    if (getPlayerTurnIdentifier() == PLAYER_ONE_TURN_IDENTIFIER) {
      validCellSelected = playerOneRadar.validCellSelected(gameGrid.getSelectedCell());
    } else {
      validCellSelected = playerTwoRadar.validCellSelected(gameGrid.getSelectedCell());
    }

    invalidateFireButtonState(validCellSelected);
    gameGrid.updateCursorDisplay(validCellSelected);
    updateEndOfAimButtonState();

    if (getPlayerTurnIdentifier() == PLAYER_ONE_TURN_IDENTIFIER) {
      gameGrid.setShotDataDisplay(playerOneRadar.display());
    } else {
      gameGrid.setShotDataDisplay(playerTwoRadar.display());
    }
    // For resuming the game, as the current player's shot has been fired
    selectNextPlayerTurn();
  }

  private void setOceanButtonState() {
    fireButton.setVisibility(View.INVISIBLE);
    gameGrid.setFleetPlacementShown(true);
    gameGrid.deactivateUI();
  }

  private void setRadarButtonState() {
    fireButton.setVisibility(View.VISIBLE);
    gameGrid.setFleetPlacementShown(false);
    gameGrid.activateUI();
  }

  private void updateToggleButtonState() {
    if (radarColorSchemeInteger == GREEN_BLACK_COLOR_SCHEME) {
      if (isViewingRadar()) {
        toggleViewButton.setBackgroundColor(Color.parseColor("#81b5ed"));
        toggleViewButton.setTextColor(Color.parseColor("#000000"));
        toggleViewButton.setText(R.string.ocean);
      } else {
        toggleViewButton.setBackgroundColor(Color.parseColor("#000000"));
        toggleViewButton.setTextColor(Color.parseColor("#00ff00"));
        toggleViewButton.setText(R.string.radar);
      }
    } else {
      if (isViewingRadar()) {
        toggleViewButton.setTextColor(Color.parseColor("#000000"));
        toggleViewButton.setText(R.string.view_ocean);
      } else {
        toggleViewButton.setTextColor(Color.parseColor("#000000"));
        toggleViewButton.setText(R.string.view_radar);
      }
    }
  }

  private void selectNextPlayerTurn() {
    if (getPlayerTurnIdentifier() == PLAYER_ONE_TURN_IDENTIFIER) {
      setPlayerTurnIdentifier(PLAYER_TWO_TURN_IDENTIFIER);
    } else {
      setPlayerTurnIdentifier(PLAYER_ONE_TURN_IDENTIFIER);
    }
  }

  private void startNextTurn() {
    updateStartOfPassUIState();

    // Set display for player turn
    if (getPlayerTurnIdentifier() == PLAYER_ONE_TURN_IDENTIFIER) {
      gameGrid.setShotDataDisplay(playerOneRadar.display());
      String playerOneTurnPrompt = playerOneName + getString(R.string.ready_fire_text);
      setTitle(playerOneTurnPrompt);
    } else {
      gameGrid.setShotDataDisplay(playerTwoRadar.display());
      String playerTwoTurnPrompt = playerTwoName + getString(R.string.ready_fire_text);
      setTitle(playerTwoTurnPrompt);
    }
  }

  private void updateStartOfAimButtonState() {
    toggleViewButton.setEnabled(true);
    continueGameButton.setVisibility(View.INVISIBLE);
    continueGameButton.setEnabled(false);
  }

  private void updateEndOfAimButtonState() {
    toggleViewButton.setVisibility(View.INVISIBLE);
    continueGameButton.setVisibility(View.VISIBLE);
    continueGameButton.setEnabled(true);
  }

  private void updateStartOfPassUIState() {
    gameGrid.setVisibility(View.GONE);
    continueGameButton.setVisibility(View.GONE);
    fireButton.setVisibility(View.GONE);
    toggleViewButton.setVisibility(View.GONE);

    //readyButton.setVisibility(View.VISIBLE);
    countdown.setVisibility(View.VISIBLE);

    if (getPlayerTurnIdentifier() == PLAYER_ONE_TURN_IDENTIFIER) {
      String readyText = playerTwoName + getString(R.string.pass_text) + "\n" +
        playerOneName + getString(R.string.ready_fire_text);
      passDeviceText.setText(readyText);
    } else {
      String readyText = playerOneName + getString(R.string.pass_text) + "\n" +
        playerTwoName + getString(R.string.ready_fire_text);
      passDeviceText.setText(readyText);
    }
    passDeviceText.setVisibility(View.VISIBLE);
    countdownTimer = new CountDownTimer(2999, 1000) {
        public void onTick(long millisUntilFinished) {
          countdown.setText(Integer.toString((int) (millisUntilFinished / 1000)));
        }
        public void onFinish() {
          countdown.setVisibility(View.GONE);
          readyToAim();
        }
    }.start();
  }

  private void updateEndOfPassUIState() {
    gameGrid.setVisibility(View.VISIBLE);
    continueGameButton.setVisibility(View.INVISIBLE);
    fireButton.setVisibility(View.VISIBLE);
    toggleViewButton.setVisibility(View.VISIBLE);
    // Activate UI
    gameGrid.activateUI();

    //readyButton.setVisibility(View.GONE);
    passDeviceText.setVisibility(View.GONE);
  }

  // Continue game click handler
  public void onContinueGameButtonClicked(View view) {
    continueGame();
  }

  private void continueGame() {
    // Stop showing current toast popup
    toast.cancel();

    if (isFleetSunkCalled()) {
      // Start TwoPlayerPostGame activity
      Intent intent = new Intent(getApplicationContext(), TwoPlayerPostGameActivity.class);

      // Get player name and whose turn it was first & send to next activity
      intent.putExtra("PlayerOneName", playerOneName);
      intent.putExtra("PlayerTwoName", playerTwoName);
      intent.putExtra("PlayerOnePlaceFleet", playerOnePlaceFleet);
      intent.putExtra("PlayerTwoPlaceFleet", playerTwoPlaceFleet);
      intent.putExtra("FirstTurnIdentifier", firstTurnIdentifier);
      intent.putExtra("WinningIdentifier", winningIdentifier);

      startActivity(intent);
    } else {
      startNextTurn();
    }
  }

  public void onReadyToAimButtonClicked(View view) {
    readyToAim();
  }

  private void readyToAim() {
    updateEndOfPassUIState();
    updateStartOfAimButtonState();

    // Set display for player turn
    if (getPlayerTurnIdentifier() == PLAYER_ONE_TURN_IDENTIFIER) {
      gameGrid.setShotDataDisplay(playerOneRadar.display());
      String playerOneTurnPrompt = playerOneName + getString(R.string.shot_title_suffix);
      setTitle(playerOneTurnPrompt);
    } else {
      gameGrid.setShotDataDisplay(playerTwoRadar.display());
      String playerTwoTurnPrompt = playerTwoName + getString(R.string.shot_title_suffix);
      setTitle(playerTwoTurnPrompt);
    }
  }

  public void onFireButtonClicked(View view) {
    fire();
  }

  // Fires a shot at the selected cell
  private void fire() {
    //Simulate the shot, Update the UI
    if (getPlayerTurnIdentifier() == PLAYER_ONE_TURN_IDENTIFIER) {
      String playerOneShotTakenPrompt = playerOneName + getString(R.string.shot_taken_title_suffix);
      setTitle(playerOneShotTakenPrompt);
      playerOneRadar.takeShot(gameGrid.getSelectedCell());
    } else {
      String playerTwoShotTakenPrompt = playerTwoName + getString(R.string.shot_taken_title_suffix);
      setTitle(playerTwoShotTakenPrompt);
      playerTwoRadar.takeShot(gameGrid.getSelectedCell());
    }
    gameGrid.deactivateUI();
  }

  public void onToggleViewButtonClicked(View view) {
    toggleView();
  }

  // Toggles View between your ocean and radar
  private void toggleView() {
    updateToggleButtonState();
    if (radarColorSchemeInteger == GREEN_BLACK_COLOR_SCHEME) {
      gameGrid.swapColorScheme();
    }
    if (isViewingRadar()) {
      toggleViewRadarSelectedCell = gameGrid.getSelectedCell();
      // Set display to ocean
      if (getPlayerTurnIdentifier() == PLAYER_ONE_TURN_IDENTIFIER) {
        gameGrid.setShotDataDisplay(playerTwoRadar.display());
      } else {
        gameGrid.setShotDataDisplay(playerOneRadar.display());
      }
      //gameGrid.invalidate();
      // Set display for player turn
      if (getPlayerTurnIdentifier() == PLAYER_ONE_TURN_IDENTIFIER) {
          setTitle(playerOneName + getString(R.string.survey_fleet_title_suffix));
      } else {
          setTitle(playerTwoName + getString(R.string.survey_fleet_title_suffix));
      }
      setOceanButtonState();
    } else {
      if (getPlayerTurnIdentifier() == PLAYER_ONE_TURN_IDENTIFIER) {
        gameGrid.setShotDataDisplay(playerOneRadar.display());
      } else {
        gameGrid.setShotDataDisplay(playerTwoRadar.display());
      }
      gameGrid.setSelectedCell(toggleViewRadarSelectedCell);
      //gameGrid.invalidate();
      // Set display for player turn
      if (getPlayerTurnIdentifier() == PLAYER_ONE_TURN_IDENTIFIER) {
        setTitle(playerOneName + getString(R.string.shot_title_suffix));
      } else {
        setTitle(playerTwoName + getString(R.string.shot_title_suffix));
      }
      setRadarButtonState();
    }
    setViewingRadar(!isViewingRadar());
  }
}
