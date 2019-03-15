/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mukdongjeil.mjchurch.ui.boards;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.data.database.entity.BoardEntity;
import org.mukdongjeil.mjchurch.util.DateUtil;
import org.mukdongjeil.mjchurch.util.OnItemClickListener;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public class BoardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_BOARD = 0;

    private final Context mContext;

    private final OnItemClickListener mOnItemClickListener;
    private List<BoardEntity> mList;

    public BoardAdapter(@NonNull Context context, OnItemClickListener clickHandler) {
        mContext = context;
        mOnItemClickListener = clickHandler;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.board_list_item, viewGroup, false);
        view.setFocusable(true);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BoardEntity entity = mList.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.titleView.setText(entity.getContent());
        viewHolder.titleView.setTag(entity.getId());
        viewHolder.writerView.setText(entity.getWriter().getDisplayName());
        viewHolder.writerView.setTag(position);
        viewHolder.timestampView.setText(DateUtil.convertReadableDateTime(entity.getCreatedAt()));
        viewHolder.likeCountView.setText((mContext.getResources().getString(R.string.like_count, entity.getLikeCount())));
    }

    @Override
    public int getItemCount() {
        if (null == mList) return 0;
        return mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_BOARD;
    }

    public void swapList(final List<BoardEntity> newList) {
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
                    return mList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return mList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
                }
            });

            mList = newList;
            result.dispatchUpdatesTo(this);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView titleView;
        final TextView writerView;
        final TextView likeCountView;
        final TextView timestampView;
        final ImageView avatarView;

        ViewHolder(View view) {
            super(view);
            titleView = view.findViewById(R.id.title);
            writerView = view.findViewById(R.id.writer);
            likeCountView = view.findViewById(R.id.like_count);
            timestampView = view.findViewById(R.id.timestamp);
            avatarView = view.findViewById(R.id.avatar);
            view.setOnClickListener(v -> mOnItemClickListener.onItemClick(v));
        }
    }
}