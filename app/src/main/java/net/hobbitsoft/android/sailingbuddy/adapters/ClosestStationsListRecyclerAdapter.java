/*
 * Copyright (c) 2018.  HobbitSoft - Kevin Heath High
 */

package net.hobbitsoft.android.sailingbuddy.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import net.hobbitsoft.android.sailingbuddy.R;
import net.hobbitsoft.android.sailingbuddy.data.StationList;
import net.hobbitsoft.android.sailingbuddy.database.Favorite;
import net.hobbitsoft.android.sailingbuddy.database.SailingBuddyDatabase;
import net.hobbitsoft.android.sailingbuddy.utilities.AppExecutors;
import net.hobbitsoft.android.sailingbuddy.viewmodels.StationDetailVeiwModelRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ClosestStationsListRecyclerAdapter extends RecyclerView.Adapter<ClosestStationsListRecyclerAdapter.ViewHolder> {

    private static final String TAG = ClosestStationsListRecyclerAdapter.class.getSimpleName();
    private static List<StationList> mListStationList;
    private static List<StationList> mFinalStationList = new ArrayList<>(); //We want to have an authoritative source for the filter;
    private final Context mContext;
    private ItemClickListener clickListener;
    private LayoutInflater mInflater;


    public ClosestStationsListRecyclerAdapter(Context context, List<StationList> listStationList) {
        mListStationList = listStationList;
        mFinalStationList.addAll(listStationList);
        this.mContext = context;
        setHasStableIds(true);
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.station_list_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final StationList stationList = mListStationList.get(position);
        holder.listStationId.setText(stationList.getStationId());
        holder.listStationName.setText(stationList.getStationName());
        holder.listStationDistance.setText(stationList.getDistanceString());
        if (stationList.isFavorite()) {
            holder.isFavoriteImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_favorite_orange_24dp));
        } else {
            holder.isFavoriteImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_favorite_border_black_24dp));
        }
        holder.isFavoriteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SailingBuddyDatabase sailingBuddyDatabase = SailingBuddyDatabase.getInstance(mContext);
                if (stationList.isFavorite()) {
                    AppExecutors.getsInstance().getDiskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            Favorite favorite = sailingBuddyDatabase.favoritesDAO().getFavoriteByStation(stationList.getStationId());
                            sailingBuddyDatabase.favoritesDAO().deleteStationFromFavorites(favorite);
                        }
                    });
                    holder.isFavoriteImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_favorite_border_black_24dp));
                    stationList.setFavorite(false);
                } else {
                    final Favorite favorite = new Favorite(stationList.getStationId());
                    AppExecutors.getsInstance().getDiskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            if (!sailingBuddyDatabase.favoritesDAO().isFavorite(favorite.getStationId())) {
                                sailingBuddyDatabase.favoritesDAO().AddStationToFavorite(favorite);
                                //It is necessary to have a local copy of the data in the database for Widget use.
                                //It doesn't appear we can use LiveData in the Widget, so this serves our purpose
                                StationDetailVeiwModelRepository.getInstance(mContext).getStationDetails(favorite.getStationId());
                            }
                        }
                    });
                    holder.isFavoriteImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_favorite_orange_24dp));
                    stationList.setFavorite(true);
                }
                Log.d(TAG, "Favorites Item has been changes: " + stationList.getStationId());
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (mListStationList == null) return 0;
        else return mListStationList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.list_station_id)
        TextView listStationId;
        @BindView(R.id.list_station_name)
        TextView listStationName;
        @BindView(R.id.list_station_distance)
        TextView listStationDistance;

        @BindView(R.id.list_station_is_favorite)
        ImageView isFavoriteImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) {
                clickListener.onItemCLick(view, mListStationList.get(getAdapterPosition()).getStationId());
            }
        }
    }

    public void setClickListener(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ItemClickListener {
        void onItemCLick(View view, String stationId);
    }

    public static class SearchFilter extends Filter {
        private RecyclerView.Adapter<?> mStationsListRecyclerAdapter;
        List<StationList> mFilteredList = new ArrayList<>();

        public SearchFilter(List<StationList> stationList, RecyclerView.Adapter<?> stationsListRecyclerAdapter) {
            super();
            mStationsListRecyclerAdapter = stationsListRecyclerAdapter;
            mFilteredList.addAll(stationList);
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            mFilteredList.clear();
            final FilterResults filterResults = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                mFilteredList.addAll(mFinalStationList);
            } else {
                final String searchString = constraint.toString().toLowerCase();
                for (final StationList station : mFinalStationList) {
                    if (station.getStationId().toLowerCase().contains(searchString) ||
                            station.getStationName().toLowerCase().contains(searchString)) {
                        mFilteredList.add(station);
                    }
                }
            }
            filterResults.values = mFilteredList;
            filterResults.count = mFilteredList.size();
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (constraint != null) {
                Log.d(TAG, "Published Results for: " + constraint.toString() + " count is " + String.valueOf(results.count));
                mListStationList.clear();
                mListStationList.addAll((Collection<? extends StationList>) results.values);
                mStationsListRecyclerAdapter.notifyDataSetChanged();
            }
        }
    }
}
