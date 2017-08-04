package com.falvojr.nd818.p2.view;

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.falvojr.nd818.p2.R;
import com.falvojr.nd818.p2.data.http.TMDbService;
import com.falvojr.nd818.p2.data.prefs.TMDbPreferences;
import com.falvojr.nd818.p2.data.provider.TMDbContract;
import com.falvojr.nd818.p2.databinding.ActivityMainBinding;
import com.falvojr.nd818.p2.model.Movie;
import com.falvojr.nd818.p2.model.MovieSort;
import com.falvojr.nd818.p2.model.Results;
import com.falvojr.nd818.p2.view.base.BaseActivity;
import com.falvojr.nd818.p2.view.widget.MovieListAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MovieListActivity extends BaseActivity {

    private static final String TAG = MovieListActivity.class.getSimpleName();

    private MovieListAdapter mAdapter;

    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding.srlMovies.setOnRefreshListener(this::loadConfigImages);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter == null) {
            this.createAdapter();
            this.loadConfigImages();
        } else if (MovieSort.FAVORITE.equals(TMDbPreferences.getInstance().getSort(this))) {
            this.loadConfigImages();
        }
        mBinding.rvMovies.setLayoutManager(this.getGridLayoutByLandAndSw600dpRes());
    }

    private void loadConfigImages() {
        mBinding.srlMovies.setRefreshing(true);
        if (TMDbPreferences.getInstance().getImagesBaseUrl(this).isEmpty()) {
            TMDbService.getInstance().getApi().getConfig(super.getApiKey())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp -> {
                        TMDbPreferences.getInstance().putImagesBaseUrl(this, resp.getConfigImages().getBaseUrl());
                        this.loadMovies();
                    }, error -> {
                        super.showError(R.string.msg_error_get_config, error);
                        mBinding.srlMovies.setRefreshing(false);
                    });
        } else {
            this.loadMovies();
        }
    }

    private void loadMovies() {
        final Observable<Results<Movie>> call;
        final String storedSort = TMDbPreferences.getInstance().getSort(this);
        if (MovieSort.FAVORITE.equals(storedSort)) {
            call = Observable.just(this.getFavoriteMoviesOffline());
        } else {
            if (MovieSort.POPULAR.equals(storedSort)) {
                call = TMDbService.getInstance().getApi().getPopularMovies(super.getApiKey());
            } else {
                call = TMDbService.getInstance().getApi().getTopRatedMovies(super.getApiKey());
            }
        }
        call.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateAdapter,
                        error -> super.showError(R.string.msg_error_get_movies, error),
                        () -> mBinding.srlMovies.setRefreshing(false));
    }

    private void createAdapter() {
        mAdapter = new MovieListAdapter(Collections.emptyList(), new MovieListAdapter.OnItemListener() {
            @Override
            public void onLoadPoster(ImageView imageView, String posterPath) {
                final Integer width = getResources().getInteger(R.integer.movie_list_image_width);
                Picasso.with(MovieListActivity.this).load(getFullImageUrl(posterPath, width)).into(imageView);
            }

            @Override
            public void onClick(Movie movie) {
                final Intent intent = new Intent(MovieListActivity.this, MovieActivity.class);
                intent.putExtra(MovieActivity.KEY_MOVIE, movie);
                startActivity(intent);
            }
        });
        mBinding.rvMovies.setHasFixedSize(true);
        mBinding.rvMovies.setAdapter(mAdapter);
    }

    private void updateAdapter(Results<Movie> results) {
        mAdapter.setDataSet(results.getData());
        mAdapter.notifyDataSetChanged();
    }

    private GridLayoutManager getGridLayoutByLandAndSw600dpRes() {
        final int factor = super.getResources().getInteger(R.integer.movie_grid_column_factor);
        final int columns = super.getResources().getInteger(R.integer.movie_grid_column_count);
        return new GridLayoutManager(this, factor * columns);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.getMenuInflater().inflate(R.menu.main, menu);
        final String storedSort = TMDbPreferences.getInstance().getSort(this);
        final int id = MovieSort.POPULAR.equals(storedSort) ? R.id.mPopular : MovieSort.TOP_RATED.equals(storedSort) ? R.id.mTopRated : R.id.mFavorite;
        menu.findItem(id).setChecked(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mPopular:
                TMDbPreferences.getInstance().putSort(this, MovieSort.POPULAR);
                break;
            case R.id.mTopRated:
                TMDbPreferences.getInstance().putSort(this, MovieSort.TOP_RATED);
                break;
            case R.id.mFavorite:
                TMDbPreferences.getInstance().putSort(this, MovieSort.FAVORITE);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        this.loadConfigImages();
        item.setChecked(!item.isChecked());
        return true;
    }

    private Results<Movie> getFavoriteMoviesOffline() {
        // A "projection" defines the columns that will be returned for each row
        final String[] projection = {
                TMDbContract.TMDbEntry._ID,
                TMDbContract.TMDbEntry.COLUMN_MOVIE_ID,
                TMDbContract.TMDbEntry.COLUMN_ORIGINAL_TITLE,
                TMDbContract.TMDbEntry.COLUMN_POSTER_PATH
        };
        // Defines a string to contain the selection clause
        final String selectionClause = TMDbContract.TMDbEntry.COLUMN_FAVORITE + " = ?";
        // Initializes an array to contain selection arguments
        final String[] selectionArgs = { TMDbContract.TMDbEntry.TRUE.toString() };
        // Defines a string to contain the sort order
        final String sortOrder = "";

        // Queries the user dictionary and returns results
        final Cursor cursor = getContentResolver().query(
                TMDbContract.TMDbEntry.CONTENT_URI,  // The content URI of the movies table
                projection,            // The columns to return for each row
                selectionClause,       // Selection criteria
                selectionArgs,         // Selection criteria
                sortOrder);            // The sort order for the returned rows

        final Results<Movie> results = new Results<>();
        results.setData(new ArrayList<>());
        if (cursor != null) {
            final int idxId = cursor.getColumnIndex(TMDbContract.TMDbEntry.COLUMN_MOVIE_ID);
            final int idxOriginalTitle = cursor.getColumnIndex(TMDbContract.TMDbEntry.COLUMN_ORIGINAL_TITLE);
            final int idxPosterPath = cursor.getColumnIndex(TMDbContract.TMDbEntry.COLUMN_POSTER_PATH);
            while (cursor.moveToNext()) {
                final Movie movie = new Movie();
                movie.setId(cursor.getLong(idxId));
                movie.setOriginalTitle(cursor.getString(idxOriginalTitle));
                movie.setPosterPath(cursor.getString(idxPosterPath));
                results.getData().add(movie);
            }
            cursor.close();
        }
        return results;
    }
}
