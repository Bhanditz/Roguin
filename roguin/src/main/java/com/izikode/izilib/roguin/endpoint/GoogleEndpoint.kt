package com.izikode.izilib.roguin.endpoint

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.izikode.izilib.roguin.helper.RoguinActivity
import com.izikode.izilib.roguin.RoguinEndpoint
import com.izikode.izilib.roguin.helper.RoguinException
import com.izikode.izilib.roguin.model.RoguinToken

class GoogleEndpoint(

        private val roguinActivity: RoguinActivity

) : RoguinEndpoint {

    private val googleClient by lazy {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        GoogleSignIn.getClient(roguinActivity, options)
    }

    override val isSignedIn: Boolean
        get() = GoogleSignIn.getLastSignedInAccount(roguinActivity) != null

    override fun requestSignIn(response: (success: Boolean, token: RoguinToken?, error: RoguinException?) -> Unit) {
        roguinActivity.requestResult(googleClient.signInIntent) { success, result ->
            if (!success) {
                response.invoke(false, null, RoguinException(null, result))
            } else {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result)

                try {
                    val result = task.getResult(ApiException::class.java)

                    response.invoke(true, result.toToken(), null)
                } catch (googleApiException: ApiException) {
                    response.invoke(false, null,
                        RoguinException(googleApiException, result)
                    )
                }
            }
        }
    }

    private fun GoogleSignInAccount.toToken() = RoguinToken(
        endpoint = this@GoogleEndpoint::class,
        authenticatedToken = this.idToken ?: "",
        userId = this.id ?: ""
    )

    override fun requestSignOut(response: (success: Boolean) -> Unit) {
        googleClient.signOut().addOnCompleteListener {
            response.invoke(it.isSuccessful)
        }
    }

}