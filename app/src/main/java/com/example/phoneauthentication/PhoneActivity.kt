package com.example.phoneauthentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class PhoneActivity : AppCompatActivity() {

    private lateinit var sendOtpBtn:Button
    private lateinit var phoneNumberET:EditText
    private lateinit var auth:FirebaseAuth
    private lateinit var number:String
    private lateinit var mProgressBar:ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone)

        /*********************************************init() FUNCTION TO INITIALIZE THE VARIABLES*********************************************/
        init()
        sendOtpBtn.setOnClickListener{
            number=phoneNumberET.text.toString()
            if(number.isNotEmpty()){
                if(number.length==10){
                    mProgressBar.visibility= VISIBLE
                    number="+91$number"

                    /******************************************************************BY THIS CODE THE FIREBASE SENDS THE OTP******************/
                    val options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(number)                                      // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS)                   // Timeout and unit
                        .setActivity(this)                                          // Activity (for callback binding)
                        .setCallbacks(callbacks)                                    /*************************Callback FUNCTION IS CREATED*******/
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)

                }else{
                    Toast.makeText(this,"Plz enter Correct Number",Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this,"Plz enter Number",Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun init(){
        mProgressBar=findViewById(R.id.phoneProgressBar)
        mProgressBar.visibility=INVISIBLE
        sendOtpBtn=findViewById(R.id.sendOTPBtn)
        phoneNumberET=findViewById(R.id.phoneEditTextNumber)
        auth= FirebaseAuth.getInstance()
    }

    /***********************************************************SAME CODE JUST COPY IT*********************************************************/
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information

                        Toast.makeText(this,"Authenticate Successfull",Toast.LENGTH_SHORT).show()
                        sendToMain()
                        mProgressBar.visibility= INVISIBLE

                    val user = task.result?.user
                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Log.d("TAG","signInWithPhoneCredential : ${task.exception.toString()}")
                    }
                    // Update UI
                }
            }
    }

    private fun sendToMain(){
        startActivity(Intent(this,MainActivity::class.java))
    }

    /**************************************************JUST COPY EXCEPT SOME CHANGES************************************************************/
    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        /********************************************************** onVerificationCompleted***************************************************
         * // This callback will be invoked in two situations:
         1 - Instant verification. In some cases the phone number can be instantly
             verified without needing to send or enter a verification code.
         2 - Auto-retrieval. On some devices Google Play services can automatically
             detect the incoming verification SMS and perform verification without
             user action.
         **********IN SHORT THIS FUNCTION SAYS USER GET AUTOMATICALLY VERIFY BEFORE ENTERING THE CODE WHICH IS A REAR SITUATION***************************************************************************************************************/
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {

            /***********TO AUTHORIZE THE USER  *****************/
            signInWithPhoneAuthCredential(credential)                      /*******************************THE FUNCTION IS CREATE ABOVE**********/
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Log.d("TAG","OnVerificationFailed : ${e.toString()}")
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Log.d("TAG","OnVerificationFailed : ${e.toString()}")
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            /*****************************************************************************************************************************
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            // Save verification ID and resending token so we can use them later
             *******************************************************************************************************************************/

            val intent= Intent(this@PhoneActivity,OTPActivity::class.java)
            intent.putExtra("OTP",verificationId)
            intent.putExtra("resendToken",token)
            intent.putExtra("phoneNumber",number)
            startActivity(intent)
            mProgressBar.visibility= INVISIBLE

        }
    }

    /********************************************It checks whether the User is already login or not*********************************************/
    override fun onStart() {
        super.onStart()
        if(auth.currentUser!=null){
            val intent= Intent(this@PhoneActivity,MainActivity::class.java)
            startActivity(intent)
        }
    }
}