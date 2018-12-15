package com.udacity.demur.bakingapp.service;

import com.udacity.demur.bakingapp.model.Recipe;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface BakingJsonClient {
    @GET
    Call<List<Recipe>> listRecipes(@Url String url);
}