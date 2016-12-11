package org.kikermo.blepotcontroller;

import android.app.Application;

import com.polidea.rxandroidble.RxBleClient;

/**
 * Created by EnriqueR on 10/12/2016.
 */

public class BLEPotControllerApplication extends Application {
    private RxBleClient rxBleClient;

    @Override
    public void onTerminate() {
        rxBleClient = null;
        super.onTerminate();
    }


    @Override
    public void onLowMemory() {
        rxBleClient = null;
        super.onLowMemory();
    }

    public RxBleClient getRxBleClient() {
        if (rxBleClient == null)
            rxBleClient = RxBleClient.create(getApplicationContext());
        return rxBleClient;
    }
}
