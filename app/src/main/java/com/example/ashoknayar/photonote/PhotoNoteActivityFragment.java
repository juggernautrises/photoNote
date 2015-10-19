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

        Button upload_btn = (Button) rootView.findViewById(R.id.upload_btn);
        Button pic_btn = (Button) rootView.findViewById(R.id.pic_btn);

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
                    postData(caption_text, title_text, photoPath);
                    //Toast.makeText(getActivity().getApplicationContext(), caption_text, Toast.LENGTH_SHORT).show();

                }



            }
        });
        pic_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(i, ACTIVITY_SELECT_IMAGE);

            }
        });


        return rootView;
    }

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
    private void postData(String caption, String title, String image_path)
    {
        AsyncHttpClient client = new AsyncHttpClient();
        Bitmap bmp = ImgHelper.resize(image_path, 700);

        try {
            // Save the bitmap to a file
            File photofile = savedScaled(bmp);
            // Create and put the parameters
            RequestParams params = new RequestParams();
            params.put("userfile", photofile);
            params.put("caption", caption);
            params.put("title", title);

            // Create and  override the HTTP response handler
            // so we can customize it with our own procedures

            AsyncHttpResponseHandler rhandler = new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes) {
                    String resp = "";
                    try{
                        String tmp = new String(bytes, "UTF-8");
                        JSONObject j = new JSONObject(tmp);
                        resp = j.getString("saved");
                    } catch(Exception e)
                    {

                    }
                    if(resp.equals("true")) {
                        Log.d("nayara-success", "File uploaded");
                        Toast.makeText(getActivity().getApplicationContext(), "File Uploaded!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(getActivity().getApplicationContext(), "Upload failed. Image was not saved.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes, Throwable throwable) {
                    Toast.makeText(getActivity().getApplicationContext(), "Upload failed.", Toast.LENGTH_SHORT).show();

                }
            };

            // Mostly for debugging, but i set the timeout high so
            // it does not disconnect
            client.setTimeout(900000);
            // Post command with associated handler
            client.post("http://ashnayar.com/image-upload.php", params, rhandler);
        } catch (Exception e) {
            Log.d("nayara-fail", "Something went wrong");
        }
    }

    private File savedScaled(Bitmap bmp) throws IOException
    {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "note_" + timeStamp + "_scaled.jpg";
        //File storageDir = new File(Environment.getExternalStorageDirectory(),"PhotoNotes");
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"photoNotes");

        if(!storageDir.mkdirs())
        {
            Log.e("nayara-dirfailed",storageDir.getAbsolutePath());
        }
//        if (!storageDir.exists()){
//            storageDir.mkdirs();
//            storageDir.mkdir();
//            Toast.makeText(getActivity().getApplicationContext(), storageDir.toString()+" does not exist!", Toast.LENGTH_SHORT).show();
//
//        }
        File image = new File(storageDir, imageFileName);
//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );
        //Toast.makeText(getActivity().getApplicationContext(), image.toString(), Toast.LENGTH_SHORT).show();

        FileOutputStream fout = new FileOutputStream(image);
        //Bitmap pictureBitmap = getImageBitmap(myurl); // obtaining the Bitmap
        bmp.compress(Bitmap.CompressFormat.JPEG, 85, fout); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
        fout.flush();
        fout.close(); // do not forget to close the stream
        Log.e("ash-image", "storage dir: " + storageDir.getAbsolutePath());

        //MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), image.getAbsolutePath(), image.getName(), image.getName());
        Log.e("ash-image", "img absolute path: " + image.getAbsolutePath());
        Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(image));
        getActivity().getApplicationContext().sendBroadcast(mediaScanIntent);
        return image;

    };
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "ashNote_" + timeStamp + "_";
        //File storageDir = Environment.getExternalStoragePublicDirectory(
        //        Environment.DIRECTORY_PICTURES);

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
        //mCurrentPhotoPath = image.getAbsolutePath();
        return image;

    }

}
