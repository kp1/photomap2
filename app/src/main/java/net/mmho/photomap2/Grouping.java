package net.mmho.photomap2;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by kp on 14/07/09.
 */
public class Grouping extends ArrayList<PhotoGroup> {
    final PhotoCursor mCursor;

    Grouping(PhotoCursor c){
        mCursor = c;
    }

    void doGrouping(float distance){
        this.clear();
        if(!mCursor.moveToFirst()) return;

        do{
            int i;
            for(i=0;i<this.size();i++){
                LatLng p = mCursor.getLocation();
                LatLng c = this.get(i).getCenter();
                float[] d = new float[0];
                Location.distanceBetween(p.latitude,p.longitude,c.latitude,c.longitude,d);
                if(d[0]<distance){
                    this.get(i).append(p,mCursor.getID());
                    break;
                }
            }
            if(i==this.size()){
                PhotoGroup g = new PhotoGroup(mCursor.getLocation(), mCursor.getID());
                this.add(g);
            }
        }while(mCursor.moveToNext());
    }

}
