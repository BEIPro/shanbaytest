package spd.com.shanbaytest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by linus on 17-10-1.
 */

public class MainActivity extends AppCompatActivity {

    Button textButton;
    Button galleryButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        textButton = (Button) findViewById(R.id.text_activity);
        textButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), TextActivity.class));
            }
        });

        galleryButton = (Button) findViewById(R.id.gallery_activity);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), GalleryActivity.class));
            }
        });
    }
}
