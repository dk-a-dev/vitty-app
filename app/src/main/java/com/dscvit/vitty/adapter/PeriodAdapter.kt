package com.dscvit.vitty.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.dscvit.vitty.R
import com.dscvit.vitty.databinding.CardPeriodBinding
import com.dscvit.vitty.model.PeriodDetails
import com.dscvit.vitty.util.Effects.vibrateOnClick
import com.dscvit.vitty.util.RemoteConfigUtils
import com.dscvit.vitty.util.UtilFunctions.copyItem
import com.dscvit.vitty.util.VITMap
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PeriodAdapter(
    private val dataSet: ArrayList<PeriodDetails>,
    private val day: Int,
) : RecyclerView.Adapter<PeriodAdapter.ViewHolder>() {
    private var active = -1

    class ViewHolder(
        private val binding: CardPeriodBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        val activePeriod = binding.activePeriod
        val periodTime = binding.periodTime
        val classNav = binding.classNav
        val classIdOnline = binding.classIdOnline
        val periodCard = binding.periodCard

        fun bind(data: PeriodDetails) {
            binding.periodDetails = data
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder =
        ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.card_period,
                parent,
                false,
            ),
        )

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val item = dataSet[holder.adapterPosition]
        holder.bind(item)

        val startTime: Date = item.startTime.toDate()
        val simpleDateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val sTime: String = simpleDateFormat.format(startTime).uppercase(Locale.ROOT)

        val endTime: Date = item.endTime.toDate()
        val eTime: String = simpleDateFormat.format(endTime).uppercase(Locale.ROOT)

        val now = Calendar.getInstance()
        val s = Calendar.getInstance()
        s.time = startTime
        val start = Calendar.getInstance()
        start[Calendar.HOUR_OF_DAY] = s[Calendar.HOUR_OF_DAY]
        start[Calendar.MINUTE] = s[Calendar.MINUTE]
        val e = Calendar.getInstance()
        e.time = endTime
        val end = Calendar.getInstance()
        end[Calendar.HOUR_OF_DAY] = e[Calendar.HOUR_OF_DAY]
        end[Calendar.MINUTE] = e[Calendar.MINUTE]

        holder.apply {
            if (!RemoteConfigUtils.getOnlineMode()) {
                classNav.visibility = View.VISIBLE
                classIdOnline.visibility = View.GONE
            } else {
                classNav.visibility = View.GONE
                classIdOnline.visibility = View.VISIBLE
            }
        }

        holder.apply {
            periodTime.text = "$sTime - $eTime | ${item.slot}"
            activePeriod.visibility = View.INVISIBLE
            periodCard.strokeWidth = 0

            classNav.apply {
                setOnClickListener {
                    VITMap.openClassMap(classNav.context, item.roomNo)
                }
                setOnLongClickListener {
                    copyItem(
                        context,
                        "Room Number",
                        "ROOM_NUMBER_ITEM",
                        item.roomNo,
                    )
                    true
                }
            }
        }

        if ((((day + 1) % 7) + 1) == now[Calendar.DAY_OF_WEEK]) {
            if ((start.before(now) && end.after(now)) ||
                start.equals(now) ||
                (start.after(now) && active == -1) ||
                active == holder.adapterPosition
            ) {
                holder.activePeriod.visibility = View.VISIBLE
                holder.periodCard.strokeWidth = 2
                holder.periodCard.strokeColor = ContextCompat.getColor(holder.itemView.context, R.color.translucent)
                active = holder.adapterPosition
            }
        }

        holder.itemView.apply {
            setOnClickListener {
                vibrateOnClick(holder.itemView.context)
            }
            setOnLongClickListener {
                true
            }
        }
    }

    override fun getItemCount() = dataSet.size
}
