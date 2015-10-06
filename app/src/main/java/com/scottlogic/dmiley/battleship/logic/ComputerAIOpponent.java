package com.scottlogic.dmiley.battleship.logic;

import com.scottlogic.dmiley.battleship.logic.event.ComputerSinksFleetListener;
import com.scottlogic.dmiley.battleship.logic.event.ComputerSinksShipEvent;
import com.scottlogic.dmiley.battleship.logic.event.ComputerSinksShipListener;
import com.scottlogic.dmiley.battleship.logic.event.FleetSankEvent;
import com.scottlogic.dmiley.battleship.logic.event.FleetSankListener;
import com.scottlogic.dmiley.battleship.logic.event.ComputerSinksFleetEvent;
import com.scottlogic.dmiley.battleship.logic.event.ShipHitEvent;
import com.scottlogic.dmiley.battleship.logic.event.ShipHitListener;
import com.scottlogic.dmiley.battleship.logic.event.ShipSankEvent;
import com.scottlogic.dmiley.battleship.logic.event.ShipSankListener;
import com.scottlogic.dmiley.battleship.util.GridLocation;
import com.scottlogic.dmiley.battleship.util.ShotData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Models a computer AI opponent playing a game of battleship
public class ComputerAIOpponent implements Serializable, FleetSankListener, ShipHitListener, ShipSankListener {

  private RadarModel computerRadar;
  private List<GridLocation> shipLocations;
  private int difficulty;

  private List<ComputerSinksFleetListener> computerSinksFleetListeners;
  private List<ComputerSinksShipListener> computerSinksShipListeners;

  private final static int CADET = 0;
  private final static int LIEUTENANT = 1;
  private final static int CAPTAIN = 2;
  private final static int COMMODORE = 3;
  private final static int ADMIRAL = 4;

  public ComputerAIOpponent(RadarModel computerRadar) {
    computerSinksFleetListeners = new ArrayList<>();
    computerSinksShipListeners = new ArrayList<>();

    this.computerRadar = computerRadar;
    shipLocations = new ArrayList<>();
    populateShipLocations();

    addRadarListeners();

    setDifficulty(LIEUTENANT);
  }

  public void addRadarListeners() {
    computerRadar.addShipHitListener(this);
    computerRadar.addShipSunkListener(this);
    computerRadar.addFleetSunkListener(this);
  }

  public void removeRadarListeners() {
    computerRadar.removeShipHitListener(this);
    computerRadar.removeShipSunkListener(this);
    computerRadar.removeFleetSunkListener(this);
  }

  public RadarModel getComputerRadar() {
    return computerRadar;
  }

  public int getDifficulty() {
    return difficulty;
  }

  public void setDifficulty(int difficulty) {
    this.difficulty = difficulty;
  }

  private void populateShipLocations() {
      for (int i = 0; i < 100; i++) {
        int row = 1 + i / 10;
        int column = 1 + i % 10;
        GridLocation gridLocation = new GridLocation(row, column);
        shipLocations = computerRadar.getAllShipLocations();
      }
  }

  public ShotData[][] getGameDisplay() {
    return computerRadar.display();
  }

  public void checkShipsLeft() {
    computerRadar.checkShipsLeft();
  }

  // Add listener object to opponentSinksShip listeners
  public void addComputerSinksShipListener(ComputerSinksShipListener computerSinksShipListener) {
    computerSinksShipListeners.add(computerSinksShipListener);
  }

  // Remove listener object to opponentSinksShip listeners
  public void removeComputerSinksShipListener(ComputerSinksShipListener computerSinksShipListener) {
    computerSinksShipListeners.remove(computerSinksShipListener);
  }

  // On shipSunk event
  private void onComputerSinksShip() {
    ComputerSinksShipEvent computerSinksShipEvent = new ComputerSinksShipEvent(this);
    for (ComputerSinksShipListener computerSinksShipListener : computerSinksShipListeners) {
      (computerSinksShipListener).onComputerSinksShip(computerSinksShipEvent);
    }
  }

  // Add listener object to opponentSinksFleet listeners
  public void addComputerSinksFleetListener(ComputerSinksFleetListener computerSinksFleetListener) {
    computerSinksFleetListeners.add(computerSinksFleetListener);
  }

  // Remove listener object to opponentSinksFleet listeners
  public void removeComputerSinksFleetListener(ComputerSinksFleetListener computerSinksFleetListener) {
    computerSinksFleetListeners.remove(computerSinksFleetListener);
  }

  // On fleetSunk event
  private void onComputerSinksFleet() {
    ComputerSinksFleetEvent computerSinksFleetEvent = new ComputerSinksFleetEvent(this);
    for (ComputerSinksFleetListener computerSinksFleetListener : computerSinksFleetListeners) {
      (computerSinksFleetListener).onComputerSinksFleet(computerSinksFleetEvent);
    }
  }

  @Override
  public void onShipHit(ShipHitEvent shipHitEvent) {
    if (getComputerRadar().getCurrentGuessCellType().getIsShip()) {
      // Does nothing currently
    }
  }

  @Override
  public void onShipSank(ShipSankEvent shipSankEvent) {
    onComputerSinksShip();
  }

  @Override
  public void onFleetSank(FleetSankEvent fleetSankEvent) {
    onComputerSinksFleet();
  }

  public void takeShot() {
    int randomInteger = (int) Math.ceil(Math.random() * 100);
    switch(difficulty) {
      case CADET:
        if (randomInteger >= 10) {
          takeRandomShot();
        } else {
          takeHitShot();
        }
        break;
      case LIEUTENANT:
        if (randomInteger >= 15) {
          takeRandomShot();
        } else {
          takeHitShot();
        }
        break;
      case CAPTAIN:
        if (randomInteger >= 20) {
          takeRandomShot();
        } else {
          takeHitShot();
        }
        break;
      case COMMODORE:
        if (randomInteger >= 25) {
          takeRandomShot();
        } else {
          takeHitShot();
        }
        break;
      case ADMIRAL:
        if (randomInteger >= 30) {
          takeRandomShot();
        } else {
          takeHitShot();
        }
        break;
      default:
        break;
    }
  }

  private void takeHitShot() {
    int shipLocationsLeftToHit = shipLocations.size();
    int shipLocationToHit = (int) Math.floor(Math.random() * shipLocationsLeftToHit);
    GridLocation gridLocation = shipLocations.get(shipLocationToHit);

    boolean validCellSelected = computerRadar.validCellSelected(gridLocation);

    while (!validCellSelected) {
      shipLocations.remove(gridLocation);
      shipLocationToHit = (int) Math.floor(Math.random() * shipLocationsLeftToHit);
      gridLocation = shipLocations.get(shipLocationToHit);
      validCellSelected = computerRadar.validCellSelected(gridLocation);
    }

    computerRadar.takeShot(gridLocation);
    shipLocations.remove(gridLocation);
  }

  private void takeRandomShot() {
    int row = (int) Math.ceil(Math.random() * 10);
    int column = (int) Math.ceil(Math.random() * 10);
    GridLocation gridLocation = new GridLocation(row,column);
    boolean validCellSelected = computerRadar.validCellSelected(gridLocation);

    while (!validCellSelected) {
      row = (int) Math.ceil(Math.random() * 10);
      column = (int) Math.ceil(Math.random() * 10);
      gridLocation = new GridLocation(row,column);
      validCellSelected = computerRadar.validCellSelected(gridLocation);
    }

    computerRadar.takeShot(gridLocation);
  }
}
