package org.ezsplace.horsefallalert;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class EventsAdapter extends BaseAdapter {
    private List<AlertLogItem> data = new ArrayList<AlertLogItem>();
    private Context context;

    public EventsAdapter(Context context) {
        setContext(context);
        reload();
    }

    private void add(List<AlertLogItem> allRows) {
        data.addAll(allRows);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return ((AlertLogItem) getItem(position)).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AlertLogItem item = (AlertLogItem) getItem(position);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.event_item_view, parent, false);
        TextView datetime = (TextView) rowView.findViewById(R.id.event_datetime);
        TextView text = (TextView) rowView.findViewById(R.id.event_text);
        datetime.setText(String.valueOf(item.getEntryDatetime()));
        text.setText(item.getText());

        return rowView;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void reload() {
        data.clear();
        add(AlertLogManager.getAllRows(context));
    }
}
