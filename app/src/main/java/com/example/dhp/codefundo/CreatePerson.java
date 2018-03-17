package com.example.dhp.codefundo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.CreatePersonResult;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.FaceRectangle;
import com.microsoft.projectoxford.face.contract.Person;
import com.microsoft.projectoxford.face.rest.ClientException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class CreatePerson extends AppCompatActivity {
    EditText personName;
    EditText personGroup;
    EditText personroll;
    Button submitPerson;
    private final int PICK_IMAGE = 1;
    ProgressDialog detectionProgressDialog;
    HashMap<String, Person> personsData;
    Person[] persons;
    Face[] result;
    private FaceServiceClient faceServiceClient;
    ImageView image;
    InputStream io;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_person);
        detectionProgressDialog = new ProgressDialog(this);
        detectionProgressDialog.setMessage("Creating person");
        detectionProgressDialog.setCanceledOnTouchOutside(false);

        personName = findViewById(R.id.personName);
        personGroup = findViewById(R.id.personGroup);
        personroll = findViewById(R.id.personRoll);
        personsData = new HashMap<>();
        faceServiceClient = new FaceServiceRestClient(MainActivity.SERVER_HOST, MainActivity.SUBSCRIPTION_KEY);

        image = (ImageView)findViewById(R.id.image);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallIntent = new Intent(Intent.ACTION_GET_CONTENT);
                gallIntent.setType("image/*");
                startActivityForResult(Intent.createChooser(gallIntent, "Select Picture"), PICK_IMAGE);
                detectionProgressDialog.show();
            }
        });
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    persons = faceServiceClient.getPersons("testing");
                    for (Person person : persons) {
                        personsData.put(person.userData, person);
                    }
                    Iterator it = personsData.entrySet().iterator();
                    while (it.hasNext()) {
                        HashMap.Entry pair = (HashMap.Entry) it.next();
                        Log.v("addddddddd", pair.getKey().toString());
//                Log.v("addddddddd", pair.getValue().toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();


        submitPerson = findViewById(R.id.submitPerson);
        submitPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    createPerson();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                image.setImageBitmap(bitmap);
                detectAndFrame(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Bitmap drawFaceRectanglesOnBitmap(Bitmap originalBitmap, Face[] faces) {
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        int stokeWidth = 2;
        paint.setStrokeWidth(stokeWidth);
        if (faces != null) {
            for (Face face : faces) {
                FaceRectangle faceRectangle = face.faceRectangle;
                canvas.drawRect(
                        faceRectangle.left,
                        faceRectangle.top,
                        faceRectangle.left + faceRectangle.width,
                        faceRectangle.top + faceRectangle.height,
                        paint);
            }
        }
        return bitmap;
    }

    private void detectAndFrame(final Bitmap imageBitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
         io = new ByteArrayInputStream(outputStream.toByteArray());
         ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        AsyncTask<InputStream, String, Face[]> detectTask = new AsyncTask<InputStream, String, Face[]>() {
            @Override
            protected Face[] doInBackground(InputStream... params) {
                try {
                    publishProgress("Detecting...");
                    result = faceServiceClient.detect(
                            params[0],
                            true,         // returnFaceId
                            false,        // returnFaceLandmarks
                            null           // returnFaceAttributes: a string like "age, gender"
                    );
                    if (result == null) {
                        publishProgress("Detection Finished. Nothing detected");
                        return null;
                    }
                    publishProgress(
                            String.format("Detection Finished. %d face(s) detected",
                                    result.length));
                    return result;
                } catch (Exception e) {
                    publishProgress("Detection failed");
                    return null;
                }
            }

            @Override
            protected void onPreExecute() {
                //TODO: show progress dialog
                detectionProgressDialog.show();
                detectionProgressDialog.setCanceledOnTouchOutside(false);
            }

            @Override
            protected void onProgressUpdate(String... progress) {
                //TODO: update progress
                detectionProgressDialog.setMessage(progress[0]);
            }

            @Override
            protected void onPostExecute(Face[] result) {
                //TODO: update face frames
                detectionProgressDialog.dismiss();
                if (result == null) return;
                image.setImageBitmap(drawFaceRectanglesOnBitmap(imageBitmap, result));
            }
        };
        detectTask.execute(inputStream);
    }

    private void createPerson() throws IOException, ClientException {
        detectionProgressDialog = new ProgressDialog(this);
        detectionProgressDialog.setMessage("Creating person");
        detectionProgressDialog.show();
        detectionProgressDialog.setCanceledOnTouchOutside(false);


        String rollNumber = personroll.getText().toString();
        if (personsData.get(rollNumber) == null) {
            Thread background = new Thread() {
                @Override
                public void run() {
                    String name = personName.getText().toString();
                    String group = personGroup.getText().toString();
                    String rollNumber = personroll.getText().toString();
                    try {

                        CreatePersonResult res = faceServiceClient.createPerson(group, name, rollNumber);
                        detectionProgressDialog.dismiss();

                        calladdface(res.personId,rollNumber);

                    } catch (Exception e) {
                        e.printStackTrace();
                        detectionProgressDialog.dismiss();
                    }
                }
            };
            background.start();
        } else {
            Toast.makeText(getApplicationContext(), "Person exists", Toast.LENGTH_SHORT).show();
            detectionProgressDialog.dismiss();
        }
    }

    private void calladdface(UUID personId,String roll) {
        FaceRectangle faceRectangle=null;
        for (Face face : result) {
            faceRectangle = face.faceRectangle;
        }
        try {
            faceServiceClient.addPersonFace("testing",personId,io,roll,faceRectangle);
            calltraing("testing");
        } catch (ClientException e) {
            e.printStackTrace();
        } catch (IOException e) {
                e.printStackTrace();
        }
    }

    private void calltraing(String testing) {
        try {
            faceServiceClient.trainPersonGroup(testing);
        } catch (ClientException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

