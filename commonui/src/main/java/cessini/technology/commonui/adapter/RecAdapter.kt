package cessini.technology.commonui.adapter

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cessini.technology.commonui.R


 class RecAdapter(pm : PackageManager?, private val apps: List<ResolveInfo?>, val context: Context?, val link:String):
    RecyclerView.Adapter<RecAdapter.MyViewHolder>() {


     class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
         val label = view.findViewById<View>(R.id.mylabel) as TextView
         var icon: ImageView = view.findViewById<View>(R.id.myicon) as ImageView
     }

     private var pm: PackageManager? = null
     override fun getItemCount(): Int {
         return apps.size
     }

     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
         val itemView = LayoutInflater.from(parent.context)
             .inflate(R.layout.myrow, parent, false)
         return MyViewHolder(itemView)
     }

     override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
         val label = apps[position]?.loadLabel(pm)
         holder.label.text = label
         val icon = apps.get(position)?.loadIcon(pm)
         holder.icon.setImageDrawable(icon)
         holder.itemView.setOnClickListener {
             val launchable = apps.get(position) as ResolveInfo
             val activity: ActivityInfo = launchable.activityInfo
             val i = Intent(Intent.ACTION_SEND)
             i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
             i.type = "text/plain"
             val packageName = activity.packageName
             i.putExtra(Intent.EXTRA_TEXT, link)
             i.setPackage(packageName)
             context?.startActivity(i)
         }
     }

     init {
         this.pm = pm
     }

 }

