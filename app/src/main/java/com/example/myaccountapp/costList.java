package com.example.myaccountapp;

public class costList {
    private String id;
    private String title;
    private String date;
    private String money;
    private String category; // 分类字段（收入/支出）

    public costList() {
    }

    // 构造方法
    public costList(String id, String title, String date, String money, String category) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.money = money;
        this.category = category;
    }

    // Getter 和 Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // 修改记录的方法
    public void updateRecord(String title, String date, String money, String category) {
        this.title = title;
        this.date = date;
        this.money = money;
        this.category = category;
    }

    // 删除记录的方法（逻辑删除）
    public void deleteRecord() {
        this.id = null;
        this.title = null;
        this.date = null;
        this.money = null;
        this.category = null;
    }

    @Override
    public String toString() {
        return "CostRecord{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", date='" + date + '\'' +
                ", money='" + money + '\'' +
                ", category='" + category + '\'' +
                '}';
    }



}
