package cessini.technology.myspace.create

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import cessini.technology.commonui.activity.HomeActivity
import cessini.technology.commonui.utils.ProfileConstants
import cessini.technology.cvo.entity.SearchHistoryEntity
import cessini.technology.cvo.exploremodels.SearchCreatorApiResponse
import cessini.technology.model.PreviousProfile
import cessini.technology.myspace.FriendsModel_
import cessini.technology.myspace.R
import cessini.technology.myspace.databinding.FragmentUserCreatorBinding
import cessini.technology.navigation.NavigationFlow
import cessini.technology.navigation.ToFlowNavigable
import cessini.technology.newrepository.preferences.UserIdentifierPreferences
import com.airbnb.epoxy.AsyncEpoxyController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UserCreator : Fragment() {
    private var _binding: FragmentUserCreatorBinding? = null
    val binding get() = _binding!!

    @Inject
    lateinit var userIdentifierPreferences: UserIdentifierPreferences

    var controllerHistory: UserSearchHistoryController? = null
    var controller: UserSearchCreatorController? = null
    var history: ArrayList<SearchHistoryEntity> = arrayListOf()
    lateinit var parentFrag:SearchUsersFragment
    val viewModel: cessini.technology.myspace.SearchView by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_user_creator,
            container,
            false
        )
        binding.creatorViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.contextCreator = requireContext()

        setCreatorRecycler()
        viewModel.creatorResponseModels.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                controller!!.allCreators = it
            }
        }

        parentFrag = this@UserCreator.parentFragment as SearchUsersFragment

        return binding.root
    }

    private fun setCreatorRecycler() {
        binding.recyclerViewCreator.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            controller = UserSearchCreatorController(userIdentifierPreferences.id,context, activity)
            binding.recyclerViewCreator.setController(controller!!)

        }
    }

    override fun onResume() {
        super.onResume()

        setHistory()
        binding.recyclerViewCreatorHistory.visibility = View.VISIBLE
        viewModel.creatorHistory.observe(viewLifecycleOwner) {

            Log.i("Explore", "Outside: $it")
            if (it != null) {

                history = it
                Log.i("Explore", "Inside: $it")
                controllerHistory!!.historyList = it

                if (history.size == 0) {

                    binding.layoutCreator.visibility = View.VISIBLE
                    binding.recyclerViewCreatorHistory.visibility = View.GONE

                } else {
                    binding.layoutCreator.visibility = View.GONE
//                    binding.recyclerViewCreatorHistory.visibility = View.VISIBLE
                }
            }

        }

        val parentFrag: SearchUsersFragment =
            this@UserCreator.parentFragment as SearchUsersFragment

        parentFrag.binding.searchViewHeader.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                binding.recyclerViewCreatorHistory.visibility = View.GONE

                Log.i("Submit searched query: ", query!!)
                viewModel.fetchSearchCreatorQueryAPI(query)
                viewModel.creatorResponseModels.observe(viewLifecycleOwner) {

                    if (it != null) {
                        if (it.size > 0) {
                            binding.layoutCreator.visibility = View.GONE
                            binding.recyclerViewCreator.visibility = View.VISIBLE
                        } else {
                            binding.layoutCreator.visibility = View.GONE
                            binding.recyclerViewCreator.visibility = View.GONE
                        }
                    }


                    binding.recyclerViewCreatorHistory.visibility = View.GONE

                }

                if (query == "") {
                    binding.layoutCreator.visibility = View.GONE
                    binding.recyclerViewCreator.visibility = View.GONE
                    binding.recyclerViewCreatorHistory.visibility = View.VISIBLE
                }

                //Insert to db
                val entity = SearchHistoryEntity(
                    id = "$query Creator",
                    category = "Creator",
                    query = query
                )
                viewModel.insertSearchQueryToDB(entity)

                Log.d("Explore", entity.toString())

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                Log.i("Creator text pattern: ", newText!!)

                viewModel.creatorResponseModels.observe(viewLifecycleOwner) {
                    if (it != null) {
                        if (it.size > 0) {
                            binding.layoutCreator.visibility = View.GONE
                            binding.recyclerViewCreator.visibility = View.VISIBLE
                            binding.layoutNoResultCreator.visibility = View.GONE
                        } else {
                            binding.layoutCreator.visibility = View.GONE
                            binding.recyclerViewCreator.visibility = View.GONE
                            binding.layoutNoResultCreator.visibility = View.VISIBLE
                        }
                    }
                }

                if (newText.isEmpty()) {
                    binding.recyclerViewCreatorHistory.visibility = View.VISIBLE
                    binding.recyclerViewCreator.visibility = View.GONE
                    binding.layoutNoResultCreator.visibility = View.GONE
                }

                if (newText.length >= 2) {
                    viewModel.fetchSearchCreatorQueryAPI(newText)
                    binding.recyclerViewCreatorHistory.visibility = View.GONE
                    binding.recyclerViewCreator.visibility = View.VISIBLE
                }

                return false
            }

        })
    }

    fun setHistory() {
        binding.recyclerViewCreator.visibility = View.GONE
        binding.recyclerViewCreatorHistory.apply {
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            controllerHistory = UserSearchHistoryController(context, parentFrag)
            binding.recyclerViewCreatorHistory.setController(controllerHistory!!)
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()

        controller = null
        controllerHistory = null
        _binding = null
    }
}
class UserSearchHistoryController(var context: Context?, var parentFrag: SearchUsersFragment?) :
    AsyncEpoxyController() {
    var historyList = ArrayList<SearchHistoryEntity>()
        set(value) {
            field = value
            requestModelBuild()
        }


    override fun buildModels() {
        historyList.forEachIndexed { index, searchFriendsModel ->
//            val data= searchFriendsModel.id?.let { PreviousProfile(it,searchFriendsModel.,searchCreatorModel.channel_name,searchCreatorModel.profile_picture,0,0,"") }
//            userSearchHistory {
//                id(index)
//                historyModel(searchFriendsModel)
//
//                onClickHistory { _, _, _, position ->
////                    Toast.makeText(
////                        context,
////                        "Query: ${historyList[position].query}",
////                        Toast.LENGTH_SHORT
////                    ).show()
//
//                    parentFrag!!.binding.searchViewHeader.setQuery(historyList[position].query, true)
//                }
//            }
//            FriendsModel_()
//                .id(searchCreatorModel.id)
//                .data(data)
//                .addTo(this)

        }
    }
}
class UserSearchCreatorController(var id:String,var context: Context, var activity: FragmentActivity?) :
    AsyncEpoxyController() {

    var allCreators = emptyList<SearchCreatorApiResponse>()
        set(value) {
            field = value
            requestModelBuild()
        }

    override fun buildModels() {
        allCreators.forEachIndexed { index, searchCreatorModel ->
            Log.i("UserSearchCreator curr:", allCreators[index].id!!.toString())
            Log.i(
                "UserSearchCreator orig:",
                (activity as HomeActivity).baseViewModel.id.value.toString()
            )
            if (allCreators[index].id!!.toString() != (activity as HomeActivity).baseViewModel.id.value) {
                val data= searchCreatorModel.id?.let { PreviousProfile(it,searchCreatorModel.name,searchCreatorModel.channel_name,searchCreatorModel.profile_picture,0,0,"") }
//                userSearchCreatorItem {
//                    id(index)
//                    creator(searchCreatorModel)
//                    onClickCreator { _, _, _, _ ->
//                        ProfileConstants.creatorModel = searchCreatorModel
//                        (activity as ToFlowNavigable).navigateToFlow(
//                            NavigationFlow.GlobalProfileFlow
//                        )
//                    }
//                }
                FriendsModel_(id,context)
                    .id(searchCreatorModel.id)
                    .data(data)
                    .addTo(this)


            }
        }
    }
}