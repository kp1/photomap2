package net.mmho.photomap2;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;

public class PhotoGroup extends ArrayList<PhotoGroup.Group> {
    final PhotoCursor mCursor;

    PhotoGroup(PhotoCursor c){
        mCursor = c;
    }

    public class Group extends ArrayList<Long> {
        private LatLngBounds area;
        Group(LatLng p,long id){
            LatLngBounds.Builder b = new LatLngBounds.Builder();
            b.include(p);
            area = b.build();
            this.add(id);
        }

        LatLng getCenter(){
            return area.getCenter();
        }

        void append(LatLng point,long id){
            area = area.including(point);
            this.add(id);
        }
    }

    void exec(float distance){
        this.clear();
        if(!mCursor.moveToFirst()) return;

        do{
            int i;
            for(i=0;i<this.size();i++){
                LatLng p = mCursor.getLocation();
                LatLng c = this.get(i).getCenter();
                float[] d = new float[3];
                Location.distanceBetween(p.latitude,p.longitude,c.latitude,c.longitude,d);
                if(d[0]<distance){
                    this.get(i).append(p,mCursor.getID());
                    break;
                }
            }
            if(i==this.size()){
                Group g = new Group(mCursor.getLocation(), mCursor.getID());
                this.add(g);
            }
        }while(mCursor.moveToNext());
    }



}