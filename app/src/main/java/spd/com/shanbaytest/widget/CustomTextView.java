package spd.com.shanbaytest.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by linus on 17-9-27.
 */

public class CustomTextView extends android.support.v7.widget.AppCompatTextView {

    private int clickedX = -1;
    private int clickedY = -1;
    private List<String[]> stringArrayList;
    private List<int[]> drawOffsetsList;
    private SpanClickListener spanClickListener;

    public interface SpanClickListener {
        void onclick(View textView, boolean clickSpace);
    }

    public void setSpanClickListener(SpanClickListener spanClickListener){
        this.spanClickListener = spanClickListener;
    }

    public CustomTextView(Context context) {
        super(context);
        init();
    }

    public CustomTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        setMovementMethod(new LinkTouchMovementMethod());
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);

        if (type == BufferType.SPANNABLE){
            setEachWordSpan(this);
        }
    }

    private void clearClickedXY(){
        clickedX = -1;
        clickedY = -1;
    }

    public void clearHighlight(){
        clearClickedXY();
        postInvalidate();
    }

    /*重写onDraw方法，将单词逐个用drawText画出来
     *计算得出每一行两边对齐所缺少的空隙长度，然后将该长度平均分配到单词中间
     *在对单词进行drawText的时候将对应的偏移量加到横坐标上达到两边对齐的效果
     */
    @Override
    protected void onDraw(Canvas canvas) {

        TextPaint paint = getPaint();
        int rowIndex = getPaddingTop();
        int colIndex = getPaddingLeft();

        if (stringArrayList == null){
            stringArrayList = divideOriginalTextToStringLineList();
        }

        if (drawOffsetsList == null){
            drawOffsetsList = calculateDrawOffsets(paint, getWidth() - getPaddingLeft() -
                    getPaddingRight());
        }


        for (int i = 0; i < stringArrayList.size(); i ++){
            rowIndex += getLineHeight();
            colIndex = getPaddingLeft();

            for (int j = 0; j < stringArrayList.get(i).length; j++){
                paint.setColor(Color.BLACK);

                //首个单词不需要偏移
                if (j == 0){
                    if (i == clickedY && j == clickedX) {
                        paint.setColor(Color.GREEN);
                    }
                    canvas.drawText(stringArrayList.get(i)[j], colIndex, rowIndex, paint);
                    colIndex += paint.measureText(stringArrayList.get(i)[j]);

                } else {
                    //逐个通过drawText将单词画出来，横坐标加上对应的偏移量
                    //遇到被点击的单词切换颜色达到高亮效果
                    colIndex += drawOffsetsList.get(i)[j - 1];
                    if (i == clickedY && j == clickedX){
                        canvas.drawText(" ", colIndex, rowIndex, paint);
                        colIndex += paint.measureText(" ");

                        paint.setColor(Color.GREEN);
                        canvas.drawText(stringArrayList.get(i)[j], colIndex, rowIndex, paint);
                        colIndex += paint.measureText(stringArrayList.get(i)[j]);
                    }else {
                        canvas.drawText(" " + stringArrayList.get(i)[j], colIndex, rowIndex, paint);
                        colIndex += paint.measureText(" " + stringArrayList.get(i)[j]);
                    }

                }

            }
        }

    }


    /*根据空格的位置对文本进行拆分，将每个单词设置成clickSpan
     */
    private void setEachWordSpan(TextView textView){
        Spannable spans = (Spannable)textView.getText();
        Integer[] indices = getIndices(
                textView.getText().toString().trim(), ' ');
        int start = 0;
        int end = 0;
        for (int i = 0; i <= indices.length; i++) {
            ClickableSpan clickSpan = new ClickableSpan(){
                @Override
                public void onClick(View widget) {
                    if (spanClickListener != null){
                        spanClickListener.onclick(widget, false);
                    }else {
                        TextView tv = (TextView) widget;
                        String s = tv
                                .getText()
                                .subSequence(tv.getSelectionStart(),
                                        tv.getSelectionEnd()).toString();
                        Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
                    }

                }
            };

            //最后一个单词结尾可能没有空格，按照文本长度计算位置
            end = (i < indices.length ? indices[i] : spans.toString().trim().length());
            spans.setSpan(clickSpan, start, end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = end + 1;
        }
    }


    /*返回字符c在字符串s中的序号数组
     *
     * 获取string中所有的空格键序号，用来创建clickSpan
     * 其中特殊符号替换成空格键处理保证创建的clickSpan内容不包含多余符号
     * @return 序号数组
     */
    private Integer[] getIndices(String s, char c) {
        //将多余的特殊字符当做空格处理
        //通过ascii码对应特殊字符
        for (int i = 33 ; i <= 47; i++){
            s = s.replace((char)i, c);
        }
        for (int i = 91 ; i <= 96; i++){
            s = s.replace((char)i, c);
        }
        for (int i = 123 ; i < 126; i++){
            s = s.replace((char)i, c);
        }

        int pos = s.indexOf(c, 0);
        List<Integer> indices = new ArrayList<Integer>();
        while (pos != -1) {
            indices.add(pos);
            pos = s.indexOf(c, pos + 1);
        }

        Logger.w("s = " + s);
        return (Integer[]) indices.toArray(new Integer[0]);
    }


    private class LinkTouchMovementMethod extends LinkMovementMethod {
        private ClickableSpan mPressedSpan;
        private Spannable lastSpannable;

        @Override
        public boolean onTouchEvent(TextView textView, Spannable spannable, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (lastSpannable != null){
                    Selection.removeSelection(lastSpannable);
                }

                mPressedSpan = getPressedSpan(textView, spannable, event);
            }
            else if (event.getAction() == MotionEvent.ACTION_UP) {
                ClickableSpan touchedSpan = getPressedSpan(textView, spannable, event);

                if (touchedSpan == null){
                    clearHighlight();

                    if (spanClickListener != null){
                        spanClickListener.onclick(textView, true);
                    }
                }

                //action_up的时候判断点击位置是否发生了移动
                if (mPressedSpan != null && touchedSpan == mPressedSpan) {
                    Selection.setSelection(spannable, spannable.getSpanStart(mPressedSpan),
                            spannable.getSpanEnd(mPressedSpan));
                    lastSpannable = spannable;
                    touchedSpan.onClick(textView);
                }
            }else if (event.getAction() == MotionEvent.ACTION_CANCEL){
                super.onTouchEvent(textView, spannable, event);
            }

            return true;
        }
    }

    /**
     * 获取被点击的span
     *
     * @return 被点击的span
     */
    private ClickableSpan getPressedSpan(TextView textView, Spannable spannable, MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();

        x -= textView.getTotalPaddingLeft();
        y -= textView.getTotalPaddingTop();

        x += textView.getScrollX();
        y += textView.getScrollY();

        Layout layout = textView.getLayout();
        int line = layout.getLineForVertical(y);
        int totalOffset = getXDrawOffset(x, line);

        //getXDrawOffset返回-1则代表点击到了空白处 返回null
        if (totalOffset == -1){
            return null;
        }

        //计算单词的在整个字符串中的序号时要去除onDraw中添加的偏移量
        int off = layout.getOffsetForHorizontal(line, x - getXDrawOffset(x, line));

        ClickableSpan[] link = spannable.getSpans(off, off, ClickableSpan.class);
        ClickableSpan clickableSpan = null;
        if (link.length > 0) {
            clickableSpan = link[0];

            if (event.getAction() == MotionEvent.ACTION_UP){
                //提前设置好被点击的span在字串表 {stringArrayList} 里的对应下标
                clickedY = line;
                clickedX = getIndexXFromOffset(x, line);
            }else {
                //避免action down事件中触发on draw出现高亮
                clearClickedXY();
            }

        }
        return clickableSpan;
    }

    /**
     * 根据textview自身换行策略将文本内容按照每行拆分为多个字符数组
     *
     * @return 每一行字符数组组成的list
     */
    private List<String[]> divideOriginalTextToStringLineList (){
        List<String[]> stringArrayList=new ArrayList<>();
        String line;

        Layout layout = getLayout();
        String text = getText().toString();
        int start=0;
        int end;
        for (int i = 0; i < getLineCount(); i++) {
            end = layout.getLineEnd(i);
            line = text.substring(start,end);
            stringArrayList.add(line.split(" "));
            start = end;
        }
        return stringArrayList;
    }

    /**
     * 计算出每行文本两边对齐需要填充的空隙长度，将长度存于数组中，每行对应一个数组
     *
     * @param textPaint 画笔，用于计算单词的长度
     * @param textWidth　TextView的去除padding的宽度
     * @return 用于保存空隙长度的数组的list
     */
    private List<int[]> calculateDrawOffsets(TextPaint textPaint, int textWidth){

        List<int[]> list = new ArrayList<>();

        for (String[] strings : stringArrayList){


            int[] spaceArray;
            spaceArray = new int[strings.length -1];

            //如果是段落最后一行（按照'\n'进行判断）或者最后一行，不需要两边对齐
            String lastString = strings[strings.length - 1];
            if (TextUtils.equals(lastString.substring(lastString.length() - 1), "\n") ||
                    stringArrayList.indexOf(strings) == stringArrayList.size() - 1) {
                for (int i = 0; i < spaceArray.length; i++){
                    spaceArray[i] = 0;
                }
            } else {

                String line = "";
                for (String s : strings){
                    if ( Arrays.asList(strings).indexOf(s) != strings.length-1){
                        line = line + s + " ";
                    }else {
                        line = line + s;
                    }
                }

                int quotient = (textWidth - Math.round(textPaint.measureText(line))) / (strings.length - 1);
                int remainder = (textWidth - Math.round(textPaint.measureText(line))) % (strings.length - 1) ;

                for (int i = 0; i < spaceArray.length; i++){
                    if (remainder != 0){
                        spaceArray[i] = quotient + 1;
                        remainder --;
                    }else {
                        spaceArray[i] = quotient;
                    }
                }
            }

            list.add(spaceArray);

        }

        return list;
    }

    /**
     * 获取被点击单词在行中的序列号
     *
     * @param x 点击位置的横坐标
     * @param line　点击位置列号
     * @return 被点击单词在行中的序列号
     */
    private int getIndexXFromOffset(int x, int line){

        int actualX = x - getXDrawOffset(x, line);
        int indexX = 0;
        String[] strings = stringArrayList.get(line);
        TextPaint textPaint = getPaint();

        for (String string : strings) {
            if ((actualX -= (textPaint.measureText(string) + textPaint.measureText(" "))) > 0) {
                indexX++;
            } else {
                break;
            }
        }

        return indexX;
    }


    /**
     * 获取被点击位置对应单词在onDraw中被添加的偏移量之和
     *
     * @param x 点击位置的横坐标
     * @param line　点击位置列号
     * @return 偏移量之和
     */
    private int getXDrawOffset(int x, int line){
        int offset = 0;
        String[] strings = stringArrayList.get(line);
        int[] offsets = drawOffsetsList.get(line);

        for (int i = 0; i < strings.length; i++){

            if ((x -= getPaint().measureText(strings[i])) > 0){

                //offsets的数组长度比string少一个
                //若x减去所有单词加空隙长度仍然大于0则说明当前点击位置为段落最后一行的空格处
                //返回-1
                if (i == offsets.length){
                    offset = -1;
                    break;
                }

                //去除一个单词的长度之后若剩余长度小于偏移量加一个空格的长度之和则说明点到了空白
                //返回-1
                if ((x -= (getPaint().measureText(" ") + offsets[i])) < 0){
                    offset = -1;
                    break;
                }else {
                    offset += offsets[i];
                }
            }else {
                break;
            }

        }
        return offset;
    }
}
