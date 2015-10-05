package com.scottlogic.dmiley.battleship.activities.oneplayerversus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.scottlogic.dmiley.battleship.R;
import com.scottlogic.dmiley.battleship.activities.setting.SettingsActivity;
import com.scottlogic.dmiley.battleship.gridview.GameGridView;
import com.scottlogic.dmiley.battleship.gridview.event.SelectionChangedEvent;
import com.scottlogic.dmiley.battleship.gridview.event.SelectionChangedListener;
import com.scottlogic.dmiley.battleship.gridview.fleet.Fleet;
import com.scottlogic.dmiley.battleship.logic.ComputerAIOpponent;
import com.scottlogic.dmiley.battleship.logic.OceanModel;
import com.scottlogic.dmiley.battleship.logic.RadarModel;
import com.scottlogic.dmiley.battleship.logic.SearchedGridModel;
import com.scottlogic.dmiley.battleship.logic.event.ComputerSinksFleetEvent;
import com.scottlogic.dmiley.battleship.logic.event.ComputerSinksFleetListener;
import com.scottlogic.dmiley.battleship.logic.event.ComputerSinksShipEvent;
import com.scottlogic.dmiley.battleship.logic.event.ComputerSinksShipListener;
import com.scottlogic.dmiley.battleship.logic.event.FleetSankEvent;
import com.scottlogic.dmiley.battleship.logic.event.FleetSankListener;
import com.scottlogic.dmiley.battleship.logic.event.ShipHitEvent;
import com.scottlogic.dmiley.battleship.logic.event.ShipHitListener;
import com.scottlogic.dmiley.battleship.logic.event.ShipSankEvent;
import com.scottlogic.dmiley.battleship.logic.event.ShipSankListener;
import com.scottlogic.dmiley.battleship.util.GridLocation;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;

public class OnePlayerVersusGameActivity extends ActionBarActivity implements SelectionChangedListener, ComputerSinksFleetListener, ShipHitListener, ShipSankListener, FleetSankListener, ComputerSinksShipListener {

    private GameGridView gameGrid;
    private Button fireButton;
    private Button toggleViewButton;
    private Button continueGameButton;

    private String playerName;
    private boolean playerPlaceFleet;

    private boolean viewingRadar;
    private GridLocation toggleViewRadarSelectedCell;

    private boolean fleetSunkCalled;

    private int radarColorSchemeInteger;
    private int startingRadarColorSchemeInteger;

    private Toast toast;

    private RadarModel playerRadar;
    private ComputerAIOpponent computerAIOpponent;

    private final static int MAX_NAME_LENGTH = 30;

    private final static int BLUE_WHITE_COLOR_SCHEME = 0;
    private final static int GREEN_BLACK_COLOR_SCHEME = 1;

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), OnePlayerVersusEntryActivity.class);
        startActivity(intent);
    }

    // Initialise activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Delete complete fleet placement save
        resetFleetPlacementSave();

        // Initialise the layout
        setContentView(R.layout.activity_one_player_versus_game);
        toast = Toast.makeText(getApplicationContext(), null, Toast.LENGTH_SHORT);

        // Get views from Layout
        gameGrid = (GameGridView) findViewById(R.id.onePlayerVersusRadarGrid);
        fireButton = (Button) findViewById(R.id.one_player_versus_fire_button);
        toggleViewButton = (Button) findViewById(R.id.one_player_versus_toggle_view_button);
        continueGameButton = (Button) findViewById(R.id.one_player_versus_finish_game_button);

        // Initialise game logic and player name, load unfinished game if present
        Intent intent = getIntent();
        boolean resumedGame = intent.getBooleanExtra("ResumeOnePlayerVersusGame", false);

        setFleetSunkCalled(false);

        gameGrid.setFleetPlacementShown(false);
        setViewingRadar(true);

        if (resumedGame) {
            // Load Player name
            loadPlayer();

            // Load Game state
            loadGameState();

        } else {
            playerName = intent.getStringExtra("PlayerName");
            playerPlaceFleet = intent.getBooleanExtra("PlayerPlaceFleet", false);
            Fleet playerFleet = intent.getParcelableExtra("PlayerFleetPlacement");

            OceanModel playerFleetPlacement = new OceanModel();
            SearchedGridModel playerSearchedGrid = new SearchedGridModel();
            playerRadar = new RadarModel(playerFleetPlacement, playerSearchedGrid);

            OceanModel opponentFleetPlacement;
            if (playerFleet == null) {
                opponentFleetPlacement = new OceanModel();
            } else {
                opponentFleetPlacement = new OceanModel(playerFleet);
            }
            SearchedGridModel opponentSearchedGrid = new SearchedGridModel();
            RadarModel opponentRadar = new RadarModel(opponentFleetPlacement, opponentSearchedGrid);
            computerAIOpponent = new ComputerAIOpponent(opponentRadar);
        }

        // Hook up touch events
        gameGrid.addSelectionChangedListener(this);

        // Set loaded UI
        startingRadarColorSchemeInteger = BLUE_WHITE_COLOR_SCHEME;

        // Set display for player turn
        gameGrid.setShotDataDisplay(playerRadar.display());
        String playerTurnPrompt = playerName + getString(R.string.shot_title_suffix);
        setTitle(playerTurnPrompt);
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

        String computerAIDifficultyString = sharedPreferences.getString("versusGameDifficulty", "1");
        int computerAIDifficulty = Integer.parseInt(computerAIDifficultyString);
        computerAIOpponent.setDifficulty(computerAIDifficulty);

        String radarColorSchemeString = sharedPreferences.getString("onePlayerVersusRadarColorScheme", "1");
        radarColorSchemeInteger = Integer.parseInt(radarColorSchemeString);

        updateToggleButtonState();
        // Set color scheme to radar view
        if (radarColorSchemeInteger != startingRadarColorSchemeInteger) {
            gameGrid.swapColorScheme();
            startingRadarColorSchemeInteger = radarColorSchemeInteger;
        }

        // Hook up model events
        playerRadar.addShipHitListener(this);
        playerRadar.addShipSunkListener(this);
        playerRadar.addFleetSunkListener(this);

        computerAIOpponent.addRadarListeners();
        computerAIOpponent.addComputerSinksShipListener(this);
        computerAIOpponent.addComputerSinksFleetListener(this);

        gameGrid.invalidate();

        // Calls fleet sunk listener if needed.
        playerRadar.checkShipsLeft();
        computerAIOpponent.checkShipsLeft();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unhook model events
        playerRadar.removeShipHitListener(this);
        playerRadar.removeShipSunkListener(this);
        playerRadar.removeFleetSunkListener(this);

        computerAIOpponent.removeRadarListeners();
        computerAIOpponent.removeComputerSinksShipListener(this);
        computerAIOpponent.removeComputerSinksFleetListener(this);

        // Save player name
        savePlayer();

        // Save playerRadar & opponentRadar
        saveGameState();

        saveOnePlayerVersusGameMarker();
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

    public boolean isViewingRadar() {
        return viewingRadar;
    }

    public void setViewingRadar(boolean viewingRadar) {
        this.viewingRadar = viewingRadar;
    }

    private void loadPlayer() {
        try {
            FileInputStream fileInputStream = openFileInput("playerversusname");
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);

            char[] stringBuffer = new char[MAX_NAME_LENGTH];
            int readCharacters = inputStreamReader.read(stringBuffer);
            playerName = String.copyValueOf(stringBuffer, 0, readCharacters);

            inputStreamReader.close();
            fileInputStream.close();
        } catch (Exception e) {
            playerName = getString(R.string.player_one_default_name);
        }

        try {
            FileInputStream fileInputStream = openFileInput("playerversusplacefleetpreference");
            ObjectInputStream objectInputStream = new ObjectInputStream (fileInputStream);
            playerPlaceFleet = objectInputStream.readBoolean();
            objectInputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadGameState() {
        try {
            FileInputStream fileInputStream = openFileInput("playerversusradar");
            ObjectInputStream objectInputStream = new ObjectInputStream (fileInputStream);
            Object playerRadarReadObject = objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();

            if (playerRadarReadObject instanceof RadarModel) {
                playerRadar = (RadarModel) playerRadarReadObject;
            }

        } catch (Exception e) {
            OceanModel playerRandomFleetPlacement = new OceanModel();
            SearchedGridModel playerSearchedGrid = new SearchedGridModel();
            playerRadar = new RadarModel(playerRandomFleetPlacement, playerSearchedGrid);
        }

        try {
            FileInputStream fileInputStream = openFileInput("opponentversusradar");
            ObjectInputStream objectInputStream = new ObjectInputStream (fileInputStream);
            Object opponentRadarReadObject = objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();

            if (opponentRadarReadObject instanceof RadarModel) {
                RadarModel opponentRadar = (RadarModel) opponentRadarReadObject;
                if (!(opponentRadar == null)) {
                    computerAIOpponent = new ComputerAIOpponent(opponentRadar);
                }
            }

        } catch (Exception e) {
            OceanModel opponentRandomFleetPlacement = new OceanModel();
            SearchedGridModel opponentSearchedGrid = new SearchedGridModel();
            RadarModel opponentRadar = new RadarModel(opponentRandomFleetPlacement, opponentSearchedGrid);
            computerAIOpponent = new ComputerAIOpponent(opponentRadar);
        }
    }

    private void savePlayer() {
        try {
            FileOutputStream fileOutputStream = openFileOutput("playerversusname", Context.MODE_PRIVATE);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(playerName);
            outputStreamWriter.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            FileOutputStream fileOutputStream = openFileOutput("playerversusplacefleetpreference", Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream (fileOutputStream);
            objectOutputStream.writeBoolean(playerPlaceFleet);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveGameState() {
        try {
            FileOutputStream fileOutputStream = openFileOutput("playerversusradar", Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream (fileOutputStream);
            objectOutputStream.writeObject(playerRadar);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            RadarModel opponentRadar = computerAIOpponent.getComputerRadar();
            FileOutputStream fileOutputStream = openFileOutput("opponentversusradar", Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream (fileOutputStream);
            objectOutputStream.writeObject(opponentRadar);
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
            objectOutputStream.writeBoolean(true);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isFleetSunkCalled() {
        return fleetSunkCalled;
    }

    public void setFleetSunkCalled(boolean fleetSunkCalled) {
        this.fleetSunkCalled = fleetSunkCalled;
    }

    // Handles cell touch on RadarGrid
    public void onSelectionChanged(SelectionChangedEvent selectionChangedEvent) {
        boolean validCellSelected = playerRadar.validCellSelected(gameGrid.getSelectedCell());

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
        boolean hit = playerRadar.getCurrentGuessCellType().getIsShip();

        if(hit) {
            String hitString = getString(R.string.hit);
            toast.setText(hitString);
        } else {
            String missString = getString(R.string.miss);
            toast.setText(missString);
        }
        toast.show();

        updateUI();
    }

    // Handles shipSank event
    public void onShipSank(ShipSankEvent shipSankEvent) {
        shipSank();
    }

    // Called when ship sunk but fleet not sunk
    private void shipSank() {
        String cellTypeSunk = playerRadar.getCurrentGuessCellType().getName();

        String reportShipSunkTextMessage = getString(R.string.hit) + ". " + cellTypeSunk + " " + getString(R.string.sunk);

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
        String cellTypeSunk = playerRadar.getCurrentGuessCellType().getName();

        String reportFleetSunkTextMessage = getString(R.string.hit) + ". " + cellTypeSunk + " " + getString(R.string.sunk) + "\n";
        reportFleetSunkTextMessage += getString(R.string.fleet_sunk);

        // Displays text popup informing user of fleet sinking.
        toast.setText(reportFleetSunkTextMessage);
        toast.show();

        setFleetSunkCalled(true);

        updateUI();

        gameGrid.deactivateUI();

        setTitle(playerName + " " + getString(R.string.fleet_sunk_title_suffix));
        fireButton.setEnabled(false);
        toggleViewButton.setVisibility(View.INVISIBLE);
        continueGameButton.setVisibility(View.VISIBLE);
        continueGameButton.setEnabled(true);
    }

    @Override
    public void onComputerSinksShip(ComputerSinksShipEvent computerSinksShipEvent) {
        computerSinksShip();
    }

    private void computerSinksShip() {
        gameGrid.deactivateUI();
        fireButton.setVisibility(View.INVISIBLE);

        new CountDownTimer(1000, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                if (isViewingRadar()) {
                    toggleViewButton.performClick();
                }

                toast.setText(getString(R.string.computer_sunk_ship_prefix) + computerAIOpponent.getComputerRadar().getCurrentGuessCellType().getName());
                toast.show();
            }
        }.start();
    }

    @Override
    public void onComputerSinksFleet(ComputerSinksFleetEvent computerSinksFleetEvent) {
        computerSinksFleet();
    }

    private void computerSinksFleet() {
        if (!isFleetSunkCalled()) {
            gameGrid.deactivateUI();
            fireButton.setVisibility(View.INVISIBLE);

            new CountDownTimer(1000, 1000) {

                public void onTick(long millisUntilFinished) {

                }

                public void onFinish() {
                    if (isViewingRadar()) {
                        toggleViewButton.performClick();
                    }

                    setTitle(getString(R.string.computer_fleet_sunk_title_suffix));

                    toast.setText(getString(R.string.computer_sunk_fleet));
                    toast.show();

                    toggleViewButton.setVisibility(View.INVISIBLE);
                    continueGameButton.setVisibility(View.VISIBLE);
                    continueGameButton.setEnabled(true);
                }
            }.start();
        }
    }

    // Reevaluate fireButton state
    private void invalidateFireButtonState(boolean validCellSelected) {
        if (validCellSelected) {
            fireButton.setEnabled(true);
        } else {
            fireButton.setEnabled(false);
        }
    }

    // Updates the fire button and grid state
    private void updateUI() {
        boolean validCellSelected = playerRadar.validCellSelected(gameGrid.getSelectedCell());

        invalidateFireButtonState(validCellSelected);
        gameGrid.updateCursorDisplay(validCellSelected);

        gameGrid.setShotDataDisplay(playerRadar.display());
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

    // Continue game click handler
    public void onFinishGameButtonClicked(View view) {
        finishGame();
    }

    private void finishGame() {
        // Stop showing current toast popup
        toast.cancel();

        // Start OnePlayerVersusPostGame activity
        Intent intent = new Intent(getApplicationContext(), OnePlayerVersusPostGameActivity.class);

        // Get player name and whose turn it was first & send to next activity
        intent.putExtra("PlayerName", playerName);
        intent.putExtra("PlayerPlaceFleet", playerPlaceFleet);

        if (isFleetSunkCalled()) {
            intent.putExtra("PlayerWon", true);
        } else {
            intent.putExtra("PlayerWon", false);
        }

        startActivity(intent);
    }

    public void onFireButtonClicked(View view) {
        fire();
    }

    // Fires a shot at the selected cell
    private void fire() {
        //Simulate the shot, Update the UI
        playerRadar.takeShot(gameGrid.getSelectedCell());
        computerAIOpponent.takeShot();
    }

    public void onToggleViewButtonClicked(View view) {
        toggleView();
    }

    // Toggles View between your ocean and radar
    private void toggleView() {
        if (isViewingRadar()) {
            toggleViewRadarSelectedCell = gameGrid.getSelectedCell();
            setViewingRadar(false);
            updateToggleButtonState();
            if (radarColorSchemeInteger == GREEN_BLACK_COLOR_SCHEME) {
                gameGrid.swapColorScheme();
            }
            // Set display to ocean
            gameGrid.setShotDataDisplay(computerAIOpponent.getGameDisplay());
            //gameGrid.invalidate();

            // Set display for player turn
            String playerTurnPrompt = playerName + getString(R.string.survey_fleet_title_suffix);
            setTitle(playerTurnPrompt);

            setOceanButtonState();
        } else {
            setViewingRadar(true);
            updateToggleButtonState();
            // Set display to radar
            if (radarColorSchemeInteger == GREEN_BLACK_COLOR_SCHEME) {
                gameGrid.swapColorScheme();
            }

            gameGrid.setShotDataDisplay(playerRadar.display());
            gameGrid.setSelectedCell(toggleViewRadarSelectedCell);
            //gameGrid.invalidate();

            // Set display for player turn
            String playerTurnPrompt = playerName + getString(R.string.shot_title_suffix);
            setTitle(playerTurnPrompt);

            setRadarButtonState();
        }
    }
}