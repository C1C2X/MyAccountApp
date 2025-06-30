package com.example.myaccountapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class YearExpenseActivity extends AppCompatActivity {

    private DBHelper helper;
    private TextView tvIncome, tvExpense;
    private Spinner spYear;
    private int selectedYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_year_expense); // 关联布局文件

        initView();
        loadExpenseData();
    }

    private void initView() {
        helper = new DBHelper(this);
        tvIncome = findViewById(R.id.tv_income);
        tvExpense = findViewById(R.id.tv_expense);
        spYear = findViewById(R.id.sp_year);

        // 设置年份选择器
        setupYearSpinner();

        // 默认选择年份后加载数据
        spYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
                selectedYear = Integer.parseInt(spYear.getSelectedItem().toString());
                loadExpenseData();  // 加载年度数据
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
    }

    // 设置年份选择器
    private void setupYearSpinner() {
        // 填充年份的数据
        ArrayList<String> years = new ArrayList<>();
        for (int i = 2024; i <= 2030; i++) {
            years.add(String.valueOf(i));
        }

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spYear.setAdapter(yearAdapter);
    }

    // 加载年度收入和支出数据
    private void loadExpenseData() {
        SQLiteDatabase db = helper.getReadableDatabase();

        // 设置查询条件
        String selection = "Date LIKE ? AND Category=?";
        String[] selectionArgsIncome = {selectedYear + "%", "收入"}; // 按年份查询
        String[] selectionArgsExpense = {selectedYear + "%", "支出"}; // 按年份查询

        // 计算收入
        Cursor incomeCursor = db.query("account", new String[]{"SUM(Money) AS Income"}, selection, selectionArgsIncome, null, null, null);
        double income = 0;
        if (incomeCursor.moveToFirst()) {
            income = incomeCursor.getDouble(incomeCursor.getColumnIndex("Income"));
        }
        tvIncome.setText("收入: " + income);

        // 计算支出
        Cursor expenseCursor = db.query("account", new String[]{"SUM(Money) AS Expense"}, selection, selectionArgsExpense, null, null, null);
        double expense = 0;
        if (expenseCursor.moveToFirst()) {
            expense = expenseCursor.getDouble(expenseCursor.getColumnIndex("Expense"));
        }
        tvExpense.setText("支出: " + expense);

        // 关闭游标
        incomeCursor.close();
        expenseCursor.close();
        db.close();
    }

    // 返回按钮点击事件
    public void backButton3(View view) {
        // 创建一个Intent，跳转到MainActivity
        Intent intent = new Intent(YearExpenseActivity.this, MainActivity.class);
        startActivity(intent);
        finish();  // 结束当前Activity，防止用户点击返回按钮后回到此页面
    }
}
