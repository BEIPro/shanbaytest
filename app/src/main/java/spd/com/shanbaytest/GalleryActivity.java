package spd.com.shanbaytest;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import spd.com.myapplication.R;
import spd.com.shanbaytest.utils.FileUtils;
import spd.com.shanbaytest.models.Pojo.ImageDetails;
import spd.com.shanbaytest.Adapter.GalleryRecycleViewAdapter;

/**
 * Created by linus on 17-10-1.
 */

public class GalleryActivity extends AppCompatActivity {

    String json = "[{\"url\": \"https://static.baydn.com/media/media_store/image/f1672263006c6e28bb9dee7652fa4cf6.jpg\"}," +
                    "{\"url\": \"https://static.baydn.com/media/media_store/image/8c997fae9ebb2b22ecc098a379cc2ca3.jpg\"}," +
                    "{\"url\": \"https://static.baydn.com/media/media_store/image/2a4616f067285b4bd59fe5401cd7106b.jpeg\"}," +
                    "{\"url\": \"https://static.baydn.com/media/media_store/image/b0e3ab329c8d8218d2af5c8dfdc21125.jpg\"}," +
                    "{\"url\": \"https://static.baydn.com/media/media_store/image/670abb28408a9a0fc3dd9666e5ca1584.jpg\"}," +
                    "{\"url\": \"https://static.baydn.com/media/media_store/image/1e8d675468ab61f4e5bdebd4bcb5f007.jpg\"}," +
                    "{\"url\": \"https://static.baydn.com/media/media_store/image/9b2f93cbfa104dae1e67f540ff14a4c2.jpg\"}," +
                    "{\"url\": \"https://static.baydn.com/media/media_store/image/f5e0631e00a09edbbf2eb21eb71b4d3c.jpeg\"}]";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_activity);

        List<ImageDetails> list;

        Gson gson = new Gson();
        list = gson.fromJson(json , new TypeToken<List<ImageDetails>>() {}.getType());

        //每次进入activity初始化图片加载列表，判断本地是否有对应已经下载好的图片
        for (ImageDetails imageDetails : list){

            if (FileUtils.isFileExists(FileUtils.getImgLocalPath(imageDetails.getUrl(), this))){
                imageDetails.setLocalPath(FileUtils.getImgLocalPath(imageDetails.getUrl(), this));
            }
        }


        GalleryRecycleViewAdapter galleryRecycleViewAdapter = new GalleryRecycleViewAdapter(list);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.gallery);

        recyclerView.addItemDecoration(new SpacesItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.waterfall_item_margin)));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(galleryRecycleViewAdapter);

    }

    private static class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;

        SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            outRect.bottom = space;
            outRect.top = space;
            outRect.left = 2 * space;
            outRect.right = 2 * space;
        }
    }
}
