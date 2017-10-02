package spd.com.shanbaytest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import spd.com.myapplication.R;
import spd.com.shanbaytest.Dialog.WordDetailsDialog;
import spd.com.shanbaytest.widget.CustomTextView;

public class TextActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_activity);

        final String text = "KIEV — The European Union warned Ukraine on Thursday time was running out to " +
                "revive shelved deals on free trade and political association by meeting the bloc's " +
                "concerns over the jailing of opposition leader Yulia Tymoshenko and bringing in " +
                "reforms.\n" +
                "A senior EU official also made it clear the agreements would fall through if Ukrainejoined the Russia-led post-Soviet Customs Union trade bloc. \"We have a window of " +
                "opportunity. But time is short,'' Stefan Fuele, the European Commissioner for " +
                "Enlargement and European Neighbourhood Policy, said on a visit to Ukraine.\n" +
                "Brussels put off signing the landmark agreements after a Ukrainian court jailed former " +
                "prime minister Tymoshenko, President Viktor Yanukovich's main opponent, on an abuse-" +
                "of-office charge in October 2011.\n" +
                "The EU says the Tymoshenko case and those of other prosecuted opposition politicians " +
                "are examples of selective justice and are a barrier to Ukraine's ambition of European " +
                "integration.\n" +
                "Two other issues raised by the bloc are related to the electoral system, which came " +
                "under fire from Western observers following the parliamentary election in October, and " +
                "legal reforms needed to bring Ukraine closer to EU standards.\n" +
                "\"The European Union is committed to signing the association agreement...provided " +
                "there is determined action and tangible progress on the three key issues: selective " +
                "justice, addressing the shortcomings of the October election and advancing the " +
                "association agenda reforms,'' Fuele told reporters. \"After several recent setbacks in " +
                "Ukraine there is a need to regain confidence that Ukraine could emerge as a modern " +
                "European country.''\n" +
                "Fuele, whose visit may set the tone of a Feb. 25 EU-Ukraine summit, said the two " +
                "agreements could be signed at the EU's Eastern Partnership summit in November if the " +
                "former Soviet republic met the bloc's conditions.\n" +
                "But he warned the Kiev government that joining a customs union with Russia, " +
                "aggressively promoted by Moscow, would ruin those prospects.\n" +
                "\"Joining any structure which would imply transferring the ability to set tariffs and define " +
                "its trade policy to a supranational body would mean that Ukraine would no longer be " +
                "able to implement the tariff dismantling agreed with the European Union in the context " +
                "of the DCFTA [Deep and Comprehensive Free Trade Agreement],'' Fuele said in a speech " +
                "at the Ukrainian parliament.\n" +
                "\"It would also not be able anymore to regulate areas such as food standards, or " +
                "technical product standards, all of them important in the framework of the DCFTA. It will " +
                "not be able to integrate economically with the European Union,\" he continued.\n" +
                "Ukrainian officials say they are committed to European integration. But they say they are " +
                "also looking for a way to cooperate with the Customs Union because both blocs are " +
                "Ukraine's major trade partners.Fuele urged Ukraine to make sure it adopts and implements laws that actually work and " +
                "adhere to European standards, citing as an example the law on state procurement - " +
                "purchases of goods and services by the government.\n" +
                "The EU suspended some of its Ukraine financial aid programs after Kiev adopted a law " +
                "on state procurement which Brussels said was riddled with loopholes and thus failed to " +
                "ensure transparent and competitive procedures.";

        final CustomTextView textView = (CustomTextView) findViewById(R.id.text);
        Logger.w("text size = " + textView.getPaint().getTextSize());
        textView.setText(text, TextView.BufferType.SPANNABLE);

        final WordDetailsDialog detailsDialog = WordDetailsDialog
                .buildDialog(this.getApplicationContext());

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
