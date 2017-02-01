package saberapplications.pawpads.service;

import android.app.IntentService;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.quickblox.chat.model.QBAttachment;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import saberapplications.pawpads.R;

public class FileDownloadService extends IntentService {

    private static Handler handler;

    public FileDownloadService() {
        super("FileDownloadService");
    }

    public static final String ATTACHMENT_NAME="attachement_name";
    public static final String ATTACHMENT_ID="attachement_id";

    public static void startService(Context context, QBAttachment attachment) {
        Intent intent = new Intent(context, FileDownloadService.class);
        intent.putExtra(ATTACHMENT_ID,attachment.getId());
        //attachment.getName() + attachment.getType()
        intent.putExtra(ATTACHMENT_NAME,attachment.getName());

        context.startService(intent);
        handler=new Handler();
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            try {
                String fileId=intent.getStringExtra(ATTACHMENT_ID);
                final String fileName=intent.getStringExtra(ATTACHMENT_NAME);
                if (fileId==null) return;
                File targetFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);
                if (!targetFile.exists()) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(FileDownloadService.this, R.string.download_started,Toast.LENGTH_LONG).show();
                        }
                    });

                    QBFile file = QBContent.getFile(Integer.parseInt(fileId));
                    URL u = new URL(file.getPrivateUrl());
                    InputStream inputStream = u.openStream();

                    OutputStream outStream = new FileOutputStream(targetFile);

                    byte[] buffer = new byte[1024 * 100];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, bytesRead);
                    }
                }
                MimeTypeMap myMime = MimeTypeMap.getSingleton();
                Intent newIntent = new Intent(Intent.ACTION_VIEW);
                String mimeType = myMime.getMimeTypeFromExtension(fileExt(fileName));
                newIntent.setDataAndType(Uri.fromFile(targetFile),mimeType);
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(newIntent);
                } catch (ActivityNotFoundException e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(FileDownloadService.this, R.string.file_downloaded, Toast.LENGTH_LONG).show();
                        }
                    });

                }

            } catch (final IOException e) {
                e.printStackTrace();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FileDownloadService.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                    }
                });

            }

        }
    }
    private String fileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }
}
