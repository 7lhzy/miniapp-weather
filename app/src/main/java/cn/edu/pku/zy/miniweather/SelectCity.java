package cn.edu.pku.zy.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.List;

import cn.edu.pku.zy.bean.City;

public class SelectCity extends Activity  implements View.OnClickListener {
    private ImageView mBackBtn;
    private ListView mList;
    private List<City> cityList;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);
       // mBackBtn=(ImageView)findViewById(R.id.title_back);
       // mBackBtn.setOnClickListener(this);
        initViews();
    }
    public void onClick(View v){
        switch (v.getId()){
            case R.id.title_back:
                Intent i=new Intent();
                i.putExtra("cityCode","101160101");
                setResult(RESULT_OK,i);
                finish();
                break;
            default:
                break;
        }
    }
    private void initViews(){

        mBackBtn = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);


    }
}
