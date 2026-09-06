package kz.codingOnTheMoon.codeassistlibgdxtemplatev1.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import kz.codingOnTheMoon.codeassistlibgdxtemplatev1.R

class PluginInfoActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plugin_info_activity)
        val codeAssistRepo: ImageView = findViewById(R.id.codeassist_repo)
        codeAssistRepo.setOnClickListener{
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/tyron12233/CodeAssist"))
            startActivity(intent)
        }
        
        val codeAssistLibGDXRepo : ImageView = findViewById(R.id.codeassist_gdx_repo)
        codeAssistLibGDXRepo.setOnClickListener{
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ItIsMeYourMoon/CodeAssist-LibGDX"))
            startActivity(intent)
        }
    }
}