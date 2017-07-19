package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ncapdevi.fragnav.FragNavController;

import java.util.ArrayList;
import java.util.List;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.BaseFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.ForgotPwFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.LoginFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.RegisterFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.ResetPasswordFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.Services.LoginService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private LoginActivity getActivity() {
        return this;
    }



    @Override
    protected void onNewIntent(Intent intent) {


        if (intent.hasCategory("android.intent.category.BROWSABLE")) {
            List<String> pathSegments =intent.getData().getPathSegments();
            if (pathSegments.size() > 1) {
                if (pathSegments.get(0).equals("reset")) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.loginContainer, ResetPasswordFragment.newInstance(pathSegments.get(1)))
                            //.commit();  https://stackoverflow.com/questions/7575921/illegalstateexception-can-not-perform-this-action-after-onsaveinstancestate-wit
                            .commitAllowingStateLoss(); // Fix crash while starting app from Email-link

                } else if (pathSegments.get(0).equals("confirm")) {
                    ServiceGenerator.createService(LoginService.class).confirmEmail(pathSegments.get(1)).enqueue(new Callback<Object>() {
                        @Override
                        public void onResponse(Call<Object> call, Response<Object> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getActivity(), "Email confirmed.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getActivity(), "Email already confirmed.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Object> call, Throwable t) {
                            Toast.makeText(getActivity(), "Connectivity error.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.loginContainer, LoginFragment.newInstance());
        transaction.commit();




    }

}

