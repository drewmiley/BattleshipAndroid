package com.scottlogic.dmiley.battleship.gridview.fleet;

import com.scottlogic.dmiley.battleship.util.GridLocation;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;
import java.util.ArrayList;

// Models the strips to be deployed onto the Ocean grid
public class Fleet implements Parcelable, Serializable {

  private Strip destroyer;
  private Strip cruiser;
  private Strip submarine;
  private Strip battleship;
  private Strip aircraftCarrier;
  private ArrayList<Strip> strips;

  private Strip activeStrip;

  // Default constructor for fleet
  public Fleet(int[] stripColors) {
    destroyer = new Strip(false, 2, stripColors[0]);
    cruiser = new Strip(false, 3, stripColors[1]);
    submarine = new Strip(false, 3, stripColors[2]);
    battleship = new Strip(false, 4, stripColors[3]);
    aircraftCarrier = new Strip(false, 5, stripColors[4]);

    strips = new ArrayList<>();
    strips.add(destroyer);
    strips.add(cruiser);
    strips.add(submarine);
    strips.add(battleship);
    strips.add(aircraftCarrier);
  }

  public Fleet(Parcel in) {
    destroyer = in.readParcelable(Strip.class.getClassLoader());
    cruiser = in.readParcelable(Strip.class.getClassLoader());
    submarine = in.readParcelable(Strip.class.getClassLoader());
    battleship = in.readParcelable(Strip.class.getClassLoader());
    aircraftCarrier = in.readParcelable(Strip.class.getClassLoader());

    strips = new ArrayList<>();
    strips.add(destroyer);
    strips.add(cruiser);
    strips.add(submarine);
    strips.add(battleship);
    strips.add(aircraftCarrier);
  }

  public ArrayList<Strip> getStrips() {
    return strips;
  }

  // Checks if a strip is currently selected
  public boolean hasActiveStrip() {
    return getActiveStrip()!= null;
  }

  public Strip getActiveStrip() {
    return activeStrip;
  }

  public void setActiveStrip(int stripId) {
    this.activeStrip = strips.get(stripId - 1);
  }

  // Resets active strip for when no strip selected
  public void resetActiveStrip() {
    this.activeStrip = null;
  }

  // Overloaded setters for active strip location
  public void setActiveStripLocation(GridLocation[] location) {
    getActiveStrip().setLocation(location);
  }

  // Adds active strip to ocean
  public void setActiveStripLocation(GridLocation locationStart, GridLocation locationEnd) {
    getActiveStrip().setLocation(locationStart, locationEnd);
  }

  // Adds active strip to ocean
  public void addActiveStripToOcean() {
    setActiveStripLocation(new GridLocation(1, 1), new GridLocation(getActiveStrip().getLength(), 1));
    getActiveStrip().setIsPlaced(true);
  }

  // Removes active strip from ocean
  public void removeActiveStripFromOcean() {
    getActiveStrip().resetStripLocation();
    getActiveStrip().setIsPlaced(false);
  }

  public int describeContents(){
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(destroyer, flags);
    dest.writeParcelable(cruiser, flags);
    dest.writeParcelable(submarine, flags);
    dest.writeParcelable(battleship, flags);
    dest.writeParcelable(aircraftCarrier, flags);
  }

  public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
    public Fleet createFromParcel(Parcel in) {
      return new Fleet(in);
    }
    public Fleet[] newArray(int size) {
      return new Fleet[size];
    }
  };
}
