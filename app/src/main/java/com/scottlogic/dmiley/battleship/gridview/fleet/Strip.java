package com.scottlogic.dmiley.battleship.gridview.fleet;

import android.os.Parcel;
import android.os.Parcelable;

import com.scottlogic.dmiley.battleship.util.GridLocation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Models strip to be placed on Ocean Grid
public class Strip implements Parcelable, Serializable {

    private boolean isPlaced;
    private int length;
    private int foregroundColor;
    private GridLocation[] location;

    // Default constructor containing whether strip placed on ocean, its length and coloring on grid
    Strip(boolean isPlaced, int length, int foregroundColor) {
        setIsPlaced(isPlaced);
        setLength(length);
        setForegroundColor(foregroundColor);
        resetStripLocation();
    }

    Strip(Parcel in) {
        isPlaced = in.readByte() != 0x00;
        length = in.readInt();
        foregroundColor = in.readInt();
        location = new GridLocation[2];
        location[0] = in.readParcelable(GridLocation.class.getClassLoader());
        location[1] = in.readParcelable(GridLocation.class.getClassLoader());
    };

    public boolean getIsPlaced() {
        return isPlaced;
    }

    protected void setIsPlaced(boolean placed) {
        this.isPlaced = placed;
    }

    public int getLength() {
        return length;
    }

    private void setLength(int length) {
        this.length = length;
    }

    public int getForegroundColor() {
        return foregroundColor;
    }

    private void setForegroundColor(int foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    public GridLocation[] getLocation() {
        return location;
    }

    // Overloaded setters for location on ocean
    protected void setLocation(GridLocation[] location) {
        this.location = location;
    }

    // Overloaded setters for location on ocean
    protected void setLocation(GridLocation locationStart, GridLocation locationEnd) {
        this.location[0] = locationStart;
        this.location[1] = locationEnd;
    }

    // Takes strip off ocean grid
    protected void resetStripLocation() {
        this.location = new GridLocation[2];
    }

    // Checks if strip horizontal
    public boolean isHorizontal() {
        return getLocation()[0].getRow() == getLocation()[1].getRow();
    }

    // Checks if strip vertical
    public boolean isVertical() {
        return getLocation()[0].getColumn() == getLocation()[1].getColumn();
    }

    // Checks if two strips equal
    public boolean equals(Strip strip) {
        if (!(getIsPlaced() == strip.getIsPlaced())) {
            return false;
        }
        if (!(getLength() == strip.getLength())) {
            return false;
        }
        if (!(getForegroundColor() == strip.getForegroundColor())) {
            return false;
        }
        if (!(getLocation()[0].equals(strip.getLocation()[0]))) {
            return false;
        }
        if (!(getLocation()[1].equals(strip.getLocation()[1]))) {
            return false;
        }
        return true;
    }

    // Checks if strip contains a specified grid location
    public boolean contains(GridLocation gridLocation) {
        GridLocation[] gridLocationArray = new GridLocation[getLength()];
        if (isHorizontal()) {
            for (int i = 0; i < getLength(); i++) {
                gridLocationArray[i] = new GridLocation(getLocation()[0].getRow(), getLocation()[0].getColumn() + i);
            }
        } else {
            for (int i = 0; i < getLength(); i++) {
                gridLocationArray[i] = new GridLocation(getLocation()[0].getRow() + i, getLocation()[0].getColumn());
            }
        }
        for (int j = 0; j < getLength(); j++) {
            if (gridLocationArray[j].equals(gridLocation)) {
                return true;
            }
        }
        return false;
    }

    // Checks if two strips intersect one another
    public boolean intersects(Strip strip) {
        GridLocation[] gridLocationArray = new GridLocation[getLength()];
        if (isHorizontal()) {
            for (int i = 0; i < getLength(); i++) {
                gridLocationArray[i] = new GridLocation(getLocation()[0].getRow(), getLocation()[0].getColumn() + i);
            }
        } else {
            for (int i = 0; i < getLength(); i++) {
                gridLocationArray[i] = new GridLocation(getLocation()[0].getRow() + i, getLocation()[0].getColumn());
            }
        }
        GridLocation[] stripGridLocationArray = new GridLocation[strip.getLength()];
        if (strip.isHorizontal()) {
            for (int i = 0; i < strip.getLength(); i++) {
                stripGridLocationArray[i] = new GridLocation(strip.getLocation()[0].getRow(), strip.getLocation()[0].getColumn() + i);
            }
        } else {
            for (int i = 0; i < strip.getLength(); i++) {
                stripGridLocationArray[i] = new GridLocation(strip.getLocation()[0].getRow() + i, strip.getLocation()[0].getColumn());
            }
        }
        for (int j = 0; j < getLength(); j++) {
            for (int k = 0; k < strip.getLength(); k++) {
                if (gridLocationArray[j].equals(stripGridLocationArray[k])) {
                    return true;
                }
            }
        }
        return false;
    }

    public GridLocation[] findGridLocations() {
        GridLocation[] gridLocations = new GridLocation[getLength()];
        if (isHorizontal()) {
            for (int i = 0; i < getLength(); i++) {
                gridLocations[i] = new GridLocation(getLocation()[0].getRow(), getLocation()[0].getColumn() + i);
            }
        } else {
            for (int i = 0; i < getLength(); i++) {
                gridLocations[i] = new GridLocation(getLocation()[0].getRow() + i, getLocation()[0].getColumn());
            }
        }
        return gridLocations;
    }

    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isPlaced ? 0x01 : 0x00));
        dest.writeInt(length);
        dest.writeInt(foregroundColor);
        dest.writeParcelable(location[0], flags);
        dest.writeParcelable(location[1], flags);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        public Strip createFromParcel(Parcel in) {
            return new Strip(in);
        }

        public Strip[] newArray(int size) {
            return new Strip[size];
        }
    };
}

