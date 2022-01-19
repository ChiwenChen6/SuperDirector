package com.aver.superdirector.utility;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import com.aver.superdirector.R;

public class AccountInfoAdapter extends
        RecyclerView.Adapter<AccountInfoAdapter.ViewHolder> {
    private List<AVerAccountInfo> mList;
    private final ItemActionListener mListener;
    private boolean mIsDelete = false;

    public interface ItemActionListener {
        void onEditClick(AVerAccountInfo info);
        void onDeleteClick(AVerAccountInfo info);
        void onFavoriteClick(AVerAccountInfo info);
        void onItemLongClick(AVerAccountInfo info);
        void onItemClick(AVerAccountInfo info);
    }

    public AccountInfoAdapter(List<AVerAccountInfo> list, ItemActionListener listener) {
        mList = list;
        mListener = listener;
    }

    public void setAccountList(List<AVerAccountInfo> list) {
        mList = list;
        notifyDataSetChanged();
    }

    public void setDeleteMode(boolean enable){
        mIsDelete = enable;
        notifyDataSetChanged();
    }

    public boolean getDeleteMode(){
        return mIsDelete;
    }

    ////////////////
    // Life Cycle //
    ////////////////

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View tView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.view_accountinfo,
                parent,
                false);
        return (new ViewHolder(tView));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AVerAccountInfo tInfo = mList.get(position);

        holder.InfoItem = tInfo;
        holder.TxtName.setText(tInfo.AccountName);
        holder.TxtRemote.setText(tInfo.RemoteUri);
        holder.BtnFavorite.setSelected(tInfo.IsFavorite);
        if(mIsDelete){
            holder.BtnEdit.setVisibility(View.GONE);
            holder.BtnFavorite.setVisibility(View.GONE);
            holder.BtnDelete.setVisibility(View.VISIBLE);
        }else{
            holder.BtnEdit.setVisibility(View.VISIBLE);
            holder.BtnFavorite.setVisibility(View.VISIBLE);
            holder.BtnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    //////////////////
    // Class Member //
    //////////////////

    class ViewHolder extends RecyclerView.ViewHolder {
        View ViewMain;
        TextView TxtName;
        TextView TxtRemote;
        Button BtnEdit;
        Button BtnDelete;
        Button BtnFavorite;

        AVerAccountInfo InfoItem;

        ViewHolder(View view) {
            super(view);
            ViewMain = view;
            ViewMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!mIsDelete) {
                        mListener.onItemClick(InfoItem);
                    }
                }
            });

            ViewMain.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mListener.onItemLongClick(InfoItem);
                    return false;
                }
            });

            TxtName = view.findViewById(R.id.txtName);
            TxtRemote = view.findViewById(R.id.txtRemote);

            BtnEdit = view.findViewById(R.id.btnEdit);
            BtnEdit.setOnClickListener(v -> {
                mListener.onEditClick(InfoItem);
            });

            BtnDelete = view.findViewById(R.id.btnDelete);
            BtnDelete.setOnClickListener(v -> {
                mListener.onDeleteClick(InfoItem);
            });

            BtnFavorite = view.findViewById(R.id.btnFavorite);
            BtnFavorite.setOnClickListener(v -> {
                        InfoItem.IsFavorite = !InfoItem.IsFavorite;
                        BtnFavorite.setSelected(InfoItem.IsFavorite);
                        mListener.onFavoriteClick(InfoItem);
                    }
            );
        }
    }

}