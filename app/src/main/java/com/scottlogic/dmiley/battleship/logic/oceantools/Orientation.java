package com.scottlogic.dmiley.battleship.logic.oceantools;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Orientation implements Parcelable, Serializable {

	private int rowIterator;
	
	private int columnIterator;
	
	private int[] validRandomIntegerArray = new int[]{0, 1, 2, 3};

	public static int VERTICAL_NEGATIVE_ITERATOR = 0;

	public static int VERTICAL_POSITIVE_ITERATOR = 1;

	public static int HORIZONTAL_NEGATIVE_ITERATOR = 2;

	public static int HORIZONTAL_POSITIVE_ITERATOR = 3;

	public Orientation(int generatedRandomInteger) {
		int rowIterator = convertRandomIntegerToRowIterator(generatedRandomInteger);
		setRowIterator(rowIterator);
		int columnIterator = convertRandomIntegerToColumnIterator(generatedRandomInteger);
		setColumnIterator(columnIterator);
	}

    public Orientation(Parcel in) {
        rowIterator = in.readInt();
        columnIterator = in.readInt();
    }

	public int getRowIterator() {
		return rowIterator;
	}

	private void setRowIterator(int rowIterator) {
		this.rowIterator = rowIterator;
	}
	
	public int getColumnIterator() {
		return columnIterator;
	}

	private void setColumnIterator(int columnIterator) {
		this.columnIterator = columnIterator;
	}
	
	private int convertRandomIntegerToColumnIterator(int generatedRandomInteger) {
		if (generatedRandomInteger == 2) {
			return -1;
		} else if (generatedRandomInteger == 3) {
			return 1;
		} else {
			return 0;
		}
	}

	private int convertRandomIntegerToRowIterator(int generatedRandomInteger) {
		if (generatedRandomInteger == 0) {
			return -1;
		} else if (generatedRandomInteger == 1) {
			return 1;
		} else {
			return 0;
		}
	}

    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(rowIterator);
        dest.writeInt(columnIterator);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        public Orientation createFromParcel(Parcel in) {
            return new Orientation(in);
        }

        public Orientation[] newArray(int size) {
            return new Orientation[size];
        }
    };
}
