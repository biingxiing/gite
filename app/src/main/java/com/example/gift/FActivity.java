package com.example.gift;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class FActivity extends ListActivity {

    public static final int REQUEST_CODE_GET_PHOTO = 1;
    public static final int REQUEST_CODE_GET_VIDEO = 2;

    public static final String EXTRA_MEMO_ID = "memoId";
    public static final String EXTRA_MEMO_NAME = "memoName";
    public static final String EXTRA_MEMO_CONTENT = "memoContent";
    TextWatcher isNull=new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if(i2>0){
                findViewById(R.id.add).setClickable(true);
            }
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }


    };
    private int memoId = -1;
    private EditText mName;
    private MediaAdapter adapter;
    private DbHelper db;
    private SQLiteDatabase dbRead, dbWrite;
    private String currentPath = null;
    /**
     * 按钮点击的监听器，实现OnClickListener接口
     */
    private View.OnClickListener btnClick = new View.OnClickListener() {

        Intent i;
        File f;

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.AddPhoto:// 添加照片按钮
                    // 使用Intent调用系统照相机，传入图像保存路径和名称
                    i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    f = new File(getMediaDir(), System.currentTimeMillis() + ".jpg");

                    if (!f.exists()) {
                        try {
                            f.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    currentPath = f.getAbsolutePath();
                    i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(i, REQUEST_CODE_GET_PHOTO);
                    break;
                case R.id.AddVideo:// 添加视频按钮
                    // 使用Intent调用系统录像器，传入视频保存路径和名称
                    i = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    f = new File(getMediaDir(), System.currentTimeMillis() + ".mp4");
                    if (!f.exists()) {
                        try {
                            f.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    currentPath = f.getAbsolutePath();
                    i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));

                    startActivityForResult(i, REQUEST_CODE_GET_VIDEO);
                    break;
                case R.id.back:// 保存按钮
                    // 保存多媒体信息和笔记信息到数据库，然后关闭当前页面，返回到笔记列表页面/主页面
                    saveMedia(saveMemo());
                    setResult(RESULT_OK);
                    finish();
                    break;
                case R.id.add://新建按钮
                    //保存多媒体信息和笔记信息到数据库，新建一个mian页面，并跳转到mian页面（若edittexet无信息则不可点击）
                    saveMedia(saveMemo());
                    i = new Intent(FActivity.this,FActivity.class);
                    setResult(RESULT_OK);
                    startActivity(i);
                    finish();
                    break;
                default:
                    break;
            }

        }

    };

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        db = new DbHelper(this);
        dbRead = db.getReadableDatabase();
        dbWrite = db.getWritableDatabase();

        // 显示多媒体列表
        adapter = new MediaAdapter(this);
        setListAdapter(adapter);

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick( final AdapterView<?> adapterView, View view, int i, long l) {
                final MediaListCellData deledata=adapter.getItem(i);
                final int line=i;
                final String delepath=deledata.path;
                AlertDialog.Builder builder=new AlertDialog.Builder(FActivity.this);
                builder.setMessage("确定删除?");
                builder.setTitle("提示");


                //添加AlertDialog.Builder对象的setPositiveButton()方法
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //如何获得相应的Item值，来对相应的文件进行操作
                        File f=new File(delepath);

                        f.delete();
                        adapter.delete(deledata);

                        adapter.notifyDataSetChanged();
                        deleMedia(line);

                        showString("删除该资源");
                    }
                });

                //添加AlertDialog.Builder对象的setNegativeButton()方法
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.create().show();
                return true;
            }
        });


        mName = (EditText) findViewById(R.id.edittext);

        // 获取Activity传递过来的noteId
        memoId = getIntent().getIntExtra(EXTRA_MEMO_ID, -1);

        if (memoId > -1) {
            mName.setText(getIntent().getStringExtra(EXTRA_MEMO_NAME));


            // 查询本笔记的noteId并且检查是否有对应的多媒体，有则遍历显示在MediaList中
            Cursor c = dbRead.query(DbHelper.TABLE_NAME_MEDIA, null,
                    DbHelper.COLUMN_NAME_MEDIA_OWNER_MEMO_ID + "=?",
                    new String[] { memoId + "" }, null, null, null);
            while (c.moveToNext()) {
                adapter.add(new MediaListCellData(c.getString(c
                        .getColumnIndex(DbHelper.COLUMN_NAME_MEDIA_PATH)), c
                        .getInt(c.getColumnIndex(DbHelper.COLUMN_NAME_ID))));
            }

            /**
             * Notifies the attached observers that the underlying data has been
             * changed and any View reflecting the data set should refresh
             * itself.
             */
            adapter.notifyDataSetChanged();
        }

        findViewById(R.id.back).setOnClickListener(btnClick);
        findViewById(R.id.add).setOnClickListener(btnClick);
        findViewById(R.id.AddPhoto).setOnClickListener(btnClick);
        findViewById(R.id.AddVideo).setOnClickListener(btnClick);

        mName.addTextChangedListener(isNull);
        findViewById(R.id.add).setClickable(false);


//        new Thread(new Runnable() {
//            boolean isNull=true;
//            @Override
//            public void run() {
//                while(isNull){
//                if(mName.getText().toString().equals("")){
//                    findViewById(R.id.add).setClickable(false);
//
//                }else{
//                    findViewById(R.id.add).setClickable(true);
//                    isNull=false;
//                }}
//            }
//        }).start();
    }

    public  boolean onKeyDown(int keyCode,KeyEvent event){
        if (keyCode==KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            saveMedia(saveMemo());
            setResult(RESULT_OK);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }




//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event)  {
//		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { //按下的如果是BACK，同时没有重复
//			Toast.makeText(ml78.this,"魔力去吧Back键测试",1).show();
//			return true;
//		}
//
//		return super.onKeyDown(keyCode, event);
//	}

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        MediaListCellData data = adapter.getItem(position);
        Intent i ;
        File f;

        switch (data.type) {
            case MediaType.PHOTO:
                 f=new File(data.path);
                i=getPhotoFileIntent(f);
                startActivity(i);
                break;
            case MediaType.VIDEO:
                f = new File(data.path);
                i=getVideoFileIntent(f);
                startActivity(i);
                break;
        }

        super.onListItemClick(l, v, position, id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        System.out.println(data);

        switch (requestCode) {
            case REQUEST_CODE_GET_PHOTO:
            case REQUEST_CODE_GET_VIDEO:
                if (resultCode == RESULT_OK) {
                    adapter.add(new MediaListCellData(currentPath));
                    adapter.notifyDataSetChanged();
                }
                break;


            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    private Intent getPhotoFileIntent(File photoFile){
        Intent i=new Intent(Intent.ACTION_VIEW);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Uri uri=Uri.fromFile(photoFile);
        i.setDataAndType(uri,"image/*");
        return i;
    }
    private Intent getVideoFileIntent(File videoFile){
        Intent i=new Intent(Intent.ACTION_VIEW);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Uri uri=Uri.fromFile(videoFile);
        i.setDataAndType(uri,"video/*");
        return i;
    }

    /**
     * 获取存储Media的目录路径
     *
     * @return File类型的目录路径
     */
    public File getMediaDir() {
        File dir = new File(Environment.getExternalStorageDirectory(),"MemosMedia");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * 保存Media信息到数据库
     *
     * @param memoId
     */
    public void saveMedia(int memoId) {

        MediaListCellData data;
        ContentValues cv;

        for (int i = 0; i < adapter.getCount(); i++) {
            data = adapter.getItem(i);

            if (data.id <= -1) {
                cv = new ContentValues();
                cv.put(DbHelper.COLUMN_NAME_MEDIA_PATH, data.path);
                cv.put(DbHelper.COLUMN_NAME_MEDIA_OWNER_MEMO_ID, memoId);
                dbWrite.insert(DbHelper.TABLE_NAME_MEDIA, null, cv);
            }
        }
    }
    public  void deleMedia(int hId ){


        dbWrite.delete(DbHelper.TABLE_NAME_MEDIA, DbHelper.COLUMN_NAME_MEDIA_OWNER_MEMO_ID+ "=?" , new String[] {hId+ ""});

    }

    /**
     * 保存备忘录到数据库
     * 并返回新添加的行数
     * @return
     */
    public int saveMemo() {

        ContentValues cv = new ContentValues();
        if(!mName.getText().toString().equals("")) {
            cv.put(DbHelper.COLUMN_NAME_MEMO_NAME, mName.getText().toString());

            cv.put(DbHelper.COLUMN_NAME_MEMO_DATE, new SimpleDateFormat(
                    "yyyy-MM-dd hh:mm:ss").format(new Date()));
        }
        if (memoId > -1) {
            dbWrite.update(DbHelper.TABLE_NAME_MEMOS, cv, DbHelper.COLUMN_NAME_ID
                    + "=?", new String[] { memoId + "" });
            return memoId;
        } else {
            return (int) dbWrite.insert(DbHelper.TABLE_NAME_MEMOS, null, cv);
        }
    }



    /**
     * 复写Activity的生命周期方法，用于关闭读写数据库的操作
     */
    @Override
    protected void onDestroy() {
        dbRead.close();
        dbWrite.close();
//		File fileDirectory=new File(Environment.getExternalStorageDirectory()+"/NotesMedia");
//		deleteFile(fileDirectory);
        super.onDestroy();
    }


//	public  void deleteFile(File file){
//		if(file.exists()){
//			if(file.isFile()){
//				file.delete();
//			}else if(file.isDirectory()){
//				File files[]=file.listFiles();
//				for(int i=0;i<files.length;i++){
//					this.deleteFile(files[i]);
//				}
//			}
//			file.delete();
//		}else {
//			showString("文件不存在");
//		}
//
//	}

    public  void showString(String sth){
        Toast.makeText(getBaseContext(), sth, Toast.LENGTH_SHORT).show();
    }

    /**
     * 继承BaseAdapter类的MediaAdapter类，用于显示媒体信息
     *
     * @author TOPS
     *
     */
    static class MediaAdapter extends BaseAdapter {
        private Context context;
        private List<MediaListCellData> list = new ArrayList<MediaListCellData>();

        public MediaAdapter(Context context) {
            this.context = context;
        }

        public void add(MediaListCellData data) {
            list.add(data);
        }
        public void delete(MediaListCellData data){list.remove(data);}
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public MediaListCellData getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.src_list, null);
            }

            MediaListCellData data = getItem(position);

            ImageView ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
            TextView tvPath = (TextView) convertView.findViewById(R.id.tvPath);

            ivIcon.setImageResource(data.iconId);
            tvPath.setText(data.path);
            return convertView;
        }
//		public View deleView(int position,View convertView, ViewGroup parent){
//			if(convertView==null){
//				convertView=LayoutInflater.from(context).inflate(R.layout.media_list_cell,null);
//							}
//			MediaListCellData data=getItem(position);
//			ImageView ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
//			TextView tvPath = (TextView) convertView.findViewById(R.id.tvPath);
//
//
//
//
//			return convertView;
//		}


    }

    /**
     * 显示多媒体的条目类
     *
     * @author TOPS
     *
     */
    static class MediaListCellData {
        int type = 0;
        int id = -1;
        String path = "";
        int iconId = R.drawable.ic_launcher;

        public MediaListCellData(String path, int id) {
            this(path);

            this.id = id;
        }

        public MediaListCellData(String path) {
            this.path = path;

            if (path.endsWith(".jpg")) {
                iconId = R.drawable.ic_launcher;
                type = MediaType.PHOTO;
            } else if (path.endsWith(".mp4")) {
                iconId = R.drawable.ic_launcher;
                type = MediaType.VIDEO;
            }
        }

    }

    /**
     * 多媒体的种类
     *
     * @author TOPS
     *
     */
    static class MediaType {
        static final int PHOTO = 1;
        static final int VIDEO = 2;
    }
}
