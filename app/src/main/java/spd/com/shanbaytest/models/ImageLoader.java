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
import spd.com.shanbaytest.Utils.FIleUtils;
import spd.com.shanbaytest.models.Pojo.ImageDetails;

/**
 * Created by linus on 17-10-1.
 */

public class ImageLoader {

    private ImageLoader() {

    }

    List<String> noRetryList = new ArrayList<>();

    public static ImageLoader newInstance() {

        return new ImageLoader();
    }

    public void loadImage(final ImageDetails imageDetails, final ImageView imageView){

        Glide.with(imageView.getContext()).load(imageDetails.getLocalUrl()).override(400, 720).listener(new RequestListener<String, GlideDrawable>() {


            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target,
                                       boolean isFirstResource) {
                loadImageFromNetwork(imageDetails, imageView);
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, String model,
                                           Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                return false;
            }
        }).into(imageView);

    }

    private void loadImageFromNetwork(final ImageDetails imageDetails, final ImageView imageView){
        String[] strings = imageDetails.getUrl().split("/");
        final String fileName = strings[strings.length -1];
        Log.w("ok", "filename = " + fileName);
        getService().downloadPicFromNet(fileName).subscribeOn(Schedulers.newThread())
                .retryWhen(new RetryWithDelay(3, 1000))
                .map(new Func1<ResponseBody, Boolean>() {
                    @Override
                    public Boolean call(ResponseBody responseBody) {
                        Log.w("ok", "download response");
                        Boolean writeSuccess = writeResponseBodyToDisk(imageView.getContext(), responseBody, imageDetails.getUrl());
                        if (writeSuccess){
                            imageDetails.setLocalUrl(FIleUtils.getImgLocalPath(imageDetails.getUrl(), imageView.getContext()));
                        }
                        return writeSuccess;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                        Log.w("ok", "download response onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.w("ok", "download response onError");
                        Glide.with(imageView.getContext()).load(R.mipmap
                                .f82e4bbb12978195797a3447d80f50ff).override(400, 720).into
                                (imageView);
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Boolean writeSuccess) {
                        Log.w("ok", "download response success");
                        Log.w("ok", "writeSuccess = " + writeSuccess);
                            Glide.with(imageView.getContext()).load(imageDetails.getLocalUrl()).override(400, 720).error(R.mipmap
                                    .f82e4bbb12978195797a3447d80f50ff).listener(new RequestListener<String, GlideDrawable>() {


                                @Override
                                public boolean onException(Exception e, String model,
                                                           Target<GlideDrawable> target, boolean isFirstResource) {
                                    e.printStackTrace();
                                    Log.w("ok", "glide onException");
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    Log.w("ok", "glide onResourceReady");
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

    private ServiceApi getService(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://static.baydn.com/media/media_store/image/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create()) //添加Rxjava
                .build();
        return retrofit.create(ServiceApi.class);
    }

    private boolean writeResponseBodyToDisk(Context context, ResponseBody body, String url) {
        try {
            String localDirString = FIleUtils.getImgLocalDir(context);
            if (localDirString == null){
                return false;
            }
            File localDir = new File(localDirString);

            if (!localDir.exists()){
                localDir.mkdirs();
            }

            // todo change the file location/name according to your needs
            String localUrl = FIleUtils.getImgLocalPath(url, context);
            if (localUrl == null){
                return false;
            }

            File futureStudioIconFile = new File(localUrl);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;


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

    private class RetryWithDelay implements
            Func1<Observable<? extends Throwable>, Observable<?>> {

        private final int maxRetries;
        private final int retryDelayMillis;
        private int retryCount;

        public RetryWithDelay(int maxRetries, int retryDelayMillis) {
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
                                Log.w("ok", "get error, it will try after " + retryDelayMillis * retryCount
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
