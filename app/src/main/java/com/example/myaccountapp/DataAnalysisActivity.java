package com.example.myaccountapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DataAnalysisActivity extends AppCompatActivity {

    private DBHelper helper;
    private Spinner spYear;
    private BarChart barChart;
    private int selectedYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_analysis);

        initView();
        setupYearSpinner();
        loadMonthlyData();
    }

    private void initView() {
        helper = new DBHelper(this);
        spYear = findViewById(R.id.sp_year);
        barChart = findViewById(R.id.bar_chart);
    }

    private void setupYearSpinner() {
        List<String> years = new ArrayList<>();
        for (int i = 2024; i <= 2030; i++) {
            years.add(String.valueOf(i));
        }

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spYear.setAdapter(yearAdapter);

        spYear.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parentView, android.view.View view, int position, long id) {
                selectedYear = Integer.parseInt(spYear.getSelectedItem().toString());
                loadMonthlyData();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parentView) {}
        });
    }

    private void loadMonthlyData() {
        if (spYear.getSelectedItem() == null) {
            Log.e("DataAnalysis", "No year selected.");
            return;
        }
        int year = 0;
        try {
            year = Integer.parseInt(spYear.getSelectedItem().toString());
        } catch (NullPointerException | NumberFormatException e) {
            Log.e("DataAnalysis", "Error parsing year: " + e.getMessage());
            return;
        }

        SQLiteDatabase db = null;
        try {
            db = helper.getReadableDatabase();

            List<BarEntry> incomeEntries = new ArrayList<>();
            List<BarEntry> expenseEntries = new ArrayList<>();
            double maxValue = 0;

            for (int month = 1; month <= 12; month++) {
                String selection = "Date LIKE ? AND Category = ?";
                String[] selectionArgsIncome = {String.format(Locale.US, "%d-%02d", year, month) + "%", "收入"};
                String[] selectionArgsExpense = {String.format(Locale.US, "%d-%02d", year, month) + "%", "支出"};

                Log.d("DataAnalysis", "Income Query - Selection: " + selection + ", Args: " + String.join(", ", selectionArgsIncome));
                double income = querySum(db, selection, selectionArgsIncome);
                Log.d("DataAnalysis", "Income for Year " + year + " Month " + month + ": " + income);
                if (income > 0) {
                    incomeEntries.add(new BarEntry(month - 1, (float) income));
                }

                Log.d("DataAnalysis", "Expense Query - Selection: " + selection + ", Args: " + String.join(", ", selectionArgsExpense));
                double expense = querySum(db, selection, selectionArgsExpense);
                Log.d("DataAnalysis", "Expense for Year " + year + " Month " + month + ": " + expense);
                if (expense > 0) {
                    expenseEntries.add(new BarEntry(month - 1, (float) expense));
                }

                maxValue = Math.max(maxValue, Math.max(income, expense));
            }

            Log.d("DataAnalysis", "Income entries size: " + incomeEntries.size());
            Log.d("DataAnalysis", "Expense entries size: " + expenseEntries.size());

            if (incomeEntries.isEmpty() && expenseEntries.isEmpty()) {
                Log.e("DataAnalysis", "No data available for year " + year);
                barChart.clear();
                barChart.setNoDataText("暂无 " + year + " 年的收支数据");
                barChart.invalidate();
                return;
            }

            float yAxisMax = (float) (maxValue * 1.1);
            if (yAxisMax < 100) {
                yAxisMax = 100;
            }

            BarData barData = createBarData(incomeEntries, expenseEntries);
            barChart.setData(barData);

            // 设置 X 轴显示月份
            XAxis xAxis = barChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            final String[] months = new String[]{"1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"};
            xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
            xAxis.setTextSize(8f);
            xAxis.setGranularity(1f);
            xAxis.setLabelCount(12, true);
            xAxis.setAxisMinimum(-0.5f);
            xAxis.setAxisMaximum(11.5f);

            // 设置 Y 轴
            YAxis leftYAxis = barChart.getAxisLeft();
            leftYAxis.setAxisMinimum(0f);
            leftYAxis.setAxisMaximum(yAxisMax);
            leftYAxis.setLabelCount(20, true);
            leftYAxis.setTextSize(8f);

            YAxis rightYAxis = barChart.getAxisRight();
            rightYAxis.setEnabled(false);

            // 设置图例
            Legend legend = barChart.getLegend();
            legend.setTextSize(8f);
            legend.setFormSize(8f);
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
            legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            legend.setDrawInside(false);

            barChart.getDescription().setEnabled(false);
            barChart.setTouchEnabled(false);
            barChart.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DataAnalysis", "Error loading monthly data: " + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    private double querySum(SQLiteDatabase db, String selection, String[] selectionArgs) {
        String query = "SELECT SUM(COALESCE(CAST(Money AS REAL), 0)) AS Total FROM account WHERE " + selection;
        Log.d("DataAnalysis", "Query SQL: " + query);
        Cursor cursor = db.rawQuery(query, selectionArgs);
        double sum = 0;
        if (cursor.moveToFirst()) {
            int sumIndex = cursor.getColumnIndex("Total");
            if (sumIndex != -1) {
                sum = cursor.getDouble(sumIndex);
            }
            Log.d("DataAnalysis", "Query result: " + sum);
        } else {
            Log.d("DataAnalysis", "No data found for the query.");
        }
        cursor.close();
        return sum;
    }

    private BarData createBarData(List<BarEntry> incomeEntries, List<BarEntry> expenseEntries) {
        BarDataSet incomeDataSet = new BarDataSet(incomeEntries, "收入");
        incomeDataSet.setColor(android.graphics.Color.GREEN);

        BarDataSet expenseDataSet = new BarDataSet(expenseEntries, "支出");
        expenseDataSet.setColor(android.graphics.Color.RED);

        float barWidth = 0.35f;
        float groupSpace = 0.1f;
        float barSpace = 0.02f;

        BarData barData = new BarData(incomeDataSet, expenseDataSet);
        barData.setBarWidth(barWidth);
        if (!incomeEntries.isEmpty() && !expenseEntries.isEmpty()) {
            barChart.groupBars(0f, groupSpace, barSpace);
        }

        return barData;
    }

    public void backButton(View view) {
        finish();
    }
}