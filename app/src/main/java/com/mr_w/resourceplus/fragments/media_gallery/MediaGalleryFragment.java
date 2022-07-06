package com.mr_w.resourceplus.fragments.media_gallery;

import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.mr_w.resourceplus.BR;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.databinding.FragmentMediaGalleryBinding;
import com.mr_w.resourceplus.fragments.document_tab.DocumentTab;
import com.mr_w.resourceplus.fragments.links_tab.LinksTab;
import com.mr_w.resourceplus.fragments.multi_media_tab.MultiMediaTab;
import com.mr_w.resourceplus.injections.di.component.FragmentComponent;
import com.mr_w.resourceplus.injections.ui.base.BaseFragment;
import com.mr_w.resourceplus.model.Conversation;

import java.util.ArrayList;
import java.util.List;

public class MediaGalleryFragment extends BaseFragment<FragmentMediaGalleryBinding, MediaGalleryViewModel> implements MediaGalleryNavigator {

    FragmentMediaGalleryBinding mBinding;
    int pos = 1;
    private Conversation conversation;

    @Override
    public int getBindingVariable() {
        return BR.media_gallery;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_media_gallery;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel.setNavigator(this);
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
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
        conversation = (Conversation) getArguments().getSerializable("conversation");

        mBinding.back.setOnClickListener(v -> {
            getActivity().onBackPressed();
        });

        mBinding.main.setOnClickListener(v -> {
            //TODO
        });
        mBinding.title.setText(conversation.getTitle());

        setUpWithViewPager(mBinding.viewPager);
        mBinding.tabLayout.setupWithViewPager(mBinding.viewPager);
        mBinding.tabLayout.setBackgroundColor(getResources().getColor(R.color.primary));

        mBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                pos = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mBinding.viewPager.setCurrentItem(0);

    }

    @Override
    public void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    private void setUpWithViewPager(ViewPager viewPager) {
        MediaGalleryFragment.SectionsPagerAdapter adapter = new MediaGalleryFragment.SectionsPagerAdapter(getChildFragmentManager());
        MultiMediaTab multiMediaTab = new MultiMediaTab();
        DocumentTab documentTab = new DocumentTab();
        LinksTab linksTab = new LinksTab();
        Bundle bundle = new Bundle();
        bundle.putSerializable("conversation", conversation);
        multiMediaTab.setArguments(bundle);
        documentTab.setArguments(bundle);
        linksTab.setArguments(bundle);
        adapter.addFragment(multiMediaTab, "MEDIA");
        adapter.addFragment(documentTab, "DOCS");
        adapter.addFragment(linksTab, "LINKS");
        viewPager.setAdapter(adapter);
    }

    private static class SectionsPagerAdapter extends FragmentPagerAdapter {

        public final List<Fragment> mFragmentList = new ArrayList<>();
        public final List<String> mFragmentTitleList = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}