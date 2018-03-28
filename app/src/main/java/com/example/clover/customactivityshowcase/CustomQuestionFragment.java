package com.example.clover.customactivityshowcase;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomQuestionFragment extends Fragment {
  private String question;
  private int rating;
  private View view;
  private TextView customQuestion;
  private ImageView[] offs = new ImageView[5];
  private ImageView[] ons = new ImageView[5];

  public static CustomQuestionFragment newInstance(String question, int rating){
    CustomQuestionFragment fragment = new CustomQuestionFragment();
    fragment.setQuestion(question);
    fragment.setRating(rating);
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_custom_question, container, false);

    customQuestion = (TextView) view.findViewById(R.id.RatingsQuestion);
    customQuestion.setText(question);

    offs[0] = (ImageView)view.findViewById(R.id.imageView1off);
    offs[1] = (ImageView)view.findViewById(R.id.imageView2off);
    offs[2] = (ImageView)view.findViewById(R.id.imageView3off);
    offs[3] = (ImageView)view.findViewById(R.id.imageView4off);
    offs[4] = (ImageView)view.findViewById(R.id.imageView5off);

    ons[0] = (ImageView)view.findViewById(R.id.imageView1on);
    ons[1] = (ImageView)view.findViewById(R.id.imageView2on);
    ons[2] = (ImageView)view.findViewById(R.id.imageView3on);
    ons[3] = (ImageView)view.findViewById(R.id.imageView4on);
    ons[4] = (ImageView)view.findViewById(R.id.imageView5on);

    setStars();

    return view;
  }

  public void setStars(){
    for(int i = offs.length-1; i>=0; i--){
      for(int j = 0; j < offs.length; j++){
        if( j < rating){
          ons[j].setVisibility(View.VISIBLE);
            offs[j].setVisibility(View.GONE);
        }
        else {
            ons[j].setVisibility(View.GONE);
            offs[j].setVisibility(View.VISIBLE);
          }
      }
    }
  }

  public void setQuestion(String question){
    this.question = question;
  }

  public void setRating(int rating){
    this.rating = rating;
    if(view != null){
      setStars();
    }
  }


}
