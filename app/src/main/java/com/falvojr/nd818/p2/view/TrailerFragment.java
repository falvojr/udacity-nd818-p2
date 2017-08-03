package com.falvojr.nd818.p2.view;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.falvojr.nd818.p2.R;
import com.falvojr.nd818.p2.data.http.TMDbService;
import com.falvojr.nd818.p2.databinding.FragmentTrailerBinding;
import com.falvojr.nd818.p2.model.Movie;
import com.falvojr.nd818.p2.view.base.BaseFragment;
import com.falvojr.nd818.p2.view.widget.MovieTrailerAdapter;

import java.util.Collections;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class TrailerFragment extends BaseFragment<MovieActivity> {

    FragmentTrailerBinding mBinding;
    private MovieTrailerAdapter mAdapter;

    public TrailerFragment() {
        // Required empty public constructor
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_trailer, container, false);

        mAdapter = new MovieTrailerAdapter(Collections.emptyList(), uri -> startActivity(new Intent(Intent.ACTION_VIEW, uri)));
        mBinding.rvTrailers.setLayoutManager(new LinearLayoutManager(super.getContext()));
        mBinding.rvTrailers.setHasFixedSize(true);
        mBinding.rvTrailers.setAdapter(mAdapter);

        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        final Movie movie = super.getBaseActivity().getMovie();
        if (this.isIncompleteMovie(movie)) {
            this.findMovieTrailers(movie);
        } else {
            this.fillTrailers(movie);
        }
    }

    private boolean isIncompleteMovie(Movie movie) {
        return movie.getTrailers() == null;
    }

    private void findMovieTrailers(Movie movie) {
        super.showProgress(mBinding.rvTrailers, mBinding.progress.clContent);
        TMDbService.getInstance().getApi().getMovieTrailers(movie.getId(), super.getBaseActivity().getApiKey())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(results -> {
                            movie.setTrailers(results.getData());
                            this.fillTrailers(movie);
                        }, error -> super.getBaseActivity().showError(R.string.msg_error_get_trailers, error)
                        , () -> super.hideProgress(mBinding.rvTrailers, mBinding.progress.clContent));
    }

    private void fillTrailers(Movie movie) {
        mAdapter.setDataSet(movie.getTrailers());
        mAdapter.notifyDataSetChanged();
    }
}
