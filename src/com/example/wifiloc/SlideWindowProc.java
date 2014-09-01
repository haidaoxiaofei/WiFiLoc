package com.example.wifiloc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.example.interfaces.EstimationListener;
import com.example.structures.AreaPoint;
import com.example.utils.CustomHttpClient;
import com.example.utils.MyJsonResponse;
import com.example.utils.ScanResultBuffer;
import com.google.gson.Gson;

public class SlideWindowProc {
	public static final int WINDOW_SIZE = 5;
	public static final int SLIDE_SIZE = 2;
	private int count = 0;//ap count
	private EstimationListener listener;
	private Map<Long, Integer> macMap = new HashMap<Long, Integer>();
	private List<ArrayList<Integer>> signals = new ArrayList<ArrayList<Integer>>();
    String url = "http://lccpu4.cse.ust.hk/phpLearn/index.php/welcome/IndoorLocalization";
    float medianSignals[] = null;
    long macList[] = null;
    public boolean isLocalizating = false;


	public SlideWindowProc(EstimationListener listener) {
		super();
		this.listener = listener;
	}

	public void produceResult(){
		while (estimateWindowResult()) {
			slideToNextWin();
		}
	}
	
	public boolean slideToNextWin(){
		if (ScanResultBuffer.strengthList.size() - SLIDE_SIZE <= 0) {
			return false;
		}
		for (int i = 0; i < SLIDE_SIZE; i++) {
			ScanResultBuffer.macList.remove(0);
			ScanResultBuffer.strengthList.remove(0);
		}
		
		return true;
	}
	
	public boolean estimateWindowResult(){
		if (ScanResultBuffer.strengthList.size() < WINDOW_SIZE) {
			return false;
		}
		
		reset();
		long[] macTmp;
		float[] strengthTmp;
		for (int i = 0; i < WINDOW_SIZE; i++) {
			macTmp = ScanResultBuffer.macList.get(i);
			strengthTmp = ScanResultBuffer.strengthList.get(i);
			for (int j = 0; j < macTmp.length; j++) {
				if (macMap.containsKey(macTmp[j])) {
					Integer index = (Integer) macMap.get(macTmp[j]);
					signals.get(index).add((int) strengthTmp[j]);
				} else {
					macMap.put(macTmp[j], count);
					signals.add(new ArrayList<Integer>());
					signals.get(count).add((int) strengthTmp[j]);
					count++;
				}
			}
		}
		
		//calculate median strength value for each AP
		medianSignals = new float[macMap.size()];
		macList = new long[macMap.size()];
		Set<Long> macs = macMap.keySet();
		for (Long mac : macs) {
			int index = macMap.get(mac);
			macList[index] = mac;
			List<Integer> tmp = signals.get(index);
			Collections.sort(tmp);
			if (tmp.size() > 0) {
				if (tmp.size() % 2 == 1) {
					medianSignals[index] = tmp.get(tmp.size() / 2);
				} else {
					medianSignals[index] = (float) ((tmp.get(tmp.size() / 2) + tmp.get((tmp.size() - 1) / 2)) / 2.0);
				}
			}
			tmp.clear();
		}

        if (!isLocalizating){
            new RemoteLocalizationTask().execute();
        }


		return true;
	}
	public void reset(){
		count = 0;
		signals.clear();
		macMap.clear();
	}

    public class RemoteLocalizationTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            isLocalizating = true;
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < macList.length; i++){
                sb.append(macList[i]);
                sb.append("#");
            }
            sb.append(";");
            for (int i = 0; i < medianSignals.length; i++){
                sb.append(medianSignals[i]);
                sb.append("#");
            }
           postParameters.add(new BasicNameValuePair("fp", sb.toString()));

            String response = null;
            try {
            	
                response = CustomHttpClient.executeHttpPost(url, postParameters);
                String res = response.toString();
                res = res.replaceAll("\\s+", "");
                MyJsonResponse resp = MyJsonResponse.getResponse(new JSONObject(res));
                String s = resp.getContent();
                if (resp.getType() == MyJsonResponse.TYPES_ENUM.SUCCESS)
                    return s;
                else
                    return null;



            } catch (Exception e) {
                return null;
            }

        }
        @Override
        protected void onPostExecute(final String content) {
            JSONObject jb = null;
            Gson gson = new Gson();
            final AreaPoint ap = gson.fromJson(content, AreaPoint.class);

            listener.updateEstimatedPosition(ap);


            isLocalizating = false;
        }
    }
}
