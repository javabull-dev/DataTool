package cn.ljpc.datatool.entity;

import java.util.ArrayList;

public class Item {

    /**
     * ip地址
     */
    public String address;

    /**
     * 接收时间
     */
    public String time;

    /**
     * 信息类型
     * type==0:file
     * type==1:message
     */
    public int type;

    /**
     * 信息
     */
    public String message;

    /**
     * 文件名
     */
    public String filename;

    /**
     * 文件路径
     */
    public String filepath;

    public static final ArrayList<Item> mItems = new ArrayList<>(10);

    /**
     * 在数组的末尾添加一个数据
     */
    public static void add(Item item) {
        synchronized (mItems) {
            mItems.add(item);
        }
    }

    /**
     * 清除所有数据
     */
    public static void clear() {
        synchronized (mItems) {
            mItems.clear();
        }
    }

    /**
     * 移除最后一个数据
     */
    public static void remove() {
        synchronized (mItems) {
            if (mItems.size() > 0) {
                mItems.remove(mItems.size() - 1);
            }
        }
    }

    /**
     * 移除指定索引的数据
     */
    public static void remove(int index) {
        synchronized (mItems) {
            if (index >= 0 && index < mItems.size()) {
                mItems.remove(index);
            }
        }
    }

    /**
     * 删除指定数据
     */
    public static void remove(Item item) {
        synchronized (mItems) {
            if (item != null) {
                mItems.remove(item);
            }
        }
    }
}
