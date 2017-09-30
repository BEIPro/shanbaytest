package spd.com.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by linus on 17-9-27.
 */

public class CustomTextView extends android.support.v7.widget.AppCompatTextView {

    private int clickedX = -1;
    private int clickedY = -1;
    private List<String[]> stringArrayList = new ArrayList<>();
    private List<int[]> drawOffsetsList = new ArrayList<>();
    private SpanClickListener spanClickListener;

    interface SpanClickListener {
        void onclick(View textView);
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
            getEachWord(this);
        }
    }

    public void clearHighlight(){
        clickedX = -1;
        clickedY = -1;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        TextPaint paint = getPaint();
        int rowIndex = getPaddingTop();
        int colIndex = getPaddingLeft();

        stringArrayList = divideOriginalTextToStringLineList();
        drawOffsetsList = calculateDrawOffsets(paint, getWidth() - getPaddingLeft() -
                getPaddingRight());

        for (int i = 0; i < stringArrayList.size(); i ++){
            rowIndex += getLineHeight();
            colIndex = getPaddingLeft();

            for (int j = 0; j < stringArrayList.get(i).length; j++){
                paint.setColor(Color.BLACK);

                if (j == 0){
                    if (i == clickedY && j == clickedX) {
                        paint.setColor(Color.GREEN);
                    }
                    canvas.drawText(stringArrayList.get(i)[j], colIndex, rowIndex, paint);
                    colIndex += paint.measureText(stringArrayList.get(i)[j]);

                }else {
                    colIndex += drawOffsetsList.get(stringArrayList.indexOf(stringArrayList.get(i)))[j - 1];
                    if (stringArrayList.indexOf(stringArrayList.get(i)) == clickedY && j == clickedX){
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


    private void getEachWord(TextView textView){
        Spannable spans = (Spannable)textView.getText();
        Integer[] indices = getIndices(
                textView.getText().toString().trim(), ' ');
        int start = 0;
        int end = 0;
        // to cater last/only word loop will run equal to the length of indices.length
        for (int i = 0; i <= indices.length; i++) {
            ClickableSpan clickSpan = new ClickableSpan(){
                @Override
                public void onClick(View widget) {
                    if (spanClickListener != null){
                        spanClickListener.onclick(widget);
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
            // to cater last/only word
            end = (i < indices.length ? indices[i] : spans.toString().trim().length());
            spans.setSpan(clickSpan, start, end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = end + 1;
        }
    }

    private Integer[] getIndices(String s, char c) {
        int pos = s.indexOf(c, 0);
        List<Integer> indices = new ArrayList<Integer>();
        while (pos != -1) {
            indices.add(pos);
            pos = s.indexOf(c, pos + 1);
        }
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
                }

                if (mPressedSpan != null && touchedSpan == mPressedSpan) {
                    Selection.setSelection(spannable, spannable.getSpanStart(mPressedSpan),
                            spannable.getSpanEnd(mPressedSpan));
                    lastSpannable = spannable;
                    mPressedSpan.onClick(textView);
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

        //x轴偏移量为-1则代表点击到了空白处 返回null
        if (getXDrawOffset(x, line) == -1){
            return null;
        }

        int off = layout.getOffsetForHorizontal(line, x - getXDrawOffset(x, line));

        ClickableSpan[] link = spannable.getSpans(off, off, ClickableSpan.class);
        ClickableSpan clickableSpan = null;
        if (link.length > 0) {
            clickableSpan = link[0];
            //提前设置好被点击的span在字串表 {stringArrayList} 里的对应下标
            clickedY = line;
            clickedX = getIndexXFromOffset(x - getXDrawOffset(x, line), line);
        }
        return clickableSpan;
    }

    private List<String[]> divideOriginalTextToStringLineList (){
        List<String[]> stringArrayList=new ArrayList<>();
        String line;

        Layout layout = getLayout();
        String text = getText().toString();
        int start=0;
        int end;
        for (int i=0; i<getLineCount(); i++) {
            end = layout.getLineEnd(i);
            line = text.substring(start,end);
            stringArrayList.add(line.split(" "));
            start = end;
        }
        return stringArrayList;
    }

    private List<int[]> calculateDrawOffsets(TextPaint textPaint, int textWidth){

        List<int[]> list = new ArrayList<>();

        for (String[] strings : stringArrayList){
            String line = "";
            for (String s : strings){
                if ( Arrays.asList(strings).indexOf(s) != strings.length-1){
                    line = line + s + " ";
                }else {
                    line = line + s;
                }
            }

            int[] spaceArray;
            if (strings.length == 1){
                spaceArray = new int[]{-1};
            }else {
                spaceArray = new int[strings.length -1];

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

    private int getIndexXFromOffset(int x, int line){

        int indexX = 0;
        String[] strings = stringArrayList.get(line);
        TextPaint textPaint = getPaint();

        for (String string : strings) {
            if ((x -= (textPaint.measureText(string) + textPaint.measureText(" "))) > 0) {
                indexX++;
            } else {
                break;
            }
        }

        return indexX;
    }

    private int getXDrawOffset(int x, int line){
        int offset = 0;
        String[] strings = stringArrayList.get(line);
        int[] offsets = drawOffsetsList.get(line);

        for (int i = 0; i < strings.length - 1; i++){
            if ((x -= getPaint().measureText(strings[i])) > 0){
                if (offsets[i] == -1 || (x -= getPaint().measureText(" ")) < 0){
                    offset = -1;
                    continue;
                }

                if (( x -= offsets[i]) > 0){
                    offset += offsets[i];
                }else {
                    offset = -1;
                }
            }else {
                break;
            }

        }
        return offset;
    }
}
