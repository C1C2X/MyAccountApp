package com.example.myaccountapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EditCostActivity extends AppCompatActivity {

    private EditText etTitle, etMoney;
    private DatePicker ed_date;
    private RadioGroup rgCategory;
    private RadioButton rbIncome, rbExpense;
    private Button btnSave, btnSelectImage, btnDelete;
    private ImageView ivImage;
    private DBHelper dbHelper;
    private String recordId;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_cost);

        initView();

        recordId = getIntent().getStringExtra("record_id");
        if (recordId != null) {
            loadRecordData(recordId);
        }

        btnSave.setOnClickListener(v -> saveChanges());
        btnSelectImage.setOnClickListener(v -> openImagePicker());
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void initView() {
        etTitle = findViewById(R.id.et_title);
        etMoney = findViewById(R.id.et_money);
        ed_date = findViewById(R.id.dp_date);
        rgCategory = findViewById(R.id.rg_category);
        rbIncome = findViewById(R.id.rb_income);
        rbExpense = findViewById(R.id.rb_expense);
        btnSave = findViewById(R.id.btn_save);
        btnSelectImage = findViewById(R.id.btn_select_image);
        btnDelete = findViewById(R.id.btn_delete);
        ivImage = findViewById(R.id.iv_image);
        dbHelper = new DBHelper(this);
    }

    private void loadRecordData(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("account", null, "_id=?", new String[]{id}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int titleIndex = cursor.getColumnIndex("Title");
            int moneyIndex = cursor.getColumnIndex("Money");
            int dateIndex = cursor.getColumnIndex("Date");
            int categoryIndex = cursor.getColumnIndex("Category");
            int photoIndex = cursor.getColumnIndex("Photo");

            if (titleIndex >= 0) etTitle.setText(cursor.getString(titleIndex));
            if (moneyIndex >= 0) etMoney.setText(cursor.getString(moneyIndex));

            if (dateIndex >= 0) {
                String[] dateParts = cursor.getString(dateIndex).split("-");
                if (dateParts.length == 3) {
                    ed_date.updateDate(
                            Integer.parseInt(dateParts[0]),
                            Integer.parseInt(dateParts[1]) - 1,
                            Integer.parseInt(dateParts[2])
                    );
                }
            }

            if (categoryIndex >= 0) {
                String category = cursor.getString(categoryIndex);
                if ("收入".equals(category)) rbIncome.setChecked(true);
                else if ("支出".equals(category)) rbExpense.setChecked(true);
            }

            if (photoIndex >= 0) {
                byte[] imageByteArray = cursor.getBlob(photoIndex);
                if (imageByteArray != null) {
                    ivImage.setImageBitmap(BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length));
                }
            }
        }

        if (cursor != null) cursor.close();
        db.close();
    }

    private void openImagePicker() {
        startActivityForResult(
                new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            imageUri = data.getData();
            ivImage.setImageURI(imageUri);
        }
    }

    private void saveChanges() {
        String title = etTitle.getText().toString().trim();
        String money = etMoney.getText().toString().trim();
        String date = String.format("%d-%02d-%02d", ed_date.getYear(), ed_date.getMonth() + 1, ed_date.getDayOfMonth());
        String category = rbIncome.isChecked() ? "收入" : "支出";

        if (title.isEmpty() || money.isEmpty()) {
            Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] imageByteArray = null;
        if (imageUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                imageByteArray = byteArrayOutputStream.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Title", title);
        values.put("Date", date);
        values.put("Money", money);
        values.put("Category", category);
        if (imageByteArray != null) values.put("Photo", imageByteArray);

        int rows = db.update("account", values, "_id=?", new String[]{recordId});
        db.close();

        if (rows > 0) {
            Toast.makeText(this, "记录更新成功", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "记录更新失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定要删除这条记录吗？")
                .setPositiveButton("确定", (dialog, which) -> deleteRecord())
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteRecord() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete("account", "_id=?", new String[]{recordId});
        db.close();

        if (rows > 0) {
            Toast.makeText(this, "记录已删除", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
        }
    }

    public void backButton2(View view) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}