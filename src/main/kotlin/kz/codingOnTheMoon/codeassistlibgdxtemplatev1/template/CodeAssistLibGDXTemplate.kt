package kz.codingOnTheMoon.codeassistlibgdxtemplatev1.template

import dev.ide.model.BuildSystemId
import dev.ide.model.template.ProjectScaffold
import dev.ide.model.template.ProjectTemplate
import dev.ide.model.template.TemplateArgs
import dev.ide.model.template.TemplateCategory
import dev.ide.model.template.TemplateDependency
import dev.ide.model.template.TemplateId
import dev.ide.model.template.TemplateParameter
import kz.codingOnTheMoon.codeassistlibgdxtemplatev1.android.AndroidApiLevels
import kz.codingOnTheMoon.codeassistlibgdxtemplatev1.android.AndroidAppAssets
import kz.codingOnTheMoon.codeassistlibgdxtemplatev1.android.AndroidFacet

/*
Inspired by:
https://github.com/tyron12233/CodeAssist/blob/main/android-support%2Fsrc%2Fmain%2Fkotlin%2Fdev%2Fide%2Fandroid%2Fsupport%2Ftemplates%2FAndroidTemplates.kt 

Comments mainly left untouched on purpose
*/
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
        help = "Lowest Android version the app supports.",
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

    /** Source language for the generated starter code. */
    val languageParam = TemplateParameter.Choice(
        key = "language",
        label = "Language",
        options = listOf(
            TemplateParameter.Choice.Option("java", "Java")
        ),
        defaultIndex = 0,
        help = "Language of the starter source files.",
    )

    /** What every built-in template compiles against: the newest level the IDE ships support for. */
    const val COMPILE_SDK = AndroidApiLevels.LATEST

    /** Google's Material Components for Android — the library behind Material You theming + the FAB/Snackbar. */
    const val GDX_COORDINATE = "com.badlogicgames.gdx:gdx:1.14.2"
    const val GDX_BACKEND_ANDROID_COORDINATE = "com.badlogicgames.gdx:gdx-backend-android:1.14.2"
    const val GDX_BOX_COORDINATE = "com.badlogicgames.gdx:gdx-box2d:1.14.2"

    fun isKotlin(args: TemplateArgs): Boolean = args.string("language", "java").equals("kotlin", ignoreCase = true)

    /**
     * The module-relative ProGuard/R8 keep-rules file the `release` build type references by default
     * ([AndroidFacet.DEFAULT_BUILD_TYPES]). Written for new modules so that, when minification is enabled,
     * the entry resolves to a real file instead of being silently skipped. Comments only by default
     * (the bundled `proguard-android-optimize.txt` carries the framework keep rules); add app-specific rules here.
     */
    val PROGUARD_RULES_PRO: String = """
        # Add project-specific ProGuard/R8 keep rules here.
        # These are applied on top of the bundled defaults (proguard-android-optimize.txt) when the
        # build type has minifyEnabled = true.
        #
        # Keep a class that is referenced only by reflection / from XML, e.g.:
        # -keep class com.example.SomeClass { *; }
        #
        # Preserve line numbers for readable crash stack traces, then hide the original file name:
        # -keepattributes SourceFile,LineNumberTable
        # -renamesourcefileattribute SourceFile
    """.trimIndent() + "\n"
}

/**
 * A native Android application: one `app` module (android-app) with an `AndroidFacet`, an editable
 * `AndroidManifest.xml`, `res/` (strings, colors, theme, and an `activity_main` layout), and a
 * `MainActivity` that inflates that layout to show a "Hello, World!" page. A complete, dependency-free
 * starter app that assembles to a signed APK through the existing `AndroidBuildSystem` pipeline.
 */

object CodeAssistLibGDXTemplate : ProjectTemplate {

    override val id = TemplateId("codeassist-libgdxt")

    override val displayName: String = "CodeAssist LibGDX Template"

    override val description: String = "LibGDX template for CodeAssist"

    override val category = TemplateCategory.ANDROID

    override val iconId: String = "pkg"

    override fun parameters(): List<TemplateParameter> = listOf(
        TemplateSupport.languageParam,
        TemplateSupport.minSdkParam,
        TemplateSupport.targetSdkParam
    )
    override fun dependencies(args: TemplateArgs): List<TemplateDependency> {
        return listOf(
            TemplateDependency(module = "app", TemplateSupport.GDX_COORDINATE),
            TemplateDependency(module = "app", TemplateSupport.GDX_BACKEND_ANDROID_COORDINATE),
            TemplateDependency(module = "app", TemplateSupport.GDX_BOX_COORDINATE)
        )
    }

    override fun generate(scaffold: ProjectScaffold, args: TemplateArgs) {
        val pkg = args.packageName
        val minSdk = args.int("minSdk", 26)
        val targetSDK = args.int("targetSdk", TemplateSupport.COMPILE_SDK)
        val kotlin = false
        scaffold.workspace.beginModification().apply {
            addProject(args.name, BuildSystemId.NATIVE, scaffold.rootDir)
            commit()
        }
        scaffold.workspace.projects.first {
            it.name == args.name
        }.beginModification().apply {
            addModule("app", scaffold.moduleType("android-app")).apply {
                languageLevel = scaffold.languageLevel
                putFacet(
                    AndroidFacet(
                        namespace = pkg,
                        compileSdk = TemplateSupport.COMPILE_SDK,
                        minSdk = minSdk,
                        targetSdk = targetSDK
                    )
                )
            }
            commit()
        }

        val path = TemplateSupport.pkgPath(pkg)
        scaffold.writeText(
            "app/src/main/AndroidManifest.xml",
            """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="$pkg">
                <application
                    android:allowBackup="true"
                    android:icon="@mipmap/ic_launcher"
                    android:label="@string/app_name"
                    android:roundIcon="@mipmap/ic_launcher_round"
                    android:supportsRtl="true"
                    android:theme="@style/Theme.App">
                    <activity android:name=".MainActivity" android:exported="true">
                        <intent-filter>
                            <action android:name="android.intent.action.MAIN"/>
                            <category android:name="android.intent.category.LAUNCHER"/>
                        </intent-filter>
                    </activity>
                </application>
            </manifest>
            """,
        )
        scaffold.writeText(
            "app/src/main/res/values/strings.xml",
            """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <string name="app_name">${args.name}</string>
                <string name="hello_world">Hello, World!</string>
            </resources>
            """,
        )
        scaffold.writeText(
            "app/src/main/res/values/colors.xml",
            """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <color name="primary">#FF6200EE</color>
                <color name="on_primary">#FFFFFFFF</color>
                ${AndroidAppAssets.ICON_BACKGROUND_COLOR_XML}
            </resources>
            """,
        )
        scaffold.writeText("app/src/main/res/values/themes.xml", AndroidAppAssets.themesXml)
        scaffold.writeText("app/src/main/res/values-night/themes.xml", AndroidAppAssets.themesNightXml)
        for ((rel, content) in AndroidAppAssets.launcherIconResFiles) {
            scaffold.writeText("app/src/main/res/$rel", content)
        }
        
        scaffold.writeText(
            "app/src/main/java/$path/MainActivity.java",
            """
                package $pkg;

                import android.os.Bundle;
                import com.badlogic.gdx.backends.android.AndroidApplication;
                import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

                public class MainActivity extends AndroidApplication {
                    @Override
                    protected void onCreate(Bundle savedInstanceState) {
                        super.onCreate(savedInstanceState);
            
                        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
            
                        cfg.useAccelerometer = false; 
                        cfg.useCompass = false;
                        cfg.useImmersiveMode = true;
                        initialize(new MyGdxGame(), cfg);
                    }
                }
                """,
        )

        scaffold.writeText(
            relPath = "app/src/main/java/$path/MyGdxGame.java",
            content = """
                package $pkg;

                import com.badlogic.gdx.*;
                import com.badlogic.gdx.graphics.*;
                import com.badlogic.gdx.graphics.g2d.*;

                 public class MyGdxGame implements ApplicationListener{
                
                    	Texture texture;
                    	SpriteBatch batch;

                    	@Override
                    	public void create(){
                        	texture = new Texture(Gdx.files.internal("android.jpg"));
                        	batch = new SpriteBatch();
                    	}

                    	@Override
                    	public void render(){        
                        	Gdx.gl.glClearColor(1, 1, 1, 1);
                        	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                         batch.begin();
                        	batch.draw(texture, Gdx.graphics.getWidth() / 4, 0, 
                        	Gdx.graphics.getWidth() / 2, Gdx.graphics.getWidth() / 2);
                        	batch.end();
                    	}

                    	@Override
                    	public void dispose(){
                    	}

                    	@Override
                    	public void resize(int width, int height){
                    	}

                    	@Override
                    	public void pause()	{
                    	}

                    	@Override
                    	public void resume(){
                    	}
                }
            """
        )
        /*
        So we're now copying it from this template plugin into newly created LibGDX project's jniLibs folder
        Keep in mind that we're injecting one for 64 wnd one for 32 bit system. If you need others to just get .so files somewhere else and put them there accordingly 
        Also pay attention that their version is 1.14.2 and if you want to update them them in the feature just replace them with newer .so files
        */
        val platform64JarBytes = readResourceBytes("/templates/gdx-platform-1.14.2-natives-arm64-v8a.so")
        val platform32JarBytes = readResourceBytes("/templates/gdx-platform-1.14.2-natives-armeabi-v7a.so")
        
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
            bytes = platform64JarBytes
        )
        scaffold.writeBytes(
            relPath = "app/src/main/jniLibs/armeabi-v7a/libgdx.so",
            bytes = platform32JarBytes
        )
    }

    private fun readResourceBytes(resourcePath: String): ByteArray {
        val stream = this::class.java.getResourceAsStream(resourcePath)
        ?: throw IllegalStateException(
            "Bundled resource not found: $resourcePath. " +
            "Ensure it's packaged under src/main/resources in the plugin build."
        )
        return stream.use { it.readBytes() }
    }
}
