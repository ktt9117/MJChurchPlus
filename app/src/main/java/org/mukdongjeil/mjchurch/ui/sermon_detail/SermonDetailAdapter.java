package org.mukdongjeil.mjchurch.ui.sermon_detail;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.data.database.entity.SermonReplyEntity;
import org.mukdongjeil.mjchurch.util.DateUtil;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public class SermonDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = SermonDetailAdapter.class.getSimpleName();

    private final Context mContext;
    private List<SermonReplyEntity> mList;

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
        //Log.e(TAG, "onBindViewHolder position : " + position);
        ReplyViewHolder replyViewHolder = (ReplyViewHolder) viewHolder;
        SermonReplyEntity entity = mList.get(position);
        replyViewHolder.contentView.setText(entity.getContent());
        replyViewHolder.writerView.setText(entity.getWriter().getDisplayName());
        replyViewHolder.dateView.setText(DateUtil.convertReadableDateTime(entity.getCreatedAt()));
        // TODO: feature - display avatar image on avatarView using entity.getAvatarUri()
//        if (entity.getWriter() != null && TextUtils.isEmpty(entity.getWriter().getAvatarPath()) == false) {
//            String avatarPath = entity.getWriter().getAvatarPath();
//            Log.e(TAG, "avatarPath : " + avatarPath);
//            Glide.with(mContext)
//                    .load(avatarPath)
//                    .into(replyViewHolder.avatarView)
//                    .onLoadFailed(mContext.getResources().getDrawable(R.mipmap.ic_launcher_round));
//        }
    }

    @Override
    public int getItemCount() {
        return (null == mList) ? 0 : mList.size();
    }

    void swapList(final List<SermonReplyEntity> newList) {
        if (mList == null) {
            mList = newList;
            notifyItemRangeInserted(0, newList.size());

        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mList.size();
                }

                @Override
                public int getNewListSize() {
                    return newList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mList.get(oldItemPosition).getDocumentId()
                            == newList.get(newItemPosition).getDocumentId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return mList.get(oldItemPosition).getContent()
                            == newList.get(newItemPosition).getContent();
                }
            });

            mList.clear();
            mList.addAll(newList);
            result.dispatchUpdatesTo(SermonDetailAdapter.this);
        }
    }

    class ReplyViewHolder extends RecyclerView.ViewHolder {
        final TextView contentView, writerView, dateView;
        final ImageView avatarView;

        ReplyViewHolder(View view) {
            super(view);
            contentView = view.findViewById(R.id.reply_content);
            writerView = view.findViewById(R.id.reply_writer);
            dateView = view.findViewById(R.id.reply_date);
            avatarView = view.findViewById(R.id.reply_avatar);
        }
    }
}