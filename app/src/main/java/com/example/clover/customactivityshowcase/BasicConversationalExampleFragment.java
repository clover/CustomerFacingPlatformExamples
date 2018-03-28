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
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class BasicConversationalExampleFragment extends Fragment {

  private WeakReference<ICloverConnector> cloverConnectorWeakReference;
  private View view;
  private Switch nonBlocking;
  private Button startActivity, sendJoke;
  private TextView finalPayload, jokeResponse;
  private EditText initialPayload;

  public static BasicConversationalExampleFragment newInstance(ICloverConnector cloverConnector) {
    BasicConversationalExampleFragment fragment = new BasicConversationalExampleFragment();
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
    view = inflater.inflate(R.layout.fragment_basic_conversational_example, container, false);

    nonBlocking = view.findViewById(R.id.BasicConversationalBlocking);
    initialPayload = view.findViewById(R.id.BasicConversationalActivityPayload);
    finalPayload = view.findViewById(R.id.BasicConversationalFinalPayload);
    jokeResponse = view.findViewById(R.id.JokeResponse);
    startActivity = view.findViewById(R.id.BasicConversationalStartActivity);
    startActivity.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ((CustomShowcase)getActivity()).startActivity(view, "BasicConversationalExample", nonBlocking.isChecked(), initialPayload.getText().toString());
        sendJoke.setEnabled(true);
        finalPayload.setText(null);
        jokeResponse.setText(null);
        initialPayload.setText(getString(R.string.payload_placeholder));
      }
    });
    sendJoke = view.findViewById(R.id.SendJoke);
    sendJoke.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ((CustomShowcase)getActivity()).sendMessageToActivity(view,"BasicConversationalExample", getString(R.string.basic_conversational_example_joke));
      }
    });

    return view;
  }

  public void jokeResponse(final String payload){
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        jokeResponse.setText(payload);
      }
    });
  }

  public void finalPayload(final String payload){
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        finalPayload.setText(payload);
        sendJoke.setEnabled(false);
      }
    });
  }

  public void setCloverConnector(ICloverConnector cloverConnector) {
    this.cloverConnectorWeakReference = new WeakReference<ICloverConnector>(cloverConnector);
  }
}
