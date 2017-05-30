package com.android.launcher3.util;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.view.View;


public class MAnimationUtil {
	
	public static void fligerAnim(final View view,int topY,int bottomY){
		ObjectAnimator oa0 = ObjectAnimator.ofFloat(view, "y", topY);
		oa0.setDuration(900);
		ObjectAnimator oa2 = ObjectAnimator.ofFloat(view, "y", bottomY);
		oa2.setDuration(550);
		final AnimatorSet animSet = new AnimatorSet();
		animSet.play(oa0).after(300).before(oa2).after(200);
		animSet.addListener(new AnimatorListener() {
			public void onAnimationStart(Animator arg0) {
			}
			public void onAnimationRepeat(Animator arg0) {
			}
			public void onAnimationEnd(Animator arg0) {
				if(view != null)
					animSet.start();
			}
			public void onAnimationCancel(Animator arg0) {
			}
		});
		if(view != null)
			animSet.start();
	}
	
}
