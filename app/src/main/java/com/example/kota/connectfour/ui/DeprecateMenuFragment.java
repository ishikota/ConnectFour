package com.example.kota.connectfour.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.kota.connectfour.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kota on 2015/01/10.
 * This Fragment represents single line list menu_list_single.
 */
public class DeprecateMenuFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v  = inflater.inflate(R.layout.menu_list_single, container, false);

        ArrayList<Bean> items = new ArrayList<Bean>();
        for(int i=0;i<10;i++) {
            Bean bean = new Bean(); bean.color=R.drawable.move_red; bean.text="Level "+(i+1); bean.arg1=i+1;
            items.add(bean);
        }

        ListView listView = (ListView)v.findViewById(R.id.list_view);
        MenuListAdapter adapter = new MenuListAdapter(getActivity(), items);
        listView.setAdapter(adapter);

        return v;
    }

    class Bean {
        int color;   // color drawable
        int arg1;    // max depth
        String text; // text shown on UI
    }

    /**
     * show list of menu_list_single.
     * when user clicked item, change the color of icon on the item
     * and un-color last-selected item.
     */
    private class MenuListAdapter extends ArrayAdapter<Bean> {
        private final LayoutInflater mInflater;
        private boolean is_first = true; // flg to check if selected item is default one.
        private int selected_pos = 0;
        private final View[] view_holder;

        MenuListAdapter(Context context, List<Bean> objects) {
            super(context, 0, objects);
            mInflater = LayoutInflater.from(context);
            view_holder = new View[objects.size()];

        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.menu_row, parent, false);
                holder = new ViewHolder();
                holder.icon = (ImageView)convertView.findViewById(R.id.icon);
                holder.text = (TextView)convertView.findViewById(R.id.text);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            view_holder[position] = convertView;
            Bean  bean = getItem(position);
            holder.text.setText(bean.text);
            if(position == selected_pos) {
                holder.icon.setImageResource(R.drawable.move_yellow);
                holder.text.setTextColor(getResources().getColor(R.color.text));
            } else {
                holder.icon.setImageResource(R.drawable.move_blank);
                holder.text.setTextColor(getResources().getColor(R.color.hint));
            }

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // default selected item handling.
                    if(!is_first && selected_pos == position) {
                        Intent intent = new Intent(getActivity(), VSCPUActivity.class);
                        intent.putExtra(VSCPUActivity.EXTRA_SECOND_PLAYER_STRENGTH, position+1);
                        startActivity(intent);
                        return;
                    }
                    is_first = false;

                    // update un-selected item color
                    View selected_view = view_holder[selected_pos];
                    ImageView icon = (ImageView)selected_view.findViewById(R.id.icon);
                    TextView text = (TextView)selected_view.findViewById(R.id.text);
                    View selected_label = selected_view.findViewById(R.id.label);
                    icon.setImageResource(R.drawable.move_blank);
                    text.setTextColor(getResources().getColor(R.color.hint));
                    // update selected item color
                    View focusing_view = view_holder[position];
                    View focusing_label = focusing_view.findViewById(R.id.label);
                    holder.icon.setImageResource(R.drawable.move_yellow);
                    holder.text.setTextColor(getResources().getColor(R.color.text));
                    labelAnim(selected_label, focusing_label);
                    selected_pos = position;
                }
            });


            return convertView;
        }

        /**
         * show selected item's label and hide un-selected item's label
         * with animation.
         * @param label_unselect : View of label which was selected until now.
         * @param label_select   : View of label which is selected now.
         */
        private void labelAnim(View label_unselect, final View label_select) {

            // deal with last-selected item
            ObjectAnimator outAnim = ObjectAnimator.ofFloat(
                    label_unselect,
                    View.TRANSLATION_X,
                    0,
                    500
            );
            outAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    //lavel_holder[selected_pos].setVisibility(View.GONE);
                }
            });
            outAnim.setDuration(300);
            outAnim.start();

            // deal with new-selected item
            ObjectAnimator inAnim = ObjectAnimator.ofFloat(
                    label_select,
                    View.TRANSLATION_X,
                    500,
                    0
            );
            inAnim.setDuration(300);
            inAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    label_select.setVisibility(View.VISIBLE);
                }
            });
            inAnim.start();
        }



        private class ViewHolder {
            ImageView icon;
            TextView text;
        }
    }

}
