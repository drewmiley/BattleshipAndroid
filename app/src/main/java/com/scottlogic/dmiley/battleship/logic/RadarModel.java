package com.scottlogic.dmiley.battleship.logic;

import android.os.Parcel;
import android.os.Parcelable;

import com.scottlogic.dmiley.battleship.logic.event.FleetSankEvent;
import com.scottlogic.dmiley.battleship.logic.event.FleetSankListener;
import com.scottlogic.dmiley.battleship.logic.event.ShipHitEvent;
import com.scottlogic.dmiley.battleship.logic.event.ShipHitListener;
import com.scottlogic.dmiley.battleship.logic.event.ShipSankEvent;
import com.scottlogic.dmiley.battleship.logic.event.ShipSankListener;
import com.scottlogic.dmiley.battleship.logic.oceantools.CellType;
import com.scottlogic.dmiley.battleship.util.GridLocation;
import com.scottlogic.dmiley.battleship.util.ShotData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RadarModel implements Parcelable, Serializable {

	private CellType currentGuessCellType;

	private OceanModel oceanModel;
	
	private SearchedGridModel searchedGrid;
	
	private int shipTotalLeftToFind;

    private List<ShipHitListener> shipHitListeners;
    private List<ShipSankListener> shipSankListeners;
    private List<FleetSankListener> fleetSankListeners;

    private final static int GRID_SIZE = 10;

	public RadarModel(OceanModel oceanModel, SearchedGridModel searchedGrid) {
        shipHitListeners = new ArrayList<>();
        shipSankListeners = new ArrayList<>();
        fleetSankListeners = new ArrayList<>();

		this.oceanModel = oceanModel;
		this.shipTotalLeftToFind = oceanModel.getShipTotal();
		this.searchedGrid = searchedGrid;
	}

    public RadarModel(Parcel in) {
        shipHitListeners = new ArrayList<>();
        shipSankListeners = new ArrayList<>();
        fleetSankListeners = new ArrayList<>();

        currentGuessCellType = in.readParcelable(CellType.class.getClassLoader());
        oceanModel = in.readParcelable(OceanModel.class.getClassLoader());
        searchedGrid = in.readParcelable(SearchedGridModel.class.getClassLoader());
        shipTotalLeftToFind = in.readInt();
    }

    // Generates 2D shot array for use in loading persisted state, passed to view by activity
    public ShotData[][] display() {
        ShotData[][] display = new ShotData[GRID_SIZE + 1][GRID_SIZE + 1];
        for (int row = 1; row <= GRID_SIZE; row ++) {
            for (int column = 1; column <= GRID_SIZE; column++) {
                GridLocation gridLocation = new GridLocation(row, column);

                boolean searched;
                CellType cellType = oceanModel.getPoint(gridLocation);
                boolean sunk;

                if (alreadySearched(gridLocation)) {
                    searched = true;
                } else {
                    searched = false;
                }
                if (cellType.getIsShip()) {
                    if (cellType.remainingHealth() == 0) {
                        sunk = true;
                    } else {
                        sunk = false;
                    }
                } else {
                    sunk = false;
                }
                display[row][column] = new ShotData(searched, cellType, sunk);
            }
        }
        return display;
    }

	public CellType getCurrentGuessCellType() {
		return currentGuessCellType;
	}

	private void setCurrentGuessCellType(CellType currentGuessCellType) {
		this.currentGuessCellType = currentGuessCellType;
	}

    // Checks if gridlocation already searched
	private boolean alreadySearched(GridLocation gridLocation) {
		boolean searched = searchedGrid.getPoint(gridLocation);
		return searched;
	}

	public void takeShot(GridLocation gridLocation) {
        updateModel(gridLocation);
	}

    // Updates the model with details of gridlocation that has been fired on
    private void updateModel(GridLocation gridLocation) {
        searchedGrid.setPointToTrue(gridLocation);
        CellType cellType = oceanModel.getPoint(gridLocation);
        setCurrentGuessCellType(cellType);
        if (cellType.getIsShip()) {
            cellType.hit();
            if (shipSunkThisTurn()) {
                shipTotalLeftToFind--;
                if (!shipLeftToFind()) {
                    fleetSunk();
                } else {
                    shipSunk();
                }
            } else {
                shipHit();
            }
        } else {
            shipHit();
        }
    }

    public void checkShipsLeft() {
        if(!shipLeftToFind()) {
            fleetSunk();
        }
    }


    private boolean shipLeftToFind() {
        return shipTotalLeftToFind != 0;
	}

	// Checks if a CellType has been sunk this turn
	private boolean shipSunkThisTurn() {
		CellType cellType = getCurrentGuessCellType();
		if(cellType.getIsShip()) {
            if(cellType.remainingHealth() == 0) {
				return true;
			}
		}
		return false;
	}

    // Finds if cell selected is valid to fire on
    public boolean validCellSelected(GridLocation selectedCell) {
        if (selectedCell == null) {
            return false;
        }
        if (!selectedCell.locatedInGrid()) {
            return false;
        }
        if (alreadySearched(selectedCell)) {
            return false;
        }
        return true;
    }

    // Returns ship locations, passed to activity and used for color transition in view when ship sunk
    public List<GridLocation> getShipLocations(CellType cellTypeHit) {
        List<GridLocation> shipLocations = new ArrayList<>();
        for (int row = 1; row <= GRID_SIZE; row++) {
            for (int column = 1 ; column <= GRID_SIZE; column++) {
                GridLocation gridLocation = new GridLocation(row, column);
                if(cellTypeHit.equals(oceanModel.getPoint(gridLocation))) {
                    shipLocations.add(gridLocation);
                }
            }
        }

        return shipLocations;
    }

    public List<GridLocation> getAllShipLocations() {
        List<GridLocation> shipLocations = new ArrayList<>();
        for (int row = 1; row <= GRID_SIZE; row++) {
            for (int column = 1 ; column <= GRID_SIZE; column++) {
                GridLocation gridLocation = new GridLocation(row, column);
                if(oceanModel.getPoint(gridLocation).getIsShip()) {
                    shipLocations.add(gridLocation);
                }
            }
        }

        return shipLocations;
    }

    public void shipHit() {
        onShipHit();
    }

    // Add listener object to shipHit listeners
    public void addShipHitListener(ShipHitListener shipHitListener) {
        shipHitListeners.add(shipHitListener);
    }

    // Remove listener object from shipHit listeners
    public void removeShipHitListener(ShipHitListener shipHitListener) {
        shipHitListeners.remove(shipHitListener);
    }

    // On shipHit event
    private void onShipHit() {
        ShipHitEvent shipHitEvent = new ShipHitEvent(this);
        for (ShipHitListener shipHitListener : shipHitListeners) {
            (shipHitListener).onShipHit(shipHitEvent);
        }
    }

    public void shipSunk() {
        onShipSunk();
    }

    // Add listener object to shipSunk listeners
    public void addShipSunkListener(ShipSankListener shipSankListener) {
        shipSankListeners.add(shipSankListener);
    }

    // Remove listener object from shipSunk listeners
    public void removeShipSunkListener(ShipSankListener shipSankListener) {
        shipSankListeners.remove(shipSankListener);
    }

    // On shipSunk event
    private void onShipSunk() {
        ShipSankEvent shipSankEvent = new ShipSankEvent(this);
        for (ShipSankListener shipSankListener : shipSankListeners) {
            (shipSankListener).onShipSank(shipSankEvent);
        }
    }

    public void fleetSunk() {
        onFleetSunk();
    }

    // Add listener object to fleetSunk listeners
    public void addFleetSunkListener(FleetSankListener fleetSankListener) {
        fleetSankListeners.add(fleetSankListener);
    }

    // Remove listener object from fleetSunk listeners
    public void removeFleetSunkListener(FleetSankListener fleetSankListener) {
        fleetSankListeners.remove(fleetSankListener);
    }

    // On fleetSunk event
    private void onFleetSunk() {
        FleetSankEvent fleetSankEvent = new FleetSankEvent(this);
        for (FleetSankListener fleetSankListener : fleetSankListeners) {
            (fleetSankListener).onFleetSank(fleetSankEvent);
        }
    }

    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(currentGuessCellType, flags);
        dest.writeParcelable(oceanModel, flags + 1);
        dest.writeParcelable(searchedGrid, flags + 2);
        dest.writeInt(shipTotalLeftToFind);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        public RadarModel createFromParcel(Parcel in) {
            return new RadarModel(in);
        }

        public RadarModel[] newArray(int size) {
            return new RadarModel[size];
        }
    };
}
