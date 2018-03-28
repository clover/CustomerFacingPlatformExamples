package com.example.clover.customactivityshowcase;

import com.clover.remote.client.ICloverConnector;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.lang.ref.WeakReference;

/**
 * Created by rachel.antion on 3/12/18.
 */

public class SelectCustomActivityFragment extends Fragment {

  private WeakReference<ICloverConnector> cloverConnectorWeakReference;
  private LinearLayout basic, basicConversational, carousel, webView, ratings, nfc;
  private View view;

  public static SelectCustomActivityFragment newInstance(ICloverConnector cloverConnector) {
    SelectCustomActivityFragment fragment = new SelectCustomActivityFragment();
    fragment.setCloverConnector(cloverConnector);
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_select_custom, container, false);

    basic = (LinearLayout) view.findViewById(R.id.BasicExample);
    basicConversational = (LinearLayout) view.findViewById(R.id.BasicConversationalExample);
    carousel = (LinearLayout) view.findViewById(R.id.CarouselExample);
    webView = (LinearLayout) view.findViewById(R.id.WebViewExample);
    ratings = (LinearLayout) view.findViewById(R.id.RatingsExample);
    nfc = (LinearLayout) view.findViewById(R.id.NFCExample);

    basic.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ((CustomShowcase)getActivity()).showBasicExample(view);
      }
    });
    basicConversational.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
//        ((CustomShowcase)getActivity()).startActivity(view, "BasicConversationalExample");
        ((CustomShowcase)getActivity()).showBasicConversationalExample(view);
      }
    });
    carousel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
//        ((CustomShowcase)getActivity()).startActivity(view, "CarouselExample");
        ((CustomShowcase)getActivity()).showCarouselExample(view);
      }
    });
    webView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
//        ((CustomShowcase)getActivity()).startActivity(view, "WebViewExample");
        ((CustomShowcase)getActivity()).showWebViewExample(view);
      }
    });
    ratings.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
//        ((CustomShowcase)getActivity()).startActivity(view, "RatingsExample");
        ((CustomShowcase)getActivity()).showRatingsExample(view);
      }
    });
    nfc.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
//        ((CustomShowcase)getActivity()).startActivity(view, "NFCExample");
        ((CustomShowcase)getActivity()).showNFCExample(view);
      }
    });

    return view;
  }

  public void setCloverConnector(ICloverConnector cloverConnector) {
    this.cloverConnectorWeakReference = new WeakReference<ICloverConnector>(cloverConnector);
  }

}
