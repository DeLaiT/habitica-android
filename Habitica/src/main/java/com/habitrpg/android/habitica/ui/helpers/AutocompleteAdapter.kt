package com.habitrpg.android.habitica.ui.helpers

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.social.FindUsernameResult
import com.habitrpg.android.habitica.ui.views.social.UsernameLabel
import net.pherth.android.emoji_library.EmojiMap
import net.pherth.android.emoji_library.EmojiParser
import net.pherth.android.emoji_library.EmojiTextView

class AutocompleteAdapter(val context: Context, val socialRepository: SocialRepository? = null, var autocompleteContext: String? = null, var groupID: String? = null) : BaseAdapter(), Filterable {
    private var userResults: List<FindUsernameResult> = arrayListOf()
    private var emojiResults: List<String> = arrayListOf()
    private var isAutocompletingUsers = true

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint != null && constraint.isNotEmpty()) {
                    if (constraint[0] == '@' && constraint.length >= 3 && socialRepository != null) {
                        isAutocompletingUsers = true
                        userResults = socialRepository.findUsernames(constraint.toString().drop(1), autocompleteContext, groupID).blockingFirst(arrayListOf())
                        filterResults.values = userResults
                        filterResults.count = userResults.size
                    } else if (constraint[0] == ':') {
                        isAutocompletingUsers = false
                        emojiResults = EmojiMap.invertedEmojiMap.keys.filter { it.startsWith(constraint) }
                        filterResults.values = emojiResults
                        filterResults.count = emojiResults.size
                    }
                }
                return filterResults
            }

            override fun publishResults(contraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return if (isAutocompletingUsers) {
            val view = parent?.inflate(R.layout.autocomplete_username)
            val result = getItem(position) as? FindUsernameResult
            val displaynameView = view?.findViewById<UsernameLabel>(R.id.display_name_view)
            displaynameView?.username = result?.username
            displaynameView?.tier = result?.contributor?.level ?: 0
            view?.findViewById<TextView>(R.id.username_view)?.text = result?.formattedUsername
            view
        } else {
            val view = parent?.inflate(R.layout.autocomplete_emoji)
            val result = getItem(position) as? String
            val emojiTextView = view?.findViewById<EmojiTextView>(R.id.emoji_textview)
            emojiTextView?.setEmojiconSize(24.dpToPx(context))
            emojiTextView?.text = EmojiParser.parseEmojis(result)
            view?.findViewById<TextView>(R.id.label)?.text = result
            view
        } ?: View(context)
    }

    override fun getItem(position: Int): Any {
        return if (isAutocompletingUsers) userResults[position] else emojiResults[position]
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).hashCode().toLong()
    }

    override fun getCount(): Int {
        return if (isAutocompletingUsers) userResults.size else emojiResults.size
    }


}