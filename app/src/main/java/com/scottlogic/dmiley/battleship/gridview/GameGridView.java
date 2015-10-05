package com.scottlogic.dmiley.battleship.gridview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.scottlogic.dmiley.battleship.R;
import com.scottlogic.dmiley.battleship.logic.oceantools.CellType;
import com.scottlogic.dmiley.battleship.util.GridLocation;
import com.scottlogic.dmiley.battleship.util.ShotData;

// Display a touchable Battleship Radar
public class GameGridView extends BattleshipGridView {

    private int validCellSelectedBorderAlternateColor;
    private int validCellSelectedBorderColor;
    private int invalidCellSelectedBorderColor;
    private int validCellSelectedForegroundAlternateColor;
    private int validCellSelectedForegroundColor;
    private int invalidCellSelectedForegroundColor;
    private int searchedCellForegroundColor;
    private int sunkCellFormatColor;
    private int hitCellFormatColor;
    private int missCellFormatColor;
    private int missCellFormatAlternateColor;

    private GridLocation selectedCell;

    private boolean validCellSelected;

    private ShotData[][] shotDataDisplay;

    private boolean fleetPlacementShown;

    private int[] stripColors;

    private final static int BLUE_WHITE_COLOR_SCHEME = 0;
    private final static int GREEN_BLACK_COLOR_SCHEME = 1;

    public GameGridView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        fleetPlacementShown = false;

        shotDataDisplay = new ShotData[GRID_SIZE + 1][GRID_SIZE + 1];

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.GameGridView, 0, 0);
        try {
            validCellSelectedBorderColor = typedArray.getInteger(R.styleable.GameGridView_validCellSelectedBorderColor, 0);
            validCellSelectedBorderAlternateColor = typedArray.getInteger(R.styleable.GameGridView_validCellSelectedBorderAlternateColor, 0);
            invalidCellSelectedBorderColor = typedArray.getInteger(R.styleable.GameGridView_invalidCellSelectedBorderColor, 0);
            validCellSelectedForegroundColor = typedArray.getInteger(R.styleable.GameGridView_validCellSelectedForegroundColor, 0);
            validCellSelectedForegroundAlternateColor = typedArray.getInteger(R.styleable.GameGridView_validCellSelectedForegroundAlternateColor, 0);
            invalidCellSelectedForegroundColor = typedArray.getInteger(R.styleable.GameGridView_invalidCellSelectedForegroundColor, 0);
            searchedCellForegroundColor = typedArray.getInteger(R.styleable.GameGridView_searchedCellForegroundColor, 0);
            sunkCellFormatColor = typedArray.getInteger(R.styleable.GameGridView_sunkCellFormatColor, 0);
            hitCellFormatColor = typedArray.getInteger(R.styleable.GameGridView_hitCellFormatColor, 0);
            missCellFormatColor = typedArray.getInteger(R.styleable.GameGridView_missCellFormatColor, 0);
            missCellFormatAlternateColor = typedArray.getInteger(R.styleable.GameGridView_missCellFormatAlternateColor, 0);
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
        } finally {
            typedFleetArray.recycle();
        }
    }

    @Override
    public void swapColorScheme() {
        super.swapColorScheme();

        int tempColorMemory = missCellFormatColor;
        missCellFormatColor = missCellFormatAlternateColor;
        missCellFormatAlternateColor = tempColorMemory;

        tempColorMemory = validCellSelectedBorderColor;
        validCellSelectedBorderColor = validCellSelectedBorderAlternateColor;
        validCellSelectedBorderAlternateColor = tempColorMemory;

        tempColorMemory = validCellSelectedForegroundColor;
        validCellSelectedForegroundColor = validCellSelectedForegroundAlternateColor;
        validCellSelectedForegroundAlternateColor = tempColorMemory;
    }

    public boolean getFleetPlacementShown() {
        return fleetPlacementShown;
    }

    public void setFleetPlacementShown(boolean fleetPlacementShown) {
        this.fleetPlacementShown = fleetPlacementShown;
    }

    public GridLocation getSelectedCell() {
        return selectedCell;
    }

    public void setSelectedCell(GridLocation touchedCell) {
        selectedCell = touchedCell;
        onSelectedCellChanged();
    }

    public void resetSelectedCell() {
        selectedCell = null;
        onSelectedCellChanged();
    }

    private void setValidCellSelected(boolean validCellSelected) {
        this.validCellSelected = validCellSelected;
    }

    public ShotData[][] getShotDataDisplay() {
        return shotDataDisplay;
    }

    public void setShotDataDisplay(ShotData[][] shotDataDisplay) {
        this.shotDataDisplay = shotDataDisplay;
    }

    private ShotData getShotDisplayPoint(GridLocation gridLocation) {
        return shotDataDisplay[gridLocation.getRow()][gridLocation.getColumn()];
    }

    private void setShotDisplayPoint(GridLocation firedAtCell, ShotData currentShotData) {
        shotDataDisplay[firedAtCell.getRow()][firedAtCell.getColumn()] = currentShotData;
    }

    // Indicates the selected cell property changed
    private void onSelectedCellChanged() {
        onSelectionChanged();
    }

    // Logic for touching on radar
    @Override
    protected void onCellTouched(GridLocation touchedCell) {
        if (gridSelectionEnabled) {
            if (touchedCell.locatedInGrid()) {
                setSelectedCell(touchedCell);
            }
        }
    }

    // Logic for dragging on radar
    @Override
    protected void onCellDragged(GridLocation touchedCell) {
        if(gridSelectionEnabled) {
            if (touchedCell.locatedInGrid()) {
                setSelectedCell(touchedCell);
            } else {
                resetSelectedCell();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        calculateCanvasParameters();

        if (!(getSelectedCell() == null)) {
            drawCoordinateLabelBorder(canvas);
            drawCoordinateLabelForeground(canvas);
            drawSelectedCellCoordinateLabelForeground(canvas);
            drawCoordinateLabelText(canvas);
        } else {
            drawCoordinateLabel(canvas);
        }
        drawGridBorder(canvas);

        drawDefaultGridBorder(canvas);
        if (!(getSelectedCell() == null)) {
            drawSelectedCellBorder(canvas);
        }

        drawDefaultGridForeground(canvas);

        if (fleetPlacementShown) {
            drawFleetPlacementForeground(canvas);
        }

        drawFiredAtGridPeg(canvas);
    }

    private void drawFleetPlacementForeground(Canvas canvas) {
        for (int row = 1; row < GRID_SIZE + 1; row++) {
            for (int column = 1; column < GRID_SIZE + 1; column++) {
                GridLocation gridLocation = new GridLocation(row, column);
                if (getShotDisplayPoint(gridLocation).isHit()) {
                    CellType cellType = getShotDisplayPoint(gridLocation). getCellType();
                    drawCellTypeForeground(canvas, gridLocation, cellType);
                }
            }
        }
    }

    private void drawCellTypeForeground(Canvas canvas, GridLocation gridLocation, CellType cellType) {
        Paint paint = new Paint();
        paint.setColor(stripColors[cellType.getID() - 1]);

        drawCellForeground(canvas, gridLocation, paint);
    }

    private void drawSearchedGridForeground(Canvas canvas) {
        for (int row = 1; row < GRID_SIZE + 1; row++) {
            for (int column = 1; column < GRID_SIZE + 1; column++) {
                GridLocation gridLocation = new GridLocation(row, column);
                if(!(getShotDisplayPoint(gridLocation) == null)) {
                    drawSearchedCellForeground(canvas, gridLocation);
                }
            }
        }
    }

    private void drawFiredAtGridPeg(Canvas canvas) {
        for (int row = 1; row < GRID_SIZE + 1; row++) {
            for (int column = 1; column < GRID_SIZE + 1; column++) {
                GridLocation gridLocation = new GridLocation(row, column);
                if (!(getShotDisplayPoint(gridLocation) == null)) {
                    if(getShotDisplayPoint(gridLocation).isSearched()) {
                        if (getShotDisplayPoint(gridLocation).isHit()) {
                            if(getShotDisplayPoint(gridLocation).isSunk()) {
                                drawSunkCell(canvas, gridLocation);
                            } else {
                                drawHitCell(canvas, gridLocation);
                            }
                        } else if (!getShotDisplayPoint(gridLocation).isHit()) {
                            drawMissCell(canvas, gridLocation);
                        }
                    }
                }
            }
        }
    }

    private void drawSelectedCellBorder(Canvas canvas) {
        Paint paint = new Paint();
        if (validCellSelected) {
            paint.setColor(validCellSelectedBorderColor);
        } else {
            paint.setColor(invalidCellSelectedBorderColor);
        }
        drawCellBorder(canvas, getSelectedCell(), paint);
    }

    private void drawSelectedCellForeground(Canvas canvas) {
        Paint paint = new Paint();
        if(validCellSelected) {
            paint.setColor(validCellSelectedForegroundColor);
        } else {
            paint.setColor(invalidCellSelectedForegroundColor);
            drawCellForeground(canvas, getSelectedCell(), paint);
        }
    }

    // Draw border on coordinate axis to signify selected cell
    private void drawSelectedCellCoordinateLabelBorder(Canvas canvas) {
        Paint paint = new Paint();
        if (validCellSelected) {
            paint.setColor(validCellSelectedBorderColor);
        } else {
            paint.setColor(invalidCellSelectedBorderColor);
        }
        int column = getSelectedCell().getColumn();
        GridLocation gridLocation = new GridLocation(0, column);
        drawCellBorder(canvas, gridLocation, paint);
        int row = getSelectedCell().getRow();
        gridLocation = new GridLocation(row, 0);
        drawCellBorder(canvas, gridLocation, paint);
    }

    // Draw selected cell position on coordinate axis
    private void drawSelectedCellCoordinateLabelForeground(Canvas canvas) {
        Paint paint = new Paint();
        if (validCellSelected) {
            paint.setColor(validCellSelectedForegroundColor);
        } else {
            paint.setColor(invalidCellSelectedForegroundColor);
        }
        int column = getSelectedCell().getColumn();
        GridLocation gridLocation = new GridLocation(0, column);
        drawCellForeground(canvas, gridLocation, paint);
        int row = getSelectedCell().getRow();
        gridLocation = new GridLocation(row, 0);
        drawCellForeground(canvas, gridLocation, paint);
    }

    private void drawSearchedCellForeground(Canvas canvas, GridLocation gridLocation) {
        if (gridLocation.locatedInGrid()) {
            Paint paint = new Paint();
            paint.setColor(searchedCellForegroundColor);
            drawCellForeground(canvas, gridLocation, paint);
        }
    }

    private void drawMissCell(Canvas canvas, GridLocation gridLocation) {
        if (gridLocation.locatedInGrid()) {
            Paint paint = new Paint();
            paint.setColor(missCellFormatColor);
            drawPeg(canvas, gridLocation, paint);
        }
    }

    private void drawHitCell(Canvas canvas, GridLocation gridLocation) {
        if (gridLocation.locatedInGrid()) {
            Paint paint = new Paint();
            paint.setColor(hitCellFormatColor);
            drawPeg(canvas, gridLocation, paint);
        }
    }

    private void drawSunkCell(Canvas canvas, GridLocation gridLocation) {
        if (gridLocation.locatedInGrid()) {
            Paint paint = new Paint();
            paint.setColor(sunkCellFormatColor);
            drawPeg(canvas, gridLocation, paint);
        }
    }

    public void activateUI() {
        setGridSelectionEnabled(true);
        invalidate();
    }

    public void deactivateUI() {
        setGridSelectionEnabled(false);
        resetSelectedCell();
        redrawCoordinateLabelForeground();
        redrawGridBorder();
    }

    public void updateCursorDisplay(boolean validCellSelected) {
        setValidCellSelected(validCellSelected);
        redrawCoordinateLabelForeground();
        redrawGridBorder();
    }

    public void updateCellDisplay(GridLocation gridLocation, ShotData currentShotData) {
        setShotDisplayPoint(gridLocation, currentShotData);
        redrawGridForeground();
    }
}

