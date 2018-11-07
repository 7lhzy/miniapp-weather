package cn.edu.pku.zy.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.os.Message;


import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;


import cn.edu.pku.zy.bean.TodayWeather;
import cn.edu.pku.zy.util.NetUtil;
import cn.edu.pku.zy.util.PinYin;
import dalvik.annotation.TestTarget;


public class MainActivity extends Activity implements View.OnClickListener{
    private static final int UPDATE_TODAY_WEATHER=1;
    private ImageView mUpdateBtn;
    private ImageView mCitySelect;
    private TextView cityTv,timeTv,humidityTv,weekTv,pmDataTv,pmQualityTv,temperatureTv,climateTv,windTv,city_name_Tv;
    private ImageView weatherImg,pmImg;
    private ProgressBar mProgressBar;
    public int intCounter=0;
    private Handler mHander=new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);
        mUpdateBtn = (ImageView)findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);
        mProgressBar=(ProgressBar) findViewById(R.id.title_update_progress);
        mCitySelect=(ImageView)findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);
        //mUpdateBtn.setOnTouchListener(this);
        if(NetUtil.getNetworkState(this)!=NetUtil.NETWORK_NONE){
            Log.d("myWeather","网络OK");
            Toast.makeText(MainActivity.this,"网络OK",Toast.LENGTH_LONG).show();;
        }else{
            Log.d("myWeather","网络无法连接");
            Toast.makeText(MainActivity.this,"网络无法连接",Toast.LENGTH_LONG).show();
        }
        initView();
    }
    public void onClick(View view){//点击事件
        if(view.getId()==R.id.title_city_manager){//点击城市列表
            SharedPreferences sharedPreferences = getSharedPreferences("config",MODE_PRIVATE);//获取sp
            Intent i = new Intent(this,SelectCity.class).putExtra("nowCity",
                    sharedPreferences.getString("city","北京"));//sp城市名传给selectcity，默认值为 北京
            //startActivity(i);
            startActivityForResult(i,1);//跳转
        }
        if(view.getId()==R.id.title_update_btn){//点击更新按钮
            SharedPreferences sharedPreferences = getSharedPreferences("config",MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code","101010100");//获取sp里的城市代码，默认值为北京代码
            Log.d("myWeather",cityCode);
            if(NetUtil.getNetworkState(this)!=NetUtil.NETWORK_NONE){
                mUpdateBtn.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                Log.d("myWeather","网络OK");
                queryWeatherCode(cityCode);
            }else
            {
                Log.d("myWeather","网络无法连接");
                Toast.makeText(MainActivity.this, "网络无法连接", Toast.LENGTH_LONG).show();
            }

        }
    }
    private void queryWeatherCode(String cityCode) {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather", address);
        new Thread(new Runnable() {//新线程去执行
            @Override
            public void run() {
                HttpURLConnection con = null;
                TodayWeather todayWeather = null;
                try {//构建http连接
                    URL url = new URL(address);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null) {
                        response.append(str);
                        Log.d("myWeather", str);
                    }
                    String responseStr = response.toString();
                    Log.d("myWeather", responseStr);
                    //todayWeather=todayWeather.parseXML(responseStr);
                    todayWeather=parseXML(responseStr);//调用解析函数
                    if (todayWeather != null) {//成功解析后
                        Log.d("myWeather", todayWeather.toString());
                        Message msg=new Message();
                        msg.what=UPDATE_TODAY_WEATHER;
                        msg.obj=todayWeather;
                        mHander.sendMessage(msg);//成功后数据传给UI线程
                    }
                    //parseXML(responseStr);
                } catch (Exception e) {//不成功抛出异常
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();//关闭连接
                    }
                }
            }
        }).start();
    }

    public TodayWeather parseXML(String xmldata){
        TodayWeather todayWeather=null;
        int fengxiangCount=0;
        int fengliCount=0;
        int dateCount=0;
        int highCount=0;
        int lowCount=0;
        int typeCount=0;
        try{
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser=fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather","parseXML");
            while(eventType!=XmlPullParser.END_DOCUMENT){
                switch (eventType){//判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:break;//判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp")){
                            todayWeather=new TodayWeather();
                        }
                        if(todayWeather!=null){
                            if(xmlPullParser.getName().equals("city")){
                                eventType=xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("updatetime")){
                                eventType=xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("shidu")){
                                eventType=xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("wendu")){
                                eventType=xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("pm25")){
                                eventType=xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("quality")){
                                eventType=xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("fengxiang")&&fengxiangCount==0){
                                eventType=xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            }else if(xmlPullParser.getName().equals("fengli")&&fengliCount==0){
                                eventType=xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengliCount++;
                            }else if(xmlPullParser.getName().equals("date")&&dateCount==0){
                                eventType=xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                            }else if(xmlPullParser.getName().equals("high")&&highCount==0){
                                eventType=xmlPullParser.next();
                                todayWeather.setHigh((xmlPullParser.getText().substring(2).trim()));
                                highCount++;
                            }else if(xmlPullParser.getName().equals("low")&&lowCount==0){
                                eventType=xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }else if(xmlPullParser.getName().equals("type")&&typeCount==0){
                                eventType=xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                typeCount++;
                            }
                        }
                        break;//判断当前事件是否为标签元素结束事件
                    case XmlPullParser.END_TAG:break;
                }
                eventType=xmlPullParser.next();//进入下一个元素并触发相应事件
            }
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return todayWeather;
    }

    void initView(){//初始化布局
        city_name_Tv=(TextView)findViewById(R.id.title_city_name);
        cityTv=(TextView)findViewById((R.id.city));
        timeTv=(TextView)findViewById(R.id.time);
        humidityTv=(TextView)findViewById(R.id.humidity);
        weekTv=(TextView)findViewById(R.id.week_today);
        pmDataTv=(TextView)findViewById(R.id.pm_data);
        pmQualityTv=(TextView)findViewById(R.id.pm2_5_quality);
        pmImg=(ImageView)findViewById(R.id.pm2_5_img);
        temperatureTv=(TextView)findViewById(R.id.temperature);
        climateTv=(TextView)findViewById(R.id.climate);
        windTv=(TextView)findViewById(R.id.wind);
        weatherImg=(ImageView)findViewById(R.id.weather_img);
        SharedPreferences sharedPreferences = getSharedPreferences("config",MODE_PRIVATE);
        city_name_Tv.setText(sharedPreferences.getString("city","N/A"));
        cityTv.setText(sharedPreferences.getString("city","N/A"));
        timeTv.setText(sharedPreferences.getString("time","N/A"));
        humidityTv.setText(sharedPreferences.getString("humidity","N/A"));
        pmDataTv.setText(sharedPreferences.getString("pmData","N/A"));
        pmQualityTv.setText(sharedPreferences.getString("pmQuality","N/A"));
        weekTv.setText(sharedPreferences.getString("week","N/A"));
        temperatureTv.setText(sharedPreferences.getString("temperature_high","N/A")+" "+sharedPreferences.getString("temperature_low", ""));
        climateTv.setText(sharedPreferences.getString("climate","N/A"));
        windTv.setText(sharedPreferences.getString("wind","N/A"));
        String nowPM = sharedPreferences.getString("pmDate","0");
        int pmValue=0;
        if(nowPM!=null)
            pmValue=Integer.parseInt(nowPM.trim());
        String pmImgStr = "0_50";
        if (pmValue>50 && pmValue < 201) {//构建50-200pm的图片名
            int startV = (pmValue - 1) / 50 * 50 + 1;
            int endV = ((pmValue - 1) / 50 + 1) * 50;
            pmImgStr = Integer.toString(startV) + "_" + endV;
        }
        else if (pmValue>=201 && pmValue < 301){//构建200-300pm的图片名
            pmImgStr= "201_300";
        }
        else if (pmValue >= 301) {//构建300以上pm的图片名
            pmImgStr = "greater_300";
        }//否则使用默认的小于50的图片名
        String typeImg = "biz_plugin_weather_" +
                PinYin.converterToSpell(sharedPreferences.getString("climate", "晴"));//汉字转拼音，同时构建天气图片名
        Class aClass = R.drawable.class;
        int typeId = -1;
        int pmImgId = -1;
        try{
            //一般尽量采用这种形式
            Field field = aClass.getField(typeImg);//反射
            Object value = field.get(new Integer(0));
            typeId = (int)value;//得到对应r.id.的值
            Field pmField = aClass.getField("biz_plugin_weather_" + pmImgStr);//反射
            Object pmImgO = pmField.get(new Integer(0));
            pmImgId = (int) pmImgO;//得到对应r.id.的值
        }catch(Exception e){
            //e.printStackTrace();
            if ( -1 == typeId)
                typeId = R.drawable.biz_plugin_weather_qing;
            if ( -1 == pmImgId)
                pmImgId= R.drawable.biz_plugin_weather_0_50;
        }finally {
            Drawable drawable = getResources().getDrawable(typeId);
            weatherImg.setImageDrawable(drawable);
            drawable = getResources().getDrawable(pmImgId);
            pmImg.setImageDrawable(drawable);
            Toast.makeText(MainActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
        }
    }


    void updateTodayWeather(TodayWeather todayWeather){
        SharedPreferences sharedPreferences=getSharedPreferences("config",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        mProgressBar.setVisibility(View.GONE);
        mUpdateBtn.setVisibility(View.VISIBLE);
        city_name_Tv.setText(todayWeather.getCity()+"天气");
        cityTv.setText((todayWeather.getCity()));
        timeTv.setText(todayWeather.getUpdatetime()+"发布");
        humidityTv.setText("湿度:"+todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh()+"~"+todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:"+todayWeather.getFengli());
        int pmValue = 0;
        String nowPM=todayWeather.getPm25();
        if(nowPM!=null){
            pmValue=Integer.parseInt(nowPM.trim());
        }
        String pmImgStr = "0_50";
        if (pmValue>50 && pmValue < 201) {
            int startV = (pmValue - 1) / 50 * 50 + 1;
            int endV = ((pmValue - 1) / 50 + 1) * 50;
            pmImgStr = Integer.toString(startV) + "_" + endV;
        }
        else if (pmValue>=201 && pmValue < 301){
            pmImgStr= "201_300";
        }
        else if (pmValue >= 301) {
            pmImgStr = "greater_300";
        }
        String typeImg  = "biz_plugin_weather_" + PinYin.converterToSpell(todayWeather.getType());
        Class aClass = R.drawable.class;
        int typeId = -1;
        int pmImgId = -1;
        try{
            //一般尽量采用这种形式
            Field field = aClass.getField(typeImg);
            Object value = field.get(new Integer(0));
            typeId = (int)value;
            Field pmField = aClass.getField("biz_plugin_weather_" + pmImgStr);
            Object pmImgO = pmField.get(new Integer(0));
            pmImgId = (int) pmImgO;
        }catch(Exception e){
            //e.printStackTrace();
            if ( -1 == typeId)
                typeId = R.drawable.biz_plugin_weather_qing;
            if ( -1 == pmImgId)
                pmImgId= R.drawable.biz_plugin_weather_0_50;
        }finally {
            Drawable drawable = getResources().getDrawable(typeId);
            weatherImg.setImageDrawable(drawable);
            drawable = getResources().getDrawable(pmImgId);
            pmImg.setImageDrawable(drawable);
            Toast.makeText(MainActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
        }
        editor.putString("city",todayWeather.getCity());
        editor.putString("time",todayWeather.getUpdatetime());
        editor.putString("humidity",todayWeather.getShidu());
        editor.putString("pmData",todayWeather.getPm25());
        editor.putString("pmQuality",todayWeather.getQuality());
        editor.putString("week",todayWeather.getDate());
        editor.putString("temperature_high",todayWeather.getHigh());
        editor.putString("temperature_low",todayWeather.getLow());
        editor.putString("climate",todayWeather.getType());
        editor.putString("wind",todayWeather.getFengli());
        editor.commit();
        Toast.makeText(MainActivity.this,"更新成功",Toast.LENGTH_SHORT).show();
    }


    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode == 1&&resultCode == 10){
            String newCityCode=data.getStringExtra("cityCode");
            Log.d("myWeather","选择的城市代码为"+newCityCode);
            SharedPreferences sharedPreferences = getSharedPreferences("config",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("main_city_code",newCityCode);
            editor.commit();
            if(NetUtil.getNetworkState(this)!=NetUtil.NETWORK_NONE){
                Log.d("myWeather","网络OK");
                queryWeatherCode(newCityCode);
            }else{
                Log.d("myWeather","网络无法连接");
                Toast.makeText(MainActivity.this, "网络无法连接", Toast.LENGTH_LONG).show();
            }
        }
    }
}
