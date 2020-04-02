package com.example.secretsharingtesting;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.codahale.shamir.Scheme;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import android.view.View;
import android.widget.*;

public class MainActivity extends AppCompatActivity {

    Scheme scheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editTextSecret = (EditText) findViewById(R.id.editTextSecret);
        EditText editTextThreshold = (EditText) findViewById(R.id.editTextThreshold);
        EditText editTextShareNum = (EditText) findViewById(R.id.editTextShareNum);
        EditText editTextShares = (EditText) findViewById(R.id.editTextShares);

        TextView textViewReconstructed = (TextView) findViewById(R.id.textViewReconstructed);

        Button buttonShare = (Button) findViewById(R.id.buttonShare);
        buttonShare.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextShares.setText("");

                int thresholdNum = Integer.parseInt(editTextThreshold.getText().toString());
                int shareNum = Integer.parseInt(editTextShareNum.getText().toString());

                scheme = new Scheme(new SecureRandom(), shareNum, thresholdNum);

                final byte[] secret = editTextSecret.getText().toString().getBytes(StandardCharsets.UTF_8);
                final Map<Integer, byte[]> parts = scheme.split(secret);

                for (int i=1; i<=parts.size(); i++) {
                    byte[] share = parts.get(i);
                    editTextShares.setText(editTextShares.getText().toString() + i + "-" +byteArrayToHex(share) + " ");
                    System.out.println(byteArrayToHex(share));
                }
            }
        });

        Button buttonReconstruct = (Button) findViewById(R.id.buttonReconstruct);
        buttonReconstruct.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //shareInput example: "1-000000 2-aaaaa 3-2fab8 ..."
                String sharesInput  =  editTextShares.getText().toString();
                //shares example: {"1-00000", "2-aaaaa", "3-2fab8", ...}
                String[] shares = sharesInput.split(" ");

                //longShare example: {"1", "00000"}
                String[] longShare;
                //share example: "00000"
                String share;

                Map<Integer, byte[]> parts = new HashMap<>();

                for (int i=1; i<=shares.length; i++){
                    longShare = shares[i-1].split("-");
                    share = longShare[longShare.length - 1];

                    parts.put(i,hexToByteArray(share));
                    System.out.println(share);
                }

                final byte[] recovered = scheme.join(parts);
                textViewReconstructed.setText("\n\n"+(new String(recovered, StandardCharsets.UTF_8)));
                System.out.println(new String(recovered, StandardCharsets.UTF_8));
            }
        });
    }
//
//    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
//    public static String bytesToHex(byte[] bytes) {
//        char[] hexChars = new char[bytes.length * 2];
//        for (int j = 0; j < bytes.length; j++) {
//            int v = bytes[j] & 0xFF;
//            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
//            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
//        }
//        return new String(hexChars);
//    }

    public static byte[] hexToByteArray(String hex) {
        if (hex == null || hex.length() == 0) {
            return null;
        }

        byte[] ba = new byte[hex.length() / 2];
        for (int i = 0; i < ba.length; i++) {
            ba[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return ba;
    }

    public static String byteArrayToHex(byte[] ba) {
        if (ba == null || ba.length == 0) {
            return null;
        }

        StringBuffer sb = new StringBuffer(ba.length * 2);

        String hexNumber;
        for (int x = 0; x < ba.length; x++) {
            hexNumber = "0" + Integer.toHexString(0xff & ba[x]);
            sb.append(hexNumber.substring(hexNumber.length() - 2));
        }

        return sb.toString();
    }

}
