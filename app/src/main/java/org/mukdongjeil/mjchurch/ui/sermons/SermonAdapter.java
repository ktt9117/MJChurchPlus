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
package org.mukdongjeil.mjchurch.ui.sermons;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.data.database.entity.SermonEntity;
import org.mukdongjeil.mjchurch.util.CommonUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public class SermonAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SERMON = 0;

    private final Context mContext;

    private final SermonAdapterOnItemClickHandler mClickHandler;
    private List<SermonEntity> mSermonList;

    public SermonAdapter(@NonNull Context context, SermonAdapterOnItemClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.sermon_list_item, viewGroup, false);
        view.setFocusable(true);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SermonEntity currentSermon = mSermonList.get(position);

        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.titleView.setText(currentSermon.getTitle());
        viewHolder.viewCountView.setText(String.format("조회수 %d회", currentSermon.getViewCount()));

        String thumbnailUrl = CommonUtils.getYoutubeThumbnailUrl(currentSermon.getVideoUrl());
        Glide.with(mContext)
                .load(thumbnailUrl)
                .into(viewHolder.thumbnailView);
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our sermon
     */
    @Override
    public int getItemCount() {
        if (null == mSermonList) return 0;
        return mSermonList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_SERMON;
    }

    public void swapList(final List<SermonEntity> newList) {
        if (mSermonList == null) {
            mSermonList = newList;
            notifyItemRangeInserted(0, newList.size());

        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mSermonList.size();
                }

                @Override
                public int getNewListSize() {
                    return newList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mSermonList.get(oldItemPosition).getBbsNo() == newList.get(newItemPosition).getBbsNo();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    SermonEntity newOne = newList.get(newItemPosition);
                    SermonEntity oldOne = mSermonList.get(oldItemPosition);
                    return newOne.getBbsNo() == oldOne.getBbsNo()
                            && newOne.getDate().equals(oldOne.getDate());
                }
            });

            mSermonList = newList;
            result.dispatchUpdatesTo(this);
        }
    }

    /**
     * The interface that receives onItemClick messages.
     */
    public interface SermonAdapterOnItemClickHandler {
        void onItemClick(View v, int bbsNo);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView thumbnailView;
        final TextView titleView;
        final TextView viewCountView;

        ViewHolder(View view) {
            super(view);
            thumbnailView = view.findViewById(R.id.thumbnail);
            titleView = view.findViewById(R.id.title);
            viewCountView = view.findViewById(R.id.view_count);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mClickHandler.onItemClick(v, mSermonList.get(adapterPosition).getBbsNo());
        }
    }
}