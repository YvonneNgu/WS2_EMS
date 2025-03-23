package com.example.workshop2.organiser;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.workshop2.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AnalyticsOrganizerFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics_organizer, container, false);

        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);

        setupViewPager();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewPager != null) {
            viewPager.setAdapter(null); // Reset the adapter to avoid stale fragment state issues
            setupViewPager();
        }
    }

    private void setupViewPager() {
        viewPager.setAdapter(new AnalyticsPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Participants");
                    break;
                case 1:
                    tab.setText("Revenue");
                    break;
                case 2:
                    tab.setText("Event Types");
                    break;
            }
        }).attach();

        // Disable state restoration if not needed
        viewPager.setSaveEnabled(false);
    }

    static class AnalyticsPagerAdapter extends FragmentStateAdapter {

        public AnalyticsPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new ParticipantsFragment();
                case 1:
                    return new RevenueFragment();
                case 2:
                    return new EventTypesFragment();
                default:
                    throw new IllegalArgumentException("Invalid position");
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean containsItem(long itemId) {
            return itemId >= 0 && itemId < getItemCount();
        }
    }
}