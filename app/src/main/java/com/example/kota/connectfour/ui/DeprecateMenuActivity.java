package com.example.kota.connectfour.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.example.kota.connectfour.R;

public class DeprecateMenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deprecate_menu_activity);
        setClickListener();
        startAnimation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    private void setClickListener() {
        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                squashAndBoundInBottom(v, null);
            }
        });
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                squashAndBoundInBottom(v, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        Intent intent = new Intent(DeprecateMenuActivity.this, MenuActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                squashAndBoundInBottom(v, null);
            }
        });
    }

    private void startAnimation() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                final View v1 = findViewById(R.id.button1);
                final View v2 = findViewById(R.id.button2);
                final View v3 = findViewById(R.id.button3);

                PropertyValuesHolder phsx1 = PropertyValuesHolder.ofFloat(View.SCALE_X, 0,1);
                PropertyValuesHolder phsy1 = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0, 1);
                ObjectAnimator animator1 = ObjectAnimator.ofPropertyValuesHolder(
                        v1, phsx1, phsy1
                );
                animator1.setDuration(500);
                animator1.setInterpolator(new OvershootInterpolator());
                animator1.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        v1.setVisibility(View.VISIBLE);
                    }
                    @Override
                public void onAnimationEnd(Animator animator) {
                        super.onAnimationEnd(animator);
                        final Handler handler1 = new Handler();
                        final ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(
                                v1,
                                PropertyValuesHolder.ofFloat(View.SCALE_X, 1,1.2f),
                                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1, 1.2f)
                        );
                        anim.setDuration(500);
                        anim.setInterpolator(new OvershootInterpolator());
                        anim.setRepeatCount(1);
                        anim.setRepeatMode(ValueAnimator.REVERSE);
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                anim.start();
                                handler1.postDelayed(this, 15000);
                            }
                        };
                        handler1.postDelayed(runnable, 1000);
                    }
                });
                animator1.start();

                ObjectAnimator animator2 = ObjectAnimator.ofPropertyValuesHolder(
                        findViewById(R.id.button2), phsx1, phsy1
                );
                animator2.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        v2.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        super.onAnimationEnd(animator);
                        final Handler handler1 = new Handler();
                        final ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(
                                v2,
                                PropertyValuesHolder.ofFloat(View.SCALE_X, 1,1.2f),
                                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1, 1.2f)
                        );
                        anim.setDuration(500);
                        anim.setInterpolator(new OvershootInterpolator());
                        anim.setRepeatCount(1);
                        anim.setRepeatMode(ValueAnimator.REVERSE);
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                anim.start();
                                handler1.postDelayed(this, 15000);
                            }
                        };
                        handler1.postDelayed(runnable, 6000);

                    }
                });
                animator2.setDuration(500);
                animator2.setInterpolator(new OvershootInterpolator());
                animator2.start();

                ObjectAnimator animator3 = ObjectAnimator.ofPropertyValuesHolder(
                        findViewById(R.id.button3), phsx1, phsy1
                );
                animator3.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        v3.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        super.onAnimationEnd(animator);
                        super.onAnimationEnd(animator);
                        final Handler handler1 = new Handler();
                        final ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(
                                v3,
                                PropertyValuesHolder.ofFloat(View.SCALE_X, 1,1.2f),
                                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1, 1.2f)
                        );
                        anim.setDuration(500);
                        anim.setInterpolator(new OvershootInterpolator());
                        anim.setRepeatCount(1);
                        anim.setRepeatMode(ValueAnimator.REVERSE);
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                anim.start();
                                handler1.postDelayed(this, 15000);
                            }
                        };
                        handler1.postDelayed(runnable, 11000);
                    }
                });
                animator3.setDuration(500);
                animator3.setInterpolator(new OvershootInterpolator());
                animator3.start();
            }
        }, 700);
    }

    private void squashAndBoundInBottom(View target, AnimatorListenerAdapter listener) {
        long animationDuration = (long) (100);
        // Scale around bottom/middle to simplify squash against the window bottom
        target.setPivotX(target.getWidth() / 2);
        target.setPivotY(target.getHeight());

        // Animate the button down, accelerating, while also stretching in Y and squashing in X
        PropertyValuesHolder pvhTY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y,
                100);
        PropertyValuesHolder pvhSX = PropertyValuesHolder.ofFloat(View.SCALE_X, .7f);
        PropertyValuesHolder pvhSY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.2f);
        ObjectAnimator downAnim = ObjectAnimator.ofPropertyValuesHolder(
                target, pvhTY, pvhSX, pvhSY);
        downAnim.setInterpolator(new AccelerateInterpolator());
        downAnim.setDuration(animationDuration * 2);
        // Stretch in X, squash in Y, then reverse
        pvhSX = PropertyValuesHolder.ofFloat(View.SCALE_X, 2);
        pvhSY = PropertyValuesHolder.ofFloat(View.SCALE_Y, .5f);
        ObjectAnimator stretchAnim =
                ObjectAnimator.ofPropertyValuesHolder(target, pvhSX, pvhSY);
        stretchAnim.setRepeatCount(1);
        stretchAnim.setRepeatMode(ValueAnimator.REVERSE);
        stretchAnim.setInterpolator(new DecelerateInterpolator());
        stretchAnim.setDuration(animationDuration);

        // Animate back to the start
        pvhTY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0);
        pvhSX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1);
        pvhSY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1);
        ObjectAnimator upAnim =
                ObjectAnimator.ofPropertyValuesHolder(target, pvhTY, pvhSX, pvhSY);
        upAnim.setDuration((long) (animationDuration * 2));
        upAnim.setInterpolator(new DecelerateInterpolator());
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(downAnim, stretchAnim, upAnim);
        if(listener!=null) set.addListener(listener);
        set.start();
    }
}
