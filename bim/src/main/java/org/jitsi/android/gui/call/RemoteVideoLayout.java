/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui.call;

import android.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import net.java.sip.communicator.util.*;

import org.jitsi.android.util.java.awt.Dimension;

/**
 * Layout that aligns remote video <tt>View</tt> by stretching it to screen
 * width or height. It also controls whether call control buttons group should
 * be auto hidden or stay visible all the time. This layout will work only with
 * <tt>VideoCallActivity</tt>.<br/>
 * IMPORTANT: it can't be done from <tt>Activity</tt>, because just after
 * the views are created we don't know their sizes yet(return 0 or invalid).
 *
 * @author Pawel Domas
 */
public class RemoteVideoLayout extends LinearLayout {
    /**
     * The logger
     */
    private final static Logger logger = Logger.getLogger(RemoteVideoLayout.class);

    /**
     * Preferred video size used to calculate the ratio.
     */
    private Dimension preferredSize = new Dimension(1, 1);

    /**
     * Stores last preferred size.
     */
    private boolean preferredSizeChanged = false;

    /**
     * Stores last child count.
     */
    private int lastChildCount = -1;

    public RemoteVideoLayout(Context context) {
        super(context);
    }

    public RemoteVideoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RemoteVideoLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setVideoPreferredSize(Dimension dimension) {
        this.preferredSize = dimension;

        preferredSizeChanged = true;

        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childCount = getChildCount();
        if (childCount == lastChildCount && !preferredSizeChanged) {
            // Do nothing
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        // Store values to prevent from too many calculations
        lastChildCount = childCount;
        preferredSizeChanged = false;

        Context ctx = getContext(); if (!(ctx instanceof VideoCallActivity)) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        VideoCallActivity videoActivity = (VideoCallActivity) ctx;

        if (childCount > 0) {
            int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
            int parentHeight = MeasureSpec.getSize(heightMeasureSpec);

            double width = preferredSize.width;
            double height = preferredSize.height;

            // Stretch to match height
            if (parentHeight <= parentWidth) {
                logger.info("Stretch to height");
                double ratio = width / height;
                height = parentHeight; width = height * ratio;

                videoActivity.ensureAutoHideFragmentAttached();
            }
            // Stretch to match width
            else {
                logger.info("Stretch to width");
                double ratio = height / width; width = parentWidth;
                height = width * ratio;

                videoActivity.ensureAutoHideFragmentDetached();
            }

            logger.info("Remote video view width: " + width + ", height: " + height);

            this.setMeasuredDimension((int) width, (int) height);

            ViewGroup.LayoutParams params = getLayoutParams();
            params.width = (int) width;
            params.height = (int) height;
            this.setLayoutParams(params);

            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                ViewGroup.LayoutParams chP = child.getLayoutParams();
                chP.width = params.width;
                chP.height = params.height;
                child.setLayoutParams(chP);
            }
        }
        else {
            ViewGroup.LayoutParams params = getLayoutParams();
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            this.setLayoutParams(params);

            videoActivity.ensureAutoHideFragmentDetached();
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}