package com.iven.vectorify.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.iven.vectorify.R
import com.iven.vectorify.mVectorifyPreferences
import com.iven.vectorify.ui.Utils

class IconsAdapter(private val context: Context) : RecyclerView.Adapter<IconsAdapter.IconsHolder>() {

    var onIconClick: ((Int?) -> Unit)? = null

    //first = background color, second = icon color
    private val mIcons = listOf(
        //from https://material.io/resources/icons
        //and
        //https://materialdesignicons.com/

        //android or tech related
        R.drawable.android,
        R.drawable.android_debug_bridge,
        R.drawable.android_head,
        R.drawable.android_studio,
        R.drawable.github_face,
        R.drawable.gitlab,
        R.drawable.ladybug,
        R.drawable.fingerprint,
        R.drawable.bug,
        R.drawable.robot,
        R.drawable.google_cardboard,
        R.drawable.memory,
        R.drawable.navigation,
        R.drawable.place,
        R.drawable.map_marker_check,

        //figures and simbols
        R.drawable.dot,
        R.drawable.origin,
        R.drawable.triangle_inverted,
        R.drawable.grade,
        R.drawable.hearth_border,
        R.drawable.heart,
        R.drawable.heart_multiple,
        R.drawable.heart_pulse,
        R.drawable.charity,
        R.drawable.high,
        R.drawable.help,
        R.drawable.alert,
        R.drawable.alert_outline,
        R.drawable.alert_circle,

        //animals
        R.drawable.cat,
        R.drawable.dog,
        R.drawable.dog_side,
        R.drawable.pets,
        R.drawable.fish,
        R.drawable.cow,
        R.drawable.rabbit,
        R.drawable.sheep,
        R.drawable.owl,
        R.drawable.panda,
        R.drawable.penguin,
        R.drawable.pig,
        R.drawable.pig_variant,
        R.drawable.tortoise,
        R.drawable.donkey,
        R.drawable.duck,
        R.drawable.bee_flower,
        R.drawable.jellyfish,

        //emoticons
        R.drawable.face,
        R.drawable.emoticon,
        R.drawable.emoticon_cool,
        R.drawable.emoticon_excited,
        R.drawable.emoticon_happy,
        R.drawable.emoticon_tongue,
        R.drawable.emoticon_poop,
        R.drawable.sentiment_very_satisfied,
        R.drawable.sticker_emoji,
        R.drawable.star_face,
        R.drawable.alien,

        //let's have fun, parties and relax
        R.drawable.toys,
        R.drawable.videogame,
        R.drawable.beach,
        R.drawable.balloon,

        //food
        R.drawable.ice_pop,
        R.drawable.cookie,
        R.drawable.food_croissant,
        R.drawable.coffee,
        R.drawable.coffee_outline,
        R.drawable.tea,
        R.drawable.tea_outline,
        R.drawable.cupcake,
        R.drawable.pizza,
        R.drawable.chef_hat,
        R.drawable.fastfood,
        R.drawable.rice,
        R.drawable.sausage,
        R.drawable.baguette,
        R.drawable.chili_mild,
        R.drawable.corn,
        R.drawable.mushroom,
        R.drawable.food_apple,
        R.drawable.carrot,

        //nature
        R.drawable.nature,
        R.drawable.nature_people,
        R.drawable.pine_tree,
        R.drawable.flower_poppy,
        R.drawable.flower,
        R.drawable.flower_tulip,
        R.drawable.tree,
        R.drawable.water,
        R.drawable.leaf,
        R.drawable.sprout,
        R.drawable.seed,
        R.drawable.clover,
        R.drawable.wb_sunny,
        R.drawable.cloud,
        R.drawable.cloud_outline,

        //other
        R.drawable.school,
        R.drawable.tie,
        R.drawable.brain,
        R.drawable.thumb_up,
        R.drawable.human_greeting,
        R.drawable.airballoon,
        R.drawable.sunglasses,

        //other symbols
        R.drawable.yin_yang,
        R.drawable.pi,
        R.drawable.alpha,
        R.drawable.beta,
        R.drawable.lambda,
        R.drawable.sigma,
        R.drawable.sigma_lower,
        R.drawable.infinity,
        R.drawable.flash,
        R.drawable.fire,
        R.drawable.sticker,

        //science
        R.drawable.hexagon_outline,
        R.drawable.diamond,
        R.drawable.flask,
        R.drawable.flask_outline,
        R.drawable.flask_empty,
        R.drawable.flask_empty_outline,
        R.drawable.beaker,
        R.drawable.beaker_outline,
        R.drawable.test_tube,
        R.drawable.grain,
        R.drawable.radioactive,
        R.drawable.nuke,
        R.drawable.biohazard,
        R.drawable.bacteria,
        R.drawable.atom,
        R.drawable.atom_variant,
        R.drawable.meteor,
        R.drawable.rocket,

        //music
        R.drawable.music_note,
        R.drawable.music,
        R.drawable.music_clef_treble,
        R.drawable.guitar_electric,
        R.drawable.violin,
        R.drawable.guitar_pick,
        R.drawable.odnoklassniki,
        R.drawable.saxophone,

        //nerd
        R.drawable.space_invaders,
        R.drawable.ghost,
        R.drawable.one_up,
        R.drawable.pokeball,
        R.drawable.death_star,
        R.drawable.death_star_variant,
        R.drawable.db_tenkaichi,
        R.drawable.kame_sennin_mark,
        R.drawable.dragon_sphere,
        R.drawable.puzzle,

        //buildings
        R.drawable.city,
        R.drawable.city_variant,
        R.drawable.city_variant_outline,
        R.drawable.pillar,
        R.drawable.bank,

        //alert
        //zodiac
        R.drawable.zodiac_aries,
        R.drawable.zodiac_cancer,
        R.drawable.zodiac_capricorn,
        R.drawable.zodiac_gemini,
        R.drawable.zodiac_leo,
        R.drawable.zodiac_libra,
        R.drawable.zodiac_pisces,
        R.drawable.zodiac_sagittarius,
        R.drawable.zodiac_scorpio,
        R.drawable.zodiac_taurus,
        R.drawable.zodiac_virgo
    )

    private var mSelectedDrawable = R.drawable.android

    init {
        mSelectedDrawable = mVectorifyPreferences.icon
    }

    fun getIconPosition(drawable: Int): Int {
        return try {
            mIcons.indexOf(drawable)
        } catch (e: Exception) {
            0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconsHolder {
        return IconsHolder(LayoutInflater.from(parent.context).inflate(R.layout.icon_option, parent, false))
    }

    override fun getItemCount(): Int {
        return mIcons.size
    }

    override fun onBindViewHolder(holder: IconsHolder, position: Int) {
        holder.bindItems(mIcons[holder.adapterPosition])
    }

    inner class IconsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val accent = Utils.getSystemAccentColor(context)

        fun bindItems(drawable: Int) {

            val iconButton = itemView as ImageButton

            iconButton.setImageResource(drawable)

            if (mSelectedDrawable == drawable) iconButton.setBackgroundColor(accent)
            else
                iconButton.setBackgroundColor(Color.TRANSPARENT)

            itemView.setOnClickListener {
                notifyItemChanged(getIconPosition(mSelectedDrawable))
                mSelectedDrawable = drawable
                iconButton.setBackgroundColor(accent)
                onIconClick?.invoke(drawable)
            }
        }
    }
}