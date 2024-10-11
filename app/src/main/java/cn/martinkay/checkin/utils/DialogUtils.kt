package cn.martinkay.autocheckin.utils

import android.content.Context
import android.content.DialogInterface.OnClickListener
import android.view.LayoutInflater
import android.view.View
import cn.martinkay.autocheckinplugin.R
import cn.martinkay.autocheckinplugin.databinding.LayoutDialogProgressBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.jetbrains.annotations.Contract

object DialogUtils {

  @JvmStatic
  @JvmOverloads
  fun newProgressDialog(
    context: Context,
    title: String,
    message: String? = null,
    cancelable: Boolean = false,
    onCancelClick: OnClickListener? = null
  ): MaterialAlertDialogBuilder {
    val binding = LayoutDialogProgressBinding.inflate(LayoutInflater.from(context))
    val builder = newMaterialDialogBuilder(context)
    builder.setTitle(title)
    builder.setView(binding.root)
    builder.setCancelable(cancelable)

    if (message != null) {
      binding.message.text = message
      binding.message.visibility = View.VISIBLE
    }

    if (onCancelClick != null) {
      builder.setPositiveButton(android.R.string.cancel) { dialog, which ->
        dialog.dismiss()
        onCancelClick.onClick(dialog, which)
      }
    }

    return builder
  }

  /**
   * Create a new alert dialog with two buttons: <span>Yes</span> and <span>No</span>. This method
   * simply calls [.newYesNoDialog] with default values for title and message.
   *
   * @param context The context for the dialog.
   * @param positiveClickListener A listener that will be invoked on the <span>Yes</span> button
   * click.
   * @param negativeClickListener A listener that will be invoked on the <span>No</span> button
   * click.
   * @return The newly created dialog.
   */
  @JvmStatic
  @JvmOverloads
  fun newYesNoDialog(
    context: Context,
    positiveClickListener: OnClickListener? = null,
    negativeClickListener: OnClickListener? = null
  ): MaterialAlertDialogBuilder {
    return newYesNoDialog(
      context,
      context.getString(R.string.msg_yesno_def_title),
      context.getString(R.string.msg_yesno_def_message),
      positiveClickListener,
      negativeClickListener
    )
  }

  /**
   * Create a new alert dialog with two buttons: <span>Yes</span> and <span>No</span>.
   *
   * @param context The context for the dialog.
   * @param title The title of the dialog.
   * @param message The message of the dialog.
   * @param positiveClickListener A listener that will be invoked on the <span>Yes</span> button
   * click.
   * @param negativeClickListener A listener that will be invoked on the <span>No</span> button
   * click.
   * @return The newly created dialog instance.
   */
  @JvmStatic
  @JvmOverloads
  fun newYesNoDialog(
    context: Context,
    title: String,
    message: String? = null,
    positiveClickListener: OnClickListener? = null,
    negativeClickListener: OnClickListener? = null
  ): MaterialAlertDialogBuilder {
    val builder = newMaterialDialogBuilder(context)
    builder.setTitle(title)
    builder.setMessage(message)
    builder.setPositiveButton(R.string.yes, positiveClickListener)
    builder.setNegativeButton(R.string.no, negativeClickListener)
    return builder
  }

  /**
   * Creates a new MaterialAlertDialogBuilder with the app's default style.
   *
   * @param context The context for the dialog builder.
   * @return The new MaterialAlertDialogBuilder instance.
   */
  @JvmStatic
  @Contract("_ -> new")
  fun newMaterialDialogBuilder(context: Context): MaterialAlertDialogBuilder {
    return MaterialAlertDialogBuilder(context, R.style.AppTheme_MaterialAlertDialog)
  }
}
