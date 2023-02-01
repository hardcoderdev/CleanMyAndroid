package runtime.exception.cleanmyandroid.ui

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.*
import com.google.android.gms.ads.MobileAds
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import runtime.exception.cleanmyandroid.common.features.ads.AppOpenAdManager
import runtime.exception.cleanmyandroid.di.*

class BaseApplication : Application(), Application.ActivityLifecycleCallbacks, LifecycleObserver {

    private lateinit var appOpenManager: AppOpenAdManager
    private var currentActivity: Activity? = null

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        if (!appOpenManager.isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

    fun showAdIfAvailable(
        activity: Activity,
        onShowAdCompleteListener: OnShowAdCompleteListener
    ) {
        appOpenManager.showAdIfAvailable(activity, onShowAdCompleteListener)
    }

    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }

    override fun onCreate() {
        super.onCreate()
        setUpKoin()
        setUpAds()
    }

    private fun setUpAds() {
        MobileAds.initialize(this)
        appOpenManager = AppOpenAdManager()
    }

    private fun setUpKoin() {
        startKoin {
            androidLogger(level = Level.ERROR)
            androidContext(this@BaseApplication)
            modules(
                viewModelModule,
                featuresModule,
                providersModule,
                formattersModule,
                managersModule,
                sendersModule,
                installersModule
            )
        }
    }
}