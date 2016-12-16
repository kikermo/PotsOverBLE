package org.kikermo.blesansamp.activity;

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

import org.kikermo.blesansamp.BLESansAmpApplication;
import org.kikermo.blesansamp.R;
import org.kikermo.blesansamp.utils.Utils;

import java.util.UUID;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

import static com.polidea.rxandroidble.RxBleConnection.RxBleConnectionState.CONNECTED;
import static com.polidea.rxandroidble.RxBleConnection.RxBleConnectionState.DISCONNECTED;
import static org.kikermo.blesansamp.utils.Constants.BK_DEVICE;
import static org.kikermo.blesansamp.utils.Constants.UUID_CHARACTERISTIC;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    private TextView value;
    private SeekBar pot;

    private BluetoothDevice device;

    private RxBleClient rxBleClient;
    private RxBleConnection connection;
    private CompositeSubscription bleSubscription;

    private static final String TAG = "MainActivity";

    private boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rxBleClient = ((BLESansAmpApplication) getApplication()).getRxBleClient();

        value = (TextView) findViewById(R.id.value);
        pot = ((SeekBar) findViewById(R.id.pot));

        pot.setOnSeekBarChangeListener(this);

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

        bleSubscription.add(establishConnectionAndSubscribeToNotifications());
        bleSubscription.add(subscribeToConnectionState());
    }

    @Override
    protected void onStop() {
        bleSubscription.clear();
        connected = false;
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
    public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
        value.setText(i + "%");

        if (fromUser && connected) {
            String msg = i + "very long messag\n";
            byte[] data = msg.getBytes();
            //byte[] data = new byte[]{Utils.percentageToByte(i)};
            Subscription writeSubs = connection.writeCharacteristic(UUID.fromString(UUID_CHARACTERISTIC), data).subscribe(o -> {
            }, throwable -> Log.w(TAG, throwable));
            bleSubscription.add(writeSubs);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private Subscription establishConnectionAndSubscribeToNotifications() {
        Observable<RxBleConnection> bleObservable = rxBleClient.getBleDevice(device.getAddress())
                .establishConnection(this, false)
                .share();

        final UUID uuid = UUID.fromString(UUID_CHARACTERISTIC);

        Subscription subscription = bleObservable
                .doOnNext(connection -> this.connection = connection)
                .flatMap(connection ->
                        Observable.merge(connection.readCharacteristic(uuid),
                                connection.setupNotification(uuid).flatMap(notificationObservable -> notificationObservable)))
                .filter(Utils::notNull)
                .filter(bytes -> bytes.length > 0)
                .map(bytes -> bytes[0])
                .map(Utils::byteToPercentage)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(progress -> pot.setProgress(progress),
                        throwable -> Log.w(TAG, throwable));
        return subscription;

    }


    private Subscription subscribeToConnectionState() {
        Subscription subscription = rxBleClient
                .getBleDevice(device.getAddress())
                .observeConnectionStateChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(connectionState -> {
                            if (connectionState.equals(CONNECTED)) {
                                showToast("Connected");
                                connected = true;
                            } else if (connectionState.equals(DISCONNECTED)) {
                                showToast("Disconnected");
                                connected = false;
                                Intent intent = new Intent(this, SelectServiceActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        },
                        throwable -> Log.w(TAG, throwable)
                );
        return subscription;

    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
