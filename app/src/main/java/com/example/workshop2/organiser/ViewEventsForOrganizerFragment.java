package com.example.workshop2.organiser;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.workshop2.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ViewEventsForOrganizerFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_organizer, container, false);

        // Initialize TabLayout, ViewPager2, and SwipeRefreshLayout
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager2 viewPager2 = view.findViewById(R.id.viewPager2);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // Set up the ViewPager2 adapter
        viewPager2.setAdapter(new EventCategoryAdapter(requireActivity()));

        // Attach TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Ongoing");
                    break;
                case 1:
                    tab.setText("Future");
                    break;
                case 2:
                    tab.setText("Past");
                    break;
                    case 3:
                    tab.setText("Status");
                    break;
            }
        }).attach();
        //tambah function status
        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshFragments();
            swipeRefreshLayout.setRefreshing(false);
        });

        return view;
    }

    private void refreshFragments() {
        for (Fragment fragment : requireActivity().getSupportFragmentManager().getFragments()) {
            if (fragment instanceof OngoingEventsFragment) {
                ((OngoingEventsFragment) fragment).refreshEvents();
            } else if (fragment instanceof FutureEventsFragment) {
                ((FutureEventsFragment) fragment).refreshEvents();
            } else if (fragment instanceof PastEventsFragment) {
                ((PastEventsFragment) fragment).refreshEvents();
            }else if (fragment instanceof StatusEventsFragment) {
                ((StatusEventsFragment) fragment).refreshEvents();
            }
        }
    }
    //tambah function status
    private static class EventCategoryAdapter extends FragmentStateAdapter {
        public EventCategoryAdapter(FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new OngoingEventsFragment();
                case 1:
                    return new FutureEventsFragment();
                case 2:
                    return new PastEventsFragment();
                    case 3:
                    return new StatusEventsFragment();
                default:
                    return new OngoingEventsFragment();
            }
        }
        //tambah function status

        @Override
        public int getItemCount() {
            return 4; // Ongoing, Future, Past
        }
    }
}