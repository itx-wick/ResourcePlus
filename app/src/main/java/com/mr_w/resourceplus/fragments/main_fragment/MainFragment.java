package com.mr_w.resourceplus.fragments.main_fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.fxn.pix.Pix;
import com.google.android.material.tabs.TabLayoutMediator;
import com.mr_w.resourceplus.BR;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.activities.contacts_activity.ContactActivity;
import com.mr_w.resourceplus.activities.select_contact.SelectContactActivity;
import com.mr_w.resourceplus.activities.settings_activity.SettingsActivity;
import com.mr_w.resourceplus.databinding.FragmentMainBinding;
import com.mr_w.resourceplus.injections.di.component.FragmentComponent;
import com.mr_w.resourceplus.injections.ui.base.BaseFragment;
import com.mr_w.resourceplus.fragments.call_list.CallListFragment;
import com.mr_w.resourceplus.fragments.camera_fragment.CameraFragment;
import com.mr_w.resourceplus.fragments.chat_list.ChatListFragment;
import com.mr_w.resourceplus.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import org.jetbrains.annotations.NotNull;

import io.ak1.pix.PixFragment;
import io.ak1.pix.helpers.PixBus;
import io.ak1.pix.helpers.PixEventCallback;
import io.ak1.pix.models.Mode;
import io.ak1.pix.models.Options;
import io.ak1.pix.models.Ratio;

public class MainFragment extends BaseFragment<FragmentMainBinding, MainFragmentViewModel> implements MainFragmentNavigator {
    private FragmentMainBinding mBinding;
    public static MainFragment instance;
    int pos = 1;
    Toolbar toolbar;
    public boolean isKeyBoardShowing = false;
    public boolean isLayoutShown = false;

    private final ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            int heightDiff = mBinding.frame.getRootView().getHeight() - mBinding.frame.getHeight();
            isKeyBoardShowing = heightDiff > Utils.dpToPx(128);
        }
    };

    public FragmentMainBinding getmBinding() {
        return mBinding;
    }

    public MainFragment() {
        instance = this;
    }

    @Override
    public int getBindingVariable() {
        return BR.main_fragment;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    public void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel.setNavigator(this);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                getActivity().finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding = getViewDataBinding();
        mBinding.frame.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);
        setUpWithViewPager(mBinding.viewPager);
        new TabLayoutMediator(mBinding.tabLayout, mBinding.viewPager,
                (tab, position) -> tab.setText(position == 0 ? "" : position == 1 ? "Chats" : "Calls")
        ).attach();
        mBinding.tabLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.primary));
        View tab1 = LayoutInflater.from(getActivity()).inflate(R.layout.custom_camera_tab, null);
        try {
            mBinding.tabLayout.getTabAt(0).setCustomView(tab1);
            mBinding.tabLayout.getTabAt(0).view
                    .setLayoutParams(new LinearLayout.LayoutParams((int) Utils.dpToPx(60), LinearLayout.LayoutParams.WRAP_CONTENT));
        } catch (Exception e) {
            e.printStackTrace();
        }
        toolbar = view.findViewById(R.id.toolbar);
        mBinding.viewPager.setCurrentItem(1); // Defualt display CHats tab

        mBinding.searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mBinding.searchBar.length() > 0) {
                    if (mBinding.clear.getVisibility() == View.GONE)
                        mBinding.clear.setVisibility(View.VISIBLE);
                } else {
                    mBinding.clear.setVisibility(View.GONE);
                }
                if (pos == 1) {
                    if (ChatListFragment.object.adapter != null)
                        ChatListFragment.object.adapter.getFilter().filter(mBinding.searchBar.getText().toString());
                } else if (pos == 2) {
                    if (CallListFragment.callListObject.mAdapter != null)
                        CallListFragment.callListObject.mAdapter.getFilter().filter(mBinding.searchBar.getText().toString());
                }
            }
        });

        mBinding.btnBack.setOnClickListener(v -> {
            if (mBinding.layoutSearch.getVisibility() == View.VISIBLE) {
                closeKeyboard();
                isLayoutShown = false;
                isKeyBoardShowing = false;
                mBinding.searchBar.setText("");
                mBinding.layoutSearch.setVisibility(View.GONE);
                mBinding.btnBack.setVisibility(View.GONE);
                mBinding.logoXmarks.setVisibility(View.VISIBLE);
                mBinding.search.setVisibility(View.VISIBLE);
            }
        });

        mBinding.clear.setOnClickListener(v -> {
            mBinding.clear.setVisibility(View.GONE);
            mBinding.searchBar.setText("");
        });

        mBinding.search.setOnClickListener(v -> {
            Utils.showKeyboard(getActivity());
            isLayoutShown = true;
            isKeyBoardShowing = true;
            mBinding.searchBar.requestFocus();
            mBinding.layoutSearch.setVisibility(View.VISIBLE);
            mBinding.btnBack.setVisibility(View.VISIBLE);
            mBinding.logoXmarks.setVisibility(View.GONE);
            mBinding.search.setVisibility(View.GONE);
        });

        mBinding.topAppBar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_new_group) {
                startActivity(new Intent(getActivity(), SelectContactActivity.class)
                        .putExtra("group", "group"));
            } else if (id == R.id.action_settings) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
            }
            return true;
        });

        mBinding.fabAction.setOnClickListener(v -> startActivity(new Intent(getActivity(), ContactActivity.class)));

        mBinding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if (position == 0) {
                    hideSystemUI();
                } else {
                    showSystemUI();
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                changeFabICon(position);
                pos = position;
                hideSearchLayout();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

    }

    private void hideSystemUI() {
        mBinding.topBar.setVisibility(View.GONE);
    }

    private void showSystemUI() {
        mBinding.topBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mBinding.logoXmarks.getVisibility() == View.GONE) {
            closeKeyboard();
            mBinding.searchBar.setText("");
            mBinding.layoutSearch.setVisibility(View.GONE);
            mBinding.btnBack.setVisibility(View.GONE);
            mBinding.logoXmarks.setVisibility(View.VISIBLE);
            mBinding.search.setVisibility(View.VISIBLE);
        }

    }

    public void hideSearchLayout() {
        if (isKeyBoardShowing)
            closeKeyboard();
        isLayoutShown = false;
        mBinding.searchBar.setText("");
        mBinding.layoutSearch.setVisibility(View.GONE);
        mBinding.btnBack.setVisibility(View.GONE);
        mBinding.logoXmarks.setVisibility(View.VISIBLE);
        mBinding.search.setVisibility(View.VISIBLE);
    }

    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();

        if (view == null) {
            mBinding.searchBar.requestFocus();
            view = getActivity().getCurrentFocus();
        }

        InputMethodManager manager
                = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void setUpWithViewPager(ViewPager2 viewPager) {

        Options options = new Options();
        options.setFrontFacing(true);
        options.setVideoDurationLimitInSeconds(20);
        options.setMode(Mode.All);
        options.setCount(1);
        options.setRatio(Ratio.RATIO_AUTO);

        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getActivity());
        viewPager.setAdapter(adapter);
    }

    private static class SectionsPagerAdapter extends FragmentStateAdapter {
        public SectionsPagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NotNull
        @Override
        public Fragment createFragment(int position) {
//            return position == 0 ? new PixFragment() :
            return position == 0 ? new CameraFragment() :
                    position == 1 ? new ChatListFragment() :
                            new CallListFragment();
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    private void changeFabICon(final int index) {
        mBinding.fabAction.hide();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (index) {
                    case 0:
                        mBinding.fabAction.hide();
                        break;
                    case 1:
                        mBinding.fabAction.show();
                        mBinding.fabAction.setImageDrawable(getResources().getDrawable(R.drawable.ic_chat_black_24dp));
                        mBinding.fabAction.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(getActivity(), ContactActivity.class));
                            }
                        });
                        break;
                    case 2:
                        mBinding.fabAction.show();
                        mBinding.fabAction.setImageDrawable(getResources().getDrawable(R.drawable.ic_call_black_24dp));
                        mBinding.fabAction.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(getActivity(), ContactActivity.class));
                            }
                        });
                        break;
                }
            }
        }, 400);

    }


}