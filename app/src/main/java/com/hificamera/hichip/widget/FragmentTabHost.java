package com.hificamera.hichip.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;

import java.util.ArrayList;

public class FragmentTabHost extends TabHost implements TabHost.OnTabChangeListener {
    private final ArrayList<FragmentTabHost.TabInfo> mTabs = new ArrayList<>();

    private FrameLayout mRealTabContent;
    private Context mContext;
    private FragmentManager mFragmentManager;
    private int mContainerId;
    private TabHost.OnTabChangeListener mOnTabChangeListener;
    private FragmentTabHost.TabInfo mLastTab;
    private boolean mAttached;
    private boolean isRestored;

    static final class TabInfo {
        final @NonNull String tag;
        final @NonNull Class<?> clss;
        final @Nullable Bundle args;
        Fragment fragment;

        TabInfo(@NonNull String _tag, @NonNull Class<?> _class, @Nullable Bundle _args) {
            tag = _tag;
            clss = _class;
            args = _args;
        }
    }

    static class DummyTabFactory implements TabHost.TabContentFactory {
        private final Context mContext;

        public DummyTabFactory(Context context) {
            mContext = context;
        }

        @Override
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
    }

    static class SavedState extends BaseSavedState {
        String curTab;

        SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState(Parcel in) {
            super(in);
            curTab = in.readString();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(curTab);
        }

        @Override
        public String toString() {
            return "FragmentTabHost.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " curTab=" + curTab + "}";
        }

        public static final Parcelable.Creator<FragmentTabHost.SavedState> CREATOR = new Parcelable.Creator<FragmentTabHost.SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new FragmentTabHost.SavedState(in);
            }

            @Override
            public FragmentTabHost.SavedState[] newArray(int size) {
                return new FragmentTabHost.SavedState[size];
            }
        };
    }

    public FragmentTabHost(Context context) {
        // Note that we call through to the version that takes an AttributeSet,
        // because the simple Context construct can result in a broken object!
        super(context, null);
        initFragmentTabHost(context, null);
    }

    public FragmentTabHost(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFragmentTabHost(context, attrs);
    }

    private void initFragmentTabHost(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.inflatedId}, 0, 0);
        mContainerId = a.getResourceId(0, 0);
        a.recycle();

        super.setOnTabChangedListener(this);
    }

    private void ensureHierarchy(Context context) {
        // If owner hasn't made its own view hierarchy, then as a convenience
        // we will construct a standard one here.
        if (findViewById(android.R.id.tabs) == null) {
            LinearLayout ll = new LinearLayout(context);
            ll.setOrientation(LinearLayout.VERTICAL);
            addView(ll, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            TabWidget tw = new TabWidget(context);
            tw.setId(android.R.id.tabs);
            tw.setOrientation(TabWidget.HORIZONTAL);
            ll.addView(tw, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0));

            FrameLayout fl = new FrameLayout(context);
            fl.setId(android.R.id.tabcontent);
            ll.addView(fl, new LinearLayout.LayoutParams(0, 0, 0));

            mRealTabContent = fl = new FrameLayout(context);
            mRealTabContent.setId(mContainerId);
            ll.addView(fl, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        }
    }

    @Override
    @Deprecated
    public void setup() {
        throw new IllegalStateException("Must call setup() that takes a Context and FragmentManager");
    }

    public void setup(Context context, FragmentManager manager) {
        ensureHierarchy(context);  // Ensure views required by super.setup()
        super.setup();
        mContext = context;
        mFragmentManager = manager;
        ensureContent();
    }

    public void setup(Context context, FragmentManager manager, int containerId) {
        ensureHierarchy(context);  // Ensure views required by super.setup()
        super.setup();
        mContext = context;
        mFragmentManager = manager;
        mContainerId = containerId;
        ensureContent();
        mRealTabContent.setId(containerId);

        // We must have an ID to be able to save/restore our state.  If
        // the owner hasn't set one at this point, we will set it ourselves.
        if (getId() == View.NO_ID) {
            setId(android.R.id.tabhost);
        }
    }

    private void ensureContent() {
        if (mRealTabContent == null) {
            mRealTabContent = (FrameLayout) findViewById(mContainerId);
            if (mRealTabContent == null) {
                throw new IllegalStateException("No tab content FrameLayout found for id " + mContainerId);
            }
        }
    }

    @Override
    public void setOnTabChangedListener(OnTabChangeListener l) {
        mOnTabChangeListener = l;
    }

    public void addTab(@NonNull TabHost.TabSpec tabSpec, @NonNull Class<?> clss, @Nullable Bundle args) {
        tabSpec.setContent(new FragmentTabHost.DummyTabFactory(mContext));

        final String tag = tabSpec.getTag();
        final FragmentTabHost.TabInfo info = new FragmentTabHost.TabInfo(tag, clss, args);

        if (mAttached) {
            // If we are already attached to the window, then check to make
            // sure this tab's fragment is inactive if it exists.  This shouldn't
            // normally happen.
            info.fragment = mFragmentManager.findFragmentByTag(tag);
            if (info.fragment != null && !info.fragment.isDetached()) {
                final FragmentTransaction ft = mFragmentManager.beginTransaction();
                ft.detach(info.fragment);
                ft.commitAllowingStateLoss();//ft.commit();
            }
        }

        mTabs.add(info);
        addTab(tabSpec);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        final String currentTag = getCurrentTabTag();

        // Go through all tabs and make sure their fragments match
        // the correct state.
        FragmentTransaction ft = null;
        for (int i = 0, count = mTabs.size(); i < count; i++) {
            final FragmentTabHost.TabInfo tab = mTabs.get(i);
            tab.fragment = mFragmentManager.findFragmentByTag(tab.tag);
            if (tab.fragment != null && !tab.fragment.isDetached()) {
                if (tab.tag.equals(currentTag)) {
                    // The fragment for this tab is already there and
                    // active, and it is what we really want to have
                    // as the current tab.  Nothing to do.
                    mLastTab = tab;
                } else {
                    // This fragment was restored in the active state,
                    // but is not the current tab.  Deactivate it.
                    if (ft == null) {
                        ft = mFragmentManager.beginTransaction();
                    }
                    ft.detach(tab.fragment);
                }
            }
        }

        // We are now ready to go.  Make sure we are switched to the
        // correct tab.
        mAttached = true;
        ft = doTabChanged(currentTag, ft);
        if (ft != null) {
            ft.commitAllowingStateLoss();//ft.commit()
            mFragmentManager.executePendingTransactions();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttached = false;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        FragmentTabHost.SavedState ss = new FragmentTabHost.SavedState(superState);
        ss.curTab = getCurrentTabTag();
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof FragmentTabHost.SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        FragmentTabHost.SavedState ss = (FragmentTabHost.SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCurrentTabByTag(ss.curTab);
        isRestored = true;

    }

    @Override
    public void onTabChanged(String tabId) {
        if (mAttached) {
            final FragmentTransaction ft = doTabChanged(tabId, null);
            if (ft != null) {
                ft.commitAllowingStateLoss();//ft.commit();
            }
        }
        if (mOnTabChangeListener != null) {
            mOnTabChangeListener.onTabChanged(tabId);
        }
    }

    @Nullable
    private FragmentTransaction doTabChanged(@Nullable String tag, @Nullable FragmentTransaction ft) {

        FragmentTabHost.TabInfo newTab = getTabInfoForTag(tag);
        if (newTab == null) {
            throw new IllegalStateException("No tab known for tag " + tag);
        }

        if (isRestored) {
            if (mTabs != null && mTabs.size() > 0) {
                for (int i = 0; i < mTabs.size(); i++) {
                    TabInfo tab = mTabs.get(i);
                    if (!tab.tag.equals(mLastTab.tag)) {
                        tab.fragment = null;
                    }
                }
            }
            isRestored = false;
        }

        if (mLastTab != newTab) {
            if (ft == null) {
                ft = mFragmentManager.beginTransaction();
            }

            if (mLastTab != null) {
                if (mLastTab.fragment != null) {
                    //ft.detach(mLastTab.fragment);
                    ft.hide(mLastTab.fragment);
                }
            }

            if (newTab != null) {
                if (newTab.fragment == null) {

                    newTab.fragment = Fragment.instantiate(mContext, newTab.clss.getName(), newTab.args);
                    ft.add(mContainerId, newTab.fragment, newTab.tag);
                } else {
                    // ft.attach(newTab.fragment);
                    ft.show(newTab.fragment);
                }
            }

            mLastTab = newTab;
        }

        return ft;
    }

    @Nullable
    private FragmentTabHost.TabInfo getTabInfoForTag(String tabId) {
        for (int i = 0, count = mTabs.size(); i < count; i++) {
            final FragmentTabHost.TabInfo tab = mTabs.get(i);
            if (tab.tag.equals(tabId)) {
                return tab;
            }
        }
        return null;
    }
}

