package pro.bravit.fleetPlugin.funCounter

import fleet.api.workspace.document.MediaType
import fleet.ast.AST
import fleet.ast.children
import fleet.common.document.EditorEntity
import fleet.common.document.mediaType
import fleet.frontend.actions.FleetDataKeys
import fleet.frontend.actions.actions
import fleet.frontend.editor.layout
import fleet.frontend.editor.ownerTab
import fleet.frontend.lang.ASTContainer
import fleet.frontend.lang.syntaxes
import fleet.frontend.navigation.displayName
import fleet.frontend.notifications.*
import fleet.kernel.cascadeDelete
import fleet.kernel.change
import fleet.kernel.plugins.*
import fleet.kernel.rete.*
import fleet.kernel.withEntities
import kotlinx.coroutines.launch
import noria.model.ActionRegistrar

typealias FunCounterAPI = Unit
class FunCounter : Plugin<FunCounterAPI> {
    companion object : Plugin.Key<FunCounterAPI>
    override val key: Plugin.Key<FunCounterAPI> = FunCounter

    override fun ContributionScope.load(pluginScope: PluginScope) {
        notificationCategory(countFunctionsNotification)
        actions {
            setupCountFunctionsAction(pluginScope)
        }
    }
}

val countFunctionsNotification = NotificationCategory(
    id = NotificationCategoryId("CountFunctions"),
    readableName = "Count Functions"
)

private suspend fun createCountFunctionsNotification(editor: EditorEntity): NotificationEntity {
    val fileName = editor.layout?.ownerTab()?.displayName() ?: "Unknown file"
    val title = "Function counter ($fileName)"
    val description = "Number of top-level functions: ?"
    return change {
        val notification = showNotification(
            countFunctionsNotification,
            title, NotificationIcon.Info, description,
            isSticky = true
        )
        cascadeDelete(editor, notification)
        notification
    }
}

private suspend fun updateCountFunctionsNotification(notification: NotificationEntity,
                                                    numberOfFunctions: Int) {
    val description = "Number of top-level functions: $numberOfFunctions"
    change {
        notification.description = description
    }
}

private fun ActionRegistrar.setupCountFunctionsAction(pluginScope: PluginScope) {
    action(id = "Count-Functions", name = "Count Functions") {
        val requiredEditor = required(FleetDataKeys.LastEditorEntity)
        dynamic {
            val editor = requiredEditor.value
            if (editor.document.mediaType == MediaType("text", "kotlin")) {
                callback {
                    pluginScope.launch {
                        performCountFunctionsAction(editor)
                    }
                }
            }
        }
    }
}

private suspend fun performCountFunctionsAction(editor: EditorEntity) {
    withEntities(editor) {
        val notification = createCountFunctionsNotification(editor)
        withEntities(notification) {
            query {
                editor.document.syntaxes?.firstNotNullOfOrNull(ASTContainer::getDataAsync)
            }.collectLatest { ast ->
                val numberOfFunctions = countFunctions(ast?.await())
                updateCountFunctionsNotification(notification, numberOfFunctions)
            }
        }
    }
}

private fun countFunctions(tree: AST<*>?): Int =
    tree?.root()
        ?.children()
        ?.count {
            it.type.toString() == "FUN"
        } ?: 0