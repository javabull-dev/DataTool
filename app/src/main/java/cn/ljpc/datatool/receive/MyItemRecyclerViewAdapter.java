package cn.ljpc.datatool.receive;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.ljpc.datatool.R;
import cn.ljpc.datatool.entity.Item;

class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private final List<Item> mItems;
    private final ReceiveFragment.OnListFragmentInteractionListener mListener;

    public MyItemRecyclerViewAdapter(List<Item> items,
                                     ReceiveFragment.OnListFragmentInteractionListener listener) {
        mItems = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Item item = mItems.get(position);
        holder.mItem = item;
        holder.mAddrView.setText("地址：" + item.address);
        holder.mTimeView.setText("时间：" + item.time);
        holder.mTypeView.setText("类型：" + (item.type == 0 ? "file" : "text"));

        //点击item时
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });

        //长按item时
        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            //setOnLongClickListener中return的值决定是否在长按后再加一个短按动作
            //true为不加短按,false为加入短按
            @Override
            public boolean onLongClick(View v) {
                mListener.OnLongClickListener(holder.mItem);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    /**
     * 视图持有者
     * ViewHolder
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        //界面中recycleView中一行的真实的view
        View mView;
        //mView中用来显示ip：port的view
        TextView mAddrView;
        //mView中用来显示接收时间的view
        TextView mTimeView;
        //mView中用来显示接收数据类型的view
        TextView mTypeView;
        //数据
        Item mItem;


        public ViewHolder(View view) {
            super(view);
            mView = view;
            mAddrView = view.findViewById(R.id.item_addr);
            mTimeView = view.findViewById(R.id.item_itme);
            mTypeView = view.findViewById(R.id.item_type);
        }
    }
}
