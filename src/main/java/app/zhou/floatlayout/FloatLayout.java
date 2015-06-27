package app.zhou.floatlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by zzhoujay on 2015/6/6 0006.
 */
public class FloatLayout extends ViewGroup {

    private int widthSpace = 10;//子View横向间距
    private int heightSpace = 10;//子View纵向间距
    private boolean autoCenter = false;

    public FloatLayout(Context context) {
        this(context, null);
    }

    public FloatLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray=context.obtainStyledAttributes(attrs,R.styleable.FloatLayout);
        autoCenter=typedArray.getBoolean(R.styleable.FloatLayout_auto_center,false);
        widthSpace= (int) typedArray.getDimension(R.styleable.FloatLayout_width_space,widthSpace);
        heightSpace= (int) typedArray.getDimension(R.styleable.FloatLayout_height_space,heightSpace);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int currScanWidth = 0;//当前已扫描到的宽度
        int currScanHeight = 0;//当前已扫描到的高度
        int currScanMaxHeight = 0;//当前已扫描到的该行最大高度
        int currLineIndex = 0;//当前行数

        ArrayList<Line> viewLines =null;

        if(autoCenter){
            viewLines=new ArrayList<>();
            viewLines.add(new Line(0));
        }

        int allWidth = r - l;//提供给子View的最大宽度
        int childCount = getChildCount();//子View数目
        //遍历子View并将它们分成n行
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            if (childHeight > currScanMaxHeight) {
                currScanMaxHeight = childHeight;
            }

            if (currScanWidth + childWidth > allWidth) {
                //new line
                if(autoCenter){
                    if (viewLines != null) {
                        viewLines.get(currLineIndex).generateMaxHeight(currScanMaxHeight);
                    }
                }
                currScanHeight += currScanMaxHeight + heightSpace;

                if (autoCenter) {
                    Line line = new Line();
                    line.minHeight = currScanHeight;
                    if (viewLines != null) {
                        viewLines.add(line);
                    }
                    currLineIndex++;
                }

                currScanMaxHeight = childHeight;
                currScanWidth = 0;
            }

            if (!autoCenter) {
                child.layout(currScanWidth, currScanHeight, currScanWidth + childWidth, currScanHeight + childHeight);
            } else {
                if (i == (childCount - 1)) {
                    if (viewLines != null) {
                        viewLines.get(currLineIndex).generateMaxHeight(currScanMaxHeight);
                    }
                }
                if (viewLines != null) {
                    viewLines.get(currLineIndex).addSize();
                }
            }

            currScanWidth += childWidth + widthSpace;
        }
        if (autoCenter) {
            int currViewIndex = 0;
            //按照上面分好的行，为每个子View设置位置
            if (viewLines != null) {
                for (Line line : viewLines) {
                    currScanWidth = 0;
                    int size = line.viewSize;
                    int gap = line.maxHeight - line.minHeight;
                    for (int i = 0; i < size; i++, currViewIndex++) {
                        View v = getChildAt(currViewIndex);
                        int childWidth = v.getMeasuredWidth();
                        int childHeight = v.getMeasuredHeight();

                        int dh = (gap - childHeight) / 2;
                        v.layout(currScanWidth, dh + line.minHeight, currScanWidth + childWidth, line.maxHeight - dh);
                        currScanWidth += childWidth + widthSpace;
                    }
                }
            }
        }

    }

    private class Line {
        public int minHeight;
        public int maxHeight;
        public int viewSize;

        public Line() {
        }

        public Line(int minHeight) {
            this.minHeight = minHeight;
        }

        public void generateMaxHeight(int add) {
            maxHeight = minHeight + add;
        }

        public void addSize() {
            this.viewSize++;
        }

        @Override
        public String toString() {
            return "Line{" +
                    "minHeight=" + minHeight +
                    ", maxHeight=" + maxHeight +
                    ", viewSize=" + viewSize +
                    '}';
        }
    }

    public int getWidthSpace() {
        return widthSpace;
    }

    public void setWidthSpace(int widthSpace) {
        this.widthSpace = widthSpace;
        requestLayout();
    }

    public int getHeightSpace() {
        return heightSpace;
    }

    public void setHeightSpace(int heightSpace) {
        this.heightSpace = heightSpace;
        requestLayout();
    }

    public boolean isAutoCenter() {
        return autoCenter;
    }

    public void setAutoCenter(boolean autoCenter) {
        this.autoCenter = autoCenter;
        requestLayout();
    }
}
