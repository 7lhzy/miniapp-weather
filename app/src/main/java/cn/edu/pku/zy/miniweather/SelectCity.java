package cn.edu.pku.zy.miniweather;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.zy.app.MyApplication;
import cn.edu.pku.zy.bean.City;

import static android.content.ContentValues.TAG;

public class SelectCity extends Activity  implements View.OnClickListener {
    private ImageView mBackBtn;
    private ListView mList;
    private TextView mTitle;
    private SearchView mSearchView;
    private ArrayAdapter mAdapter;
    private ArrayList<City> cityList;
    private ArrayList<String> data;
    private String mNCC="";
    private static MyApplication myApplication;
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
                //Intent i=new Intent();
                //i.putExtra("cityCode","101160101");
                //setResult(RESULT_OK,i);
                finish();
                break;
            default:
                break;
        }
    }
    private void initViews(){
        Intent intent=getIntent();
        String cityinfo=intent.getStringExtra("nowCity");
        cityList=new ArrayList<City>(myApplication.getInstance().getCityList());
        data=new ArrayList<String>();
        for(City city:cityList){
            data.add(city.getCity());
        }
        mBackBtn = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);
        mList=(ListView)findViewById(R.id.title_list);
        mTitle=(TextView)findViewById(R.id.title_name);
        mSearchView=(SearchView)findViewById(R.id.search);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
        mTitle.setText("当前城市"+cityinfo);
        Log.d(TAG,mList.toString());
        //ArrayAdapter<String>adapter=new ArrayAdapter<String>(SelectCity.this,android.R.layout.simple_list_item_1,data);
        mAdapter=new ArrayAdapter<String>(SelectCity.this,android.R.layout.simple_list_item_1,data);
        mList.setAdapter(mAdapter);
        mList.setTextFilterEnabled(true);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                AlertDialog.Builder builder=new AlertDialog.Builder(SelectCity.this);
                builder.setTitle("提醒");
                String nowProv="";
                String nowCit="";
                for(City s:cityList){
                    if(s.getCity()==mAdapter.getItem(position)){
                        nowProv=s.getProvince();
                        nowCit=s.getCity();
                        mNCC=s.getNumber();
                    }
                }
                builder.setMessage("您确定将城市切换到"+nowProv+"-"+nowCit+"吗？");
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //toast("取消");
                    }
                });
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        Intent i=new Intent(SelectCity.this,MainActivity.class).putExtra("cityCode",mNCC);
                        setResult(10,i);
                        finish();
                    }
                });
                AlertDialog dialog=builder.create();
                dialog.show();
                //Toast.makeText(SelectCity.this,"你点击的是"+position,Toast.LENGTH_SHORT).show();
                //Intent i=new Intent(SelectCity.this,MainActivity.class).putExtra("cityCode",cityList.get(position).getNumber());
                //setResult(10,i);
                //finish();
            }
        });


    }
}
