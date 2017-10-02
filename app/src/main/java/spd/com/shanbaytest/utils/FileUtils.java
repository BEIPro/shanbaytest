package spd.com.shanbaytest.utils;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;

/**
 * Created by joe on 17-10-2.
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

    public static String getImgLocalDir(Context context){

        File sdFileDir = context.getExternalCacheDir();
        if (sdFileDir == null){
            return null;
        }

        String[] extStrings;
        extStrings = sdFileDir.toString().split("/");
        return sdFileDir.toString().replace(extStrings[extStrings.length - 1], "Images") + File
                .separator;

    }

    /**
     * 将retrofit获取的response写到本地sd卡
     */
    public static boolean writeResponseBodyToDisk(Context context, ResponseBody body, String url) {
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
}
