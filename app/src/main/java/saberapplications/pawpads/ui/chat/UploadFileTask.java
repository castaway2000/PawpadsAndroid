package saberapplications.pawpads.ui.chat;

import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.content.model.QBFileStatus;
import com.quickblox.content.task.TaskSyncUploadFile;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.ContentType;

import java.io.File;

/**
 * Created by Stanislav Volnjanskij on 11/15/16.
 */

public class UploadFileTask extends TaskSyncUploadFile {
    private QBProgressCallback progressCallback;
    private File file;
    private boolean publicAccess;
    private String tags;
    private int fileSize;
    private QBFile qbfile;

    public UploadFileTask(File file, boolean publicAccess, String tags) {
        super(file,publicAccess,tags);
        this.fileSize = 0;
        this.qbfile = new QBFile();
        this.file = file;
        this.publicAccess = publicAccess;
        this.tags = tags;
    }

    public UploadFileTask(File file, boolean publicAccess, String tags, QBProgressCallback progressCallback) {
        this(file, publicAccess, tags);
        this.progressCallback = progressCallback;
    }

    @Override
    public QBFile execute() throws QBResponseException {

        String contentType = ContentType.getContentType(this.file);
        if (contentType==null || contentType.length()<4){
            contentType="application/octet-stream";
        }
        this.qbfile.setName(this.file.getName());
        this.qbfile.setPublic(Boolean.valueOf(this.publicAccess));
        this.qbfile.setContentType(contentType);
        this.qbfile.setTags(this.tags);
        QBFile fileResult = QBContent.createFile(this.qbfile);
        fileResult.copyFieldsTo(this.qbfile);
        String params = this.qbfile.getFileObjectAccess().getParams();
        QBContent.uploadFile(this.file, params, this.progressCallback);
        int fileId = this.qbfile.getId().intValue();
        this.fileSize = (int)this.file.length();
        QBContent.declareFileUploaded(fileId, this.fileSize);
        this.qbfile.setSize(this.fileSize);
        this.qbfile.setStatus(QBFileStatus.COMPLETE);
        return this.qbfile;
    }
}
