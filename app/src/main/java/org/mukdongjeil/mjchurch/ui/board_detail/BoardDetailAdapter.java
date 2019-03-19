package org.mukdongjeil.mjchurch.ui.board_detail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.data.database.entity.BoardEntity;
import org.mukdongjeil.mjchurch.data.database.entity.ReplyEntity;
import org.mukdongjeil.mjchurch.ui.reply.DefaultReplyAdapter;
import org.mukdongjeil.mjchurch.util.DateUtil;
import org.mukdongjeil.mjchurch.util.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BoardDetailAdapter extends DefaultReplyAdapter {
    private static final String TAG = BoardDetailAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_HEADER = 1;

    private List<ReplyEntity> mList;
    private BoardEntity mHeaderContent;

    public BoardDetailAdapter(@NonNull Context context, OnItemClickListener listener) {
        super(context, listener);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.board_detail_header_view, viewGroup, false);
            return new HeaderViewHolder(view);

        } else {
            return super.onCreateViewHolder(viewGroup, viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_HEADER) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) viewHolder;
            if (mHeaderContent != null) {
                headerViewHolder.contentView.setText(mHeaderContent.getContent());
                headerViewHolder.timestampView.setText(
                        DateUtil.convertReadableDateTime(mHeaderContent.getCreatedAt()));
                headerViewHolder.writerView.setText(mHeaderContent.getWriter().getDisplayName());
                headerViewHolder.likeCount.setText
                        (mContext.getResources().getString(R.string.like_count, mHeaderContent.getLikeCount()));

                if (mSignedUp && mUser.getDisplayName().equals(mHeaderContent.getWriter().getDisplayName())) {
                    //TODO: to enable edit, remove this posts
                }
                //TODO: feature - display avatar image on avatarView using entity.getAvatarUri()
//                if (headerContent.getWriter() != null && !TextUtils.isEmpty(headerContent.getWriter().getAvatarPath()) == false) {
//                    String avatarPath = headerContent.getWriter().getAvatarPath();
//                    Log.e(TAG, "avatarPath : " + avatarPath);
//                    Glide.with(mContext)
//                            .load(avatarPath)
//                            .into(headerViewHolder.avatarView)
//                            .onLoadFailed(mContext.getResources().getDrawable(R.drawable.ic_sentiment_very_satisfied_48dp));
//                }
            }
        } else {
            ReplyViewHolder replyViewHolder = (ReplyViewHolder) viewHolder;
            ReplyEntity entity = mList.get(position - 1);
            replyViewHolder.contentView.setText(entity.getContent());
            replyViewHolder.writerView.setText(entity.getWriter().getDisplayName());
            replyViewHolder.dateView.setText(DateUtil.convertReadableDateTime(entity.getCreatedAt()));
            if (mSignedUp && mUser.getDisplayName().equals(entity.getWriter().getDisplayName())) {
                replyViewHolder.moreView.setVisibility(View.VISIBLE);
            } else {
                replyViewHolder.moreView.setVisibility(View.GONE);
            }

            //TODO: feature - display avatar image on avatarView using entity.getAvatarUri()
            //            if (entity.getWriter() != null && !TextUtils.isEmpty(entity.getWriter().getAvatarPath()) == false) {
            //                String avatarPath = entity.getWriter().getAvatarPath();
            //                Log.e(TAG, "avatarPath : " + avatarPath);
            //                Glide.with(mContext)
            //                        .load(avatarPath)
            //                        .into(replyViewHolder.avatarView)
            //                        .onLoadFailed(mContext.getResources().getDrawable(R.drawable.ic_sentiment_very_satisfied_48dp));
            //            }
        }
    }

    @Override
    public int getItemViewType(int position) {
       if (position == 0) {
           return VIEW_TYPE_HEADER;
       } else {
           return VIEW_TYPE_ITEM;
       }
    }

    @Override
    public int getItemCount() {
        return (null == mList) ? 0 : mList.size() + 1;
    }

    public void setHeaderContent(BoardEntity content) {
        mHeaderContent = content;
        if (getItemCount() == 0) {
            mList = new ArrayList<>();
        }

        notifyItemRangeInserted(0, 1);
    }

    public void swapList(final List<ReplyEntity> newList) {
        if (mList == null) {
            mList = new ArrayList<>();
        } else {
            mList.clear();
        }

        mList.addAll(newList);
        notifyDataSetChanged();
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        final TextView contentView, timestampView, writerView;
        final ImageView avatarView;
        final MaterialButton btnLike;
        final TextView likeCount;

        HeaderViewHolder(View view) {
            super(view);
            btnLike = view.findViewById(R.id.btn_like);
            likeCount = view.findViewById(R.id.txt_like_count);
            timestampView = view.findViewById(R.id.timestamp);
            writerView = view.findViewById(R.id.writer);
            avatarView = view.findViewById(R.id.avatar);
            contentView = view.findViewById(R.id.content);
            view.findViewById(R.id.container_view).setOnClickListener(v -> {
                if (mListener != null) mListener.onItemClick(v);
            });

        }
    }
}