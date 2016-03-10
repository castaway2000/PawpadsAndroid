package saberapplications.pawpads.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;

import com.quickblox.content.QBContent;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import saberapplications.pawpads.PawPadsApplication;
import saberapplications.pawpads.Util;

/**
 * Created by Stanislav Volnyansky on 10.03.16.
 */
public class AvatarLoaderHelper {
    public static void loadImage(int fileId, final ImageView imageView, final int width, final int height){
        if (fileId==0) return;
        File CacheDir=PawPadsApplication.getInstance().getCacheDir();
        final File file=new File(CacheDir.getAbsolutePath()+"/"+fileId+".jpg");
        // Trying get image from cache
        if (file.exists()){
            Picasso.with(imageView.getContext()).load(file).centerCrop().resize(width,height).into(imageView);
        }
        else {
            QBContent.downloadFileTask(fileId, new QBEntityCallback<InputStream>() {
                @Override
                public void onSuccess(InputStream inputStream, Bundle params) {
                    new AsyncTask<InputStream, Void, Bitmap>() {

                        @Override
                        protected Bitmap doInBackground(InputStream... params) {
/*
                            BitmapFactory.Options o = new BitmapFactory.Options();
                            int width_tmp = o.outWidth, height_tmp = o.outHeight;
                            int scale = 1;

                            while (true) {
                                if (width_tmp / 2 < 80 || height_tmp / 2 < 80)
                                    break;
                                width_tmp /= 2;
                                height_tmp /= 2;
                                scale *= 2;
                            }

                            BitmapFactory.Options o2 = new BitmapFactory.Options();
                            o2.inSampleSize = scale;
*/
                            return BitmapFactory.decodeStream(params[0]);
                        }

                        @Override
                        protected void onPostExecute(Bitmap bitmap) {
                            super.onPostExecute(bitmap);

                            try {
                                FileOutputStream stream=new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.JPEG,90,stream);
                                stream.close();
                                bitmap.recycle();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Picasso.with(imageView.getContext()).load(file).centerCrop().resize(width,height).into(imageView);


                        }
                    }.execute(inputStream);
                }

                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(List<String> list) {
                    Util.onError(list, PawPadsApplication.getInstance().getApplicationContext());
                }


            }, new QBProgressCallback() {
                @Override
                public void onProgressUpdate(int progress) {

                }
            });
        }


    }
}
