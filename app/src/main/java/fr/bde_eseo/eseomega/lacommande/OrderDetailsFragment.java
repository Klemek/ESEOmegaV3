package fr.bde_eseo.eseomega.lacommande;

import android.animation.ObjectAnimator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import fr.bde_eseo.eseomega.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.lacommande.model.HistoryItem;
import fr.bde_eseo.eseomega.profile.UserProfile;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.EncryptUtils;
import fr.bde_eseo.eseomega.utils.Utilities;

/**
 * Created by Rascafr on 28/07/2015.
 * Affiche les détails de la commande sélectionnée (numéro de commande, prix, etc)
 */
public class OrderDetailsFragment extends Fragment {

    private float oldScreenBrightness;
    private TextView tvOrderDetails, tvOrderPrice, tvOrderDate, tvOrderNumero, tvDesc;
    private ImageView imgCategory;
    private ProgressBar progressBar;
    private RelativeLayout rl1, rl2;
    private int idcmd;
    private static Handler mHandler;
    private static final int RUN_UPDATE = 15000;
    private static final int RUN_START = 100;
    private static boolean run;
    private UserProfile profile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_order_details, container, false);

        // UI
        tvOrderDate = (TextView) rootView.findViewById(R.id.tvCommandDate);
        tvOrderPrice = (TextView) rootView.findViewById(R.id.tvCommandPrice);
        tvOrderDetails = (TextView) rootView.findViewById(R.id.tvOrderDetail);
        tvOrderNumero = (TextView) rootView.findViewById(R.id.tvCommandNumero);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressDetails);
        tvDesc = (TextView) rootView.findViewById(R.id.textView3);
        imgCategory = (ImageView) rootView.findViewById(R.id.imgOrder);
        rl1 = (RelativeLayout) rootView.findViewById(R.id.relativeLayout3);
        rl2 = (RelativeLayout) rootView.findViewById(R.id.relativeLayout5);

        progressBar.setVisibility(View.VISIBLE);
        tvOrderDate.setVisibility(View.INVISIBLE);
        tvOrderPrice.setVisibility(View.INVISIBLE);
        tvOrderDetails.setVisibility(View.INVISIBLE);
        tvOrderNumero.setVisibility(View.INVISIBLE);
        tvDesc.setVisibility(View.INVISIBLE);
        imgCategory.setVisibility(View.INVISIBLE);
        rl1.setVisibility(View.INVISIBLE);
        rl2.setVisibility(View.INVISIBLE);

        // profile
        profile = new UserProfile();
        profile.readProfilePromPrefs(getActivity());

        // Save old brightness level and set it now to 100%
        WindowManager.LayoutParams layout = getActivity().getWindow().getAttributes();
        oldScreenBrightness = layout.screenBrightness;
        layout.screenBrightness = 1F;
        getActivity().getWindow().setAttributes(layout);

        // Delay to update data


        //idcmd = -1; // in case of

        return rootView;
    }

    public void setIdcmd (int idcmd) {
        this.idcmd = idcmd;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Delay to update data
        run = true;

        if (mHandler == null) {
            mHandler = new android.os.Handler();
            mHandler.postDelayed(updateTimerThread, RUN_START);
        } else {
            mHandler.removeCallbacks(updateTimerThread);
            mHandler.postDelayed(updateTimerThread, RUN_START);
        }
    }

    @Override
    public void onPause() {
        if( mHandler != null) {
            mHandler.removeCallbacks(updateTimerThread);
        }
        run = false;
        super.onPause();
    }

    /**
     * Background task to fetch data periodically from server
     */
    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            try {
                if (run) {
                    AsyncDetails async = new AsyncDetails();
                    async.execute();
                    run = false;
                }
            } catch (NullPointerException e) { // Stop handler if fragment disappears
                mHandler.removeCallbacks(updateTimerThread);
                run = false;
            }
        }
    };



    /**
     * Async task to download order details
     */
    private class AsyncDetails extends AsyncTask <String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            run = false;
        }

        @Override
        protected String doInBackground(String... params) {

            if (getActivity() != null) {

                HashMap<String, String> pairs = new HashMap<>();
                pairs.put(getActivity().getResources().getString(R.string.idcmd), String.valueOf(idcmd));
                pairs.put(getActivity().getResources().getString(R.string.username), profile.getId());
                pairs.put(getActivity().getResources().getString(R.string.password), profile.getPassword());
                pairs.put(getActivity().getResources().getString(R.string.hash), EncryptUtils.sha256(getActivity().getResources().getString(R.string.MACRO_SYNC_SINGLE) + String.valueOf(idcmd) + profile.getId() + profile.getPassword()));

                return ConnexionUtils.postServerData(Constants.URL_SYNC_SINGLE, pairs, getActivity());
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (Utilities.isNetworkDataValid(s)) {
                try {
                    JSONArray array = new JSONArray(s);
                    if (array.length() > 0) {
                        JSONObject jsonSync = array.getJSONObject(0);
                        tvOrderDate.setText("Votre commande du\n" + getFrenchDate(jsonSync.getString("datetime")));
                        tvOrderNumero.setText(jsonSync.getString("strcmd") + " " + new DecimalFormat("000").format(jsonSync.getInt("modcmd")));
                        String txtDesc = jsonSync.getString("resume");
                        txtDesc = " - " + txtDesc.replaceAll("<br>", "\n - ");
                        tvOrderDetails.setText(txtDesc);
                        tvOrderPrice.setText(new DecimalFormat("0.00").format(jsonSync.getDouble("price")) + "€");
                        ImageLoader.getInstance().displayImage(Constants.URL_ASSETS + jsonSync.getString("imgurl"), imgCategory);
                        int color = 0, color2 = 0;
                        switch (jsonSync.getInt("status")) {
                            case HistoryItem.STATUS_PREPARING:
                                color = getActivity().getResources().getColor(R.color.circle_preparing);
                                color2 = getActivity().getResources().getColor(R.color.blue_light);
                                break;
                            case HistoryItem.STATUS_DONE:
                                color = getActivity().getResources().getColor(R.color.circle_done);
                                color2 = getActivity().getResources().getColor(R.color.gray_light);
                                break;
                            case HistoryItem.STATUS_READY:
                                color = getActivity().getResources().getColor(R.color.circle_ready);
                                color2 = getActivity().getResources().getColor(R.color.green_light);
                                break;
                            case HistoryItem.STATUS_NOPAID:
                                color = getActivity().getResources().getColor(R.color.circle_error);
                                color2 = getActivity().getResources().getColor(R.color.orange_light);
                                break;
                        }

                        tvOrderDate.setVisibility(View.VISIBLE);
                        tvOrderPrice.setVisibility(View.VISIBLE);
                        tvOrderDetails.setVisibility(View.VISIBLE);
                        tvOrderNumero.setVisibility(View.VISIBLE);
                        tvDesc.setVisibility(View.VISIBLE);
                        imgCategory.setVisibility(View.VISIBLE);
                        rl1.setVisibility(View.VISIBLE);
                        rl2.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);

                        tvOrderPrice.setTextColor(color);
                        tvDesc.setTextColor(color);
                        rl1.setBackgroundColor(color);
                        rl2.setBackgroundColor(color2);
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        tvOrderDate.setVisibility(View.INVISIBLE);
                        tvOrderPrice.setVisibility(View.INVISIBLE);
                        tvOrderDetails.setVisibility(View.INVISIBLE);
                        tvOrderNumero.setVisibility(View.INVISIBLE);
                        tvDesc.setVisibility(View.INVISIBLE);
                        imgCategory.setVisibility(View.INVISIBLE);
                        rl1.setVisibility(View.INVISIBLE);
                        rl2.setVisibility(View.INVISIBLE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                if (getActivity() != null) {

                    Toast.makeText(getActivity(), "Connexion serveur impossible", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            getActivity().onBackPressed();
                        }
                    }, 500);
                }
            }

            mHandler.postDelayed(updateTimerThread, RUN_UPDATE);
            run = true;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        run = false;
        mHandler.removeCallbacks(updateTimerThread);

        WindowManager.LayoutParams layout = getActivity().getWindow().getAttributes();
        layout.screenBrightness = oldScreenBrightness;
        getActivity().getWindow().setAttributes(layout);
    }

    public Date getParsedDate (String strDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE);
        Date date = null;
        try {
            date = format.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public String getFrenchDate (String strDate) {
        Date d = getParsedDate(strDate);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRANCE);
        return sdf.format(d);
    }
}
