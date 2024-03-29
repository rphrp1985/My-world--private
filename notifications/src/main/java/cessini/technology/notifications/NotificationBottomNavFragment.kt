package cessini.technology.notifications

import android.os.Bundle
import android.view.View
import cessini.technology.commonui.activity.HomeActivity
import cessini.technology.commonui.common.BaseFragment
import cessini.technology.notifications.databinding.FragmentNotificationBottomNavBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class NotificationBottomNavFragment : BaseFragment<FragmentNotificationBottomNavBinding>(
    R.layout.fragment_notification_bottom_nav
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this

        (requireActivity() as HomeActivity).setUpNavProfileIcon(
            null,
            (requireActivity() as HomeActivity).profileDrawable,
            false
        )

        binding.vpNotificationMessage.currentItem = 0

        binding.vpNotificationMessage.adapter =
            NotificationMessageAdapter(childFragmentManager, lifecycle)

        TabLayoutMediator(
            binding.tabNotificationMessage, binding.vpNotificationMessage
        ) { tab: TabLayout.Tab, position: Int ->

            tab.text = if (position == 0) "Messages" else "Notification"
        }.attach()
    }
}