package com.example.myfaceapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button chose;
    TextView resultTv;
    ImageView imageView;
    public static final int PIC_IMAGE = 121;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        chose = findViewById(R.id.button2);
        chose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Image"),PIC_IMAGE);
            }
        });

        resultTv = findViewById(R.id.textView);
        imageView = findViewById(R.id.imageView2);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PIC_IMAGE){
            imageView.setImageURI(data.getData());

            final FirebaseVisionImage image;
            try {
                final Bitmap bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(),data.getData());
                final Bitmap mutableBmp = bmp.copy(Bitmap.Config.ARGB_8888,true);
                final Canvas canvas = new Canvas(mutableBmp);

                image = FirebaseVisionImage.fromFilePath(getApplicationContext(), data.getData());

                FirebaseVisionFaceDetectorOptions options =
                        new FirebaseVisionFaceDetectorOptions.Builder()

                                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                                .build();

                FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                        .getVisionFaceDetector(options);


                Task<List<FirebaseVisionFace>> result =
                        detector.detectInImage(image)
                                .addOnSuccessListener(
                                        new OnSuccessListener<List<FirebaseVisionFace>>() {
                                            @Override
                                            public void onSuccess(List<FirebaseVisionFace> faces) {
                                                // Task completed successfully
                                                // ...
                                                Log.d("tryFace","success");
                                                for (FirebaseVisionFace face : faces) {
                                                    Rect bounds = face.getBoundingBox();

                                                    Paint p = new Paint();
                                                    p.setColor(Color.YELLOW);
                                                    p.setStyle(Paint.Style.STROKE);
                                                    canvas.drawRect(bounds,p);
                                                    imageView.setImageBitmap(mutableBmp);

                                                    float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                                                    float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees

                                                    Log.d("tryFace",rotY+"    "+rotZ);

                                                    // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                                                    // nose available):
                                                    FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
                                                    if (leftEar != null) {
                                                        FirebaseVisionPoint leftEarPos = leftEar.getPosition();
                                                        Rect rect2 = new Rect((int)(leftEarPos.getX() - 20),(int)(leftEarPos.getY()-20),(int)(leftEarPos.getX() + 20),(int)(leftEarPos.getY()+20));
                                                        canvas.drawRect(rect2,p);
                                                        imageView.setImageBitmap(mutableBmp);
                                                    }

                                                    FirebaseVisionFaceLandmark RightEye = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE);
                                                    if (leftEar != null) {
                                                        FirebaseVisionPoint rightEyePos = RightEye.getPosition();
                                                    }

                                                    // If contour detection was enabled:
                                                    List<FirebaseVisionPoint> rightEyeContour =
                                                            face.getContour(FirebaseVisionFaceContour.RIGHT_EYE).getPoints();
                                                    List<FirebaseVisionPoint> upperLipBottomContour =
                                                            face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).getPoints();
                                                    for(FirebaseVisionPoint point : upperLipBottomContour){

                                                    }

                                                    // If classification was enabled:
                                                    Paint p2 = new Paint();
                                                    p2.setColor(Color.BLACK);
                                                    p2.setTextSize(16);
                                                    if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                                        float smileProb = face.getSmilingProbability();
//                                                        if(smileProb>0.5){
//                                                            canvas.drawText("Smiling",bounds.exactCenterX(),bounds.exactCenterY(),p2);
//                                                        }else{
//                                                            canvas.drawText("Not Smiling",bounds.exactCenterX(),bounds.exactCenterY(),p2);
//                                                        }
//                                                        imageView.setImageBitmap(mutableBmp);

                                                    }
                                                    if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                                        float rightEyeOpenProb = face.getRightEyeOpenProbability();
                                                    }

                                                    // If face tracking was enabled:
                                                    if (face.getTrackingId() != FirebaseVisionFace.INVALID_ID) {
                                                        int id = face.getTrackingId();
                                                    }
                                                }
                                            }
                                        })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Task failed with an exception
                                                // ...
                                                Log.d("tryFace","fail");
                                            }
                                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}