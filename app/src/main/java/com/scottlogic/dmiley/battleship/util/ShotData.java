package com.scottlogic.dmiley.battleship.util;

import android.os.Parcel;
import android.os.Parcelable;

import com.scottlogic.dmiley.battleship.logic.oceantools.CellType;

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
        if(searchedInteger == 1) {
            setSearched(true);
        } else {
            setSearched(false);
        }

        cellType = in.readParcelable(CellType.class.getClassLoader());

        int sunkInteger = in.readInt();
        if(sunkInteger == 1) {
            setSunk(true);
        } else {
            setSunk(false);
        }
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
        if(searched) {
            int searchedInteger = 1;
            dest.writeInt(searchedInteger);
        } else {
            int searchedInteger = 0;
            dest.writeInt(searchedInteger);
        }

        dest.writeParcelable(cellType, flags);

        if(sunk) {
            int sunkInteger = 1;
            dest.writeInt(sunkInteger);
        } else {
            int sunkInteger = 0;
            dest.writeInt(sunkInteger);
        }
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
