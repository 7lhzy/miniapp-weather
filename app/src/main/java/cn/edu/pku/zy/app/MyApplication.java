package cn.edu.pku.zy.app;

import android.app.Application;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import cn.edu.pku.zy.bean.City;
import cn.edu.pku.zy.db.CityDB;
import cn.edu.pku.zy.miniweather.R;

public class MyApplication extends Application{
    private static final String TAG="MyAPP";
    private static MyApplication mApplication;
    private CityDB mCityDB;
    private ArrayList<City> mCityList;
    private Map<String, Integer> mWeatherIcon;
    public Map<String, Integer> getWeatherIconMap() {
        return mWeatherIcon;
    }



    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(TAG,"MyApplication->onCreate");
        mApplication=this;
        mCityDB=openCityDB();
        initCityList();
    }
    /*private void initCityList(){
        mCityList=new ArrayList<City>();
        new Thread(new Runnable(){
            public void run(){
                prepareCityList();
            }
        }).start();
    }*/
    private void initCityList(){
        mCityList = new ArrayList<City>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                prepareCityList();
            }
        }).start();
    }
    private boolean prepareCityList(){
        mCityList = mCityDB.getAllCity();
        int i=0;
        for(City city:mCityList){
            i++;
            String cityName=city.getCity();
            String cityCode=city.getNumber();
            Log.d(TAG,cityCode+":"+cityName);
        }
        Log.d(TAG,"i="+i);
        return true;
    }

    public ArrayList<City> getCityList() {
        return mCityList;
    }

    public static MyApplication getInstance(){
        return mApplication;
    }
    private CityDB openCityDB(){
        String path="/data"
                + Environment.getDataDirectory().getAbsolutePath()
                + File.separator+getPackageName()
                +File.separator+"databases1"
                +File.separator
                +CityDB.CITY_DB_NAME;
        File db=new File(path);
        Log.d(TAG,db.toString());
        Log.d(TAG,path);
        if(!db.exists()){
            String pathfolder = "/data"
                    + Environment.getDataDirectory().getAbsolutePath()
                    + File.separator+getPackageName()
                    +File.separator+"databases1"
                    +File.separator;
            File dirFirstFolder = new File(pathfolder);
            if(!dirFirstFolder.exists()){
                dirFirstFolder.mkdirs();
                Log.i("MyApp","mkdirs");
            }
            Log.i("MyApp","db is not exists");
            try{
                InputStream is = getAssets().open("city.db");
                FileOutputStream fos = new FileOutputStream(db);
                int len =-1;
                byte[] buffer =new byte[1024];
                while((len=is.read(buffer))!=-1){
                    fos.write(buffer,0,len);
                    fos.flush();
                }
                fos.close();
                is.close();
            }catch (IOException e){
                e.printStackTrace();
                System.exit(0);
            }
        }
        return new CityDB(this,path);
    }
    public int getWeatherIcon(String climate) {
        int weatherRes = R.drawable.biz_plugin_weather_qing;
        if (TextUtils.isEmpty(climate))
            return weatherRes;
        String[] strs = { "晴", "晴" };
        if (climate.contains("转")) {// 天气带转字，取前面那部分
            strs = climate.split("转");
            climate = strs[0];
            if (climate.contains("到")) {// 如果转字前面那部分带到字，则取它的后部分
                strs = climate.split("到");
                climate = strs[1];
            }
        }
        if (mWeatherIcon.containsKey(climate)) {
            weatherRes = mWeatherIcon.get(climate);
        }
        return weatherRes;
    }

}
