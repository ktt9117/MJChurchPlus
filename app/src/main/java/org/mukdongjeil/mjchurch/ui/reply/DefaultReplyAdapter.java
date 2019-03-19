package org.mukdongjeil.mjchurch.ui.reply;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.data.database.entity.ReplyEntity;
import org.mukdongjeil.mjchurch.util.DateUtil;
import org.mukdongjeil.mjchurch.util.OnItemClickListener;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public class DefaultReplyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = DefaultReplyAdapter.class.getSimpleName();

    protected final Context mContext;
    protected OnItemClickListener mListener;
    private List<ReplyEntity> mList;
    protected FirebaseUser mUser;
    protected boolean mSignedUp;

    public DefaultReplyAdapter(@NonNull Context context, OnItemClickListener listener) {
        mContext = context;
        mListener = listener;
        setupUser();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.reply_list_item, viewGroup, false);
        return new ReplyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        ReplyViewHolder replyViewHolder = (ReplyViewHolder) viewHolder;
        ReplyEntity entity = mList.get(position);
        replyViewHolder.contentView.setText(entity.getContent());
        replyViewHolder.writerView.setText(entity.getWriter().getDisplayName());
        replyViewHolder.dateView.setText(DateUtil.convertReadableDateTime(entity.getCreatedAt()));
        if (mSignedUp && mUser.getDisplayName().equals(entity.getWriter().getDisplayName())) {
            replyViewHolder.moreView.setVisibility(View.VISIBLE);
        } else {
            replyViewHolder.moreView.setVisibility(View.GONE);
        }

        // TODO: feature - display avatar image on avatarView using entity.getAvatarUri()
//        if (entity.getWriter() != null && !TextUtils.isEmpty(entity.getWriter().getAvatarPath()) == false) {
//            String avatarPath = entity.getWriter().getAvatarPath();
//            Log.e(TAG, "avatarPath : " + avatarPath);
//            Glide.with(mContext)
//                    .load(avatarPath)
//                    .into(replyViewHolder.avatarView)
//                    .onLoadFailed(mContext.getResources().getDrawable(R.drawable.ic_sentiment_very_satisfied_48dp));
//        }
    }

    @Override
    public int getItemCount() {
        return (null == mList) ? 0 : mList.size();
    }

    public void swapList(final List<ReplyEntity> newList) {
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
            result.dispatchUpdatesTo(DefaultReplyAdapter.this);
        }
    }

    private void setupUser() {
        if (FirebaseAuth.getInstance() != null) {
            mUser = FirebaseAuth.getInstance().getCurrentUser();
        }

        mSignedUp = (mUser == null) ? false : true;
    }

    public class ReplyViewHolder extends RecyclerView.ViewHolder {
        public final TextView contentView, writerView, dateView;
        public final ImageView avatarView, moreView;

        ReplyViewHolder(View view) {
            super(view);
            contentView = view.findViewById(R.id.reply_content);
            writerView = view.findViewById(R.id.reply_writer);
            dateView = view.findViewById(R.id.reply_date);
            avatarView = view.findViewById(R.id.reply_avatar);
            moreView = view.findViewById(R.id.btn_more);
            moreView.setOnClickListener(v -> {
                if (mListener != null) mListener.onItemClick(v);
            });

            view.findViewById(R.id.reply_container_view).setOnClickListener(v -> {
                if (mListener != null) mListener.onItemClick(v);
            });
        }
    }
}