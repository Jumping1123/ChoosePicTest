package com.example.jumping.choosepictest;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    public static final int TAKE_PHOTO =1;
    public static final int CROP_PHOTO = 2;
    public static final int CHOOSE_PHOTO = 3;
    private Button takephoto;
    private Button choosefromalbum;
    private ImageView picture;
    private Uri imageuri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        takephoto = (Button) findViewById(R.id.take_photo);
        picture = (ImageView) findViewById(R.id.picture);
        takephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File outputimage = new File(Environment.getExternalStorageDirectory(), "output_image.jpg");
                try {
                    if (outputimage.exists()) {
                        outputimage.delete();
                    }
                    outputimage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageuri = Uri.fromFile(outputimage);
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
                startActivityForResult(intent, TAKE_PHOTO);

            }
        });

        choosefromalbum = (Button) findViewById(R.id.choose_from_album);
        choosefromalbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                intent.setType("image/*");
                startActivityForResult(intent,CHOOSE_PHOTO);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(imageuri, "image/*");
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
                    startActivityForResult(intent, CROP_PHOTO);
                }
                break;
            case CROP_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageuri));
                        picture.setImageBitmap(bitmap);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();

                    }
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        handleimageonhighversion(data);
                    } else {
                        handleimagelowversion(data);
                    }
                }
                break;
            default:
                break;

        }
    }

    private void handleimageonhighversion(Intent data) {
        String imagepath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docid = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docid.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagepath = getimagepath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contenturi = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docid));
                imagepath = getimagepath(contenturi, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagepath = getimagepath(uri, null);
        }
        displayimage(imagepath);
    }

    private void handleimagelowversion(Intent data) {
        Uri uri = data.getData();
        String imagepath = getimagepath(uri, null);
        displayimage(imagepath);

    }

    private String getimagepath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null){
        if (cursor.moveToFirst()) {
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        }
            cursor.close();
        }
        return path;
    }


    private void displayimage(String imagepath) {
        if (imagepath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagepath);
            picture.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this,"Failed to get image",Toast.LENGTH_SHORT).show();
        }
    }
}
