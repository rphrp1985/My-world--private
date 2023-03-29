package cessini.technology.profile.controller

import cessini.technology.commonui.fragment.suggestion.category
import cessini.technology.model.User
import cessini.technology.profile.fragment.FollowerTabFragment
import cessini.technology.profile.fragment.editProfile.AreaOfExpertiseFragment
import com.airbnb.epoxy.AsyncEpoxyController

class AreaOfExpertiseController(
    private val fragment: AreaOfExpertiseFragment
): AsyncEpoxyController() {

    var categoryList = ArrayList<String>()
        set(value) {
            field = value
            requestModelBuild()
        }

    override fun buildModels() {

        categoryList.forEachIndexed { index, s ->

        }
    }

}