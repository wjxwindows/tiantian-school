package com.tiantian.school.ui.nav

import android.net.Uri

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"

    const val PAPER_DETAIL = "paper/{code}"
    const val MEMBERSHIP = "membership"
    const val TIANCOIN = "tiancoin"
    const val INVENTORY = "inventory"
    const val APPEAL = "appeal"
    const val SETTINGS = "settings"
    const val SCHOOL = "school"
    const val PROFILE_EDIT = "profile_edit"
    const val DEVICES = "devices"

    // ---------- 家长端 ----------
    const val PARENT_MAIN = "parent_main"
    const val ADD_CHILD = "add_child"
    const val CHILD_DETAIL = "child_detail/{childId}"

    // ---------- 孩子端 ----------
    const val BIND_PARENT = "bind_parent"
    const val GUARD_LOCK = "guard_lock"

    const val ARG_CODE = "code"
    const val ARG_CHILD_ID = "childId"

    fun paperDetail(code: String): String = "paper/${Uri.encode(code)}"

    fun childDetail(childId: String): String = "child_detail/${Uri.encode(childId)}"
}
