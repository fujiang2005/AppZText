package com.example.appztext;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import com.tencent.connect.common.Constants;
import com.example.appztext.utils.CounstanUtils;
import com.example.appztext.utils.Util;
import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQAuth;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends Activity implements OnClickListener{
	EditText et_name,et_pass;
	Button bt_login;
	//qq主操作对象
	Tencent mTencent;
	//qq用户信息
	UserInfo userIn;
	//授权登录监视器
	IUiListener loginIuilistener;
	
	static QQAuth qqAuth;

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        listener();
    }
    @Override
    protected void onStart() {
    	// TODO Auto-generated method stub
    	qqAuth=QQAuth.createInstance(CounstanUtils.APP_ID, getApplicationContext());
    	mTencent=Tencent.createInstance(CounstanUtils.APP_ID, MainActivity.this);
    	super.onStart();
    }
    public void init(){
    	et_name=(EditText) findViewById(R.id.ed_uname);
    	et_pass=(EditText) findViewById(R.id.ed_upass);
    	bt_login=(Button) findViewById(R.id.bt_ulogin);
    }
    public void listener(){
    	bt_login.setOnClickListener(this);
    }
    public void userUpdataInfo(){
    	if(qqAuth!=null && qqAuth.isSessionValid()){
    		IUiListener listene=new IUiListener() {
				
				@Override
				public void onError(UiError arg0) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onComplete(Object arg0) {
					JSONObject jso=(JSONObject) arg0;
					Util.showResultDialog(MainActivity.this, jso.toString(), "登录成功");
				}
				
				@Override
				public void onCancel() {
					// TODO Auto-generated method stub
					
				}
			};
			userIn=new UserInfo(this, qqAuth.getQQToken());
			userIn.getUserInfo(listene);
    	}
    }
    public static boolean ready(Context context){
    	if(qqAuth==null){
    		return false ;
    	}
    	boolean ready=qqAuth.isSessionValid()
    			&& qqAuth.getQQToken().getOpenId()!=null;
    	if(!ready){
    		Toast.makeText(context, "login and get openId first,please", Toast.LENGTH_SHORT).show();
    	}
    	return ready;
    }
    class BaseListener implements IUiListener{

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onComplete(Object arg0) {
			// TODO Auto-generated method stub
			JSONObject jso=(JSONObject) arg0;
			Util.showResultDialog(MainActivity.this, jso.toString(), "BaseListener 回调登录成功");
			doComplete(jso);
		}
		protected void doComplete(JSONObject response) {
			// TODO Auto-generated method stub

		}
		
		@Override
		public void onError(UiError arg0) {
			// TODO Auto-generated method stub
			
		}
    	
    }
    public void onclick(){
    	if(!qqAuth.isSessionValid()){
    		loginIuilistener=new BaseListener(){
    			
    			@Override
    			protected void doComplete(JSONObject response) {
    				//无法执行到里面，应该不是重写BaseListener的方法,而使用proctected 则可以在多态中使用
    				initOpenid(response);
    				userUpdataInfo();
    				super.doComplete(response);
    			}
    			
        	};
        	qqAuth.login(this, CounstanUtils.SCOPE, loginIuilistener);
        	mTencent.login(this, CounstanUtils.SCOPE, loginIuilistener);
    	}else{
    		qqAuth.logout(this);
    		userUpdataInfo();
    	}
    	
    }
    public  void initOpenid(JSONObject values){
    	try {
			String token=values.getString(Constants.PARAM_ACCESS_TOKEN);
			String openid=values.getString(Constants.PARAM_OPEN_ID);
			String exp=values.getString(Constants.PARAM_EXPIRES_IN);
			//因为使用的是QQAuth所以需要设置QQAuth的id和token属性，设置Tencent的属性无效
			qqAuth.setOpenId(getApplicationContext(), openid);
			qqAuth.setAccessToken(token, exp);
//			mTencent.setOpenId(openid);
//			mTencent.setAccessToken(token, exp);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
   
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
	@Override
	public void onClick(View v) {
		// 
		Context contxt=(Context) v.getParent();
		Class<?> cls=null;
		switch (v.getId()) {
		case R.id.bt_ulogin:
			onclick();
			bt_login.setTextColor(Color.RED);
			break;
			
		default:
			break;
		}
		if(cls!=null){
			Intent intent=new Intent(contxt, cls);
			startActivity(intent);
		}
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
		super.onActivityResult(requestCode, resultCode, data);
		 Tencent.onActivityResultData(requestCode, resultCode, data, loginIuilistener);

		    if(requestCode == Constants.REQUEST_API) {
		        if(resultCode == Constants.REQUEST_APPBAR) {
		            Tencent.handleResultData(data, loginIuilistener);
		        }
		    }
	}
	
	/*
	 * 判断是否支持SSO登录
	 */
	private void isSessionSSO(){
		if(mTencent.isSupportSSOLogin(MainActivity.this)){
			Toast.makeText(MainActivity.this, "支持SSO登录", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(MainActivity.this, "不支持SSO登录", Toast.LENGTH_SHORT).show();
		}
	}
}
