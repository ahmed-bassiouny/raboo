package bluetoothlesmartcharger.ramk.com.blesmartcharger1.daemon;

import android.app.Activity;
import android.content.Context;

import java.util.Stack;

public class AppManager {
	private static Stack<Activity> activityStack;
	private static AppManager instance;
	public static Activity context;

	private AppManager()
	{
	}

	public static AppManager getAppManager()
	{
		if(instance == null)
		{
			instance = new AppManager();
		}
		return instance;
	}

	public void addActivity(Activity activity)
	{
		if(activityStack == null)
		{
			activityStack = new Stack<Activity>();
		}
		activityStack.add(activity);
	}

	public Activity currentActivity()
	{
		if(activityStack.size() <= 0)
		{
			return null;
		}
		Activity activity = activityStack.lastElement();
		return activity;
	}

	public void finishActivity()
	{
		if (null != activityStack ){
			if (!activityStack.isEmpty()){
				Activity activity = activityStack.lastElement();
				if (null != activity){
					finishActivity(activity);
				}
			}
		}
	}

	public void finishActivity(Activity activity)
	{
		if(activity != null)
		{
			activityStack.remove(activity);
			activity.finish();
			activity = null;
		}
	}

	public void finishActivity(Class<?> cls)
	{
		for(Activity activity : activityStack)
		{
			if(activity.getClass().equals(cls))
			{
				finishActivity(activity);
			}
		}
	}

	public void finishASomectivity(Class<?> cls)
	{
		for(Activity activity : activityStack)
		{
			if(!activity.getClass().equals(cls))
			{
				finishActivity(activity);
			}
		}
	}

	public void finishAllActivity()
	{
		for(int i = 0, size = activityStack.size(); i < size; i++)
		{
			if(null != activityStack.get(i))
			{
				activityStack.get(i).finish();
			}
		}
		activityStack.clear();
	}

	public void AppExit(Context context)
	{
		try
		{
			finishAllActivity();
			System.exit(0);
		}
		catch(Exception e)
		{
		}
	}

	public int CountActivity()
	{
		return activityStack.size();
	}

}
