package project.mweather.zero.com.mweather;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import net.ConnectionNet;
import net.ConnectionServerListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends Activity implements ConnectionServerListener {

    private static final int UPDATE_CITY = 99;
    private TextView cityName, publish_time, tv_now_weather, ziwaixian, now_temperature, tv_pm25;
    private TextView tv_chuanyi,tv_ganmao,tv_yundong;

    private ImageView img_today_morning, img_tomorrow_morning, img_aftertomorrow_morning,
            img_today_night, img_tomorrow_night, img_aftertomorrow_night,img_now_weather;

    private TextView tv_today_morning,tv_tomorrow_moring,
            tv_tomorrow_night, tv_aftertomorrow_morning, tv_aftertomorrow_night;


    private  Button btn_refesh;
    private BDLocation mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        //初始化定位
        initLocation();

        if (!isNetworkAvailable(this)){
            Toast.makeText(MainActivity.this,"亲，断网了！！",Toast.LENGTH_SHORT).show();
        }

    }
//    定位相关
    private LocationClient mLocationClient;
    private String mCity;
    private MyLocationListener mLocationListener;

    private void initLocation() {
        mLocationClient=new LocationClient(this);
        mLocationListener=new MyLocationListener();
        mLocationClient.registerLocationListener(mLocationListener);

        LocationClientOption option=new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setScanSpan(10*60*1000);
        option.setIgnoreKillProcess(false);
        mLocationClient.setLocOption(option);

    }

    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==UPDATE_CITY){
                String requestData = requestWeather();
                final ProgressDialog pd=ProgressDialog.show(MainActivity.this,"稍等....","正在获取天气...");
                new ConnectionNet(requestData, "GET", MainActivity.this).execute();
                pd.dismiss();
            }
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        if (!mLocationClient.isStarted()&&isNetworkAvailable(this)){
            mLocationClient.start();//启动定位
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocationClient.stop();//取消定位
    }

    private void initView() {

        cityName = (TextView) findViewById(R.id.tvcityname);
        publish_time = (TextView) findViewById(R.id.tv_publish_time);
        tv_now_weather = (TextView) findViewById(R.id.tv_now_weather);
        ziwaixian = (TextView) findViewById(R.id.tv_ziwaixian);
        tv_pm25 = (TextView) findViewById(R.id.tv_pm25);
        now_temperature = (TextView) findViewById(R.id.now_temperature);


        tv_chuanyi= (TextView) findViewById(R.id.chuanyi);
        tv_ganmao= (TextView) findViewById(R.id.ganmao);
        tv_yundong= (TextView) findViewById(R.id.yundong);

        tv_today_morning = (TextView) findViewById(R.id.tv_today_morning_tmp);

        tv_tomorrow_moring = (TextView) findViewById(R.id.tv_tomorrow_morning_tmp);
        tv_tomorrow_night = (TextView) findViewById(R.id.tv_tomorrow_night_tmp);
        tv_aftertomorrow_morning = (TextView) findViewById(R.id.tv_aftertomorrow_morning_tmp);
        tv_aftertomorrow_night = (TextView) findViewById(R.id.tv_aftertomorrow_night_tmp);


        img_now_weather = (ImageView) findViewById(R.id.iv_now_weather);


        img_today_morning = (ImageView) findViewById(R.id.img_today_morning);
        img_tomorrow_morning = (ImageView) findViewById(R.id.img_tomorrow_morning);
        img_aftertomorrow_morning = (ImageView) findViewById(R.id.img_aftertomorrow_morning);

        img_today_night = (ImageView) findViewById(R.id.img_today_night);
        img_tomorrow_night = (ImageView) findViewById(R.id.img_tomorrow_night);
        img_aftertomorrow_night = (ImageView) findViewById(R.id.img_aftertomorrow_night);


        btn_refesh= (Button) findViewById(R.id.btn_refesh);
        btn_refesh.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (isNetworkAvailable(MainActivity.this)){
                    startActivity(new Intent(MainActivity.this,MainActivity.class));
                    btn_refesh.setVisibility(View.GONE);
                    finish();

                }else{
                    Toast.makeText(MainActivity.this,"亲，网络走神了...",Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

//格式化请求参数。
    public String requestWeather() {
        String request = null;
        mCity=mLocation.getCity();
        Map params = new HashMap();//请求参数
        params.put("cityname",mCity);//要查询的城市，如：温州、上海、北京
        params.put("key", ConnectionNet.APPKEY);//应用APPKEY(应用详细页查询)
        params.put("dtype", "json");//返回数据的格式,xml或json，默认json
        request = urlencode(params);

        return request;
    }


    //将map型转为请求参数型
    public String urlencode(Map<String, String> data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry i : data.entrySet()) {
            try {
                sb.append(i.getKey()).append("=").append(URLEncoder.encode(i.getValue() + "", "UTF-8")).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }


    /**
     * 从接口中获取天气数据
     * @param s
     */
    @Override
    public void getData(String s) {
        if (s!=null){
            parseJSONWeather(s);
            btn_refesh.setVisibility(View.GONE);
        }else {
            Toast.makeText(MainActivity.this,"亲，断网了！！",Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 检查当前网络是否可用
     */

    public boolean isNetworkAvailable(Activity activity)
    {
        Context context = activity.getApplicationContext();
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null)
        {
            return false;
        }
        else
        {
            // 获取NetworkInfo对象
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

            if (networkInfo != null && networkInfo.length > 0)
            {
                for (int i = 0; i < networkInfo.length; i++)
                {
                    // 判断当前网络状态是否为连接状态
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 将接收的数据，解析并显示。
     * @param result
     */
    public void parseJSONWeather(String result){

        try {
            JSONObject jsonObject=new JSONObject(result);
            JSONObject res=jsonObject.getJSONObject("result");
            JSONObject data=res.getJSONObject("data");
            JSONObject realTime=data.getJSONObject("realtime");
            JSONObject life=data.getJSONObject("life");
            JSONObject pm25=data.getJSONObject("pm25");
            JSONArray threeWeather=data.getJSONArray("weather");
            JSONObject keypm25=pm25.getJSONObject("pm25");
            /* 1、头部时间和地点*/
            cityName.setText(mCity);
            String date=realTime.getString("date").substring(5);
            date=date.replace('-','/');
            String time=realTime.getString("time").substring(0,5);
            publish_time.setText(date+" "+time+" 发布");
            JSONObject nowWeather=realTime.getJSONObject("weather");
            String temperature=nowWeather.getString("temperature");
            /* 2、现在天气和温度*/
            String info=nowWeather.getString("info");//设置img_now_weather
            tv_now_weather.setText(info);
            //现在天气的图标还没设置。。。。
            chooseWeatherImage(info,img_now_weather);
            now_temperature.setText(temperature);

            /* 3、PM2.5和紫外线设置*/
            String curPm=keypm25.getString("curPm");
            String quality=keypm25.getString("quality");
            tv_pm25.setText(curPm+" "+quality);
            JSONArray ziwai=life.getJSONObject("info").getJSONArray("ziwaixian");
            Log.i("tag",ziwai.get(0).toString());
            ziwaixian.setText(ziwai.get(0).toString());


            /* 4、---------中间的3天早晚天气预报 -------------- */
            JSONObject todayWeather=threeWeather.getJSONObject(0);
            JSONArray todayDay= todayWeather.getJSONObject("info").getJSONArray("day");
            Log.i("tag", todayDay.get(2).toString());
            tv_today_morning.setText(todayDay.get(2).toString());
            chooseWeatherImage(todayDay.get(1).toString(),img_today_morning);

            JSONArray todayNight= todayWeather.getJSONObject("info").getJSONArray("night");
            Log.i("tag", todayNight.get(2).toString());
            tv_today_morning.setText(todayNight.get(2).toString());
            chooseWeatherImage(todayNight.get(1).toString(),img_today_night);

//----------------------------------分割线-----------------------------------------------
            JSONObject tomorrowWeather=threeWeather.getJSONObject(1);
            JSONArray tomorrowDay= tomorrowWeather.getJSONObject("info").getJSONArray("day");
            Log.i("tag", tomorrowDay.get(2).toString());
            tv_tomorrow_moring.setText(tomorrowDay.get(2).toString());
            chooseWeatherImage(tomorrowDay.get(1).toString(),img_tomorrow_morning);

            JSONArray tomorrowNight= tomorrowWeather.getJSONObject("info").getJSONArray("night");
            Log.i("tag", tomorrowNight.get(2).toString());
            tv_tomorrow_night.setText(tomorrowNight.get(2).toString());
            chooseWeatherImage(tomorrowNight.get(1).toString(),img_tomorrow_night);


//----------------------------------分割线-----------------------------------------------
            JSONObject afterWeather=threeWeather.getJSONObject(2);
            JSONArray afterDay= afterWeather.getJSONObject("info").getJSONArray("day");
            Log.i("tag", afterDay.get(2).toString());
            tv_aftertomorrow_morning.setText(afterDay.get(2).toString());
            chooseWeatherImage(afterDay.get(1).toString(),img_aftertomorrow_morning);

            JSONArray afterNight= afterWeather.getJSONObject("info").getJSONArray("night");
            Log.i("tag", afterNight.get(2).toString());
            tv_aftertomorrow_night.setText(afterNight.get(2).toString());
            chooseWeatherImage(afterNight.get(1).toString(),img_aftertomorrow_night);



//----------------------------------分割线-----------------------------------------------

            /* 5、温馨提示栏         */
            String chuanyi=life.getJSONObject("info").getJSONArray("chuanyi").toString();
            String ganmao=life.getJSONObject("info").getJSONArray("ganmao").toString();
            String yundong=life.getJSONObject("info").getJSONArray("yundong").toString();

            tv_chuanyi.setText(chuanyi.substring(1,chuanyi.length()-1));
            tv_ganmao.setText(ganmao.substring(1,ganmao.length()-1));
            tv_yundong.setText(yundong.substring(1,yundong.length()-1));


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 天气选择图片
     * @param weatherImg
     * @param imgId
     */
    public void chooseWeatherImage(String weatherImg,ImageView imgId){
        switch (weatherImg){
            case "晴":
                imgId.setImageResource(R.drawable.d00);
                break;
            case "多云":
                imgId.setImageResource(R.drawable.d01);
                break;

            case "阵雨":
                imgId.setImageResource(R.drawable.d03);
                break;

            case "雷阵雨":
                imgId.setImageResource(R.drawable.d04);
                break;

            case "阴天":
                imgId.setImageResource(R.drawable.d02);
                break;

            case "暴雨":
                imgId.setImageResource(R.drawable.d06);
                break;

            case "大暴雨":
                imgId.setImageResource(R.drawable.d08);
                break;
            default:
                imgId.setImageResource(R.drawable.d00);

        }

    }


    //定位相关的监听内部类。
    private class MyLocationListener implements BDLocationListener{

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            mLocation=bdLocation;
//            mCity=bdLocation.getCity();
//            Log.i("tag",mCity);
            Message message=new Message();
            message.what=UPDATE_CITY;
            mHandler.sendMessage(message);

        }
    }
}
