package spd.com.shanbaytest.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;


import spd.com.shanbaytest.R;
import spd.com.shanbaytest.models.ImageLoader;
import spd.com.shanbaytest.models.Bean.ImageDetails;

/**
 * Created by linus on 17-10-1.
 */

public class GalleryRecycleViewAdapter extends RecyclerView.Adapter{

    private List<ImageDetails> imageDetailsList;
    private ImageLoader imageLoader;


    public GalleryRecycleViewAdapter(List<ImageDetails> imageDetailsList) {
        this.imageDetailsList = imageDetailsList;
        imageLoader = ImageLoader.newInstance();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
        return new ItemViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder){
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            imageLoader.loadImageInto(imageDetailsList.get(position), itemViewHolder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return imageDetailsList.size();
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;

        ItemViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image);
        }
    }
}
