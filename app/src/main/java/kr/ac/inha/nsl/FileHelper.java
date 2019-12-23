package kr.ac.inha.nsl;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class FileHelper {
    private static final String TAG = "FileHelper";
    private static final int FILE_SIZE_LIMIT = 50;// in KBytes
    private static FileOutputStream fileOutputStream = null;
    private static String filename;
    //private static StpArgRunnable fileWriterTasksHandler;
    //private static Thread fileWriterTasksHandlerThread;
    //private static LinkedList<StpArgRunnable> writeTaskQueue;
    //private static Context con;

    /*public static void openFile(Context con) {
        long timestamp_cur = System.currentTimeMillis();
        filename = "sp_" + timestamp_cur + ".csv";
        try {
            fileOutputStream = con.openFileOutput(filename, Context.MODE_APPEND);
            Log.e(TAG, "Open file: " + con.getFilesDir() + "/" + filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void closeFile(Context con) {
        try {
            if (fileOutputStream != null) {
                fileOutputStream.close();
                Log.e("FILE: CLOSE", "Closing file: " + con.getFilesDir() + "/" + filename);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToFileAndReopen(Context con, short data_src, String value) throws Exception {
        String resultString = data_src + "," + value + "\n";
        long fileSizeKBytes = 0;

        if (fileOutputStream != null && fileOutputStream.getChannel().isOpen())
            fileSizeKBytes = fileOutputStream.getChannel().size() / 1024;
        else
            openFile(con);

        if (fileSizeKBytes < FILE_SIZE_LIMIT) {
            if (fileOutputStream.getChannel().isOpen()) {
                fileOutputStream.write(resultString.getBytes());
                //Log.e("FILE: SAVE", "Saving file: " + con.getFilesDir() + "/" + filename);
            } else {
                openFile(con);
            }
        } else {
            closeFile(con); //close the file
            //region Sending File
            submitSensorData(con);
            Tools.sendHeartbeat(con);
            //endregion
            openFile(con); //reopen the file
        }
    }*/

    //function to submit the sensor data files
    public static void submitSensorData(final Context context) {
        SharedPreferences loginPrefs = context.getSharedPreferences("UserLogin", MODE_PRIVATE);
        if (Tools.isNetworkAvailable(context)) {
            List<Long> fileNamesInLong = new ArrayList<>();
            File[] filePaths = context.getFilesDir().listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return (file.getPath().endsWith(".csv"));
                }
            });

            for (File filePath : filePaths) {
                String fName = filePath.getName();
                String tmp = fName.substring(fName.lastIndexOf('_') + 1);
                fileNamesInLong.add(Long.valueOf(tmp.substring(0, tmp.lastIndexOf('.'))));
            }
            Collections.sort(fileNamesInLong);

            for (int n = 0; n < fileNamesInLong.size(); n++) {
                String fPath = context.getFilesDir() + "/" + "sp_" + fileNamesInLong.get(n) + ".csv";
                File file = new File(fPath);

                String url = context.getString(R.string.url_sensor_data_submit, context.getString(R.string.server_ip));
                String email = loginPrefs.getString(SignInActivity.user_id, null);
                String password = loginPrefs.getString(SignInActivity.password, null);

                try {
                    JSONObject json = new JSONObject(Tools.postFiles(url, email, password, file));

                    switch (json.getInt("result")) {
                        case Tools.RES_OK:
                            Log.d(TAG, "Submitted to server");
                            if (file.delete()) {
                                Log.e(TAG, "File " + file.getName() + " deleted");
                            } else {
                                Log.e(TAG, "File " + file.getName() + " NOT deleted");
                            }
                            break;
                        case Tools.RES_FAIL:
                            Thread.sleep(1000);
                            Log.e(TAG, "Submission to server Failed");
                            break;
                        case Tools.RES_SRV_ERR:
                            Thread.sleep(1000);
                            Log.d(TAG, "Submission Failed (SERVER)");
                            break;
                        default:
                            break;
                    }
                } catch (IOException | JSONException | InterruptedException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Submission to server Failed");
                }
            }
        }
    }

    /*public static void initFileWriterThread(final Context context) {
        FileHelper.con = context;
        if (writeTaskQueue != null)
            writeTaskQueue.clear();
        else
            writeTaskQueue = new LinkedList<>();

        fileWriterTasksHandlerThread = new Thread(fileWriterTasksHandler = new StpArgRunnable((short) 0, null) {
            @Override
            public void run() {
                while (!stopThread) {
                    if (writeTaskQueue.size() > 0) {
                        try {
                            writeTaskQueue.poll().run();
                        } catch (NullPointerException e) {
                            writeTaskQueue.clear();
                        }
                    }
                }
            }
        });
        fileWriterTasksHandlerThread.start();
    }

     */

    /*public static void destroyFileWriterThread() throws InterruptedException {
        if (fileWriterTasksHandler != null && fileWriterTasksHandlerThread != null && fileWriterTasksHandlerThread.isAlive()) {
            fileWriterTasksHandler.stopThread = true;
            writeTaskQueue.clear();
            fileWriterTasksHandlerThread.join();
        }
    }

     */

    /*public static void addWriteTask(short dataSrcAcc, String toString) {
        writeTaskQueue.add(new StpArgRunnable(dataSrcAcc, toString));
        //Log.e(TAG, "SIZE: " + writeTaskQueue.size());
    }

     */

    /*private static class StpArgRunnable implements Runnable {
        StpArgRunnable(short arg1, String arg2) {
            this.stopThread = false;
            this.arg1 = arg1;
            this.arg2 = arg2;
        }

        boolean stopThread;
        short arg1;
        String arg2;

        @Override
        public void run() {
            try {
                writeToFileAndReopen(arg1, arg2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

     */
}