package spd.com.myapplication;

import android.text.TextPaint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linus on 17-9-27.
 */

public class JustifyTextUtils {

    /***
     * this method get the string and divide it to a list of StringLines according to textAreaWidth
     * @param textPaint
     * @param originalText
     * @param textAreaWidth
     * @return
     */
    public List<String> divideOriginalTextToStringLineList(TextPaint textPaint, String originalText, int textAreaWidth) {

        List<String> listStringLine=new ArrayList<String>();

        String line="";
        float textWidth;

        String[] listParageraphes = originalText.split("\n");

        for(int j=0;j<listParageraphes.length;j++)
        {
            String[] arrayWords = listParageraphes[j].split(" ");

            for (int i=0;i<arrayWords.length;i++){

                line += arrayWords[i]+" ";
                textWidth = textPaint.measureText(line);

                //if text width is equal to textAreaWidth then just add it to ListStringLine
                if (textAreaWidth==textWidth){

                    listStringLine.add(line);
                    line="";//make line clear
                    continue;
                }
                //else if text width excite textAreaWidth then remove last word and justify the StringLine
                else if (textAreaWidth<textWidth){

                    int lastWordCount=arrayWords[i].length();

                    //remove last word that cause line width to excite textAreaWidth
                    line=line.substring(0, line.length()-lastWordCount-1);

                    // if line is empty then should be skipped
                    if (line.trim().length()==0)
                        continue;

                    //and then we need to justify line
                    line=justifyTextLine(textPaint,line.trim(),textAreaWidth);

                    listStringLine.add(line);
                    line="";
                    i--;
                    continue;
                }

                //if we are now at last line of paragraph then just add it
                if (i==arrayWords.length-1){
                    listStringLine.add(line);
                    line="";
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

        while (lineWidth<textAreaWidth && lineWidth>0){

            gapIndex=lineString.indexOf(" ", gapIndex+2);
            if (gapIndex==-1){
                gapIndex=0;
                gapIndex=lineString.indexOf(" ", gapIndex+1);
                if (gapIndex==-1)
                    return lineString;
            }

            lineString=lineString.substring(0, gapIndex)+ "  " +lineString.substring(gapIndex+1, lineString.length());

            lineWidth=textPaint.measureText(lineString);
        }
        return lineString;
    }
}
