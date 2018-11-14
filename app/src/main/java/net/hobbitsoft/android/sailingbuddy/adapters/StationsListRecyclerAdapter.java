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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class StationsListRecyclerAdapter extends RecyclerView.Adapter<StationsListRecyclerAdapter.ViewHolder> {

    private static final String TAG = StationsListRecyclerAdapter.class.getSimpleName();
    private static List<StationList> mListStationList = new ArrayList<>();
    private static final List<StationList> mFinalStationList = new ArrayList<>(); //We want to have an authorative source for the filter;
    private final Context mContext;
    private ItemClickListener clickListener;
    private LayoutInflater mInflater;

    public StationsListRecyclerAdapter(Context context, List<StationList> listStationList) {
        this.mListStationList.addAll(listStationList);
        this.mFinalStationList.addAll(listStationList);
        this.mContext = context;
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
        holder.listStationId.setText(((StationList) stationList).getStationId());
        holder.listStationName.setText(((StationList) stationList).getStationName());
        holder.listStationDistance.setText(((StationList) stationList).getDistanceString());
        if (stationList.isFavorite()) {
            holder.isFavoriteImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_favorite_orange_24dp));
        } else {
            holder.isFavoriteImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_favorite_border_black_24dp));
        }
        holder.isFavoriteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Favorite favorite = new Favorite(stationList.getStationId());
                final SailingBuddyDatabase sailingBuddyDatabase = SailingBuddyDatabase.getInstance(mContext);
                if (stationList.isFavorite()) {
                    AppExecutors.getsInstance().getDiskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            sailingBuddyDatabase.favoritesDAO().deleteStationFromFavorites(favorite);
                        }
                    });
                    holder.isFavoriteImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_favorite_border_black_24dp));
                    stationList.setFavorite(false);
                } else {
                    AppExecutors.getsInstance().getDiskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            if (!sailingBuddyDatabase.favoritesDAO().isFavorite(favorite.getStationId())) {
                                sailingBuddyDatabase.favoritesDAO().AddStationToFavorite(favorite);
                            }
                        }
                    });
                    holder.isFavoriteImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_favorite_orange_24dp));
                    stationList.setFavorite(true);
                }
                Log.d(TAG, "Favorites Item has been changes: " + stationList.getStationId());
                notifyItemChanged(position);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mListStationList == null) return 0;
        else return mListStationList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
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
                clickListener.onItemCLick(view, (String) mListStationList.get(getAdapterPosition()).getStationId());
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
                Log.d(TAG, "Publised Results for: " + constraint.toString() + " count is " + String.valueOf(results.count));
                mListStationList.clear();
                mListStationList.addAll((Collection<? extends StationList>) results.values);
                mStationsListRecyclerAdapter.notifyDataSetChanged();
            }
        }
    }
}
