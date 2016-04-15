/*
Copyright 2016 The Open Source Project

Author: hailongqiu <356752238@qq.com>
Maintainer: hailongqiu <356752238@qq.com>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.open.androidtvwidget.menu;

import java.util.ArrayList;

import com.open.androidtvwidget.R;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Toast;

/**
 * 菜单.
 * 
 * @author hailongqiu
 *
 */
public class OpenMenuBuilder implements OpenMenu {

	private static final String TAG = "OpenMenuBuilder";

	private ArrayList<OpenMenuItemImpl> mItems;
	private Context mContext;
	private Resources mResources;

	// 菜单视图.(暂时测试)
	OpenListMenuView mMenuView;
	LayoutAnimationController mLayoutAnimationController;
	MenuAdapter mAdapter;
	LayoutInflater mInflater;

	public OpenMenuBuilder(Context context) {
		init(context);
		//
		ViewGroup rootView = (ViewGroup) ((Activity)context).findViewById(Window.ID_ANDROID_CONTENT);
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(300,
				ViewGroup.LayoutParams.MATCH_PARENT);
		View view = (View) getMenuView();
		view.setVisibility(View.GONE);
		rootView.addView(view, layoutParams);
	}
	
	public void showMenu() {
		View view = (View) getMenuView();
		view.setVisibility(View.VISIBLE);
	}
	
	private void init(Context context) {
		this.mContext = context;
		if (this.mContext != null) {
			this.mResources = context.getResources();
			this.mItems = new ArrayList<OpenMenuItemImpl>();
			// 菜单视图 (暂时测试)
			this.mInflater = LayoutInflater.from(mContext);
		}
	}

	Resources getResources() {
		return mResources;
	}

	Context getContext() {
		return mContext;
	}

	private int mDefaultShowAsAction = 0;

	public OpenMenuItem addInternal(int groupId, int itemId, int order, CharSequence title) {
		final int ordering = order;
		final OpenMenuItemImpl item = new OpenMenuItemImpl(this, groupId, itemId, order, ordering, title,
				mDefaultShowAsAction);
		mItems.add(item);
		return item;
	}

	@Override
	public OpenMenuItem add(int groupId, int itemId, int order, CharSequence title) {
		return addInternal(groupId, itemId, order, title);
	}

	@Override
	public OpenMenuItem add(int groupId, int itemId, int order, int titleRes) {
		return addInternal(groupId, itemId, order, this.mResources.getString(titleRes));
	}

	@Override
	public OpenMenuItem add(CharSequence title) {
		return addInternal(0, 0, 0, title);
	}

	/**
	 * 添加子菜单.
	 */
	@Override
	public OpenSubMenu addSubMenu(int pos, OpenSubMenu openSubMenu) {
		mItems.get(pos).setSubMenu(openSubMenu);
		return openSubMenu;
	}

	/**
	 * 菜单动画.
	 */
	@Override
	public OpenMenu setLayoutAnimation(LayoutAnimationController layoutAnimationController) {
		this.mLayoutAnimationController = layoutAnimationController;
		if (mMenuView != null && mLayoutAnimationController != null) {
			mMenuView.setLayoutAnimation(layoutAnimationController);
		}
		return this;
	}

	// 菜单视图(暂时测试放这里)

	public OpenMenuView getMenuView() {
		// 多个listview---主菜单--子菜单（无限个)
		if (mMenuView == null) {
			mMenuView = new OpenListMenuView(mContext);
			mMenuView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if (mItems.get(position).hasSubMenu()) {
						// test.
						FrameLayout.LayoutParams mainLayoutParams = (LayoutParams) mMenuView.getLayoutParams();
						
						OpenMenuBuilder subMenu = (OpenMenuBuilder) mItems.get(position).getSubMenu();
						OpenListMenuView menuView = (OpenListMenuView) subMenu.getMenuView();
						FrameLayout.LayoutParams layPar = (LayoutParams) menuView.getLayoutParams();
						layPar.leftMargin = mainLayoutParams.leftMargin + mMenuView.getMeasuredWidth();
						subMenu.showMenu();
						//
						Toast.makeText(mContext, "子菜单" + menuView, Toast.LENGTH_LONG).show();
					}
				}
			});
			mMenuView.setBackgroundColor(Color.parseColor("#50000000"));
			if (mLayoutAnimationController != null) {
				mMenuView.setLayoutAnimation(mLayoutAnimationController);
			}
			if (mAdapter == null) {
				mAdapter = new MenuAdapter();
			}
			mMenuView.setAdapter(mAdapter);
			// mMenuView.setOnItemClickListener(this);
		}
		return mMenuView;
	}

	private class MenuAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mItems.size();
		}

		@Override
		public OpenMenuItemImpl getItem(int position) {
			return mItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(mMenuItemLayoutID, parent, false);
			}
			OpenMenuView.ItemView itemView = (OpenMenuView.ItemView) convertView;
			itemView.initialize(getItem(position), 0);
			return convertView;
		}

	}

	@Override
	public String toString() {
		for (OpenMenuItem item : mItems) {
			String title = item.getTitle().toString();
			Log.e(TAG, "hailongqiu menu item:" + title);
			OpenSubMenu submenu = item.getSubMenu();
			if (submenu != null) {
				Log.e(TAG, "hailongqiu =======sub menu======start start start");
				submenu.toString();
				Log.e(TAG, "hailongqiu =======sub menu======end end end");
			}
		}
		return super.toString();
	}

	private int mMenuItemLayoutID = R.layout.list_menu_item_layout;

	/**
	 * 修改菜单item布局. <br>
	 * 修改的布局ID必需和 <br>
	 * list_menu_item_layout 中的名字一样.
	 */
	public void setMenuItemLayoutResId(int resId) {
		mMenuItemLayoutID = resId;
		if (this.mAdapter != null) {
			this.mAdapter.notifyDataSetChanged();
		}
	}

}
