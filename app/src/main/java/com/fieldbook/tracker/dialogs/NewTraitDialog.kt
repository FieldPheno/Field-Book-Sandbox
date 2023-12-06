package com.fieldbook.tracker.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.fieldbook.tracker.R
import com.fieldbook.tracker.activities.CollectActivity
import com.fieldbook.tracker.activities.TraitEditorActivity
import com.fieldbook.tracker.adapters.TraitFormatAdapter
import com.fieldbook.tracker.database.DataHelper
import com.fieldbook.tracker.objects.TraitObject
import com.fieldbook.tracker.offbeat.traits.formats.Formats
import com.fieldbook.tracker.offbeat.traits.formats.TraitFormatParametersAdapter
import com.fieldbook.tracker.offbeat.traits.formats.ValidationResult
import com.fieldbook.tracker.offbeat.traits.formats.ui.ParameterScrollView
import com.fieldbook.tracker.preferences.GeneralKeys
import com.fieldbook.tracker.utilities.DialogUtils
import com.fieldbook.tracker.utilities.SoundHelperImpl
import com.fieldbook.tracker.utilities.VibrateUtil
import dagger.hilt.android.AndroidEntryPoint
import org.phenoapps.utils.SoftKeyboardUtil
import javax.inject.Inject

@AndroidEntryPoint
class NewTraitDialog(
    private val activity: Activity,
    private val onNewTraitDialogDismiss: () -> Unit
) :
    DialogFragment(),
    TraitFormatAdapter.FormatSelectionListener,
    TraitFormatParametersAdapter.TraitFormatAdapterController {

    @Inject
    lateinit var soundHelperImpl: SoundHelperImpl

    @Inject
    lateinit var vibrator: VibrateUtil

    @Inject
    lateinit var prefs: SharedPreferences

    @Inject
    lateinit var database: DataHelper

    // UI elements of new trait dialog
    private lateinit var traitFormatsRv: RecyclerView
    private lateinit var parametersSv: ParameterScrollView
    private lateinit var variableEditableErrorTv: TextView

    private var negativeBtn: Button? = null
    private var positiveBtn: Button? = null

    //holds the trait objects sent from trait editor activity
    private var initialTraitObject: TraitObject? = null

    //when editing this tracks the original object, to see if values changed when discarding
    private var originalInitialTraitObject: TraitObject? = null

    //private var createVisible: Boolean
    private var brapiDialogShown = false

    init {
        setBrAPIDialogShown((activity as TraitEditorActivity).brAPIDialogShown)
    }

    fun setTraitObject(traitObject: TraitObject?) {

        initialTraitObject = traitObject?.clone()
        originalInitialTraitObject = traitObject?.clone()

    }

    override fun onStart() {
        super.onStart()

        positiveBtn = (dialog as AlertDialog?)?.getButton(AlertDialog.BUTTON_POSITIVE)
        negativeBtn = (dialog as AlertDialog?)?.getButton(AlertDialog.BUTTON_NEGATIVE)

        /**
         * EditText's inside a dialog fragment need certain window flags to be cleared
         * for the software keyboard to show.
         */
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)

        //following stretches dialog a bit for more pixel real estate
        val params = dialog?.window?.attributes
        params?.width = LinearLayout.LayoutParams.MATCH_PARENT
        params?.height = LinearLayout.LayoutParams.WRAP_CONTENT
        dialog?.window?.attributes = params

        context?.let { ctx ->
            show(ctx)
        }
    }

    private fun showFormatLayouts() {

        dialog?.setTitle(context?.getString(R.string.trait_creator_title_layout))

        traitFormatsRv.visibility = View.VISIBLE
        parametersSv.visibility = View.GONE

        negativeBtn?.setText(R.string.dialog_cancel)
        negativeBtn?.setOnClickListener {
            dismiss()
        }

        positiveBtn?.setText(R.string.next)
        positiveBtn?.setOnClickListener {
            showFormatParameters()
            if (context?.let { ctx -> getSelectedFormat(ctx) } == null) {
                Toast.makeText(
                    context,
                    R.string.dialog_new_trait_error_must_select_a_layout,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        if (traitFormatsRv.adapter == null) {

            context?.let { ctx ->

                setupTraitFormatsRv(getSelectedFormat(ctx))

            }
        }
    }

    private fun showFormatParameters(format: Formats) {

        traitFormatsRv.visibility = View.GONE
        parametersSv.visibility = View.VISIBLE

        //if editing a variable and observations exist, don't allow the format to change
        var observationsExist = false
        if (initialTraitObject != null) {
            initialTraitObject?.id?.let { traitDbId ->
                observationsExist = database.getAllObservationsOfVariable(traitDbId).isNotEmpty()
                variableEditableErrorTv.visibility =
                    if (observationsExist) View.VISIBLE else View.GONE
            }
        }

        if (initialTraitObject == null || !observationsExist) {

            negativeBtn?.setText(R.string.dialog_back)
            negativeBtn?.setOnClickListener {
                //close keyboard programmatically
                SoftKeyboardUtil.closeKeyboard(context, traitFormatsRv, 1L)

                if (observationsExist) {
                    onCancel()
                }

                showFormatLayouts()
            }

        } else {

            negativeBtn?.setText(R.string.dialog_cancel)
            negativeBtn?.setOnClickListener {
                onCancel()
            }

        }

        positiveBtn?.setText(R.string.dialog_save)
        positiveBtn?.setOnClickListener {
            onSave()
        }

        setupParametersLinearLayout()

        context?.let { ctx ->
            dialog?.setTitle(
                ctx.getString(
                    R.string.trait_creator_parameters_title,
                    format.getName(ctx)
                )
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val view = layoutInflater.inflate(R.layout.dialog_trait_creator, null)

        val builder = AlertDialog.Builder(
            activity,
            R.style.AppAlertDialog
        )

        builder.setTitle(R.string.trait_creator_title_layout)
            .setCancelable(true)
            .setView(view)

        builder.setPositiveButton(R.string.next) { _, _ -> }
        builder.setNegativeButton(R.string.dialog_cancel) { _, _ -> }

        traitFormatsRv = view.findViewById(R.id.dialog_new_trait_formats_rv)
        parametersSv = view.findViewById(R.id.dialog_new_trait_parameters_psv)
        variableEditableErrorTv =
            view.findViewById(R.id.dialog_new_trait_variable_editable_error_tv)

        return builder.create()
    }

    private fun show(ctx: Context) {
        if (initialTraitObject == null) showFormatLayouts() else showFormatParameters(
            Formats.values().first {
                initialTraitObject?.format == it.getDatabaseName(ctx)
            }
        )
    }

    private fun getSelectedFormat(ctx: Context): Formats? =
        Formats.values().find { it.getDatabaseName(ctx) == initialTraitObject?.format }
            ?: (traitFormatsRv.adapter as? TraitFormatAdapter)?.selectedFormat

    private fun setupParametersLinearLayout() {

        parametersSv.clear()

        context?.let { ctx ->

            val format = getSelectedFormat(ctx)

            format?.getTraitFormatDefinition()?.parameters?.forEach { parameter ->

                parameter.createViewHolder(parametersSv)?.let { holder ->

                    holder.bind(parameter, initialTraitObject)

                    parametersSv.addViewHolder(holder)
                }
            }
        }
    }

    private fun setupTraitFormatsRv(format: Formats?) {

        context?.let { ctx ->

            val formatsAdapter = TraitFormatAdapter(ctx, this)

            formatsAdapter.selectedFormat = format

            traitFormatsRv.adapter = formatsAdapter

            formatsAdapter.submitList(Formats.values().toList())

        }
    }

    private fun onSave() {

        var pass = true

        if (validateParameters().result != true) pass = false

        if (pass && initialTraitObject == null) {

            if (validateFormat().result != true) {

                pass = false

            } else {

                val pos: Int = database.maxPositionFromTraits + 1

                val t = createTraitObjectFromUi()

                t.realPosition = pos

                database.insertTraits(t)

                onSaveFinish()
            }

        } else if (pass) {

            initialTraitObject?.let { traitObject ->

                if (validateFormat().result != true) {

                    pass = false

                } else {

                    val t = updateInitialTraitObjectFromUi(traitObject)

                    updateDatabaseTrait(t)

                    onSaveFinish()
                }
            }
        }

        if (!pass) {

            vibrator.vibrate()

            soundHelperImpl.playError()
        }
    }

    private fun onSaveFinish() {

        val ed = this.prefs.edit()
        ed.putBoolean(GeneralKeys.TRAITS_EXPORTED, false)
        ed.apply()

        // Display our BrAPI dialog if it has not been show already
        // Get our dialog state from our adapter to see if a trait has been selected
        setBrAPIDialogShown((activity as TraitEditorActivity).adapter.infoDialogShown)
        if (!brapiDialogShown) {
            setBrAPIDialogShown(
                activity.displayBrapiInfo(activity, null, true)
            )
        }

        onNewTraitDialogDismiss()

        CollectActivity.reloadData = true

        soundHelperImpl.playCelebrate()

        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        initialTraitObject = null
        originalInitialTraitObject = null

        parametersSv.clear()
        traitFormatsRv.adapter = null
    }

    private fun onCancel() {

        initialTraitObject?.let { traitObject ->

            val t = updateInitialTraitObjectFromUi(traitObject)

            if (t != originalInitialTraitObject) {

                askUserToVerifyDismiss()

            } else dismiss()
        }
    }

    private fun askUserToVerifyDismiss() {

        val builder = AlertDialog.Builder(activity, R.style.AppAlertDialog)

        builder.setTitle(activity.getString(R.string.dialog_close))

        builder.setMessage(activity.getString(R.string.dialog_confirm))

        builder.setPositiveButton(activity.getString(R.string.dialog_yes)) { dialog, _ ->
            dialog.dismiss()
            dismiss()
        }

        builder.setNegativeButton(activity.getString(R.string.dialog_no)) { dialog, _ ->
            dialog.dismiss()
        }

        val alert = builder.create()

        alert.show()

        DialogUtils.styleDialogs(alert)
    }

    /**
     * Takes a trait object (initialTraitObject) and loads parameters from UI into it.
     */
    private fun updateInitialTraitObjectFromUi(traitObject: TraitObject): TraitObject {

        return parametersSv.merge(traitObject)
    }

    /**
     * Returns a new trait object based on the UI parameters
     */
    private fun createTraitObjectFromUi(): TraitObject {

        val index = (traitFormatsRv.adapter as TraitFormatAdapter).selectedFormat?.ordinal
            ?: Formats.TEXT.ordinal
        val format = Formats.values()[index]
        var t = TraitObject()
        t.format = format.getDatabaseName(activity)

        t = parametersSv.merge(t)

        t.visible = true
        t.traitDataSource = "local"

        return t
    }

    /**
     * The trait object passed should already have UI loaded values.
     * Simply pass these to the DataHelper editTraits function to call SQL update.
     */
    private fun updateDatabaseTrait(traitObject: TraitObject) {

        database.editTraits(
            traitObject.id,
            traitObject.name,
            traitObject.format,
            traitObject.defaultValue,
            traitObject.minimum,
            traitObject.maximum,
            traitObject.details,
            traitObject.categories
        )
    }

    private fun validateFormat(): ValidationResult {

        context?.let { ctx ->
            getSelectedFormat(ctx)?.let { selectedFormat ->
                return parametersSv.validateFormat(selectedFormat)
            }
        }

        return ValidationResult()
    }

    private fun validateParameters(): ValidationResult {

        return parametersSv.validateParameters(
            database = database,
            initialTraitObject = initialTraitObject
        )
    }

    // when this value changes in this class,
    // the value in TraitEditorActivity must change
    private fun setBrAPIDialogShown(b: Boolean) {
        brapiDialogShown = b
        (activity as TraitEditorActivity).brAPIDialogShown = b
    }

    private fun showFormatParameters() {

        val adapter = traitFormatsRv.adapter as? TraitFormatAdapter

        if (adapter?.selectedFormat != null) {

            context?.let { ctx ->

                initialTraitObject?.format = adapter.selectedFormat?.getDatabaseName(ctx)

                showFormatParameters(adapter.selectedFormat ?: Formats.TEXT)

            }
        }
    }

    override fun onSelected(format: Formats) {

        if (format == Formats.BRAPI) {

            dismiss()

            (activity as TraitEditorActivity).startBrapiTraitActivity()

        } else {

            showFormatParameters()

        }
    }
}