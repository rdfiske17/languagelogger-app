/*
 * Copyright (C) 2016 - 2018 ResearchIME Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.lmu.ifi.researchime.views.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import de.lmu.ifi.researchime.R;

/**
 * Implementation for expandable panel views with animated handle view - based on the android tutorial
 *
 */
public class ExpandablePanel extends LinearLayout {

    private final int mContentId;

    // Contains references to the content view
    private View mContent;

    private boolean mIsAnimationRunning = false;

    // the panel starts expanded
    private boolean mExpanded = true;
    // The height of the content when collapsed
    private int mCollapsedHeight = 0;
    // The full expanded height of the content (calculated)
    private int mContentHeight = 0;
    // How long the expand animation takes
    private int mAnimationDuration = 0;

    // Listener that gets fired onExpand and onCollapse
    private OnExpandListener mListener;

    public ExpandablePanel(Context context) {
        this(context, null);
    }

    /**
     * The constructor simply validates the arguments being passed in and
     * sets the global variables accordingly. Required attributes are
     * 'handle' and 'content'
     */
    public ExpandablePanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mListener = new DefaultOnExpandListener();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExpandablePanel, 0, 0);

        // How high the content should be in "collapsed" state
        mCollapsedHeight = (int) a.getDimension(R.styleable.ExpandablePanel_collapsedHeight, 0.0f);

        // How long the animation should take
        mAnimationDuration = a.getInteger(R.styleable.ExpandablePanel_animationDuration, 500);

        int contentId = a.getResourceId(R.styleable.ExpandablePanel_content, 0);
        mContentId = contentId;

        a.recycle();
    }

    // Some public setters for manipulating the
    // ExpandablePanel programmatically
    public void setOnExpandListener(OnExpandListener listener) {
        mListener = listener;
    }

    public void setCollapsedHeight(int collapsedHeight) {
        mCollapsedHeight = collapsedHeight;
    }

    public void setAnimationDuration(int animationDuration) {
        mAnimationDuration = animationDuration;
    }

    public void setStartExpanded(boolean startExpanded){
        mExpanded = startExpanded;
        mContent.getLayoutParams().height = startExpanded ? mContent.getHeight() : mCollapsedHeight;

        requestLayout();
        invalidate();
    }

    /**
     * This method gets called when the View is physically
     * visible to the user
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
/*
        if (mHandle == null) {
            throw new IllegalArgumentException(
                "The handle attribute is must refer to an"
                    + " existing child.");
        }
*/
        mContent = findViewById(mContentId);
        if (mContent == null) {
            throw new IllegalArgumentException(
                    "The content attribute must refer to an"
                            + " existing child.");
        }
    }

    /**
     * This is where the magic happens for measuring the actual
     * (un-expanded) height of the content. If the actual height
     * is less than the collapsedHeight, the handle will be hidden.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if(! isAnimationRunning()){
            mContent.getLayoutParams().height = mCollapsedHeight;
        }

        mContent.measure(widthMeasureSpec, MeasureSpec.UNSPECIFIED);
        mContentHeight = mContent.getMeasuredHeight();

        if(!isAnimationRunning()){
            mContent.getLayoutParams().height = mExpanded ? mContentHeight : mCollapsedHeight;
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * This is the on click listener for the handle.
     * It basically just creates a new animation instance and fires
     * animation.
     */
    private class PanelToggler implements OnClickListener {
        public void onClick(View v) {
            toggle();
        }
    }

    /**
     * Open/Close view
     */
    public void toggle(){
        if(mIsAnimationRunning){ //to prevent panel toggle spamming
            return;
        }

        final Animation a;
        if (mExpanded) {
            a = new ExpandAnimation(mContentHeight, mCollapsedHeight);
            mListener.onCollapse();
        } else {
            a = new ExpandAnimation(mCollapsedHeight, mContentHeight);
            mListener.onExpand();
        }
        a.setDuration(mAnimationDuration);
        a.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIsAnimationRunning = true;
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                mIsAnimationRunning = false;
            }
        });

        mContent.startAnimation(a);
        ((View) mContent.getParent()).invalidate();
        mExpanded = !mExpanded;
    }

    public void expand() {
        if(mExpanded){
            mListener.onExpand();
        }else{
            toggle();
        }
    }

    public void collapse(){
        if(mExpanded){
            toggle();
        }
    }

    public boolean isExpanded(){
        return mExpanded;
    }

    public int getCollapsibleDistance(){
        return mExpanded ? mCollapsedHeight : mContentHeight;
    }

    /**
     * This is a private animation class that handles the expand/collapse
     * animations. It uses the animationDuration attribute for the length
     * of time it takes.
     */
    private class ExpandAnimation extends Animation {
        private final int mStartHeight;
        private final int mDeltaHeight;

        public ExpandAnimation(int startHeight, int endHeight) {
            mStartHeight = startHeight;
            mDeltaHeight = endHeight - startHeight;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            android.view.ViewGroup.LayoutParams lp = mContent.getLayoutParams();
            lp.height = (int) (mStartHeight + mDeltaHeight * interpolatedTime);
            mContent.setLayoutParams(lp);
            mListener.onSlide(interpolatedTime);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }


    /**
     * Custom interface to be able to react on expand / collapse events
     */
    public interface OnExpandListener {
        void onExpand();
        void onCollapse();
        void onSlide(float offset);
    }

    private class DefaultOnExpandListener implements OnExpandListener {
        public void onCollapse() {}
        public void onExpand() {}
        public void onSlide(float offset) {}
    }

    public void registerHandleView(View handleView){
        handleView.setOnClickListener(new PanelToggler());
    }

    public boolean isAnimationRunning(){
        return mIsAnimationRunning;
    }
}