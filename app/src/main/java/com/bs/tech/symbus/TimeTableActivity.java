package com.bs.tech.symbus;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.io.IOException;

public class TimeTableActivity extends AppCompatActivity
{
    //Button bdisplay;
    ImageView imgdisplay;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table);
        //bdisplay=(Button)findViewById(R.id.buttonshowimg);
        imgdisplay=(ImageView)findViewById(R.id.time_tab);
        firebaseStorage=FirebaseStorage.getInstance();
        storageReference=firebaseStorage.getReferenceFromUrl("gs://symbus-jwt.appspot.com").child("ttable.jpeg");
          try
          {
              final File localFile = File.createTempFile("ttable", "jpeg");
              storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>()
              {
                  @Override
                  public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)
                  {
                      Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                      imgdisplay.setImageBitmap(bitmap);
                  }
              }).addOnFailureListener(new OnFailureListener()
              {
                  @Override
                  public void onFailure(@NonNull Exception exception) {}
              });
          }
          catch (IOException e ) {}
    }

}
