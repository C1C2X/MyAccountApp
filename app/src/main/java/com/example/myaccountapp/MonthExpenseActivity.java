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

public class MonthExpenseActivity extends AppCompatActivity {

    private DBHelper helper;
    private TextView tvIncome, tvExpense;
    private Spinner spYear, spMonth;
    private int selectedYear, selectedMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_expense);

        initView();
        loadExpenseData();
    }

    private void initView() {
        helper = new DBHelper(this);
        tvIncome = findViewById(R.id.tv_income);
        tvExpense = findViewById(R.id.tv_expense);
        spYear = findViewById(R.id.sp_year);
        spMonth = findViewById(R.id.sp_month);

        // 设置年份和月份选择器
        setupYearMonthSpinner();

        // 默认选择年份和月份后加载数据
        spYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
                selectedYear = Integer.parseInt(spYear.getSelectedItem().toString());
                loadExpenseData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        spMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
                selectedMonth = position + 1; // 月份从0开始
                loadExpenseData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
    }

    // 设置年份和月份选择器
    private void setupYearMonthSpinner() {
        // 填充年份和月份的数据
        ArrayList<String> years = new ArrayList<>();
        ArrayList<String> months = new ArrayList<>();
        for (int i = 2024; i <= 2030; i++) {
            years.add(String.valueOf(i));
        }

        for (int i = 1; i <= 12; i++) {
            months.add(String.format("%02d", i));
        }

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spYear.setAdapter(yearAdapter);

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMonth.setAdapter(monthAdapter);
    }

    // 加载每月收入和支出数据
    private void loadExpenseData() {
        SQLiteDatabase db = helper.getReadableDatabase();

        // 设置查询条件
        String selection = "Date LIKE ? AND Category=?";
        String[] selectionArgsIncome = {selectedYear + "-" + String.format("%02d", selectedMonth) + "%", "收入"};
        String[] selectionArgsExpense = {selectedYear + "-" + String.format("%02d", selectedMonth) + "%", "支出"};

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
    public void backButton3(View view) {
        // 创建一个Intent，跳转到MainActivity
        Intent intent = new Intent(MonthExpenseActivity.this, MainActivity.class);
        startActivity(intent);
        finish();  // 结束当前Activity，防止用户点击返回按钮后回到此页面
    }
}
