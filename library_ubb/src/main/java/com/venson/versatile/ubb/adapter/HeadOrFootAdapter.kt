package com.venson.versatile.ubb.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

abstract class HeadOrFootAdapter<VB : ViewBinding> :
    RecyclerView.Adapter<HeadOrFootAdapter.HeadOrFootHolder<VB>>() {

    @Px
    private var mHeadSpacing: Int = 0

    @Px
    private var mFootSpacing: Int = 0

    @SuppressLint("NotifyDataSetChanged")
    fun updateSpacing(headSpacing: Int, footSpacing: Int) {
        mHeadSpacing = headSpacing
        mFootSpacing = footSpacing
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeadOrFootHolder<VB> {
        val binding = inflateBindingWithGeneric<VB>(parent)
        binding.root.layoutParams?.let { layoutParams ->
            layoutParams as ViewGroup.MarginLayoutParams
            if (mHeadSpacing > 0) {
                layoutParams.bottomMargin = mHeadSpacing
            }
            if (mFootSpacing > 0) {
                layoutParams.topMargin = mFootSpacing
            }
            binding.root.layoutParams = layoutParams
        }
        onInflateBinding(binding)
        return HeadOrFootHolder(binding)
    }

    protected abstract fun onInflateBinding(binding: VB)

    override fun onBindViewHolder(holder: HeadOrFootHolder<VB>, position: Int) {
        onBindBinding(holder.getViewBinding())
    }

    protected abstract fun onBindBinding(binding: VB)

    override fun getItemCount(): Int = 1

    class HeadOrFootHolder<VB : ViewBinding>(private val binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        constructor(itemView: View) : this(ViewBinding { itemView })

        @Suppress("UNCHECKED_CAST")
        fun getViewBinding() = binding as VB
    }

    @JvmName("inflateWithGeneric")
    private fun <VB : ViewBinding> inflateBindingWithGeneric(parent: ViewGroup): VB =
        inflateBindingWithGeneric(LayoutInflater.from(parent.context), parent, false)

    @JvmName("inflateWithGeneric")
    private fun <VB : ViewBinding> inflateBindingWithGeneric(
        layoutInflater: LayoutInflater,
        parent: ViewGroup?,
        attachToParent: Boolean
    ): VB = withGenericBindingClass<VB>(this) { clazz ->
        clazz.getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        ).invoke(null, layoutInflater, parent, attachToParent) as VB
    }

    private fun <VB : ViewBinding> withGenericBindingClass(any: Any, block: (Class<VB>) -> VB): VB {
        any.allParameterizedType.forEach { parameterizedType ->
            parameterizedType.actualTypeArguments.forEach {
                try {
                    return block.invoke(it as Class<VB>)
                } catch (e: NoSuchMethodException) {
                } catch (e: ClassCastException) {
                }
            }
        }
        throw IllegalArgumentException("There is no generic of ViewBinding.")
    }

    private val Any.allParameterizedType: List<ParameterizedType>
        get() {
            val genericParameterizedType = mutableListOf<ParameterizedType>()
            var genericSuperclass = javaClass.genericSuperclass
            var superclass = javaClass.superclass
            while (superclass != null) {
                if (genericSuperclass is ParameterizedType) {
                    genericParameterizedType.add(genericSuperclass)
                }
                genericSuperclass = superclass.genericSuperclass
                superclass = superclass.superclass
            }
            return genericParameterizedType
        }
}