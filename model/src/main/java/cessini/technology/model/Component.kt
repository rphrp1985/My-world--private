package cessini.technology.model

import com.google.gson.annotations.SerializedName

data class Component (
    val message: MessageD? = null,
    val status: Boolean? = null
)

data class MessageD (
    @SerializedName("Technology")
    val trendingTechnology: List<HealthFitness>? = null,

    @SerializedName("Trending Technology")
    val trendingNews: List<HealthFitness>? = null,

    @SerializedName("Entertainment")
    val entertainment: List<HealthFitness>? = null,

    @SerializedName("Food")
    val healthFitness: List<HealthFitness>? = null,

    @SerializedName("Knowledge & Careers")
    val knowledgeCareers: List<HealthFitness>? = null
)

data class HealthFitness (
    @SerializedName("_id")
    val id: String? = null,

    val profile: ProfileI? = null,
    val title: String? = null,
    val description: String? = null,
    val thumbnail: String? = null,
    val duration: String? = null,
    val category: List<String>? = null,
    //val category: String? = null,

    @SerializedName("upload_file")
    val uploadFile: String? = null,

    val timestamp: Double? = null
)

data class ProfileI (
    @SerializedName("_id")
    val id: String? = null,

    val name: String? = null,

    @SerializedName("channel_name")
    val channelName: String? = null,

    @SerializedName("profile_picture")
    val profilePicture: String? = null
)




data class ComponentRecorded (
    val message: MessageRecorded? = null,
    val status: Boolean? = null
)

data class MessageRecorded (
    @SerializedName("Technology")
    val trendingTechnology: List<itemRecorded>? = null,

    @SerializedName("Trending Technology")
    val trendingNews: List<itemRecorded>? = null,

    @SerializedName("Entertainment")
    val entertainment: List<itemRecorded>? = null,

    @SerializedName("Food")
    val healthFitness: List<itemRecorded>? = null,

    @SerializedName("Knowledge & Careers")
    val knowledgeCareers: List<itemRecorded>? = null
)



data class itemRecorded(
    @SerializedName("title")
    val title:String?=null,

    @SerializedName("thumbnail")
    val thumbnail: String?=null,

    @SerializedName("recordingsFilePath")
    val record: String? = null,

    @SerializedName("creator")
    val creator:ProfileI
)