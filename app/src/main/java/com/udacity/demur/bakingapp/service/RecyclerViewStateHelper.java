package com.udacity.demur.bakingapp.service;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.udacity.demur.bakingapp.BR;

public class RecyclerViewStateHelper extends BaseObservable {
    private Boolean loadingState = false;
    private Boolean errorState = false;

    public RecyclerViewStateHelper() {
    }

    @Bindable
    public Boolean getLoadingState() {
        return loadingState;
    }

    @Bindable
    public Boolean getErrorState() {
        return errorState;
    }

    public void setLoadingState(Boolean state) {
        loadingState = state;
        notifyPropertyChanged(BR.loadingState);
    }

    public void setErrorState(Boolean state) {
        errorState = state;
        notifyPropertyChanged(BR.errorState);
    }
}