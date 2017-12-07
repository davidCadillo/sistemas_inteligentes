package pe.edu.upao.lectordesuministros;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class OCRActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 10;
    private static final String TAG = "OCR";

    // @BindView(R.id.takePicture) FloatingActionButton btnTakePicture;

    @BindView(R.id.til_ruc) TextInputLayout til_ruc;
    @BindView(R.id.btn_read) Button btnRead;
    @BindView(R.id.btn_find) Button btnFind;
    @BindView(R.id.img_picture) CropImageView imgPicture;
    @BindView(R.id.textoMostrar) TextInputEditText campo_ruc;
    @BindView(R.id.btn_teseract) com.github.clans.fab.FloatingActionButton btn_tesseract;
    private TessBaseAPI mTess;
    private String datapath = "";
    private Bitmap image;
    private static
    String listaBlanca = "1234567890!@#$%^&*()_+=-qwertyuiop[]}{POIU\nYTREWQasdASDfghFGHjklJKLl;L:'\\\"\\\\|~`xcvXCVbnmBNM,./<>?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);
        ButterKnife.bind(this);
          ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Preparando...un momento");
        new LoadTask(progress, this).execute();

    }

    @OnClick({R.id.btn_teseract, R.id.btn_read, R.id.btn_tensor})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.btn_teseract:
                CropImage.activity()
                        .start(this);
                break;

        }
    }

    @OnClick(R.id.btn_read)
    public void leer(Button button) {

        ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Intentado leer el texto...");
        new ReadTask(progress, this).execute();
    }

    @OnClick(R.id.btn_find)
    public void find(Button button){
        startActivity(new Intent(this, DataActivity.class));
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    image = (Bitmap) extras.get("data");
                    storeImage();
                    imgPicture.setImageBitmap(image);
                    btnRead.setVisibility(View.VISIBLE);
                }
                break;

            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    btnRead.setVisibility(View.VISIBLE);
                    Uri imageUri = result.getUri();
                    imgPicture.setImageUriAsync(imageUri);

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
        }


    }


    private void storeImage() {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d(TAG, "Error creando archivo imagen. Revisa los permisos: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
            galleryAddPic(pictureFile);
        } catch (FileNotFoundException e) {
            Log.d(TAG, "Archivo no encontrado: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accediendo al archivo:  " + e.getMessage());
        }
    }

    private File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
   /*     File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }*/
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName = "MI_" + timeStamp + ".jpg";
        mediaFile = new File(Environment.getExternalStorageDirectory().toString() + File.separator + mImageName);
//        mediaFile = new File(Environment.getExternalStorageDirectory().toString() + File.separator + mImageName);
        return mediaFile;
    }


    private void galleryAddPic(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void checkFile(File dir) {
        if (!dir.exists() && dir.mkdirs())
            copyFiles();
        if (dir.exists()) {
            String datafilepath = datapath + "/tessdata/spa.traineddata";
            File datafile = new File(datafilepath);
            if (!datafile.exists())
                copyFiles();
        }
    }

    private void copyFiles() {
        try {
            String filepath = datapath + "/tessdata/spa.traineddata";
            AssetManager assetManager = getAssets();
            InputStream instream = assetManager.open("tessdata/spa.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

            File file = new File(filepath);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public class ReadTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog progress;
        OCRActivity act;
        String resultado;

        public ReadTask(ProgressDialog progress, OCRActivity act) {
            this.progress = progress;
            this.act = act;
        }

        public void onPreExecute() {
            progress.show();
            mTess.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SPARSE_TEXT);
            mTess.setImage(imgPicture.getCroppedImage());
            mTess.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, listaBlanca);
        }

        public void onPostExecute(Void unused) {
            campo_ruc.setText(resultado);
            progress.dismiss();
        }

        protected Void doInBackground(Void... params) {
            resultado = mTess.getUTF8Text();
            return null;
        }

    }

    public class LoadTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog progress;
        OCRActivity act;
        File file;

        public LoadTask(ProgressDialog progress, OCRActivity act) {
            this.progress = progress;
            this.act = act;
        }

        public void onPreExecute() {
            progress.show();
            datapath = getFilesDir() + "/tesseract/";
            mTess = new TessBaseAPI();
            mTess.setDebug(true);
            file = new File(datapath + "tessdata/");
            if (!file.exists()) {
                file.mkdirs();
            }

        }

        public void onPostExecute(Void unused) {
            progress.dismiss();
            mTess.init(datapath, "spa");
        }

        protected Void doInBackground(Void... params) {
            checkFile(file);
            return null;
        }

    }

    @OnTextChanged(R.id.textoMostrar)
    protected void onTextChangedNombre(CharSequence ruc) {

        if(Pattern.matches(PatternString.RUC, ruc)){
            setFieldValidate(til_ruc, "Correcto", false);
            btnFind.setVisibility(View.VISIBLE);
        }else if(ruc.length() == 0 || campo_ruc.getText().toString().isEmpty()){
            setEmpty(til_ruc);
            btnFind.setVisibility(View.GONE);
        }else if(ruc.length() == 11){
            setInvalidate(til_ruc);
            btnFind.setVisibility(View.GONE);
        }

    }

    private  void setFieldValidate(TextInputLayout campo, String message, boolean checkImage) {
        campo.setHintTextAppearance(R.style.Hint);
        campo.setErrorTextAppearance(R.style.Validado);
        campo.setErrorEnabled(true);
        campo.setError(message);
        //if (checkImage && campo.getEditText() != null)
          //  campo.getEditText().setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_circle_black_24px, 0);
    }
    public static void setEmpty(TextInputLayout textInputLayout) {
        textInputLayout.setCounterEnabled(false);
        textInputLayout.setCounterMaxLength(11);
        textInputLayout.setHintTextAppearance(R.style.Normal);
        textInputLayout.setHintAnimationEnabled(false);
        textInputLayout.setErrorEnabled(false);
    }

    public static void setInvalidate(TextInputLayout textInputLayout) {
        textInputLayout.setCounterEnabled(true);
        textInputLayout.setCounterMaxLength(11);
        textInputLayout.setHintTextAppearance(R.style.Error);
        textInputLayout.setErrorTextAppearance(R.style.Error);
        textInputLayout.setHintAnimationEnabled(true);
        textInputLayout.setErrorEnabled(true);
        textInputLayout.setError("Incorrecto");
    }

}
