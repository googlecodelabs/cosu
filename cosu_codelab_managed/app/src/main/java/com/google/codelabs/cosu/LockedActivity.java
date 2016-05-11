// Copyright 2016 Google Inc.
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//      http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.codelabs.cosu;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class LockedActivity extends Activity {

    private ImageView imageView;
    private Button stopLockButton;
    private String mCurrentPhotoPath;
    private DevicePolicyManager mDevicePolicyManager;

    private static final String PREFS_FILE_NAME = "MyPrefsFile";
    private static final String PHOTO_PATH = "Photo Path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locked);

        mDevicePolicyManager = (DevicePolicyManager)
                getSystemService(Context.DEVICE_POLICY_SERVICE);

        // Setup stop lock task button
        stopLockButton = (Button) findViewById(R.id.stop_lock_button);
        stopLockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityManager am = (ActivityManager) getSystemService(
                        Context.ACTIVITY_SERVICE);
                if (am.getLockTaskModeState() ==
                        ActivityManager.LOCK_TASK_MODE_LOCKED) {
                    stopLockTask();
                }
                Intent intent = new Intent(
                        getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Set image to View
        setImageToView();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start lock task mode if its not already active
        if(mDevicePolicyManager.isLockTaskPermitted(this.getPackageName())){
            ActivityManager am = (ActivityManager) getSystemService(
                    Context.ACTIVITY_SERVICE);
            if (am.getLockTaskModeState() ==
                    ActivityManager.LOCK_TASK_MODE_NONE) {
                startLockTask();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_locked, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop(){
        super.onStop();

        // Get editor object and make preference changes to save photo filepath
        SharedPreferences settings = getSharedPreferences(PREFS_FILE_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PHOTO_PATH, mCurrentPhotoPath);
        editor.commit();
    }

    private void setImageToView(){

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_FILE_NAME, 0);
        String savedPhotoPath = settings.getString(PHOTO_PATH, null);

        //Initialize the image view and display the picture if one exists
        imageView = (ImageView) findViewById(R.id.lock_imageView);
        Intent intent = getIntent();

        String passedPhotoPath = intent.getStringExtra(MainActivity.EXTRA_FILEPATH);
        if (passedPhotoPath != null) {
            mCurrentPhotoPath = passedPhotoPath;
        } else {
            mCurrentPhotoPath = savedPhotoPath;
        }

        if (mCurrentPhotoPath != null) {
            int targetH = imageView.getMaxHeight();
            int targetW = imageView.getMaxWidth();

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            int photoH = bmOptions.outHeight;
            int photoW = bmOptions.outWidth;

            // Determine how much to scale down image
            int scaleFactor = Math.min(photoW/targetW, photoH/targetH);


            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            Bitmap imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            imageView.setImageBitmap(imageBitmap);
        }
    }
}
