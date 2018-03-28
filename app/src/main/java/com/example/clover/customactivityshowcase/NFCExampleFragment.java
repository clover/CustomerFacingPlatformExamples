package com.example.clover.customactivityshowcase;

import android.app.Activity;
import android.os.Bundle;

import com.clover.remote.client.ICloverConnector;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class NFCExampleFragment extends Fragment {

  private WeakReference<ICloverConnector> cloverConnectorWeakReference;
  private View view;
  private Switch nonBlocking;
  private Button startActivity;
  private TextView finalPayload;

  public static NFCExampleFragment newInstance(ICloverConnector cloverConnector) {
    NFCExampleFragment fragment = new NFCExampleFragment();
    fragment.setCloverConnector(cloverConnector);
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_nfc_example, container, false);
    nonBlocking = view.findViewById(R.id.NFCBlocking);
    finalPayload = view.findViewById(R.id.NFCFinalPayload);
    startActivity = view.findViewById(R.id.NFCStartActivity);
    startActivity.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ((CustomShowcase)getActivity()).startActivity(view, "NFCExample", nonBlocking.isChecked());
        finalPayload.setText(null);
      }
    });
    return view;
  }

  public void finalPayload(final String payload){
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        finalPayload.setText(payload);
      }
    });
  }

  public void setCloverConnector(ICloverConnector cloverConnector) {
    this.cloverConnectorWeakReference = new WeakReference<ICloverConnector>(cloverConnector);
  }
}

