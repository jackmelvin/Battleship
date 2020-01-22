package jp.ac.jjc.battleship;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class BoardView extends RelativeLayout {
    private boolean readyToDraw = false;
    Board board;
    Canvas mCanvas;
    Paint boardLinePaint;

    public BoardView(Context context) {
        super(context);
        initializePaint();
    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializePaint();
    }

    private void initializePaint() {
        boardLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boardLinePaint.setColor(Color.BLUE);
        boardLinePaint.setStyle(Paint.Style.STROKE);
        boardLinePaint.setStrokeWidth(3);
    }

    void readyToDraw() {
        readyToDraw = true;
    }

    void setBoard(Board board) {
        this.board = board;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvas = canvas;

        drawGrid();
        drawBoard();
    }

    void drawGrid() {
        if(board == null) return;
        for(int i = 0; i < board.getSize() + 1; i++) {
            //Draw vertical lines
            mCanvas.drawLine(i * cellSize(), 0, i * cellSize(), board.getSize() * cellSize(), boardLinePaint);
            //Draw horizontal lines
            mCanvas.drawLine(0, i * cellSize(), board.getSize() * cellSize(), i * cellSize(), boardLinePaint);
        }
    }

    void drawBoard() {
        if(!readyToDraw || board == null) {
            return;
        }
        Cell cell;
        for(int x = 0; x < board.getSize(); x++) {
            for(int y = 0; y < board.getSize(); y++) {
                cell = board.getCell(x, y);
                float left = cell.getX() * cellSize();
                float top = cell.getY() * cellSize();
                //Skip cells without image
                if(!cell.hasShip() && !cell.isHit()) {
                    continue;
                }
                if(cell.hasShip() && !cell.getShip().isVisible()) {
                    if (cell.isHit()) {
                        mDrawBitmap(R.drawable.user_hit, left, top);
                    }
                } else {
                    mDrawBitmap(cell.getImageId(), left, top);
                }
            }
        }
    }

    void mDrawBitmap(int imageId, float x, float y) {
        Bitmap original = BitmapFactory.decodeResource(getResources(), imageId);
        Bitmap bitmapToDraw = Bitmap.createScaledBitmap(original, (int)cellSize(), (int)cellSize(), true);
        mCanvas.drawBitmap(bitmapToDraw, x, y, null);

    }

    public int[] getDisplayContentSize() {
        Context appContext = MyApp.getContext();
        final WindowManager windowManager = (WindowManager)appContext.getSystemService(Context.WINDOW_SERVICE);
        final Point size = new Point();
        int screenHeight = 0, actionBarHeight = 0;
        int screenWidth = 0;
//        if (appContext.getActionBar() != null) {
//            actionBarHeight = getActionBar().getHeight();
//        }
//        int contentTop = ((ViewGroup) findViewById(android.R.id.content)).getTop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            windowManager.getDefaultDisplay().getSize(size);
            screenWidth = size.x;
            screenHeight = size.y;
        } else {
            Display d = windowManager.getDefaultDisplay();
            screenWidth = d.getWidth();
            screenHeight = d.getHeight();
        }
        return new int[]{screenWidth, screenHeight};
    }

    float cellSize() {
        return boardSize() / (float) board.getSize();
    }

    float boardSize() {
//        return Math.min(getMeasuredHeight(), getMeasuredWidth());
        int[] dSize = getDisplayContentSize();
        return Math.max(dSize[0], dSize[1]) * 9.5f / 20.0f;
    }

    Cell locateCell(float x, float y) {
        return board.getCell((int)(x / cellSize()), (int)(y / cellSize()));
    }

}
