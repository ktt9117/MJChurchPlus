package org.mukdongjeil.mjchurch.ui.sermon_detail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.data.database.entity.ReplyEntity;
import org.mukdongjeil.mjchurch.data.database.entity.SermonEntity;
import org.mukdongjeil.mjchurch.ui.reply.DefaultReplyAdapter;
import org.mukdongjeil.mjchurch.util.DateUtil;
import org.mukdongjeil.mjchurch.util.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SermonDetailAdapter extends DefaultReplyAdapter {
    private static final String TAG = SermonDetailAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_HEADER = 1;

    private List<ReplyEntity> mList;
    private SermonEntity mHeaderContent;

    public SermonDetailAdapter(@NonNull Context context, OnItemClickListener listener) {
        super(context, listener);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.sermon_detail_header_view, viewGroup, false);
            return new HeaderViewHolder(view);

        } else {
            return super.onCreateViewHolder(viewGroup, viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_HEADER) {
            if (mHeaderContent != null) {
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) viewHolder;
                headerViewHolder.title.setText(mHeaderContent.getTitle());
                headerViewHolder.viewCount.setText(String.format("조회수 %d회", mHeaderContent.getViewCount()));
                headerViewHolder.content.setText(mHeaderContent.getContent());
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

    public void setHeaderContent(SermonEntity content) {
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
        private TextView title, viewCount, content;

        HeaderViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.detail_title);
            viewCount = view.findViewById(R.id.detail_view_count);
            content = view.findViewById(R.id.detail_content);

            view.findViewById(R.id.header_container).setOnClickListener(v -> {
                if (mListener != null) mListener.onItemClick(v);
            });
        }
    }
}