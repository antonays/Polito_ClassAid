package fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import smartsched.classaid_ver12.MainActivity;
import smartsched.classaid_ver12.R;

public class MapViewFragment extends Fragment implements MapFragmentCommunicator{
    //@Nullable
    ImageView roomImageView;
    View view;
    String roomName;
    TextView textualIndicator;
    public Context context;
    boolean isExistRoom;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.map_fragment,container,false);
        textualIndicator = (TextView)view.findViewById(R.id.nextOrNotroom);

        return view;
}
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        context = getActivity();
        ((MainActivity)context).mapFragmentCommunicator = this;
    }

    private void setMap(String path){
        int roomNameId = getResources().getIdentifier(path, "drawable", getActivity().getPackageName());
        Drawable drawable = getResources().getDrawable(roomNameId);
        roomImageView = (ImageView) view.findViewById(R.id.room_image_id);
        roomImageView.setImageDrawable(drawable);
        if (isExistRoom){
            if (roomName.equals("nouser")){
                textualIndicator.setText("Please Register");
            } else{
                textualIndicator.setText("Your Next Room: " + roomName);
            }
        }
        else{
            textualIndicator.setText("Next lecture is free.");
        }
    }

    /*@Override
    public void onStop(){
        super.onStop();
        roomImageView = null;
        roomName = null;
    }*/

    @Override
    public void passDataToMapFragment(String nextRoom, boolean isExistNextRoom) {
        isExistRoom = isExistNextRoom;
        roomName = nextRoom;
        String mapPath = "room"+nextRoom.toLowerCase();
        setMap(mapPath);
    }
}