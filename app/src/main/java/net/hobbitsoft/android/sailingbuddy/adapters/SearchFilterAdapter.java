/*
 * https://stackoverflow.com/questions/33711398/how-to-use-autocompletetextview-with-recyclerview-adapter-but-not-with-arrayadap/42701792
 */

package net.hobbitsoft.android.sailingbuddy.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import net.hobbitsoft.android.sailingbuddy.data.StationList;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SearchFilterAdapter<SearchFilterAdapter extends RecyclerView.ViewHolder> extends BaseAdapter implements Filterable {

    private final String TAG = this.getClass().getSimpleName();
    private final RecyclerView.Adapter<SearchFilterAdapter> mRecyclerAdapterAdapter;
    private List<StationList> mStationLists = new ArrayList<>();

    public SearchFilterAdapter(RecyclerView.Adapter<SearchFilterAdapter> adapter) {
        this.mRecyclerAdapterAdapter = adapter;
    }

    @Override
    public int getCount() {
        return mRecyclerAdapterAdapter.getItemCount();
    }

    @Override
    public Object getItem(int position) {
        return mRecyclerAdapterAdapter.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        return mRecyclerAdapterAdapter.getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SearchFilterAdapter holder;
        if (convertView == null) {
            holder = mRecyclerAdapterAdapter.createViewHolder(parent, getItemViewType(position));
            convertView = holder.itemView;
            convertView.setTag(holder);
        } else {
            holder = (SearchFilterAdapter) convertView.getTag();
        }
        mRecyclerAdapterAdapter.bindViewHolder(holder, position);
        return holder.itemView;
    }

    public void setStationLists(List<StationList> stationsLists) {
        mStationLists = stationsLists;
    }

    @Override
    public Filter getFilter() {
        return new StationsListRecyclerAdapter.SearchFilter(mStationLists, mRecyclerAdapterAdapter);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}
