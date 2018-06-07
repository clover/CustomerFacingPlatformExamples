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

public class CarouselExampleFragment extends Fragment {

  private WeakReference<ICloverConnector> cloverConnectorWeakReference;
  private View view;
  private Switch nonBlocking;
  private Button startActivity;

  public static CarouselExampleFragment newInstance(ICloverConnector cloverConnector) {
    CarouselExampleFragment fragment = new CarouselExampleFragment();
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
    view = inflater.inflate(R.layout.fragment_carousel_example, container, false);

    nonBlocking = view.findViewById(R.id.CarouselBlocking);
    startActivity = view.findViewById(R.id.CarouselStartActivity);
    startActivity.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ((CustomShowcase)getActivity()).startActivity(view, "CarouselExample", nonBlocking.isChecked());
      }
    });

    return view;
  }

  public void setCloverConnector(ICloverConnector cloverConnector) {
    this.cloverConnectorWeakReference = new WeakReference<ICloverConnector>(cloverConnector);
  }
}
