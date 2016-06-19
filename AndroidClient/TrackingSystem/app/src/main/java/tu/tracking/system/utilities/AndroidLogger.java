package tu.tracking.system.utilities;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.GregorianCalendar;

public class AndroidLogger {
    private static AndroidLogger instance;

    private AndroidLogger(){}

    public static AndroidLogger getInstance(){
        if(instance == null){
            instance = new AndroidLogger();
        }

        return instance;
    }

    public void logMessage(String tag, String message){
        if(isExternalStorageAvailable()) {
            FileOutputStream fop = null;
            try {
                deleteOldLogs();
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File(sdCard.getAbsolutePath() + Constants.LOG_PATH);
                boolean dirsMade = dir.mkdirs();
                GregorianCalendar now = new GregorianCalendar();
                String fileName = DateManager.getDateStringWithHyphensFromCalendar(now) + Constants.LOG_FILE_EXTENSION;
                File file = new File(dir, fileName);

                fop = new FileOutputStream(file, true);
                String content = DateManager.getBGDateTimeStringFromCalendar(now) + "\t" + tag + ":\t" + message + "\n";
                byte[] contentInBytes = content.getBytes();

                fop.write(contentInBytes);
                fop.flush();
                fop.close();
            } catch (Exception e) {
            } finally {
                try {
                    if (fop != null) {
                        fop.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void logError(String tag, Exception e){
        String message = "Error: " + e.toString() + " | Message: " + e.getMessage();
        logMessage(tag, message);
    }

    private void deleteOldLogs(){
        File sdCard = Environment.getExternalStorageDirectory();
        File folder = new File(sdCard.getAbsolutePath() + Constants.LOG_PATH);

        if (folder.exists())
        {
            if(folder.list().length >= Constants.MAX_LOG_FILES){
                new File(folder, findOldestFile(folder)).delete();
            }
        }
    }

    private String findOldestFile(File folder){
        String[] files = folder.list();
        File oldestFile = new File(folder, files[0]);
        for (int i = 0; i < files.length; i++){
            File file = new File(folder, files[i]);
            if(file.isFile()){
                if(oldestFile.lastModified() > file.lastModified()){
                    oldestFile = file;
                }
            }
        }

        return oldestFile.getName();
    }

    private boolean isExternalStorageAvailable(){
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWritable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mExternalStorageAvailable = mExternalStorageWritable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            mExternalStorageAvailable = true;
            mExternalStorageWritable = false;
        } else {
            mExternalStorageAvailable = mExternalStorageWritable = false;
        }

        return mExternalStorageAvailable && mExternalStorageWritable;
    }
}
