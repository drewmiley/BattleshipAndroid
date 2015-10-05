package com.scottlogic.dmiley.battleship.util;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

// Battleship Grid Location
public class GridLocation implements Parcelable, Serializable {
	
	private int row;
	private int column;

    private final int GRID_SIZE = 10;
    private final String[] COLUMN_COORDINATE = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
    private final String[] ROW_COORDINATE = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};

    // Creates a new coordinate structure based on its constituent coordinates
	public GridLocation(int row, int column) {
        if (locatedInView(row, column)) {
            setRow(row);
            setColumn(column);
        }
	}

    public GridLocation(Parcel in) {
        row = in.readInt();
        column = in.readInt();
    }

    // Checks if touched coordinate within Battleship view
    private boolean locatedInView(int row, int column) {
        if (row <= GRID_SIZE + 1 && row >= 0) {
            if (column <= GRID_SIZE + 1 && column >= 0) {
                return true;
            }
        }
        return false;
    }

    // Checks if touched coordinate within selectable Battleship grid.
    public boolean locatedInGrid() {
        if (getColumn() > 0 && getColumn() <= GRID_SIZE) {
            if (getRow() > 0 && getRow() <= GRID_SIZE) {
                return true;
            }
        }
        return false;
    }

    // Gets the one based index of the row
	public int getRow() {
		return row;
	}

    // Sets the one based index of the row
	private void setRow(int row) {
		this.row = row;
	}

    // Gets the one based index of the column
	public int getColumn() {
		return column;
	}

    // Sets the one based index of the column
	private void setColumn(int column) {
		this.column = column;
	}

    // Returns a String depiction of a grid location
    public String displayText() {

        String displayText = COLUMN_COORDINATE[getColumn() - 1];
        displayText += ROW_COORDINATE[getRow() - 1];

        return displayText;
    }

    public boolean equals(GridLocation gridLocation) {
        if (getRow() == gridLocation.getRow()) {
            if (getColumn() == gridLocation.getColumn()) {
                return true;
            }
        }
        return false;
    }

    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(row);
        dest.writeInt(column);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        public GridLocation createFromParcel(Parcel in) {
            return new GridLocation(in);
        }

        public GridLocation[] newArray(int size) {
            return new GridLocation[size];
        }
    };
}
