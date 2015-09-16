package fr.bde_eseo.eseomega.news;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rascafr.test.matdesignfragment.R;

import fr.bde_eseo.eseomega.Constants;

/**
 * Created by Rascafr on 30/08/2015.
 */
public class ImageViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        String imgUrl = "http://mabite";

        // Get parameters
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                Toast.makeText(ImageViewActivity.this, "Erreur de l'application (c'est pas normal)", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                imgUrl = extras.getString(Constants.KEY_IMG);
            }
        }

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();

        TouchImageView touchImageView = (TouchImageView) findViewById(R.id.touchImg);
        ImageLoader imgLoad = ImageLoader.getInstance();

        imgLoad.displayImage(imgUrl, touchImageView, options);

    }

}
