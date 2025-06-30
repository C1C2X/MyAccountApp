package com.example.myaccountapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class new_cost extends AppCompatActivity {
    private DBHelper helper;
    private EditText et_cost_title;
    private EditText et_cost_money;
    private DatePicker dp_cost_date;
    private RadioGroup rg_cost_category;
    private RadioButton rb_income, rb_expense;
    private ImageView iv_uploaded_photo;
    private Uri imageUri;

    private static final int REQUEST_CODE_CAMERA = 1;
    private static final int REQUEST_CODE_GALLERY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_cost);
        initView();

        // 检查并请求权限
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA},
                    100);
        }
    }

    private void initView() {
        helper = new DBHelper(new_cost.this);
        et_cost_title = findViewById(R.id.et_cost_title);
        et_cost_money = findViewById(R.id.et_cost_money);
        dp_cost_date = findViewById(R.id.dp_cost_date);
        rg_cost_category = findViewById(R.id.rg_cost_category);
        rb_income = findViewById(R.id.rb_income);
        rb_expense = findViewById(R.id.rb_expense);
        iv_uploaded_photo = findViewById(R.id.iv_uploaded_photo);
    }

    public void okButton(View view) {
        String titleStr = et_cost_title.getText().toString().trim();
        String moneyStr = et_cost_money.getText().toString().trim();
        String dateStr = String.format("%d-%02d-%02d",
                dp_cost_date.getYear(),
                dp_cost_date.getMonth() + 1,  // 月份从0开始，需+1
                dp_cost_date.getDayOfMonth());
        int selectedCategoryId = rg_cost_category.getCheckedRadioButtonId();

        // 验证是否选择了分类
        if (selectedCategoryId == -1) {
            Toast toast = Toast.makeText(this, "请选择收入或支出", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        String categoryStr = (selectedCategoryId == R.id.rb_income) ? "收入" : "支出";

        if ("".equals(moneyStr)) { // 金额不能为空
            Toast toast = Toast.makeText(this, "请填写金额", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("Title", titleStr);
            values.put("Money", moneyStr);
            values.put("Date", dateStr);
            values.put("Category", categoryStr); // 保存分类

            // 获取照片字节数据
            byte[] imageBytes = imageToByteArray(imageUri);
            if (imageBytes != null) {
                values.put("Photo", imageBytes); // 保存图片
            }

            long account = db.insert("account", null, values);
            if (account > 0) {
                Toast toast = Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                setResult(1);
                finish();
            } else {
                Toast toast = Toast.makeText(this, "保存失败，请重试", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
            db.close();
        }
    }

    public void backButton(View view) {
        Intent intent = new Intent(new_cost.this, MainActivity.class);
        startActivity(intent);
        finish();  // 结束当前Activity，防止用户点击返回按钮后回到此页面
    }

    // 上传照片的功能
    public void uploadPhoto(View view) {
        // 弹出选择框，选择相册
        String[] options = {"从相册选择"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择图片来源");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    // 打开相册
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, REQUEST_CODE_GALLERY);
                }
            }
        });
        builder.show();
    }


    // 处理返回的图片
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) {
                iv_uploaded_photo.setImageURI(imageUri);
            } else if (requestCode == REQUEST_CODE_GALLERY) {
                // 从相册返回
                Uri selectedImage = data.getData();
                iv_uploaded_photo.setImageURI(selectedImage);
                imageUri = selectedImage; // 记录选中的图片 URI
            }
        }
    }

    // 将图片转换为字节数组
    private byte[] imageToByteArray(Uri imageUri) {
        if (imageUri == null) {
            return null;
        }
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int read;
            byte[] buffer = new byte[1024];
            while ((read = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, read);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
