package cn.martinkay.autocheckinplugin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.List;

import cn.martinkay.autocheckinplugin.R;
import cn.martinkay.autocheckinplugin.entity.AutoConfig;

public class AutoConfigAdapter extends BaseAdapter {

    private Context context;
    private List<AutoConfig> autoConfigList;

    public AutoConfigAdapter(Context context, List<AutoConfig> autoConfigList) {
        this.context = context;
        this.autoConfigList = autoConfigList;
    }

    @Override
    public int getCount() {
        return autoConfigList.size();
    }

    @Override
    public Object getItem(int position) {
        return autoConfigList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (long) this.autoConfigList.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AutoConfig autoConfig = autoConfigList.get(position);
        View view = LayoutInflater.from(context).inflate(R.layout.rule_item, null);
        TextView name = view.findViewById(R.id.auto_config_name);
        TextView time = view.findViewById(R.id.auto_config_time);
        TextView week = view.findViewById(R.id.auto_config_week);
        TextView count = view.findViewById(R.id.auto_config_count);
        TextView next = view.findViewById(R.id.auto_config_next);

        name.setText(autoConfig.getName().toString());
        time.setText(autoConfig.getActiveTime().toString());
        week.setText(autoConfig.getActiveWeek().toString());
        count.setText(autoConfig.getActiveCount().toString());
        next.setText(autoConfig.getNextActiveCount().toString());
        return view;
    }
}
