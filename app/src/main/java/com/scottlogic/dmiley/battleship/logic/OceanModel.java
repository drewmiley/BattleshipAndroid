package com.scottlogic.dmiley.battleship.logic;

import com.scottlogic.dmiley.battleship.gridview.fleet.Fleet;
import com.scottlogic.dmiley.battleship.gridview.fleet.Strip;
import com.scottlogic.dmiley.battleship.logic.oceantools.CellType;
import com.scottlogic.dmiley.battleship.logic.oceantools.Orientation;
import com.scottlogic.dmiley.battleship.util.GridLocation;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Models the fleet placement onto the ocean
public class OceanModel implements Parcelable, Serializable {

	private CellType[][] cellTypeGrid;
	private int shipTotal;
	private CellType sea;
	private List <CellType> ships;

	private static final int ORIENTATIONS_POSSIBLE = 4;
	private static final int GRID_SIZE = 10;

	public OceanModel() {
	  sea = new CellType(0, false, "sea", 1);

	  CellType destroyer = new CellType(1, true, "Destroyer", 2);
	  CellType cruiser = new CellType(2, true, "Cruiser", 3);
	  CellType submarine = new CellType(3, true, "Submarine", 3);
	  CellType battleship = new CellType(4, true, "Battleship", 4);
	  CellType aircraftCarrier = new CellType(5, true, "Aircraft Carrier", 5);

	  ships = new ArrayList<>();
	  ships.add(destroyer);
	  ships.add(cruiser);
	  ships.add(submarine);
	  ships.add(battleship);
	  ships.add(aircraftCarrier);

		cellTypeGrid = new CellType[GRID_SIZE + 1][GRID_SIZE + 1];
		shipTotal = 0;

		newAutomaticSetup();
  }

	public OceanModel(Fleet fleet) {
		sea = new CellType(0, false, "sea", 1);

		CellType destroyer = new CellType(1, true, "Destroyer", 2);
		CellType cruiser = new CellType(2, true, "Cruiser", 3);
		CellType submarine = new CellType(3, true, "Submarine", 3);
		CellType battleship = new CellType(4, true, "Battleship", 4);
		CellType aircraftCarrier = new CellType(5, true, "Aircraft Carrier", 5);

		ships = new ArrayList<>();
		ships.add(destroyer);
		ships.add(cruiser);
		ships.add(submarine);
		ships.add(battleship);
		ships.add(aircraftCarrier);

		cellTypeGrid = new CellType[GRID_SIZE + 1][GRID_SIZE + 1];
		shipTotal = 0;

		newManualSetup(fleet);
	}

	public OceanModel(Parcel in) {
    int row;
    int column;

    cellTypeGrid = new CellType[GRID_SIZE + 1][GRID_SIZE + 1];
    shipTotal = in.readInt();

    for (int i = 0; i < (GRID_SIZE + 1)*(GRID_SIZE + 1); i++) {
      row = i / (GRID_SIZE + 1);
      column = i % (GRID_SIZE + 1);
      if (row > 0 && column > 0) {
        cellTypeGrid[row][column] = in.readParcelable(CellType.class.getClassLoader());
      }
    }
  }

	public CellType getPoint(GridLocation coordinate) {
		return cellTypeGrid[coordinate.getRow()][coordinate.getColumn()];
	}

	private void setPoint(GridLocation coordinate, CellType cellType) {
		this.cellTypeGrid[coordinate.getRow()][coordinate.getColumn()] = cellType;
	}

	public int getShipTotal() {
		return shipTotal;
	}

	private void increaseShipTotalByOne() {
		this.shipTotal++;
	}

	// Sets a random fleet placement
	private void newAutomaticSetup() {
		for (CellType ship : ships) {
			addAutomaticCellType(ship);
		}
		addOpenSea();
	}

    // Places fleet in accordance with selected placement
	private void newManualSetup(Fleet fleet) {
		List<Strip> strips = fleet.getStrips();
		for (int i = 0; i < strips.size(); i++) {
			Strip strip = strips.get(i);
			CellType ship = ships.get(i);
			addManualCellType(ship, strip);
		}
    addOpenSea();
	}

	// Adds sea once the fleet has been placed
	private void addOpenSea() {
		for (int row = 1; row <= GRID_SIZE; row++) {
			for (int column = 1; column <= GRID_SIZE; column++) {
				GridLocation coordinate = new GridLocation(row, column);
				if (getPoint(coordinate) == null) {
					setPoint(coordinate, sea);
				}
			}
		}
	}

	// Generates and checks placement, and then places a selected CellType at the start of the game
	private void addAutomaticCellType(CellType cellType) {
		int[] randomData = randomData();
    GridLocation coordinate = new GridLocation(randomData[1], randomData[2]);
    Orientation orientation = new Orientation(randomData[0]);

    while (!canPlaceCellType(coordinate, orientation, cellType)) {
      randomData = randomData();
      coordinate = new GridLocation(randomData[1], randomData[2]);
      orientation = new Orientation(randomData[0]);
    }

    placeCellType(coordinate, orientation, cellType);
    increaseShipTotalByOne();
	}

	private void addManualCellType(CellType cellType, Strip strip) {
    GridLocation coordinate = strip.getLocation()[0];
    Orientation orientation;
    if (strip.isHorizontal()) {
      orientation = new Orientation(Orientation.HORIZONTAL_POSITIVE_ITERATOR);
    } else {
      orientation = new Orientation(Orientation.VERTICAL_POSITIVE_ITERATOR);
    }

    placeCellType(coordinate, orientation, cellType);
    increaseShipTotalByOne();
	}

	// Checks whether a CellType can be placed in a certain position on the grid
	private boolean canPlaceCellType(GridLocation coordinate, Orientation orientation, CellType cellType) {
		for (int i = 0 ; i < cellType.getLength(); i++) {
			int row = coordinate.getRow() + i * orientation.getRowIterator();
			int column = coordinate.getColumn() + i * orientation.getColumnIterator();
			GridLocation checkGridLocation = new GridLocation(row, column);
			if (!checkGridLocation.locatedInGrid()) {
				return false;
			}
			if (getPoint(checkGridLocation) != null) {
				return false;
			}
		}
		return true;
	}

	// Places CellTypes onto the grid
	private void placeCellType(GridLocation coordinate, Orientation orientation, CellType cellType) {
		for (int i = 0 ; i < cellType.getLength(); i++) {
			GridLocation placeGridLocation = new GridLocation(coordinate.getRow() + i * orientation.getRowIterator(),
			  coordinate.getColumn() + i * orientation.getColumnIterator());
			setPoint(placeGridLocation, cellType);
		}
	}

	// Generates pseudo-random array for use in placing CellTypes of a given inputted length.
	private int[] randomData() {
		int[] randomData = new int[3];
		randomData[0] = (int) (Math.random() * ORIENTATIONS_POSSIBLE);
		randomData[1] = (int) Math.ceil((Math.random() * GRID_SIZE));
		randomData[2] = (int) Math.ceil((Math.random() * GRID_SIZE));
		return randomData;
	}

  public int describeContents(){
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    int row;
    int column;
    dest.writeInt(shipTotal);

    for (int i = 0; i < (GRID_SIZE + 1)*(GRID_SIZE + 1); i++) {
      row = i / (GRID_SIZE + 1);
      column = i % (GRID_SIZE + 1);
      if (row > 0 && column > 0) {
        dest.writeParcelable(cellTypeGrid[row][column], flags);
      }
    }
  }

  public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
    public OceanModel createFromParcel(Parcel in) {
      return new OceanModel(in);
    }
    public OceanModel[] newArray(int size) {
      return new OceanModel[size];
    }
  };
}
