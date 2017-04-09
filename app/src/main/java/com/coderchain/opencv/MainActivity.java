package com.coderchain.opencv;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;

import java.net.Socket;
import java.net.SocketAddress;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    Button send;
    String mCurrentPhotoPath;
    File photoFile;
    static{
        if(!OpenCVLoader.initDebug()){
            Log.d("info","opencv not loaded");
        }else{
            Log.d("info","opencv loaded");
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button takePhoto = (Button)findViewById(R.id.button);
        Button openGallery = (Button)findViewById(R.id.button2);
        send = (Button)findViewById(R.id.button3);
        send.setEnabled(false);
        imageView=(ImageView)findViewById(R.id.image);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        Toast.makeText(getApplicationContext(),"Error! cannot create file",Toast.LENGTH_SHORT).show();
                    }
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(getApplicationContext(), "com.coderchain.opencv.fileprovider", photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, 0);
                    }
                }
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new senddata().execute();
            }
        });

        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getPath());
            imageView.setImageBitmap(bitmap);
            send.setEnabled(true);
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    public class senddata extends AsyncTask{
        @Override
        protected Object doInBackground(Object[] params) {
            try {
                Socket socket=new Socket("192.168.8.74",2020);
                Bitmap bmp=BitmapFactory.decodeFile(photoFile.getPath());
                ByteArrayOutputStream bos=new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG,100,bos);
                String image= Base64.encodeToString(bos.toByteArray(),Base64.DEFAULT);
                DataOutputStream dStream=new DataOutputStream(socket.getOutputStream());
                dStream.writeUTF(image);
                Log.v("image",image);
                dStream.flush();
                dStream.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
                //Toast.makeText(getApplicationContext(),e.getMessage().toString(),Toast.LENGTH_SHORT).show();
            }
            return null;
        }
    }
}

