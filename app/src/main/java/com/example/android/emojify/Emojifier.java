package com.example.android.emojify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

/**
 * Created by jeremy on 7/19/17.
 * These methods use the Google Mobile Vision API to detect faces in a bitmap (photo).
 * Faces detected are then drawn over with emoji based on eyes open/closed and smile/frown.
 */

public class Emojifier {

    //Method to detect faces in the parsed bitmap image.
    //Returns modified bitmap.
    public static Bitmap detectFaces (Context context, Bitmap bitmap) {

        // If bitmap is immutable, create a mutable bitmap we can draw upon.
        if (!bitmap.isMutable()) {
            bitmap = bitmap.copy(bitmap.getConfig(), true);
        }
        /* Paint definitions needed for drawing face landmarks below.
        Paint myRectPaint = new Paint();
        myRectPaint.setStrokeWidth(5);
        myRectPaint.setColor(Color.RED);
        myRectPaint.setStyle(Paint.Style.STROKE);
        */

        //Create a canvas so we draw on the bitmap image.
        Canvas tempCanvas = new Canvas(bitmap);
        //tempCanvas.setBitmap(bitmap);
        //tempCanvas.drawBitmap(bitmap, 0, 0, null);

        //Setup face detector options and build detector.
        FaceDetector faceDetector = new
                FaceDetector.Builder(context).setTrackingEnabled(false)
                .setMode(FaceDetector.ACCURATE_MODE)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setProminentFaceOnly(false)
                .build();
        if(!faceDetector.isOperational()){
            Log.i("INFO", "Could not setup the face detector!");
            return bitmap; //Just return unaltered image.
        } else {
            //Create needed frame to run face detector.
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<Face> faces = faceDetector.detect(frame);
            //Iterate through faces and draw on each face.
            for(int i=0; i<faces.size(); i++) {
                Face thisFace = faces.valueAt(i);
                float left = thisFace.getPosition().x;
                float top = thisFace.getPosition().y;
                float right = left + thisFace.getWidth();
                float bottom = top + thisFace.getHeight();
                Bitmap emoji = getEmoji(context, thisFace);
                Rect emojiRect = new Rect((int) left, (int) top, (int) right, (int) bottom);
                tempCanvas.drawBitmap(emoji, null, emojiRect, null);
                /*****
                for ( Landmark landmark : thisFace.getLandmarks() ) {
                    int cx = (int) ( landmark.getPosition().x );
                    int cy = (int) ( landmark.getPosition().y );
                    tempCanvas.drawCircle( cx, cy, 10, myRectPaint );
                }
                 ***/
            }
            //release the face detector resources.
            faceDetector.release();

        }
            return bitmap; //return the modified bitmap image.
    }

    //This method takes a list of faces and returns corresponding emoji
    //based on smile/frown and eye open/closed values.
    private static Bitmap getEmoji (Context context, Face face) {

        //initialize the variables as smile.png (binary = 0b0111)
        //corresponds to 0|smile|leftEye|righteye.
        byte isSmile = 0b0100; //set smile bit.
        byte leftOpen = 0b0010; //set left eye open bit.
        byte rightOpen = 0b0001; // set right eye open bit.
        if (face.getIsSmilingProbability() < 0.5f) {
            isSmile = 0; //clear smile bit.
        }
        if (face.getIsLeftEyeOpenProbability() < 0.5f) {
            leftOpen = 0; //clear left eye open bit.
        }
        if (face.getIsRightEyeOpenProbability() < 0.5f) {
            rightOpen = 0; //clear right eye open bit.
        }
        byte emojiByte = (byte) (isSmile + leftOpen + rightOpen); //add the bits together

        switch (emojiByte) { //select emoji drawable based on bit code.
            case 0b111: //smile
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.smile);
            case 0b110:
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.rightwink);
            case 0b101:
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.leftwink);
            case 0b100:
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.closed_smile);
            case 0b000:
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.closed_frown);
            case 0b001:
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.leftwinkfrown);
            case 0b010:
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.rightwinkfrown);
            case 0b011:
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.frown);
        }
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.smile);
    }

}
