package com.scottlogic.dmiley.battleship.activities.oneplayerversus;

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
import android.widget.Toast;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class OnePlayerVersusFleetPlacementActivity extends ActionBarActivity implements SelectionChangedListener, StripTouchedListener, AdapterView.OnItemSelectedListener {

  private boolean resumedGame;
  private String playerName;
  private boolean playerPlaceFleet;
  private Fleet playerFleetPlacement;

  private FleetPlacementGridView fleetPlacementGrid;
  private Button placeFleetButton;
  private Spinner fleetSpinner;
  private Button continueGameButton;
  private Toast toast;

  // Initialise activity
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Initialise the layout
    setContentView(R.layout.activity_one_player_versus_fleet_placement);
    toast = Toast.makeText(getApplicationContext(), null, Toast.LENGTH_SHORT);

    // Get views from Layout
    fleetPlacementGrid = (FleetPlacementGridView) findViewById(R.id.versusFleetPlacementGrid);
    placeFleetButton = (Button) findViewById(R.id.versus_place_fleet_button);
    fleetSpinner = (Spinner) findViewById(R.id.fleet_spinner);
    String[] spinnerTextArray = getResources().getStringArray(R.array.fleet_array);
    int[] spinnerColorArray = fleetPlacementGrid.getStripColors();
    ArrayAdapter<CharSequence> fleetAdaptor = new StripArrayAdaptor(this, android.R.layout.simple_spinner_dropdown_item, spinnerTextArray, spinnerColorArray);
    fleetSpinner.setAdapter(fleetAdaptor);
    continueGameButton = (Button) findViewById(R.id.versus_fleet_placement_continue_game_button);

    Intent intent = getIntent();
    resumedGame = intent.getBooleanExtra("ResumeOnePlayerVersusGame", false);
    playerName = intent.getStringExtra("PlayerName");
    playerPlaceFleet = intent.getBooleanExtra("PlayerPlaceFleet", false);

    setTitle(playerName + getString(R.string.place_fleet_title_suffix));

    loadIncompleteFleetPlacement();

    setIncompleteFleetPlacementUI();

    // Hook up events
    fleetPlacementGrid.addSelectionChangedListener(this);
    fleetPlacementGrid.addStripTouchedListener(this);
    fleetSpinner.setOnItemSelectedListener(this);
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
    saveIncompleteFleetPlacement();
  }

  @Override
  public void onResume() {
    super.onResume();
    //Set options
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    String booleanString = sharedPreferences.getString("touchToSelectEnabled", "1");
    fleetPlacementGrid.setTouchToSelectEnabled(Integer.parseInt(booleanString) == 1);
  }

  private void loadIncompleteFleetPlacement() {
    try {
      FileInputStream fileInputStream = openFileInput("playerversusfleetplacement");
      ObjectInputStream objectInputStream = new ObjectInputStream (fileInputStream);
      Object playerOneFleetPlacementReadObject = objectInputStream.readObject();
      objectInputStream.close();
      fileInputStream.close();
      if (playerOneFleetPlacementReadObject instanceof Fleet) {
        playerFleetPlacement = (Fleet) playerOneFleetPlacementReadObject;
      }
    } catch (Exception e) {
      playerFleetPlacement = null;
    }
  }

  private void saveIncompleteFleetPlacement() {
    playerFleetPlacement = fleetPlacementGrid.getFleet();
    try {
      FileOutputStream fileOutputStream = openFileOutput("playerversusfleetplacement", Context.MODE_PRIVATE);
      ObjectOutputStream objectOutputStream = new ObjectOutputStream (fileOutputStream);
      objectOutputStream.writeObject(playerFleetPlacement);
      objectOutputStream.close();
      fileOutputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void resetIncompleteFleetPlacementSave() {
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

  private void setIncompleteFleetPlacementUI() {
    if (playerFleetPlacement != null) {
      fleetPlacementGrid.setFleet(playerFleetPlacement);
    }
    fleetPlacementGrid.invalidate();
  }

  // Handles selection from Spinner
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    fleetSpinner.setSelection(position);
    fleetPlacementGrid.selectStrip(position);
  }

  // Stub function required to implement Listener
  public void onNothingSelected(AdapterView<?> parent) { }

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

    playerFleetPlacement = fleetPlacementGrid.getFleet();

    fleetSpinner.setEnabled(false);
    fleetPlacementGrid.deactivateUI();

    placeFleetButton.setEnabled(false);
    continueGameButton.setEnabled(true);
    continueGameButton.setVisibility(View.VISIBLE);
  }

  public void onContinueGameButtonClicked(View view) { continueGame(); }

  private void continueGame() {
      toast.cancel();
      // Start One Player Versus Game activity
      Intent intent = new Intent(getApplicationContext(), OnePlayerVersusGameActivity.class);
      // Pass through previous intent extras
      intent.putExtra("ResumeOnePlayerVersusGame", resumedGame);
      intent.putExtra("PlayerName", playerName);
      intent.putExtra("PlayerPlaceFleet", playerPlaceFleet);
      intent.putExtra("PlayerFleetPlacement", (Parcelable) playerFleetPlacement);
      startActivity(intent);
  }
}
