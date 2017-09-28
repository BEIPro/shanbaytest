package spd.com.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linus on 17-9-27.
 */

public class CustomTextView extends android.support.v7.widget.AppCompatTextView {
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
    }

    private List<String> lineList=new ArrayList<String>();

    @Override
    public void setText(CharSequence text, BufferType type) {


        Log.w("ok", "setText " + type + text);

        if (type == BufferType.SPANNABLE){

            lineList = divideOriginalTextToStringLineList(getPaint(), text.toString(), 720);

            String finalString = "";
            for (String s : lineList){
                finalString = finalString + s + " ";
            }

            super.setText(finalString, type);

            getEachWord(this);
            setMovementMethod(new LinkTouchMovementMethod());
        }

    }

    public  void clearHighlight(){
        ((LinkTouchMovementMethod)getMovementMethod()).clearOldSpan();
    }


    private void getEachWord(TextView textView){
        Spannable spans = (Spannable)textView.getText();
        Integer[] indices = getIndices(
                textView.getText().toString().trim(), ' ');
        int start = 0;
        int end = 0;
        // to cater last/only word loop will run equal to the length of indices.length
        for (int i = 0; i <= indices.length; i++) {
            ClickableSpan clickSpan = new TouchableSpan(Color.BLACK, Color.WHITE, Color.GREEN);
            // to cater last/only word
            end = (i < indices.length ? indices[i] : spans.length());
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

    /*private int rowIndex=0,colIndex=0;
    @Override
    protected void onDraw(Canvas canvas) {

        Log.w("ok", "on draw");
        rowIndex=getPaddingTop();
        colIndex=getPaddingLeft();

        for (int i=0;i<lineList.size();i++){
            rowIndex+=getLineHeight();

            canvas.drawText(lineList.get(i), colIndex,rowIndex , getPaint());
        }

    }
*/

    private class LinkTouchMovementMethod extends LinkMovementMethod {
        private TouchableSpan mPressedSpan;
        private TouchableSpan mOldPressedSpan;

        @Override
        public boolean onTouchEvent(TextView textView, Spannable spannable, MotionEvent event) {

            Log.w("ok", "touch event = " + event.getAction());

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mPressedSpan = getPressedSpan(textView, spannable, event);
                mPressedSpan.setPressed(false);

            }
            else if (event.getAction() == MotionEvent.ACTION_UP) {
                TouchableSpan touchedSpan = getPressedSpan(textView, spannable, event);
                if (mPressedSpan != null && touchedSpan == mPressedSpan) {
                    if (mOldPressedSpan != null){
                        mOldPressedSpan.setPressed(false);
                    }
                    mPressedSpan.setPressed(true);
                    Selection.setSelection(spannable, spannable.getSpanStart(mPressedSpan),
                            spannable.getSpanEnd(mPressedSpan));
                    mOldPressedSpan = mPressedSpan;
                }
                super.onTouchEvent(textView, spannable, event);
            }else if (event.getAction() == MotionEvent.ACTION_CANCEL){
                super.onTouchEvent(textView, spannable, event);
            }

            return true;
        }

        private TouchableSpan getPressedSpan(TextView textView, Spannable spannable, MotionEvent event) {

            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= textView.getTotalPaddingLeft();
            y -= textView.getTotalPaddingTop();

            x += textView.getScrollX();
            y += textView.getScrollY();

            Layout layout = textView.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            TouchableSpan[] link = spannable.getSpans(off, off, TouchableSpan.class);
            TouchableSpan touchedSpan = null;
            if (link.length > 0) {
                touchedSpan = link[0];
            }
            return touchedSpan;
        }

        void clearOldSpan() {
            if (mOldPressedSpan != null){
                mOldPressedSpan.setPressed(false);
            }
            postInvalidate();
        }
    }

    private class TouchableSpan extends ClickableSpan {
        private boolean mIsPressed;
        private int mPressedBackgroundColor;
        private int mNormalTextColor;
        private int mPressedTextColor;

        TouchableSpan(int normalTextColor, int pressedTextColor, int pressedBackgroundColor) {
            mNormalTextColor = normalTextColor;
            mPressedTextColor = pressedTextColor;
            mPressedBackgroundColor = pressedBackgroundColor;
        }

        void setPressed(boolean isSelected) {
            mIsPressed = isSelected;
        }

        @Override
        public void onClick(View widget) {
            TextView tv = (TextView) widget;
            String s = tv
                    .getText()
                    .subSequence(tv.getSelectionStart(),
                            tv.getSelectionEnd()).toString();
            Toast.makeText(widget.getContext(), s, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            Log.w("ok", "update pressed = " + mIsPressed);
            ds.setColor(mIsPressed ? mPressedTextColor : mNormalTextColor);
            ds.bgColor = mIsPressed ? mPressedBackgroundColor : Color.WHITE;
            ds.setUnderlineText(false);
        }
    }


    /***
     * this method get the string and divide it to a list of StringLines according to textAreaWidth
     * @param textPaint
     * @param originalText
     * @param textAreaWidth
     * @return
     */
    private List<String> divideOriginalTextToStringLineList(TextPaint textPaint, String
            originalText, int textAreaWidth) {

        Log.w("ok", "area width = " + textAreaWidth);
        List<String> listStringLine=new ArrayList<String>();

        String line="";
        float textWidth;

        String[] listParageraphes = originalText.split("\n");

        for (String listParageraphe : listParageraphes) {
            String[] arrayWords = listParageraphe.split(" ");

            for (int i = 0; i < arrayWords.length; i++) {

                line += arrayWords[i] + " ";
                textWidth = textPaint.measureText(line);

                //if text width is equal to textAreaWidth then just add it to ListStringLine
                if (textAreaWidth == textWidth) {

                    listStringLine.add(line);
                    line = "";//make line clear
                    continue;
                }
                //else if text width excite textAreaWidth then remove last word and justify the
                // StringLine
                else if (textAreaWidth < textWidth) {

                    int lastWordCount = arrayWords[i].length();

                    //remove last word that cause line width to excite textAreaWidth
                    line = line.substring(0, line.length() - lastWordCount - 1);

                    // if line is empty then should be skipped
                    if (line.trim().length() == 0)
                        continue;

                    //and then we need to justify line
                    line = justifyTextLine(textPaint, line.trim(), textAreaWidth);

                    listStringLine.add(line);
                    line = "";
                    i--;
                    continue;
                }

                //if we are now at last line of paragraph then just add it
                if (i == arrayWords.length - 1) {
                    listStringLine.add(line);
                    line = "";
                }
            }
        }

        return listStringLine;

    }


    /**
     * this method add space in line until line width become equal to textAreaWidth
     * @param textPaint
     * @param lineString
     * @param textAreaWidth
     * @return
     */
    private String justifyTextLine(TextPaint textPaint, String lineString, int textAreaWidth) {

        int gapIndex=0;

        float lineWidth=textPaint.measureText(lineString);
        int i = 0;

        while (lineWidth < (textAreaWidth - textPaint.measureText(" ") + 1) && lineWidth>0){

            gapIndex=lineString.indexOf(" ", gapIndex + 2 + i);
            if (gapIndex==-1){
                gapIndex=0;
                i++;
                gapIndex=lineString.indexOf(" ", gapIndex + 1);
                if (gapIndex==-1)
                    return lineString;
            }

            lineString = lineString.substring(0, gapIndex)+ "  " +lineString.substring(gapIndex + 1, lineString.length());

            lineWidth=textPaint.measureText(lineString);
        }
        return lineString;
    }
}
