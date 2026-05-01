package com.sz.cp2102.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sz.cp2102.BleActiity;
import com.sz.cp2102.bean.MessageEvent;
import com.sz.cp2102.R;
import com.sz.cp2102.bean.BleHex;
import com.sz.cp2102.config.EventBusId;
import com.sz.cp2102.utils.LogUtil;
import com.sz.cp2102.utils.PreferencesUtil;
import com.sz.cp2102.utils.TextUtils;
import com.sz.cp2102.utils.logFile;
import com.clj.fastble.BleManager;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.interfaces.OnConfirmListener;
import com.lxj.xpopup.interfaces.OnInputConfirmListener;
import com.lxj.xpopup.interfaces.OnSelectListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.EasyPermissions;

public class LogFragment extends Fragment {
    private ListView listView;
    public List<logFile> logList = new ArrayList<>();
    private LogAdapter logAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        EventBus.getDefault().register(this);

        initView(view);
        initData();
        Log.e("onCreateView11", "onCreateView222");
        return view;
    }

    public void initView(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        listView = view.findViewById(R.id.listView);
        logAdapter = new LogAdapter(getContext());
        listView.setAdapter(logAdapter);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Operation to perform on refresh
                setdata();
            }
        });
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(getActivity(), perms)) {
            setdata();
        } else {
            EasyPermissions.requestPermissions(getActivity(), getResources().getText(R.string.applyBlue).toString(), 10002, perms);
        }
    }

    public void setdata() {
        logList.clear();
        logList.addAll(LogUtil.getAllDataFileName());
        logAdapter.addResult(logList);
        logAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }


    public void initData() {
        int timeAll = PreferencesUtil.getInt(getContext(), "timeAll", 0);
        int time = PreferencesUtil.getInt(getContext(), "time", 0);
        timeAll = timeAll + time;
        PreferencesUtil.putInt(getContext(), "timeAll", timeAll);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        Log.e("onDestroy", "onDestroy3");
        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }


    private int timeAll = 0;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void upData(MessageEvent<String> event) {
        if (!event.getId().equals(EventBusId.upData)) {
            return;
        }
    }


    private class LogAdapter extends BaseAdapter {

        private Boolean seleteHex;
        private Context context;
        private List<logFile> logList;
        private String mac;

        LogAdapter(Context context) {
            this.context = context;
            logList = new ArrayList<>();

        }

        public void setSeleteHex(Boolean seleteHex) {
            this.seleteHex = seleteHex;
        }

        void addResult(List<logFile> characteristicList) {
//            for ( int i=0;i<characteristicList.size();i++ ){
//
//            }
            this.logList = characteristicList;
        }

        void clear() {
            logList.clear();
        }

        @Override
        public int getCount() {
            return logList.size();
        }

        @Override
        public logFile getItem(int position) {
            if (position > logList.size())
                return null;
            return logList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LogAdapter.ViewHolder holder;
            if (convertView != null) {
                holder = (LogAdapter.ViewHolder) convertView.getTag();
            } else {
                convertView = View.inflate(context, R.layout.adapter_log_file, null);
                holder = new LogAdapter.ViewHolder();
                holder.txt_log = (TextView) convertView.findViewById(R.id.txt_log);
                holder.btn_selete = (TextView) convertView.findViewById(R.id.btn_selete);
                convertView.setTag(holder);
            }
//            holder.txt_title.setText("Data:");
            holder.txt_log.setText(logList.get(position).getName());
            holder.txt_log.setTag(logList.get(position).getPath());
            holder.txt_log.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String path = (String) v.getTag();
//                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                    intent.addCategory(Intent.CATEGORY_DEFAULT);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    Uri uri = Uri.fromFile(new File(LogUtil.getFilePath(path)));
//                    intent.setDataAndType(uri, "text/plain");
//                    context.startActivity(intent);
                    openFile(path);
                }
            });
            holder.btn_selete.setTag(logList.get(position).getPath());
            holder.btn_selete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String path = (String) v.getTag();
//                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                    intent.addCategory(Intent.CATEGORY_DEFAULT);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    Uri uri = Uri.fromFile(new File(LogUtil.getFilePath(path)));
//                    intent.setDataAndType(uri, "text/plain");
//                    context.startActivity(intent);
                    showDelete(path);
                }
            });


            return convertView;
        }

        class ViewHolder {
            TextView txt_log;
            TextView btn_selete;

        }
    }

    public void showEdittex(final String delFile) {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(getActivity(), perms)) {
            new XPopup.Builder(getActivity()).asInputConfirm(getResources().getString(R.string.deteleLog), getResources().getString(R.string.file), new OnInputConfirmListener() {
                @Override
                public void onConfirm(String text) {
                    if (text.length()>0){
                        renameFile(delFile,text);
                    }else {
                        Toast.makeText(getActivity(), R.string.textEmpty, Toast.LENGTH_SHORT).show();
                    }
                }
            }).show();
        } else {
            EasyPermissions.requestPermissions(getActivity(), getResources().getText(R.string.applyBlue).toString(), 10002, perms);
        }
    }

    public void showDelete(final String delFile) {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(getActivity(), perms)) {
            new XPopup.Builder(getActivity()).asConfirm(getResources().getString(R.string.deteleLog),
                    getResources().getString(R.string.deleteDevice), new OnConfirmListener() {
                        @Override
                        public void onConfirm() {
                            if (delete(delFile)) {
                                setdata();
                            }
                        }
                    }).show();
        } else {
            EasyPermissions.requestPermissions(getActivity(), getResources().getText(R.string.applyBlue).toString(), 10002, perms);
        }
    }

    /**
     * oldPath and newPath must be absolute paths to the old and new files
     */
    private void renameFile(String oldPath, String newPath) {
        File oldFile = new File(oldPath);
        if (oldFile.exists()) {
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            File dir = new File(filePath, "bleLog");
            File newFile = new File(dir, newPath + ".txt");
            boolean b = oldFile.renameTo(newFile);
            File file2 = new File(newFile.getPath());
            setdata();
        } else {
            Toast.makeText(getActivity(), R.string.fileIsdelete, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Delete a file or directory
     *
     * @param delFile Directory or file name to delete
     * @return Returns true if deletion succeeds; otherwise false
     */
    public boolean delete(String delFile) {
        File file = new File(delFile);
        if (!file.exists()) {
//            Toast.makeText(HnUiUtils.getContext(), "Failed to delete file:" + delFile + "does not exist!", Toast.LENGTH_SHORT).show();
            Log.e("删除文件失败:", delFile + "不存在！");
            return false;
        } else {
            if (file.isFile())
                return deleteSingleFile(delFile);
            else
                return deleteDirectory(delFile);
        }
    }

    /**
     * Delete a single file
     *
     * @param filePath$Name File name to delete
     * @return Returns true if the single file is deleted; otherwise false
     */
    public static boolean deleteSingleFile(String filePath$Name) {
        File file = new File(filePath$Name);
        // If the path exists and points to a file, delete it directly
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                Log.e("--Method--", "Copy_Delete.deleteSingleFile: 删除单个文件" + filePath$Name + "成功！");

                return true;
            } else {
                Log.e("删除单个文件", filePath$Name + "失败！");
                return false;
            }
        } else {
            Log.e("删除单个文件失败：", filePath$Name + "不存在！");
            return false;
        }
    }

    /**
     * Delete a directory and all files under it
     *
     * @param filePath Path of the directory to delete
     * @return Returns true if the directory is deleted; otherwise false
     */
    public static boolean deleteDirectory(String filePath) {
        // If dir does not end with the file separator, append it automatically
        if (!filePath.endsWith(File.separator))
            filePath = filePath + File.separator;
        File dirFile = new File(filePath);
        // If dir does not exist or is not a directory, exit
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            Log.e("删除目录失败：", filePath + "不存在！");
            return false;
        }
        boolean flag = true;
        // Delete all files in the directory, including subdirectories
        File[] files = dirFile.listFiles();
        for (File file : files) {
            // Delete child file
            if (file.isFile()) {
                flag = deleteSingleFile(file.getAbsolutePath());
                if (!flag)
                    break;
            }
            // Delete child directory
            else if (file.isDirectory()) {
                flag = deleteDirectory(file
                        .getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            Log.e("删除目录失败！", "544");
            return false;
        }
        // Delete the current directory
        if (dirFile.delete()) {
            Log.e("--Method--", "Copy_Delete.deleteDirectory: 删除目录" + filePath + "成功！");
            return true;
        } else {
            Log.e("删除目录：", filePath + "失败！");
            return false;
        }
    }
    /**Declare dataType values for each file type**/
    private static final String DATA_TYPE_ALL = "*/*";// No specific file type is declared; use a chooser instead of an exact-type handler
    private static final String DATA_TYPE_APK = "application/vnd.android.package-archive";
    private static final String DATA_TYPE_VIDEO = "video/*";
    private static final String DATA_TYPE_AUDIO = "audio/*";
    private static final String DATA_TYPE_HTML = "text/html";
    private static final String DATA_TYPE_IMAGE = "image/*";
    private static final String DATA_TYPE_PPT = "application/vnd.ms-powerpoint";
    private static final String DATA_TYPE_EXCEL = "application/vnd.ms-excel";
    private static final String DATA_TYPE_WORD = "application/msword";
    private static final String DATA_TYPE_CHM = "application/x-chm";
    private static final String DATA_TYPE_TXT = "text/plain";
    private static final String DATA_TYPE_PDF = "application/pdf";
    /**
     * Get the URI for the given file
     * @param intent Target Intent
     * @param file File object
     * @return
     */
    private  Uri getUri(Intent intent, File file) {
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //Check whether the Android version is 7.0 or higher
            uri =
                    FileProvider.getUriForFile(getContext(),
                            getContext().getPackageName() + ".fileprovider",
                            file);
            //Add this flag to grant the target app temporary access to the file represented by this URI
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }
    /**
     * Open a file
     * @param filePath Full file path, including the file name
     */
    private   void openFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()){
            //If the file does not exist
            Toast.makeText(getContext(), "打开失败，原因：文件已经被移动或者删除", Toast.LENGTH_SHORT).show();
            return;
        }
        /* Get the file extension */
        String end = file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length()).toLowerCase(Locale.getDefault());
        /* Determine the MIME type from the file extension */
        Intent intent = null;
        if (end.equals("m4a") || end.equals("mp3") || end.equals("mid") || end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
            intent =  generateVideoAudioIntent(filePath,DATA_TYPE_AUDIO);
        } else if (end.equals("3gp") || end.equals("mp4")) {
            intent = generateVideoAudioIntent(filePath,DATA_TYPE_VIDEO);
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png") || end.equals("jpeg") || end.equals("bmp")) {
            intent = generateCommonIntent(filePath,DATA_TYPE_IMAGE);
        } else if (end.equals("apk")) {
            intent = generateCommonIntent(filePath,DATA_TYPE_APK);
        } else if (end.equals("ppt")) {
            intent = generateCommonIntent(filePath,DATA_TYPE_PPT);
        } else if (end.equals("xls")) {
            intent = generateCommonIntent(filePath,DATA_TYPE_EXCEL);
        } else if (end.equals("doc")) {
            intent = generateCommonIntent(filePath,DATA_TYPE_WORD);
        } else if (end.equals("pdf")) {
            intent = generateCommonIntent(filePath,DATA_TYPE_PDF);
        } else if (end.equals("chm")) {
            intent = generateCommonIntent(filePath,DATA_TYPE_CHM);
        } else if (end.equals("txt")) {
            intent = generateCommonIntent(filePath, DATA_TYPE_TXT);
        } else {
            intent = generateCommonIntent(filePath,DATA_TYPE_ALL);
        }
        getContext().startActivity(intent);
    }

    /**
     * Create an Intent for opening file types other than video, audio, and web pages
     * @param filePath File path
     * @param dataType File type
     * @return
     */
    private   Intent generateCommonIntent(String filePath, String dataType) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        File file = new File(filePath);
        Uri uri = getUri(intent, file);
        intent.setDataAndType(uri, dataType);
        return intent;
    }

    /**
     * Create an Intent for opening video or audio files
     * @param filePath File path
     * @param dataType File type
     * @return
     */
    private  Intent generateVideoAudioIntent(String filePath, String dataType){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        File file = new File(filePath);
        intent.setDataAndType(getUri(intent,file), dataType);
        return intent;
    }
    /**
     * Create an Intent for opening web files
     * @param filePath File path
     * @return
     */
    private  Intent generateHtmlFileIntent(String filePath) {
        Uri uri = Uri.parse(filePath)
                .buildUpon()
                .encodedAuthority("com.android.htmlfileprovider")
                .scheme("content")
                .encodedPath(filePath)
                .build();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, DATA_TYPE_HTML);
        return intent;
    }
}

