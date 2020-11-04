package com.example.pintu;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import com.example.pintu.util.MLog;

import java.io.IOException;
import java.io.InputStream;

import static android.app.AlertDialog.*;

public class MainView extends View {
    private static final String TAG = MainView.class.getSimpleName();
    private final Context context;
    private final int WIDTH = MainActivity2.getScreenWidth();//获取屏幕宽度
    private Bitmap back;//背景
    private Paint paint;
    private int tileWidth;
    private int tileHeight;
    private Bitmap[] bitmapTiles;//存储子图片
    private int[][] dataTiles;//记录移动后的图片位置
    private Board tilesBoard;
    private  int COL;
    private  int ROW;
    private int[][] dir = {
            {-1, 0},//左
            {0, -1},//上
            {1, 0},//右
            {0, 1}//下
    };
    private boolean isSuccess;

    int steps = 0;

    public MainView(Context context,int col) {
        super(context);
        this.context = context;
        this.COL = col;
        this.ROW = col;
        paint = new Paint();
        paint.setAntiAlias(true);
        init();
        startGame();
        MLog.d(TAG, WIDTH + "," + WIDTH);
    }

    /**
     * 初始化
     */
    private void init() {
        //载入图像，并将图片切成块
        AssetManager assetManager = context.getAssets();
        try {
            InputStream assetInputStream = assetManager.open("back.jpg");
            Bitmap bitmap = BitmapFactory.decodeStream(assetInputStream);
            back = Bitmap.createScaledBitmap(bitmap, WIDTH, WIDTH, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        tileWidth = back.getWidth() / COL;
        tileHeight = back.getHeight() / ROW;
        bitmapTiles = new Bitmap[COL * ROW];
        int idx = 0;
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                bitmapTiles[idx++] = Bitmap.createBitmap(back, j * tileWidth, i * tileHeight, tileWidth, tileHeight);
            }
        }
    }

    /**
     * 开始游戏
     */
    private void startGame() {
        tilesBoard = new Board();
        dataTiles = tilesBoard.createRandomBoard(ROW, COL);
        isSuccess = false;
        steps = 0;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        paint.setTextSize(100);
        paint.setColor(Color.BLACK);
        canvas.drawText("拼图大作战",500,300,paint);
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                int idx = dataTiles[i][j];
                if (idx == ROW * COL - 1 && !isSuccess)
                    continue;
                canvas.drawBitmap(bitmapTiles[idx], j * tileWidth, i * tileHeight+500, paint);
            }
        }
    }

    /**
     * 将屏幕上的点转换成,对应拼图块的索引
     *
     * @param x
     * @param y
     * @return
     */
    private Point xyToIndex(int x, int y) {
        int extraX = x % tileWidth > 0 ? 1 : 0;
        int extraY = x % tileWidth > 0 ? 1 : 0;
        int col = x / tileWidth + extraX;
        int row = y / tileHeight + extraY;

        return new Point(col - 1, row - 1);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Point point = xyToIndex((int) event.getX(), (int) event.getY()-500);

            for (int i = 0; i < dir.length; i++) {
                int newX = point.getX() + dir[i][0];
                int newY = point.getY() + dir[i][1];

                if (newX >= 0 && newX < COL && newY >= 0 && newY < ROW) {
                    if (dataTiles[newY][newX] == COL * ROW - 1) {
                        steps++;
                        int temp = dataTiles[point.getY()][point.getX()];
                        dataTiles[point.getY()][point.getX()] = dataTiles[newY][newX];
                        dataTiles[newY][newX] = temp;
                        invalidate();
                        if (tilesBoard.isSuccess(dataTiles)) {
                            isSuccess = true;
                            invalidate();
                            String successText = String.format("恭喜你拼图成功，移动了"+steps+"次");
                            new

                                    Builder(context)
                                    .setTitle("拼图成功")
                                    .setCancelable(false)
                                    .setMessage(successText)
                                    .setPositiveButton("重新开始", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            startGame();
                                        }
                                    })
                                    .setNegativeButton("退出游戏", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            System.exit(0);
                                        }
                                    })
                                    .create()
                                    .show();
                        }
                    }
                }
            }
        }
        return true;
    }

    private void printArray(int[][] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                sb.append(arr[i][j] + ",");
            }
            sb.append("\n");
        }
        MLog.d(TAG, sb.toString());
    }
}
