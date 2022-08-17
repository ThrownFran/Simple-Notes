package brillembourg.notes.simple.presentation.base

//class FragmentViewBindingDelegate<R : Fragment, T : ViewDataBinding>(
//    @LayoutRes val layoutRes: Int,
//    private val container: ViewGroup,
//    private val inflater: LayoutInflater
//) :
//    ReadOnlyProperty<R, T> {
//
//    var binding: T? = null
//
//    override fun getValue(thisRef: R, property: KProperty<*>): T {
//        if (binding == null) {
//            binding = DataBindingUtil.inflate<T>(inflater, layoutRes, container, false).apply {
//                lifecycleOwner = thisRef
//            }
//        }
//        return binding!!
//    }
//
//
//}
//
//fun <R : Fragment, T : ViewDataBinding>
//        fragmentViews(
//    @LayoutRes layoutRes: Int,
//    container: ViewGroup,
//    inflater: LayoutInflater
//): FragmentViewBindingDelegate<R, T> =
//    FragmentViewBindingDelegate(layoutRes,container,inflater)