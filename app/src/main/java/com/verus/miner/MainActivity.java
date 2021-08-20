package com.verus.miner;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import java.lang.Process;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import com.verus.miner.VerusMiner;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.ActivityCompat;
import android.support.design.widget.TextInputLayout;
import android.graphics.Color;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CheckBox;
import android.os.Handler;
import java.io.File;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {
    VerusMiner miner;
    Handler handler = new Handler();
    boolean mining = false;
    String LOG = "";

	private EditText threads;
	private EditText worker;
	private EditText address;
	private EditText pool;
	private EditText pass;
	private Button button;
    private ImageView img;
	private TextInputLayout sa;
	private TextInputLayout sa1;
	private TextInputLayout sa2;
	private TextInputLayout sa3;
	private TextInputLayout sa4;

    void Settings(){
		threads = (EditText) findViewById(R.id.threads);
		worker = (EditText) findViewById(R.id.worker);
		address = (EditText) findViewById(R.id.address);
		pool = (EditText) findViewById(R.id.pool);
		pass = (EditText) findViewById(R.id.pass);
		button = (Button)findViewById(R.id.button);

        if(readSettings(this) == null)
            saveSettings(threads.getText().toString() + '\n' + worker.getText().toString() + '\n' + pool.getText().toString() + '\n' + pass.getText().toString() + '\n' + address.getText().toString(), this);

        String settings = readSettings(this);
        threads.setText(settings.split("\n")[1]);
        worker.setText(settings.split("\n")[2]);
        pool.setText(settings.split("\n")[3]);
        pass.setText(settings.split("\n")[4]);
        address.setText(settings.split("\n")[5]);

    }
    void perms(){
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
			android.Manifest.permission.ACCESS_NETWORK_STATE,
			android.Manifest.permission.INTERNET,
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }
    void getCpuInfo(){

        Button button = (Button)findViewById(R.id.button);
        EditText threads = (EditText) findViewById(R.id.threads);
        TextView text = (TextView)findViewById(R.id.LOG);
        try {
            Scanner scanner = new Scanner( new File("/proc/cpuinfo") );
            String output = scanner.useDelimiter("\\A").next();
            scanner.close();
            String ver = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("/system/bin/uname -a ").getInputStream())).readLine();

            int count = 0;
            int pos = output.indexOf("aes");
            while (pos > -1) {
                ++count;
                pos = output.indexOf("aes", ++pos);
            }
            if(count > 0) {
               // threads.setText(String.valueOf(count));
                text.setText("มือถือคุณมี " + String.valueOf(count) +" คอ");
                Log.e("test", String.valueOf(count) + " คอ");

            } else {
                threads.setText("None");
                text.setText("ซีพียูของคุณไม่มี AES...");
                Log.e("test", "cpu ไม่มี aes");
                button.setClickable(false);
            }
            if(!ver.contains("aarch64") && !ver.contains("armv8")){
                if(ver.contains("armv7")&& count > 0){
                    Log.e("test", "armv7l but realy armv8 ?");
                    text.setText("เรากำลังดำเนินการเพื่อให้ cpu ของคุณทำงาน...");
                    button.setClickable(false);
                } else if (count > 0){
                    text.setText("cpu ที่ไม่รู้จัก \nจำนวน AES: " + String.valueOf(count) + "/n" + ver );
                    Log.e("test", "cpu ที่ไม่รู้จัก \nจำนวน AES: " + String.valueOf(count) + "/n" + ver );
                    button.setClickable(false);
                }
            } else {
                if(count == 0){
                    text.setText("เวอร์ชันเคอร์เนลไม่ถูกต้อง");
                    Log.e("test","เวอร์ชันเคอร์เนลไม่ถูกต้อง" );
                    button.setClickable(false);
                }
            }

        }catch (Exception e){
            text.setText(e.toString());
            Log.e("test",e.toString());
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		img = (ImageView) findViewById(R.id.ic_img);
        File homePath = MainActivity.this.getFilesDir();
        miner = new VerusMiner(homePath,this);
        perms();
        Settings();
        getCpuInfo();
        ((TextView)findViewById(R.id.LOG)).setMovementMethod(new ScrollingMovementMethod());
		
		img.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					new AlertDialog.Builder(MainActivity.this)					
						.setMessage("คุณแน่ใจหรือไม่ว่าต้องการหยุดขุด ?")
						.setPositiveButton("ออก", new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which) {
								finish();    
							}

						})
						.setNegativeButton("ยกเลิก", null)
						.show();
				}
			});
    }

	public void onBackPressed() {
		new AlertDialog.Builder(MainActivity.this)					
			.setMessage("คุณแน่ใจหรือไม่ว่าต้องการหยุดขุด ?")
			.setPositiveButton("ออก", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();    
				}

			})
			.setNegativeButton("ยกเลิก", null)
			.show();
	}

    public void mine(View view){
        TextView text = (TextView)findViewById(R.id.LOG);
		sa = (TextInputLayout)findViewById(R.id.sa);
		sa1 = (TextInputLayout)findViewById(R.id.sa1);
		sa2 = (TextInputLayout)findViewById(R.id.sa2);
		sa3 = (TextInputLayout)findViewById(R.id.sa3);
		sa4 = (TextInputLayout)findViewById(R.id.sa4);
        Button button = (Button)findViewById(R.id.button);
        if(mining){
            handler.removeCallbacks(mainLoop);
            miner.stop();
            mining = false;
            text.setText("");
            text.scrollTo(0, 0);
            LOG = "";
            button.setText("Start");
			sa.setVisibility(View.VISIBLE);
			sa1.setVisibility(View.VISIBLE);
			sa2.setVisibility(View.VISIBLE);
			sa3.setVisibility(View.VISIBLE);
			sa4.setVisibility(View.VISIBLE);
        }else {
            saveSettings(threads.getText().toString()  + '\n' + worker.getText().toString()  + '\n' + pool.getText().toString() + '\n' + pass.getText().toString() + '\n' + address.getText().toString(),this);
            CheckBox bench = (CheckBox)findViewById(R.id.bench);
            miner.mine(threads.getText().toString(),pass.getText().toString(),pool.getText().toString(),worker.getText().toString(),address.getText().toString(),bench.isChecked());
            handler.postDelayed(mainLoop, 200);
            mining = true;
            button.setText("Stop");
			sa.setVisibility(View.GONE);
			sa1.setVisibility(View.GONE);
			sa2.setVisibility(View.GONE);
			sa3.setVisibility(View.GONE);
			sa4.setVisibility(View.GONE);
        }
    }

    void genricError(){
        Button button = (Button) findViewById(R.id.button);
        TextView text = (TextView) findViewById(R.id.LOG);
        miner.stop();
        if (text.getScrollY() >= (text.getLayout().getLineTop(text.getLineCount()) - text.getHeight()) - 2) {
            text.setText(LOG);
            text.scrollTo(0, text.getLayout().getLineTop(text.getLineCount()) - text.getHeight());
        } else {
            text.setText(LOG);
        }        mining = false;
        button.setText("Start");
        Log.e("test", LOG);
        handler.removeCallbacks(mainLoop);
    }

    private Runnable mainLoop = new Runnable() {
        @Override
        public void run() {
            Button button = (Button) findViewById(R.id.button);
            TextView text = (TextView) findViewById(R.id.LOG);

            if (LOG.split("\n").length > 100) {
                LOG = LOG.substring(LOG.indexOf('\n') + (LOG.split("\n").length - 100));
            }
            try{
                if(miner.cmd.process.exitValue() != 0) {
                    LOG += "process exited: " + String.valueOf(miner.cmd.process.exitValue());
                    genricError();
                } else {
                    LOG += String.valueOf(miner.cmd.process.exitValue());
                    genricError();
                }
            }catch (IllegalThreadStateException e) {

                if (miner.errors != null) {
                    LOG += miner.errors + miner.error() + miner.output();
                    genricError();
                } else if (!miner.error().isEmpty()) {
                    LOG += miner.error() + miner.output();
                    genricError();
                } else {
                    LOG += miner.output();
                    Log.e("test", LOG);
                    if (text.getScrollY() >= (text.getLayout().getLineTop(text.getLineCount()) - text.getHeight()) - 2) {
                        text.setText(LOG);
                        text.scrollTo(0, text.getLayout().getLineTop(text.getLineCount()) - text.getHeight());
                    } else {
                        text.setText(LOG);
                    }
                    handler.postDelayed(this, 200);
                }
            } catch (Exception e) {
                LOG += e.toString();
                genricError();
            }
        }
    };
    private static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    private void saveSettings(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    private String readSettings(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("config.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
            return null;
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
}
