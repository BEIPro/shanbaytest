package spd.com.shanbaytest.utils;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Streaming;
import rx.Observable;

/**
 * Created by linus on 17-10-2.
 */

public class RetrofitServiceUtils {

    public interface ServiceApi{

        @Streaming
        @GET("{id}")
        Observable<ResponseBody> downloadPicFromNet(@Path("id") String id);
    }

    /**
     * 创建retrofit服务，添加rxJava适配器
     */
    public static ServiceApi getService(String baseUrl){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                //添加Rxjava
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit.create(ServiceApi.class);
    }
}
