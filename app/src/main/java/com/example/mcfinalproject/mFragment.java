package com.example.mcfinalproject;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import static com.android.volley.VolleyLog.TAG;

public class mFragment extends Fragment {
    private int configuration;
    public mFragment(int configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(((MainActivity)getActivity()).getmPublisher()!=null && ((MainActivity)getActivity()).getmSubscriber()!=null)
        {
            if(configuration==-1)
            {
                addPublisher(((MainActivity)getActivity()).getmPublisher().getView());
                addSubscriber(((MainActivity)getActivity()).getmSubscriber().getView());
            }
            else
            {
                addSubscriber(((MainActivity)getActivity()).getmSubscriber().getView());
                addPublisher(((MainActivity)getActivity()).getmPublisher().getView());
            }
        }
//        ((MainActivity)getActivity()).afterFragmentLoaded();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSharedElementEnterTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.move));
        }
        Log.d("fragment","onCreate called");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d("fragment","onCreateView called");
        if(configuration==1)
            return inflater.inflate(R.layout.fragment1, container, false);
        else
            return inflater.inflate(R.layout.fragment2, container, false);
//        return fragView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("fragment","onViewCreated called");
        final ViewGroup frameLayout2 = (FrameLayout) view.findViewById(R.id.frameLayout2);
        final ViewGroup frameLayout3 = (FrameLayout) view.findViewById(R.id.frameLayout3);
        FrameLayout mSubContainer = view.findViewById(R.id.subscriber_container);
        FrameLayout mPubContainer = view.findViewById(R.id.publisher_container);
        Log.d("fragment","onViewCreated, config "+configuration);
        frameLayout2.setOnClickListener(v->{
            if(configuration==-1)
            {
                mFragment invertedFragment = getInvertedFragment();
                ((MainActivity)getActivity()).setCurrentFragment(invertedFragment);
                removePublisher();
                removeSubscriber();

//                androidx.fragment.app.Fragment fragment2 = new Fragment2();
                getFragmentManager()
                        .beginTransaction()
                        .addSharedElement(frameLayout2, ViewCompat.getTransitionName(frameLayout2))
                        .addSharedElement(frameLayout3, ViewCompat.getTransitionName(frameLayout3))
//                        .addToBackStack(TAG)
                        .replace(R.id.main_fragmentFrame, invertedFragment)
                        .commit();
                getFragmentManager().executePendingTransactions();
            }
        });
        frameLayout3.setOnClickListener(v -> {
            if(configuration==1)
            {
                mFragment invertedFragment = getInvertedFragment();
                ((MainActivity)getActivity()).setCurrentFragment(invertedFragment);
                removePublisher();
                removeSubscriber();

//                androidx.fragment.app.Fragment fragment2 = new Fragment2();
                getFragmentManager()
                        .beginTransaction()
                        .addSharedElement(frameLayout2, ViewCompat.getTransitionName(frameLayout2))
                        .addSharedElement(frameLayout3, ViewCompat.getTransitionName(frameLayout3))
//                        .addToBackStack(TAG)
                        .replace(R.id.main_fragmentFrame, invertedFragment)
                        .commit();
                getFragmentManager().executePendingTransactions();
            }
        });
    }
    public mFragment getInvertedFragment()
    {
        return new mFragment(configuration*-1);
    }
    public void addPublisher(View view)
    {
        FrameLayout publisherContainer = getView().findViewById(R.id.publisher_container);
        publisherContainer.addView(view);
    }
    public void addSubscriber(View view)
    {
        FrameLayout subscriberContainer = getView().findViewById(R.id.subscriber_container);
        subscriberContainer.addView(view);
    }
    public void removeSubscriber()
    {
        FrameLayout subscriberContainer = getView().findViewById(R.id.subscriber_container);
        subscriberContainer.removeAllViews();
    }
    public void removePublisher()
    {
        FrameLayout publisherContainer = getView().findViewById(R.id.publisher_container);
        publisherContainer.removeAllViews();
    }
    public View getCanvasViewClient()
    {
        return getView().findViewById(R.id.canvas);
    }

    public void setColor(int color) {
        if(getView()==null)
            Log.d("fragment","isNull");
        ((CanvasViewClient)getView().findViewById(R.id.canvas)).setColor(color);
    }
}