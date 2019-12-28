package jp.ac.jjc.battleship;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class BoardView extends RelativeLayout {

    Paint boardLinePaint;
    Board board;
    Bitmap mBitmap;
    Canvas mCanvas;
    Bitmap bmpUserHit;
    final int BOARD_SIZE = 10;
    private boolean readyToDraw = false;

    public BoardView(Context context) {
        super(context);
        init();
    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void setBoard(Board board) {
        this.board = board;
    }

    private void init() {
        boardLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boardLinePaint.setColor(Color.BLUE);
        boardLinePaint.setStyle(Paint.Style.STROKE);
        boardLinePaint.setStrokeWidth(2);
        bmpUserHit = BitmapFactory.decodeResource(getResources(), R.drawable.user_hit);
//        bmpUserHit = Bitmap.createScaledBitmap(bmpUserHit, Math.round(cellSize()), Math.round(cellSize()), true);
    }

    @Override
     protected void onSizeChanged(int w, int h, int oldw, int oldh) {
         super.onSizeChanged(w, h, oldw, oldh);

         mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
         mCanvas = new Canvas(mBitmap);
     }

    @Override
     protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mCanvas = canvas;

        drawGrid();
        if(readyToDraw) {
            drawBoard();
        }
     }

     private Bitmap createBitmap(int imageId) {
         Bitmap bmp = BitmapFactory.decodeResource(getResources(), imageId);
         return Bitmap.createScaledBitmap(bmp, Math.round(cellSize()), Math.round(cellSize()), true);
     }

     void drawBoard(){
        if(board == null){
            return;
        }
        for(Cell[] cells : board.getCells()){
            for(Cell cell : cells) {
                if(!cell.hasShip() && !cell.isHit() && cell.getImageId() != -1) {
                    continue;
                }
                if(cell.hasShip() && !cell.getShip().isVisible()) {
                    if (cell.isHit()) {
                        mCanvas.drawBitmap(bmpUserHit, cell.getX() * cellSize(), cell.getY() * cellSize(), null);
                    }
                } else {
                    mCanvas.drawBitmap(createBitmap(cell.getImageId()), cell.getX() * cellSize(), cell.getY() * cellSize(), null);
                }

            }
        }
     }

     void drawGrid() {
        for(int i = 0; i < numOfLines(); i++) {
            //Draw horizontal lines
            mCanvas.drawLine(0, i * cellSize(), maxCoord(), i * cellSize(), boardLinePaint);
            //Draw vertical lines
            mCanvas.drawLine(i * cellSize(), 0, i * cellSize(), maxCoord(), boardLinePaint);
        }
     }

     int numOfLines() {
         return BOARD_SIZE + 1;
     }

     float cellSize() {
         return Math.min(getMeasuredHeight(), getMeasuredWidth()) / (float) BOARD_SIZE;
     }

     float maxCoord() {
         return cellSize() * BOARD_SIZE;
     }

     void readyToDraw() {
        readyToDraw = true;
     }

     Cell locateCell(float x, float y) {
         int cellX = (int) (x / cellSize());
         int cellY = (int) (y / cellSize());
         if(board.isOutOfBounds(cellX, cellY)) {
             return null;
         }
         return board.getCells()[cellX][cellY];
    }
}
