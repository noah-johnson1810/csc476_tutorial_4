package edu.sdsmt.mad_hatter_johnson_noah;

/*
 * ___X___	45 	Tutorial completed (points based on percent completed)
 *
 * ___X___	15 	Landscape using Constraint\RelativeLayout correct for Pixel C, Pixel 3a XL, and 5.1" FWVGA (-3pt for each minor error)
 *
 * ___X___	10 	Scaling works (-5pt for semi working)
 *
 * __n/a__	10 	CSC 576 ONLY: Scaling keeps the same image point under the touch point (-5pt for semi working)
 *
 * ___X___	5 	Feather appears and disappears as required
 *
 * ___X___	5 	Color picker opens Color Activity and closes on color tap
 *
 * ___X___	5 	Color picker returns a color
 *
 * ___X___	5 	Spinner is correct in all cases
 *
 * ___X___	5 	Feather checkmark is correct in all cases
 *
 * ___X___	5 	Color button disables when not valid
 */


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class HatterActivity extends AppCompatActivity {

    private static final String PARAMETERS = "parameters";

    /**
     * The hatter view object
     */
    private HatterView hatterView = null;

    /**
     * The color select button
     */
    private Button colorButton = null;

    /**
     * The feather checkbox
     */
    private CheckBox featherCheck = null;

    /**
     * The hat choice spinner
     */
    private Spinner spinner;

    /**
     * Activity launcher for content
     */
    ActivityResultLauncher<String> resultLauncher;

    ActivityResultLauncher<Integer> colorResultLauncher;

    /**
     * id to identify why permission was requested
     */
    public static final int NEED_PERMISSIONS = 1;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        hatterView.putToBundle(PARAMETERS, outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
         * Get some of the views we'll keep around
         */
        hatterView = (HatterView) findViewById(R.id.hatterView);
        colorButton = (Button) findViewById(R.id.buttonColor);
        featherCheck = (CheckBox) findViewById(R.id.checkFeather);
        spinner = (Spinner) findViewById(R.id.spinnerHat);

        resultLauncher =
                registerForActivityResult(new ActivityResultContracts.GetContent(), new HandleResult());
        /*
         * Set up the spinner
         */

        colorResultLauncher = registerForActivityResult(new ActivityResultContract<Integer, Integer>() {
            @NonNull
            @Override
            public Intent createIntent(@NonNull Context context, Integer integer) {
                return new Intent(context, ColorSelectActivity.class);
            }

            @Override
            public Integer parseResult(int i, @Nullable Intent intent) {
                if (intent != null) {
                    return intent.getIntExtra("color", 0);
                }
                return 0;
            }
        }, new HandleColorResult());

        // Create an ArrayAdapter using the string array and a default spinner layout
        String[] hats = getResources().getStringArray(R.array.hats_spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, hats);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View view,
                                       int pos, long id) {
                hatterView.setHat(pos);
                colorButton.setEnabled(pos == 2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });

        /*
         * Restore any state
         */
        if (savedInstanceState != null) {
            hatterView.getFromBundle(PARAMETERS, savedInstanceState);
            spinner.setSelection(hatterView.getHat());
        }

        featherCheck.setOnCheckedChangeListener((buttonView, isChecked) -> hatterView.setShowFeather(isChecked));

        updateUI();
    }

    /**
     * Handle a Picture button press
     *
     * @param view the source view
     */
    public void onPicture(View view) {
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;

        //change in image permission in Tiramisu and later
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        }

        if (ActivityCompat.checkSelfPermission(this,
                permission) != PackageManager.PERMISSION_GRANTED) {

            //no permission yet, ask for permission and cancel this image request
            ActivityCompat.requestPermissions(this, new String[]{permission}, NEED_PERMISSIONS);
        } else {
            resultLauncher.launch("image/*");
        }
    }

    /**
     * Ensure the user interface components match the current state
     */
    private void updateUI() {
        spinner.setSelection(hatterView.getHat());
        featherCheck.setActivated(hatterView.getShowFeather());
    }

    public void onColor(View view) {
        int result = 0;
        colorResultLauncher.launch(result);
    }

    public class HandleResult implements ActivityResultCallback<Uri> {

        @Override
        public void onActivityResult(Uri result) {
            if (result != null) {
                Log.i("Path", result.toString());
                hatterView.setImageUri(result.toString());
            }
        }
    }

    public class HandleColorResult implements ActivityResultCallback<Integer> {

        @Override
        public void onActivityResult(Integer color) {
            if (color != null) {
                hatterView.setColor(color);
            }
        }
    }
}