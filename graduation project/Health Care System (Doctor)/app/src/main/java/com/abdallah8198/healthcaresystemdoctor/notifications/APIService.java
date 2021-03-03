package com.abdallah8198.healthcaresystemdoctor.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAACMKhCeQ:APA91bGL-09MAC02h1_a_AYOSpTkkfefN1yRlgJ2BLqwbG-vwEy4J81N6zJLfUyRQbFYTme4YW3zWFu7Td5QSvScSwg_BG-2hPBVtT32QaDxTHhtMIZPR3mZSHn83VCBfLslIelaocfF"
    })


    @POST("fcm/send ")
    Call<Response> sendNotification(@Body Sender body );

}
