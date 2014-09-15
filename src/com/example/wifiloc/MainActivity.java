package com.example.wifiloc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.interfaces.EstimationListener;
import com.example.structures.AreaPoint;
import com.example.structures.Point.PointType;
import com.example.utils.CommonUtils;

public class MainActivity extends Activity implements EstimationListener {



    private WifiManager wifiManager;
    private WifiReceiver wifiReceiver;
    private SlideWindowProc slideWindowProc;

    private MultiPointTouchListener mtpl;
    private ImageView imageView;
    private TextView text;
    private Canvas canvas;
    private Paint paint;
    private static int count = 0;
    private static String PRE_PATH ="/sdcard/gmission/.map/";


    private String imgFilePath;
    private static int COMPRESS_RATE = 4;
    public boolean isNavigating = true;
    public boolean hasCalculated = false;
    private int canvasSaveCount = -1;
    private int areaId = -1;

    private AreaPoint cPoint = new AreaPoint();//current point

    private List<String> fileNameArray = new ArrayList<String>();
    private Button startLocationButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifiManager = (WifiManager) getSystemService("wifi");
        if (!wifiManager.isWifiEnabled()
                && wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLING) {
            wifiManager.setWifiEnabled(true);
        }
        wifiReceiver = new WifiReceiver(wifiManager, this);


        ///////////////////////////////////////////////
        String IP = getString(R.string.IP);
        String PORT = getString(R.string.PORT);
        String WebService = getString(R.string.IndoorLocalizationService);
        String url = CommonUtils.getAbsoluteURL(IP, PORT, WebService);
        slideWindowProc = new SlideWindowProc(this, url);
//        slideWindowProc.url = url;
        //////////////////////////////////////////////
        registerReceiver(wifiReceiver, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        // setDefault Image
        imageView = (ImageView) findViewById(R.id.imageView);
        text = (TextView) findViewById(R.id.textView1);
        mtpl = new MultiPointTouchListener();
        imageView.setOnTouchListener(mtpl);
        startLocationButton = (Button) findViewById(R.id.localization);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }



    public void onLocation(View v) {
        Log.i("localization info", "location");

        Toast.makeText(getApplicationContext(), "location start",
                Toast.LENGTH_LONG).show();
        wifiReceiver.startScan();

    }

    private boolean setImageView(String floorFolderName) {
        imgFilePath = PRE_PATH + floorFolderName+"/map.jpg";
        File mapFile = new File(imgFilePath);
        if(!mapFile.exists()){
            mapFile.getParentFile().mkdirs();
            new RemoteMapRetrieveTask().execute(floorFolderName, imgFilePath);
            return false;
        } else {
            Bitmap bmp = null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = COMPRESS_RATE;
            bmp = BitmapFactory.decodeFile(imgFilePath, options).copy(
                    Bitmap.Config.ARGB_8888, true);
            canvas = new Canvas(bmp);
            paint = new Paint();
            paint.setStyle(Style.STROKE);
            canvas.drawBitmap(bmp, 0, 0, paint);
            paint.setAntiAlias(true);
            imageView.setImageBitmap(bmp);
            areaId = cPoint.areaId;
        }
        return true;
    }

    public void updateInfo() {
        boolean result = true;
        if(areaId != cPoint.areaId){
            result = setImageView(String.valueOf(cPoint.areaId));
        }
        if(result){
            drawPoint(cPoint.x/COMPRESS_RATE, cPoint.y/COMPRESS_RATE, PointType.START);
        }
    }

    public void onLocatization(View v){
        wifiReceiver.startScan();
        startLocationButton.setVisibility(View.GONE);
    }
    private void drawPoint(double x, double y, PointType type) {
        if (type == PointType.START) {
            paint.setColor(Color.RED);
        }

        if (type == PointType.END) {
            paint.setColor(Color.MAGENTA);
        }
        paint.setStrokeWidth(2);
        canvas.drawPoint((float) x, (float) y, paint);
        imageView.invalidate();

    }
    @Override
    public void estimatePosition() {

        new LocationEstimateTask().execute();
    }
    public class LocationEstimateTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            //Run true estimation task !
            if (slideWindowProc.estimateWindowResult()) {
                slideWindowProc.slideToNextWin();
            }
            return null;
        }

    }
    public void updateEstimatedPosition(AreaPoint p) {


        if (p == null) {
            Log.i("point", "This position has not been explored!");
            text.setText("This position has not been explored!");
        } else {
            text.setText(p.areaId+":"+p.toString()+count++);
            Log.i("point", p.areaId+":"+p.toString()+count);
            cPoint.x = p.x;
            cPoint.y = p.y;
            cPoint.areaId = p.areaId;
            updateInfo();
        }
        wifiReceiver.startScan();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(wifiReceiver);
        wifiReceiver.stopScan();
        super.onDestroy();
    }

    public class RemoteMapRetrieveTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... arg0) {
            String imgUrl =getString(R.string.IP) + getString(R.string.img_url);
            imgUrl = imgUrl.replace("*areaId*", arg0[0]);

            try {
                URL imageUrl = new URL(imgUrl);
                URLConnection ucon = imageUrl.openConnection();

                InputStream is = ucon.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);

                ByteArrayBuffer baf = new ByteArrayBuffer(1024);
                int current = 0;
                while ((current = bis.read()) != -1) {
                    baf.append((byte) current);
                }
                OutputStream out = new BufferedOutputStream(new FileOutputStream(arg0[1]));
                out.write(baf.toByteArray());
                out.close();
                is.close();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return arg0[0];
        }

        @Override
        protected void onPostExecute(final String content) {
            updateInfo();
        }
    }
}




