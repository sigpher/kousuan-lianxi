package com.example.myapplication.ui.arithmetic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.arithmetic.AccountRepository
import com.example.myapplication.arithmetic.ArithmeticRepository
import com.example.myapplication.arithmetic.WrongProblem
import com.example.myapplication.databinding.FragmentReviewBinding
import com.example.myapplication.databinding.ItemReviewBinding

class ReviewFragment : Fragment() {

    private var _binding: FragmentReviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.recyclerReview.layoutManager = LinearLayoutManager(view.context)
        val adapter = ReviewAdapter()
        binding.recyclerReview.adapter = adapter

        val activity = requireActivity()
        val userId = AccountRepository(view.context).currentUserId()
        ArithmeticRepository(view.context).fetchWrong(userId) { list ->
            activity.runOnUiThread {
                val current = _binding ?: return@runOnUiThread
                current.textReviewEmpty.visibility =
                    if (list.isEmpty()) View.VISIBLE else View.GONE
                adapter.submitList(list)
            }
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class ReviewAdapter :
        ListAdapter<WrongProblem, ReviewViewHolder>(object : DiffUtil.ItemCallback<WrongProblem>() {

            override fun areItemsTheSame(oldItem: WrongProblem, newItem: WrongProblem): Boolean =
                oldItem.question.text == newItem.question.text &&
                    oldItem.createdAt == newItem.createdAt

            override fun areContentsTheSame(oldItem: WrongProblem, newItem: WrongProblem): Boolean =
                oldItem == newItem
        }) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder =
            ReviewViewHolder(
                ItemReviewBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

        override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
            val wrong = getItem(position)
            holder.binding.textReviewFormula.text = wrong.question.text
            holder.binding.textReviewCorrect.text = wrong.question.answer.toString()
            holder.binding.textReviewYours.text = wrong.userAnswer
        }
    }

    private class ReviewViewHolder(val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root)
}