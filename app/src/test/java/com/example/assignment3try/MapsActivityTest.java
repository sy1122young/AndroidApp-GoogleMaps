package com.example.assignment3try;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.junit.*;
import static org.junit.Assert.*;


public class MapsActivityTest {
    @Test
    public void ValidWebCamUrl() {
        String generatedUrl = MapsActivity.getWebCamUrl(10, 10);
        String expectedUrl = "https://api.windy.com/api/webcams/v2/list/limit=5,0/nearby=10.0,10.0,50?show=webcams:location;?&key=a8QislEEDfgpF3c48QIIUeJWzqXf8K6X";
        assertEquals("Correctly generates Url for webcams",expectedUrl, generatedUrl);
    }

    @Test
    public void ValidWebCamIDUrl() {
        String generatedUrl = DetailsActivity.getCamIdUrl("1234");
        String expectedUrl = "https://api.windy.com/api/webcams/v2/list/webcam=1234?show=webcams:location,image;&key=a8QislEEDfgpF3c48QIIUeJWzqXf8K6X";
        assertEquals("Correctly generates Url for a specific webcam id",expectedUrl, generatedUrl);
    }

    @Test
    public void ValidWeatherUrl() {
        String generatedUrl = MapsActivity.getWeatherUrl(10, 10);
        String expectedUrl = "https://api.openweathermap.org/data/2.5/weather?lat=10.0&lon=10.0&appid=f312c527574103fdab33428dc72292f5";
        assertEquals("Correctly generates Url for weather",expectedUrl, generatedUrl);
    }
}