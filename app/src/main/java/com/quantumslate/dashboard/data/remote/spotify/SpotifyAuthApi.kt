package com.quantumslate.dashboard.data.remote.spotify

import com.google.gson.annotations.SerializedName
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Spotify's token endpoint. Separate from the Web API service because it lives on a
 * different host and speaks form-encoding rather than JSON.
 */
interface SpotifyAuthApiService {

    @FormUrlEncoded
    @POST("api/token")
    suspend fun exchangeCode(
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("client_id") clientId: String,
        @Field("code_verifier") codeVerifier: String
    ): SpotifyTokenResponse

    @FormUrlEncoded
    @POST("api/token")
    suspend fun refreshToken(
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("refresh_token") refreshToken: String,
        @Field("client_id") clientId: String
    ): SpotifyTokenResponse

    companion object {
        const val BASE_URL = "https://accounts.spotify.com/"
    }
}

data class SpotifyTokenResponse(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("token_type") val tokenType: String? = null,
    @SerializedName("expires_in") val expiresIn: Long = 0,
    /** Absent on a refresh response when Spotify chooses to keep the existing one. */
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("scope") val scope: String? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("error_description") val errorDescription: String? = null
)
