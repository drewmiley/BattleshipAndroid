package com.scottlogic.dmiley.battleship.logic;

import com.scottlogic.dmiley.battleship.util.GridLocation;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;

// Models which cells have been searched and which are still available to fire on
public class SearchedGridModel implements Parcelable, Serializable {

	private boolean[][] searchedGrid;

	private final static int GRID_SIZE = 10;

	public SearchedGridModel() {
		searchedGrid = new boolean[GRID_SIZE + 1][GRID_SIZE + 1];
	}

  public SearchedGridModel(Parcel in) {
    searchedGrid = new boolean[GRID_SIZE + 1][GRID_SIZE + 1];
    boolean[] searchedGridRow = new boolean[GRID_SIZE + 1];
    for (int row = 0; row <= GRID_SIZE; row++) {
      in.readBooleanArray(searchedGridRow);
      for (int column = 0; column <= GRID_SIZE; column++) {
        searchedGrid[row][column] = searchedGridRow[column];
      }
    }
  }

	public boolean getPoint(GridLocation gridLocation) {
		return searchedGrid[gridLocation.getRow()][gridLocation.getColumn()];
	}

	public void setPointToTrue(GridLocation gridLocation) {
		searchedGrid[gridLocation.getRow()][gridLocation.getColumn()] = true;
	}

  public int describeContents(){
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    boolean[] searchedGridRow;
    for (int row = 0; row <= GRID_SIZE; row++) {
      searchedGridRow = searchedGridRow(row);
      dest.writeBooleanArray(searchedGridRow);
    }
  }

  private boolean[] searchedGridRow(int row) {
    boolean[] searchedGridRow = new boolean[GRID_SIZE + 1];
    for (int column = 0; column <= GRID_SIZE; column++) {
      searchedGridRow[column] = searchedGrid[row][column];
    }
    return searchedGridRow;
  }

  public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
    public SearchedGridModel createFromParcel(Parcel in) {
      return new SearchedGridModel(in);
    }
    public SearchedGridModel[] newArray(int size) {
      return new SearchedGridModel[size];
    }
  };
}
