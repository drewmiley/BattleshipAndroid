package com.scottlogic.dmiley.battleship.logic.oceantools;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;

public class CellType implements Parcelable, Serializable {

  private int ID;
	private boolean isShip;
	private String name;
	private int length;
	private int currentHealth;

	public CellType(int ID, boolean isShip, String name, int length) {
    this.setID(ID);
		this.setIsShip(isShip);
		this.setName(name);
		this.setLength(length);
		this.startingHealth(length);
	}

  public CellType(Parcel in) {
    ID = in.readInt();
    int isShipInteger = in.readInt();
    isShip = isShipInteger == 1;
    name = in.readString();
    length = in.readInt();
    currentHealth = in.readInt();
  }

  public int getID() {
      return ID;
  }

  public void setID(int ID) {
      this.ID = ID;
  }

  public boolean getIsShip() {
      return isShip;
  }

  private void setIsShip(boolean isShip) {
      this.isShip = isShip;
  }

	public int getLength() {
		return length;
	}

	private void setLength(int length) {
		this.length = length;
	}

	public String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

	private void startingHealth(int startingHealth) {
		this.currentHealth = startingHealth;
	}

	public int remainingHealth() {
		return currentHealth;
	}

	public void hit() {
		currentHealth--;
	}

  public boolean equals(CellType cellType) {
    if (!(getIsShip() == cellType.getIsShip())) {
        return false;
    }
    if (!(getName().equals(cellType.getName()))) {
        return false;
    }
    if (!(getLength() == cellType.getLength())) {
        return false;
    }
    if (!(remainingHealth() == cellType.remainingHealth())) {
        return false;
    }
    return true;
  }

  public int describeContents(){
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(ID);
    int isShipInteger = isShip ? 1 : 0;
    dest.writeInt(isShipInteger);
    dest.writeString(name);
    dest.writeInt(length);
    dest.writeInt(currentHealth);
  }

  public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
    public CellType createFromParcel(Parcel in) {
      return new CellType(in);
    }
    public CellType[] newArray(int size) {
      return new CellType[size];
    }
  };
}
