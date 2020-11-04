package com.example.pintu;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;


import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import static android.app.AlertDialog.*;

@SuppressLint("ViewConstructor")
public class MainView extends View {
    private final String TITLE= "拼图大作战";
    private final Context context;
    private final int WIDTH = MainActivity2.getScreenWidth();//获取屏幕宽度
    private final int HEIGHT = MainActivity2.getScreenWidth();
    private Bitmap back;//背景
    private final Paint paint;
    private int subWidth;
    private Bitmap[] bitmapTiles;//存储子图片
    private int[][] array;
    private int[][] picPosition;//记录移动后的图片位置
    private final int COL;
    private final int ROW;
    private boolean isSuccess;
    int steps;
    private final int[][] dir = {
            {-1, 0},//左
            {0, -1},//上
            {1, 0},//右
            {0, 1}//下
    };

    /**
     * @param context context传入
     * @param col     行数，难度
     */
    public MainView(Context context, int col) {
        super(context);
        this.context = context;
        this.COL = col;
        this.ROW = col;
        paint = new Paint();
        paint.setAntiAlias(true);
        init();
        startGame();
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
            back = Bitmap.createScaledBitmap(bitmap, WIDTH, HEIGHT, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        subWidth = back.getWidth() / COL;
        bitmapTiles = new Bitmap[COL * ROW];
        int idx = 0;
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                bitmapTiles[idx++] = Bitmap.createBitmap(back, j * subWidth, i * subWidth, subWidth, subWidth);
            }
        }
    }

    /**
     * 开始游戏
     */
    private void startGame() {
        picPosition = createRandomBoard(ROW, COL);
        isSuccess = false;
        steps = 0;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        paint.setTextSize(100);
        paint.setColor(Color.BLACK);

        @SuppressLint("DrawAllocation") Rect bounds = new Rect();
        paint.getTextBounds(TITLE, 0, TITLE.length(), bounds);
        canvas.drawText(TITLE, (getMeasuredWidth() >> 1) - (bounds.width() >> 1), 300, paint);
        paint.setTextSize(70);
        canvas.drawText("移动步数："+steps,(getMeasuredWidth() >> 1) - (bounds.width() >> 1), 400,paint);
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                int idx = picPosition[i][j];
                if (idx == ROW * COL - 1 && !isSuccess)
                    continue;
                canvas.drawBitmap(bitmapTiles[idx], j * subWidth, i * subWidth + 500, paint);
            }
        }
    }

    /**
     * 将屏幕上的点转换成,对应拼图块的索引
     *
     * @param x 横坐标
     * @param y 纵坐标
     * @return 数组索引
     */
    private Point xyToIndex(int x, int y) {
        int col = (x / subWidth) + (x % subWidth > 0 ? 1 : 0);
        int row = (y / subWidth) + (y % subWidth > 0 ? 1 : 0);

        return new Point(col - 1, row - 1);
    }


    /**
     * @param arr 传入现图片排列顺序
     * @return 是否成功
     */
    public boolean isSuccess(int[][] arr) {
        int idx = 0;
        for (int[] ints : arr) {
            for (int j = 0; j < ints.length && idx < ROW * COL - 1; j++) {
                if (ints[j] != idx) {
                    return false;
                }
                idx++;
            }
        }
        return true;
    }

    private void createBasicArray(int row, int col) {
        array = new int[row][col];
        int idx = 0;
        for (int i = 0; i < row; i++)
            for (int j = 0; j < col; j++)
                array[i][j] = idx++;
    }

    /**
     * 移动块的位置
     * @param src 起始点
     * @param dirId
     * @return 新的位置，错误返回new Point(-1,-1);
     */
    public Point move(Point src, int dirId) {

        int x = src.getX() + dir[dirId][0];
        int y = src.getY() + dir[dirId][1];
        if (!isPointIn(new Point(x,y))) {
            return new Point(-1, -1);
        }

        int temp = array[y][x];
        array[y][x] = array[src.getY()][src.getX()];
        array[src.getY()][src.getX()] = temp;

        return new Point(x, y);
    }

    /**
     * 得到下一个可以移动的位置
     *
     * @param src
     * @return
     */
    private Point getNextPoint(Point src) {
        Random rd = new Random();
        int idx = rd.nextInt(4);//产生0~3的随机数
        Point newPoint = move(src, idx);
        if (newPoint.getX() != -1 && newPoint.getY() != -1) {
            return newPoint;
        }
        return getNextPoint(src);
    }

    /**
     * 生成拼图数据
     * @param row
     * @param col
     * @return
     */
    public int[][] createRandomBoard(int row, int col) {
        createBasicArray(row, col);
        int count = 0;
        Point tempPoint = new Point(col - 1, row - 1);
        Random rd = new Random();
        int num = rd.nextInt(100) + 50;//产生50~149的随机数
        while (count < num) {
            tempPoint = getNextPoint(tempPoint);
            count++;
        }
        return array;
    }

    public boolean isPointIn(Point point) {
        return point.getX() >= 0 && point.getX() < ROW && point.getY() >= 0 && point.getY() < COL;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && event.getY() >= 500 && event.getY() <= 500 + back.getHeight()) {
            Point point = xyToIndex((int) event.getX(), (int) event.getY() - 500);

            for (int[] ints : dir) {
                int newX = point.getX() + ints[0];
                int newY = point.getY() + ints[1];

                if (isPointIn(new Point(newX,newY))) {
                    if (picPosition[newY][newX] == COL * ROW - 1) {
                        steps++;
                        int temp = picPosition[point.getY()][point.getX()];
                        picPosition[point.getY()][point.getX()] = picPosition[newY][newX];
                        picPosition[newY][newX] = temp;
                        invalidate();
                        if (isSuccess(picPosition)) {
                            isSuccess = true;
                            invalidate();
                            String successText = String.format("恭喜你拼图成功，一共移动了" + steps + "次");
                            new Builder(context)
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
}
