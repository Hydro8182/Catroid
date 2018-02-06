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
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import com.squareup.picasso.Picasso;
import org.catrobat.catroid.R;
import org.catrobat.catroid.common.Constants;
import org.catrobat.catroid.common.ScratchProgramData;
import org.catrobat.catroid.ui.ScratchProgramDetailsActivity;
import org.catrobat.catroid.ui.recyclerview.viewholder.ExtendedVH;
import org.catrobat.catroid.utils.Utils;

import java.util.List;

public class ScratchRemixedProgramAdapter extends ExtendedRVAdapter<ScratchProgramData> {
	private static final String TAG = ScratchRemixedProgramAdapter.class.getSimpleName();

	private ScratchRemixedProgramEditListener onClickListener;
	@Override
	public void onBindViewHolder(final ExtendedVH holder, int position) {

		final ScratchProgramData item = items.get(position);
		holder.name.setText(item.getTitle());
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
		holder.details.setVisibility(View.VISIBLE);
		holder.name.setSingleLine(true);
		holder.leftTopDetails.setText(item.getOwner());
		holder.background.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickListener.onProjectEdit(item);
			}
		});
	}


	public ScratchRemixedProgramAdapter(List<ScratchProgramData> objects) {
		super(objects);
		Log.d(TAG, "Number of remixes: " + objects.size());
	}

	public interface ScratchRemixedProgramEditListener {
		void onProjectEdit(ScratchProgramData item);
	}

	public void setOnClickListener(ScratchRemixedProgramEditListener listener){
		this.onClickListener = listener;
	}
}
