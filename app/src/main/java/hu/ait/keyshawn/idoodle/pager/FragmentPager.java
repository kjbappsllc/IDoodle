package hu.ait.keyshawn.idoodle.pager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by vickievictor on 5/15/17.
 */

public class FragmentPager extends FragmentPagerAdapter{

    public FragmentPager(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position){
            case 0:
                fragment = new FragmentLobbies();
                break;
            case 1:
                fragment = new FragmentUserDetails();
                break;
            default:
                fragment = new FragmentLobbies();
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position==0) {
            return "Current Lobbies";
        } else {
            return "User Details";
        }
    }
}
