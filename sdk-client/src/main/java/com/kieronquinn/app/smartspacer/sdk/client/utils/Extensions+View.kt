package com.kieronquinn.app.smartspacer.sdk.client.utils

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.annotation.RestrictTo
import com.kieronquinn.app.smartspacer.sdk.client.views.base.SmartspacerBasePageView.SmartspaceTargetInteractionListener
import com.kieronquinn.app.smartspacer.sdk.client.views.base.SmartspacerBasePageView.SmartspaceTargetInteractionListener.Companion.launchAction
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.utils.sendSafely

@RestrictTo(RestrictTo.Scope.LIBRARY)
fun View.setOnClick(
    target: SmartspaceTarget,
    action: TapAction?,
    interactionListener: SmartspaceTargetInteractionListener?
) {
    setOnLongClickListener {
        interactionListener?.onLongPress(target)
        false
    }
    if(action == null) return
    setOnClickListener {
        val launch = when {
            action.intent != null -> {
                {
                    try {
                        context.startActivity(action.intent?.apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    } catch (e: Exception) {
                        Log.e("Smartspacer", "Error firing intent", e)
                        interactionListener?.onInteraction(target, action.id.toString())
                    }
                }
            }
            action.pendingIntent != null -> {
                {
                    try {
                        val pendingIntent = action.pendingIntent
                        if(interactionListener?.shouldTrampolineLaunches() == true
                            && pendingIntent != null) {
                            interactionListener.trampolineLaunch(pendingIntent)
                        }else{
                            action.pendingIntent?.sendSafely()
                        }
                    } catch (e: Exception) {
                        interactionListener?.onInteraction(target, action.id.toString())
                    }
                }
            }
            else -> null
        }
        if(launch != null) {
            interactionListener.launchAction(!action.shouldShowOnLockScreen) { launch() }
        }
        interactionListener?.onInteraction(target, action.id.toString())
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY)
fun View.setOnClick(
    target: SmartspaceTarget,
    action: SmartspaceAction?,
    interactionListener: SmartspaceTargetInteractionListener?
) {
    setOnLongClickListener {
        interactionListener?.onLongPress(target)
        false
    }
    if(action == null) return
    setOnClickListener {
        val launch = when {
            action.intent != null -> {
                {
                    try {
                        context.startActivity(action.intent?.apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    } catch (e: Exception) {
                        Log.e("Smartspacer", "Error firing intent", e)
                        interactionListener?.onInteraction(target, action.id)
                    }
                }
            }
            action.pendingIntent != null && !action.skipPendingIntent -> {
                {
                    try {
                        val pendingIntent = action.pendingIntent
                        if(interactionListener?.shouldTrampolineLaunches() == true
                            && pendingIntent != null) {
                            interactionListener.trampolineLaunch(pendingIntent)
                        }else{
                            action.pendingIntent?.sendSafely()
                        }
                    } catch (e: Exception) {
                        interactionListener?.onInteraction(target, action.id)
                    }
                }
            }
            else -> null
        }
        if(launch != null) {
            interactionListener.launchAction(!action.launchDisplayOnLockScreen) { launch() }
        }
        interactionListener?.onInteraction(target, action.id)
    }
}

fun OnAttachStateChangeListener(callback: (isAttached: Boolean) -> Unit) = object : View.OnAttachStateChangeListener {
    override fun onViewAttachedToWindow(v: View) = callback(true)
    override fun onViewDetachedFromWindow(v: View) = callback(false)
}

fun View.observeAttachedState(callback: (isAttached: Boolean) -> Unit): () -> Unit {
    var wasAttached = false
    val listener = OnAttachStateChangeListener { isAttached ->
        if (wasAttached != isAttached) {
            wasAttached = isAttached
            callback(isAttached)
        }
    }
    addOnAttachStateChangeListener(listener)
    if (isAttachedToWindow) {
        listener.onViewAttachedToWindow(this)
    }
    return { removeOnAttachStateChangeListener(listener) }
}
