/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2017 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catrobat.catroid.ui.recyclerview.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.images.WebImage;
import com.squareup.picasso.Picasso;

import org.catrobat.catroid.R;
import org.catrobat.catroid.common.ScratchProgramData;
import org.catrobat.catroid.scratchconverter.protocol.Job;
import org.catrobat.catroid.ui.recyclerview.viewholder.ScratchJobVH;
import org.catrobat.catroid.ui.recyclerview.viewholder.ViewHolder;
import org.catrobat.catroid.utils.Utils;

import java.util.List;
import java.util.Locale;

public class ScratchJobAdapter extends RVAdapter<Job> implements RVAdapter.OnItemClickListener<Job> {
	private static final String TAG = ScratchRemixedProgramAdapter.class.getSimpleName();

	private OnItemClickListener<Job> scratchJobEditListener;


	private static LayoutInflater inflater;

	public ScratchJobAdapter(Context context, List<Job> objects) {
		super(objects);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Log.d(TAG, "Number of remixes: " + objects.size());
	}

	public void setScratchJobEditListener(OnItemClickListener<Job> listener) {
		scratchJobEditListener = listener;
	}

	@Override
	public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_scratch_job_list_item, parent, false);
		return new ScratchJobVH(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		onBindViewHolder((ScratchJobVH) holder, position);
	}

	public void onBindViewHolder(final ScratchJobVH holder, int position) {
		final Job item = items.get(position);

		holder.title.setText(item.getTitle());
		if (item.getImage().getUrl() != null) {
			final int height = holder.image.getContext().getResources().getDimensionPixelSize(R.dimen
					.scratch_project_thumbnail_height);
			final String originalImageURL = item.getImage().getUrl().toString();
			// load image but only thumnail!
			// in order to download only thumbnail version of the original image
			// we have to reduce the image size in the URL
			final String thumbnailImageURL = Utils.changeSizeOfScratchImageURL(originalImageURL, height);
			Picasso.with(holder.image.getContext()).load(thumbnailImageURL).into(holder.image);
		} else {
			// clear old image of other program if this is a reused view element
			holder.image.setImageBitmap(null);
		}

		// set name of project:
		holder.title.setSingleLine(true);

		// set status of project:
		holder.status.setTextColor(Color.WHITE);
		holder.details.setVisibility(View.VISIBLE);

		short progress = 0;
		boolean showProgressBar = false;
		switch (item.getState()) {
			case UNSCHEDULED:
				holder.status.setText("-");
				break;
			case SCHEDULED:
				holder.status.setText(holder.status.getContext().getString(R.string.status_scheduled));
				showProgressBar = true;
				break;
			case READY:
				holder.status.setText(holder.status.getContext().getString(R.string.status_waiting_for_worker));
				showProgressBar = true;
				break;
			case RUNNING:
				holder.status.setText(holder.status.getContext().getString(R.string.status_started));
				showProgressBar = true;
				progress = item.getProgress();
				break;
			case FINISHED:
				int messageID;
				switch (item.getDownloadState()) {
					case DOWNLOADING:
						messageID = R.string.status_downloading;
						progress = item.getDownloadProgress();
						showProgressBar = true;
						break;
					case DOWNLOADED:
						messageID = R.string.status_download_finished;
						break;
					default:
						messageID = R.string.status_conversion_finished;
				}
				holder.status.setText(holder.status.getContext().getString(messageID));
				break;
			case FAILED:
				holder.status.setText(R.string.status_conversion_failed);
				holder.status.setTextColor(Color.RED);
				break;
		}


		if (showProgressBar) {
			// update progress state of project:
			holder.progress.setText(String.format(Locale.getDefault(), "%1$d%%", progress));
			holder.progressBar.setProgress(progress);
			holder.progressLayout.setVisibility(View.VISIBLE);
		} else {
			holder.progressLayout.setVisibility(View.GONE);
		}

		// set project image (threaded):
		WebImage httpImageMetadata = item.getImage();
		if (httpImageMetadata != null && httpImageMetadata.getUrl() != null) {
			final int height = holder.status.getContext().getResources().getDimensionPixelSize(R.dimen.scratch_project_thumbnail_height);
			final String originalImageURL = httpImageMetadata.getUrl().toString();

			// load image but only thumnail!
			// in order to download only thumbnail version of the original image
			// we have to reduce the image size in the URL
			final String thumbnailImageURL = Utils.changeSizeOfScratchImageURL(originalImageURL, height);
			Picasso.with(holder.status.getContext()).load(thumbnailImageURL).into(holder.image);
		} else {
			// clear old image of other project if this is a reused view element
			holder.image.setImageBitmap(null);
		}

		holder.background.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (scratchJobEditListener != null) {
					scratchJobEditListener.onItemClick(item);
				}
			}
		});

		holder.background.setBackgroundResource(R.drawable.button_background_selector);

	}

	public void onItemClick(Job item) {}
	public void onItemLongClick(Job item, ViewHolder h) {}

	public interface ScratchJobEditListener {
		void onProjectEdit(int position);
	}
}
