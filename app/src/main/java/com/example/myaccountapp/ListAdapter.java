package com.example.myaccountapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ListAdapter extends BaseAdapter {
    private List<costList> mList; // 数据列表
    private LayoutInflater mLayoutInflater; // 用于加载布局

    // 构造方法
    public ListAdapter(Context context, List<costList> list) {
        mList = list;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList == null ? null : mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = mLayoutInflater.inflate(R.layout.list_item, null);
        // 获取数据
        costList item = mList.get(position);

        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvDate = view.findViewById(R.id.tv_date);
        TextView tvMoney = view.findViewById(R.id.tv_money);
        TextView tvCategory = view.findViewById(R.id.tv_category);

        // 设置数据
        tvTitle.setText(item.getTitle());
        tvDate.setText(item.getDate());
        tvMoney.setText(item.getMoney());
        tvCategory.setText(item.getCategory());

        // 根据分类设置金额的颜色
        if ("收入".equals(item.getCategory())) {
            tvMoney.setTextColor(Color.rgb(0, 255, 0)); // 绿色
        } else if ("支出".equals(item.getCategory())) {
            tvMoney.setTextColor(Color.rgb(255, 0, 0)); // 红色
        }

        return view;
    }


    // ViewHolder模式：减少 findViewById 的调用次数
    private static class ViewHolder {
        TextView tvTitle;
        TextView tvDate;
        TextView tvMoney;
        TextView tvCategory; // 分类信息控件
    }


}
