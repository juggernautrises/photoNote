package com.example.ashoknayar.photonote;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.loopj.android.http.*;

import org.apache.http.Header;
import org.json.JSONObject;


/**
 * A placeholder fragment containing a simple view.
 */
public class PhotoNoteActivityFragment extends Fragment {

    private static final int ACTIVITY_SELECT_IMAGE = 100;

    private ImageView img_view;
    private String photoPath = "None";
    public PhotoNoteActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_photo_note, container, false);

        final EditText caption_txt = (EditText) rootView.findViewById(R.id.caption_txt);
        final EditText title_txt = (EditText) rootView.findViewById(R.id.title_txt);

        final Button upload_btn = (Button) rootView.findViewById(R.id.upload_btn);
        Button pic_btn = (Button) rootView.findViewById(R.id.pic_btn);

        final CheckBox publish_box = (CheckBox) rootView.findViewById(R.id.post_check_box);

        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(photoPath.equals("None"))
                {

                    Toast.makeText(getActivity().getApplicationContext(), "Need a Photo!", Toast.LENGTH_SHORT).show();

                }
                else {
                    String caption_text = caption_txt.getText().toString();
                    String title_text = title_txt.getText().toString();
                    String publish_status;
                    if (publish_box.isChecked()){
                        publish_status = "True";
                    } else{
                        publish_status = "False";
                    }
                    upload_btn.setText("Uploading...");
                    postData(caption_text, title_text, photoPath,publish_status);

                }



            }
        });
        pic_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //i.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(i, ACTIVITY_SELECT_IMAGE);

            }
        });

        if(savedInstanceState != null)
        {
            photoPath = savedInstanceState.getString("iconBMP");
            if (!photoPath.equals("None"))
            {
                Log.e("nayara-img",photoPath);
                Bitmap bmp = ImgHelper.decodeScaledBitmapFromSdCard(photoPath, 100, 100);
                img_view = (ImageView) rootView.findViewById(R.id.thumbnail);
                img_view.setImageBitmap(bmp);
            }
            caption_txt.setText(savedInstanceState.getString("caption"));
            title_txt.setText(savedInstanceState.getString("title"));
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        EditText caption_txt = (EditText) getView().findViewById(R.id.caption_txt);
        EditText title_txt = (EditText) getView().findViewById(R.id.title_txt);
        String title = title_txt.getText().toString();
        String caption = caption_txt.getText().toString();
        outState.putString("title",title);
        outState.putString("caption",caption);
        outState.putString("iconBMP", photoPath);
    }

    // TODO: Get filepath of cloud based image
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case ACTIVITY_SELECT_IMAGE:
                if (resultCode !=  Activity.RESULT_CANCELED && data != null) {
                    Uri selected_img = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getActivity().getContentResolver().query(selected_img, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    photoPath = filePath;
                    cursor.close();
                    Bitmap bmp = ImgHelper.decodeScaledBitmapFromSdCard(filePath,100,100);
                    Log.i("ashokimggoogle", filePath);
                    img_view = (ImageView) getView().findViewById(R.id.thumbnail);
                    img_view.setImageBitmap(bmp);

                }
                break;

        }

    }
    private void postData(String caption, String title, String image_path, String publish_status)
    {
        AsyncHttpClient client = new AsyncHttpClient();
        //Bitmap bmp = ImgHelper.resize(image_path, 500);
        Bitmap bmp = ImgHelper.resizeToMax(image_path, 600);
        //Bitmap bmp = ImgHelper.decodeScaledBitmapFromSdCard(image_path, 100, 100);
        try {
            // Save the bitmap to a file
            final File photofile = savedScaled(bmp);
            // Create and put the parameters
            RequestParams params = new RequestParams();
            params.put("fileupload", photofile);
            params.put("caption", caption);
            params.put("title", title);
            params.put("publish", publish_status);

            // Create and  override the HTTP response handler
            // so we can customize it with our own procedures

            AsyncHttpResponseHandler rhandler = new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes) {
                    String resp = "";
                    try{
                        String tmp = new String(bytes, "UTF-8");
                        Log.d("nayara-staus", tmp);
                        JSONObject j = new JSONObject(tmp);
                        resp = j.getString("saved");


                    } catch(Exception e)
                    {

                    }
                    if(resp.equals("true")) {
                        Log.d("nayara-success", "File uploaded");
                        EditText caption = (EditText)getView().findViewById(R.id.caption_txt);
                        caption.setText("");
                        EditText title = (EditText)getView().findViewById(R.id.title_txt);
                        title.setText("");
                        ImageView img = (ImageView) getView().findViewById(R.id.thumbnail);
                        img.setImageDrawable(null);
                        photoPath = "None";
                        Button upload_btn = (Button) getView().findViewById(R.id.upload_btn);
                        upload_btn.setText("Upload");
                        Toast.makeText(getActivity().getApplicationContext(), "File Uploaded!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Button upload_btn = (Button) getView().findViewById(R.id.upload_btn);
                        upload_btn.setText("Upload");
                        Toast.makeText(getActivity().getApplicationContext(), "Upload failed. Image was not saved.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes, Throwable throwable) {
                    Button upload_btn = (Button) getView().findViewById(R.id.upload_btn);
                    upload_btn.setText("Upload");

                    try{
                        String tmp = new String(bytes, "UTF-8");
                        Log.d("nayara-fail",tmp);
                    } catch (Exception e){

                    }

                    //Log.d("nayara-staus", tmp);
                    Toast.makeText(getActivity().getApplicationContext(), "Upload failed.", Toast.LENGTH_SHORT).show();
                }
            };

            // Mostly for debugging, but i set the timeout high so
            // it does not disconnect
            client.setTimeout(900000);
            // Post command with associated handler
            String url = URL.UPLOAD_URL;
            client.post(url, params, rhandler);
        } catch (Exception e) {
            Log.d("nayara-fail", "Something went wrong");
        }
    }

    private File savedScaled(Bitmap bmp) throws IOException
    {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "note_" + timeStamp + "_scaled.jpg";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"photoNotes");

        if(!storageDir.mkdirs())
        {
            storageDir.mkdirs();
            Log.e("nayara-dirfailed",storageDir.getAbsolutePath());
        }
        File image = new File(storageDir, imageFileName);

        FileOutputStream fout = new FileOutputStream(image);
        bmp.compress(Bitmap.CompressFormat.JPEG,100,fout);
        //bmp.compress(Bitmap.CompressFormat.PNG, 100, fout);
        fout.flush();
        fout.close(); // do not forget to close the stream
        Log.e("ash-image", "storage dir: " + storageDir.getAbsolutePath());
        Log.e("ash-image", "img absolute path: " + image.getAbsolutePath());
        Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(image));
        getActivity().getApplicationContext().sendBroadcast(mediaScanIntent);
        return image;

    };



    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "ashNote_" + timeStamp + "_";

        File storageDir = new File(Environment.getExternalStorageDirectory()+"/PhotoNotes/");
        if (!storageDir.exists()){
            storageDir.mkdir();
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        return image;

    }

}
