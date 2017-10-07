package spd.com.shanbaytest.models;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.HttpException;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import spd.com.shanbaytest.R;
import spd.com.shanbaytest.utils.FileUtils;
import spd.com.shanbaytest.models.bean.ImageDetails;
import spd.com.shanbaytest.utils.RetrofitServiceUtils;

/**
 * Created by joe on 17-10-1.
 */

public class ImageLoader {

    private List<String> notTryList = new ArrayList<>();
    private ImageLoader() {
    }

    public static ImageLoader newInstance() {
        return new ImageLoader();
    }

    /**
     * 通过glide先试图本地图片加载
     * 本地没有则通过网络下载
     */
    public void loadImageInto(final ImageDetails imageDetails, final ImageView imageView) {
        DisplayMetrics dm = imageView.getContext().getResources().getDisplayMetrics();
        int imgLoadWidth = dm.widthPixels/2;
        int imgLoadHeight = dm.heightPixels*2/3;

        Glide.with(imageView.getContext()).load(imageDetails.getLocalPath()).override(imgLoadWidth, imgLoadHeight)
                .listener(new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target,
                                       boolean isFirstResource) {
                loadImageFromNetworkInto(imageDetails, imageView);
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, String model,
                                           Target<GlideDrawable> target, boolean
                                                   isFromMemoryCache, boolean isFirstResource) {
                return false;
            }
    }).into(imageView);

    }

    /**
     * 通过RxJava配合retrofit进行下载以及重试
     * 通过glide进行图片压缩显示
     *
     * @param imageDetails 图片相关信息，包含本地存储路径和网络获取url
     * @param imageView　用于更新的imageView
     */
    private void loadImageFromNetworkInto(final ImageDetails imageDetails, final ImageView imageView) {
        DisplayMetrics dm = imageView.getContext().getResources().getDisplayMetrics();
        final int imgLoadWidth = dm.widthPixels/2;
        final int imgLoadHeight = dm.heightPixels*2/3;

        String[] strings = imageDetails.getUrl().split("/");
        final String fileName = strings[strings.length - 1];
        Logger.w("filename = " + fileName);
        RetrofitServiceUtils.getService("https://static.baydn.com/media/media_store/image/")
                .downloadPicFromNet(fileName).subscribeOn(Schedulers.newThread())
                //重试3次，时间为1000ms,2000ms,3000ms
                //重试3次失败之后不再重试
                .retryWhen(notTryList.contains(imageDetails.getUrl())?
                        new RetryWithDelay(0, 0) : new RetryWithDelay(3, 1000))
                .map(new Func1<ResponseBody, Boolean>() {
                    @Override
                    public Boolean call(ResponseBody responseBody) {
                        Logger.w("download response");
                        //从网络获取资源则成功写入sd卡
                        Boolean writeSuccess = FileUtils.writeResponseBodyToDisk(imageView.getContext(),
                                responseBody, imageDetails.getUrl());
                        if (writeSuccess) {
                            imageDetails.setLocalPath(FileUtils.getImgLocalPath(imageDetails
                                    .getUrl(), imageView.getContext()));
                        }
                        return writeSuccess;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                        Logger.w("download response onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof HttpException){
                            //三次重试都失败，则本生命周期内不再尝试加载
                            if (((HttpException) e).response().code() == 404) {
                                if (!notTryList.contains(imageDetails.getUrl())){
                                    notTryList.add(imageDetails.getUrl());
                                }

                            }
                        }

                        Logger.w("download response onError");
                        Glide.with(imageView.getContext()).load(R.mipmap
                                .f82e4bbb12978195797a3447d80f50ff).override(imgLoadWidth, imgLoadHeight).into
                                (imageView);
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Boolean writeSuccess) {
                        Logger.w("writeSuccess = " + writeSuccess);
                        Glide.with(imageView.getContext()).load(imageDetails.getLocalPath())
                                .override(imgLoadWidth, imgLoadHeight).error(R.mipmap
                                .f82e4bbb12978195797a3447d80f50ff).listener(new RequestListener<String, GlideDrawable>() {

                            @Override
                            public boolean onException(Exception e, String model,
                                                       Target<GlideDrawable> target, boolean
                                                               isFirstResource) {
                                e.printStackTrace();
                                Logger.w("glide onException");
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model,
                                                           Target<GlideDrawable> target, boolean
                                                                   isFromMemoryCache, boolean
                                                                   isFirstResource) {
                                Logger.w("glide onResourceReady");
                                return false;
                            }
                        }).into(imageView);
                    }
                });
    }

    /**
     * RxJava重试
     * 构造的时候传入重试次数
     * 每次重试时间随次数同比例增长　　
     */
    private class RetryWithDelay implements
            Func1<Observable<? extends Throwable>, Observable<?>> {

        private final int maxRetries;
        private final int retryDelayMillis;
        private int retryCount;

        RetryWithDelay(int maxRetries, int retryDelayMillis) {
            this.maxRetries = maxRetries;
            this.retryDelayMillis = retryDelayMillis;
        }

        @Override
        public Observable<?> call(Observable<? extends Throwable> attempts) {
            return attempts
                    .flatMap(new Func1<Throwable, Observable<?>>() {
                        @Override
                        public Observable<?> call(Throwable throwable) {
                            if (++retryCount <= maxRetries) {

                                Logger.w("get error, it will try after " + retryDelayMillis * retryCount
                                        + " millisecond, retry count " + retryCount);
                                return Observable.timer(retryDelayMillis * retryCount,
                                        TimeUnit.MILLISECONDS);
                            }

                            return Observable.error(throwable);
                        }
                    });
        }
    }

}
