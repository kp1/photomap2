package net.mmho.photomap2;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class PhotoCardLayout extends RelativeLayout{

    private ThumbnailImageView thumbnail;
    private TextView title;
    private TextView description;
    private TextView count;

    private PhotoGroup group = null;

    public PhotoCardLayout(Context context) {
        this(context,null);
    }

    public PhotoCardLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PhotoCardLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        thumbnail = (ThumbnailImageView)findViewById(R.id.thumbnail);
        title = (TextView)findViewById(R.id.title);
        description = (TextView)findViewById(R.id.description);
        ImageView menu = (ImageView) findViewById(R.id.overflow);
        menu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(getContext(),v);
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()){
                case R.id.map:
                    moveMap();
                    return true;
                default:
                    return false;
                }
            });
            popup.inflate(R.menu.photo_list_popup_menu);
            popup.show();
        });
        count = (TextView)findViewById(R.id.count);
    }

    private void moveMap(){
        Intent i = new Intent(getContext(),PhotoMapActivity.class);
        i.putExtra(PhotoMapFragment.EXTRA_GROUP, (Parcelable) group);
        getContext().startActivity(i);
    }

    public void setData(PhotoGroup g){
        group = g;

        count.setText(String.format("%2d",g.size()));

        thumbnail.startLoading(g.get(0).getPhotoId());

        String address= g.getTitle();
        if(address==null) {
            title.setText(R.string.loading);
            description.setText(g.locationToString());
        }
        else{
            title.setText(address);
            description.setText(g.getDescription());
        }

    }

}
