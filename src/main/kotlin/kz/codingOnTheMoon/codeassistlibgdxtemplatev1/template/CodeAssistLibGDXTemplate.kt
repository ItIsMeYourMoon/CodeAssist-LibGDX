package kz.codingOnTheMoon.codeassistlibgdxtemplatev1.template

import dev.ide.model.BuildSystemId
import dev.ide.model.FacetData
import dev.ide.model.template.ProjectScaffold
import dev.ide.model.template.ProjectTemplate
import dev.ide.model.template.TemplateArgs
import dev.ide.model.template.TemplateCategory
import dev.ide.model.template.TemplateDependency
import dev.ide.model.template.TemplateId
import dev.ide.model.template.TemplateParameter
import kz.codingOnTheMoon.codeassistlibgdxtemplatev1.android.AndroidApiLevels
import kz.codingOnTheMoon.codeassistlibgdxtemplatev1.android.AndroidAppAssets

/*
Inspired by:
https://github.com/tyron12233/CodeAssist/blob/main/docs%2Fcustom-project-templates.md 

Comments mainly left untouched on purpose
*/

/**
 * A native Android application: one `app` module (android-app) with an `AndroidFacet`, an editable
 * `AndroidManifest.xml`, `res/` (strings, colors, theme, and an `activity_main` layout), and a
 * `MainActivity` that inflates that layout to show a "Hello, World!" page. A complete, dependency-free
 * starter app that assembles to a signed APK through the existing `AndroidBuildSystem` pipeline.
 */

object CodeAssistLibGDXTemplate : ProjectTemplate {

    override val id = TemplateId("codeassist-libgdxt")

    override val displayName: String = "CodeAssist LibGDX"

    override val description: String = "LibGDX template for CodeAssist"

    override val category = TemplateCategory.ANDROID

    override val iconId: String = "module.android"
    
    private const val MODULE = "app"
    
    override fun parameters(): List<TemplateParameter> = listOf(
        TemplateSupport.minSdkParam,
        TemplateSupport.targetSdkParam
    )
    override fun dependencies(args: TemplateArgs): List<TemplateDependency> {
        return listOf(
            TemplateDependency(MODULE, TemplateSupport.GDX_COORDINATE),
            TemplateDependency(MODULE, TemplateSupport.GDX_BACKEND_ANDROID_COORDINATE),
            TemplateDependency(MODULE, TemplateSupport.GDX_BOX_COORDINATE)
        )
    }

    override fun generate(scaffold: ProjectScaffold, args: TemplateArgs) {
        val pkg = args.packageName
        val minSdk = args.int("minSdk", 26).toLong()
        val targetSDK = args.int("targetSdk", 36).toLong()
        scaffold.workspace.beginModification().apply {
            addProject(args.name, BuildSystemId.NATIVE, scaffold.rootDir)
            commit()
        }
        scaffold.workspace.projects.first {
            it.name == args.name
        }.beginModification().apply {
            addModule(
               MODULE, 
               scaffold.moduleType("android-app")).apply {
                   languageLevel = languageLevel
                   putFacetData(
                       FacetData(
                           tomlTable = "android",
                           values = mapOf(
                               "namespace" to pkg,
                               "compileSdk" to 36L,
                               "minSdk" to minSdk,
                               "targetSdk" to targetSDK,
                               "isApplication" to true
                           )
                       )
                   )
               }
            commit()
        }

        val path = TemplateSupport.pkgPath(pkg)
        scaffold.writeText(
            "app/src/main/AndroidManifest.xml",
            TemplateContent.manifest(pkg)
        )
        scaffold.writeText(
            "app/src/main/res/values/strings.xml",
            TemplateContent.strings(args.name)
        )
        scaffold.writeText(
            "app/src/main/res/values/colors.xml",
            TemplateContent.colors()
        )
        scaffold.writeText("app/src/main/res/values/themes.xml", AndroidAppAssets.themesXml)
        scaffold.writeText("app/src/main/res/values-night/themes.xml", AndroidAppAssets.themesNightXml)
        for ((rel, content) in AndroidAppAssets.launcherIconResFiles) {
            scaffold.writeText("app/src/main/res/$rel", content)
        }
        
        scaffold.writeText(
            "app/src/main/java/$path/MainActivity.java",
            TemplateContent.mainActivity(pkg)
        )

        scaffold.writeText(
            relPath = "app/src/main/java/$path/MyGdxGame.java",
            content = TemplateContent.myGdxGame(pkg)
        )
        /*
        So we're now copying libgdx.so from this template plugin into newly created LibGDX project's jniLibs folder
        Keep in mind that we're injecting one for 64, one for 32 and one for x86_64. If you need others to just get .so files somewhere else and put them there accordingly 
        Also pay attention that their versions are 1.14.2 and if you want to update them them in the feature just replace them with newer .so files
        */
        val platform64SoBytes = readResourceBytes("/templates/gdx-platform-1.14.2-natives-arm64-v8a.so")
        val platform32SoBytes = readResourceBytes("/templates/gdx-platform-1.14.2-natives-armeabi-v7a.so")
        val platformx86_64SoBytes = readResourceBytes("/templates/gdx-platform-1.14.2-natives-x86_64.so")
        /*
        We're aslo copying an image from plugin template into newly created LibGDX project. It'll use it later 
        */
        val androidJpgBytes = readResourceBytes("/templates/android.jpg")
        
        scaffold.writeBytes(
            relPath = "app/src/main/assets/android.jpg",
            bytes = androidJpgBytes
        )
        scaffold.writeBytes(
            relPath = "app/src/main/jniLibs/arm64-v8a/libgdx.so",
            bytes = platform64SoBytes
        )
        
        scaffold.writeBytes(
            relPath = "app/src/main/jniLibs/x86_64/libgdx.so",
            bytes = platformx86_64SoBytes
        )
        scaffold.writeBytes(
            relPath = "app/src/main/jniLibs/armeabi-v7a/libgdx.so",
            bytes = platform32SoBytes
        )
    }

    private fun readResourceBytes(resourcePath: String): ByteArray =
        javaClass.getResourceAsStream(resourcePath)?.use { it.readBytes() }
            ?: error("Bundled resource not found: $resourcePath")
}

internal object TemplateSupport {

    fun pkgPath(pkg: String): String = pkg.replace('.', '/')

    private fun options(levels: List<AndroidApiLevels.Level>) =
    levels.map { TemplateParameter.Choice.Option(it.api.toString(), it.label) }

    /** The minSdk picker offered by both Android templates, defaulting to the level new modules use. */
    val minSdkParam = TemplateParameter.Choice(
        key = "minSdk",
        label = "Minimum SDK",
        options = options(AndroidApiLevels.MIN_SDK_LEVELS),
        defaultIndex = AndroidApiLevels.MIN_SDK_LEVELS.indexOfFirst { it.api == AndroidApiLevels.DEFAULT_MIN_SDK },
        help = "Lowest Android version the game supports.",
    )

    /** The targetSdk picker: the API level the app is tested/optimised against, newest by default (Play
     *  requires a current target, and an old one opts the app into compatibility behaviour). */
    val targetSdkParam = TemplateParameter.Choice(
        key = "targetSdk",
        label = "Target SDK",
        options = options(AndroidApiLevels.TARGET_SDK_LEVELS),
        defaultIndex = AndroidApiLevels.TARGET_SDK_LEVELS.lastIndex,
        help = "The API level the app is built and optimised against.",
    )

    /** What every built-in template compiles against: the newest level the IDE ships support for. */
    const val COMPILE_SDK = AndroidApiLevels.LATEST

    /*LibGDX dependencies*/
    private const val GDX = "1.14.2"
    
    val GDX_COORDINATE = "com.badlogicgames.gdx:gdx:$GDX"
    val GDX_BACKEND_ANDROID_COORDINATE = "com.badlogicgames.gdx:gdx-backend-android:$GDX"
    val GDX_BOX_COORDINATE = "com.badlogicgames.gdx:gdx-box2d:$GDX"   
}
