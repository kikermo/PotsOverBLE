package org.kikermo.blepotcontroller.activity;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;

import org.kikermo.blepotcontroller.BLEPotControllerApplication;
import org.kikermo.blepotcontroller.R;
import org.kikermo.blepotcontroller.utils.Utils;

import java.util.UUID;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.polidea.rxandroidble.RxBleConnection.RxBleConnectionState.CONNECTED;
import static com.polidea.rxandroidble.RxBleConnection.RxBleConnectionState.DISCONNECTED;
import static org.kikermo.blepotcontroller.utils.Constants.BK_DEVICE;
import static org.kikermo.blepotcontroller.utils.Constants.UUID_CHARACTERISTIC;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    private TextView value;

    private BluetoothDevice device;

    private RxBleClient rxBleClient;
    private RxBleConnection connection;
    private CompositeSubscription bleSubscription;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rxBleClient = ((BLEPotControllerApplication) getApplication()).getRxBleClient();

        value = (TextView) findViewById(R.id.value);
        ((SeekBar) findViewById(R.id.pot)).setOnSeekBarChangeListener(this);

        bleSubscription = new CompositeSubscription();

        if (savedInstanceState != null)
            device = savedInstanceState.getParcelable(BK_DEVICE);
        else
            device = getIntent().getParcelableExtra(BK_DEVICE);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BK_DEVICE, device);
    }

    @Override
    protected void onStart() {
        super.onStart();


        bleSubscription.add(establishConnection());
        bleSubscription.add(subscribeToConnectionState());

    }

    @Override
    protected void onStop() {
        bleSubscription.clear();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        rxBleClient = null;
        bleSubscription.unsubscribe();
        bleSubscription = null;
        super.onDestroy();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        value.setText(i + "%");

        byte[] data = new byte[]{Utils.percentageToByte(i)};
        Subscription writeSubs = connection.writeCharacteristic(UUID.fromString(UUID_CHARACTERISTIC), data).subscribe();
        bleSubscription.add(writeSubs);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private Subscription establishConnection() {
        Subscription subscription = rxBleClient.getBleDevice(device.getAddress())
                .establishConnection(this, false)
                .share()
                .subscribe(connection -> this.connection = connection,
                        throwable -> Log.w(TAG, throwable.toString()));

        return subscription;

    }


    private Subscription subscribeToConnectionState() {
        Subscription subscription = rxBleClient
                .getBleDevice(device.getAddress())
                .observeConnectionStateChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(connectionState -> {
                            if (connectionState.equals(CONNECTED))
                                showToast("Connected");
                            else if (connectionState.equals(DISCONNECTED)) {
                                showToast("Disconnected");
                                Intent intent = new Intent(this, SelectServiceActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        },
                        throwable -> Log.w(TAG, throwable.toString())
                );
        return subscription;

    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
