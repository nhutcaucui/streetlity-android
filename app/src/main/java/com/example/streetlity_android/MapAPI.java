package com.example.streetlity_android;

import org.json.JSONArray;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

public interface MapAPI {
    @GET("service/fuel/all")
    Call<ResponseBody> getAllFuel();

    @GET("service/atm/all")
    Call<ResponseBody> getAllATM();

    @GET("service/toilet/all")
    Call<ResponseBody> getAllWC();

    @GET("service/maitain/all")
    Call<ResponseBody> getAllMaintenance();

    @GET("service/fuel/range")
    Call<ResponseBody> getFuelInRange(@Header("Version") String version, @Query("location") float lat, @Query("location") float lon,
                                      @Query("range") float range);

    @GET("service/atm/range")
    Call<ResponseBody> getATMInRange(@Header("Version") String version, @Query("location") float lat, @Query("location") float lon,
                                      @Query("range") float range);

    @GET("service/toilet/range")
    Call<ResponseBody> getWCInRange(@Header("Version") String version, @Query("location") float lat, @Query("location") float lon,
                                      @Query("range") float range);

    @GET("service/maintenance/range")
    Call<ResponseBody> getMaintenanceInRange(@Header("Version") String version, @Query("location") float lat, @Query("location") float lon,
                                    @Query("range") float range);

    @GET("service/range")
    Call<ResponseBody> getServiceInRange(@Query("location") float lat, @Query("location") float lon,
                                         @Query("range") float range);

    @FormUrlEncoded
    @POST("service/fuel/create")
    Call<ResponseBody> addFuel(@Header("Version") String version, @Header("Auth") String token, @Field("location") float lat, @Field("location" )float lon,
                               @Field("address") String address, @Field("note") String note, @Field("images") String[] images);

    @FormUrlEncoded
    @POST("service/atm/create")
    Call<ResponseBody> addATM(@Header("Version") String version, @Header("Auth") String token,@Field("location") float lat, @Field("location" )float lon,
                              @Field("bank_id") int bankId, @Field("address") String address, @Field("note") String note, @Field("images") String[] images);

    @FormUrlEncoded
    @POST("service/toilet/create")
    Call<ResponseBody> addWC(@Header("Version") String version, @Header("Auth") String token,@Field("location") float lat, @Field("location" )float lon,
                             @Field("address") String address, @Field("note") String note, @Field("images") String[] images);

    @FormUrlEncoded
    @POST("service/maintenance/create")
    Call<ResponseBody> addMaintenance(@Header("Version") String version, @Header("Auth") String token,@Field("location") float lat, @Field("location" )float lon,
                                      @Field("address") String address, @Field("name") String name, @Field("note") String note);

    @GET("json")
    Call<ResponseBody> geocode(@Query("address") String address, @Query("key") String key);

    @FormUrlEncoded
    @POST("user/login")
    Call<ResponseBody> login(@Field("username") String username, @Field("passwd") String password,
                             @Field("deviceToken") String deviceToken);

    @FormUrlEncoded
    @POST("user/common/register")
    Call<ResponseBody> signUpCommon(@Field("username") String username, @Field("passwd") String password,
                                    @Field("email") String email, @Field("phone") String phone, @Field("address") String address);

    @FormUrlEncoded
    @POST("user/maintenance/register")
    Call<ResponseBody> signUpMaintainer(@Field("username") String username, @Field("passwd") String password,
                                        @Field("email") String email, @Field("phone") String phone,
                                        @Field("address") String address, @Field("serviceId") int id);

    @FormUrlEncoded
    @POST("user/logout")
    Call<ResponseBody> logout(@Header("Auth") String token, @Field("username") String username,
                              @Field("deviceToken") String deviceToken);

    @FormUrlEncoded
    @POST("service/maintenance/order")
    Call<ResponseBody> broadcast(@Header("Version") String version, @Field("commonUser") String username, @Field("reason") String reason, @Field("name") String name,
                                 @Field("phone") String phone, @Field("note") String note,
                                 @Field("id") int[] id, @Field("address") String address, @Field("preferTime") String time);

    @FormUrlEncoded
    @POST("user/device")
    Call<ResponseBody> addDevice(@Header("Version") String version ,@Field("token") String token, @Field("user") String username);

    @FormUrlEncoded
    @POST("service/atm/bank/create")
    Call<ResponseBody> addBank(@Header("Version") String version ,@Field("token") String token, @Field("name") String name);

    @GET("service/atm/bank/all")
    Call<ResponseBody> getBank(@Header("Version") String version ,@Query("token") String token);

    @GET("user/validate/email")
    Call<ResponseBody> validateEmail(@Query("email") String email);

    @GET("user/validate/user")
    Call<ResponseBody> validateUser(@Query("username") String username);

    @Multipart
    @POST()
    Call<ResponseBody> upload(@Url String url,@Part List<MultipartBody.Part> body);

    @GET("/")
    Call<ResponseBody> download(@Query("f") String name);

    @GET("service/fuel/review/query")
    Call<ResponseBody> getFuelReview(@Header("Version") String version ,@Query("service_id") int id
            , @Query("order") int order, @Query("limit") int limit);

    @GET("service/atm/review/query")
    Call<ResponseBody> getAtmReview(@Header("Version") String version ,@Query("service_id") int id
            , @Query("order") int order, @Query("limit") int limit);

    @GET("service/maintenance/review/query")
    Call<ResponseBody> getMaintenanceReview(@Header("Version") String version ,@Query("service_id") int id
            , @Query("order") int order, @Query("limit") int limit);

    @GET("service/toilet/review/query")
    Call<ResponseBody> getWCReview(@Header("Version") String version ,@Query("service_id") int id
            , @Query("order") int order, @Query("limit") int limit);

    @FormUrlEncoded
    @POST("service/fuel/review/create")
    Call<ResponseBody> createFuelReview(@Header("Version") String version ,@Field("service_id") int id
                                        ,@Field("reviewer") String username, @Field("score") float rating,
                                        @Field("body") String comment);
    @FormUrlEncoded
    @POST("service/atm/review/create")
    Call<ResponseBody> createAtmReview(@Header("Version") String version ,@Field("service_id") int id
            ,@Field("reviewer") String username, @Field("score") float rating,
                                        @Field("body") String comment);
    @FormUrlEncoded
    @POST("service/maintenance/review/create")
    Call<ResponseBody> createMaintenanceReview(@Header("Version") String version ,@Field("service_id") int id
            ,@Field("reviewer") String username, @Field("score") float rating,
                                        @Field("body") String comment);
    @FormUrlEncoded
    @POST("service/toilet/review/create")
    Call<ResponseBody> createWCReview(@Header("Version") String version ,@Field("service_id") int id
            ,@Field("reviewer") String username, @Field("score") float rating,
                                               @Field("body") String comment);

    @FormUrlEncoded
    @POST("service/fuel/review/")
    Call<ResponseBody> updateFuelReview(@Header("Version") String version ,@Field("review_id") int id,
            @Field("score") float rating,
                                        @Field("new_body") String comment);
    @FormUrlEncoded
    @POST("service/atm/review/")
    Call<ResponseBody> updateATMReview(@Header("Version") String version ,@Field("review_id") int id,
                                        @Field("score") float rating,
                                        @Field("new_body") String comment);
    @FormUrlEncoded
    @POST("service/maintenance/review/")
    Call<ResponseBody> updateMaintenanceReview(@Header("Version") String version ,@Field("review_id") int id,
                                        @Field("score") float rating,
                                        @Field("new_body") String comment);
    @FormUrlEncoded
    @POST("service/toilet/review/")
    Call<ResponseBody> updateWCReview(@Header("Version") String version ,@Field("review_id") int id,
                                        @Field("score") float rating,
                                        @Field("new_body") String comment);

    @HTTP(method="DELETE", path="service/fuel/review/", hasBody = true)
    Call<ResponseBody> deleteFuelReview(@Header("Version") String version ,@Field("review_id") int id);
    @DELETE("service/atm/review/")
    Call<ResponseBody> deleteATMReview(@Header("Version") String version ,@Query("review_id") int id);
    @DELETE("service/maintenance/review/")
    Call<ResponseBody> deleteMaintenanceReview(@Header("Version") String version ,@Query("review_id") int id);
    @DELETE("service/toilet/review/")
    Call<ResponseBody> deleteWCReview(@Header("Version") String version ,@Query("review_id") int id);
}
