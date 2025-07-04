package com.example.fakeapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;

import android.os.*;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.hardware.camera2.*;

import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Message;
import javax.mail.Authenticator;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class FakeShutdownActivity extends Activity {
    interface EmailCallback {
        void onEmailsLoaded(String[] emails);
    }


    private BroadcastReceiver sentReceiver;
    private BroadcastReceiver deliveredReceiver;
    private CameraDevice cameraDevice;
    private MediaRecorder recorder;
    private Handler handler;
    private FusedLocationProviderClient fusedLocationClient;
    private AppDatabase db;
    private boolean isPhotoCaptured = false;
    private boolean isAudioRecorded = false;
    private boolean isLocationSaved = false;
    private SessionManager session;
    private int CurrentUserId;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = AppDatabase.getInstance(getApplicationContext());
        session = new SessionManager(this);
        CurrentUserId = session.getUserId();



        // Make screen fullscreen & non-touchable
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        );

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

// Create a layout with black background
        FrameLayout layout = new FrameLayout(this);
        layout.setBackgroundColor(Color.BLACK);

// Create the "Shutting down..." TextView
        TextView shuttingDownText = new TextView(this);
        shuttingDownText.setText("Shutting down...");
        shuttingDownText.setTextColor(Color.WHITE);
        shuttingDownText.setTextSize(24);
        shuttingDownText.setGravity(Gravity.CENTER);

// Set layout parameters for centering
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        shuttingDownText.setLayoutParams(params);

// Add TextView to layout
        layout.addView(shuttingDownText);

// Set the layout as the content view
        setContentView(layout);



        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Start camera thread
        HandlerThread thread = new HandlerThread("CameraThread");
        thread.start();
        handler = new Handler(thread.getLooper());

        // Start capture and location tasks
        capturePhoto();
        startRecordingAudio();
        getLocation();
    }

    @SuppressLint("MissingPermission") // already checked
    private void getLocation() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setNumUpdates(1);

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        Log.d("Location", "Lat: " + latitude + ", Lon: " + longitude);
                        saveLocationToFile(latitude, longitude);
                    } else {
                        Log.e("Location", "Location is null");
                    }
                });
    }


    private void saveLocationToFile(double lat, double lon) {
        try {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "FakeShutdown");
            if (!dir.exists()) dir.mkdirs();

            String filename = "location_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".txt";
            File locationFile = new File(dir, filename);

            FileOutputStream fos = new FileOutputStream(locationFile);
            String data = "Latitude: " + lat + "\nLongitude: " + lon;
            fos.write(data.getBytes());
            fos.close();

            MediaScannerConnection.scanFile(this,
                    new String[]{locationFile.getAbsolutePath()},
                    null, null); // Auto-detect MIME

            Log.d("FakeShutdown", "📍 Location saved: " + locationFile.getAbsolutePath());
            isLocationSaved = true;
            checkAndSendEmail();

        } catch (Exception e) {
            Log.e("FakeShutdown", "❌ Error saving location: " + e.getMessage());
        }
    }

    private void capturePhoto() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String id : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        Log.e("FakeShutdown", "Camera permission not granted");
                        return;
                    }
                    manager.openCamera(id, new CameraDevice.StateCallback() {
                        @Override
                        public void onOpened(@NonNull CameraDevice camera) {
                            cameraDevice = camera;
                            takePicture();
                        }

                        @Override
                        public void onDisconnected(@NonNull CameraDevice camera) {
                            camera.close();
                        }

                        @Override
                        public void onError(@NonNull CameraDevice camera, int error) {
                            camera.close();
                        }
                    }, handler);
                    break;
                }
            }
        } catch (Exception e) {
            Log.e("FakeShutdown", "Camera error: " + e.getMessage());
        }
    }

    private void takePicture() {
        try {
            final ImageReader imageReader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 1);
            Surface surface = imageReader.getSurface();

            final CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            builder.addTarget(surface);

            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "FakeShutdown");
            if (!dir.exists()) dir.mkdirs();
            String filename = "img_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".jpg";
            File imageFile = new File(dir, filename);

            imageReader.setOnImageAvailableListener(reader -> {
                try (Image image = reader.acquireLatestImage()) {
                    if (image != null) {
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        try (FileOutputStream output = new FileOutputStream(imageFile)) {
                            output.write(bytes);
                            Log.d("FakeShutdown", "Image saved: " + imageFile.getAbsolutePath());
                            isPhotoCaptured = true;
                            checkAndSendEmail();
                        }
                        MediaScannerConnection.scanFile(this,
                                new String[]{imageFile.getAbsolutePath()},
                                new String[]{"image/jpeg"}, null);
                    }
                } catch (Exception e) {
                    Log.e("FakeShutdown", "Error saving image: " + e.getMessage());
                }
            }, handler);

            cameraDevice.createCaptureSession(
                    Collections.singletonList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            try {
                                session.capture(builder.build(), new CameraCaptureSession.CaptureCallback() {
                                    @Override
                                    public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                                        Log.d("FakeShutdown", "Capture complete.");

                                    }
                                }, handler);
                            } catch (CameraAccessException e) {
                                Log.e("FakeShutdown", "Capture error: " + e.getMessage());
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Log.e("FakeShutdown", "Session configuration failed.");
                        }
                    }, handler
            );

        } catch (Exception e) {
            Log.e("FakeShutdown", "takePicture error: " + e.getMessage());
        }
    }

    private void startRecordingAudio() {
        try {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "FakeShutdown");
            if (!dir.exists()) dir.mkdirs();

            String filename = "audio_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".mp4";
            File file = new File(dir, filename);

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // Changed
            recorder.setOutputFile(file.getAbsolutePath());
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC); // Changed
            recorder.prepare();
            recorder.start();

            MediaScannerConnection.scanFile(this,
                    new String[]{file.getAbsolutePath()},
                    new String[]{"audio/mp4"}, null);

            isAudioRecorded = true;
            checkAndSendEmail();

        } catch (Exception e) {
            Log.e("FakeShutdown", "Audio error: " + e.getMessage());
        }
    }

    public static void sendEmailWithAttachments(
            String senderEmail,
            String senderPassword,
            String[] toEmails,
            String subject,
            String body,
            File[] attachments
    ) {
        // Prepare the email content
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setSubject(subject);
            message.setRecipients(Message.RecipientType.TO, toInternetAddresses(toEmails));
            message.setText(body);

            // Add attachments
            if (attachments != null && attachments.length > 0) {
                Multipart multipart = new MimeMultipart();
                for (File file : attachments) {
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    attachmentPart.attachFile(file);
                    multipart.addBodyPart(attachmentPart);
                }
                message.setContent(multipart);
            }

            // Send the email
            Transport.send(message);

            Log.d("Email", "Email sent successfully");

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Email", "Failed to send email: " + e.getMessage());
        }
    }
    private static InternetAddress[] toInternetAddresses(String[] emails) throws AddressException {
        InternetAddress[] addresses = new InternetAddress[emails.length];
        for (int i = 0; i < emails.length; i++) {
            addresses[i] = new InternetAddress(emails[i].trim());
        }
        return addresses;
    }

    private void getRegisteredEmailsAsync(EmailCallback callback) {
        new Thread(() -> {
            List<EmailEntity> emails = db.userDao().getEmailsByUserId(CurrentUserId);
            String[] emailArray;
            if (emails != null && !emails.isEmpty()) {
                emailArray = new String[emails.size()];
                for (int i = 0; i < emails.size(); i++) {
                    emailArray[i] = emails.get(i).email;
                }
            } else {
                emailArray = new String[]{"default@example.com"};
            }

            String[] finalEmailArray = emailArray;

            runOnUiThread(() -> {
                callback.onEmailsLoaded(finalEmailArray); // callback here
            });
        }).start();
    }

    private void checkAndSendEmail() {
        if (isPhotoCaptured && isAudioRecorded && isLocationSaved) {
            getRegisteredEmailsAsync(emails -> {
                String subject = "📸 Fake Shutdown Alert!";
                String body = "This email contains captured photo/audio/location during fake shutdown.";

                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "FakeShutdown");
                File[] attachments = dir.listFiles();

                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(() -> {
                    // Send the email with attachments
                    sendEmailWithAttachments(
                            "your_mail@domain.com",
                            "Your_app_password",
                            emails,
                            subject,
                            body,
                            attachments
                    );

                    // Once email is sent, send SMS
                    new Thread(() -> {
                        List<ContactEntity> contacts = db.userDao().getContactsByUserId(CurrentUserId);
                        List<String> phoneNumbers = new ArrayList<>();

                        for (ContactEntity contact : contacts) {
                            if (contact.phone != null && !contact.phone.isEmpty()) {
                                phoneNumbers.add(contact.phone);
                            }
                        }

                        // Use activity context when calling sendSilentSMS
                        SmsHelper.sendSilentSMS(FakeShutdownActivity.this, phoneNumbers);
                    }).start();


                    // After email and SMS are sent, update UI and finish the activity
                    runOnUiThread(() -> {
                        // Make the screen touchable again before finishing
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        finish(); // Close black screen
                    });
                });
            });
        }
    }
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onStart() {
        super.onStart();

        // Initialize and register
        sentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.d("SMS", "SMS sent successfully!");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Log.e("SMS", "Generic failure.");
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Log.e("SMS", "No service.");
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Log.e("SMS", "Null PDU.");
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Log.e("SMS", "Radio off.");
                        break;
                }
            }
        };

        deliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.d("SMS", "SMS delivered.");
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.e("SMS", "SMS not delivered.");
                        break;
                }
            }
        };

        // Register the receivers
        registerReceiver(sentReceiver, new IntentFilter("SMS_SENT"));
        registerReceiver(deliveredReceiver, new IntentFilter("SMS_DELIVERED"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (sentReceiver != null) unregisterReceiver(sentReceiver);
        if (deliveredReceiver != null) unregisterReceiver(deliveredReceiver);
    }

    @Override
    public void onBackPressed() {
        // Do nothing
    }

    @Override
    protected void onDestroy() {
        if (recorder != null) {
            try {
                recorder.stop();
                recorder.release();
            } catch (Exception ignored) {}
        }
        if (cameraDevice != null) {
            cameraDevice.close();
        }
        super.onDestroy();
    }
}
