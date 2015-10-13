package com.example.ashoknayar.photonote;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
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

/**
 * A placeholder fragment containing a simple view.
 */
public class PhotoNoteActivityFragment extends Fragment {

    private static final int ACTIVITY_SELECT_IMAGE = 100;

    private ImageView img_view;

    public PhotoNoteActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_photo_note, container, false);

        final EditText caption_txt = (EditText) rootView.findViewById(R.id.caption_txt);
        Button upload_btn = (Button) rootView.findViewById(R.id.upload_btn);
        Button pic_btn = (Button) rootView.findViewById(R.id.pic_btn);

        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String caption_text = caption_txt.getText().toString();

            }
        });
        pic_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
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
                    cursor.close();
                    Bitmap bmp = ImgHelper.decodeScaledBitmapFromSdCard(filePath,100,100);
                    Log.i("ashokimggoogle", filePath);
                    img_view = (ImageView) getView().findViewById(R.id.thumbnail);
                    img_view.setImageBitmap(bmp);

                }
                break;

        }

    }
}
