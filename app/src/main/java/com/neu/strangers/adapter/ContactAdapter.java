package com.neu.strangers.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.neu.strangers.R;
import com.neu.strangers.activities.ChatActivity;
import com.neu.strangers.view.MyRippleLayout;
import com.woozzu.android.util.StringMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created with Android Studio.
 * Author: Enex Tapper
 * Date: 15/5/27
 * Project: Strangers
 * Package: com.neu.strangers.adapter
 */
public class ContactAdapter extends BaseAdapter implements SectionIndexer{
	private final static String SECTIONS = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private ArrayList<ContactAdapterItem> stringArray;
	private Context context;

	public ContactAdapter(Context context) {
		this.stringArray = new ArrayList<>();
		this.context = context;
	}

	@Override
	public int getCount() {
		return stringArray.size();
	}

	@Override
	public Object getItem(int i) {
		return stringArray.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		final LayoutInflater inflate = ((Activity) context).getLayoutInflater();
		final ContactAdapterItem item = stringArray.get(i);

		ViewHolderItem viewHolderItem;
		if(view==null){
			view = inflate.inflate(R.layout.contact_item, null);
			viewHolderItem = new ViewHolderItem();
			viewHolderItem.contactName = (TextView)view.findViewById(R.id.contact_name);
            viewHolderItem.contacLayout = (RelativeLayout)view.findViewById(R.id.contact_layout);

			view.setTag(viewHolderItem);

		}else{
			viewHolderItem = (ViewHolderItem)view.getTag();
		}

		viewHolderItem.contactName.setText(item.getUserName());

        viewHolderItem.contactName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("username",String.valueOf(item.getId()));

                context.startActivity(intent);
            }
        });

		return view;
	}

	@Override
	public Object[] getSections() {
		String[] sections = new String[SECTIONS.length()];
		for (int i = 0; i < SECTIONS.length(); i++)
			sections[i] = String.valueOf(SECTIONS.charAt(i));
		return sections;
	}

	@Override
	public int getPositionForSection(int section) {
		// If there is no item for current section, previous section will be selected
		for (int i = section; i >= 0; i--) {
			for (int j = 0; j < getCount(); j++) {
				if (i == 0) {
					// For numeric section
					for (int k = 0; k <= 9; k++) {
						if (StringMatcher.match(String.valueOf(
								stringArray.get(j).getPinyin().charAt(0)).toUpperCase(), String.valueOf(k)))
							return j;
					}
				} else {
					if (StringMatcher.match(String.valueOf(
									stringArray.get(j).getPinyin().charAt(0)).toUpperCase(),
							String.valueOf(SECTIONS.charAt(i))))
						return j;
				}
			}
		}
		return 0;
	}

	@Override
	public int getSectionForPosition(int i) {
		return 0;
	}

	public void refreshList(ArrayList<String> contactsList, ArrayList<Integer> idsList){
		stringArray.clear();
		for(int i = 0;i<contactsList.size();i++){
			String item = contactsList.get(i);
			stringArray.add(
					new ContactAdapterItem(idsList.get(i),item, PinyinHelper.getShortPinyin(item)));
		}

		// 在此处排序
		Collections.sort(stringArray, new Comparator<ContactAdapterItem>() {
			@Override
			public int compare(ContactAdapterItem item1, ContactAdapterItem item2) {
				return item1.getPinyin().compareTo(item2.getPinyin());
			}
		});

		notifyDataSetChanged();
	}

	private static class ViewHolderItem {
		TextView contactName;
        RelativeLayout contacLayout;
	}
}
