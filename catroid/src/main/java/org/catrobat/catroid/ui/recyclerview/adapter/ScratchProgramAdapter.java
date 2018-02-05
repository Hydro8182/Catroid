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
import android.view.View;
import android.widget.ListView;
import android.widget.ScrollView;

import com.squareup.picasso.Picasso;

import org.catrobat.catroid.R;
import org.catrobat.catroid.common.ScratchProgramData;
import org.catrobat.catroid.ui.recyclerview.viewholder.ExtendedVH;
import org.catrobat.catroid.utils.Utils;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ScratchProgramAdapter extends ExtendedRVAdapter<ScratchProgramData> {

	private boolean showDetails;
	private int selectMode;
	private Set<Integer> checkedPrograms = new TreeSet<>();
	public ScratchProgramAdapter(List<ScratchProgramData> objects) {
		super(objects);
		showDetails = true;
		selectMode = ListView.CHOICE_MODE_NONE;
	}


	@Override
	public void onBindViewHolder(final ExtendedVH holder, int position) {
		ScratchProgramData item = items.get(position);

		holder.name.setText(item.getTitle());
		holder.image.setImageBitmap(null); //TODO: set image
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

		holder.name.setSingleLine(true);

		if (showDetails) {
			holder.details.setVisibility(View.VISIBLE);
			holder.leftTopDetails.setText(item.getOwner());

		}
	}

	public void setShowDetails(boolean showDetails) {
		this.showDetails = showDetails;
	}

	public boolean getShowDetails() {
		return showDetails;
	}

	public void setSelectMode(int selectMode) {
		this.selectMode = selectMode;
	}

	public int getSelectMode() {
		return selectMode;
	}

	public Set<Integer> getCheckedPrograms() {
		return checkedPrograms;
	}

	public int getAmountOfCheckedPrograms() {
		return checkedPrograms.size();
	}

	public void clearCheckedPrograms() {
		checkedPrograms.clear();
	}

}
