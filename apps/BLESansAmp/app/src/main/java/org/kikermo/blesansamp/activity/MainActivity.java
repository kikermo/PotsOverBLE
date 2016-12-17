package org.kikermo.blesansamp.activity;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;


import org.kikermo.blesansamp.BLESansAmpApplication;
import org.kikermo.blesansamp.R;
import org.kikermo.blesansamp.model.Command;
import org.kikermo.blesansamp.utils.Utils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

import static com.polidea.rxandroidble.RxBleConnection.RxBleConnectionState.CONNECTED;
import static com.polidea.rxandroidble.RxBleConnection.RxBleConnectionState.DISCONNECTED;
import static org.kikermo.blesansamp.utils.Constants.BK_DEVICE;
import static org.kikermo.blesansamp.utils.Constants.UUID_CHARACTERISTIC;
import static org.kikermo.blesansamp.utils.Utils.byteToPercentage;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    private SeekBar level, high, low, volume;

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


        level = (SeekBar) findViewById(R.id.level);
        high = (SeekBar) findViewById(R.id.high);
        low = (SeekBar) findViewById(R.id.low);
        volume = (SeekBar) findViewById(R.id.volume);


        level.setOnSeekBarChangeListener(this);
        high.setOnSeekBarChangeListener(this);
        low.setOnSeekBarChangeListener(this);
        volume.setOnSeekBarChangeListener(this);

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
        // value.setText(i + "%");

        if (fromUser && connected) {
            Command command = new Command();
            command.setCmd(0x01);
            command.setValue(Utils.percentageToByte(i));

            switch (seekBar.getId()) {
                case R.id.level:
                    command.setAct(0);
                    break;
                case R.id.high:
                    command.setAct(1);
                    break;
                case R.id.low:
                    command.setAct(2);
                    break;
                case R.id.volume:
                    command.setAct(3);
                    break;
            }


            //byte[] data = new byte[]{Utils.percentageToByte(i)};
            Subscription writeSubs = connection.writeCharacteristic(UUID.fromString(UUID_CHARACTERISTIC), command.toByteArray()).subscribe(o -> {
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
                .flatMap(connection -> {
                    final Command cmd1 = new Command();
                    cmd1.setCmd(0x00);
                    cmd1.setAct(0x00);
                    final Command cmd2 = new Command();
                    cmd2.setCmd(0x00);
                    cmd2.setAct(0x01);
                    final Command cmd3 = new Command();
                    cmd3.setCmd(0x00);
                    cmd3.setAct(0x02);
                    final Command cmd4 = new Command();
                    cmd4.setCmd(0x00);
                    cmd4.setAct(0x03);

                    return Observable.merge(connection.setupNotification(uuid).flatMap(not -> not),
                            connection.writeCharacteristic(uuid, cmd1.toByteArray()).delaySubscription(200, TimeUnit.MILLISECONDS),
                            connection.writeCharacteristic(uuid, cmd2.toByteArray()).delaySubscription(400, TimeUnit.MILLISECONDS),
                            connection.writeCharacteristic(uuid, cmd3.toByteArray()).delaySubscription(600, TimeUnit.MILLISECONDS),
                            connection.writeCharacteristic(uuid, cmd4.toByteArray()).delaySubscription(800, TimeUnit.MILLISECONDS));
                })
                .filter(Utils::notNull)
                .filter(bytes -> bytes.length > 2)
                .map(Command::new)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::renderCommand,
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

    private void renderCommand(Command command) {
        switch (command.getAct()) {
            case 0:
                level.setProgress(byteToPercentage((byte) command.getValue()));
                break;
            case 1:
                high.setProgress(byteToPercentage((byte) command.getValue()));
                break;
            case 2:
                low.setProgress(byteToPercentage((byte) command.getValue()));
                break;
            case 3:
                volume.setProgress(byteToPercentage((byte) command.getValue()));
                break;
        }
    }


}
