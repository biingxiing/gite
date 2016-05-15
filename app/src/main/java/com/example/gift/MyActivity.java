package com.example.gift;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.io.File;


public class MyActivity extends ListActivity {
    private SimpleCursorAdapter adapter = null;
    private DbHelper db;
    private SQLiteDatabase dbRead,dbWrite;

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_EDIT_NOTE = 2;

    /**
     * 实现OnClickListener接口，添加备忘录按钮的监听
     */
    private View.OnClickListener btnAddMemo_clickHandler = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // 有返回结果的开启编辑备忘录的Activity，
            // requestCode If >= 0, this code will be returned
            // in onActivityResult() when the activity exits.
            startActivityForResult(new Intent(MyActivity.this,
                    FActivity.class), REQUEST_CODE_ADD_NOTE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.factivity);

        // 操作数据库
        db = new DbHelper(this);
        dbRead = db.getReadableDatabase();
        dbWrite=db.getWritableDatabase();

        // 查询数据库并将数据显示在ListView上。
        // 建议使用CursorLoader，这个操作因为在UI线程，容易引起无响应错误
        adapter = new SimpleCursorAdapter(this, R.layout.listview, null,
                new String[] { DbHelper.COLUMN_NAME_MEMO_NAME,
                        DbHelper.COLUMN_NAME_MEMO_DATE }, new int[] {
                R.id.mMemo});
        setListAdapter(adapter);

        refreshMemosListView();

        findViewById(R.id.fadd).setOnClickListener(
                btnAddMemo_clickHandler);

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick( final AdapterView<?> adapterView, View view,   int i, long l) {
               Cursor c=adapter.getCursor();
                c.moveToPosition(i);
               final String delename= c.getString(c.getColumnIndex(DbHelper.COLUMN_NAME_MEMO_NAME));

                AlertDialog.Builder builder=new AlertDialog.Builder(MyActivity.this);
                builder.setMessage("确定删除此备忘录"+delename+"?");
                builder.setTitle("提示");


                //添加AlertDialog.Builder对象的setPositiveButton()方法
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       dbWrite.delete(DbHelper.TABLE_NAME_MEMOS,
                               DbHelper.COLUMN_NAME_MEMO_NAME+"=?",new String[]{delename});
                        refreshMemosListView();

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

    }

    /**
     * 复写方法，笔记列表中的笔记条目被点击时被调用，打开编辑笔记页面，同事传入当前笔记的信息
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        // 获取当前备忘录条目的Cursor对象
        Cursor c = adapter.getCursor();
        c.moveToPosition(position);

        // 显式Intent开启编辑备忘录页面
        Intent i = new Intent(MyActivity.this, FActivity.class);

        // 传入备忘录id，name，content
        i.putExtra(FActivity.EXTRA_MEMO_ID,
                c.getInt(c.getColumnIndex(DbHelper.COLUMN_NAME_ID)));
        i.putExtra(FActivity.EXTRA_MEMO_NAME,
                c.getString(c.getColumnIndex(DbHelper.COLUMN_NAME_MEMO_NAME)));
        i.putExtra(FActivity.EXTRA_MEMO_CONTENT,
                c.getString(c.getColumnIndex(DbHelper.COLUMN_NAME_MEMO_CONTENT)));

        // 有返回的开启Activity
        startActivityForResult(i, REQUEST_CODE_EDIT_NOTE);

        super.onListItemClick(l, v, position, id);
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with 当被开启的Activity存在并返回结果时调用的方法
     *
     * 当从编辑笔记页面返回时调用，刷新笔记列表
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_CODE_ADD_NOTE:
            case REQUEST_CODE_EDIT_NOTE:
                if (resultCode == Activity.RESULT_OK) {
                    refreshMemosListView();
                }
                break;

            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 刷新备忘录列表，内容从数据库中查询
     */
    public void refreshMemosListView() {
        /**
         * Change the underlying cursor to a new cursor. If there is an existing
         * cursor it will be closed.
         *
         * Parameters: cursor The new cursor to be used
         */
        adapter.changeCursor(dbRead.query(DbHelper.TABLE_NAME_MEMOS, null, null,
                null, null, null, null));

    }


}

