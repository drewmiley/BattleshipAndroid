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

    private final static int CADET = 0;
    private final static int LIEUTENANT = 1;
    private final static int CAPTAIN = 2;
    private final static int COMMODORE = 3;
    private final static int ADMIRAL = 4;

    private ComputerAIOpponent computerAIOpponent;
    private boolean gameCompleted;
    private int currentTestDifficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aitest);

        cadetTextView = (TextView) findViewById(R.id.test_cadet_textview);
        lieutenantTextView = (TextView) findViewById(R.id.test_lieutenant_textview);
        captainTextView = (TextView) findViewById(R.id.test_captain_textview);
        commodoreTextView = (TextView) findViewById(R.id.test_commodore_textview);
        admiralTextView = (TextView) findViewById(R.id.test_admiral_textview);

        OceanModel fleetPlacement = new OceanModel();
        SearchedGridModel searchedGrid = new SearchedGridModel();
        RadarModel radar = new RadarModel(fleetPlacement, searchedGrid);
        computerAIOpponent = new ComputerAIOpponent(radar);
    }

    @Override
    public void onComputerSinksFleet(ComputerSinksFleetEvent computerSinksFleetEvent) {
        gameCompleted = true;
    }

    public void onCadetTestButtonClicked(View view) {
        currentTestDifficulty = CADET;
        cadetTextView.setText(String.valueOf(calculateTestValue()));
    }

    public void onLieutenantTestButtonClicked(View view) {
        currentTestDifficulty = LIEUTENANT;
        lieutenantTextView.setText(String.valueOf(calculateTestValue()));
    }

    public void onCaptainTestButtonClicked(View view) {
        currentTestDifficulty = CAPTAIN;
        captainTextView.setText(String.valueOf(calculateTestValue()));
    }

    public void onCommodoreTestButtonClicked(View view) {
        currentTestDifficulty = COMMODORE;
        commodoreTextView.setText(String.valueOf(calculateTestValue()));
    }

    public void onAdmiralTestButtonClicked(View view) {
        currentTestDifficulty = ADMIRAL;
        admiralTextView.setText(String.valueOf(calculateTestValue()));
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
