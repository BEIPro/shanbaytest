package spd.com.shanbaytest.models;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Streaming;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import spd.com.myapplication.R;
import spd.com.shanbaytest.Utils.FileUtils;
import spd.com.shanbaytest.models.Pojo.ImageDetails;

/**
 * Created by linus on 17-10-1.
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
    public void loadImage(final ImageDetails imageDetails, final ImageView imageView) {

        Glide.with(imageView.getContext()).load(imageDetails.getLocalPath()).override(400, 720)
                .listener(new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target,
                                       boolean isFirstResource) {
                loadImageFromNetwork(imageDetails, imageView);
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
    private void loadImageFromNetwork(final ImageDetails imageDetails, final ImageView imageView) {
        String[] strings = imageDetails.getUrl().split("/");
        final String fileName = strings[strings.length - 1];
        Log.d("ok", "filename = " + fileName);
        getService().downloadPicFromNet(fileName).subscribeOn(Schedulers.newThread())
                //重试3次，时间为1000ms,2000ms,3000ms
                //重试3次失败之后不再重试
                .retryWhen(notTryList.contains(imageDetails.getUrl())?
                        new RetryWithDelay(0, 0) : new RetryWithDelay(3, 1000))
                .map(new Func1<ResponseBody, Boolean>() {
                    @Override
                    public Boolean call(ResponseBody responseBody) {
                        Log.d("ok", "download response");
                        Boolean writeSuccess = writeResponseBodyToDisk(imageView.getContext(),
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
                        Log.d("ok", "download response onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {

                        if (e instanceof HttpException){
                            if (((HttpException) e).response().code() == 404) {
                                if (!notTryList.contains(imageDetails.getUrl())){
                                    notTryList.add(imageDetails.getUrl());
                                }

                            }
                        }

                        Log.d("ok", "download response onError");
                        Glide.with(imageView.getContext()).load(R.mipmap
                                .f82e4bbb12978195797a3447d80f50ff).override(400, 720).into
                                (imageView);
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Boolean writeSuccess) {
                        Log.d("ok", "writeSuccess = " + writeSuccess);
                        Glide.with(imageView.getContext()).load(imageDetails.getLocalPath())
                                .override(400, 720).error(R.mipmap
                                .f82e4bbb12978195797a3447d80f50ff).listener(new RequestListener<String, GlideDrawable>() {


                            @Override
                            public boolean onException(Exception e, String model,
                                                       Target<GlideDrawable> target, boolean
                                                               isFirstResource) {
                                e.printStackTrace();
                                Log.d("ok", "glide onException");
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model,
                                                           Target<GlideDrawable> target, boolean
                                                                   isFromMemoryCache, boolean
                                                                   isFirstResource) {
                                Log.d("ok", "glide onResourceReady");
                                return false;
                            }
                        }).override(400, 720).into(imageView);
                    }
                });
    }

    interface ServiceApi{

        @Streaming
        @GET("{id}")
        Observable<ResponseBody> downloadPicFromNet(@Path("id") String id);
    }

    /**
     * 创建retrofit服务，添加rxJava适配器
     */
    private ServiceApi getService(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://static.baydn.com/media/media_store/image/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create()) //添加Rxjava
                .build();
        return retrofit.create(ServiceApi.class);
    }

    /**
     * 将retrofit获取的response写到本地sd卡
     */
    private boolean writeResponseBodyToDisk(Context context, ResponseBody body, String url) {
        try {
            String localDirString = FileUtils.getImgLocalDir(context);
            if (localDirString == null){
                return false;
            }
            File localDir = new File(localDirString);

            if (!localDir.exists()){
                localDir.mkdirs();
            }

            // todo change the file location/name according to your needs
            String localUrl = FileUtils.getImgLocalPath(url, context);
            if (localUrl == null){
                return false;
            }

            File futureStudioIconFile = new File(localUrl);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
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
                                // When this Observable calls onNext, the original Observable
                                // will be retried (i.e. re-subscribed).
                                Log.d("ok", "get error, it will try after " + retryDelayMillis * retryCount
                                        + " millisecond, retry count " + retryCount);
                                return Observable.timer(retryDelayMillis * retryCount,
                                        TimeUnit.MILLISECONDS);
                            }

                            // Max retries hit. Just pass the error along.
                            return Observable.error(throwable);
                        }
                    });
        }
    }

}
