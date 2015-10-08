package com.scottlogic.dmiley.battleship.gridview;

import com.scottlogic.dmiley.battleship.R;
import com.scottlogic.dmiley.battleship.gridview.event.StripTouchedEvent;
import com.scottlogic.dmiley.battleship.gridview.event.StripTouchedListener;
import com.scottlogic.dmiley.battleship.gridview.fleet.Fleet;
import com.scottlogic.dmiley.battleship.gridview.fleet.Strip;
import com.scottlogic.dmiley.battleship.util.GridLocation;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import java.util.ArrayList;
import java.util.List;

// Display a touchable Battleship Ocean
public class FleetPlacementGridView extends BattleshipGridView {

  private int validStripSelectedBorderColor;
  private int invalidStripSelectedBorderColor;
  private int[] stripColors;

  private Fleet fleet;
  private boolean touchToSelectEnabled;

  private List<StripTouchedListener> stripTouchedListeners;

  // Default view constructor
  public FleetPlacementGridView(Context context, AttributeSet attributeSet) {
    super(context, attributeSet);

    // Initialise stripTouched listeners list
    stripTouchedListeners = new ArrayList<>();

    // Initialise colors of selected strip highlight
    TypedArray typedArray = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.FleetPlacementGridView, 0, 0);
    try {
      validStripSelectedBorderColor = typedArray.getInteger(R.styleable.FleetPlacementGridView_validStripSelectedBorderColor, 0);
      invalidStripSelectedBorderColor = typedArray.getInteger(R.styleable.FleetPlacementGridView_invalidStripSelectedBorderColor, 0);
    } finally {
      typedArray.recycle();
    }

    //Initialise the fleet
    TypedArray typedFleetArray = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.Fleet, 0, 0);
    try {
      int destroyerForegroundColor = typedFleetArray.getInteger(R.styleable.Fleet_destroyerForegroundColor, 0);
      int cruiserForegroundColor = typedFleetArray.getInteger(R.styleable.Fleet_cruiserForegroundColor, 0);
      int submarineForegroundColor = typedFleetArray.getInteger(R.styleable.Fleet_submarineForegroundColor, 0);
      int battleshipForegroundColor = typedFleetArray.getInteger(R.styleable.Fleet_battleshipForegroundColor, 0);
      int aircraftCarrierForegroundColor = typedFleetArray.getInteger(R.styleable.Fleet_aircraftCarrierForegroundColor, 0);
      stripColors = new int[]{destroyerForegroundColor, cruiserForegroundColor, submarineForegroundColor, battleshipForegroundColor, aircraftCarrierForegroundColor};

      fleet = new Fleet(stripColors);
    } finally {
      typedFleetArray.recycle();
    }
  }

  public void setTouchToSelectEnabled(boolean touchToSelectEnabled) {
    this.touchToSelectEnabled = touchToSelectEnabled;
  }

  public int[] getStripColors() {
    return stripColors;
  }

  public Fleet getFleet() {
    return fleet;
  }

  public void setFleet(Fleet fleet) {
    this.fleet = fleet;
  }

  public void resetFleet() {
    fleet = new Fleet(stripColors);
    invalidate();
  }

  // Add listener object to strip touched listeners
  public void addStripTouchedListener(StripTouchedListener stripTouchedListener) {
    stripTouchedListeners.add(stripTouchedListener);
  }

  // Remove listener object from strip touched listeners
  public void removeStripTouchedListener(StripTouchedListener stripTouchedListener) {
    stripTouchedListeners.remove(stripTouchedListener);
  }

  // On strip touched event, passes in which strip was selected
  private void onStripSelected(int stripID) {
    StripTouchedEvent stripTouchedEvent = new StripTouchedEvent(this);
    for (StripTouchedListener stripTouchedListener : stripTouchedListeners) {
      (stripTouchedListener).onStripTouched(stripTouchedEvent, stripID);
    }
  }

  // Sets active strip location after cell touched
  private void setActiveStripLocation(GridLocation[] touchedStrip) {
    fleet.setActiveStripLocation(touchedStrip);
    onActiveStripLocationChanged();
  }

  // Called by Ocean activity, logic for selecting a strip
  public void selectStrip(int stripID) {
    if (fleet.hasActiveStrip()) {
      if (!validActiveStripPlacement()) {
        fleet.removeActiveStripFromOcean();
      }
    }
    if (stripID > 0) {
      fleet.setActiveStrip(stripID);
      if (!fleet.getActiveStrip().getIsPlaced()) {
        fleet.addActiveStripToOcean();
      }
    } else {
      fleet.resetActiveStrip();
    }
    onSelectionChanged();
    invalidate();
  }

  // Indicates the selected strip location changed
  private void onActiveStripLocationChanged() {
    onSelectionChanged();
  }

  // Logic for touching on ocean
  @Override
  protected void onCellTouched(GridLocation touchedCell) {
    if (gridSelectionEnabled) {
      if (touchedCell.locatedInGrid()) {
        if (touchToSelectEnabled) {
          touchToSelect(touchedCell);
        }
        // Logic for moving active strip according to the cell touched
        if (!fleet.hasActiveStrip()) {
          addNextStrip();
        }
        if (fleet.hasActiveStrip()) {
          GridLocation[] touchedStrip = findStripLocation(touchedCell);
          setActiveStripLocation(touchedStrip);
        }
    } else {
        // Touch (0,0) to add a new strip to ocean grid
        if (touchedCell.getColumn() == 0 && touchedCell.getRow() == 0) {
          addNextStrip();
        }
      }
      invalidate();
    }
  }

  // Logic for touch to select strip
  private void touchToSelect(GridLocation touchedCell) {
    int stripID = 0;
    for (Strip strip : fleet.getStrips()) {
      stripID ++;
      if (strip.getIsPlaced()) {
        if (fleet.hasActiveStrip()) {
          if (strip.contains(touchedCell) && !fleet.getActiveStrip().intersects(strip)) {
            onStripSelected(stripID);
          }
        } else {
          if (strip.contains(touchedCell)) {
            onStripSelected(stripID);
          }
        }
      }
    }
  }

  private void addNextStrip() {
    int stripID = 0;
    for (Strip strip : fleet.getStrips()) {
      stripID ++;
      if (!strip.getIsPlaced()) {
        onStripSelected(stripID);
        onSelectionChanged();
        return;
      }
    }
  }

  // Logic for dragging on ocean
  @Override
  protected void onCellDragged(GridLocation touchedCell) {
    if (gridSelectionEnabled) {
      if (fleet.hasActiveStrip() && touchedCell.locatedInGrid()) {
        GridLocation[] touchedStrip = findStripLocation(touchedCell);
        setActiveStripLocation(touchedStrip);
        invalidate();
      }
    }
  }

    // Logic for double tapping on ocean
    @Override
    protected void onCellDoubleTapped(GridLocation touchedCell) {
      if (gridSelectionEnabled) {
        if (fleet.hasActiveStrip() && touchedCell.locatedInGrid()) {
          GridLocation[] touchedStrip = findStripRotation(touchedCell);
          setActiveStripLocation(touchedStrip);
          invalidate();
        }
      }
    }

  // Returns new strip location for touch event
  private GridLocation[] findStripLocation(GridLocation touchedCell) {
    GridLocation[] stripLocation;
    if (fleet.getActiveStrip().isHorizontal()) {
      stripLocation = findHorizontalShipPlacement(touchedCell);
    } else {
      stripLocation = findVerticalShipPlacement(touchedCell);
    }
    return stripLocation;
  }

  // Returns new strip location for double tap event
  private GridLocation[] findStripRotation(GridLocation touchedCell) {
    GridLocation[] stripLocation;
    if (fleet.getActiveStrip().isHorizontal()) {
      stripLocation = findVerticalShipPlacement(touchedCell);
    } else {
      stripLocation = findHorizontalShipPlacement(touchedCell);
    }
    return stripLocation;
  }

  // Returns horizontal ship placement for a touched cell
  private GridLocation[] findHorizontalShipPlacement(GridLocation touchedCell) {
    if (touchedCell.getColumn() < GRID_SIZE / 2) {
      int columnStart = touchedCell.getColumn() - (int) ((fleet.getActiveStrip().getLength() - 1) / 2);
      columnStart = columnStart >= 1 ? columnStart : 1;
      return new GridLocation[]{new GridLocation(touchedCell.getRow(), columnStart), new GridLocation(touchedCell.getRow(), columnStart + fleet.getActiveStrip().getLength() - 1)};
    } else {
      int columnEnd = touchedCell.getColumn() + (int) ((fleet.getActiveStrip().getLength() - 1) / 2);
      columnEnd = columnEnd <= GRID_SIZE ? : columnEnd : GRID_SIZE;
      return new GridLocation[]{new GridLocation(touchedCell.getRow(), columnEnd - fleet.getActiveStrip().getLength() + 1), new GridLocation(touchedCell.getRow(), columnEnd)};
    }
  }

  // Returns vertical ship placement for a touched cell
  private GridLocation[] findVerticalShipPlacement(GridLocation touchedCell) {
    if (touchedCell.getRow() < GRID_SIZE / 2) {
      int rowStart = touchedCell.getRow() - (int) ((fleet.getActiveStrip().getLength() - 1) / 2);
      rowStart = rowStart >= 1 ? rowStart : 1;
      return new GridLocation[]{new GridLocation(rowStart, touchedCell.getColumn()), new GridLocation(rowStart + fleet.getActiveStrip().getLength() - 1, touchedCell.getColumn())};
    } else {
      int rowEnd = touchedCell.getRow() + (int) ((fleet.getActiveStrip().getLength() - 1) / 2);
      rowEnd = rowEnd <= GRID_SIZE ? columnEnd : GRID_SIZE;
      return new GridLocation[]{new GridLocation(rowEnd - fleet.getActiveStrip().getLength() + 1, touchedCell.getColumn()), new GridLocation(rowEnd, touchedCell.getColumn())};
    }
  }

  public void activateUI() {
    setGridSelectionEnabled(true);
    invalidate();
  }

  public void deactivateUI() {
    setGridSelectionEnabled(false);
    fleet.resetActiveStrip();
    redrawCoordinateLabelForeground();
    redrawGridBorder();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    calculateCanvasParameters();

    if(fleet.hasActiveStrip()) {
      drawCoordinateLabelBorder(canvas);
      drawSelectedStripCoordinateLabelBorder(canvas);
      drawCoordinateLabelForeground(canvas);
      drawSelectedStripCoordinateLabelForeground(canvas);
      drawCoordinateLabelText(canvas);
    } else {
      drawCoordinateLabel(canvas);
    }

    drawGridBorder(canvas);

    drawDefaultGridBorder(canvas);
    if (fleet.hasActiveStrip()) {
      drawSelectedStripBorder(canvas);
    }

    drawDefaultGridForeground(canvas);
    drawFleetForeground(canvas);
  }

  // Draw border on coordinate axis to signify selected strip placement validity
  private void drawSelectedStripCoordinateLabelBorder(Canvas canvas) {
    Paint paint = new Paint();
    if (validActiveStripPlacement()) {
      paint.setColor(validStripSelectedBorderColor);
    } else {
      paint.setColor(invalidStripSelectedBorderColor);
    }
    if (fleet.getActiveStrip().isHorizontal()) {
      int columnStart = fleet.getActiveStrip().getLocation()[0].getColumn();
      int columnEnd = fleet.getActiveStrip().getLocation()[1].getColumn();
      int row = fleet.getActiveStrip().getLocation()[0].getRow();
      GridLocation gridLocation = new GridLocation(row, 0);
      drawCellBorder(canvas, gridLocation, paint);
      for (int columnCounter = columnStart; columnCounter <= columnEnd; columnCounter++) {
        gridLocation = new GridLocation(0, columnCounter);
        drawCellBorder(canvas, gridLocation, paint);
      }
    } else {
      int rowStart = fleet.getActiveStrip().getLocation()[0].getRow();
      int rowEnd = fleet.getActiveStrip().getLocation()[1].getRow();
      int column = fleet.getActiveStrip().getLocation()[0].getColumn();
      GridLocation gridLocation = new GridLocation(0, column);
      drawCellBorder(canvas, gridLocation, paint);
      for (int rowCounter = rowStart; rowCounter <= rowEnd; rowCounter++) {
        gridLocation = new GridLocation(rowCounter, 0);
        drawCellBorder(canvas, gridLocation, paint);
      }
    }
  }

  // Draw color of ship foreground on coordinate axis
  private void drawSelectedStripCoordinateLabelForeground(Canvas canvas) {
    Paint paint = new Paint();
    paint.setColor(fleet.getActiveStrip().getForegroundColor());
    if (fleet.getActiveStrip().isHorizontal()) {
      int columnStart = fleet.getActiveStrip().getLocation()[0].getColumn();
      int columnEnd = fleet.getActiveStrip().getLocation()[1].getColumn();
      int row = fleet.getActiveStrip().getLocation()[0].getRow();
      GridLocation gridLocation = new GridLocation(row, 0);
      drawCellForeground(canvas, gridLocation, paint);
      for (int columnCounter = columnStart; columnCounter <= columnEnd; columnCounter++) {
        gridLocation = new GridLocation(0, columnCounter);
        drawCellForeground(canvas, gridLocation, paint);
      }
    } else {
      int rowStart = fleet.getActiveStrip().getLocation()[0].getRow();
      int rowEnd = fleet.getActiveStrip().getLocation()[1].getRow();
      int column = fleet.getActiveStrip().getLocation()[0].getColumn();
      GridLocation gridLocation = new GridLocation(0, column);
      drawCellForeground(canvas, gridLocation, paint);
      for (int rowCounter = rowStart; rowCounter <= rowEnd; rowCounter++) {
        gridLocation = new GridLocation(rowCounter, 0);
        drawCellForeground(canvas, gridLocation, paint);
      }
    }
  }

  // Draw border of selected strip to indicate validity
  private void drawSelectedStripBorder(Canvas canvas) {
    Paint paint = new Paint();
    if (validActiveStripPlacement()) {
      paint.setColor(validStripSelectedBorderColor);
    } else {
      paint.setColor(invalidStripSelectedBorderColor);
    }
    if (fleet.getActiveStrip().isHorizontal()) {
      for (int i = 0; i < fleet.getActiveStrip().getLength(); i++) {
        GridLocation gridLocation = new GridLocation(fleet.getActiveStrip().getLocation()[0].getRow(), fleet.getActiveStrip().getLocation()[0].getColumn() + i);
        drawCellBorder(canvas, gridLocation, paint);
      }
    } else {
      for (int i = 0; i < fleet.getActiveStrip().getLength(); i++) {
        GridLocation gridLocation = new GridLocation(fleet.getActiveStrip().getLocation()[0].getRow() + i, fleet.getActiveStrip().getLocation()[0].getColumn());
        drawCellBorder(canvas, gridLocation, paint);
      }
    }
  }

  // Draw strip color foreground on main grid
  private void drawStripForeground(Canvas canvas, Strip strip) {
    Paint paint = new Paint();
    paint.setColor(strip.getForegroundColor());
    if (strip.isHorizontal()) {
      for (int i = 0; i < strip.getLength(); i++) {
        GridLocation gridLocation = new GridLocation(strip.getLocation()[0].getRow(), strip.getLocation()[0].getColumn() + i);
        drawCellForeground(canvas, gridLocation, paint);
      }
    } else {
      for (int i = 0; i < strip.getLength(); i++) {
        GridLocation gridLocation = new GridLocation(strip.getLocation()[0].getRow() + i, strip.getLocation()[0].getColumn());
        drawCellForeground(canvas, gridLocation, paint);
      }
    }
  }

  // Draws foreground for each strip in fleet
  private void drawFleetForeground(Canvas canvas) {
    for (Strip strip : fleet.getStrips()) {
      if (strip.getIsPlaced()) {
        drawStripForeground(canvas, strip);
      }
    }
    // Draw selected strip foreground at highest level
    if (fleet.hasActiveStrip()) {
      drawStripForeground(canvas, fleet.getActiveStrip());
    }
  }

  // Checks if active strip placed in a valid position on ocean grid
  private boolean validActiveStripPlacement() {
    for (Strip strip : fleet.getStrips()) {
      if (strip.getIsPlaced()) {
        if (!fleet.getActiveStrip().equals(strip)) {
          if (fleet.getActiveStrip().intersects(strip)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  // Checks if whole fleet is placed in a valid position on grid
  public boolean validFleetPlacement() {
    if (fleet.hasActiveStrip()) {
      if (!validActiveStripPlacement()) {
        return false;
      }
    }
    for (Strip strip : fleet.getStrips()) {
      if (!strip.getIsPlaced()) {
        return false;
      }
    }
    return true;
  }
}
