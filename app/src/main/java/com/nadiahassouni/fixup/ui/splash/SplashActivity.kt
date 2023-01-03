package com.nadiahassouni.fixup.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.nadiahassouni.fixup.R
import com.nadiahassouni.fixup.ui.agent.AgentActivity
import com.nadiahassouni.fixup.ui.auth.AuthenticationActivity
import com.nadiahassouni.fixup.ui.client.ClientActivity
import com.nadiahassouni.fixup.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        Handler().postDelayed({ checkAuthState() }, 2000)

    }

    private fun checkAuthState() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            openAuthActivity()
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userCollectionRef = FirebaseAuth.getInstance().currentUser?.uid?.let {
                        Firebase.firestore.collection("users")
                            .document(it)
                    }
                    val querySnapshot = userCollectionRef?.get()?.await()
                    val userRole = querySnapshot?.toObject<User>()?.role
                    withContext(Dispatchers.Main) {
                        when (userRole) {
                            "client" -> {
                                openClientActivity()
                            }
                            "agent" -> {
                                openAgentActivity()
                            }
                        }
                    }
                }catch (e : Exception){

                }
            }
        }
    }

    private fun openClientActivity() {
        val intent = Intent(this , ClientActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun openAgentActivity() {
        val intent = Intent(this , AgentActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun openAuthActivity() {
        startActivity(Intent(this , AuthenticationActivity::class.java))
        startActivity(intent)
        finish()
    }

}