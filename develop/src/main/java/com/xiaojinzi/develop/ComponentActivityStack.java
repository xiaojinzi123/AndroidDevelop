package com.xiaojinzi.develop;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import java.util.Stack;

/**
 * Activity 栈
 *
 * @author xiaojinzi
 */
public class ComponentActivityStack {

    /**
     * the stack will be save all reference of Activity
     */
    private Stack<Activity> activityStack = new Stack<>();

    private ComponentActivityStack() {
    }

    private static class Holder {
        private static ComponentActivityStack INSTANCE = new ComponentActivityStack();
    }

    public static ComponentActivityStack getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * 进入栈
     */
    public synchronized void pushActivity(Activity activity) {
        if (activity == null) {
            return;
        }
        if (activityStack.contains(activity)) {
            return;
        }
        activityStack.add(activity);
    }

    /**
     * remove the reference of Activity
     *
     * @author xiaojinzi
     */
    public synchronized void removeActivity(Activity activity) {
        activityStack.remove(activity);
    }

    /**
     * @return whether the the size of stack of Activity is zero or not
     */
    public synchronized boolean isEmpty() {
        if (activityStack == null || activityStack.size() == 0) {
            return true;
        }
        return false;
    }

    /**
     * @return the size of stack of Activity
     */
    public synchronized int getSize() {
        if (activityStack == null) {
            return 0;
        }
        return activityStack.size();
    }

    /**
     * 返回顶层的 Activity
     */
    @Nullable
    public synchronized Activity getTopActivity() {
        return isEmpty() ? null : activityStack.get(activityStack.size() - 1);
    }

    /**
     * 返回顶层的活着的 Activity
     */
    @Nullable
    public synchronized Activity getTopAliveActivity() {
        Activity result = null;
        if (!isEmpty()) {
            int size = activityStack.size();
            for (int i = size - 1; i >= 0; i--) {
                Activity activity = activityStack.get(i);
                // 如果已经销毁, 就下一个
                if (!isActivityDestoryed(activity)) {
                    result = activity;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 返回顶层的 Activity除了某一个
     */
    @Nullable
    public synchronized Activity getTopActivityExcept(@NonNull Class<? extends Activity> clazz) {
        int size = activityStack.size();
        for (int i = size - 1; i >= 0; i--) {
            Activity itemActivity = activityStack.get(i);
            if (itemActivity.getClass() != clazz) {
                return itemActivity;
            }
        }
        return null;
    }

    /**
     * 返回底层的 Activity
     */
    @Nullable
    public synchronized Activity getBottomActivity() {
        return isEmpty() || activityStack.size() < 1 ? null : activityStack.get(0);
    }

    /**
     * 是否存在某一个 Activity
     */
    public synchronized boolean isExistActivity(@NonNull Class<? extends Activity> clazz) {
        for (Activity activity : activityStack) {
            if (activity.getClass() == clazz) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean isExistOtherActivityExcept(@NonNull Class<? extends Activity> clazz) {
        for (Activity activity : activityStack) {
            if (activity.getClass() != clazz) {
                return true;
            }
        }
        return false;
    }

    /**
     * Activity 是否被销毁了
     */
    public static boolean isActivityDestoryed(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return activity.isFinishing() || activity.isDestroyed();
        } else {
            return activity.isFinishing();
        }
    }

    public synchronized void destroyAll() {
        int size = activityStack.size();
        for (int i = size - 1; i >= 0; i--) {
            Activity activity = activityStack.get(i);
            if (!isActivityDestoryed(activity)) {
                activity.finish();
            }
            activityStack.remove(i);
        }
    }

}
