package com.scottlogic.dmiley.battleship.activities;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.scottlogic.dmiley.battleship.R;
import com.scottlogic.dmiley.battleship.logic.ComputerAIOpponent;
import com.scottlogic.dmiley.battleship.logic.OceanModel;
import com.scottlogic.dmiley.battleship.logic.RadarModel;
import com.scottlogic.dmiley.battleship.logic.SearchedGridModel;
import com.scottlogic.dmiley.battleship.logic.event.ComputerSinksFleetEvent;
import com.scottlogic.dmiley.battleship.logic.event.ComputerSinksFleetListener;

public class AITestActivity extends ActionBarActivity implements ComputerSinksFleetListener {

    private TextView cadetTextView;
    private TextView lieutenantTextView;
    private TextView captainTextView;
    private TextView commodoreTextView;
    private TextView admiralTextView;

    private ComputerAIOpponent computerAIOpponent;

    private boolean gameCompleted;

    private int currentTestDifficulty;

    private final static int CADET = 0;
    private final static int LIEUTENANT = 1;
    private final static int CAPTAIN = 2;
    private final static int COMMODORE = 3;
    private final static int ADMIRAL = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aitest);

        OceanModel fleetPlacement = new OceanModel();
        SearchedGridModel searchedGrid = new SearchedGridModel();
        RadarModel radar = new RadarModel(fleetPlacement, searchedGrid);
        computerAIOpponent = new ComputerAIOpponent(radar);

        cadetTextView = (TextView) findViewById(R.id.test_cadet_textview);
        lieutenantTextView = (TextView) findViewById(R.id.test_lieutenant_textview);
        captainTextView = (TextView) findViewById(R.id.test_captain_textview);
        commodoreTextView = (TextView) findViewById(R.id.test_commodore_textview);
        admiralTextView = (TextView) findViewById(R.id.test_admiral_textview);
    }

    @Override
    public void onComputerSinksFleet(ComputerSinksFleetEvent computerSinksFleetEvent) {
        gameCompleted = true;
    }

    public void onCadetTestButtonClicked(View view) {
        currentTestDifficulty = CADET;

        int turnCount = calculateTestValue();

        cadetTextView.setText(String.valueOf(turnCount));
    }

    public void onLieutenantTestButtonClicked(View view) {
        currentTestDifficulty = LIEUTENANT;

        int turnCount = calculateTestValue();

        lieutenantTextView.setText(String.valueOf(turnCount));
    }

    public void onCaptainTestButtonClicked(View view) {
        currentTestDifficulty = CAPTAIN;

        int turnCount = calculateTestValue();

        captainTextView.setText(String.valueOf(turnCount));
    }

    public void onCommodoreTestButtonClicked(View view) {
        currentTestDifficulty = COMMODORE;

        int turnCount = calculateTestValue();

        commodoreTextView.setText(String.valueOf(turnCount));
    }

    public void onAdmiralTestButtonClicked(View view) {
        currentTestDifficulty = ADMIRAL;

        int turnCount = calculateTestValue();

        admiralTextView.setText(String.valueOf(turnCount));
    }

    private int calculateTestValue() {
        int turnCount = 0;

        for (int i = 1; i <= 100; i++) {
            int turn = completeGame();
            turnCount += turn;
        }
        return turnCount;
    }

    private int completeGame() {
        computerAIOpponent.removeComputerSinksFleetListener(this);

        OceanModel fleetPlacement = new OceanModel();
        SearchedGridModel searchedGrid = new SearchedGridModel();
        RadarModel radar = new RadarModel(fleetPlacement, searchedGrid);
        computerAIOpponent = new ComputerAIOpponent(radar);
        computerAIOpponent.setDifficulty(currentTestDifficulty);

        computerAIOpponent.addComputerSinksFleetListener(this);

        gameCompleted = false;

        for (int i = 1; i <= 100; i++) {
            computerAIOpponent.takeShot();
            if (gameCompleted) {
                return i;
            }
        }
        return 100;
    }
}
