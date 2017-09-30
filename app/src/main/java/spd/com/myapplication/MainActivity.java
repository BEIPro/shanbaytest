package spd.com.myapplication;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String text = "KIEV — The European Union warned Ukraine on Thursday time was running out " +
                "to" +
                "revive shelved deals on free trade and political association by meeting the " +
                "bloc's" +
                "concerns over the jailing of opposition leader Yulia Tymoshenko and bringing " +
                "in" +
                "reforms.\n " +
                "A senior EU official also made it clear that agreements would fall through if " +
                "Ukrainejoined the Russia-led post-Soviet Customs Union trade bloc. We have a " +
                "window of" +
                "opportunity. But time is short,'' Stefan Fuele, the European Commissioner for " +
                "Enlargement and European Neighbourhood Policy, said on a visit to Ukraine." +
                "Brussels put off signing the landmark agreements after a Ukrainian court jailed " +
                "former" +
                "prime minister Tymoshenko, President Viktor Yanukovich's main opponent, on an " +
                "abuse-" +
                "of-office charge in October 2011.";

        final CustomTextView textView = (CustomTextView) findViewById(R.id.text);
        Log.w("ok", "text size = " + textView.getPaint().getTextSize());
        textView.setText(text, TextView.BufferType.SPANNABLE);

        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.clearHighlight();
            }
        });

    }



}
