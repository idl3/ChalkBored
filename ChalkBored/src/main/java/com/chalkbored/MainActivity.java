package com.chalkbored;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.graphics.Bitmap;
import java.util.UUID;
import android.provider.MediaStore;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;

public class MainActivity extends Activity implements OnClickListener {
    static final int REQUEST_LINK_TO_DBX = 0;

    final static private String APP_KEY = "u7uqp9yw4olbhwi";
    final static private String APP_SECRET = "vd0iwxgjys3ks0t";

    private String fileName = "";

    private DbxAccountManager mAccountManager;
    private DbxAccount mAccount;

    private DrawingView drawView;
    private ImageButton currPaint, drawBtn, eraseBtn, newBtn, saveBtn;
    private MenuItem newActBtn, saveActBtn;
    private TextView mTestOutput;
    private float smallBrush, mediumBrush, largeBrush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawView = (DrawingView)findViewById(R.id.drawing);
        LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
        currPaint = (ImageButton)findViewById(R.id.default_brush);
        //currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);
        drawView.setBrushSize(smallBrush);

        String color = "#65BCA5";
        drawView.setColor(color);

        eraseBtn = (ImageButton)findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);

        mAccountManager = DbxAccountManager.getInstance(getApplicationContext(), APP_KEY, APP_SECRET);
        if (mAccountManager.hasLinkedAccount()) {
            mAccount = mAccountManager.getLinkedAccount();
            // ... Now display your own UI using the linked account information.
        } else {
        }
    }

    @Override
    public void onClick(View view){
        //respond to clicks
        if(view.getId()==R.id.erase_btn){
            //switch to erase - choose size
            /*final Dialog brushDialog = new Dialog(this);
            brushDialog.setTitle("Eraser size:");
            brushDialog.setContentView(R.layout.brush_chooser);
            ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(smallBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {*/
                    drawView.setErase(true);
                    drawView.setBrushSize(mediumBrush);
            /*        brushDialog.dismiss();
                }
            });
            ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(largeBrush);
                    brushDialog.dismiss();
                }
            });
            brushDialog.show();*/
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.newActBtn:
                //new button
                AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
                newDialog.setTitle("Start a new drawing");
                newDialog.setMessage("Be careful! You will lose your current drawing.");
                newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        drawView.startNew();
                        dialog.dismiss();
                    }
                });
                newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        dialog.cancel();
                    }
                });
                newDialog.show();
                System.out.println("NEW BUTTON WAS PRESSED YO");
                return true;
            case R.id.saveActBtn:
                if(mAccountManager.hasLinkedAccount()){
                    AlertDialog.Builder saveDropbox = new AlertDialog.Builder(this);
                    final EditText input = new EditText(this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    saveDropbox.setView(input);
                    saveDropbox.setTitle("Save drawing to Dropbox");
                    saveDropbox.setMessage("Give it a cool name!");
                    saveDropbox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //save drawing
                            if(input.getText().toString().length() > 0){
                                fileName = input.getText().toString();
                            }else{
                                fileName = UUID.randomUUID().toString();
                            }
                            drawView.setDrawingCacheEnabled(true);
                            Bitmap b = drawView.getDrawingCache();
                            String imgSaved = MediaStore.Images.Media.insertImage(
                                    getContentResolver(), b,
                                    fileName + ".png", "drawing");
                            if (imgSaved != null) {
                                try {
                                    final String IMG_FILE = fileName + ".png";
                                    DbxPath imgPath = new DbxPath(DbxPath.ROOT, IMG_FILE);

                                    // Create DbxFileSystem for synchronized file access.
                                    DbxFileSystem dbxFs = DbxFileSystem.forAccount(mAccountManager.getLinkedAccount());

                                    // Print the contents of the root folder.  This will block until we can
                                    // sync metadata the first time.
                                    List<DbxFileInfo> infos = dbxFs.listFolder(DbxPath.ROOT);

                                    // Create a test file only if it doesn't already exist.
                                    if (!dbxFs.exists(imgPath)) {
                                        DbxFile testFile = dbxFs.create(imgPath);
                                        testFile.close();
                                        try {
                                            testFile = dbxFs.open(imgPath);
                                            b.compress(Bitmap.CompressFormat.PNG,90,testFile.getWriteStream());
                                        } finally {
                                            testFile.close();
                                        }
                                    }

                                    // Read and print the contents of test file.  Since we're not making
                                    // any attempt to wait for the latest version, this may print an
                                    // older cached version.  Use getSyncStatus() and/or a listener to
                                    // check for a new version.
                                    if (dbxFs.isFile(imgPath)) {
                                        String resultData;
                                        DbxFile testFile = dbxFs.open(imgPath);
                                        try {
                                            resultData = testFile.toString();
                                        } finally {
                                            testFile.close();
                                        }
                                    } else if (dbxFs.isFolder(imgPath)) {
                                    }
                                } catch (IOException e) {
                                    System.out.println("Dropbox test failed: " + e);
                                }
                                Toast savedToast = Toast.makeText(getApplicationContext(),
                                        "Drawing saved to Dropbox!", Toast.LENGTH_SHORT);
                                savedToast.show();
                            } else {
                                Toast unsavedToast = Toast.makeText(getApplicationContext(),
                                        "Oops! Image could not be saved", Toast.LENGTH_SHORT);
                                unsavedToast.show();
                            }
                            drawView.destroyDrawingCache();
                        }
                    });
                    saveDropbox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    saveDropbox.show();
                }else{
                    //save drawing
                    AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
                    saveDialog.setTitle("Save drawing");
                    saveDialog.setMessage("Save drawing to device Gallery?");
                    saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which){
                            //save drawing
                            drawView.setDrawingCacheEnabled(true);
                            Bitmap b = drawView.getDrawingCache();
                            String imgSaved = MediaStore.Images.Media.insertImage(
                                    getContentResolver(), b,
                                    UUID.randomUUID().toString()+".png", "drawing");
                            if(imgSaved!=null){
                                Toast savedToast = Toast.makeText(getApplicationContext(),
                                        "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
                                savedToast.show();
                            }
                            else{
                                Toast unsavedToast = Toast.makeText(getApplicationContext(),
                                        "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
                                unsavedToast.show();
                            }
                            drawView.destroyDrawingCache();
                        }
                    });
                    saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which){
                            dialog.cancel();
                        }
                    });
                    saveDialog.show();
                }
                System.out.println("SAVE BUTTON WAS PRESSED YO");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    public void paintClicked(View view){
        //use chosen color
        drawView.setErase(false);
        if(view!=currPaint){
            //update color
            ImageButton imgView = (ImageButton)view;
            String color = view.getTag().toString();
            drawView.setColor(color);
            drawView.setBrushSize(smallBrush);
            //imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            //currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint=(ImageButton)view;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LINK_TO_DBX) {
            if (resultCode == Activity.RESULT_OK) {
                mAccount = mAccountManager.getLinkedAccount();
                // ... Now display your own UI using the linked account information.
            } else {
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
