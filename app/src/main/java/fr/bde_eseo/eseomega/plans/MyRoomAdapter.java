package fr.bde_eseo.eseomega.plans;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;

import fr.bde_eseo.eseomega.R;

/**
 * Created by Rascafr on 23/03/2016.
 */
public class MyRoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int TYPE_HEADER = 0;
    private int TYPE_ITEM = 1;

    private ArrayList<RoomItem> roomItems;
    private Context ctx;

    public ArrayList<RoomItem> getRoomItems() {
        return roomItems;
    }

    public void setRoomItems(ArrayList<RoomItem> roomItems) {
        this.roomItems = roomItems;
    }

    public MyRoomAdapter(Context ctx, ArrayList<RoomItem> roomItems) {
        this.roomItems = roomItems;
        this.ctx = ctx;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER)
            return new RoomHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_room_header, parent, false));
        else
            return new RoomItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_room, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return roomItems.get(position).isHeader() ? TYPE_HEADER : TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        RoomItem ri = roomItems.get(position);

        if (!ri.isHeader()) {
            RoomItemViewHolder rivh = (RoomItemViewHolder) holder;
            rivh.name.setText(ri.getName());
            rivh.details.setText(ri.getDetails());
        } else {
            RoomHeaderViewHolder rhvh = (RoomHeaderViewHolder) holder;
            rhvh.name.setText(ri.getName());
        }
    }

    @Override
    public int getItemCount() {
        return roomItems.size();
    }

    // Classic View Holder for Room item
    public class RoomItemViewHolder extends RecyclerView.ViewHolder {

        protected TextView name, details;

        public RoomItemViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.tvRoomName);
            details = (TextView) v.findViewById(R.id.tvRoomDesc);
        }
    }

    // Classic View Holder for Room header
    public class RoomHeaderViewHolder extends RecyclerView.ViewHolder {

        protected TextView name, details;

        public RoomHeaderViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.tvRoomHeader);
        }
    }
}