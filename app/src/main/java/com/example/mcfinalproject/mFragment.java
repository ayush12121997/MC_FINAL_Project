package com.example.mcfinalproject;

import android.graphics.Matrix;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

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

                addPublisher(((MainActivity)getActivity()).getPublisherView());
                addSubscriber(((MainActivity)getActivity()).getSubscriberView());
            }
            else
            {
                addSubscriber(((MainActivity)getActivity()).getSubscriberView());
                addPublisher(((MainActivity)getActivity()).getPublisherView());
            }
        }
//        ((MainActivity)getActivity()).afterFragmentLoaded();
        if(configuration==-1)
        {
           //Subscriber small
            getView().findViewById(R.id.canvasSubClient).setVisibility(View.INVISIBLE);
            ((MainActivity)getActivity()).checkDrawing();
        }
        else
        {
            //Publisher small
            getView().findViewById(R.id.canvasPubClient).setVisibility(View.INVISIBLE);
            ((MainActivity)getActivity()).checkDrawing();
        }
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
                ((MainActivity)getActivity()).invertSubBig();
                removePublisher();
                removeSubscriber();

                getFragmentManager()
                        .beginTransaction()
                        .addSharedElement(frameLayout2, ViewCompat.getTransitionName(frameLayout2))
                        .addSharedElement(frameLayout3, ViewCompat.getTransitionName(frameLayout3))
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
                ((MainActivity)getActivity()).invertSubBig();
                removePublisher();
                removeSubscriber();

                getFragmentManager()
                        .beginTransaction()
                        .addSharedElement(frameLayout2, ViewCompat.getTransitionName(frameLayout2))
                        .addSharedElement(frameLayout3, ViewCompat.getTransitionName(frameLayout3))
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

    public void setColor(int color) {
        while (getView()==null);
//            Log.d("fragment","isNull");
        if(configuration==1)
        {
            ((CanvasViewClient)getView().findViewById(R.id.canvasSubClient)).setColor(color);
        }
        else
        {
            ((CanvasViewClient)getView().findViewById(R.id.canvasPubClient)).setColor(color);
        }
//        ((CanvasViewClient)getView().findViewById(R.id.canvas)).setColor(color);
    }
    public View getCanvasPubClient()
    {
        while(getView()==null);
        return getView().findViewById(R.id.canvasPubClient);
    }
    public View getCanvasPubServer()
    {
        while(getView()==null);
        return getView().findViewById(R.id.canvasPubServer);
    }
    public View getCanvasSubClient()
    {
        while(getView()==null);
        return getView().findViewById(R.id.canvasSubClient);
    }
    public View getCanvasSubServer()
    {
        while(getView()==null);
        return getView().findViewById(R.id.canvasSubServer);
    }
}