package spd.com.shanbaytest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


import spd.com.shanbaytest.dialog.WordDetailsDialog;
import spd.com.shanbaytest.widget.CustomTextView;

public class TextActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_activity);

        final CustomTextView textView = (CustomTextView) findViewById(R.id.text);
        textView.setText(R.string.article, TextView.BufferType.SPANNABLE);

        final WordDetailsDialog detailsDialog = WordDetailsDialog
                .buildDialog(this);

        textView.setSpanClickListener(new CustomTextView.SpanClickListener() {
            @Override
            public void onclick(View textView, boolean clickSpace) {

                if (clickSpace) {
                    detailsDialog.dismiss();
                } else {
                    TextView tv = (TextView) textView;
                    String s = tv
                            .getText()
                            .subSequence(tv.getSelectionStart(),
                                    tv.getSelectionEnd()).toString();
                    detailsDialog.loadWordDetails(s);
                }
            }
        });

    }


}
