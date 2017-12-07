package pe.edu.upao.lectordesuministros;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.internal.Utils;
import com.googlecode.tesseract.android.TessBaseAPI;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.ti_usuario) TextInputLayout usuario;
    @BindView(R.id.ti_password) TextInputLayout password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        usuario.getEditText().setText("admin");
        password.getEditText().setText("admin");
    }


    @OnClick(R.id.ingresar)
    public void login() {
        if ("admin".equals(usuario.getEditText().getText().toString()) && "admin".equals(password.getEditText().getText().toString())) {
            startActivity(new Intent(this, OCRActivity.class));
        }
    }



}
