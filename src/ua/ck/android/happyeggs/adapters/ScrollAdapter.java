package ua.ck.android.happyeggs.adapters;

import ua.ck.android.happyeggs.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ScrollAdapter extends BaseAdapter {
	private int mCount;
	private Context mContext;
	private LayoutInflater mInflator;
	
	public ScrollAdapter(int mCount, Context mContext) {
		super();
		this.mCount = mCount;
		this.mContext = mContext;
		this.mInflator = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		return mCount;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = mInflator.inflate(R.layout.item_scroll, null);
		
		return v;
	}

}
