package kz.codingOnTheMoon.codeassistlibgdxtemplatev1.template

import kz.codingOnTheMoon.codeassistlibgdxtemplatev1.android.AndroidAppAssets

object TemplateContent{
    
    fun manifest(pkg: String) : String{
        return """
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
        """
    }
    
    fun strings(name: String): String {
        return """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <string name="app_name">${name}</string>
                <string name="hello_world">Hello, World!</string>
            </resources>
            """
    }
    
    fun colors(): String{
        return """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <color name="primary">#FF6200EE</color>
                <color name="on_primary">#FFFFFFFF</color>
                ${AndroidAppAssets.ICON_BACKGROUND_COLOR_XML}
            </resources>
            """
    }
    
    fun mainActivity(pkg: String): String{
        return """
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
                """
    }
    
    fun myGdxGame(pkg: String): String{
        return """
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
    }
}
