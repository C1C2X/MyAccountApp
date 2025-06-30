package com.example.myaccountapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DBHelper helper;
    private ListView listView;
    private ImageButton addButton;
    private List<costList> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();

        // 设置列表点击和长按事件
        setupListViewListeners();
    }

    // 初始化控件
    private void initView() {
        helper = new DBHelper(MainActivity.this);
        listView = findViewById(R.id.list_view);
        addButton = findViewById(R.id.add);
    }

    // 初始化数据
    private void initData() {
        list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("account", null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            costList clist = new costList();
            clist.setId(cursor.getString(cursor.getColumnIndex("_id")));
            clist.setTitle(cursor.getString(cursor.getColumnIndex("Title")));
            clist.setDate(cursor.getString(cursor.getColumnIndex("Date")));
            clist.setMoney(cursor.getString(cursor.getColumnIndex("Money")));
            clist.setCategory(cursor.getString(cursor.getColumnIndex("Category"))); // 读取分类
            list.add(clist);
        }

        // 绑定适配器
        listView.setAdapter(new ListAdapter(this, list));
        db.close();
    }

    // 跳转到添加新记录页面
    public void addAccount(View view) {
        Intent intent = new Intent(MainActivity.this, new_cost.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 1) {
            // 重新加载数据
            initData();
        }
    }

    // 设置列表点击和长按事件
    private void setupListViewListeners() {
        // 长按删除记录
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDeleteDialog(position);
                return true;
            }
        });

        // 点击修改记录
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editRecord(position);
            }
        });
    }

    // 显示删除确认对话框
    private void showDeleteDialog(final int position) {
        new AlertDialog.Builder(this)
                .setTitle("删除记录")
                .setMessage("确定要删除这条记录吗？")
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteRecord(position);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // 删除记录
    private void deleteRecord(int position) {
        costList record = list.get(position);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete("account", "_id=?", new String[]{record.getId()});
        db.close();

        // 刷新列表
        list.remove(position);
        ((ListAdapter) listView.getAdapter()).notifyDataSetChanged();
    }

    // 编辑记录
    private void editRecord(int position) {
        costList record = list.get(position);
        Intent intent = new Intent(MainActivity.this, EditCostActivity.class);
        intent.putExtra("record_id", record.getId());
        startActivityForResult(intent, 2);
    }
    public void goToMonthExpenseActivity(View view) {
        Intent intent = new Intent(MainActivity.this, MonthExpenseActivity.class);
        startActivity(intent);
    }
    public void goToYearExpenseActivity(View view) {
        // 创建 Intent 跳转到 YearExpenseActivity
        Intent intent = new Intent(MainActivity.this, YearExpenseActivity.class);
        startActivity(intent);
    }

    private static final String TAG = "MainActivity";


    // 跳转到折线图分析界面
    public void goToLineAnalysis(View view) {
        try {
            Log.d(TAG, "准备跳转到数据分析界面");
            Intent intent = new Intent(this, DataAnalysisActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "跳转失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
