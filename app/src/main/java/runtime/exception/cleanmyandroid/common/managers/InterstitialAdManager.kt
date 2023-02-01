package runtime.exception.cleanmyandroid.common.managers

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import runtime.exception.cleanmyandroid.R

class InterstitialAdManager(
    private val context: Context
) {

    private var interstitialAd: InterstitialAd? = null

    init {
        load()
    }

    private fun load() {
        InterstitialAd.load(
            context,
            context.getString(R.string.interstitial_admob_test_ads),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("InterstitialAdManager", error.toString())
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }
            }
        )
    }

    fun show(activity: Activity, onFinished: () -> Unit = {}) {
        interstitialAd?.let {
            it.setOnPaidEventListener { onFinished() }
            it.show(activity)
        }
        load()
    }
}