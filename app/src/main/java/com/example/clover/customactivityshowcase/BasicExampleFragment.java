package com.example.clover.customactivityshowcase;

import com.clover.remote.client.ICloverConnector;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class BasicExampleFragment extends Fragment {

  private WeakReference<ICloverConnector> cloverConnectorWeakReference;
  private View view;
  private Switch nonBlocking;
  private Button startActivity;
  private TextView finalPayload;
  private EditText initialPayload;

  public static BasicExampleFragment newInstance(ICloverConnector cloverConnector) {
    BasicExampleFragment fragment = new BasicExampleFragment();
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
    view = inflater.inflate(R.layout.fragment_basic_example, container, false);

    nonBlocking = view.findViewById(R.id.BasicBlocking);
    initialPayload = view.findViewById(R.id.BasicActivityPayload);
    finalPayload = view.findViewById(R.id.BasicFinalPayload);
    startActivity = view.findViewById(R.id.BasicStartActivity);
    startActivity.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ((CustomShowcase)getActivity()).startActivity(view, "BasicExample", nonBlocking.isChecked(), initialPayload.getText().toString());
        finalPayload.setText(null);
        initialPayload.setText(getString(R.string.payload_placeholder));
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
