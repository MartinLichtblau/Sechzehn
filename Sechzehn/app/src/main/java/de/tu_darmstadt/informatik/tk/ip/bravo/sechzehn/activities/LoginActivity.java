package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.List;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.LoginFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments.ResetPasswordFragment;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.ApiMessage;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.DefaultCallback;
import retrofit2.Call;
import retrofit2.Response;

import static de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.LoginService.LoginService;

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
            List<String> pathSegments = intent.getData().getPathSegments();
            if (pathSegments.size() > 1) {
                if (pathSegments.get(0).equals("reset")) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.loginContainer, ResetPasswordFragment.newInstance(pathSegments.get(1)))
                            //.commit();  https://stackoverflow.com/questions/7575921/illegalstateexception-can-not-perform-this-action-after-onsaveinstancestate-wit
                            .commitAllowingStateLoss(); // Fix crash while starting app from Email-link

                } else if (pathSegments.get(0).equals("confirm")) {
                    LoginService.confirmEmail(pathSegments.get(1)).enqueue(new DefaultCallback<ApiMessage>(getActivity()) {
                        @Override
                        public void onResponse(Call<ApiMessage> call, Response<ApiMessage> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getActivity(), "Email confirmed.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getActivity(), "Email already confirmed.", Toast.LENGTH_SHORT).show();
                            }
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

