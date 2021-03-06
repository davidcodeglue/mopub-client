/*
 * Copyright (c) 2011, MoPub Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'MoPub Inc.' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mopub.mobileads;

import java.util.HashMap;

import com.mopub.mobileads.MoPubView.LocationAwareness;
import com.mopub.mobileads.MoPubView.OnAdFailedListener;
import com.mopub.mobileads.MoPubView.OnAdLoadedListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

public class MoPubInterstitial {
    
    private MoPubInterstitialView mInterstitialView;
    private MoPubInterstitialListener mListener;
    private Activity mActivity;
    private String mAdUnitId;
    
    public interface MoPubInterstitialListener {
        public void OnInterstitialLoaded();
        public void OnInterstitialFailed();
    }
    
    public class MoPubInterstitialView extends MoPubView {
        
        public MoPubInterstitialView(Context context) {
            super(context);
        }

        @Override
        protected void loadNativeSDK(HashMap<String, String> paramsHash) {
            if (paramsHash == null) return;
            
            MoPubInterstitial interstitial = MoPubInterstitial.this;
            String type = paramsHash.get("X-Adtype");

            if (type != null && type.equals("interstitial")) {
                String interstitialType = paramsHash.get("X-Fulladtype");
                
                Log.i("MoPub", "Loading native adapter for interstitial type: " + interstitialType);
                BaseInterstitialAdapter adapter =
                        BaseInterstitialAdapter.getAdapterForType(interstitialType);
                
                if (adapter != null) {
                    String jsonParams = paramsHash.get("X-Nativeparams");
                    adapter.init(interstitial, jsonParams);
                    adapter.loadInterstitial();
                    return;
                }
            }
            
            Log.i("MoPub", "Couldn't load native adapter. Trying next ad...");
            interstitial.interstitialFailed();
        }
        
        protected void trackImpression() {
            Log.d("MoPub", "Tracking impression for interstitial.");
            if (mAdView != null) mAdView.trackImpression();
        }
    }
    
    public MoPubInterstitial(Activity activity, String id) {
        mActivity = activity;
        mAdUnitId = id;
        
        mInterstitialView = new MoPubInterstitialView(mActivity);
        mInterstitialView.setAdUnitId(mAdUnitId);
        mInterstitialView.setOnAdLoadedListener(new OnAdLoadedListener() {
            public void OnAdLoaded(MoPubView m) {
                if (mListener != null) {
                    mListener.OnInterstitialLoaded();
                }
                
                if (mActivity != null) {
                    String responseString = mInterstitialView.getResponseString();
                    Intent i = new Intent(mActivity, MoPubActivity.class);
                    i.putExtra("com.mopub.mobileads.AdUnitId", mAdUnitId);
                    i.putExtra("com.mopub.mobileads.Keywords", mInterstitialView.getKeywords());
                    i.putExtra("com.mopub.mobileads.Source", responseString);
                    i.putExtra("com.mopub.mobileads.ClickthroughUrl",
                            mInterstitialView.getClickthroughUrl());
                    mActivity.startActivity(i);
                }
            }
        });
        mInterstitialView.setOnAdFailedListener(new OnAdFailedListener() {
            public void OnAdFailed(MoPubView m) {
                if (mListener != null) {
                    mListener.OnInterstitialFailed();
                }
            }
        });
    }
    
    public Activity getActivity() {
        return mActivity;
    }
    
    public void showAd() {
        mInterstitialView.loadAd();
    }
/* TODO:
    public void prefetchAd() {
    
    }
    
    public void showPrefetchedAd() {
        
    }
*/
    public void setListener(MoPubInterstitialListener listener) {
        mListener = listener;
    }
    
    public MoPubInterstitialListener getListener() {
        return mListener;
    }
    
    public Location getLocation() {
        return mInterstitialView.getLocation();
    }
    
    protected void interstitialLoaded() {
        mInterstitialView.trackImpression();
        if (mListener != null) mListener.OnInterstitialLoaded();
    }
    
    protected void interstitialFailed() {
        mInterstitialView.loadFailUrl();
    }
    
    protected void interstitialClicked() {
        mInterstitialView.registerClick();
    }
    
    public void destroy() {
        mInterstitialView.destroy();
    }
    
    public void setLocationAwareness(LocationAwareness awareness) {
        mInterstitialView.setLocationAwareness(awareness);
    }

    public LocationAwareness getLocationAwareness() {
        return mInterstitialView.getLocationAwareness();
    }

    public void setLocationPrecision(int precision) {
        mInterstitialView.setLocationPrecision(precision);
    }

    public int getLocationPrecision() {
        return mInterstitialView.getLocationPrecision();
    }
}
