package cessini.technology.myspace

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import cessini.technology.myspace.create.UserCreator

class SearchAdapter(
    titles: ArrayList<String>,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        Log.i("Viewpager adaptor no.:", "$position")
        return when (position) {
            0 -> {
                UserCreator()
            }
            else -> {
                UserCreator()
            }
        }
    }
}
