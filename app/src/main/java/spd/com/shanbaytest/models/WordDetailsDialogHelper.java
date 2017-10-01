package spd.com.shanbaytest.models;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import spd.com.myapplication.R;
import spd.com.shanbaytest.models.Pojo.WordDetails;
import spd.com.shanbaytest.models.WordDetailsModel;

/**
 * Created by linus on 17-10-1.
 */

public class WordDetailsDialogHelper {

    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

    public static DetailsDialog buildDialog(Context context){

        View rootView = LayoutInflater.from(context).inflate(R.layout.word_details_dialog, null, false);

        return new DetailsDialog(context, rootView);
    }

    public static class DetailsDialog{
        private Context context;
        private View rootView;
        private WindowManager windowManager;
        private ProgressBar progressBar;
        private TextView pronunciation;
        private TextView content;
        private TextView msg;
        private TextView definition;
        private View touchView;
        private WindowManager.LayoutParams params;
        private BroadcastReceiver broadcastReceiver;
        private Boolean receiverRegistered = false;

        DetailsDialog(Context context, final View rootView) {
            this.context = context;
            this.rootView = rootView;


            windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            content = (TextView) rootView.findViewById(R.id.content);
            msg = (TextView) rootView.findViewById(R.id.msg);
            pronunciation = (TextView) rootView.findViewById(R.id.pronunciation);
            definition = (TextView) rootView.findViewById(R.id.definition);
            progressBar = (ProgressBar) rootView.findViewById(R.id.prograss_bar);
            touchView = rootView.findViewById(R.id.touch_space);

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
                    slidOut();
                }
            });
        }

        /**
         * word查询结果返回更新dialog
         */
        void updateDetails(WordDetails wordDetails){

            progressBar.setVisibility(View.INVISIBLE);
            if (wordDetails.getStatus_code() == 1){
                msg.setVisibility(View.VISIBLE);
                msg.setText(wordDetails.getMsg());
            }else {
                msg.setVisibility(View.INVISIBLE);
            }

            content.setText(wordDetails.getData().getContent());
            pronunciation.setText(wordDetails.getData().getPronunciation());
            definition.setText(wordDetails.getData().getDefinition());

        }


        private void addView(){

            //检查home时间广播接收器有没有解注册
            if (!receiverRegistered){
                context.registerReceiver(broadcastReceiver, new IntentFilter(Intent
                        .ACTION_CLOSE_SYSTEM_DIALOGS));
            }

            int flag = WindowManager.LayoutParams.TYPE_TOAST;
            if (Build.VERSION.SDK_INT < 19) {
                flag = WindowManager.LayoutParams.TYPE_PHONE;
            }
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    flag,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.BOTTOM ;

            if (rootView.getWindowToken() != null){
                return;
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
            rootView.setVisibility(View.VISIBLE);
            rootView.setFocusableInTouchMode(true);
            windowManager.addView(rootView, params);
            rootView.requestFocus();

        }

        /*　移除dialog
         */
        void removeView(){
            if (rootView.getWindowToken() != null){
                //先设置invisible避免闪烁
                rootView.setVisibility(View.INVISIBLE);
                windowManager.removeView(rootView);

                if (receiverRegistered){
                    context.unregisterReceiver(broadcastReceiver);
                    receiverRegistered = false;
                }
            }
        }

        void show(){
            addView();
        }

        public void slidIn(){
            addView();
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(rootView, "translationY", rootView.getHeight(), 0);
            objectAnimator.setDuration(300);
            objectAnimator.start();
        }

        public void slidOut(){

            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(rootView, "translationY", 0, rootView.getHeight());
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



}
