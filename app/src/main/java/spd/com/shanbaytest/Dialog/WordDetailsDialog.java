package spd.com.shanbaytest.Dialog;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.NoConnectionError;
import com.orhanobut.logger.Logger;

import java.io.IOException;

import spd.com.shanbaytest.R;
import spd.com.shanbaytest.models.Pojo.WordDetails;
import spd.com.shanbaytest.models.WordDetailsModel;

/**
 * Created by linus on 17-10-2.
 */

public class WordDetailsDialog {
    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

    private Context context;
    private View rootView;
    private WindowManager windowManager;
    private ProgressBar progressBar;
    private TextView pronunciation;
    private TextView content;
    private TextView msg;
    private TextView definition;
    private View touchView;
    private BroadcastReceiver broadcastReceiver;
    private Boolean receiverRegistered = false;
    private ViewGroup contentView;

    public static WordDetailsDialog buildDialog(Context context){

        View rootView = LayoutInflater.from(context).inflate(R.layout.word_details_dialog, null, false);

        return new WordDetailsDialog(context, rootView);
    }

    private WordDetailsDialog(Context context, final View rootView) {
        this.context = context;
        this.rootView = rootView;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        content = (TextView) rootView.findViewById(R.id.content);
        msg = (TextView) rootView.findViewById(R.id.msg);
        pronunciation = (TextView) rootView.findViewById(R.id.pronunciation);
        definition = (TextView) rootView.findViewById(R.id.definition);
        progressBar = (ProgressBar) rootView.findViewById(R.id.prograss_bar);
        touchView = rootView.findViewById(R.id.touch_space);
        contentView = (ViewGroup) ((Activity)context).findViewById(android.R.id.content);

        touchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidOut();
            }
        });

        //接收home事件，removeView
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                    String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                    if (reason != null) {
                        if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                            removeView();
                        }
                    }
                }
            }
        };
    }

    public void loadWordDetails(final String word){
        progressBar.setVisibility(View.VISIBLE);
        WordDetailsModel.getInstance().getWordDetails(context, word, new WordDetailsModel.GetDetailsListener() {

            @Override
            public void success(WordDetails wordDetails) {
                updateDetails(wordDetails);
                slidIn();
            }

            @Override
            public void fail(Object fail) {
                Logger.w(fail.toString());
                updateDetails(fail);
                slidIn();
            }
        });
    }

    /**
     * word查询结果返回更新dialog
     */
    private void updateDetails(Object o){

        if (o instanceof WordDetails){
            final WordDetails wordDetails = (WordDetails) o;
            progressBar.setVisibility(View.INVISIBLE);
            if (wordDetails.getStatus_code() == 1){
                msg.setVisibility(View.VISIBLE);
                msg.setText(wordDetails.getMsg());
                pronunciation.setVisibility(View.INVISIBLE);
            }else {
                msg.setVisibility(View.INVISIBLE);
                pronunciation.setVisibility(View.VISIBLE);
                pronunciation.setText(wordDetails.getData().getPronunciation());
            }

            content.setText(wordDetails.getData().getContent());

            definition.setText(wordDetails.getData().getDefinition());

            pronunciation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(wordDetails.getData().getAudio());
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }else if (o instanceof NoConnectionError){
            progressBar.setVisibility(View.INVISIBLE);
            pronunciation.setVisibility(View.INVISIBLE);
            msg.setVisibility(View.VISIBLE);
            msg.setText(R.string.no_connection);
        }
    }

    private void addView(){

        if (rootView.getParent() == null){
            contentView.addView(rootView);
        }

        //检查home时间广播接收器有没有解注册
        if (!receiverRegistered){
            context.registerReceiver(broadcastReceiver, new IntentFilter(Intent
                    .ACTION_CLOSE_SYSTEM_DIALOGS));
        }

        rootView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK){
                    slidOut();
                    return true;
                }
                return false;
            }
        });

        rootView.setFocusableInTouchMode(true);
        rootView.requestFocus();

    }

    /*移除dialog
     */
    private void removeView(){
        if (rootView.getWindowToken() != null){
            contentView.removeView(rootView);
        }
    }

    private void slidIn(){
        addView();

        rootView.measure(View.MeasureSpec.makeMeasureSpec((1 << 30 - 1), View.MeasureSpec
                .AT_MOST), View.MeasureSpec.makeMeasureSpec((1 << 30 - 1), View.MeasureSpec
                .AT_MOST));

        Logger.w("getMeasuredHeight = " + rootView.getMeasuredHeight());

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(rootView, "translationY",
                contentView.getHeight(), contentView.getHeight() - rootView.getMeasuredHeight());
        objectAnimator.setDuration(300);
        objectAnimator.start();
    }

    public void dismiss(){
        slidOut();
    }

    private void slidOut(){

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(rootView, "translationY", rootView
                .getY(), rootView.getHeight());
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                removeView();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        objectAnimator.setDuration(300);
        objectAnimator.start();
    }
}
