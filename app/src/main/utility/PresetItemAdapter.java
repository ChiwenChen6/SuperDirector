package com.aver.superdirector.utility;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

//import com.aver.superdirector.PresetFragment;
import com.aver.superdirector.R;

import java.util.HashMap;
import java.util.List;

public class PresetItemAdapter extends RecyclerView.Adapter<PresetItemAdapter.ViewHolder> {
    // private String TAG = getClass().getSimpleName();

    private final List<AVerPresetInfo> mList;
    //private IPresetListener mListener;
    private final HashMap<Integer, ViewHolder> mViewHolderList = new HashMap<>();
    private boolean isInit = false;
    private int mLastSelectPosition = -1;
    private boolean mSetPresetEnable = true;

    public PresetItemAdapter(List<AVerPresetInfo> mList) {
        this.mList = mList;
    }

//    public PresetItemAdapter(List<AVerPresetInfo> list, PresetFragment.IPresetListener itemClick) {
//        mList = list;
//        mListener = itemClick;
//    }

    ////////////////
    // Life Cycle //
    ////////////////

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.view_presetitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        mViewHolderList.put(position, holder);
        AVerPresetInfo tInfo = mList.get(position);

        holder.InfoItem = tInfo;

        holder.TxtIdx.setText(String.valueOf(tInfo.Id));
        holder.TxtEdtName.setText(tInfo.Name);

        if (!tInfo.HasImage) {
            holder.ImgCapture.setVisibility(View.INVISIBLE);
            holder.TxtNone.setVisibility(View.VISIBLE);
        } else {
            holder.ImgCapture.setVisibility(View.VISIBLE);
            holder.ImgCapture.setImageBitmap(tInfo.CaptureImage);
            holder.TxtNone.setVisibility(View.INVISIBLE);
        }

        holder.ViewMain.setSelected(tInfo.IsSelected);
        holder.BtnEdit.setEnabled(tInfo.IsSelected);
        if (!tInfo.IsSelected) {
            holder.EdtName.setEnabled(false);
            holder.BtnSet.setVisibility(View.INVISIBLE);
        }else{
            if(holder.InfoItem.HasImage){
                holder.BtnSet.setVisibility(View.INVISIBLE);
            }else {
                if(mLastSelectPosition == position) {
                    mLastSelectPosition = -1;
                    holder.BtnSet.setVisibility(View.INVISIBLE);
                }else{
                    holder.BtnSet.setVisibility(View.VISIBLE);
                }
            }
        }
        if(!mSetPresetEnable){
            holder.BtnSet.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void enablePresetButton(int index, boolean enable){
        if(mSetPresetEnable) {
            mLastSelectPosition = index;
            if (mViewHolderList.containsKey(index)) {
                if (enable) {
                    mLastSelectPosition = index;
                    mViewHolderList.get(index).BtnSet.setVisibility(View.VISIBLE);
                } else {
                    mViewHolderList.get(index).BtnSet.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    public void setPresetEnable(boolean enable){
        mSetPresetEnable = enable;
    }

    public void setInit(){
        if(!isInit) {
            if(mViewHolderList.containsKey(0) && mViewHolderList.get(0) != null) {
                mViewHolderList.get(0).ViewMain.performClick();
                isInit = true;
            }
        }
    }

    //////////////////
    // Class Member //
    //////////////////

    class ViewHolder extends RecyclerView.ViewHolder {
        View ViewMain;
        TextView TxtIdx;
        EditText EdtName;
        TextView TxtEdtName;

        Button BtnEdit;
        ImageView ImgCapture;
        TextView TxtNone;
        Button BtnSet;

        AVerPresetInfo InfoItem;

        ViewHolder(View view) {
            super(view);

            ViewMain = view;
//            view.setOnClickListener(v -> mListener.onItemClick(InfoItem));

            TxtIdx = view.findViewById(R.id.txtIdx);
            EdtName = view.findViewById(R.id.edtName);
            TxtEdtName = view.findViewById(R.id.txedtName);
            EdtName.setSingleLine();
            TxtEdtName.setSingleLine();
            TxtEdtName.setMaxEms(6);
            EdtName.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    InfoItem.Name = EdtName.getText().toString();

                    EdtName.setText("");
                    EdtName.setEnabled(false);

                    TxtEdtName.setEnabled(true);
                    TxtEdtName.setVisibility(View.VISIBLE);
                    TxtEdtName.setText(InfoItem.Name);
                    // hide virtual keyboard
                    InputMethodManager imm
                            = (InputMethodManager) ViewMain.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(EdtName.getWindowToken(), 0);
                    return true;
                }

                return false;
            });
            EdtName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {

                    } else {
                        InputMethodManager imm
                                = (InputMethodManager) ViewMain.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(EdtName.getWindowToken(), 0);
                    }
                }
            });
            BtnEdit = view.findViewById(R.id.btnEdit);
            BtnEdit.setOnClickListener(v -> {
                if (!InfoItem.IsSelected)
                    return;
                TxtEdtName.setEnabled(false);
                TxtEdtName.setVisibility(View.INVISIBLE);
                EdtName.setEnabled(true);
                EdtName.requestFocus();
            });

            ImgCapture = view.findViewById(R.id.imgCapture);
//            ImgCapture.setOnClickListener(v -> mListener.onItemClick(InfoItem));

            TxtNone = view.findViewById(R.id.txtNone);

            BtnSet = view.findViewById(R.id.btnSetPreset);
            BtnSet.setOnClickListener(v -> {
//                mListener.onSetPreset(InfoItem);
            });
        }
    }

}