package spd.com.shanbaytest.dialog;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.NoConnectionError;
import com.orhanobut.logger.Logger;

import java.io.IOException;

import spd.com.shanbaytest.R;
import spd.com.shanbaytest.models.bean.WordDetails;
import spd.com.shanbaytest.models.WordDetailsModel;

/**
 * Created by joe on 17-10-2.
 */

public class WordDetailsDialog {
    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";

    private Context context;
    private View rootView;
    private RelativeLayout mainDetailsContainer;
    private ProgressBar progressBar;
    private TextView pronunciation;
    private TextView content;
    private TextView msg;
    private TextView definition;
    private View touchView;
    private BroadcastReceiver broadcastReceiver;
    private Boolean receiverRegistered = false;
    private ViewGroup contentView;

    private int firstPointY;
    private int currentPointY;
    private float rawX, rawY, startX, startY;
    private int ABS;
    private int statusBarHeight;
    private int actionBarHeight;
    private boolean moveFlag;

    public static WordDetailsDialog buildDialog(Context context){

        View rootView = LayoutInflater.from(context).inflate(R.layout.word_details_dialog, null, false);
        return new WordDetailsDialog(context, rootView);
    }

    private WordDetailsDialog(Context context, final View rootView){
        this.context = context;
        this.rootView = rootView;

        ABS = ViewConfiguration.get(context).getScaledTouchSlop();
        statusBarHeight = (int) Math.ceil((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 24 :
                25) * context.getResources().getDisplayMetrics().density);

        //初始化mainDetailsContainer内部相关元素
        content = (TextView) rootView.findViewById(R.id.content);
        msg = (TextView) rootView.findViewById(R.id.msg);
        pronunciation = (TextView) rootView.findViewById(R.id.pronunciation);
        definition = (TextView) rootView.findViewById(R.id.definition);
        progressBar = (ProgressBar) rootView.findViewById(R.id.prograss_bar);
        touchView = rootView.findViewById(R.id.touch_space);
        mainDetailsContainer = (RelativeLayout) rootView.findViewById(R.id.main_details_container);

        //获取activity对应的contentView,以便将detailsDialog add上去

        contentView = (ViewGroup) ((Activity)context).findViewById(android.R.id.content);
        if (context instanceof AppCompatActivity){
            android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) context)
                    .getSupportActionBar();
            if (actionBar != null){
                if(actionBar.isShowing()){
                    TypedValue tv = new TypedValue();
                    if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
                    {
                        actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context
                                .getResources().getDisplayMetrics());
                    }
                }
            }
        }else{
            ActionBar actionBar = ((Activity)context).getActionBar();
            if (actionBar != null){
                if(actionBar.isShowing()){
                    TypedValue tv = new TypedValue();
                    if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
                    {
                        actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context
                                .getResources().getDisplayMetrics());
                    }
                }
            }
        }


        touchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidOut();
            }
        });
        addTouchGestureListener();

        //接收home事件，removeView
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                    String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                    if (reason != null) {
                        if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY) || reason.equals
                                (SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                            removeView();
                        }
                    }
                }
            }
        };
    }

    public void loadWordDetails(final String word){
        progressBar.setVisibility(View.VISIBLE);
        WordDetailsModel.getInstance().getWordDetails(context, word, new WordDetailsModel
                .GetDetailsListener() {

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
            receiverRegistered = true;
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

        if (receiverRegistered){
            context.unregisterReceiver(broadcastReceiver);
            receiverRegistered = false;
        }
        
        if (rootView.getParent() != null){
            contentView.removeView(rootView);
        }
    }

    /*从屏幕下方滑入
     */
    private void slidIn(){
        addView();

        //刚刚更新ui，由于无法保证view height可靠性所以自行测量，并使用measuredHeight作为依据启动动画
        mainDetailsContainer.measure(View.MeasureSpec.makeMeasureSpec((1 << 30 - 1), View.MeasureSpec
                .AT_MOST), View.MeasureSpec.makeMeasureSpec((1 << 30 - 1), View.MeasureSpec
                .AT_MOST));

        Logger.w("getMeasuredHeight = " + mainDetailsContainer.getMeasuredHeight());

        firstPointY = contentView.getHeight() - mainDetailsContainer
                .getMeasuredHeight();

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(rootView, "translationY",
                contentView.getHeight(), contentView.getHeight() - mainDetailsContainer
                        .getMeasuredHeight());
        objectAnimator.setDuration(300);
        objectAnimator.start();
        currentPointY = firstPointY;
    }

    /*滑动到positionY对应位置，此处采用contentView对应坐标系
     */
    private void slidTo(int positionY){

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(rootView, "translationY", rootView
                .getY(), positionY);
        objectAnimator.setDuration(300);
        objectAnimator.start();
        currentPointY = positionY;
    }

    public void dismiss(){
        slidOut();
    }

    /*滑出屏幕下方
     */
    private void slidOut(){

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(rootView, "translationY", rootView
                .getY(), contentView.getHeight());
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
        currentPointY = contentView.getHeight();
    }

    //为touch设置相应的touchListener
    private void addTouchGestureListener(){

        touchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                rawX = event.getRawX();
                rawY = event.getRawY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = rawX;
                        startY = rawY;
                        moveFlag = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        
                        //rawY和contentView中的元素处于两套不同的坐标系
                        //进行比较的时候rawY要减去statusBar和actionBar的长度以及touchView本身带来的误差
                        if (Math.abs(rawY - startY) > ABS || Math.abs(rawX - startX) > ABS) {
                            moveFlag = true;
                        }
                        if (moveFlag && rawY - touchView.getHeight() / 2 - statusBarHeight -
                                actionBarHeight > context.getResources().getDimensionPixelSize(R
                                .dimen.top_stop_margin)) {
                            rootView.setY(rawY - touchView.getHeight() / 2 - statusBarHeight -
                                    actionBarHeight);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!moveFlag) {
                            slidOut();
                        } else {
                            
                            moveFlag = false;
                            //当位于屏幕上部的时候(view初始停留位置48dp以上)将activity滑到顶部
                            if (rawY - touchView.getHeight() / 2 - statusBarHeight -
                                    actionBarHeight < firstPointY - context.getResources()
                                    .getDimensionPixelSize(R.dimen.top_extend_length)) {
                                if (currentPointY == firstPointY){
                                    slidTo(context.getResources().getDimensionPixelSize(R.dimen
                                                    .top_stop_margin));
                                }else {
                                    slidOut();
                                }
                                //位于屏幕下部的时候(view初始停留位置以下)，滑出屏幕
                            } else if(rawY - touchView.getHeight() / 2 - statusBarHeight -
                                    actionBarHeight  > firstPointY){
                                slidOut();
                            } else {
                                //位于屏幕中部的时候(view初始停留位置上方48dp以内)，滑回初始位置
                                slidTo(firstPointY);
                            }
                        }
                        return true;
                }
                return false;
            }
        });
    }
    
}
