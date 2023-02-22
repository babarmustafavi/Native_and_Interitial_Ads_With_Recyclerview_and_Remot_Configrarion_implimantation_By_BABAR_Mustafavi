package com.example.taskfunprime;

import android.Manifest;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.taskfunprime.databinding.LayoutAdBinding;
import com.example.taskfunprime.databinding.LayoutMainItemBinding;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;


public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_VIEW = 0;
    private static final int AD_VIEW = 1;
    private static final int ITEM_FEED_COUNT = 4;
    public static AdRequest adRequest;
    public static InterstitialAd mInterstitialAd;
    static int PERMISSION_ALL = 3;
    private final Activity activity;
    private final List<Main> mainList;
    public String[] PERMISSIONS = new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    File file = null;
    boolean adShow = false;
    private setdat mItemClickListener;

    public MainAdapter(Activity activity, List<Main> mainList) {
        this.activity = activity;
        this.mainList = mainList;
        adRequest = new AdRequest.Builder().build();
    }

  //  interfacelistner
    public void setOnItemClickListener(setdat mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        if (viewType == ITEM_VIEW) {
            View view = layoutInflater.inflate(R.layout.layout_main_item, parent, false);
            return new MainViewHolder(view);
        } else if (viewType == AD_VIEW) {
            View view = layoutInflater.inflate(R.layout.layout_ad, parent, false);
            return new AdViewHolder(view);
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == ITEM_VIEW) {
            int pos = position - Math.round(position / ITEM_FEED_COUNT);
            ((MainViewHolder) holder).bindData(mainList.get(pos), pos);
        } else if (holder.getItemViewType() == AD_VIEW) {
            ((AdViewHolder) holder).bindAdData();
        }
    }

    @Override
    public int getItemCount() {
        if (mainList.size() > 0) {
            return mainList.size() + Math.round(mainList.size() / ITEM_FEED_COUNT);
        }
        return mainList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if ((position + 1) % ITEM_FEED_COUNT == 0) {
            return AD_VIEW;
        }
        return ITEM_VIEW;
    }

    //intersitial ad loader
    public void InterstitialAdmob() {

        InterstitialAd.load(activity,
                activity.getString(R.string.admob_interstitial_id),
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent();
                                mInterstitialAd = null;

                                InterstitialAdmob();


                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                                super.onAdFailedToShowFullScreenContent(adError);
                                mInterstitialAd = null;
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent();
                                mInterstitialAd = null;
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                        InterstitialAdmob();

                    }
                });
    }

    private void populateNativeADView(NativeAd nativeAd, NativeAdView adView) {
        // Set the media view.
        adView.setMediaView(adView.findViewById(R.id.ad_media));

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView()).setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd);
    }

    public interface setdat {
        void onItemClickListener(View view, int position, String name);
    }

    public class MainViewHolder extends RecyclerView.ViewHolder {

        LayoutMainItemBinding binding;

        public MainViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = LayoutMainItemBinding.bind(itemView);
        }

        public File folderCreatedInGallery(String FolderName) {
            File dir = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + FolderName);
            } else {
                dir = new File(Environment.getExternalStorageDirectory() + "/" + FolderName);
            }

            // Make sure the path directory exists.
            if (!dir.exists()) {
                // Make it, if it doesn't exit
                boolean success = dir.mkdirs();
                if (!success) {
                    dir = null;
                }
            }
            return dir;
        }

        public boolean per() {
            if (!hasPermissions(activity, PERMISSIONS)) {
                ActivityCompat.requestPermissions(activity, PERMISSIONS, PERMISSION_ALL);
                //per();
                // Toast.makeText(activity, "permition not", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                // Toast.makeText(activity, "permition yes", Toast.LENGTH_SHORT).show();
                return true;
            }

        }

        public boolean hasPermissions(Activity activityy, String... permissions) {
            if (!(Build.VERSION.SDK_INT < 23 || activityy == null || permissions == null)) {
                for (String permission : permissions) {
                    if (ContextCompat.checkSelfPermission(activityy, permission) != 0) {
                        return false;
                    }
                }
            }
            return true;
        }

        private void saveImage(Bitmap image, File storageDir, String imageFileName) {

            File imageFile = new File(storageDir, imageFileName);
            String savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.close();
                Toast.makeText(activity, "Image Saved On" + savedImagePath, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(activity, "Error while saving image!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }


        private void bindData(Main main, int position) {
            InterstitialAdmob();
            Glide.with(binding.imageView.getContext()).load(main.getUrl()).into(binding.imageView);
            binding.lin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mInterstitialAd != null) {
                        if (!adShow) {
                            adShow = true;
                            mInterstitialAd.show(activity);
                            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent();
                                    InterstitialAdmob();
                                    // Toast.makeText(activity, "closed ad", Toast.LENGTH_SHORT).show();
                                    if (per()) {
                                        file = folderCreatedInGallery("FunPrime");
                                        if (file != null) {
                                            //Toast.makeText(activity, "url="+main.getUrl(), Toast.LENGTH_SHORT).show();
                                            Glide.with(activity)
                                                    .load(main.getUrl())
                                                    .into(new CustomTarget<Drawable>() {
                                                        @Override
                                                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {

                                                            if (file.exists()) {

                                                                final File dir = new File(file.toString());
                                                                BitmapDrawable drawable = (BitmapDrawable) binding.imageView.getDrawable();
                                                                Bitmap bitmap1 = drawable.getBitmap();
                                                                saveImage(bitmap1, dir, "ImageNo" + position + ".jpeg");
                                                                //Toast.makeText(activity, "bitmap1="+bitmap1+"and="+dir+"and"+"ImageNo" + position + ".jpeg", Toast.LENGTH_SHORT).show();
                                                                mItemClickListener.onItemClickListener(v, getAdapterPosition(), "ImageNo" + position);
                                                            } else {
                                                                Toast.makeText(activity, "Folder Not Created", Toast.LENGTH_SHORT).show();
                                                            }
//
                                                        }

                                                        @Override
                                                        public void onLoadCleared(@Nullable Drawable placeholder) {

                                                        }

                                                        @Override
                                                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                                            super.onLoadFailed(errorDrawable);

                                                            Toast.makeText(activity, "Failed to Download Image! Please try again later.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });

                                        } else {
                                            file = folderCreatedInGallery("FunPrime");
                                        }
                                    } else {
                                        //Toast.makeText(activity, "false aya...", Toast.LENGTH_SHORT).show();
                                        per();
                                    }
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                    super.onAdFailedToShowFullScreenContent(adError);
                                }
                            });
                        } else {
                            adShow = false;
                            file = folderCreatedInGallery("FunPrime");
                            if (file != null) {
                                //Toast.makeText(activity, "url="+main.getUrl(), Toast.LENGTH_SHORT).show();
                                Glide.with(activity)
                                        .load(main.getUrl())
                                        .into(new CustomTarget<Drawable>() {
                                            @Override
                                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {

                                                if (file.exists()) {

                                                    final File dir = new File(file.toString());
                                                    BitmapDrawable drawable = (BitmapDrawable) binding.imageView.getDrawable();
                                                    Bitmap bitmap1 = drawable.getBitmap();
                                                    saveImage(bitmap1, dir, "ImageNo" + position + ".jpeg");
                                                    //Toast.makeText(activity, "bitmap1="+bitmap1+"and="+dir+"and"+"ImageNo" + position + ".jpeg", Toast.LENGTH_SHORT).show();
                                                    mItemClickListener.onItemClickListener(v, getAdapterPosition(), "ImageNo" + position);
                                                } else {
                                                    Toast.makeText(activity, "Folder Not Created", Toast.LENGTH_SHORT).show();
                                                }
//
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {

                                            }

                                            @Override
                                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                                super.onLoadFailed(errorDrawable);

                                                Toast.makeText(activity, "Failed to Download Image! Please try again later.", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            } else {
                                file = folderCreatedInGallery("FunPrime");
                            }
                        }

                    }

                }
            });
        }
    }

    public class AdViewHolder extends RecyclerView.ViewHolder {

        LayoutAdBinding binding;

        public AdViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = LayoutAdBinding.bind(itemView);
        }

        private void bindAdData() {
            AdLoader.Builder builder = new AdLoader.Builder(activity, activity.getResources().getString(R.string.nativead))
                    .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                        @Override
                        public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                            NativeAdView nativeAdView = (NativeAdView) activity.getLayoutInflater().inflate(R.layout.layout_native_ad, null);
                            populateNativeADView(nativeAd, nativeAdView);
                            binding.adLayout.removeAllViews();
                            binding.adLayout.addView(nativeAdView);
                        }
                    });

            AdLoader adLoader = builder.withAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    Toast.makeText(activity, loadAdError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).build();

            adLoader.loadAd(new AdRequest.Builder().build());
        }
    }
}
