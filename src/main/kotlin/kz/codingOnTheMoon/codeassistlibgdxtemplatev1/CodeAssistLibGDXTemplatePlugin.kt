package kz.codingOnTheMoon.codeassistlibgdxtemplatev1

import dev.ide.model.ProjectTemplateRegistry
import dev.ide.plugin.Plugin
import dev.ide.plugin.PluginRegistration
import kz.codingOnTheMoon.codeassistlibgdxtemplatev1.template.CodeAssistLibGDXTemplate

/**
 * The entry point named by `res/raw/codeassist_plugin.toml`. The IDE instantiates it off the installed APK
 * with its own classloader as the parent, so every SPI type below binds to the IDE's copy.
 *
 * There is no manifest to declare here: that TOML is this plugin's identity, and the IDE reads it before any
 * of this code runs.
 *
 * [register] runs once, at IDE startup, after every plugin this one lists in `dependsOn`. Everything it
 * contributes is tracked and removed automatically if the plugin is unloaded.
 */
class CodeAssistLibGDXTemplatePlugin() : Plugin {

    override fun register(reg: PluginRegistration) {
        reg.contributeVia { extensionPoint, pluginId ->
            ProjectTemplateRegistry(extensionPoint).register(CodeAssistLibGDXTemplate,pluginId)
        }
    }
}