package com.example.clover.customactivityshowcase;

import com.clover.remote.client.CloverConnector;
import com.clover.remote.client.CloverDeviceConfiguration;
import com.clover.remote.client.ICloverConnector;
import com.clover.remote.client.ICloverConnectorListener;
import com.clover.remote.client.MerchantInfo;
import com.clover.remote.client.USBCloverDeviceConfiguration;
import com.clover.remote.client.WebSocketCloverDeviceConfiguration;
import com.clover.remote.client.messages.AuthResponse;
import com.clover.remote.client.messages.CapturePreAuthResponse;
import com.clover.remote.client.messages.CloseoutResponse;
import com.clover.remote.client.messages.CloverDeviceErrorEvent;
import com.clover.remote.client.messages.CloverDeviceEvent;
import com.clover.remote.client.messages.ConfirmPaymentRequest;
import com.clover.remote.client.messages.CustomActivityRequest;
import com.clover.remote.client.messages.CustomActivityResponse;
import com.clover.remote.client.messages.ManualRefundResponse;
import com.clover.remote.client.messages.MessageFromActivity;
import com.clover.remote.client.messages.MessageToActivity;
import com.clover.remote.client.messages.PreAuthResponse;
import com.clover.remote.client.messages.PrintJobStatusResponse;
import com.clover.remote.client.messages.PrintManualRefundDeclineReceiptMessage;
import com.clover.remote.client.messages.PrintManualRefundReceiptMessage;
import com.clover.remote.client.messages.PrintPaymentDeclineReceiptMessage;
import com.clover.remote.client.messages.PrintPaymentMerchantCopyReceiptMessage;
import com.clover.remote.client.messages.PrintPaymentReceiptMessage;
import com.clover.remote.client.messages.PrintRefundPaymentReceiptMessage;
import com.clover.remote.client.messages.ReadCardDataResponse;
import com.clover.remote.client.messages.RefundPaymentResponse;
import com.clover.remote.client.messages.ResetDeviceResponse;
import com.clover.remote.client.messages.ResultCode;
import com.clover.remote.client.messages.RetrieveDeviceStatusResponse;
import com.clover.remote.client.messages.RetrievePaymentResponse;
import com.clover.remote.client.messages.RetrievePendingPaymentsResponse;
import com.clover.remote.client.messages.RetrievePrintersRequest;
import com.clover.remote.client.messages.RetrievePrintersResponse;
import com.clover.remote.client.messages.SaleResponse;
import com.clover.remote.client.messages.TipAdjustAuthResponse;
import com.clover.remote.client.messages.VaultCardResponse;
import com.clover.remote.client.messages.VerifySignatureRequest;
import com.clover.remote.client.messages.VoidPaymentResponse;
import com.clover.remote.message.TipAddedMessage;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import com.example.clover.customactivityshowcase.messages.ConversationQuestionMessage;
import com.example.clover.customactivityshowcase.messages.ConversationResponseMessage;
import com.example.clover.customactivityshowcase.messages.CustomerInfo;
import com.example.clover.customactivityshowcase.messages.CustomerInfoMessage;
import com.example.clover.customactivityshowcase.messages.PayloadMessage;
import com.example.clover.customactivityshowcase.messages.PhoneNumberMessage;
import com.example.clover.customactivityshowcase.messages.Rating;
import com.example.clover.customactivityshowcase.messages.RatingsMessage;
import com.example.clover.customactivityshowcase.utils.SecurityUtils;
import com.google.android.gms.vision.Frame;
import com.google.gson.Gson;

import java.net.URI;
import java.net.URLDecoder;
import java.security.KeyStore;
import java.util.prefs.Preferences;

public class CustomShowcase extends Activity {
  private static final String TAG = "CustomShowcase";
  public static final String EXAMPLE_POS_SERVER_KEY = "clover_device_endpoint";
  public static final String EXTRA_CLOVER_CONNECTOR_CONFIG = "EXTRA_CLOVER_CONNECTOR_CONFIG";
  public static final String EXTRA_WS_ENDPOINT = "WS_ENDPOINT";
  public static final String EXTRA_CLEAR_TOKEN = "CLEAR_TOKEN";
  public static final String CUSTOM_ACTIVITY_PACKAGE = "com.clover.cfp.examples.";
  private AlertDialog pairingCodeDialog;
  private ICloverConnector cloverConnector;
  private SharedPreferences sharedPreferences;
  private Dialog ratingsDialog;
  private ListView ratingsList;
  private ArrayAdapter<String> ratingsAdapter;
  FrameLayout frameLayout;


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_custom_showcase);

    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    String posName = "Clover Custom Activity Tester";
    String applicationId = posName + ":1.4.1";
    CloverDeviceConfiguration config;

    String configType = getIntent().getStringExtra(EXTRA_CLOVER_CONNECTOR_CONFIG);
    if ("USB".equals(configType)) {
      config = new USBCloverDeviceConfiguration(this, applicationId);
    } else if ("WS".equals(configType)) {

      String serialNumber = "Aisle 4";
      String authToken = null;

      URI uri = (URI) getIntent().getSerializableExtra(EXTRA_WS_ENDPOINT);

      String query = uri.getRawQuery();
      if (query != null) {
        try {
          String[] nameValuePairs = query.split("&");
          for (String nameValuePair : nameValuePairs) {
            String[] nameAndValue = nameValuePair.split("=", 2);
            String name = URLDecoder.decode(nameAndValue[0], "UTF-8");
            String value = URLDecoder.decode(nameAndValue[1], "UTF-8");

            if("authenticationToken".equals(name)) {
              authToken = value;
            } else {
              Log.w(TAG, String.format("Found query parameter \"%s\" with value \"%s\"",
                  name, value));
            }
          }
          uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(),uri.getPort(), uri.getPath(), null,uri.getFragment());
        } catch (Exception e) {
          Log.e(TAG, "Error extracting query information from uri.", e);
          setResult(RESULT_CANCELED);
          finish();
          return;
        }
      }
      KeyStore trustStore = SecurityUtils.createTrustStore(true);

      if(authToken == null) {
        boolean clearToken = getIntent().getBooleanExtra(EXTRA_CLEAR_TOKEN, false);
        if (!clearToken) {
          authToken = sharedPreferences.getString("AUTH_TOKEN", null);
        }
      }
      config = new WebSocketCloverDeviceConfiguration(uri, applicationId, trustStore, posName, serialNumber, authToken) {
        @Override
        public int getMaxMessageCharacters() {
          return 0;
        }

        @Override
        public void onPairingCode(final String pairingCode) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              // If we previously created a dialog and the pairing failed, reuse
              // the dialog previously created so that we don't get a stack of dialogs
              if (pairingCodeDialog != null) {
                pairingCodeDialog.setMessage("Enter pairing code: " + pairingCode);
              } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(CustomShowcase.this);
                builder.setTitle("Pairing Code");
                builder.setMessage("Enter pairing code: " + pairingCode);
                pairingCodeDialog = builder.create();
              }
              pairingCodeDialog.show();
            }
          });
        }

        @Override
        public void onPairingSuccess(String authToken) {
          Preferences.userNodeForPackage(CustomShowcase.class).put("AUTH_TOKEN", authToken);
          sharedPreferences.edit().putString("AUTH_TOKEN", authToken).apply();
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              if (pairingCodeDialog != null && pairingCodeDialog.isShowing()) {
                pairingCodeDialog.dismiss();
                pairingCodeDialog = null;
              }
            }
          });
        }
      };
    } else {
      finish();
      return;
    }

    cloverConnector = new CloverConnector(config);
    initialize();

    ratingsDialog = new Dialog(CustomShowcase.this);
    ratingsDialog.setContentView(R.layout.finalratings_layout);
    ratingsDialog.setCancelable(true);
    ratingsDialog.setCanceledOnTouchOutside(true);
    ratingsList = (ListView) ratingsDialog.findViewById(R.id.ratingsList);
    ratingsAdapter = new ArrayAdapter<>(CustomShowcase.this, android.R.layout.simple_list_item_1, new String[0]);

    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    frameLayout = (FrameLayout) findViewById(R.id.CustomFrameLayout);
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    SelectCustomActivityFragment selectCustom = SelectCustomActivityFragment.newInstance(cloverConnector);
    fragmentTransaction.add(R.id.CustomFrameLayout, selectCustom, "SELECT_CUSTOM");
    fragmentTransaction.commit();

  }

  public void initialize() {

    if (cloverConnector != null) {
      cloverConnector.dispose();
    }

    ICloverConnectorListener ccListener = new ICloverConnectorListener() {
      public void onDeviceDisconnected() {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(CustomShowcase.this, "Disconnected", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "disconnected");
          }
        });

      }

      public void onDeviceConnected() {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(CustomShowcase.this, "Connecting...", Toast.LENGTH_SHORT).show();
          }
        });
      }

      public void onDeviceReady(final MerchantInfo merchantInfo) {
        runOnUiThread(new Runnable() {
          public void run() {
            if (pairingCodeDialog != null && pairingCodeDialog.isShowing()) {
              pairingCodeDialog.dismiss();
              pairingCodeDialog = null;
            }
            Toast.makeText(CustomShowcase.this, "Ready!", Toast.LENGTH_SHORT).show();
          }
        });
        RetrievePrintersRequest rpr = new RetrievePrintersRequest();
        cloverConnector.retrievePrinters(rpr);
      }

      public void onError(final Exception e) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(CustomShowcase.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG);
          }
        });
      }

      public void onDebug(final String s) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(CustomShowcase.this, "Debug: " + s, Toast.LENGTH_LONG);
          }
        });
      }

      @Override
      public void onDeviceActivityStart(final CloverDeviceEvent deviceEvent) {

      }

      @Override
      public void onReadCardDataResponse(final ReadCardDataResponse response) {
      }

      @Override
      public void onDeviceActivityEnd(final CloverDeviceEvent deviceEvent) {
      }

      @Override
      public void onDeviceError(CloverDeviceErrorEvent deviceErrorEvent) {
        Toast.makeText(CustomShowcase.this, "DeviceError: " + deviceErrorEvent.getMessage(), Toast.LENGTH_LONG);
      }

      @Override
      public void onAuthResponse(final AuthResponse response) {
      }

      @Override
      public void onPreAuthResponse(final PreAuthResponse response) {
      }

      @Override
      public void onRetrievePendingPaymentsResponse(RetrievePendingPaymentsResponse response) {
      }

      @Override
      public void onTipAdjustAuthResponse(TipAdjustAuthResponse response) {
      }

      @Override
      public void onCapturePreAuthResponse(final CapturePreAuthResponse response) {
      }

      @Override
      public void onVerifySignatureRequest(final VerifySignatureRequest request) {
      }

      @Override
      public void onMessageFromActivity(MessageFromActivity message) {
        PayloadMessage payloadMessage = new Gson().fromJson(message.getPayload(), PayloadMessage.class);
        if (payloadMessage.messageType == null){
          showPopupMessage("Custom Activity Response", new String[]{"Payload: "+message.getPayload()}, false);
        }
        switch (payloadMessage.messageType) {
          case REQUEST_RATINGS:
            handleRequestRatings();
            break;
          case RATINGS:
            handleRatings(message.getPayload());
            break;
          case PHONE_NUMBER:
            handleCustomerLookup(message.getPayload());
            break;
          case CONVERSATION_RESPONSE:
            handleJokeResponse(message.getPayload());
            break;
          default:
            Toast.makeText(getApplicationContext(), R.string.unknown_payload + payloadMessage.messageType.name(), Toast.LENGTH_LONG).show();
        }
      }

      @Override
      public void onConfirmPaymentRequest(ConfirmPaymentRequest request) {
      }

      @Override
      public void onCloseoutResponse(CloseoutResponse response) {
      }

      @Override
      public void onSaleResponse(final SaleResponse response) {
      }

      @Override
      public void onManualRefundResponse(final ManualRefundResponse response) {
      }

      @Override
      public void onRefundPaymentResponse(final RefundPaymentResponse response) {
      }

      @Override
      public void onTipAdded(TipAddedMessage message) {

      }

      @Override
      public void onVoidPaymentResponse(VoidPaymentResponse response) {
      }

      @Override
      public void onVaultCardResponse(final VaultCardResponse response) {
      }

      @Override
      public void onPrintJobStatusResponse(PrintJobStatusResponse response) {
      }

      @Override
      public void onRetrievePrintersResponse(RetrievePrintersResponse response) {
      }

      @Override
      public void onPrintManualRefundReceipt(PrintManualRefundReceiptMessage pcm) {
      }

      @Override
      public void onPrintManualRefundDeclineReceipt(PrintManualRefundDeclineReceiptMessage pcdrm) {
      }

      @Override
      public void onPrintPaymentReceipt(PrintPaymentReceiptMessage pprm) {
      }

      @Override
      public void onPrintPaymentDeclineReceipt(PrintPaymentDeclineReceiptMessage ppdrm) {
      }

      @Override
      public void onPrintPaymentMerchantCopyReceipt(PrintPaymentMerchantCopyReceiptMessage ppmcrm) {
      }

      @Override
      public void onPrintRefundPaymentReceipt(PrintRefundPaymentReceiptMessage pprrm) {
      }

      @Override

      public void onCustomActivityResponse(final CustomActivityResponse response) {
        if (response.isSuccess()) {
          FragmentManager fragmentManager = getFragmentManager();
          Fragment fragment;
          switch (response.getAction()){
            case CUSTOM_ACTIVITY_PACKAGE + "BasicExample":
              fragment = fragmentManager.findFragmentByTag("BASIC");
              ((BasicExampleFragment)fragment).finalPayload(response.getPayload());
              break;
            case CUSTOM_ACTIVITY_PACKAGE + "BasicConversationalExample":
              fragment = fragmentManager.findFragmentByTag("BASIC_CONVERSATIONAL");
              ((BasicConversationalExampleFragment)fragment).finalPayload(response.getPayload());
              break;
            case CUSTOM_ACTIVITY_PACKAGE + "WebViewExample":
              fragment = fragmentManager.findFragmentByTag("WEB_VIEW");
            ((WebViewExampleFragment)fragment).finalPayload(response.getPayload());
            break;
            case CUSTOM_ACTIVITY_PACKAGE + "RatingsExample":
              fragment = fragmentManager.findFragmentByTag("RATINGS");
              ((RatingsExampleFragment)fragment).finalPayload(response.getPayload());
              break;
            case CUSTOM_ACTIVITY_PACKAGE + "NFCExample":
              fragment = fragmentManager.findFragmentByTag("NFC");
              ((NFCExampleFragment)fragment).finalPayload(response.getPayload());
              break;
          }
        } else {
          if (response.getResult().equals(ResultCode.CANCEL)) {
            showPopupMessage(null, new String[]{"Custom activity: " + response.getAction() + " was canceled.", "Reason: " + response.getReason()}, false);
            // showMessage("Custom activity: " + response.getAction() + " was canceled.  Reason: " + response.getReason(), 5000);
          } else {
            showPopupMessage(null, new String[]{"Failure! Custom activity: " + response.getAction() + " failed.", "Reason: " + response.getReason()}, false);
            //         showMessage("Failure! Custom activity: " + response.getAction() + " failed.  Reason: " + response.getReason(), 5000);
          }
        }
      }

      @Override
      public void onRetrieveDeviceStatusResponse(RetrieveDeviceStatusResponse response) {
      }

      @Override
      public void onResetDeviceResponse(ResetDeviceResponse response) {
      }

      @Override
      public void onRetrievePaymentResponse(RetrievePaymentResponse response) {
      }
    };
    cloverConnector.addCloverConnectorListener(ccListener);
    cloverConnector.initializeConnection();
  }

  public void sendMessageToActivity(View view, String activity_id, String messageContent) {
    String activityId = CUSTOM_ACTIVITY_PACKAGE + activity_id;
    ConversationQuestionMessage message = new ConversationQuestionMessage(messageContent);
    String payload = message.toJsonString();
    MessageToActivity messageRequest = new MessageToActivity(activityId, payload);
    cloverConnector.sendMessageToActivity(messageRequest);
  }

  public void sendMessageToActivity(String activityId, String payload) {
    MessageToActivity messageRequest = new MessageToActivity(activityId, payload);
    cloverConnector.sendMessageToActivity(messageRequest);
  }

  private void handleRequestRatings() {
    Rating rating1 = new Rating();
    FragmentManager fragmentManager = getFragmentManager();
    Fragment fragment = fragmentManager.findFragmentByTag("RATINGS");
    ((RatingsExampleFragment)fragment).handleRequestRatings();
  }

  private void handleRatings(String payload) {
    //showMessage(payload, Toast.LENGTH_SHORT);
    RatingsMessage ratingsMessage = (RatingsMessage) PayloadMessage.fromJsonString(payload);
    Rating[] ratingsPayload = ratingsMessage.ratings;
    FragmentManager fragmentManager = getFragmentManager();
    Fragment fragment = fragmentManager.findFragmentByTag("RATINGS");
    ((RatingsExampleFragment)fragment).handleRatings(ratingsPayload);
//    showRatingsDialog(ratingsPayload);
//    for (Rating rating:ratingsPayload
//         ) {
//      String ratingString = "Rating ID: " + rating.id + " - " + rating.question + " Rating value: " + Integer.toString(rating.value);
//      showMessage(ratingString, Toast.LENGTH_SHORT);
//    }
  }

  private void handleCustomerLookup(String payload) {
    PhoneNumberMessage phoneNumberMessage = new Gson().fromJson(payload, PhoneNumberMessage.class);
    String phoneNumber = phoneNumberMessage.phoneNumber;
    FragmentManager fragmentManager = getFragmentManager();
    Fragment fragment = fragmentManager.findFragmentByTag("RATINGS");
    ((RatingsExampleFragment)fragment).handleCustomerLookup(phoneNumber);
  }

  private void handleJokeResponse(String payload) {
    ConversationResponseMessage jokeResponseMessage = (ConversationResponseMessage) PayloadMessage.fromJsonString(payload);
    FragmentManager fragmentManager = getFragmentManager();
    Fragment fragment = fragmentManager.findFragmentByTag("BASIC_CONVERSATIONAL");
    ((BasicConversationalExampleFragment)fragment).jokeResponse(jokeResponseMessage.message);
  }

  private void showMessage(final String msg, final int duration) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(CustomShowcase.this, msg, duration).show();
      }
    });
  }

  protected void showPopupMessage (final String title, final String[] content, final boolean monospace) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        FragmentManager fm = getFragmentManager();
        PopupMessageFragment popupMessageFragment = PopupMessageFragment.newInstance(title, content, monospace);
        popupMessageFragment.show(fm, "fragment_popup_message");
      }
    });
  }

  public void startActivity(View view, String activity_id, boolean nonBlocking) {
    String activityId = CUSTOM_ACTIVITY_PACKAGE + activity_id;
    String payload = ("{Message: Start Activity}");

    CustomActivityRequest car = new CustomActivityRequest(activityId);
    car.setPayload(payload);
    car.setNonBlocking(nonBlocking);

    cloverConnector.startCustomActivity(car);
  }
  public void startActivity(View view, String activity_id, boolean nonBlocking, String initialPayload) {
    String activityId = CUSTOM_ACTIVITY_PACKAGE + activity_id;

    CustomActivityRequest car = new CustomActivityRequest(activityId);
    car.setPayload(initialPayload);
    car.setNonBlocking(nonBlocking);

    cloverConnector.startCustomActivity(car);
  }

  protected void hideFragments(FragmentManager fragmentManager, FragmentTransaction fragmentTransaction) {
    Fragment fragment = fragmentManager.findFragmentByTag("SELECT_CUSTOM");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("BASIC");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("BASIC_CONVERSATIONAL");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("CAROUSEL");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("RATINGS");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("WEB_VIEW");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("NFC");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
  }

  public void showBasicExample(View view){
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("BASIC");
    if (fragment == null) {
      fragment = BasicExampleFragment.newInstance(cloverConnector);
      fragmentTransaction.add(R.id.CustomFrameLayout, fragment, "BASIC");
    } else {
      ((BasicExampleFragment) fragment).setCloverConnector(cloverConnector);
      fragmentTransaction.show(fragment);
    }
    fragmentTransaction.addToBackStack("BASIC");
    fragmentTransaction.commit();
  }

  public void showBasicConversationalExample(View view){
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("BASIC_CONVERSATIONAL");
    if (fragment == null) {
      fragment = BasicConversationalExampleFragment.newInstance(cloverConnector);
      fragmentTransaction.add(R.id.CustomFrameLayout, fragment, "BASIC_CONVERSATIONAL");
    } else {
      ((BasicConversationalExampleFragment) fragment).setCloverConnector(cloverConnector);
      fragmentTransaction.show(fragment);
    }
    fragmentTransaction.addToBackStack("BASIC_CONVERSATIONAL");
    fragmentTransaction.commit();
  }

  public void showCarouselExample(View view){
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("CAROUSEL");
    if (fragment == null) {
      fragment = CarouselExampleFragment.newInstance(cloverConnector);
      fragmentTransaction.add(R.id.CustomFrameLayout, fragment, "CAROUSEL");
    } else {
      ((CarouselExampleFragment) fragment).setCloverConnector(cloverConnector);
      fragmentTransaction.show(fragment);
    }
    fragmentTransaction.addToBackStack("CAROUSEL");
    fragmentTransaction.commit();
  }

  public void showRatingsExample(View view){
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("RATINGS");
    if (fragment == null) {
      fragment = RatingsExampleFragment.newInstance(cloverConnector);
      fragmentTransaction.add(R.id.CustomFrameLayout, fragment, "RATINGS");
    } else {
      ((RatingsExampleFragment) fragment).setCloverConnector(cloverConnector);
      fragmentTransaction.show(fragment);
    }
    fragmentTransaction.addToBackStack("RATINGS");
    fragmentTransaction.commit();
  }

  public void showWebViewExample(View view){
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("WEB_VIEW");
    if (fragment == null) {
      fragment = WebViewExampleFragment.newInstance(cloverConnector);
      fragmentTransaction.add(R.id.CustomFrameLayout, fragment, "WEB_VIEW");
    } else {
      ((WebViewExampleFragment) fragment).setCloverConnector(cloverConnector);
      fragmentTransaction.show(fragment);
    }
    fragmentTransaction.addToBackStack("WEB_VIEW");
    fragmentTransaction.commit();
  }

  public void showNFCExample(View view){
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("NFC");
    if (fragment == null) {
      fragment = NFCExampleFragment.newInstance(cloverConnector);
      fragmentTransaction.add(R.id.CustomFrameLayout, fragment, "NFC");
    } else {
      ((NFCExampleFragment) fragment).setCloverConnector(cloverConnector);
      fragmentTransaction.show(fragment);
    }
    fragmentTransaction.addToBackStack("NFC");
    fragmentTransaction.commit();
  }

  public void onResetDeviceClick(View view) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        new AlertDialog.Builder(CustomShowcase.this)
            .setTitle("Reset Device")
            .setMessage("Are you sure you want to reset the device?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                cloverConnector.resetDevice();
              }
            })
            .setNegativeButton("No", null)
            .show();
      }
    });
  }

}
