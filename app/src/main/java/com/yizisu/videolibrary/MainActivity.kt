package com.yizisu.videolibrary


import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.yizisu.playerlibrary.activity.FullScreenVideoActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val readTvBeans = readTvBeans(this)
        readTvBeans?.let { datas ->
            findViewById<RecyclerView>(R.id.rcv).apply {
                adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                    override fun onCreateViewHolder(
                        parent: ViewGroup,
                        viewType: Int
                    ): RecyclerView.ViewHolder {
                        return object : RecyclerView.ViewHolder(
                            LayoutInflater.from(parent.context)
                                .inflate(android.R.layout.simple_list_item_1, parent, false)
                        ) {

                        }
                    }

                    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                        val data = datas[position]
                        (holder.itemView as TextView).text = data.name
                        holder.itemView.setOnClickListener {
                            FullScreenVideoActivity.start(
                                this@MainActivity,
                                data.urlBean[1].url,
                                data.name
                            )
                        }
                    }

                    override fun getItemCount(): Int = datas.count()
                }
            }
        }

    }
}
