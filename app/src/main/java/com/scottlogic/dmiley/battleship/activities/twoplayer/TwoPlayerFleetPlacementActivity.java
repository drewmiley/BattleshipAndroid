package com.scottlogic.dmiley.battleship.activities.twoplayer;

import com.scottlogic.dmiley.battleship.R;
import com.scottlogic.dmiley.battleship.activities.setting.SettingsActivity;
import com.scottlogic.dmiley.battleship.activities.twoplayer.spinner.StripArrayAdaptor;
import com.scottlogic.dmiley.battleship.gridview.FleetPlacementGridView;
import com.scottlogic.dmiley.battleship.gridview.event.SelectionChangedEvent;
import com.scottlogic.dmiley.battleship.gridview.event.SelectionChangedListener;
import com.scottlogic.dmiley.battleship.gridview.event.StripTouchedEvent;
import com.scottlogic.dmiley.battleship.gridview.event.StripTouchedListener;
import com.scottlogic.dmiley.battleship.gridview.fleet.Fleet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

//  FleetPlacement activity, used to place ship at start of game.
public class TwoPlayerFleetPlacementActivity extends ActionBarActivity implements SensorEventListener, SelectionChangedListener, StripTouchedListener, AdapterView.OnItemSelectedListener {

  private FleetPlacementGridView fleetPlacementGrid;
  private Button placeFleetButton;
  private Spinner fleetSpinner;
  private Button continueGameButton;

  private TextView passDeviceTextView;
  private Button readyToDeployButton;
  private TextView countdown;

  private Toast toast;

  private SensorManager sensorManager;
  private Sensor accelerometer;

  private CountDownTimer countdownTimer;

  private boolean resumedGame;
  private int firstTurnIdentifier;
  private String playerOneName;
  private String playerTwoName;
  private boolean playerOnePlaceFleet;
  private boolean playerTwoPlaceFleet;
  private Fleet playerOneFleetPlacement;
  private Fleet playerTwoFleetPlacement;

  private int playerTurnIdentifier;

  private final static int PLAYER_ONE_TURN_IDENTIFIER = 1;
  private final static int PLAYER_TWO_TURN_IDENTIFIER = 2;

  // Initialise activity
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Initialise the layout
    setContentView(R.layout.activity_two_player_fleet_placement);
    toast = Toast.makeText(getApplicationContext(), null, Toast.LENGTH_SHORT);

    //Add sensor
    sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    sensorManager.registerListener(this, accelerometer , SensorManager.SENSOR_DELAY_NORMAL);

    // Get views from Layout
    fleetPlacementGrid = (FleetPlacementGridView) findViewById(R.id.fleetPlacementGrid);
    placeFleetButton = (Button) findViewById(R.id.place_fleet_button);
    fleetSpinner = (Spinner) findViewById(R.id.fleet_spinner);
    String[] spinnerTextArray = getResources().getStringArray(R.array.fleet_array);
    int[] spinnerColorArray = fleetPlacementGrid.getStripColors();
    ArrayAdapter<CharSequence> fleetAdaptor = new StripArrayAdaptor(this, android.R.layout.simple_spinner_dropdown_item, spinnerTextArray, spinnerColorArray);
    fleetSpinner.setAdapter(fleetAdaptor);
    continueGameButton = (Button) findViewById(R.id.fleet_placement_continue_game_button);

    passDeviceTextView = (TextView) findViewById(R.id.deploy_fleet_text);
    readyToDeployButton = (Button) findViewById(R.id.deploy_fleet_button);
    countdown = (TextView) findViewById(R.id.countdownTextView);

    Intent intent = getIntent();
    resumedGame = intent.getBooleanExtra("ResumeTwoPlayerGame", false);
    firstTurnIdentifier = intent.getIntExtra("FirstTurnIdentifier", 1);
    playerOneName = intent.getStringExtra("PlayerOneName");
    playerTwoName = intent.getStringExtra("PlayerTwoName");
    playerOnePlaceFleet = intent.getBooleanExtra("PlayerOnePlaceFleet", false);
    playerTwoPlaceFleet = intent.getBooleanExtra("PlayerTwoPlaceFleet", false);

    loadIncompleteFleetPlacement();
    setIncompleteFleetPlacementUI();

    // Hook up events
    fleetPlacementGrid.addSelectionChangedListener(this);
    fleetPlacementGrid.addStripTouchedListener(this);
    fleetSpinner.setOnItemSelectedListener(this);

    if (playerOnePlaceFleet && playerTwoFleetPlacement == null) {
        playerTurnIdentifier = PLAYER_ONE_TURN_IDENTIFIER;
    } else {
        playerTurnIdentifier = PLAYER_TWO_TURN_IDENTIFIER;
    }
    setPassDeviceScreenUI();
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
  protected void onPause() {
    super.onPause();
    sensorManager.unregisterListener(this);
    saveIncompleteFleetPlacement();
  }

  @Override
  public void onResume() {
    super.onResume();
    sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    //Set options
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    String booleanString = sharedPreferences.getString("touchToSelectEnabled", "1");
    fleetPlacementGrid.setTouchToSelectEnabled(Integer.parseInt(booleanString) == 1);
  }

  private void loadIncompleteFleetPlacement() {
    try {
      FileInputStream fileInputStream = openFileInput("playeronefleetplacement");
      ObjectInputStream objectInputStream = new ObjectInputStream (fileInputStream);
      Object playerOneFleetPlacementReadObject = objectInputStream.readObject();
      objectInputStream.close();
      fileInputStream.close();
      if (playerOneFleetPlacementReadObject instanceof Fleet) {
          playerOneFleetPlacement = (Fleet) playerOneFleetPlacementReadObject;
      }
    } catch (Exception e) {
      playerOneFleetPlacement = null;
    }

    try {
      FileInputStream fileInputStream = openFileInput("playertwofleetplacement");
      ObjectInputStream objectInputStream = new ObjectInputStream (fileInputStream);
      Object playerTwoFleetPlacementReadObject = objectInputStream.readObject();
      objectInputStream.close();
      fileInputStream.close();
      if (playerTwoFleetPlacementReadObject instanceof Fleet) {
          playerTwoFleetPlacement = (Fleet) playerTwoFleetPlacementReadObject;
      }
    } catch (Exception e) {
      playerTwoFleetPlacement = null;
    }
  }

  private void saveIncompleteFleetPlacement() {
    if (playerTurnIdentifier == PLAYER_ONE_TURN_IDENTIFIER) {
      playerOneFleetPlacement = fleetPlacementGrid.getFleet();
    } else {
      playerTwoFleetPlacement = fleetPlacementGrid.getFleet();
    }

    try {
      FileOutputStream fileOutputStream = openFileOutput("playeronefleetplacement", Context.MODE_PRIVATE);
      ObjectOutputStream objectOutputStream = new ObjectOutputStream (fileOutputStream);
      objectOutputStream.writeObject(playerOneFleetPlacement);
      objectOutputStream.close();
      fileOutputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      FileOutputStream fileOutputStream = openFileOutput("playertwofleetplacement", Context.MODE_PRIVATE);
      ObjectOutputStream objectOutputStream = new ObjectOutputStream (fileOutputStream);
      objectOutputStream.writeObject(playerTwoFleetPlacement);
      objectOutputStream.close();
      fileOutputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void resetIncompleteFleetPlacementSave() {
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

  private void setIncompleteFleetPlacementUI() {
    if (playerTurnIdentifier == PLAYER_ONE_TURN_IDENTIFIER) {
      if (playerOneFleetPlacement != null) {
        fleetPlacementGrid.setFleet(playerOneFleetPlacement);
      }
    } else {
      if (playerTwoFleetPlacement != null) {
        fleetPlacementGrid.setFleet(playerTwoFleetPlacement);
      }
    }
    fleetPlacementGrid.invalidate();
  }

  private void setPassDeviceScreenUI() {
    fleetPlacementGrid.setVisibility(View.GONE);
    fleetSpinner.setVisibility(View.GONE);
    placeFleetButton.setVisibility(View.GONE);
    passDeviceTextView.setVisibility(View.VISIBLE);
    //readyToDeployButton.setVisibility(View.VISIBLE);
    countdown.setVisibility(View.VISIBLE);

    if (playerTurnIdentifier == PLAYER_ONE_TURN_IDENTIFIER) {
      setTitle(playerOneName + getString(R.string.ready_deploy_title_suffix));
      String readyText = playerTwoName + getString(R.string.pass_text) + "\n" + playerOneName + getString(R.string.ready_deploy_text);
      passDeviceTextView.setText(readyText);
    } else {
      setTitle(playerTwoName + getString(R.string.ready_deploy_title_suffix));
      String readyText = playerOneName + getString(R.string.pass_text) + "\n" + playerTwoName + getString(R.string.ready_deploy_text);
      passDeviceTextView.setText(readyText);
    }

    countdownTimer = new CountDownTimer(2999, 1000) {
      public void onTick(long millisUntilFinished) {
        countdown.setText(Integer.toString((int) (millisUntilFinished / 1000)));
      }
      public void onFinish() {
        countdown.setVisibility(View.GONE);
        readyToDeploy();
      }
    }.start();
  }

  // Handles selection from Spinner
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    fleetSpinner.setSelection(position);
    fleetPlacementGrid.selectStrip(position);
  }

  // Stub function required to implement Listener
  public void onNothingSelected(AdapterView<?> parent) { }

  @Override
  public void onSensorChanged(SensorEvent event) {
    Sensor sensor = event.sensor;
    if (sensor.getType() == Sensor.TYPE_ACCELEROMETER &&
      passDeviceTextView.getVisibility() == View.VISIBLE) {
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

  // Handles cell touch on OceanGrid
  public void onSelectionChanged(SelectionChangedEvent selectionChangedEvent) {
    // When a cell is touched, consider whether placeFleetButton should be enabled because cell selection has changed
    invalidatePlaceFleetButtonState(fleetPlacementGrid.validFleetPlacement());
  }

  // Handles strip touch on OceanGrid
  public void onStripTouched(StripTouchedEvent stripTouchedEvent, int stripID) {
    fleetSpinner.setSelection(stripID);
    fleetPlacementGrid.selectStrip(stripID);
  }

  // Reevaluate placeFleetButton state
  private void invalidatePlaceFleetButtonState(boolean validFleetPlacement) {
    placeFleetButton.setEnabled(validFleetPlacement);
  }

  public void onPlaceFleetButtonClicked(View view) {
    placeFleet();
  }

  // Places the fleet at the selected configuration
  private void placeFleet() {
    toast.setText(getString(R.string.fleet_placed));
    toast.show();

    if (playerTurnIdentifier == PLAYER_ONE_TURN_IDENTIFIER) {
      playerOneFleetPlacement = fleetPlacementGrid.getFleet();
    } else {
      playerTwoFleetPlacement = fleetPlacementGrid.getFleet();
    }

    fleetSpinner.setEnabled(false);
    fleetPlacementGrid.deactivateUI();

    placeFleetButton.setEnabled(false);
    continueGameButton.setEnabled(true);
    continueGameButton.setVisibility(View.VISIBLE);
  }

  public void onReadyToDeployFleetButtonClicked(View view) {
    readyToDeploy();
  }

  private void readyToDeploy() {
    setFleetPlacementUI();
  }

  private void setFleetPlacementUI() {
    passDeviceTextView.setVisibility(View.GONE);
    //readyToDeployButton.setVisibility(View.GONE);
    fleetPlacementGrid.setVisibility(View.VISIBLE);
    fleetPlacementGrid.activateUI();
    fleetSpinner.setVisibility(View.VISIBLE);
    fleetSpinner.setEnabled(true);
    placeFleetButton.setVisibility(View.VISIBLE);
    if (playerTurnIdentifier == PLAYER_ONE_TURN_IDENTIFIER) {
        setTitle(playerOneName + getString(R.string.place_fleet_title_suffix));
    } else {
        setTitle(playerTwoName + getString(R.string.place_fleet_title_suffix));
    }
    fleetSpinner.setSelection(0);
  }

  public void onContinueGameButtonClicked(View view) { continueGame(); }

  private void continueGame() {
    toast.cancel();

    if ((playerTurnIdentifier == PLAYER_ONE_TURN_IDENTIFIER && !playerTwoPlaceFleet) || playerTurnIdentifier == PLAYER_TWO_TURN_IDENTIFIER) {
      // Start Two Player Game activity
      Intent intent = new Intent(getApplicationContext(), TwoPlayerGameActivity.class);
      // Pass through previous intent extras
      intent.putExtra("ResumeTwoPlayerGame", resumedGame);
      intent.putExtra("FirstTurnIdentifier", firstTurnIdentifier);
      intent.putExtra("PlayerOneName", playerOneName);
      intent.putExtra("PlayerTwoName", playerTwoName);
      intent.putExtra("PlayerOnePlaceFleet", playerOnePlaceFleet);
      intent.putExtra("PlayerOneFleetPlacement", (Parcelable) playerOneFleetPlacement);
      intent.putExtra("PlayerTwoPlaceFleet", playerTwoPlaceFleet);
      intent.putExtra("PlayerTwoFleetPlacement", (Parcelable) playerTwoFleetPlacement);
      startActivity(intent);
    } else {
      continueGameButton.setEnabled(false);
      continueGameButton.setVisibility(View.INVISIBLE);
      playerTurnIdentifier = PLAYER_TWO_TURN_IDENTIFIER;
      setPassDeviceScreenUI();
      fleetPlacementGrid.resetFleet();
    }
  }
}
