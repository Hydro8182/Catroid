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

package org.catrobat.catroid.ui.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import org.catrobat.catroid.R;
import org.catrobat.catroid.common.Constants;
import org.catrobat.catroid.common.ScratchProgramData;
import org.catrobat.catroid.common.ScratchSearchResult;
import org.catrobat.catroid.scratchconverter.ConversionManager;
import org.catrobat.catroid.transfers.SearchScratchProgramsTask;
import org.catrobat.catroid.ui.ScratchConverterActivity;
import org.catrobat.catroid.ui.ScratchProgramDetailsActivity;
import org.catrobat.catroid.ui.recyclerview.adapter.ScratchProgramAdapter;
import org.catrobat.catroid.ui.recyclerview.fragment.RecyclerViewFragment;
import org.catrobat.catroid.ui.recyclerview.viewholder.ViewHolder;
import org.catrobat.catroid.utils.ExpiringLruMemoryObjectCache;
import org.catrobat.catroid.utils.ToastUtil;
import org.catrobat.catroid.web.ScratchDataFetcher;

import java.util.ArrayList;
import java.util.List;

public class SearchScratchSearchProjectsListFragment extends RecyclerViewFragment<ScratchProgramData>
		implements SearchScratchProgramsTask.SearchScratchProgramsTaskDelegate
		{

	@Override
	public void renameItem(String name) {
		//TODO:??? I don't think it should be renameable...
	}
	@Override
	protected void showRenameDialog(List<ScratchProgramData> selectedItems) {
	}
	protected boolean isBackpackEmpty() {
		//We have no backpack, guess that means always empty?
		return true;
	}
	@Override
	public void addItem(ScratchProgramData item) {
		adapter.add(item);
	}

	@Override
	public void onItemLongClick(ScratchProgramData item, ViewHolder holder) {
		//We shouldn't be able to drag it.
	}

	@Override
	public boolean isNameUnique(String name) {
		//I don't think we need it since 2 external programs could have the same name.
		return true;
	}
	@Override
	protected void copyItems(List<ScratchProgramData> selectedItems) {
		//We can't copy external programs.
	}

	@Override
	public void onSelectionChanged(int selectedItemCnt) {
		actionMode.setTitle(actionModeTitle + " " + getResources().getQuantityString(R.plurals.am_looks_title,
				selectedItemCnt,
				selectedItemCnt));
	}
	@Override
	protected void deleteItems(List<ScratchProgramData> selectedItems) {
		//We can't delete external programs.
	}

	@Override
	protected void packItems(List<ScratchProgramData> selectedItems) {
		//We can't throw something in the backpack if we can't copy external programs
	}

	@Override
	protected void switchToBackpack() { }

	@Override
	protected int getDeleteAlertTitle()
	{
		//TODO: ??? What is this?
		return R.plurals.delete_looks;
	}


	@Override
	protected void initializeAdapter() {
		initAdapter();
		onAdapterReady();
	}
	@Override
	public void handleAddButton() {
		//TODO: There should be no add button...
	}
	@Override
	public void onItemClick(ScratchProgramData item) {
		if (actionModeType != NONE) {
			return;
		}

		Preconditions.checkState(conversionManager != null);

		ScratchProgramDetailsActivity.setDataFetcher(dataFetcher);
		ScratchProgramDetailsActivity.setConversionManager(conversionManager);
		Intent intent = new Intent(activity, ScratchProgramDetailsActivity.class);
		//TODO: Is item parcelable?
		intent.putExtra(Constants.INTENT_SCRATCH_PROGRAM_DATA, (Parcelable)item);
		activity.startActivityForResult(intent, Constants.INTENT_REQUEST_CODE_CONVERT);
	}
		///-----------------------------------------------------------------------------------------------
	private static final String TAG = SearchScratchSearchProjectsListFragment.class.getSimpleName();

	private static final String BUNDLE_ARGUMENTS_SCRATCH_PROJECT_DATA = "scratch_project_data";
	private static final String SHARED_PREFERENCE_NAME = "showDetailsScratchProjects";

	private ScratchDataFetcher dataFetcher;
	private ConversionManager conversionManager;

	private ScratchConverterActivity activity;
	private String actionModeTitle;

	private SearchView searchView;
	private ImageButton audioButton;
	private RecyclerView searchResultsRecyclerView;
	private List<ScratchProgramData> scratchProgramDataList;
	private ScratchProgramData scratchProgramToEdit;
	private ExpiringLruMemoryObjectCache<ScratchSearchResult> scratchSearchResultCache;
	private ScratchProgramAdapter scratchProgramAdapter;
	private ActionMode actionMode;
	private SearchScratchProgramsTask currentSearchTask = null;

	// dependency-injection for testing with mock object
	public void setDataFetcher(final ScratchDataFetcher fetcher) {
		dataFetcher = fetcher;
	}

	public void setConversionManager(final ConversionManager manager) {
		conversionManager = manager;
	}

	public void setSearchResultsListViewMargin(int left, int top, int right, int bottom) {
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) searchResultsRecyclerView.getLayoutParams();
		params.setMargins(left, top, right, bottom);
		searchResultsRecyclerView.setLayoutParams(params);
	}

	private ActionMode.Callback convertModeCallBack = new ActionMode.Callback() {
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			setSelectMode(ListView.CHOICE_MODE_MULTIPLE);

			actionModeTitle = getString(R.string.convert);

			mode.setTitle(actionModeTitle);
			searchView.setVisibility(View.GONE);
			audioButton.setVisibility(View.GONE);
			setSearchResultsListViewMargin(0, 0, 0, 0);
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			if (scratchProgramAdapter.getAmountOfCheckedPrograms() == 0) {
				clearCheckedProjectsAndEnableButtons();
			} else {
				convertCheckedProjects();
				clearCheckedProjectsAndEnableButtons();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		super.onCreate(savedInstanceState);
		scratchSearchResultCache = ExpiringLruMemoryObjectCache.getInstance();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (currentSearchTask != null) {
			currentSearchTask.cancel(true);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(SHARED_PREFERENCE_NAME, getShowDetails());
		editor.commit();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (actionMode != null) {
			actionMode.finish();
			actionMode = null;
		}
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
		setShowDetails(settings.getBoolean(SHARED_PREFERENCE_NAME, false));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater,container,savedInstanceState);

		activity = (ScratchConverterActivity) getActivity();
		final View rootView = inflater.inflate(R.layout.fragment_scratch_search_projects_list, container, false);
		searchView = (SearchView) rootView.findViewById(R.id.search_view_scratch);
		searchResultsRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_search_scratch);
		searchResultsRecyclerView.setVisibility(View.INVISIBLE);
		audioButton = (ImageButton) rootView.findViewById(R.id.mic_button_image_scratch);

		int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
		final TextView searchTextView = (TextView) searchView.findViewById(id);
		searchTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus && !activity.isSlideUpPanelEmpty()) {
					activity.showSlideUpPanelBar(500);
				} else {
					activity.hideSlideUpPanelBar();
				}
			}
		});
		searchTextView.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);

		audioButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				searchView.setQuery("", false);
				searchView.clearFocus();
				activity.displaySpeechRecognizer();
			}
		});

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				Log.i(TAG, "Submitting query: " + query);
				searchView.clearFocus();
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				Log.i(TAG, newText);
				if (newText.length() <= 2) {
					searchResultsRecyclerView.setVisibility(View.INVISIBLE);
					return false;
				}
				search(newText);
				return false;
			}
		});
		return rootView;
	}

	public void searchAndUpdateText(final String text) {
		searchView.setQuery(text, false);
	}

	public void search(final String text) {
		ScratchSearchResult cachedResult = scratchSearchResultCache.get(text);
		if (cachedResult != null) {
			Log.d(TAG, "Cache hit!");
			onPostExecute(cachedResult);
			return;
		}

		// cache miss
		Log.d(TAG, "Cache miss! -> Searching...");
		if (currentSearchTask != null) {
			currentSearchTask.cancel(true);
		}

		currentSearchTask = new SearchScratchProgramsTask();
		currentSearchTask.setContext(activity).setDelegate(this).setFetcher(dataFetcher);
		currentSearchTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, text);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null) {
			scratchProgramToEdit = (ScratchProgramData) savedInstanceState.getSerializable(BUNDLE_ARGUMENTS_SCRATCH_PROJECT_DATA);
		}
		//initAdapter();
		searchView.setFocusable(false);
		fetchDefaultProjects();
	}

	private void fetchDefaultProjects() {
		Log.d(TAG, "Fetching default scratch projects");
		searchView.setQuery("", false);

		if (currentSearchTask != null) {
			currentSearchTask.cancel(true);
		}

		currentSearchTask = new SearchScratchProgramsTask();
		currentSearchTask.setContext(activity);
		currentSearchTask.setDelegate(this);
		currentSearchTask.setFetcher(dataFetcher);
		currentSearchTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(BUNDLE_ARGUMENTS_SCRATCH_PROJECT_DATA, scratchProgramToEdit);
		super.onSaveInstanceState(outState);
	}

	public boolean getShowDetails() {
		return scratchProgramAdapter.getShowDetails();
	}

	public void setShowDetails(boolean showDetails) {
		scratchProgramAdapter.setShowDetails(showDetails);
		scratchProgramAdapter.notifyDataSetChanged();
	}

	public void setSelectMode(int selectMode) {
		scratchProgramAdapter.setSelectMode(selectMode);
		scratchProgramAdapter.notifyDataSetChanged();
	}

	private void initAdapter() {
		if (scratchProgramDataList == null) {
			scratchProgramDataList = new ArrayList<>();
		}
		scratchProgramAdapter = new ScratchProgramAdapter(
				scratchProgramDataList);
		searchResultsRecyclerView.setAdapter(scratchProgramAdapter);
		adapter = scratchProgramAdapter;
		searchResultsRecyclerView.addItemDecoration(new DividerItemDecoration(searchResultsRecyclerView.getContext(),
				DividerItemDecoration.VERTICAL));
	}

	public void startConvertActionMode() {
		if (actionMode == null) {
			actionMode = activity.startActionMode(convertModeCallBack);
		}
	}

	private void convertCheckedProjects() {
		ArrayList<ScratchProgramData> projectsToConvert = new ArrayList<>();
		for (int position : scratchProgramAdapter.getCheckedPrograms()) {
			scratchProgramToEdit = scratchProgramAdapter.getItems().get(position);
			projectsToConvert.add(scratchProgramToEdit);
			Log.d(TAG, "Converting project '" + scratchProgramToEdit.getTitle() + "'");
		}
		initAdapter();
		activity.convertProjects(projectsToConvert);
	}

	private void clearCheckedProjectsAndEnableButtons() {
		setSelectMode(ListView.CHOICE_MODE_NONE);
		scratchProgramAdapter.clearCheckedPrograms();
		actionMode = null;
		searchView.setVisibility(View.VISIBLE);
		audioButton.setVisibility(View.VISIBLE);
		int marginTop = activity.getResources().getDimensionPixelSize(R.dimen.scratch_project_search_list_view_margin_top);
		int marginBottom = activity.getResources().getDimensionPixelSize(R.dimen.scratch_project_search_list_view_margin_bottom);
		setSearchResultsListViewMargin(0, marginTop, 0, marginBottom);
	}

	//----------------------------------------------------------------------------------------------
	// Scratch Search Task Delegate Methods
	//----------------------------------------------------------------------------------------------
	@Override
	public void onPreExecute() {
		Log.d(TAG, "onPreExecute for SearchScratchProgramsTask called");
	}

	@Override
	public void onPostExecute(final ScratchSearchResult result) {
		Log.d(TAG, "onPostExecute for SearchScratchProgramsTask called");
		Preconditions.checkNotNull(scratchProgramAdapter, "Scratch project adapter cannot be null!");

		if (result == null || result.getProgramDataList() == null) {
			ToastUtil.showError(activity, R.string.search_failed);
			return;
		}

		if (result.getQuery() != null) {
			scratchSearchResultCache.put(result.getQuery(), result);
		}

		scratchProgramAdapter.getItems().clear();
		for (ScratchProgramData projectData : result.getProgramDataList()) {
			scratchProgramAdapter.add(projectData);
			Log.d(TAG, projectData.getTitle());
		}

		scratchProgramAdapter.notifyDataSetChanged();
		searchResultsRecyclerView.setVisibility(View.VISIBLE);
	}
}
