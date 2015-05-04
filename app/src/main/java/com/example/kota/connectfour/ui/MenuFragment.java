package com.example.kota.connectfour.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kota.connectfour.R;

import java.util.ArrayList;

/**
 * Created by kota on 2015/01/10.
 * This Fragment represents menu screen
 * normal mode level range is 1 to 6.
 * hard mode level range is 9 to 14.(normal level + 8)
 */
public class MenuFragment extends Fragment {
    // 7th element is dummy element which would be never used.
    private static final int[] LEVEL_NAMES = {
            // normal mode
            R.string.easy, R.string.medium, R.string.hard,
            R.string.expert, R.string.pro, R.string.master,
            R.string.you, R.string.you, R.string.you,
            // hard mode
            R.string.hard1, R.string.hard2, R.string.hard3,
            R.string.hard4, R.string.hard5, R.string.hard6,
            R.string.you, R.string.you
    };
    private static final int[] LEVEL_ICONS = {
            // normal mode
            R.drawable.baby, R.drawable.boy, R.drawable.human,
            R.drawable.expert, R.drawable.wiseman, R.drawable.crown,
            R.drawable.you, R.drawable.you, 0,
            // hard mode
            R.drawable.spider, R.drawable.goast, R.drawable.monkey,
            R.drawable.threat, R.drawable.cookie, R.drawable.danger,
            R.drawable.you, R.drawable.you
    };
    private TextView text1, text2, vs_text, hint_text;
    private ImageView icon1, icon2, hint_indicator;
    private View btn_parent, root_bg;
    private Button btn_chmod, btn_cancel;

    // state : 0->select first player level, 1 -> select second player level, 2-> start game
    private int state = 0;
    // mode=0 -> normal, mode=1 -> hard
    private int mode = 0;

    // if so, show tutorial in each step
    private static final String EXTRA_IS_NEWONE = "is_newone";
    private boolean is_newone = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.menu_cpu_vs_cpu, container, false);

        text1 = (TextView)v.findViewById(R.id.text1);
        text2 = (TextView)v.findViewById(R.id.text2);
        icon1 = (ImageView)v.findViewById(R.id.icon1);
        icon2 = (ImageView)v.findViewById(R.id.icon2);
        vs_text = (TextView)v.findViewById(R.id.vs_text);
        hint_text = (TextView)v.findViewById(R.id.hint_text);
        hint_indicator = (ImageView)v.findViewById(R.id.hint_indicator);
        btn_parent = v.findViewById(R.id.button_parent);
        root_bg = v.findViewById(R.id.root_view);
        btn_chmod = (Button)v.findViewById(R.id.button7);
        btn_cancel = (Button)v.findViewById(R.id.button9);

        final Button[] buttons = {
                (Button) v.findViewById(R.id.button1),
                (Button) v.findViewById(R.id.button2),
                (Button) v.findViewById(R.id.button3),
                (Button) v.findViewById(R.id.button4),
                (Button) v.findViewById(R.id.button5),
                (Button) v.findViewById(R.id.button6),
                (Button) v.findViewById(R.id.button8), // dummy element
                (Button) v.findViewById(R.id.button8),
        };
        for (int i=0;i<buttons.length;i++) {
            buttons[i].setTag(i);
            buttons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(state==0) {
                        forwardToState1(v);
                    } else if(state==1) {
                        forwardToState2(v);
                    }
                }
            });
        }
        v.findViewById(R.id.button7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {changeMode(buttons, v);
            }
        });
        v.findViewById(R.id.button9).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backState();
            }
        });
        text1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backState();
            }
        });
        text2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backState();
            }
        });

        setHint();

        // check if new one
        boolean has_demo_finished = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getBoolean(VSCPUActivity.EXTRA_HAS_DEMO_DONE, false);
        if(!has_demo_finished) {
            // start demo
            Intent intent = new Intent(getActivity(), VSCPUActivity.class);
            intent.putExtra(VSCPUActivity.EXTRA_FIRST_PLAYER_NAME, getResources().getString(R.string.you));
            intent.putExtra(VSCPUActivity.EXTRA_SECOND_PLAYER_NAME, getResources().getString(R.string.computer));
            intent.putExtra(VSCPUActivity.EXTRA_FIRST_PLAYER_STRENGTH, 8);
            intent.putExtra(VSCPUActivity.EXTRA_SECOND_PLAYER_STRENGTH, 1);
            startActivityForResult(intent, 456);
        } else {
            is_newone = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getBoolean(EXTRA_IS_NEWONE, true);
            if (is_newone) {
                // if user haven't explored menu tutorial yet, start it.
                showDialogForNewOne();
            }
        }

        return v;
    }

    private void showDialogForNewOne() {
        MaterialAlertDialog.newInstance(
                getResources().getString(R.string.tutorial_message),
                getResources().getString(R.string.watch),
                getResources().getString(R.string.skip),
                new MaterialAlertDialog.onButtonClickListener() {
                    @Override
                    public void onClicked() {
                        TutorialDialogFragment.newInstance(TutorialDialogFragment.TUTORIAL_FIRST)
                                .show(getFragmentManager(), "tutorial");
                    }
                },
                new MaterialAlertDialog.onButtonClickListener() {
                    @Override
                    public void onClicked() {
                        SharedPreferences.Editor editor =
                                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                        editor.putBoolean(EXTRA_IS_NEWONE, false);
                        editor.apply();
                        // change flg for when user back to the menu.
                        is_newone = false;
                    }
                }
        ).show(getFragmentManager(), "tutorial");
    }

    /**
     * Called when button clicked in state=0. start sequential animation.
     * @param button : ButtonView which is clicked.
     */
    private void forwardToState1(View button) {
        state=1;
        int i = (Integer)button.getTag();
        text1.setTag(i+((mode%2)*8));
        Animator anim1 = createButtonDisappearAnim(button);
        final Animator anim2 = createPlayerSelectAnim(
                true, text1, getResources().getString(LEVEL_NAMES[(mode%2)*9+i]));
        final Animator anim3 = createButtonAppearAnim(button);
        anim1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                anim2.start();
            }
        });
        anim2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                anim3.start();
            }
        });
        anim3.setStartDelay(200);

        // show cancel button
        btn_cancel.setVisibility(View.VISIBLE);
        createButtonAppearAnim(btn_cancel).start();

        // if new one then show tutorial
        if(is_newone) {
            anim3.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    TutorialDialogFragment.newInstance(TutorialDialogFragment.TUTORIAL_SECOND)
                            .show(getFragmentManager(), "tutorial");
                }
            });
        }
        anim1.start();
        setHint();
    }

    /**
     * Called when button clicked in state=1. start sequential animation.
     * @param button : ButtonView which is clicked.
     */
    private void forwardToState2(View button) {
        state=2;
        int i = (Integer)button.getTag();
        text2.setTag(i+((mode%2)*8));
        Animator anim1 = createButtonDisappearAnim(button);
        final Animator anim2 = createPlayerSelectAnim(
                true, text2, getResources().getString(LEVEL_NAMES[(mode%2)*9+i]));
        final Animator anim3 = createButtonAppearAnim(button);
        final Animator anim4 = createVSTextAnim(true, vs_text);
        anim1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                anim2.start();
            }
        });
        anim2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                anim3.start();
            }
        });
        //anim3.setStartDelay(200); anim4.setStartDelay(200);
        anim3.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                anim4.start();
            }
        });

        // disappear change mode button
        createButtonDisappearAnim(btn_chmod).start();

        // if new one then show tutorial
        if(is_newone) {
            anim4.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    TutorialDialogFragment.newInstance(TutorialDialogFragment.TUTORIAL_THIRD)
                            .show(getFragmentManager(), "tutorial");
                }
            });
        }
        anim1.start();
        setHint();
    }

    private void backState() {
        if(state==1) {
            Animator anim = createPlayerSelectAnim(false, text1, "PLAYER1");
            anim.start();
            createButtonDisappearAnim(btn_cancel).start();
        } else if(state==2) {
            AnimatorSet anim = new AnimatorSet();
            Animator anim1 = createPlayerSelectAnim(false, text2, "PLAYER2");
            Animator anim2 = createVSTextAnim(false, vs_text);
            anim.playSequentially(anim1, anim2);
            anim.start();
            createButtonAppearAnim(btn_chmod).start();
        } else {
            return;
        }
        state--;
        setHint();
    }

    private void changeMode(Button[] buttons, final View change_btn) {
        // if both of player are already selected ,then do not need to change mode.
        if(state==2) return;

        mode++;
        final Resources r = getResources();
        ArrayList<Animator> list = new ArrayList<Animator>();
        int i = (mode%2)*9; //
        for (final Button button : buttons) {
            final String level_name = getResources().getString(LEVEL_NAMES[i]);
            final Drawable level_icon = r.getDrawable(LEVEL_ICONS[i++]);
            final Animator appearAnim = createButtonAppearAnim(button);
            //appearAnim.setStartDelay(300);
            Animator disappearAnim = createButtonDisappearAnim(button);
            disappearAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    button.setText(level_name);
                    button.setCompoundDrawablesWithIntrinsicBounds(null, level_icon, null, null);
                    appearAnim.start();
                }
            });
            list.add(disappearAnim);
        }
        Animator disappear_anim = createButtonDisappearAnim(change_btn);
        final Animator appear_anim = createButtonAppearAnim(change_btn);
        disappear_anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                Resources r = getResources();
                Drawable icon = mode%2==0 ? r.getDrawable(R.drawable.ic_action_warning) : r.getDrawable(R.drawable.ic_action_reply);
                String text = mode%2==0 ? "Hard Mode" : "Normal Mode";
                ((Button) change_btn).setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                ((Button)change_btn).setText(text);
                appear_anim.start();
            }
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(list);
        animatorSet.start();
        disappear_anim.start();

        // condition dependent term
        // do not use color resource because it doesn't bring proper color here.(may be system bug).
        int p1_text_color = mode%2==0 ?
                state > 0 ? Color.parseColor("#DE000000") : Color.parseColor("#42000000") :          // light theme
                state > 0 ? Color.parseColor("#FFFFFF") : Color.parseColor("#55FFFFFF") ;
        int p2_text_color = mode%2==0 ?
                state > 1 ? Color.parseColor("#DE000000") : Color.parseColor("#42000000") :          // light theme
                state > 1 ? Color.parseColor("#FFFFFF") : Color.parseColor("#55FFFFFF") ;
        int p1_icon_img = mode%2==0 ?
                state > 0 ? R.drawable.move_red : R.drawable.move_blank :  // light theme
                state > 0 ? R.drawable.move_test2 : R.drawable.move_blank; // dart theme
        int p2_icon_img = mode%2==0 ?
                state > 1 ? R.drawable.move_yellow : R.drawable.move_blank : // light theme
                state > 1 ? R.drawable.move_test1 : R.drawable.move_blank;     // dark theme
        int actionbar_color = mode%2==0 ? Color.parseColor("#00BCD4") : Color.parseColor("#212121");
        int bg_color   = mode%2==0 ? Color.parseColor("#FFFFFF") : Color.parseColor("#DD212121");
        int btn_color  = mode%2==0 ? Color.parseColor("#263238") : Color.parseColor("#F9212121");
        int hint_color = mode%2==0 ? Color.parseColor("#42000000") : Color.parseColor("#55FFFFFF");

        // change color
        getActivity().getActionBar().setBackgroundDrawable(new ColorDrawable(actionbar_color));
        root_bg.setBackgroundColor(bg_color);
        btn_parent.setBackgroundColor(btn_color);
        text1.setTextColor(p1_text_color);
        text2.setTextColor(p2_text_color);
        icon1.setImageResource(p1_icon_img);
        icon2.setImageResource(p2_icon_img);
        hint_text.setTextColor(hint_color);
    }

    /**
     * this method is called after state variable changed.
     */
    private void setHint() {
        int move_range = (int)getResources().getDimension(R.dimen.hint_indicator_range);
        int from = state==2 ? -move_range : 0;
        int to = state==2 ? -move_range-20 : -20;
        // init animate indicator
        final ObjectAnimator animator = ObjectAnimator.ofFloat(hint_indicator, View.TRANSLATION_Y, from, to);
        animator.setDuration(500).setRepeatCount(-1);
        animator.setRepeatMode(ValueAnimator.REVERSE);

        if(state == 0) {
            hint_text.setText("Choose Player 1");
            animator.start();
        } else if(state == 1) {
            // if flg is true, this means before state was 2. So we should down hint indicator.
            boolean flg = hint_text.getText().toString().equals("Start Game !!");
            hint_indicator.setImageResource(R.drawable.hint_indicator_down);
            hint_text.setText("Choose Player 2");
            if(flg) {
                Animator downAnim = createHintIndicatorAnim(2, hint_indicator);
                downAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        animator.start();
                    }
                });
                downAnim.start();
            }
        } else if(state == 2) {
            hint_text.setText("Start Game !!");
            hint_indicator.setImageResource(R.drawable.hint_indicator_up);
            Animator upAnim = createHintIndicatorAnim(1, hint_indicator);
            upAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    animator.start();
                }
            });
            upAnim.start();
        }
    }

    // below codes are animation related methods

    /**
     * move hint indicator to upper place or back to original position.
     * @param mode   : 1 -> lift indicator animation, 2 -> down indicator animation
     * @param target : the view to animate
     */
    private Animator createHintIndicatorAnim(int mode, View target) {
        int move_range = (int)getResources().getDimension(R.dimen.hint_indicator_range);
        int from = mode==1 ? 0 : -move_range;
        int to = mode==1 ? -move_range : 0;
        ObjectAnimator animator =
                ObjectAnimator
                        .ofFloat(target, View.TRANSLATION_Y, from, to)
                        .setDuration(500);
        if(mode==1) animator.setStartDelay(300);
        return animator;
    }

    private Animator createButtonDisappearAnim(View target) {
        ObjectAnimator shrink_x = ObjectAnimator.ofFloat(target, View.SCALE_X, 1.0f, 0);
        ObjectAnimator shrink_y = ObjectAnimator.ofFloat(target, View.SCALE_Y, 1.0f, 0);
        shrink_x.setDuration(300); shrink_y.setDuration(300);
        AnticipateInterpolator i = new AnticipateInterpolator();
        shrink_x.setInterpolator(i);shrink_y.setInterpolator(i);
        AnimatorSet anim = new AnimatorSet();
        anim.play(shrink_x).with(shrink_y);
        return anim;
    }

    private Animator createButtonAppearAnim(View target) {
        ObjectAnimator scale_x = ObjectAnimator.ofFloat(target, View.SCALE_X, 0f, 1);
        ObjectAnimator scale_y = ObjectAnimator.ofFloat(target, View.SCALE_Y, 0f, 1);
        scale_x.setDuration(300); scale_y.setDuration(300);
        OvershootInterpolator i = new OvershootInterpolator();
        scale_x.setInterpolator(i);scale_y.setInterpolator(i);
        AnimatorSet anim = new AnimatorSet();
        anim.play(scale_x).with(scale_y);
        return anim;
    }

    /**
     * Create  animation which set player text or un-select.
     * @param state  : if true then create selected animation else un-selected animation
     * @param target : TextView of player text to animate
     * @param text  : player text to set
     * @return       : return proper animation.
     */
    private Animator createPlayerSelectAnim(boolean state, final TextView target, final String text) {

        // disappear animation
        ObjectAnimator shrink_x = ObjectAnimator.ofFloat(target, View.SCALE_X, 1.0f, 0);
        ObjectAnimator shrink_y = ObjectAnimator.ofFloat(target, View.SCALE_Y, 1.0f, 0);
        shrink_x.setDuration(300); shrink_y.setDuration(300);
        AnticipateInterpolator i = new AnticipateInterpolator();
        shrink_x.setInterpolator(i);shrink_y.setInterpolator(i);
        AnimatorSet shrinkAnim = new AnimatorSet();
        shrinkAnim.play(shrink_x).with(shrink_y);

        // appear animation
        ObjectAnimator scale_x = ObjectAnimator.ofFloat(target, View.SCALE_X, 0f, 1);
        ObjectAnimator scale_y = ObjectAnimator.ofFloat(target, View.SCALE_Y, 0f, 1);
        scale_x.setDuration(300); scale_y.setDuration(300);
        OvershootInterpolator i2 = new OvershootInterpolator();
        scale_x.setInterpolator(i2);scale_y.setInterpolator(i2);
        final AnimatorSet expandAnim = new AnimatorSet();
        expandAnim.play(scale_x).with(scale_y);

        // condition dependent term
        final ImageView icon = target==text1 ? icon1 : icon2 ;
        final int icon_image = mode%2==0 ?
                // light theme
                state ? target==text1 ? R.drawable.move_red : R.drawable.move_yellow
                        : R.drawable.move_blank :
                // dark theme
                state ? target==text1 ? R.drawable.move_test2 : R.drawable.move_test1
                        : R.drawable.move_blank ;
        final int text_color = mode%2==0 ?
                state ? R.color.text : R.color.hint : // light theme
                state ? R.color.dark_text : R.color.dark_hint ; // dark theme

        // set listener
        shrinkAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                expandAnim.start();
            }
        });
        expandAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                target.setText(text);
                target.setTextColor(getResources().getColor(text_color));
                icon.setImageResource(icon_image);
            }
        });

        return shrinkAnim;
    }

    /**
     * Animate vs text
     * @param state  : if true then create selected animation else un-selected animation
     * @param target : vs TextView to animate
     */
    private Animator createVSTextAnim(final boolean state, View target) {
        // shrink animation
        ObjectAnimator anim1 = ObjectAnimator.ofPropertyValuesHolder(target,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 0.1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 0.1f),
                PropertyValuesHolder.ofFloat(View.ROTATION, 0f, -360f)
        );
        anim1.setDuration(300);

        // expand animation
        final ObjectAnimator anim2 = ObjectAnimator.ofPropertyValuesHolder(target,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0.1f, 1.0f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.1f, 1.0f),
                PropertyValuesHolder.ofFloat(View.ROTATION, 0f, 360f)
        );
        anim2.setDuration(300);
        anim2.setInterpolator(new OvershootInterpolator());

        /* add listener to change background during animation transition */
        anim1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                int bg = mode %2 == 0 ?
                        state ? R.drawable.move_cyan : R.drawable.move_blank :
                        state ? R.drawable.move_test3 : R.drawable.move_blank ;
                int text_color = mode %2 == 0 ?
                        Color.WHITE :
                        state ? Color.parseColor("#DD212121") : Color.WHITE ;
                vs_text.setBackgroundResource(bg);
                vs_text.setTextColor(text_color);
                anim2.start();
                vs_text.setOnClickListener(state ? new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // button animation
                        PropertyValuesHolder pvhsx = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f,1.2f);
                        PropertyValuesHolder pvhsy = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f,1.2f);
                        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(v, pvhsx, pvhsy);
                        anim.setDuration(300);
                        anim.setRepeatCount(1);
                        anim.setRepeatMode(ValueAnimator.REVERSE);
                        anim.start();
                        doTransitionAnim();
                    }
                } : null);
            }
        });

        return anim1;
    }

    private void doTransitionAnim() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(btn_parent, View.TRANSLATION_Y, 0f, 1000f);
        anim.setDuration(300);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // user has finished tutorial, so remember it.
                if(is_newone) {
                    SharedPreferences.Editor editor =
                            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                    editor.putBoolean(EXTRA_IS_NEWONE, false);
                    editor.apply();
                    // change flg for when user back to the menu.
                    is_newone = false;
                }

                // start VS Activity
                Intent intent = new Intent(getActivity(), VSCPUActivity.class);
                intent.putExtra(VSCPUActivity.EXTRA_FIRST_PLAYER_NAME, text1.getText().toString());
                intent.putExtra(VSCPUActivity.EXTRA_SECOND_PLAYER_NAME, text2.getText().toString());
                intent.putExtra(VSCPUActivity.EXTRA_FIRST_PLAYER_STRENGTH, (Integer) text1.getTag() + 1);
                intent.putExtra(VSCPUActivity.EXTRA_SECOND_PLAYER_STRENGTH, (Integer) text2.getTag() + 1);
                startActivityForResult(intent, 123);
            }
        });
        anim.start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 123:
                ObjectAnimator anim = ObjectAnimator.ofFloat(btn_parent, View.TRANSLATION_Y, 1000f, 0f);
                anim.setDuration(300);
                anim.setStartDelay(300);
                anim.start();
                // show tutorial dialog of Training mode
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                boolean timing = pref.getBoolean(TrainingActivity.FLG_TRAINING_TIMING, false);
                boolean has_played = pref.getBoolean(TrainingActivity.FLG_HAS_PLAYED_TRAINING, false);
                if(timing && !has_played) recommendTrainingMode();
                break;
            case 456:
                // back from demonstration
                is_newone = PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getBoolean(EXTRA_IS_NEWONE, true);
                if (is_newone) {
                    // if user haven't explored menu tutorial yet, start it.
                    showDialogForNewOne();
                }
                break;
        }
    }

    private void recommendTrainingMode() {
        // change flg not to display this dialog again.
        SharedPreferences.Editor editor =
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putBoolean(TrainingActivity.FLG_TRAINING_TIMING, false);
        editor.apply();

        MaterialAlertDialog.newInstance(
                getResources().getString(R.string.training_recommend),
                getResources().getString(R.string.ok),
                "",
                new MaterialAlertDialog.onButtonClickListener() {
                    @Override
                    public void onClicked() {
                        TutorialDialogFragment.newInstance(TutorialDialogFragment.TUTORIAL_FORTH)
                                .show(getFragmentManager(), "tutorial");
                    }
                },
                new MaterialAlertDialog.onButtonClickListener() {
                    @Override
                    public void onClicked() {

                    }
                }
        ).show(getFragmentManager(), "tutorial");
    }
}