package jp.co.kayo.android.droiddancermotionwriter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MotionListAdapter extends ArrayAdapter<MotionItem>{
    LayoutInflater inflater;
    public MotionListAdapter(Context context) {
        super(context, 0);
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    public void setData(List<MotionItem> items){
        clear();
        if(items!=null){
            addAll(items);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.textView1 = (TextView)convertView.findViewById(R.id.textView1);
            holder.textView2 = (TextView)convertView.findViewById(R.id.textView2);
            holder.textView3 = (TextView)convertView.findViewById(R.id.textView3);
            holder.textView4 = (TextView)convertView.findViewById(R.id.textView4);
            holder.textView5 = (TextView)convertView.findViewById(R.id.textView5);
            holder.textView6 = (TextView)convertView.findViewById(R.id.textView6);
            
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }
        MotionItem item = getItem(position);
        
        holder.textView1.setText(Boolean.toString(item.isLed()));
        holder.textView2.setText(Byte.toString(item.getArmleft()));
        holder.textView3.setText(Byte.toString(item.getArmright()));
        holder.textView4.setText(Byte.toString(item.getRotleft()));
        holder.textView5.setText(Byte.toString(item.getRotright()));
        holder.textView6.setText(Byte.toString(item.getTime()));
        
        return convertView;
    }
    

    private class ViewHolder{
        TextView textView1;
        TextView textView2;
        TextView textView3;
        TextView textView4;
        TextView textView5;
        TextView textView6;
    }

}
