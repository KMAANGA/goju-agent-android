package com.maangatech.gojuagent.core.ussd.accessibility

import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Reads and drives the system USSD-response dialog's node tree. Isolated from
 * [UssdAccessibilityService] so the (fiddly, OEM-specific) node-walking logic has a single,
 * clearly-named home — the service itself only wires Android lifecycle callbacks to this.
 */
object UssdDialogInteractor {

    /** Concatenates every text-bearing descendant node — USSD dialogs are single-purpose, so order rarely matters. */
    fun extractDialogText(root: AccessibilityNodeInfo): String {
        val builder = StringBuilder()
        collectText(root, builder)
        return builder.toString().trim()
    }

    private fun collectText(node: AccessibilityNodeInfo, builder: StringBuilder) {
        node.text?.let { if (it.isNotBlank()) builder.append(it).append('\n') }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            try {
                collectText(child, builder)
            } finally {
                child.recycleCompat()
            }
        }
    }

    /** Finds the single-line input field the dialog uses for USSD responses (there's ever only one). */
    fun findInputField(root: AccessibilityNodeInfo): AccessibilityNodeInfo? =
        findFirst(root) { it.isEditable || it.className == "android.widget.EditText" }

    /** Finds the affirmative action button — labels vary by locale/OEM ("SEND", "OK", "Tuma"). */
    fun findSubmitButton(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val labels = setOf("send", "ok", "tuma", "yes", "submit")
        return findFirst(root) { node ->
            node.isClickable && node.className == "android.widget.Button" &&
                node.text?.toString()?.trim()?.lowercase() in labels
        } ?: findFirst(root) { it.isClickable && it.className == "android.widget.Button" }
    }

    fun findCancelButton(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val labels = setOf("cancel", "ghairi")
        return findFirst(root) { node ->
            node.isClickable && node.className == "android.widget.Button" &&
                node.text?.toString()?.trim()?.lowercase() in labels
        }
    }

    /** Sets the input field's text without opening the soft keyboard (`ACTION_SET_TEXT`). */
    fun submitInput(root: AccessibilityNodeInfo, text: String): Boolean {
        val field = findInputField(root) ?: return false
        val arguments = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        val textSet = field.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        if (!textSet) return false

        val button = findSubmitButton(root) ?: return false
        return button.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    fun cancelDialog(root: AccessibilityNodeInfo): Boolean =
        findCancelButton(root)?.performAction(AccessibilityNodeInfo.ACTION_CLICK) ?: false

    private inline fun findFirst(
        node: AccessibilityNodeInfo,
        predicate: (AccessibilityNodeInfo) -> Boolean,
    ): AccessibilityNodeInfo? {
        if (predicate(node)) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findFirst(child, predicate)
            if (found != null) return found
        }
        return null
    }

    @Suppress("DEPRECATION")
    private fun AccessibilityNodeInfo.recycleCompat() {
        // recycle() is a no-op on API 33+ and deprecated, but still required pre-33 to avoid
        // exhausting the finite AccessibilityNodeInfo pool during a long menu-walk session.
        if (android.os.Build.VERSION.SDK_INT < 33) recycle()
    }
}
