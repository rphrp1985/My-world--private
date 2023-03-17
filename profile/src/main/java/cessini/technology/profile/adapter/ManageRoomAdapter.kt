package cessini.technology.profile.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import cessini.technology.profile.fragment.*

class ManageRoomAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
): FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                RoomsFragment()
            }
            1 -> {
                ScheduledFragment()
            }
            2 -> {
                RequestsFragment()
            }
            else -> {
                RoomsFragment()
            }
        }
    }
}
