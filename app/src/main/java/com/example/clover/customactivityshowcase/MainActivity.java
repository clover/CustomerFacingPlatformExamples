package com.example.clover.customactivityshowcase;

import com.clover.sdk.util.Platform;
import com.clover.sdk.v3.scanner.BarcodeResult;
import com.clover.sdk.v3.scanner.BarcodeScanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends Activity {

  public static final String TAG = MainActivity.class.getSimpleName();
  public static final String EXAMPLE_APP_NAME = "EXAMPLE_APP";
  public static final String LAN_PAY_DISPLAY_URL = "LAN_PAY_DISPLAY_URL";
  public static final String CONNECTION_MODE = "CONNECTION_MODE";
  public static final String USB = "USB";
  public static final String LAN = "LAN";
  public static final String WS_CONFIG = "WS";

  // Clover devices do not always support the custom Barcode scanner implemented here.
  // They DO have a different capability to scan barcodes.
  // We do a switch based on the platform to allow the example app to run on station.
  private BroadcastReceiver cloverBarcodeReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      BarcodeResult barcodeResult = new BarcodeResult(intent);

      if (barcodeResult.isBarcodeAction()) {
        String barcode = barcodeResult.getBarcode();
        Log.d(TAG, "Barcode from clover handler is " + barcode);
        if (barcode != null) {
          connect(parseValidateAndStoreURI(barcode), WS_CONFIG, false);
        }
      }
    }
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    loadBaseURL();

    if (null != getActionBar()) {
      getActionBar().hide();
    }

    RadioGroup group = (RadioGroup)findViewById(R.id.radioGroup);
    group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
        TextView textView = (TextView) findViewById(R.id.lanPayDisplayAddress);
        textView.setEnabled(checkedId == R.id.lanRadioButton);
      }
    });

    Button connectButton = (Button)findViewById(R.id.connectButton);
    connectButton.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        cleanConnect(v);
        return true;
      }
    });

    // initialize...
    TextView textView = (TextView) findViewById(R.id.lanPayDisplayAddress);
    String url = this.getSharedPreferences(EXAMPLE_APP_NAME, Context.MODE_PRIVATE).getString(LAN_PAY_DISPLAY_URL,  getText(R.string.lan_pay_address).toString());

    textView.setText(url);
    textView.setEnabled(((RadioGroup)findViewById(R.id.radioGroup)).getCheckedRadioButtonId() == R.id.lanRadioButton);

    String mode = this.getSharedPreferences(EXAMPLE_APP_NAME, Context.MODE_PRIVATE).getString(CONNECTION_MODE, USB);

    ((RadioButton)findViewById(R.id.lanRadioButton)).setChecked(LAN.equals(mode));
    ((RadioButton)findViewById(R.id.usbRadioButton)).setChecked(!LAN.equals(mode));

    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
  }

  private boolean loadBaseURL() {

    String _serverBaseURL = PreferenceManager.getDefaultSharedPreferences(this).getString(CustomShowcase.EXAMPLE_POS_SERVER_KEY, "wss://10.0.0.101:12345/remote_pay");

    TextView tv = (TextView)findViewById(R.id.lanPayDisplayAddress);
    tv.setText(_serverBaseURL);

    Log.d(TAG, _serverBaseURL);
    return true;
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  public void cleanConnect(View view) {
    connect(view, true);
  }

  public void connect(View view) {
    connect(view, false);
  }

  public void connect(View view, boolean clearToken) {

    RadioGroup group = (RadioGroup)findViewById(R.id.radioGroup);
    SharedPreferences prefs = this.getSharedPreferences(EXAMPLE_APP_NAME, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    URI uri = null;
    String config;

    if(group.getCheckedRadioButtonId() == R.id.usbRadioButton) {
      config = USB;
      editor.putString(CONNECTION_MODE, USB);
      editor.apply();
    } else {
      String uriStr = ((TextView)findViewById(R.id.lanPayDisplayAddress)).getText().toString();
      config = WS_CONFIG;
      uri = parseValidateAndStoreURI(uriStr);
    }
    connect(uri, config, clearToken);
  }

  private void connect(URI uri, String config, boolean clearToken) {
    Intent intent = new Intent();
    intent.setClass(this, CustomShowcase.class);

    if(config.equals("USB") || (config.equals(WS_CONFIG) && uri != null)) {
      intent.putExtra(CustomShowcase.EXTRA_CLOVER_CONNECTOR_CONFIG, config);
      intent.putExtra(CustomShowcase.EXTRA_CLEAR_TOKEN, clearToken);
      intent.putExtra(CustomShowcase.EXTRA_WS_ENDPOINT, uri);
      startActivity(intent);
    }
  }

  private URI parseValidateAndStoreURI(String uriStr) {
    try {
      SharedPreferences prefs = this.getSharedPreferences(EXAMPLE_APP_NAME, Context.MODE_PRIVATE);
      SharedPreferences.Editor editor = prefs.edit();
      URI uri = new URI(uriStr);
      String addressOnly = String.format("%s://%s:%d%s", uri.getScheme(), uri.getHost(), uri.getPort(), uri.getPath());
      editor.putString(LAN_PAY_DISPLAY_URL, addressOnly);
      editor.putString(CONNECTION_MODE, LAN);
      editor.apply();
      return uri;
    } catch(URISyntaxException e) {
      Log.e(TAG, "Invalid URL" ,e);
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle("Error");
      builder.setMessage("Invalid URL");
      builder.show();
      return null;
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    // For non-clover devices this is how the generic barcode scanner
    // returns the scanned barcode
//    if (requestCode == BARCODE_READER_REQUEST_CODE) {
//      if (resultCode == CommonStatusCodes.SUCCESS) {
//        if (data != null) {
//          Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
//          connect(parseValidateAndStoreURI(barcode.displayValue), WS_CONFIG, false);
//        }
//      } else Log.e(TAG, String.format(getString(R.string.barcode_error_format),
//          CommonStatusCodes.getStatusCodeString(resultCode)));
//    } else
    super.onActivityResult(requestCode, resultCode, data);
  }
}
