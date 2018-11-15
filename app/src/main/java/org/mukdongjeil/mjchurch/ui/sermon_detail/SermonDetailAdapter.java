package org.mukdongjeil.mjchurch.ui.sermon_detail;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.data.database.entity.SermonReplyEntity;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public class SermonDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = SermonDetailAdapter.class.getSimpleName();

    private final Context mContext;
    private List<SermonReplyEntity> mSermonReplyList;

    SermonDetailAdapter(@NonNull Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.sermon_reply_list_item, viewGroup, false);
        view.setFocusable(true);
        return new ReplyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        Log.e(TAG, "onBindViewHolder called");
        ReplyViewHolder replyViewHolder = (ReplyViewHolder) viewHolder;
        replyViewHolder.content.setText(mSermonReplyList.get(position-1).getContent());

    }

    @Override
    public int getItemCount() {
        return (null == mSermonReplyList) ? 0 : mSermonReplyList.size();
    }

    void swapList(final List<SermonReplyEntity> newList) {
        if (mSermonReplyList == null) {
            mSermonReplyList = newList;
            notifyDataSetChanged();
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mSermonReplyList.size();
                }

                @Override
                public int getNewListSize() {
                    return mSermonReplyList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mSermonReplyList.get(oldItemPosition).getBbsNo() == newList.get(newItemPosition).getBbsNo();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    SermonReplyEntity newOne = newList.get(newItemPosition);
                    SermonReplyEntity oldOne = mSermonReplyList.get(oldItemPosition);
                    return newOne.getBbsNo() == oldOne.getBbsNo()
                            && newOne.getDate().equals(oldOne.getDate());
                }
            });

            mSermonReplyList = newList;
            result.dispatchUpdatesTo(this);
        }
    }

    class ReplyViewHolder extends RecyclerView.ViewHolder {
        final TextView content;

        ReplyViewHolder(View view) {
            super(view);
            content = view.findViewById(R.id.reply_content);
        }
    }
}
