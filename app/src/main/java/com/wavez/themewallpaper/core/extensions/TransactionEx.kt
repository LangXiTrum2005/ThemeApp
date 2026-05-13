package com.wavez.themewallpaper.core.extensions

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.wavez.themewallpaper.R

inline fun FragmentManager.inTransaction(
    stateChange: Boolean = true,
    isAnim: Boolean = true,
    animEnter: Int = R.anim.enter_from_left,
    animExit: Int = R.anim.exit_from_left,
    func: FragmentTransaction.() -> Unit,
) {
    val fragmentTransaction = beginTransaction()
    if (isAnim) {
        fragmentTransaction.setCustomAnimations(animEnter, animExit)
    }
    fragmentTransaction.func()
    if (stateChange) {
        fragmentTransaction.commit()
    } else {
        fragmentTransaction.commitAllowingStateLoss()
    }
}

/*fun BaseActivity<*>.replaceFragment(
    fragment: Fragment,
    frameId: Int,
    backStack: Boolean = true,
    isAnim: Boolean = true,
) {
    supportFragmentManager.inTransaction(isAnim = isAnim) {
        replace(frameId, fragment, fragment.javaClass.simpleName)
        if (backStack) addToBackStack(fragment.javaClass.simpleName)
    }
}

fun BaseActivity<*>.addFragment(
    fragment: Fragment,
    frameId: Int,
    isSave: Boolean = false,
    isAnim: Boolean = true,
) {
    val manager = supportFragmentManager
    manager.inTransaction(isAnim = isAnim) {
        if (!isSave) {
            add(frameId, fragment, fragment.javaClass.simpleName)
        } else {
            manager.findFragmentByTag(fragment.javaClass.simpleName)?.let {
                if (it.isAdded) show(fragment)
            } ?: run {
                add(frameId, fragment, fragment.javaClass.simpleName)
            }
        }
    }
}

fun BaseFragment<*>.replaceFragment(
    fragment: Fragment,
    frameId: Int,
    backStack: Boolean = true,
    isAnim: Boolean = true,
) {
    requireActivity().supportFragmentManager.inTransaction(isAnim) {
        replace(frameId, fragment, fragment.javaClass.simpleName)
        if (backStack) addToBackStack(fragment.javaClass.simpleName)
    }
}

fun BaseFragment<*>.addFragment(
    fragment: Fragment,
    frameId: Int,
    isSave: Boolean = false,
    isAnim: Boolean = true,
) {
    val manager = requireActivity().supportFragmentManager
    manager.inTransaction(isAnim) {
        if (!isSave) {
            add(frameId, fragment, fragment.javaClass.simpleName)
        } else {
            manager.findFragmentByTag(fragment.javaClass.simpleName)?.let {
                if (it.isAdded) show(it)
            } ?: run {
                add(frameId, fragment, fragment.javaClass.simpleName)
            }
        }
    }
}

fun BaseFragment<*>.addFragment(
    fragment: Fragment,
    frameId: Int,
    backStack: Boolean = true,
) {
    try {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.add(frameId, fragment)
        if (backStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun BaseActivity<*>.addFragment(
    fragment: Fragment,
    frameId: Int,
    backStack: Boolean = true,
) {
    val transaction = supportFragmentManager.beginTransaction()
    transaction.add(frameId, fragment, fragment::class.java.simpleName)
    if (backStack) {
        transaction.addToBackStack(null)
    }
    transaction.commit()
}

inline fun <reified T : BaseFragment<*>> BaseActivity<*>.removeFragment(backStack: Boolean = true) {
    supportFragmentManager.findFragmentByTag(T::class.java.simpleName)?.removeSelf()
    if (backStack && supportFragmentManager.backStackEntryCount >= 1) supportFragmentManager.popBackStack()
}*/

/**
 * using when add fragment
 */
fun Fragment.removeSelf() {
    parentFragmentManager.inTransaction {
        remove(this@removeSelf)
    }
}

fun Fragment.removeSelfNoAnimation() {
    parentFragmentManager.inTransaction(isAnim = false) {
        remove(this@removeSelfNoAnimation)
    }
}

fun Fragment.hide() {
    requireActivity().supportFragmentManager.inTransaction {
        if (this@hide.isAdded) hide(this@hide)
    }
}

fun Fragment.backStackPress() {
    val manager = requireActivity().supportFragmentManager
    if (manager.backStackEntryCount >= 1) {
        manager.popBackStack()
    } else {
        return
    }
}

fun AppCompatActivity.backStackPress() {
    val manager = supportFragmentManager
    if (manager.backStackEntryCount >= 1) {
        manager.popBackStack()
    } else {
        return
    }
}

fun Fragment.backToFragment(fragment: Fragment) {
    val manager = requireActivity().supportFragmentManager
    if (manager.backStackEntryCount >= 1) {
        for (i in manager.backStackEntryCount - 1 downTo 1) {
            if (manager.getBackStackEntryAt(i).name == fragment.javaClass.simpleName) {
                manager.popBackStack()
            } else {
                break
            }
        }
    } else {
        return
    }
}
