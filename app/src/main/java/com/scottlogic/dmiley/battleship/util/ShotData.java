package com.scottlogic.dmiley.battleship.util;

import com.scottlogic.dmiley.battleship.logic.oceantools.CellType;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;

// Class to model sunk, hit, miss or null for displaying radar and ocean grids
public class ShotData implements Parcelable, Serializable {

  private boolean searched;
  private CellType cellType;
  private boolean sunk;

  public ShotData(boolean searched, CellType cellType, boolean sunk) {
    setSearched(searched);
    setCellType(cellType);
    setSunk(sunk);
  }

  public ShotData(Parcel in) {
    int searchedInteger = in.readInt();
    setSearched(searchedInteger == 1);
    cellType = in.readParcelable(CellType.class.getClassLoader());
    int sunkInteger = in.readInt();
    setSunk(sunkInteger == 1);
  }

  public boolean isSearched() {
    return searched;
  }

  public void setSearched(boolean searched) {
    this.searched = searched;
  }

  public CellType getCellType() {
    return cellType;
  }

  private void setCellType(CellType cellType) {
    this.cellType = cellType;
  }

  public boolean isHit() {
    return getCellType().getIsShip();
  }

  public boolean isSunk() {
    return sunk;
  }

  public void setSunk(boolean sunk) {
    this.sunk = sunk;
  }

  public int describeContents(){
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    int searchedInteger = searched ? 1 : 0;
    dest.writeInt(searchedInteger);
    dest.writeParcelable(cellType, flags);
    int sunkInteger = sunk ? 1 : 0;
    dest.writeInt(sunkInteger);
  }

  public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
    public ShotData createFromParcel(Parcel in) {
      return new ShotData(in);
    }
    public ShotData[] newArray(int size) {
      return new ShotData[size];
    }
  };
}
