/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.deviceinfo.aboutphone;

import android.app.Activity;
import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.UserManager;
import android.os.SystemProperties;
import android.view.View;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.deviceinfo.BluetoothAddressPreferenceController;
import com.android.settings.deviceinfo.BuildNumberPreferenceController;
import com.android.settings.deviceinfo.DeviceNamePreferenceController;
import com.android.settings.deviceinfo.FccEquipmentIdPreferenceController;
import com.android.settings.deviceinfo.FeedbackPreferenceController;
import com.android.settings.deviceinfo.IpAddressPreferenceController;
import com.android.settings.deviceinfo.ManualPreferenceController;
import com.android.settings.deviceinfo.RegulatoryInfoPreferenceController;
import com.android.settings.deviceinfo.SafetyInfoPreferenceController;
import com.android.settings.deviceinfo.UptimePreferenceController;
import com.android.settings.deviceinfo.WifiMacAddressPreferenceController;
import com.android.settings.deviceinfo.imei.ImeiInfoPreferenceController;
import com.android.settings.deviceinfo.simstatus.SimStatusPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;
import com.android.settingslib.widget.LayoutPreference;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@SearchIndexable
public class MyDeviceInfoFragment extends DashboardFragment
        implements DeviceNamePreferenceController.DeviceNamePreferenceHost {

    private static final String LOG_TAG = "MyDeviceInfoFragment";
    private static final String KEY_MY_DEVICE_INFO_HEADER = "my_device_info_header";

    private BuildNumberPreferenceController mBuildNumberPreferenceController;

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DEVICEINFO;
    }

    @Override
    public int getHelpResource() {
        return R.string.help_uri_about;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        use(ImeiInfoPreferenceController.class).setHost(this /* parent */);
        use(DeviceNamePreferenceController.class).setHost(this /* parent */);
        mBuildNumberPreferenceController = use(BuildNumberPreferenceController.class);
        mBuildNumberPreferenceController.setHost(this /* parent */);
    }

    @Override
    public void onStart() {
        super.onStart();
        initPhoneSpecs();
    }

    @Override
    protected String getLogTag() {
        return LOG_TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.my_device_info;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, this /* fragment */, getSettingsLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, MyDeviceInfoFragment fragment, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new SimStatusPreferenceController(context, fragment));
        controllers.add(new IpAddressPreferenceController(context, lifecycle));
        controllers.add(new WifiMacAddressPreferenceController(context, lifecycle));
        controllers.add(new BluetoothAddressPreferenceController(context, lifecycle));
        controllers.add(new RegulatoryInfoPreferenceController(context));
        controllers.add(new SafetyInfoPreferenceController(context));
        controllers.add(new ManualPreferenceController(context));
        controllers.add(new FeedbackPreferenceController(fragment, context));
        controllers.add(new FccEquipmentIdPreferenceController(context));
        controllers.add(new UptimePreferenceController(context, lifecycle));
        return controllers;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mBuildNumberPreferenceController.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void showDeviceNameWarningDialog(String deviceName) {
        DeviceNameWarningDialog.show(this);
    }

    public void onSetDeviceNameConfirm(boolean confirm) {
        final DeviceNamePreferenceController controller = use(DeviceNamePreferenceController.class);
        controller.updateDeviceName(confirm);
    }
    
    private void initPhoneSpecs(){
        
        final LayoutPreference specsPref = getPreferenceScreen().findPreference("phone_specs");
        final Activity context = getActivity();
        final String deviceCodename = SystemProperties.get("ro.product.board");
        
        View root = specsPref.findViewById(R.id.container);
        TextView deviceName = specsPref.findViewById(R.id.device_desc);
        TextView chipsetName = specsPref.findViewById(R.id.device_chipset_desc);
        TextView ramName = specsPref.findViewById(R.id.device_ram_desc);
        TextView gpuName = specsPref.findViewById(R.id.device_gpu_desc);
        TextView cameraName = specsPref.findViewById(R.id.device_camera_desc);
        TextView screenName = specsPref.findViewById(R.id.device_screen_desc);
    
        deviceName.setText("iM4 Phone");
        ramName.setText(String.valueOf(Math.round(Float.parseFloat(getTotalMemory().toLowerCase().replace("kb","").replace("memtotal:",""))/ 1000000)));
    
        if (deviceCodename.equals("selene")){
            chipsetName.setText("MediaTek Helio G88");
            gpuName.setText("Mali G52 MC2");
            cameraName.setText("50MP+8MP+2MP+2MP Quad Camera");
            screenName.setText("LCD 6.5 90hz Corning Gorilla Glass 3");
        } else if (deviceCodename.equals("cupid") || deviceCodename.equals("zeus") || deviceCodename.equals("psyche")){
            chipsetName.setText("Qualcomm SM8450 Snapdragon 8 Gen 1");
            gpuName.setText("Adreno 730");
            cameraName.setText("50MP + 13MP + 5MP Triple Camera, PDAF, OIS");
            screenName.setText("AMOLED 6.28, 68B colors, 120Hz, Dolby Vision, HDR10+");
        } else if (deviceCodename.equals("nabu")){
            chipsetName.setText("Qualcomm Snapdragon 860");
            gpuName.setText("Adreno 640");
            cameraName.setText("13 MP, f/2.0 Single Camera");
            screenName.setText("IPS LCD 11.0, 1B colors, 120Hz, HDR10, Dolby Vision");
        } else if (deviceCodename.equals("fleur") || deviceCodename.equals("evergreen")){
            chipsetName.setText("Mediatek Helio G96");
            gpuName.setText("Mali-G57 MC2");
            cameraName.setText("64MP + 8MP + 2MP Triple Camera");
            screenName.setText("AMOLED 6.43, 90Hz");
        } else {
            chipsetName.setText("Unknown");
            gpuName.setText("Unknown");
            cameraName.setText("Unknown");
            screenName.setText("Unknown");
        }
    }
    
    public static String getTotalMemory() {
        try {
            Process proc = Runtime.getRuntime().exec("cat /proc/meminfo");
            InputStream is = proc.getInputStream();
            String[] listMemory = getStringFromInputStream(is).split("\n");
            for(int i = 0 ; i < listMemory.length ; i++) {
                if(listMemory[i].contains("MemTotal"))
                    return listMemory[i];
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return "unknown";
    }

    public static String getStringFromInputStream(InputStream is) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;

        try {
            while((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    /**
     * For Search.
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.my_device_info) {

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(
                        Context context) {
                    return buildPreferenceControllers(context, null /* fragment */,
                            null /* lifecycle */);
                }
            };
}
