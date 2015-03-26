package net.mmho.photomap2;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.LoaderManager;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;

public class PhotoListAdapter extends ArrayAdapter<PhotoGroup> {

    private int resource;
    private LayoutInflater inflater;
    private LoaderManager manager;
    private int loader_id;

    private AddressFilter filter;
    private ArrayList<PhotoGroup> mOriginalValues;
    private ArrayList<PhotoGroup> mObjects;

    private LruCache<Long,Bitmap> mBitmapCache;

    public PhotoListAdapter(Context context, int resource, ArrayList<PhotoGroup> objects,LoaderManager m,int loader_id_base,LruCache<Long,Bitmap> cache) {
        super(context, resource, objects);
        this.resource = resource;
        manager = m;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        loader_id = loader_id_base;
        mObjects = objects;
        mBitmapCache = cache;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        int id;
        if(convertView!=null){
            v = convertView;
            id = (Integer)v.getTag();
        }
        else {
            v = inflater.inflate(resource,null);
            id = loader_id++;
            v.setTag(id);
        }
        if(position < getCount()) {
            PhotoGroup g = getItem(position);
            ((PhotoCardLayout) v).setData(g, id, manager, mBitmapCache);
        }
        return v;
    }

    @Override
    public AddressFilter getFilter() {
        if(filter==null) filter = new AddressFilter();
        return filter;
    }

    public void clear(){
        super.clear();
        mOriginalValues = null;
    }

    private void clearData(){
        super.clear();
    }

    private class AddressFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults result = new FilterResults();
            if(mOriginalValues==null){
                mOriginalValues = new ArrayList<>(mObjects);
            }
            if(constraint==null || constraint.length()==0){
                result.count = mOriginalValues.size();
                result.values = mOriginalValues;
            }
            else{
                ArrayList<PhotoGroup> filtered = new ArrayList<>();
                for(PhotoGroup group:mOriginalValues){
                    if(group.getDescription().toLowerCase().contains(String.format("%s", constraint.toString().toLowerCase()))){
                        filtered.add(group);
                    }
                }
                result.count = filtered.size();
                result.values = filtered;
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if(mOriginalValues==null) return;
            notifyDataSetInvalidated();
            clearData();
            ArrayList<PhotoGroup> list = (ArrayList<PhotoGroup>)results.values;
            for(PhotoGroup g:list) add(g);
            notifyDataSetChanged();
        }
    }

}
