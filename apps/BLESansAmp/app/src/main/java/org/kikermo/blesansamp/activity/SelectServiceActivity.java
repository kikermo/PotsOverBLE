package org.kikermo.blesansamp.activity;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.RxBleScanResult;

import org.kikermo.blesansamp.BLESansAmpApplication;
import org.kikermo.blesansamp.R;
import org.kikermo.blesansamp.adapter.BluetoothDeviceAdapter;


import java.util.UUID;
import java.util.concurrent.TimeUnit;


import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

import static org.kikermo.blesansamp.utils.Constants.*;
import static org.kikermo.blesansamp.utils.Utils.notNull;

public class SelectServiceActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final int REQUEST_ACCESS_COARSE = 43598;
    private static final String TAG = "SelectServiceActivity";

    private ListView listView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Subscription bleDiscoverySubscription;

    private RxBleClient rxBleClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_service);


        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new BluetoothDeviceAdapter(this, android.R.layout.simple_list_item_1));
        listView.setOnItemClickListener(this);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipetorefresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (notNull(bleDiscoverySubscription))
                bleDiscoverySubscription.unsubscribe();
            bleDiscoverySubscription = subscribeToBLEDiscovery();
        });
        checkPermissions();

        rxBleClient = ((BLESansAmpApplication) getApplication()).getRxBleClient();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bleDiscoverySubscription = subscribeToBLEDiscovery();

    }


    @Override
    protected void onPause() {
        if (bleDiscoverySubscription != null)
            bleDiscoverySubscription.unsubscribe();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        rxBleClient = null;
        super.onDestroy();
    }


    private Subscription subscribeToBLEDiscovery() {
        return rxBleClient.scanBleDevices(UUID.fromString(UUID_SERVICE))
                .timeout(4, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(RxBleScanResult::getBleDevice).map(RxBleDevice::getBluetoothDevice)
                .subscribe(bleDevice -> {
                            ((ArrayAdapter) listView.getAdapter()).add(bleDevice);
                            if (swipeRefreshLayout.isRefreshing())
                                swipeRefreshLayout.setRefreshing(false);
                        },
                        throwable -> {
                            if (swipeRefreshLayout.isRefreshing())
                                swipeRefreshLayout.setRefreshing(false);
                            Snackbar snackbar = Snackbar.make(listView, "No device found", Snackbar.LENGTH_SHORT);
                            snackbar.show();
                        }
                );
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        BluetoothDevice bleDevice = (BluetoothDevice) adapterView.getItemAtPosition(i);

        if (bleDevice.getBondState() != BluetoothDevice.BOND_BONDED)  //This is important in order to be able to subscribe to notifications
            bleDevice.createBond();

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(BK_DEVICE, bleDevice);
        startActivity(intent);
        finish();
    }


    private boolean checkPermissions() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_COARSE);
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_COARSE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    bleDiscoverySubscription = subscribeToBLEDiscovery();
                } else {
                    Snackbar snackbar = Snackbar.make(listView, "You have to grant location permissions in order to search for devices", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
