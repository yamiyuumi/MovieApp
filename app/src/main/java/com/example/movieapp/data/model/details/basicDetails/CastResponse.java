package com.example.movieapp.data.model.details.basicDetails;
import com.example.movieapp.data.model.details.reviews.ReviewsRemote;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CastResponse {
    @SerializedName("cast")
    private List<CastRemote> credits;

    public List<CastRemote> getCredits() {
        return credits;
    }
}
