package cn.edu.pku.zy.miniweather;

import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;

import java.util.List;

import cn.edu.pku.zy.app.MyApplication;
import cn.edu.pku.zy.bean.City;

public class LocationListener extends BDAbstractLocationListener {
    public String recity;
    public String cityCode;
    public void onReceiveLocation(BDLocation location){
        String addr=location.getAddrStr();
        String country=location.getCountry();
        String province=location.getProvince();
        String city=location.getCity();
        String district=location.getDistrict();
        String street=location.getStreet();
        Log.d("location_city",city);
        recity=city.replace("市","");
        List<City> mCityList;
        MyApplication myApplication;
        myApplication=MyApplication.getInstance();
        mCityList=myApplication.getCityList();
        for(City city1:mCityList){
            if(city1.getCity().equals(recity));
            cityCode=city1.getNumber();
            Log.d("location_code",cityCode);
        }
    }

}
