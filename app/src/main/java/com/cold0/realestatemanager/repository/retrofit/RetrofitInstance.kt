package com.cold0.realestatemanager.repository.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
	private const val BASE_URL = "https://maps.googleapis.com/maps/"
	private val retrofit: Retrofit by lazy {
		Retrofit.Builder()
			.addConverterFactory(GsonConverterFactory.create())
			.baseUrl(BASE_URL)
			.build()
	}
	val movieService: GeocoderService by lazy {
		retrofit.create(GeocoderService::class.java)
	}
}