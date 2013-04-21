package ua.ck.android.happyeggs;

import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import ua.ck.android.happyeggs.adapters.ScrollAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.bump.api.BumpAPIIntents;
import com.bump.api.IBumpAPI;
import com.devsmart.android.ui.HorizontalListView;
import com.google.analytics.tracking.android.EasyTracker;


public class MainActivity extends SherlockActivity implements AdapterView.OnItemClickListener {
	private final String JSON_TAG = "attack";
	private IBumpAPI api;
	private final String tag ="!!!CHEB!!!";
	private boolean bumpStatus; 
	private int myNumber = 0, hisNumber = 0;
	
	@Override
    protected void onStart() {
        EasyTracker.getInstance().activityStart(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bumpStatus = false;
        setContentView(R.layout.activity_main);
        ImageView imgEgg = (ImageView)findViewById(R.id.imgEgg);
        HorizontalListView listview = (HorizontalListView) findViewById(R.id.listview);
        ScrollAdapter adapter = new ScrollAdapter(30, getApplicationContext());
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(this);
        initBump();
    }
    
    private void initBump(){
		bindService(new Intent(IBumpAPI.class.getName()), connection, Context.BIND_AUTO_CREATE);    
		IntentFilter filter = new IntentFilter();
        filter.addAction(BumpAPIIntents.CHANNEL_CONFIRMED);
        filter.addAction(BumpAPIIntents.DATA_RECEIVED);
        filter.addAction(BumpAPIIntents.NOT_MATCHED);
        filter.addAction(BumpAPIIntents.MATCHED);
        filter.addAction(BumpAPIIntents.CONNECTED);
        registerReceiver(receiver, filter);
	}
    
    private byte[] sendNumber(){
    	int min = 1;
    	int max = 100;
    	Random r = new Random();
    	myNumber = r.nextInt(max - min + 1) + min;
    	try {
    		JSONObject jRoot = new JSONObject();
			jRoot.put(JSON_TAG, myNumber);
			return jRoot.toString().getBytes();
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	return "".getBytes();
    	
    }
    
    private void checkNumber(String data){
    	try{
    		JSONObject jRoot = new JSONObject(data);
    		hisNumber = jRoot.optInt(JSON_TAG);
    		if (hisNumber > myNumber){
    			Toast.makeText(getApplicationContext(), "You lose", Toast.LENGTH_SHORT).show();
    		}else if (hisNumber < myNumber){
    			Toast.makeText(getApplicationContext(), "You Win", Toast.LENGTH_SHORT).show();
    		}else{
    			Toast.makeText(getApplicationContext(), "Tie", Toast.LENGTH_SHORT).show();
    		}
    	}catch (JSONException e){
    		e.printStackTrace();
    	}
    }
        
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        final String action = intent.getAction();
	        try {
	        	Log.i(tag,"Recive something");
	        	if (action.equals(BumpAPIIntents.DATA_RECEIVED)) {
	        		checkNumber(new String(intent.getByteArrayExtra("data")));	  
	            } else if (action.equals(BumpAPIIntents.MATCHED)) {
	                api.confirm(intent.getLongExtra("proposedChannelID", 0), true);
	            } else if (action.equals(BumpAPIIntents.CHANNEL_CONFIRMED)) {	            	
	            	api.send(intent.getLongExtra("channelID", 0), sendNumber());
	            } else if (action.equals(BumpAPIIntents.CONNECTED)) {
	                api.enableBumping();
	                bumpStatus = true;
	                invalidateOptionsMenu();
	                Log.i("Status","ok");
	            } else{
	            	Log.i(tag,"Get this action: "+action.toString());
	            	Toast.makeText(getApplicationContext(), "No contact, try again", Toast.LENGTH_SHORT).show();		            
	            }	            
	        } catch (RemoteException e) {}
	    }
	};
	
	private final ServiceConnection connection = new ServiceConnection() {	    
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.i(tag,"onServiceConnected");
	        api = IBumpAPI.Stub.asInterface(binder);	        
	        Log.i(tag,"after IBumpAPI.Stub.asInterface(binder)");
	        try {
	            new Thread(new Runnable() {					
					public void run() {
						try{
							Log.i(tag,"Inner Try! before api.configured");
							api.configure("de703e6680454adbbf3d1ac99727c9b0", "Cheb");//Max ID
							//api.configure("004d36464fba4d8a99db91ab389929c7", "Cheb");//New Cheb ID
							//api.configure("b00609a8b2f143edba70f8e0bee2754e", "Cheb");//Old ChebID
							Log.i(tag,"Inner Try! after api.configured");
						}catch (RemoteException e) {
							Log.i(tag,"RemoteException error: "+ e.toString());
						}
					}
				}).start();        	
	        } catch (Exception e) {
	        	Log.i(tag,"catch error: "+ e.toString());	
	        }	        	
	    }
		public void onServiceDisconnected(ComponentName name) {}
	};
	

    
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Toast.makeText(getApplicationContext(), "Choosen", Toast.LENGTH_LONG).show();		
	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main, menu);
		if (bumpStatus){
			MenuItem statusMenu = menu.findItem(R.id.action_status);
			statusMenu.setIcon(R.drawable.ic_action_ok);
		}
		shareMenuItem(menu);
		return true;
	}
	
	private void shareMenuItem(Menu menu){
		MenuItem menuItem = menu.findItem(R.id.share);
		ShareActionProvider mShareActionProvider = (ShareActionProvider) menuItem.getActionProvider();
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.sharesubject));
		shareIntent.putExtra(Intent.EXTRA_SUBJECT,getString(R.string.sharetext));
		mShareActionProvider.setShareIntent(shareIntent);
	}    
  
}
