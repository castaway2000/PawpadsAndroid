package saberapplications.pawpads.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.quickblox.content.QBContent;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import saberapplications.pawpads.PawPadsApplication;
import saberapplications.pawpads.Util;

/**
 * Created by Stanislav Volnyansky on 10.03.16.
 */
public class AvatarLoaderHelper {

    public static void loadImage(int fileId, final ImageView imageView, final int width, final int height) {
        loadImage(fileId, imageView, width, height, null);
    }

    public static void loadImageSync(int fileId, final ImageView imageView, final int width, final int height) {
        try {
            if (fileId == 0) {
                return;
            }
            File CacheDir = PawPadsApplication.getInstance().getCacheDir();
            final File file = new File(CacheDir.getAbsolutePath() + "/" + fileId + ".jpg");
            Bitmap image=null;
            if (file.exists()) {
                image=BitmapFactory.decodeFile(file.getAbsolutePath());
            } else {
                Bundle out = new Bundle();
                InputStream stream = QBContent.downloadFile(String.valueOf(fileId), out);
                Bitmap bitmap=BitmapFactory.decodeStream(stream);
                FileOutputStream outStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
                outStream.close();
                stream.close();
            }

            final Bitmap thumbnail = ThumbnailUtils.extractThumbnail(image,width,height);
            imageView.post(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(thumbnail);
                   // thumbnail.recycle();
                }
            });
            image.recycle();

        }catch (Exception e){
            return;
        }
    }

    public static void loadImage(final int fileId, final ImageView imageView, final int width, final int height, final Callback callback) {
        if (fileId == 0) return;
        File CacheDir = PawPadsApplication.getInstance().getCacheDir();
        final File file = new File(CacheDir.getAbsolutePath() + "/" + fileId + ".jpg");
        // Trying get image from cache
        if (file.exists()) {
            try {
                Glide.with(imageView.getContext()).load(file).centerCrop().override(width, height).into(imageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (callback != null) callback.imageLoaded();
        } else {
            QBContent.downloadFileById(fileId, new QBEntityCallback<InputStream>() {
                        @Override
                        public void onSuccess(InputStream inputStream, Bundle bundle) {
                            new AsyncTask<InputStream, Void, Boolean>() {

                                @Override
                                protected Boolean doInBackground(InputStream... params) {
                                    Bitmap bitmap= BitmapFactory.decodeStream(params[0]);
                                    try {
                                        FileOutputStream stream = new FileOutputStream(file);
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                                        stream.close();
                                        bitmap.recycle();
                                        bitmap = null;
                                        return true;
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    return false;
                                }

                                @Override
                                protected void onPostExecute(Boolean res) {
                                    if (!res) return;
                                    try {
                                        if (imageView.getContext() instanceof AppCompatActivity){
                                            Activity activity=(AppCompatActivity)imageView.getContext();
                                            if (activity.isFinishing() ) return;
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                if ( activity.isDestroyed()){
                                                    return;
                                                }
                                            }
                                        }
                                        Glide.with(imageView.getContext()).load(file).centerCrop().override(width, height).into(imageView);
                                    }
                                    catch(Exception ex){

                                    } finally {

                                    }
                                    if (callback != null) callback.imageLoaded();

                                }
                            }.execute(inputStream);
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            Util.onError(e, PawPadsApplication.getInstance().getApplicationContext());
                        }
                    }, new QBProgressCallback() {
                        @Override
                        public void onProgressUpdate(int i) {

                        }
                    }
            );
        }


    }

    public interface Callback {
        void imageLoaded();

    }
}
