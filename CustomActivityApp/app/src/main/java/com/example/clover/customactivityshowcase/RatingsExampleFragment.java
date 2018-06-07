package com.example.clover.customactivityshowcase;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.clover.remote.client.ICloverConnector;

import android.app.Fragment;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.example.clover.customactivityshowcase.messages.CustomerInfo;
import com.example.clover.customactivityshowcase.messages.CustomerInfoMessage;
import com.example.clover.customactivityshowcase.messages.Rating;
import com.example.clover.customactivityshowcase.messages.RatingsMessage;

import java.lang.ref.WeakReference;

public class RatingsExampleFragment extends Fragment {

  private WeakReference<ICloverConnector> cloverConnectorWeakReference;
  private View view;
  private Switch nonBlocking;
  private Button startActivity;
  private TextView name, phoneNumber, questionsTextView;
  String question1;
  String question2;
  String question3;
  String question4;


  public static RatingsExampleFragment newInstance(ICloverConnector cloverConnector) {
    RatingsExampleFragment fragment = new RatingsExampleFragment();
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
    view = inflater.inflate(R.layout.fragment_ratings_example, container, false);
    nonBlocking = view.findViewById(R.id.RatingsBlocking);
    name = view.findViewById(R.id.RatingsName);
    phoneNumber = view.findViewById(R.id.RatingsPhoneNumber);
    questionsTextView = view.findViewById(R.id.QuestionsTextView);
    question1 = getString(R.string.question1);
    question2 = getString(R.string.question2);
    question3 = getString(R.string.question3);
    question4 = getString(R.string.question4);

    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    CustomQuestionFragment ratingQuestion1 = CustomQuestionFragment.newInstance(question1, 0);
    fragmentTransaction.add(R.id.RatingsQuestion1, ratingQuestion1, "QUESTION_1");
    CustomQuestionFragment ratingQuestion2 = CustomQuestionFragment.newInstance(question2, 0);
    fragmentTransaction.add(R.id.RatingsQuestion2, ratingQuestion2, "QUESTION_2");
    CustomQuestionFragment ratingQuestion3 = CustomQuestionFragment.newInstance(question3, 0);
    fragmentTransaction.add(R.id.RatingsQuestion3, ratingQuestion3, "QUESTION_3");
    CustomQuestionFragment ratingQuestion4 = CustomQuestionFragment.newInstance(question4, 0);
    fragmentTransaction.add(R.id.RatingsQuestion4, ratingQuestion4, "QUESTION_4");
    fragmentTransaction.commit();

    startActivity = view.findViewById(R.id.RatingsStartActivity);
    startActivity.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ((CustomShowcase)getActivity()).startActivity(view, "RatingsExample", nonBlocking.isChecked());
      }
    });
    return view;
  }

  public void handleCustomerLookup(final String number){
//    showMessage("Just received phone number " + phoneNumber + " from the Ratings remote application.", 3000);
//    showMessage("Sending customer name Ron Burgundy to the Ratings remote application for phone number " + phoneNumber, 3000);
    final String customer = getCustomer(number);
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        phoneNumber.setText(PhoneNumberUtils.formatNumber(number));
        name.setText(customer);
        ((CustomShowcase)getActivity()).showPopupMessage(null, new String[]{getString(R.string.sending_customer_name, customer, PhoneNumberUtils.formatNumber(number))}, false);
      }
    });
    CustomerInfo customerInfo = new CustomerInfo();
    customerInfo.customerName = customer;
    customerInfo.phoneNumber = number;
    CustomerInfoMessage customerInfoMessage = new CustomerInfoMessage(customerInfo);
    String customerInfoJson = customerInfoMessage.toJsonString();
    ((CustomShowcase)getActivity()).sendMessageToActivity("com.clover.cfp.examples.RatingsExample", customerInfoJson);
  }

  /*
   * This is where you could have a real customer lookup, this switch is simply here for example purposes.
   */
  public String getCustomer(String phoneNumber){
    String customerName = "";
  int lastCharacter = Integer.parseInt(phoneNumber.substring(phoneNumber.length() - 1));
    switch (lastCharacter){
      case 0:
        customerName = "Ron Burgundy";
        break;
      case 1:
        customerName = "Ron Swanson";
        break;
      case 2:
        customerName = "Arya Stark";
        break;
      case 4:
        customerName = "Micheal Scott";
        break;
      case 5:
        customerName = "Dwight Schrute";
        break;
      case 6:
        customerName = "Leslie Knope";
        break;
      case 7:
        customerName = "April Ludgate";
        break;
      case 8:
        customerName = "Leia Organa";
        break;
      case 9:
        customerName = "Donna Meagle";
        break;
    }
    return customerName;
  }

  public void handleRequestRatings() {
    Rating rating1 = new Rating();
    rating1.id = "Quality";
    rating1.question = question1;
    rating1.value = 0;
    Rating rating2 = new Rating();
    rating2.id = "Server";
    rating2.question = question2;
    rating2.value = 0;
    Rating rating3 = new Rating();
    rating3.id = "Value";
    rating3.question = question3;
    rating3.value = 0;
    Rating rating4 = new Rating();
    rating4.id = "RepeatBusiness";
    rating4.question = question4;
    rating4.value = 0;
    Rating[] ratings = new Rating[]{rating1, rating2, rating3, rating4};
    RatingsMessage ratingsMessage = new RatingsMessage(ratings);
    String ratingsListJson = ratingsMessage.toJsonString();
    ((CustomShowcase)getActivity()).sendMessageToActivity("com.clover.cfp.examples.RatingsExample", ratingsListJson);
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        questionsTextView.setText(getString(R.string.questions_sent));
      }
    });
  }

  public void handleRatings(final Rating[] ratingsPayload){
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment1 = fragmentManager.findFragmentByTag("QUESTION_1");
        if (fragment1 != null) {
          ((CustomQuestionFragment)fragment1).setRating(ratingsPayload[0].value);
        }
        Fragment fragment2 = fragmentManager.findFragmentByTag("QUESTION_2");
        if (fragment2 != null) {
          ((CustomQuestionFragment)fragment2).setRating(ratingsPayload[1].value);
        }
        Fragment fragment3 = fragmentManager.findFragmentByTag("QUESTION_3");
        if (fragment3 != null) {
          ((CustomQuestionFragment)fragment3).setRating(ratingsPayload[2].value);
        }
        Fragment fragment4 = fragmentManager.findFragmentByTag("QUESTION_4");
        if (fragment4 != null) {
          ((CustomQuestionFragment)fragment4).setRating(ratingsPayload[3].value);
        }
      }
    });
  }

  public void resetRatings(){
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment1 = fragmentManager.findFragmentByTag("QUESTION_1");
        if (fragment1 != null) {
          ((CustomQuestionFragment)fragment1).setRating(0);
        }
        Fragment fragment2 = fragmentManager.findFragmentByTag("QUESTION_2");
        if (fragment2 != null) {
          ((CustomQuestionFragment)fragment2).setRating(0);
        }
        Fragment fragment3 = fragmentManager.findFragmentByTag("QUESTION_3");
        if (fragment3 != null) {
          ((CustomQuestionFragment)fragment3).setRating(0);
        }
        Fragment fragment4 = fragmentManager.findFragmentByTag("QUESTION_4");
        if (fragment4 != null) {
          ((CustomQuestionFragment)fragment4).setRating(0);
        }
      }
    });
  }

  public void reset(){
    resetRatings();
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        phoneNumber.setText(null);
        name.setText(null);
      }
    });
  }


  // here is where you would want to save the ratings, probably associated with the customer
  public void finalPayload(final String payload){
    reset();
  }
  public void setCloverConnector(ICloverConnector cloverConnector) {
    this.cloverConnectorWeakReference = new WeakReference<ICloverConnector>(cloverConnector);
  }
}
