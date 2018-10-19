package net.happygod.jerrymouse;

import android.app.*;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.*;
import android.support.v4.app.NotificationCompat;
import java.util.*;
import net.happygod.jerrymouse.database.*;
import net.happygod.jerrymouse.server.Server;

public class WebService extends Service
{
	private static Set<Server> servers=new HashSet<>();
	private static boolean running=false;
	@Override
	public void onCreate()
	{
		super.onCreate();
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			NotificationChannel channel=new NotificationChannel("fore_service","foreground service",NotificationManager.IMPORTANCE_HIGH);
			NotificationManager notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
			notificationManager.createNotificationChannel(channel);
			Intent intentForeService=new Intent(SharedContext.get(),MainActivity.class);
			PendingIntent pendingIntent=PendingIntent.getActivity(SharedContext.get(),0,intentForeService,0);
			Notification notification=new NotificationCompat.Builder(SharedContext.get(),"fore_service").setContentTitle("Jerrymouse Web Server is running").setContentText("Touch for more options").setWhen(System.currentTimeMillis()).setSmallIcon(R.drawable.ic_launcher_white).setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher_white)).setContentIntent(pendingIntent).build();
			startForeground(1,notification);
			running=true;
			startServers();
		}
	}
	@Override
	public int onStartCommand(Intent intent,int flags,int startId)
	{
		//flags=START_STICKY;
		return super.onStartCommand(intent,flags,startId);
	}
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
	@Override
	public void onDestroy()
	{
		for(Server server : servers)
		{
			server.stop();
			//removeServer(server);
		}
		running=false;
		servers.clear();
		super.onDestroy();
	}
	void startServers()
	{
		Database db=new Database("jerrymouse");
		Result result=db.query("select * from general;");
		for(Map map:result.values)
		{
			if((int)map.get("enabled")!=0)
			{
				int port=(int)map.get("port");
				String webroot=(String)map.get("webroot");
				boolean proxyMode=(int)map.get("proxymode")!=0;
				boolean allowIndex=(int)map.get("allowindex")!=0;
				boolean servletVisible=(int)map.get("servletvisible")!=0;
				addServer(new Server(port,webroot,proxyMode,allowIndex,servletVisible));
			}
		}
	}
	boolean addServer(Server server)
	{
		if(!running||server.isRunning())
			return false;
		server.start();
		return servers.add(server);
	}
	boolean removeServer(Server server)
	{
		if(!running||!server.isRunning())
			return false;
		server.stop();
		return servers.remove(server);
	}
}
