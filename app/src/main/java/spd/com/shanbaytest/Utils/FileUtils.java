package spd.com.shanbaytest.Utils;

import android.content.Context;
import android.util.Log;

import java.io.File;

/**
 * Created by linus on 17-10-2.
 */

public class FileUtils {

    public static String getImgLocalPath(String url, Context context){
        String[] urlStrings = url.split("/");

        File sdFileDir = context.getExternalCacheDir();
        if (sdFileDir == null){
            return null;
        }

        String[] extStrings;
        extStrings = sdFileDir.toString().split("/");
        return sdFileDir.toString().replace(extStrings[extStrings.length - 1], "Images") + File
                .separator + urlStrings[urlStrings.length - 1];

    }

    public static boolean isFileExists(String url){
        return new File(url).exists();
    }

    public static String getImgLocalDir( Context context){

        File sdFileDir = context.getExternalCacheDir();
        if (sdFileDir == null){
            return null;
        }

        String[] extStrings;
        extStrings = sdFileDir.toString().split("/");
        return sdFileDir.toString().replace(extStrings[extStrings.length - 1], "Images") + File
                .separator;

    }
}
