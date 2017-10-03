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

import spd.com.shanbaytest.utils.FileUtils;
import spd.com.shanbaytest.models.Bean.ImageDetails;
import spd.com.shanbaytest.Adapter.GalleryRecycleViewAdapter;

/**
 * Created by linus on 17-10-1.
 */

public class GalleryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_activity);

        List<ImageDetails> list;
        String json = getString(R.string.json);

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
                getResources().getDimensionPixelSize(R.dimen.recycle_item_decoration)));
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
