package kz.codingOnTheMoon.codeassistlibgdxtemplatev1.android

import dev.ide.model.FacetCodecRegistry
import dev.ide.model.ModuleTypeRegistry
import dev.ide.platform.PluginId
import kz.codingOnTheMoon.codeassistlibgdxtemplatev1.android.AndroidAppModuleType
import kz.codingOnTheMoon.codeassistlibgdxtemplatev1.android.AndroidFacetCodec
import kz.codingOnTheMoon.codeassistlibgdxtemplatev1.android.AndroidLibModuleType

object LibGDXSupport {
    fun register(moduleTypes: ModuleTypeRegistry, codecs: FacetCodecRegistry, pluginId: PluginId) {
        moduleTypes.register(AndroidAppModuleType, pluginId)
        moduleTypes.register(AndroidLibModuleType, pluginId)
        codecs.register(AndroidFacetCodec)
    }
}
