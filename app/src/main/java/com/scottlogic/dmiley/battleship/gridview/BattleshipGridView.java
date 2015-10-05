package com.scottlogic.dmiley.battleship.gridview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.scottlogic.dmiley.battleship.R;
import com.scottlogic.dmiley.battleship.gridview.event.SelectionChangedEvent;
import com.scottlogic.dmiley.battleship.gridview.event.SelectionChangedListener;
import com.scottlogic.dmiley.battleship.util.GridLocation;

import java.util.ArrayList;
import java.util.List;

// Class to produce a touchable Battleship grid for use with further specialisation
public class BattleshipGridView extends View {

    protected boolean gridSelectionEnabled;

    // Resources set in activity xml file
    private int gridForegroundColorOne;
    private int gridForegroundColorTwo;
    private int gridBorderColor;
    private int coordinateLabelForegroundColor;
    private int coordinateLabelBorderColor;
    private int coordinateLabelTextColor;
    private int gridForegroundAlternateColorOne;
    private int gridForegroundAlternateColorTwo;
    private int gridBorderAlternateColor;
    private int coordinateLabelForegroundAlternateColor;
    private int coordinateLabelBorderAlternateColor;
    private int coordinateLabelTextAlternateColor;
    private int textSizeModifier;
    private int defaultBorderPixelSize;

    private GestureDetector doubleTapDetector;

    private List<SelectionChangedListener> selectionChangedListeners;

    private int measuredGridLength;
    private int measuredGridSquareLength;
    private int measuredGridBorderLength;

    private final String[] COLUMN_COORDINATE_LABEL = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
    private final String[] ROW_COORDINATE_LABEL = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    protected final int GRID_SIZE = 10;
    private final int SQUARE_BORDER_RATIO = 15;

    private final static int BLUE_WHITE_COLOR_SCHEME = 0;
    private final static int GREEN_BLACK_COLOR_SCHEME = 1;

    public BattleshipGridView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        // Used for detecting double taps
        doubleTapDetector = new GestureDetector(context, new DoubleTapListener());

        selectionChangedListeners = new ArrayList<>();

        // Read resources
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.BattleshipGridView, 0, 0);
        try {
            gridForegroundColorOne = typedArray.getInteger(R.styleable.BattleshipGridView_gridForegroundColorOne, 0);
            gridForegroundColorTwo = typedArray.getInteger(R.styleable.BattleshipGridView_gridForegroundColorTwo, 0);
            gridBorderColor = typedArray.getInteger(R.styleable.BattleshipGridView_gridBorderColor, 0);
            coordinateLabelForegroundColor = typedArray.getInteger(R.styleable.BattleshipGridView_coordinateLabelForegroundColor, 0);
            coordinateLabelBorderColor = typedArray.getInteger(R.styleable.BattleshipGridView_coordinateLabelBorderColor, 0);
            coordinateLabelTextColor = typedArray.getInteger(R.styleable.BattleshipGridView_coordinateLabelTextColor, 0);
            gridForegroundAlternateColorOne = typedArray.getInteger(R.styleable.BattleshipGridView_gridForegroundAlternateColorOne, 0);
            gridForegroundAlternateColorTwo = typedArray.getInteger(R.styleable.BattleshipGridView_gridForegroundAlternateColorTwo, 0);
            gridBorderAlternateColor = typedArray.getInteger(R.styleable.BattleshipGridView_gridBorderAlternateColor, 0);
            coordinateLabelForegroundAlternateColor = typedArray.getInteger(R.styleable.BattleshipGridView_coordinateLabelForegroundAlternateColor, 0);
            coordinateLabelBorderAlternateColor = typedArray.getInteger(R.styleable.BattleshipGridView_coordinateLabelBorderAlternateColor, 0);
            coordinateLabelTextAlternateColor = typedArray.getInteger(R.styleable.BattleshipGridView_coordinateLabelTextAlternateColor, 0);
            textSizeModifier = typedArray.getInteger(R.styleable.BattleshipGridView_textSizeModifier, 0);
            defaultBorderPixelSize = typedArray.getInteger(R.styleable.BattleshipGridView_defaultBorderPixelSize, 0);
        } finally {
            typedArray.recycle();
        }
        gridSelectionEnabled = true;
    }

    // Swaps between two color schemes of the grid
    public void swapColorScheme() {
        int tempColorMemory;

        tempColorMemory = gridForegroundColorOne;
        gridForegroundColorOne = gridForegroundAlternateColorOne;
        gridForegroundAlternateColorOne = tempColorMemory;

        tempColorMemory = gridForegroundColorTwo;
        gridForegroundColorTwo = gridForegroundAlternateColorTwo;
        gridForegroundAlternateColorTwo = tempColorMemory;

        tempColorMemory = gridBorderColor;
        gridBorderColor = gridBorderAlternateColor;
        gridBorderAlternateColor = tempColorMemory;

        tempColorMemory = coordinateLabelForegroundColor;
        coordinateLabelForegroundColor = coordinateLabelForegroundAlternateColor;
        coordinateLabelForegroundAlternateColor = tempColorMemory;

        tempColorMemory = coordinateLabelBorderColor;
        coordinateLabelBorderColor = coordinateLabelBorderAlternateColor;
        coordinateLabelBorderAlternateColor = tempColorMemory;

        tempColorMemory = coordinateLabelTextColor;
        coordinateLabelTextColor = coordinateLabelTextAlternateColor;
        coordinateLabelTextAlternateColor = tempColorMemory;
    }

    // Add listener object to selection changed listeners
    public void addSelectionChangedListener(SelectionChangedListener selectionChangedListener) {
        selectionChangedListeners.add(selectionChangedListener);
    }

    // Remove listener object from selection changed listeners
    public void removeSelectionChangedListener(SelectionChangedListener selectionChangedListener) {
        selectionChangedListeners.remove(selectionChangedListener);
    }

    // On selection changed
    protected void onSelectionChanged() {
        SelectionChangedEvent selectionChangedEvent = new SelectionChangedEvent(this);
        for (SelectionChangedListener selectionChangedListener : selectionChangedListeners) {
            (selectionChangedListener).onSelectionChanged(selectionChangedEvent);
        }
    }

    // Deals with touch events on the grid.
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        // Grid coordinates touched
        GridLocation touchedGridLocation = toGridLocation(motionEvent.getX(), motionEvent.getY());


        // A cell is touched when you touchdown or move over it
        int action = motionEvent.getAction();
        if (action == MotionEvent.ACTION_DOWN){
            onCellTouched(touchedGridLocation);
        }
        if (action == MotionEvent.ACTION_MOVE) {
            onCellDragged(touchedGridLocation);
        }

        // Whether this event represents a double-tap
        boolean doubleTapped = doubleTapDetector.onTouchEvent(motionEvent);

        // If the cell is double tapped raise the event
        if(doubleTapped) {
            onCellDoubleTapped(touchedGridLocation);
        }

        return true;
    }

    public void setGridSelectionEnabled(boolean gridSelectionEnabled) {
        this.gridSelectionEnabled = gridSelectionEnabled;
    }

    // Cell Touched Event
    protected void onCellTouched(GridLocation gridLocation){
         // Do nothing
    }

    // Cell Dragged Event
    protected void onCellDragged(GridLocation gridLocation) {
        // Do nothing
    }

    // Cell Double Tap Event
    protected void onCellDoubleTapped(GridLocation gridLocation){
        // Do nothing
    }

    // Translate canvas coordinates to grid coordinates
    protected GridLocation toGridLocation(float xFloat, float yFloat) {
        int x = Math.round(xFloat);
        int y = Math.round(yFloat);

        int horizontalTouchCell = (x + defaultBorderPixelSize) / measuredGridSquareLength;
        int verticalTouchCell = (y + defaultBorderPixelSize) / measuredGridSquareLength;

        return new GridLocation(verticalTouchCell, horizontalTouchCell);
    }

    // Calculates parameters for drawing grid
    protected void calculateCanvasParameters() {
        int maximumGridSize = Math.min(getMeasuredHeight(), getMeasuredWidth());
        measuredGridLength = maximumGridSize - 3 * defaultBorderPixelSize;
        measuredGridLength -= (measuredGridLength % (GRID_SIZE + 1));
        measuredGridLength += 3 * defaultBorderPixelSize;

        measuredGridSquareLength = measuredGridLength / (GRID_SIZE + 1);
        measuredGridBorderLength = measuredGridSquareLength / SQUARE_BORDER_RATIO;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        calculateCanvasParameters();

        drawCoordinateLabel(canvas);
        drawGridBorder(canvas);
        drawDefaultGridBorder(canvas);
        drawDefaultGridForeground(canvas);
    }

    // Redraws the selectable Battleship grid
    protected void redrawGrid() {
        GridLocation northWestGridExtremity = new GridLocation(1,1);
        GridLocation southEastGridExtremity = new GridLocation(GRID_SIZE, GRID_SIZE);

        redrawCanvas(northWestGridExtremity, southEastGridExtremity);
    }

    // Redraws the coordinate axis
    protected void redrawCoordinateLabel() {
        redrawCell(new GridLocation(0, 0));
        redrawCanvas(new GridLocation(1, 0), new GridLocation(GRID_SIZE, 0));
        redrawCanvas(new GridLocation(0, 1), new GridLocation(0, GRID_SIZE));
    }

    // Redraws a selected grid square
    protected void redrawCell(GridLocation cell) {
        int columnGridBorder = 2 * defaultBorderPixelSize;
        int rowGridBorder = 2 * defaultBorderPixelSize;
        if (cell.getColumn() == 0) {
            columnGridBorder = defaultBorderPixelSize;
        }
        if (cell.getRow() == 0) {
            rowGridBorder = defaultBorderPixelSize;
        }
        int westExtremity = cell.getColumn() * measuredGridSquareLength + columnGridBorder;
        int northExtremity = cell.getRow() * measuredGridSquareLength + rowGridBorder;
        int eastExtremity = (cell.getColumn() + 1) * measuredGridSquareLength + columnGridBorder;
        int southExtremity = (cell.getRow() + 1) * measuredGridSquareLength + rowGridBorder;
        invalidate(westExtremity, northExtremity, eastExtremity, southExtremity);
    }

    // Redraws a cell foreground
    protected void redrawCellForeground(GridLocation cell) {
        int columnGridBorder = 2 * defaultBorderPixelSize;
        int rowGridBorder = 2 * defaultBorderPixelSize;
        if (cell.getColumn() == 0) {
            columnGridBorder = defaultBorderPixelSize;
        }
        if (cell.getRow() == 0) {
            rowGridBorder = defaultBorderPixelSize;
        }
        int westExtremity = cell.getColumn() * measuredGridSquareLength + columnGridBorder + measuredGridBorderLength;
        int northExtremity = cell.getRow() * measuredGridSquareLength + rowGridBorder + measuredGridBorderLength;
        int eastExtremity = (cell.getColumn() + 1) * measuredGridSquareLength + columnGridBorder - measuredGridBorderLength;
        int southExtremity = (cell.getRow() + 1) * measuredGridSquareLength + rowGridBorder - measuredGridBorderLength;
        invalidate(westExtremity, northExtremity, eastExtremity, southExtremity);
    }

    // Redraws a cell border
    protected void redrawCellBorder(GridLocation cell) {
        int columnGridBorder = 2 * defaultBorderPixelSize;
        int rowGridBorder = 2 * defaultBorderPixelSize;
        if (cell.getColumn() == 0) {
            columnGridBorder = defaultBorderPixelSize;
        }
        if (cell.getRow() == 0) {
            rowGridBorder = defaultBorderPixelSize;
        }
        int columnPixel = cell.getColumn() * measuredGridSquareLength;
        int rowPixel = cell.getRow() * measuredGridSquareLength;
        // North Cell Border
        redrawHorizontalLine(rowPixel + rowGridBorder, measuredGridBorderLength, columnPixel + columnGridBorder, columnPixel + columnGridBorder + measuredGridSquareLength);
        // West Cell Border
        redrawVerticalLine(columnPixel + columnGridBorder, measuredGridBorderLength, rowPixel + rowGridBorder, rowPixel + rowGridBorder + measuredGridSquareLength);
        // South Cell Border
        redrawHorizontalLine(rowPixel + rowGridBorder + measuredGridSquareLength - measuredGridBorderLength, measuredGridBorderLength, columnPixel + columnGridBorder, columnPixel + columnGridBorder + measuredGridSquareLength);
        // East Cell Border
        redrawVerticalLine(columnPixel + columnGridBorder + measuredGridSquareLength - measuredGridBorderLength, measuredGridBorderLength, rowPixel + rowGridBorder, rowPixel + rowGridBorder + measuredGridSquareLength);

    }

    // Redraws specified horizontal line
    private void redrawHorizontalLine(int verticalPixelValue, int linePixelSize, int westPixelExtremity, int eastPixelExtremity) {
        invalidate(westPixelExtremity, verticalPixelValue, eastPixelExtremity, verticalPixelValue + linePixelSize);
    }

    // Redraws specified vertical line
    private void redrawVerticalLine(int horizontalPixelValue, int linePixelSize, int northPixelExtremity, int southPixelExtremity) {
        invalidate(horizontalPixelValue, northPixelExtremity, horizontalPixelValue + linePixelSize, southPixelExtremity);
    }

    // Redraws foreground on grid cells
    protected void redrawGridForeground() {
        GridLocation gridLocation;
        for (int row = 1; row <= GRID_SIZE; row++) {
            for (int column = 1; column <= GRID_SIZE; column++) {
                gridLocation = new GridLocation(row, column);
                redrawCellForeground(gridLocation);
            }
        }
    }

    // Redraws borders on grid cells
    protected void redrawGridBorder() {
        GridLocation gridLocation;
        for (int row = 1; row <= GRID_SIZE; row++) {
            for (int column = 1; column <= GRID_SIZE; column++) {
                gridLocation = new GridLocation(row, column);
                redrawCellBorder(gridLocation);
            }
        }
    }

    // Redraws foreground on coordinate cells
    protected void redrawCoordinateLabelForeground() {
        GridLocation gridLocation = new GridLocation(0, 0);
        redrawCellForeground(gridLocation);
        for (int row = 1; row <= GRID_SIZE; row++) {
            gridLocation = new GridLocation(row, 0);
            redrawCellForeground(gridLocation);
        }
        for (int column = 1; column <= GRID_SIZE; column++) {
            gridLocation = new GridLocation(0, column);
            redrawCellForeground(gridLocation);
        }
    }

    // Redraws borders on coordinate cells
    protected void redrawCoordinateLabelBorder() {
        GridLocation gridLocation = new GridLocation(0, 0);
        redrawCellBorder(gridLocation);
        for (int row = 1; row <= GRID_SIZE; row++) {
            gridLocation = new GridLocation(row, 0);
            redrawCellBorder(gridLocation);
        }
        for (int column = 1; column <= GRID_SIZE; column++) {
            gridLocation = new GridLocation(0, column);
            redrawCellBorder(gridLocation);
        }
    }
    // Redraws a selected section of the canvas
    protected void redrawCanvas(GridLocation northWestGridExtremity, GridLocation southEastGridExtremity) {
        int westGridBorder = 2 * defaultBorderPixelSize;
        int northGridBorder = 2 * defaultBorderPixelSize;
        int eastGridBorder = 2 * defaultBorderPixelSize;
        int southGridBorder = 2 * defaultBorderPixelSize;
        if (northWestGridExtremity.getColumn() == 0) {
            westGridBorder = defaultBorderPixelSize;
            if (southEastGridExtremity.getColumn() == 0) {
                eastGridBorder = defaultBorderPixelSize;
            }
        }
        if (northWestGridExtremity.getRow() == 0) {
            northGridBorder = defaultBorderPixelSize;
            if (southEastGridExtremity.getRow() == 0) {
                southGridBorder = defaultBorderPixelSize;
            }
        }
        int westExtremity = northWestGridExtremity.getColumn() * measuredGridSquareLength + westGridBorder;
        int northExtremity = northWestGridExtremity.getRow() * measuredGridSquareLength + northGridBorder;
        int eastExtremity = (southEastGridExtremity.getColumn() + 1) * measuredGridSquareLength + eastGridBorder;
        int southExtremity = (southEastGridExtremity.getRow() + 1) * measuredGridSquareLength + southGridBorder;
        invalidate(westExtremity, northExtremity, eastExtremity, southExtremity);
    }

    // Draws cell border
    protected void drawCellBackground(Canvas canvas, GridLocation gridLocation, Paint paint) {
        int columnGridBorder = 2 * defaultBorderPixelSize;
        int rowGridBorder = 2 * defaultBorderPixelSize;
        if (gridLocation.getColumn() == 0) {
            columnGridBorder = defaultBorderPixelSize;
        }
        if (gridLocation.getRow() == 0) {
            rowGridBorder = defaultBorderPixelSize;
        }
        int columnPixel = gridLocation.getColumn() * measuredGridSquareLength;
        int rowPixel = gridLocation.getRow() * measuredGridSquareLength;
        canvas.drawRect(columnPixel + columnGridBorder, rowPixel + rowGridBorder, columnPixel + columnGridBorder + measuredGridSquareLength, rowPixel + rowGridBorder + measuredGridSquareLength, paint);
    }

    // Draws cell foreground
    protected void drawCellForeground(Canvas canvas, GridLocation gridLocation, Paint paint) {
        int columnGridBorder = 2 * defaultBorderPixelSize;
        int rowGridBorder = 2 * defaultBorderPixelSize;
        if (gridLocation.getColumn() == 0) {
            columnGridBorder = defaultBorderPixelSize;
        }
        if (gridLocation.getRow() == 0) {
            rowGridBorder = defaultBorderPixelSize;
        }
        int westAlignment = measuredGridBorderLength + columnGridBorder;
        int northAlignment = measuredGridBorderLength + rowGridBorder;
        int eastAlignment = measuredGridSquareLength - measuredGridBorderLength + columnGridBorder;
        int southAlignment = measuredGridSquareLength - measuredGridBorderLength + rowGridBorder;
        int columnPixel = gridLocation.getColumn() * measuredGridSquareLength;
        int rowPixel = gridLocation.getRow() * measuredGridSquareLength;
        canvas.drawRect(columnPixel + westAlignment, rowPixel + northAlignment, columnPixel + eastAlignment, rowPixel + southAlignment, paint);
    }

    // Draws horizontal line of specified pixel width
    protected void drawHorizontalLine(Canvas canvas, int verticalPixelValue, int linePixelSize, int westPixelExtremity, int eastPixelExtremity, Paint paint) {
        canvas.drawRect(westPixelExtremity, verticalPixelValue, eastPixelExtremity, verticalPixelValue + linePixelSize, paint);
    }

    // Draws vertical line of specified pixel width
    protected void drawVerticalLine(Canvas canvas, int horizontalPixelValue, int linePixelSize, int northPixelExtremity, int southPixelExtremity, Paint paint) {
        canvas.drawRect(horizontalPixelValue, northPixelExtremity, horizontalPixelValue + linePixelSize, southPixelExtremity, paint);
    }

    // Draws cell border
    protected void drawCellBorder(Canvas canvas, GridLocation gridLocation, Paint paint) {
        int columnGridBorder = 2 * defaultBorderPixelSize;
        int rowGridBorder = 2 * defaultBorderPixelSize;
        if (gridLocation.getColumn() == 0) {
            columnGridBorder = defaultBorderPixelSize;
        }
        if (gridLocation.getRow() == 0) {
            rowGridBorder = defaultBorderPixelSize;
        }
        int columnPixel = gridLocation.getColumn() * measuredGridSquareLength;
        int rowPixel = gridLocation.getRow() * measuredGridSquareLength;
        // North Cell Border
        drawHorizontalLine(canvas, rowPixel + rowGridBorder, measuredGridBorderLength, columnPixel + columnGridBorder, columnPixel + columnGridBorder + measuredGridSquareLength, paint);
        // West Cell Border
        drawVerticalLine(canvas, columnPixel + columnGridBorder, measuredGridBorderLength, rowPixel + rowGridBorder, rowPixel + rowGridBorder + measuredGridSquareLength, paint);
        // South Cell Border
        drawHorizontalLine(canvas, rowPixel + rowGridBorder + measuredGridSquareLength - measuredGridBorderLength, measuredGridBorderLength, columnPixel + columnGridBorder, columnPixel + columnGridBorder + measuredGridSquareLength, paint);
        // East Cell Border
        drawVerticalLine(canvas, columnPixel + columnGridBorder + measuredGridSquareLength - measuredGridBorderLength, measuredGridBorderLength, rowPixel + rowGridBorder, rowPixel + rowGridBorder + measuredGridSquareLength, paint);
    }

    // Draws coordinate surround
    protected void drawCoordinateLabel(Canvas canvas) {
        drawCoordinateLabelBorder(canvas);
        drawCoordinateLabelForeground(canvas);
        drawCoordinateLabelText(canvas);
    }

    // Draws grid border surround
    protected void drawGridBorder(Canvas canvas) {
        int outerBorderExtremity = (GRID_SIZE + 1) * measuredGridSquareLength + 3 * defaultBorderPixelSize;
        Paint paint = new Paint();
        paint.setColor(coordinateLabelBorderColor);
        //North Border
        drawHorizontalLine(canvas, 0, defaultBorderPixelSize, 0, outerBorderExtremity, paint);
        //West Border
        drawVerticalLine(canvas, 0, defaultBorderPixelSize, 0, outerBorderExtremity, paint);
        //Number North Border
        drawHorizontalLine(canvas, measuredGridSquareLength + defaultBorderPixelSize, defaultBorderPixelSize, defaultBorderPixelSize, measuredGridSquareLength + defaultBorderPixelSize, paint);
        //Letter West Border
        drawVerticalLine(canvas, measuredGridSquareLength + defaultBorderPixelSize, defaultBorderPixelSize, defaultBorderPixelSize, measuredGridSquareLength + defaultBorderPixelSize, paint);
        //Number South Border
        drawHorizontalLine(canvas, outerBorderExtremity - defaultBorderPixelSize, defaultBorderPixelSize, defaultBorderPixelSize, measuredGridSquareLength + defaultBorderPixelSize, paint);
        //Letter East Border
        drawVerticalLine(canvas, outerBorderExtremity - defaultBorderPixelSize, defaultBorderPixelSize, defaultBorderPixelSize, measuredGridSquareLength + defaultBorderPixelSize, paint);

        paint.setColor(gridBorderColor);
        //North Grid Border
        drawHorizontalLine(canvas, measuredGridSquareLength + defaultBorderPixelSize, defaultBorderPixelSize, measuredGridSquareLength + defaultBorderPixelSize, outerBorderExtremity, paint);
        //West Grid Border
        drawVerticalLine(canvas, measuredGridSquareLength + defaultBorderPixelSize, defaultBorderPixelSize, measuredGridSquareLength + defaultBorderPixelSize, outerBorderExtremity, paint);
        //South Grid Border
        drawHorizontalLine(canvas, outerBorderExtremity - defaultBorderPixelSize, defaultBorderPixelSize, measuredGridSquareLength + defaultBorderPixelSize, outerBorderExtremity, paint);
        //East Grid Border
        drawVerticalLine(canvas, outerBorderExtremity - defaultBorderPixelSize, defaultBorderPixelSize, measuredGridSquareLength + defaultBorderPixelSize, outerBorderExtremity, paint);
    }

    // Draws default grid cell borders
    protected void drawDefaultGridBorder(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(gridBorderColor);
        for (int row = 1; row < GRID_SIZE + 1; row++) {
            for (int column = 1; column < GRID_SIZE + 1; column++) {
                GridLocation gridLocation = new GridLocation(row, column);
                drawCellBorder(canvas, gridLocation, paint);
            }
        }
    }

    // Draws default grid foreground
    protected void drawDefaultGridForeground(Canvas canvas) {
        Paint paint = new Paint();
        for (int row = 1; row < GRID_SIZE + 1; row++) {
            for (int column = 1; column < GRID_SIZE + 1; column++) {
                GridLocation gridLocation = new GridLocation(row, column);
                if ((row + column) % 2 == 0) {
                    paint.setColor(gridForegroundColorOne);
                } else {
                    paint.setColor(gridForegroundColorTwo);
                }
                drawCellForeground(canvas, gridLocation, paint);
            }
        }
    }

    // Draws coordinate label grid square borders
    protected void drawCoordinateLabelBorder(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(coordinateLabelBorderColor);
        GridLocation gridLocation = new GridLocation(0, 0);
        drawCellBorder(canvas, gridLocation, paint);
        for (int row = 1; row < GRID_SIZE + 1; row++) {
            gridLocation = new GridLocation(row, 0);
            drawCellBorder(canvas, gridLocation, paint);
        }
        for (int column = 1; column < GRID_SIZE + 1; column++) {
            gridLocation = new GridLocation(0, column);
            drawCellBorder(canvas, gridLocation, paint);
        }
    }

    // Draws coordinate label grid square foreground
    protected void drawCoordinateLabelForeground(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(coordinateLabelForegroundColor);
        GridLocation gridLocation = new GridLocation(0, 0);
        drawCellForeground(canvas, gridLocation, paint);
        for (int row = 1; row < GRID_SIZE + 1; row++) {
            gridLocation = new GridLocation(row, 0);
            drawCellForeground(canvas, gridLocation, paint);
        }
        for (int column = 1; column < GRID_SIZE + 1; column++) {
            gridLocation = new GridLocation(0, column);
            drawCellForeground(canvas, gridLocation, paint);
        }
    }

    // Draws text for view on grid axes
    protected void drawCoordinateLabelText(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(coordinateLabelTextColor);
        paint.setTextAlign(Paint.Align.CENTER);
        int labelTextSize = (textSizeModifier - 1) * measuredGridSquareLength / textSizeModifier;
        paint.setTextSize(labelTextSize);
        drawLetterLabelText(canvas, paint);
        drawNumberLabelText(canvas, paint);
    }

    // Draws letter coordinate label
    private void drawLetterLabelText(Canvas canvas, Paint paint) {
        int textHorizontalAlignment = measuredGridSquareLength / 2 + 2 * defaultBorderPixelSize;
        int textVerticalAlignment = defaultBorderPixelSize + measuredGridSquareLength - measuredGridSquareLength / textSizeModifier;
        for (int column = 1; column < GRID_SIZE + 1; column++) {
            canvas.drawText(COLUMN_COORDINATE_LABEL[column - 1], column * measuredGridSquareLength + textHorizontalAlignment, textVerticalAlignment, paint);
        }
    }

    // Draws number coordinate label
    private void drawNumberLabelText(Canvas canvas, Paint paint) {
        int textHorizontalAlignment = measuredGridSquareLength / 2 + defaultBorderPixelSize;
        int textVerticalAlignment = 2 * defaultBorderPixelSize - measuredGridSquareLength / textSizeModifier;
        for (int row = 1; row < GRID_SIZE + 1; row++) {
            canvas.drawText(ROW_COORDINATE_LABEL[row - 1], textHorizontalAlignment, (row + 1) * measuredGridSquareLength + textVerticalAlignment, paint);
        }
    }

    // Draws peg in centre of searched cell
    protected void drawPeg(Canvas canvas, GridLocation gridLocation, Paint paint) {
        float horizontalPixelAlignment = gridLocation.getColumn() * measuredGridSquareLength + 2 * defaultBorderPixelSize + measuredGridSquareLength / 2;
        float verticalPixelAlignment = gridLocation.getRow() * measuredGridSquareLength + 2 * defaultBorderPixelSize + measuredGridSquareLength / 2;
        float pegRadius = measuredGridSquareLength / 4;
        canvas.drawCircle(horizontalPixelAlignment, verticalPixelAlignment, pegRadius, paint);
    }
}
